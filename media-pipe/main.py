import cv2
import mediapipe as mp
from poseapi import add_pose_to_routine, upload_current_routine
from pipeutil import result_to_pose_json
from collections import deque
from physio_tools import calculate_angle, smooth_angle
from display import draw_pose_landmarks, display_text, flip_image  # Import functions from display.py

from physio_tools import ExerciseTracker
from camera_tracking import MotorController

from routines.tracking import TrackingDetail, TrackingType
from routines.workout_config import ExerciseDetail, RoutineConfig   

motor_controller = MotorController()

# -----------------------SETUP ----------------------------------
CONFIDENCE_THRESHOLD = 0.6  # NOTE: Adjust based on desired accuracy of landmark detection
TIME_THRESHOLD = 1.5 # Threshold for sending motor updates

mp_pose = mp.solutions.pose

angle_history = deque(maxlen=5) # Store the last 5 angles

smoothed_angle = 0


# ----------------------- ROUTINE / EXERCISE CONFIGURATION [HARDCODED FOR NOW] -----------------------

# TODO: hardcode the params for each exercise

# tracking details
right_leg_extension_angle_tracking = TrackingDetail(
    tracking_type=TrackingType.ANGLE_OF_THREE_POINTS,
    keypoints=["right_hip", "right_knee", "right_ankle"],
    # show_alert_if_below=0,
    # show_alert_if_above=180,
    # alert_message=""
)

# exercise details
right_leg_extension = ExerciseDetail(
    rep_keypoints=["right_hip", "right_knee", "right_ankle"],
    threshold_flexion=100,
    threshold_extension=130,
    display_name="Leg Extension",
    default_tracking_details=[right_leg_extension_angle_tracking],
)

# Tracking knee angle for squats
squat_angle_tracking = TrackingDetail(
    tracking_type=TrackingType.ANGLE_OF_THREE_POINTS,
    keypoints=["right_hip", "right_knee", "right_ankle"],
    # show_alert_if_below=60,
    # show_alert_if_above=170,
    # alert_message="Maintain proper squat depth!"
)

# Defining the squat exercise
squat_exercise = ExerciseDetail(
    rep_keypoints=["right_hip", "right_knee", "right_ankle"],
    threshold_flexion=110,
    threshold_extension=160,
    display_name="Squat",
    default_tracking_details=[squat_angle_tracking],
)


# config routine
routine_config = RoutineConfig()

routine_config.add_exercise(
    exercise_name="right_leg_extension",
    exercise_detail= right_leg_extension,
    reps=10,
)

routine_config.add_exercise(
    exercise_name="squat",
    exercise_detail=squat_exercise,
    reps=10,
)



# ----------------------- SETUP ROUTINE -----------------------

# Fetch workout details TODO: Make this dynamic
exercise_name = "right_leg_extension"

routine_data = routine_config.get_workout(exercise_name)
if not routine_data:
    raise ValueError(f"Workout '{exercise_name}' not found in routine config.")

exercise_detail = routine_data["Workout"]
tracker = ExerciseTracker(exercise_detail)
custom_tracking_details = routine_data["CustomTrackingDetails"]


# -------------------- FUNCTIONS ---------------------------------
def extract_keypoints_dynamic(landmarks, pose, keypoints):
    """Extract dynamic keypoints for the chosen exercise."""
    return [
        [
            landmarks[getattr(pose, kp.upper()).value].x,
            landmarks[getattr(pose, kp.upper()).value].y,
            landmarks[getattr(pose, kp.upper()).value].z,
        ]
        for kp in keypoints
    ]

def process_exercise_angle(landmarks, pose, exercise_detail: ExerciseDetail):
    keypoints = extract_keypoints_dynamic(landmarks, pose, exercise_detail.rep_keypoints)
    exercise_angle = calculate_angle(keypoints[0], keypoints[1], keypoints[2])
    rep_count, last_rep_duration = tracker.detect_reps(exercise_angle)
    return exercise_angle, rep_count, last_rep_duration

# TODO: Consider refactoring: This function gets visibility scores of ALL points - may be unecessary computation
def get_landmarks_visibility(landmarks, pose_landmark_enum):
    visibility_scores = {}
    for lm_name, lm_value in pose_landmark_enum.__members__.items():
        visibility_scores[lm_name.lower()] = landmarks[lm_value.value].visibility
    return visibility_scores

# ----------------------- LIVE VIDEO CAPTURE -----------------------
cap = cv2.VideoCapture(0)

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

    if results.pose_landmarks:
        # Get landmarks
        landmarks = results.pose_landmarks.landmark
        
        # ----------------------- Camera Tracking -----------------------
        # TODO: Consider dynamic tracking based on the exersize keypoints
        
        all_points = [(landmark.x, landmark.y) for landmark in landmarks] # Extract all lamdmark pts (spatial coordinates)
        h, w, _ = image.shape # Get frame dimensions (pixel coordinates)
        
        pixel_midpoint, offset_x, offset_y = motor_controller.get_offset(all_points, w, h)
        cv2.circle(image, pixel_midpoint, radius=5, color=(0, 0, 255), thickness=-1)

        motor_controller.check_offset_tolerance(offset_x, offset_y)

        # ----------------------- Excersize Processing -----------------------
        # Get visibility scores for all landmarks
        visibility = get_landmarks_visibility(landmarks, mp_pose.PoseLandmark)

        # Check if all keypoints for the current exercise are visible
        keypoints_visible = all(
            visibility.get(kp, 0) > CONFIDENCE_THRESHOLD
            for kp in exercise_detail.rep_keypoints
        )
        
        if keypoints_visible:
            exercise_angle, rep_count, last_rep_duration = process_exercise_angle(
                landmarks, mp_pose.PoseLandmark, exercise_detail
            )
            
            # TODO: Imporve smoothing
            # 5pt local average smoothing
            smoothed_angle = smooth_angle(exercise_angle, angle_history)
            
        for tracking in custom_tracking_details:
            if tracking.tracking_type == TrackingType.ANGLE_OF_THREE_POINTS:
                keypoints = extract_keypoints_dynamic(landmarks, mp_pose.PoseLandmark, tracking.keypoints)
                angle = calculate_angle(*keypoints)
                
                if tracking.show_alert_if_below and angle < tracking.show_alert_if_below:
                    print(tracking.alert_message)
    
    # ---------------------- Display Output ----------------------
    
    # Draw landmarks using the helper function
    draw_pose_landmarks(image, results.pose_landmarks)

    # Flip the image for selfie view
    # flipped_image = flip_image(image)
    flipped_image = image

    # Display text (exercise, reps, time, angle)
    display_text(flipped_image, "Seated Leg Extensions (Right)", tracker.rep_count, tracker.last_rep_duration, smoothed_angle)

    # Show the flipped image
    cv2.imshow('MediaPipe Pose', flipped_image)
    

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
cap.release()