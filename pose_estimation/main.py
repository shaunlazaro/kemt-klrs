from movenet_utils import draw_keypoints, draw_connections, calculate_angle, draw_angle_marker

import subprocess

import numpy as np
import tensorflow as tf
import tensorflow_hub as hub
from tensorflow_docs.vis import embed
import cv2

# Import matplotlib libraries
from matplotlib import pyplot as plt
from matplotlib.collections import LineCollection
import matplotlib.patches as patches

# Some modules to display an animation using imageio.
import imageio
from IPython.display import HTML, display

# List dependency versions
print("numpy: ", np.__version__)
print("tensorflow: ", tf.__version__)
print("tensorflow_hub: ", hub.__version__)
print("cv2: ", cv2.__version__)


model_name = "movenet_thunder" #@param ["movenet_lightning", "movenet_thunder", "movenet_lightning_f16.tflite", "movenet_thunder_f16.tflite", "movenet_lightning_int8.tflite", "movenet_thunder_int8.tflite"]

# TensofFlow Lite models
if "tflite" in model_name:
  if "movenet_lightning_f16" in model_name:
    subprocess.run(["wget", "-q", "-O", "model.tflite", "https://tfhub.dev/google/lite-model/movenet/singlepose/lightning/tflite/float16/4?lite-format=tflite"])
    input_size = 192
  elif "movenet_thunder_f16" in model_name:
    # Download the movenet_thunder_f16 model
    subprocess.run(["wget", "-q", "-O", "model.tflite", "https://tfhub.dev/google/lite-model/movenet/singlepose/thunder/tflite/float16/4?lite-format=tflite"])
    input_size = 256
  elif "movenet_lightning_int8" in model_name:
    # Download the movenet_lightning_int8 model
    subprocess.run(["wget", "-q", "-O", "model.tflite", "https://tfhub.dev/google/lite-model/movenet/singlepose/lightning/tflite/int8/4?lite-format=tflite"])
    input_size = 192
  elif "movenet_thunder_int8" in model_name:
    # Download the movenet_thunder_int8 model
    subprocess.run(["wget", "-q", "-O", "model.tflite", "https://tfhub.dev/google/lite-model/movenet/singlepose/thunder/tflite/int8/4?lite-format=tflite"])
    input_size = 256
  else:
    raise ValueError("Unsupported model name: %s" % model_name)

  # Initialize the TFLite interpreter
  interpreter = tf.lite.Interpreter(model_path="model.tflite")
  interpreter.allocate_tensors()

  def movenet(input_image):
    """Runs detection on an input image.

    Args:
      input_image: A [1, height, width, 3] tensor represents the input image
        pixels. Note that the height/width should already be resized and match the
        expected input resolution of the model before passing into this function.

    Returns:
      A [1, 1, 17, 3] float numpy array representing the predicted keypoint
      coordinates and scores.
    """
    # TF Lite format expects tensor type of uint8.
    input_image = tf.cast(input_image, dtype=tf.uint8)
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()
    interpreter.set_tensor(input_details[0]['index'], input_image.numpy())
    # Invoke inference.
    interpreter.invoke()
    # Get the model prediction.
    keypoints_with_scores = interpreter.get_tensor(output_details[0]['index'])
    return keypoints_with_scores

# TensofFlow models (full)
else:
  if "movenet_lightning" in model_name:
    module = hub.load("https://tfhub.dev/google/movenet/singlepose/lightning/3")
    input_size = 192
  elif "movenet_thunder" in model_name:
    module = hub.load("https://tfhub.dev/google/movenet/singlepose/thunder/4")
    input_size = 256
  else:
    raise ValueError("Unsupported model name: %s" % model_name)

  def movenet(input_image):
    """Runs detection on an input image.

    Args:
      input_image: A [1, height, width, 3] tensor represents the input image
        pixels. Note that the height/width should already be resized and match the
        expected input resolution of the model before passing into this function.

    Returns:
      A [1, 1, 17, 3] float numpy array representing the predicted keypoint
      coordinates and scores.
    """
    model = module.signatures['serving_default']
    # SavedModel format expects tensor type of int32.
    input_image = tf.cast(input_image, dtype=tf.int32)
    # Run model inference.
    outputs = model(input_image)
    # Output is a [1, 1, 17, 3] tensor.
    keypoints_with_scores = outputs['output_0']
    return keypoints_with_scores


# Define the exercises and their key parameters
EXERCISES = {
  "leg_extension": {
      "keypoints": ["left_hip", "left_knee", "left_ankle"],
      # Define key parameters
      "threshold_flexion": 60,
      "threshold_extension": 160,
      "display_name": "Leg Extension"
  },
  "air_squat": {
      "keypoints": ["left_hip", "left_knee", "left_ankle"],
      # Define key parameters
      "threshold_flexion": 90,
      "threshold_extension": 160,
      "display_name": "Air Squat"
  },
  "bicep_curl": {
      "keypoints": ["left_shoulder", "left_elbow", "left_wrist"],
      # Define key parameters
      "threshold_flexion": 50,
      "threshold_extension": 150,
      "display_name": "Bicep Curl"
  },
}

# Exercise-specific processing function
def process_exercise(frame, keypoints, exercise_config, rep_state):
    kp_names = exercise_config["keypoints"]
    kp_indices = [get_keypoint_index(name) for name in kp_names]

    # Extract keypoint positions
    points = [keypoints[idx][:2] for idx in kp_indices]
    confidences = [keypoints[idx, 2] for idx in kp_indices]

    # Check if all keypoints meet the confidence threshold
    if all(conf > threshold for conf in confidences):
        # Calculate the angle
        angle = calculate_angle(*points)

        # Draw markers and angle
        frame = draw_angle_marker(frame, points[1], points[0], points[2], angle)
        
        cv2.putText(img, f'Reps: {rep_state["rep_count"]}', (10, 100),
                    cv2.FONT_HERSHEY_SIMPLEX, 1.5, (0, 255, 0), 2)
        
        # Rep counting logic
        # TODO: add filtering to make this more robust (in case of noisy detections)
        if rep_state["is_flexed"] and angle <= exercise_config["threshold_flexion"]:
            rep_state["is_flexed"] = False
        elif not rep_state["is_flexed"] and angle >= exercise_config["threshold_extension"]:
            rep_state["is_flexed"] = True
            rep_state["rep_count"] += 1

    return frame, rep_state
  
EDGES = {
  (0, 1): 'm',
  (0, 2): 'c',
  (1, 3): 'm',
  (2, 4): 'c',
  (0, 5): 'm',
  (0, 6): 'c',
  (5, 7): 'm',
  (7, 9): 'm',
  (6, 8): 'c',
  (8, 10): 'c',
  (5, 6): 'y',
  (5, 11): 'm',
  (6, 12): 'c',
  (11, 12): 'y',
  (11, 13): 'm',
  (13, 15): 'm',
  (12, 14): 'c',
}     

def get_keypoint_index(keypoint_name):
    keypoint_mapping = {
        "nose": 0,
        "left_eye": 1,
        "right_eye": 2,
        "left_ear": 3,
        "right_ear": 4,
        "left_shoulder": 5,
        "right_shoulder": 6,
        "left_elbow": 7,
        "right_elbow": 8,
        "left_wrist": 9,
        "right_wrist": 10,
        "left_hip": 11,
        "right_hip": 12,
        "left_knee": 13,
        "right_knee": 14,
        "left_ankle": 15,
        "right_ankle": 16
    }
    return keypoint_mapping[keypoint_name]

# Confidence threshold for keypoint detection
threshold = 0.15

# Loads video source (0 is for main webcam)
video_source = 0
cap = cv2.VideoCapture(video_source)

# Checks errors while opening the Video Capture
if not cap.isOpened():
    print('Error loading video')
    quit()

success, img = cap.read()

if not success:
    print('Error reding frame')
    quit()
    
y, x, _ = img.shape

# Initialize variables
selected_exercise = "leg_extension"  # Change as needed
exercise_config = EXERCISES[selected_exercise]
rep_state = {"rep_count": 0, "is_flexed": True}


while success:
    img = cv2.flip(img, 1)
    # A frame of video or an image, represented as an int32 tensor of shape: 256x256x3. Channels order: RGB with values in [0, 255].
    tf_img = cv2.resize(img, (256,256))
    tf_img = cv2.cvtColor(tf_img, cv2.COLOR_BGR2RGB)
    tf_img = np.asarray(tf_img)
    tf_img = np.expand_dims(tf_img,axis=0)

    # Run model inference.
    keypoints = movenet(tf_img) 
    draw_connections(img, keypoints, EDGES, threshold)
    draw_keypoints(img, keypoints, threshold)
    
    keypoints = np.squeeze(np.multiply(keypoints, [y,x,1]))
    
    # Process the selected exercise
    img, rep_state = process_exercise(img, keypoints, exercise_config, rep_state)
    
    cv2.putText(img, f'{exercise_config["display_name"]}', (10, 50),
            cv2.FONT_HERSHEY_SIMPLEX, 1.5, (0, 255, 0), 2)
            
    cv2.putText(img, f'Reps: {rep_state["rep_count"]}', (10, 100),
                    cv2.FONT_HERSHEY_SIMPLEX, 1.5, (0, 255, 0), 2)
    
    # Shows image
    cv2.imshow('Movenet', img)
    # Waits for the next frame, checks if q was pressed to quit
    if cv2.waitKey(1) == ord("q"):
        break

    # Reads next frame
    success, img = cap.read()

cap.release()
cv2.destroyAllWindows()
for i in range(2):
    cv2.waitKey(1)