import cv2
import mediapipe as mp

# Initialize the MediaPipe drawing utils
mp_drawing = mp.solutions.drawing_utils
mp_drawing_styles = mp.solutions.drawing_styles
mp_pose = mp.solutions.pose

# Constants for cv2.putText styling
TEXT_COLOR = (255, 255, 255)  # White color for text
FONT = cv2.FONT_HERSHEY_SIMPLEX  # Font type
FONT_SCALE = 1  # Font size (scale)
THICKNESS = 2  # Thickness of the text
LINE_TYPE = cv2.LINE_AA  # Line type for smooth text

def draw_pose_landmarks(image, landmarks):
    """Draw the pose landmarks on the image."""
    mp_drawing.draw_landmarks(
        image,
        landmarks,
        mp_pose.POSE_CONNECTIONS,
        landmark_drawing_spec=mp_drawing_styles.get_default_pose_landmarks_style()
    )

def display_text(image, exercise_name, body_alignment, rep_count, last_rep_duration, exercise_angle, alert):
    """Display text on the image."""
        
    # Display exercise name
    cv2.putText(image, f"Exercise: {exercise_name}", (50, 50), FONT, FONT_SCALE, TEXT_COLOR, THICKNESS, LINE_TYPE)
    # Display expected body position
    cv2.putText(image, f"Body Alignment: {body_alignment}", (50, 100), FONT, FONT_SCALE, TEXT_COLOR, THICKNESS, LINE_TYPE)
    # Display reps count
    cv2.putText(image, f"Reps: {rep_count}", (50, 150), FONT, FONT_SCALE, TEXT_COLOR, THICKNESS, LINE_TYPE)
    # Display last rep duration
    cv2.putText(image, f"Last Rep Time: {last_rep_duration:.2f} sec", (50, 200), FONT, FONT_SCALE, TEXT_COLOR, THICKNESS, LINE_TYPE)
    # Display knee angle
    cv2.putText(image, f"Angle: {exercise_angle:.0f} deg", (50, 250), FONT, FONT_SCALE, TEXT_COLOR, THICKNESS, LINE_TYPE)
    # Display alert message
    if alert:
        cv2.putText(image, f"{alert}", (50, 300), FONT, FONT_SCALE, TEXT_COLOR, THICKNESS, LINE_TYPE)

def flip_image(image):
    """Flip the image horizontally (for selfie view)."""
    return cv2.flip(image, 0)
