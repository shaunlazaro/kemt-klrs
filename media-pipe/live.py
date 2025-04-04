import cv2
import mediapipe as mp
import numpy as np
import time
import serial
import math
from poseapi import add_pose_to_routine, upload_current_routine
from pipeutil import result_to_pose_json
from collections import deque


CONFIDENCE_THRESHOLD = 0.6  # Adjust based on desired accuracy

# ----------------------------- CAMERA-TRACKING ----------------------------------
# Define frame dimensions and tolerance range
FRAME_WIDTH = 640
FRAME_HEIGHT = 480

TOLERANCE = 50  # Pixels from the center where no updates are sent
MAX_MOVE_DISTANCE = 25 # Move no more than 10 units.
TIME_THRESHOLD = 2

# Initialize motors at center positions
motor_x = 90  # Horizontal motor
motor_y = 45  # Vertical motor

last_sent = time.time()


try:
    ser = serial.Serial('COM3', 115200)
except Exception as e:
   ser = None
   print(f"Error initializing serial:\n{e}\n")

mp_drawing = mp.solutions.drawing_utils
mp_drawing_styles = mp.solutions.drawing_styles
mp_pose = mp.solutions.pose

if ser:
    ser.setRTS(False)
    ser.setDTR(False)
    msg = "{},{}".format(motor_x,motor_y)
    ser.write(msg.encode('utf-8'))
    
# ---------------------------------------------------------------------------------


knee_visible = False
angle_history = deque(maxlen=5)  # Store the last 5 angles

def smooth_angle(knee_angle):
    angle_history.append(knee_angle)
    return sum(angle_history) / len(angle_history)


# Parameters
flexion_threshold = 90  # Fully bent position
extension_threshold = 160  # Fully extended position
rep_count = 0
state = "rest"  # Can be 'flexing', 'extending', or 'rest'

# Initialize variables for repetition timing
rep_start_time = None
last_rep_duration = 0  # Duration of the last completed repetition in seconds

def detect_reps(knee_angle):
    """
    Detect repetitions and measure the time taken for each repetition.
    """
    global rep_count, state, rep_start_time, last_rep_duration

    current_time = time.time()

    if state == "rest" and knee_angle < flexion_threshold:
        state = "flexing"
        rep_start_time = current_time  # Start timing when flexing begins
    elif state == "flexing" and knee_angle > extension_threshold:
        state = "extending"
        if rep_start_time is not None:
            last_rep_duration = current_time - rep_start_time  # Calculate the time for the rep
        rep_count += 1  # Count a full rep
    elif state == "extending" and knee_angle < flexion_threshold:
        state = "flexing"
        rep_start_time = current_time  # Restart the timer for the next rep

    return rep_count, last_rep_duration
    
def calculate_angle(a, b, c):
    """
    Calculate the angle between three points.
    a, b, c: Each point is a tuple (x, y, z). 'b' is the vertex point.
    Returns the angle in degrees.
    """
    a = np.array(a)  # Point 1
    b = np.array(b)  # Vertex point
    c = np.array(c)  # Point 2

    # Calculate vectors
    ba = a - b
    bc = c - b

    # Calculate cosine of angle
    cosine_angle = np.dot(ba, bc) / (np.linalg.norm(ba) * np.linalg.norm(bc))
    angle = np.arccos(cosine_angle)  # Radians

    return np.degrees(angle)  # Convert to degrees

# For webcam input:
cap = cv2.VideoCapture(1)

FRAME_WIDTH = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
FRAME_HEIGHT = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
print("frame width: ", FRAME_WIDTH)
print("frame height: ", FRAME_HEIGHT)

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
    
    knee_angle = 0
    midpoint = (0, 0)


    if results.pose_landmarks:
        # Get landmarks
        landmarks = results.pose_landmarks.landmark
        all_points = [(landmark.x, landmark.y) for landmark in landmarks]

        # Calculate the midpoint of all landmarks
        x_coords, y_coords = zip(*all_points)  # Separate x and y coordinates
        midpoint = (sum(x_coords) / len(x_coords), sum(y_coords) / len(y_coords))

        # Convert midpoint to pixel coordinates
        h, w, _ = image.shape
        pixel_midpoint = (int(midpoint[0] * w), int(midpoint[1] * h))
        #print(pixel_midpoint)

        # Draw a circle at the midpoint
        cv2.circle(image, pixel_midpoint, radius=5, color=(0, 0, 255), thickness=-1)
        # cv2.putText(image, f"Midpoint: {pixel_midpoint}", (pixel_midpoint[0] + 10, pixel_midpoint[1]),
        #             cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)
        
        midpoint_x = pixel_midpoint[0]
        midpoint_y = pixel_midpoint[1]
        
        # Calculate offsets from frame center
        offset_x = midpoint_x - FRAME_WIDTH / 2
        offset_y = midpoint_y - FRAME_HEIGHT / 2
        
        # print("Offset x: ", offset_x)
        # print("Offset y: ", offset_y)
        
        
        center_frame = (FRAME_WIDTH / 2, FRAME_HEIGHT / 2)
        
        cur = time.time()
        if last_sent and cur - last_sent > TIME_THRESHOLD:
        # Check if offsets exceed tolerance
            if abs(offset_x) > TOLERANCE or abs(offset_y) > TOLERANCE:
                # Update motor positions
                if offset_x > TOLERANCE and motor_x > 0:
                    motor_x -= min(round(offset_x/10), MAX_MOVE_DISTANCE)
                elif offset_x < -TOLERANCE and motor_x < 180:
                    motor_x -= max(round(offset_x/10), -MAX_MOVE_DISTANCE)

                if offset_y > TOLERANCE and motor_y > 0:
                    motor_y -= min(round(offset_y/10), MAX_MOVE_DISTANCE)
                elif offset_y < -TOLERANCE and motor_y < 180:
                    motor_y -= max(round(offset_y/10), -MAX_MOVE_DISTANCE)

                # Send motor commands (replace with actual communication code)
                print(f"x:{offset_x} y:{offset_y}")
                print(f"Updating motors to: X={motor_x}, Y={motor_y}")
                msg = "{},{}".format(motor_x,motor_y)
                if ser:
                    ser.write(msg.encode('utf-8'))

                last_sent = time.time()

        # Get landmark confidence values
        hip_visibility = landmarks[mp_pose.PoseLandmark.RIGHT_HIP.value].visibility
        knee_visibility = landmarks[mp_pose.PoseLandmark.RIGHT_KNEE.value].visibility
        ankle_visibility = landmarks[mp_pose.PoseLandmark.RIGHT_ANKLE.value].visibility
        
        if (
        hip_visibility > CONFIDENCE_THRESHOLD and
        knee_visibility > CONFIDENCE_THRESHOLD and
        ankle_visibility > CONFIDENCE_THRESHOLD
        ):
            knee_visible = True
            right_hip = [landmarks[mp_pose.PoseLandmark.RIGHT_HIP.value].x, 
                        landmarks[mp_pose.PoseLandmark.RIGHT_HIP.value].y,
                        landmarks[mp_pose.PoseLandmark.RIGHT_HIP.value].z]
            right_knee = [landmarks[mp_pose.PoseLandmark.RIGHT_KNEE.value].x, 
                        landmarks[mp_pose.PoseLandmark.RIGHT_KNEE.value].y,
                        landmarks[mp_pose.PoseLandmark.RIGHT_KNEE.value].z]
            right_ankle = [landmarks[mp_pose.PoseLandmark.RIGHT_ANKLE.value].x, 
                        landmarks[mp_pose.PoseLandmark.RIGHT_ANKLE.value].y,
                        landmarks[mp_pose.PoseLandmark.RIGHT_ANKLE.value].z]

            # Calculate angle
            knee_angle = calculate_angle(right_hip, right_knee, right_ankle)
            reps = detect_reps(knee_angle)

            # Display on image
        
            flipped_image = cv2.flip(image, 1)
            cv2.putText(image, f"Reps: {reps}", (50, 50), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255), 2)
            cv2.putText(image, f"Knee Angle: {int(knee_angle)}", (50, 100), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255), 2)
            image = cv2.flip(flipped_image, 1)
            
        
    # Draw landmarks
    mp_drawing.draw_landmarks(
        image,
        results.pose_landmarks,
        mp_pose.POSE_CONNECTIONS,
        landmark_drawing_spec=mp_drawing_styles.get_default_pose_landmarks_style())

    # Flip horizontally for a selfie view
    flipped_image = cv2.flip(image, 1)

    # Flip the text so it is readable
    # cv2.putText(flipped_image, f"Midpoint: {pixel_midpoint}", (pixel_midpoint[0] + 10, pixel_midpoint[1]),
    #                cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)
    # cv2.putText(flipped_image, f'Knee Angle: {int(knee_angle)} deg', 
    #         (50, 50), 
    #         cv2.FONT_HERSHEY_SIMPLEX, 
    #         1, (255, 255, 255), 2, cv2.LINE_AA)
    cv2.putText(flipped_image, f"Exersize: Seated Leg Extensions (Right)", (50, 50), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255), 2)
    cv2.putText(flipped_image, f"Reps: {rep_count}", (50, 100), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255), 2)
    cv2.putText(flipped_image, f"Last Rep Time: {last_rep_duration:.2f} sec", (50, 150), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255), 2)
    if (knee_visible):
        cv2.putText(flipped_image, f"Knee Angle: {int(smooth_angle(knee_angle))}", (50, 200), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255), 2)
    print("knee: ", knee_visible)
    knee_visible = False

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