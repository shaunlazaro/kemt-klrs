import cv2
import json
import mediapipe as mp
from poseapi import add_pose_to_routine, upload_current_routine
from pipeutil import result_to_pose_json
from collections import deque
from physio_tools import calculate_three_point_angle, calculate_two_point_vertical_angle, calculate_two_point_horizontal_angle, smooth_angle
from display import draw_pose_landmarks, display_text, flip_image  # Import functions from display.py
from scoring import score_routine_component



from physio_tools import ExerciseTracker
from camera_tracking import MotorController

from routines.tracking import TrackingDetail, TrackingType
from routines.workout_config import ExerciseDetail, RoutineConfig, RoutineComponent  
from routines.workout_data import RoutineData, RoutineComponentData

motor_controller = MotorController()

# -----------------------SETUP ----------------------------------
CONFIDENCE_THRESHOLD = 0.6  # NOTE: Adjust based on desired accuracy of landmark detection
TIME_THRESHOLD = 1.5 # Threshold for sending motor updates

mp_pose = mp.solutions.pose

angle_history = deque(maxlen=5) # Store the last 5 angles

smoothed_angle = 0


# ----------------------- ROUTINE CONFIGURATION [HARDCODED FOR NOW] -----------------------


# tracking details
right_leg_extension_angle_tracking = TrackingDetail(
    tracking_type=TrackingType.ANGLE_OF_THREE_POINTS,
    keypoints=["right_hip", "right_knee", "right_ankle"],
    dimensionality="2D",
    goal_extension=160,
    # show_alert_if_below=0,
    # show_alert_if_above=180,
    alert_message="Extend further!"
)

# exercise details
right_leg_extension = ExerciseDetail(
    rep_keypoints=["right_hip", "right_knee", "right_ankle"],
    rep_tracking=right_leg_extension_angle_tracking,
    start_angle=90,
    min_rep_time=1.5,
    threshold_flexion=100,
    threshold_extension=130,
    display_name="Leg Extension",
    start_in_flexion=True,
    body_alignment="side",
    default_tracking_details=[right_leg_extension_angle_tracking],
)

# Tracking knee angle for squats
squat_angle_tracking = TrackingDetail(
    tracking_type=TrackingType.ANGLE_OF_THREE_POINTS,
    keypoints=["hip", "knee", "ankle"],
    symmetric=True,
    dimensionality="2D",
    goal_flexion=90,
    # show_alert_if_below=60,
    # show_alert_if_above=170,
    alert_message="Go deeper!"
)

# Tracking trunk posture for squats
squat_posture_tracking = TrackingDetail(
    tracking_type=TrackingType.ANGLE_WITH_VERTICAL,
    keypoints=["hip", "shoulder"],
    symmetric=True,
    dimensionality="2D",
    # goal_flexion=50,
    # show_alert_if_below=60,
    show_alert_if_above=50,
    alert_message="Keep your back more upright!"
)

# Defining the squat exercise
squat_exercise = ExerciseDetail(
    rep_keypoints=["right_hip", "right_knee", "right_ankle"],
    rep_tracking=squat_angle_tracking,
    start_angle=180,
    min_rep_time=1.5,
    threshold_flexion=110, # TODO: move to trackingDetail
    threshold_extension=160, # TODO: move to trackingDetail
    display_name="Squat",
    start_in_flexion=False,
    body_alignment="side",
    default_tracking_details=[squat_angle_tracking, squat_posture_tracking],
)

# config routine components
routine_component_right_leg_extension = RoutineComponent(
    exercise=right_leg_extension,
    reps=3,
    custom_tracking_details=[]     
)

routine_component_squat = RoutineComponent(
    exercise=squat_exercise,
    reps=3,
    custom_tracking_details=[]     
)

# config routine
routine_config = RoutineConfig(
    name="knee_routine",
    exercises=[routine_component_squat, routine_component_right_leg_extension],
    injury="knee"
)


# ----------------------- SETUP DATA CAPTURE -----------------------

# Initialize the list to store all RoutineComponentData
routine_data = RoutineData(
    routineConfig=routine_config,
    routineComponentData=[]
)

# Define current exercise index (progressing through the routine)
current_exercise_index = 0

# Initialize the RoutineComponentData list for each exercise
routine_component_data_list = []


# -------------------- FUNCTIONS ---------------------------------
# TODO: pass in frame width and height as params
def extract_keypoints_dynamic(landmarks, pose, keypoints, mode="2D"):
    """Extract keypoints dynamically for the chosen exercise in 2D or 3D."""
    
    if mode == "3D" and (frame_width is None or frame_height is None):
        raise ValueError("Frame width and height must be provided for 3D mode.")

    extracted_points = []

    for kp in keypoints:
        landmark = landmarks[getattr(pose, kp.upper()).value]

        if mode == "3D": # 2D mode
            # Use the first keypoint as reference depth
            ref_z = landmarks[getattr(pose, keypoints[0].upper()).value].z
            extracted_points.append([
                landmark.x * frame_width, # Convert x to pixel space
                landmark.y * frame_height, # Convert y to pixel space
                (landmark.z - ref_z) # Relative depth, unscaled
            ])
        elif mode == "2D": # 2D mode
            extracted_points.append([
                landmark.x * frame_width, # Convert x to pixel space
                landmark.y * frame_height # Convert y to pixel space
            ])

    return extracted_points

def detect_leading_side(landmarks, pose):
    """Determines whether the left or right leg is leading based on x-coordinates of knees."""
    right_knee_z = landmarks[pose.RIGHT_KNEE].z
    left_knee_z = landmarks[pose.LEFT_KNEE].z
    return "right" if right_knee_z < left_knee_z else "left"

# TODO: Right now this just checks the excersize rep angle and first tracking detail
def process_exercise_metrics(landmarks, pose, exercise_detail: ExerciseDetail):
    tracking_results = []  # List of dictionaries
    
    for tracking_detail in exercise_detail.default_tracking_details:
        keypoints = []

        # Determine whether to use left or right side
        if tracking_detail.symmetric:
            # Determine whether to use left or right side
            side = detect_leading_side(landmarks, pose)  
            keypoints = [f"{side}_{kp}" for kp in tracking_detail.keypoints]
            
            if tracking_detail == exercise_detail.rep_tracking:
                exercise_detail.rep_keypoints = keypoints
        else:
            keypoints = tracking_detail.keypoints
           
        extracted_landmarks = extract_keypoints_dynamic(
            landmarks, pose, keypoints, tracking_detail.dimensionality)

        if tracking_detail.tracking_type == TrackingType.ANGLE_OF_THREE_POINTS:
            exercise_angle = calculate_three_point_angle(extracted_landmarks[0], extracted_landmarks[1], extracted_landmarks[2])
        elif tracking_detail.tracking_type == TrackingType.ANGLE_WITH_VERTICAL:
            exercise_angle = calculate_two_point_vertical_angle(extracted_landmarks[0], extracted_landmarks[1])
        elif tracking_detail.tracking_type == TrackingType.ANGLE_WITH_HORIZONTAL:
            exercise_angle = calculate_two_point_horizontal_angle(extracted_landmarks[0], extracted_landmarks[1])
        else:
            raise ValueError(f"Unsupported tracking type: {tracking_detail.tracking_type}")

        # Store results as a dictionary per tracking detail
        tracking_results.append({"detail": tracking_detail, "angle": exercise_angle})
    
    return tracking_results  # Now returns a list of dictionaries

# TODO: Consider refactoring: This function gets visibility scores of ALL points - may be unecessary computation
def get_landmarks_visibility(landmarks, pose_landmark_enum):
    visibility_scores = {}
    for lm_name, lm_value in pose_landmark_enum.__members__.items():
        visibility_scores[lm_name.lower()] = landmarks[lm_value.value].visibility
    return visibility_scores

# ----------------------- LIVE VIDEO CAPTURE -----------------------
cap = cv2.VideoCapture(0)
if not cap.isOpened():
    print("Error: Could not open video capture.")
    exit()
    
frame_width, frame_height = int(cap.get(3)), int(cap.get(4))

# Iterate through each exercise in the routine
for routine_component in routine_config.exercises:
    exercise_detail = routine_component.exercise
    tracker = ExerciseTracker(exercise_detail)  # Track exercise progress
    
    # Initialize rep data list for this specific exercise
    rep_data_list = []
    
    # Start processing the exercise
    print(f"Starting exercise: {exercise_detail.display_name}")

    with mp_pose.Pose(
        min_detection_confidence=CONFIDENCE_THRESHOLD,
        min_tracking_confidence=CONFIDENCE_THRESHOLD) as pose:
        while cap.isOpened():
            success, image = cap.read()
            if not success:
                print("Ignoring empty camera frame.")
                continue

            # To improve performance, mark the image as not writeable.
            image.flags.writeable = False
            image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
            results = pose.process(image)

            # Draw the pose annotation on the image.
            image.flags.writeable = True
            image = cv2.cvtColor(image, cv2.COLOR_RGB2BGR)

            rep_data = None
            
            if results.pose_landmarks:
                landmarks = results.pose_landmarks.landmark
                visibility = get_landmarks_visibility(landmarks, mp_pose.PoseLandmark)
                
                # ----------------------- Camera Tracking -----------------------
                # TODO: Consider dynamic tracking based on the exersize keypoints
                
                all_points = [(landmark.x, landmark.y) for landmark in landmarks] # Extract all lamdmark pts (spatial coordinates)
                h, w, _ = image.shape # Get frame dimensions (pixel coordinates)
                
                pixel_midpoint, offset_x, offset_y = motor_controller.get_offset(all_points, w, h)
                cv2.circle(image, pixel_midpoint, radius=5, color=(0, 0, 255), thickness=-1)

                motor_controller.check_offset_tolerance(offset_x, offset_y)

                # ----------------------- Excersize Processing -----------------------
            
                # TODO: make this check all keypoints involved (not just for rep detection)
                # Check rep detection keypoints for the current exercise are visible
                # Note: this isn't optimized for symmetric exercises
                
                # Update rep tracking keypoints for symmetric exercises
                if exercise_detail.rep_tracking.symmetric:
                    side = detect_leading_side(landmarks, mp_pose.PoseLandmark)  
                    exercise_detail.rep_keypoints = [f"{side}_{kp}" for kp in exercise_detail.rep_tracking.keypoints]
                
                keypoints_visible = all(
                    visibility.get(kp, 0) > CONFIDENCE_THRESHOLD
                    for kp in exercise_detail.rep_keypoints
                )
                
                
                if keypoints_visible:
                    tracking_results = process_exercise_metrics(
                        landmarks, mp_pose.PoseLandmark, exercise_detail
                    )

                    rep_data = tracker.detect_reps(tracking_results, exercise_detail, result_to_pose_json(results))
                    if rep_data:
                        rep_data_list.append(rep_data)

                    # Extract the rep tracking angle dynamically
                    rep_tracking_angle = next(
                        (entry["angle"] for entry in tracking_results if entry["detail"] == exercise_detail.rep_tracking),
                        None
                    )

                    # TODO: Improve smoothing (5-point local average)
                    smoothed_angle = smooth_angle(rep_tracking_angle, angle_history)

            # Extract latest rep data if available
            rep_count = tracker.rep_count
            last_rep_duration = tracker.last_rep_duration
            alert_message = ", ".join(tracker.alerts) if tracker.alerts else None
            concurrent_score = tracker.score
                
            # ---------------------- Display Output ----------------------
            
            # Draw landmarks using the helper function
            draw_pose_landmarks(image, results.pose_landmarks)

            # Flip the image for selfie view
            # flipped_image = flip_image(image)
            flipped_image = image
            
            # Display text (exercise, reps, time, angle)
            display_text(flipped_image, exercise_detail.display_name, exercise_detail.body_alignment, 
                rep_count, last_rep_duration, smoothed_angle, concurrent_score, alert_message)

            # Show the flipped image
            cv2.imshow('MediaPipe Pose', flipped_image)
            
            # ---------------------- Handle Routine Progression ----------------------
            
            # Check if the current exercise is finished
            if rep_data and rep_data.rep_number >= routine_component.reps:
                print(f"{exercise_detail.display_name} completed.")
                break  # End the current exercise

            # Exit with the ESC key
            keypressed = cv2.waitKey(5)
            KEYCODE_ESC = 27
            KEYCODE_ENTER = 13
            if keypressed & 0xFF == KEYCODE_ESC:
                upload_current_routine()
                break
            elif keypressed & 0xFF == KEYCODE_ENTER and results.pose_landmarks:
                n = add_pose_to_routine(result_to_pose_json(results))
                print(f"Saved pose into routine, {n} poses currently saved.")
        
        routine_component_data = RoutineComponentData(routine_component=routine_component, rep_data=rep_data_list)
        routine_component_data_list.append(routine_component_data)
        
cap.release()

routine_data.routineComponentData = routine_component_data_list


# ----------------------- SAVE ROUTINE DATA TO JSON (OPTIONAL) -----------------------
# Convert routine_data to a dictionary
routine_data_dict = routine_data.to_dict()
with open("routine_data.json", "w") as json_file:
    json.dump(routine_data_dict, json_file, indent=4)
print("Routine data saved to routine_data.json")


    
# Calc the score for each exercise in the routine
for routine_component_data in routine_data.routineComponentData:
    print("routien component data: ", routine_component_data)
    print(f"Scoring for {routine_component_data.routine_component.exercise.display_name}:")
    
    score, avg_peak_angle, target_range_of_motion, warning = score_routine_component(routine_component_data)
    
    print(f"Score: {score:.2f}")
    print(f"Avg peak angle: {avg_peak_angle:.2f}")
    print(f"Target range of motion: {target_range_of_motion}")
    print(f"Warning: {warning}")
        
