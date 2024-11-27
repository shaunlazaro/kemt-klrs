import cv2
import mediapipe as mp
import numpy as np

mp_drawing = mp.solutions.drawing_utils
mp_drawing_styles = mp.solutions.drawing_styles
mp_pose = mp.solutions.pose
    
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
cap = cv2.VideoCapture(0)
with mp_pose.Pose(
    min_detection_confidence=0.5,
    min_tracking_confidence=0.5) as pose:
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

    if results.pose_landmarks:
        # Get landmarks
        landmarks = results.pose_landmarks.landmark
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
        
    # Draw landmarks
    mp_drawing.draw_landmarks(
        image,
        results.pose_landmarks,
        mp_pose.POSE_CONNECTIONS,
        landmark_drawing_spec=mp_drawing_styles.get_default_pose_landmarks_style())

    # Flip horizontally for a selfie view
    flipped_image = cv2.flip(image, 1)
    # Flip the text so it is readable
    cv2.putText(flipped_image, f'Knee Angle: {int(knee_angle)} deg', 
            (50, 50), 
            cv2.FONT_HERSHEY_SIMPLEX, 
            1, (255, 255, 255), 2, cv2.LINE_AA)
    # Show the flipped image
    cv2.imshow('MediaPipe Pose', flipped_image)

    # Exit with the ESC key
    if cv2.waitKey(5) & 0xFF == 27:
      break
cap.release()