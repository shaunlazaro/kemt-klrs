######## Webcam Object Detection Using Tensorflow-trained Classifier #########
#
# Author: Evan Juraes and Ethan Dell
# Date: 10/27/19 & 1/30/2021
# Description: 
# This program uses a TensorFlow Lite model to perform object detection on a live webcam
# feed. It draws boxes and scores around the objects of interest in each frame from the
# webcam. To improve FPS, the webcam object runs in a separate thread from the main program.
# This script will work with either a Picamera or regular USB webcam.
#
# This code is based off the TensorFlow Lite image classification example at:
# https://github.com/tensorflow/tensorflow/blob/master/tensorflow/lite/examples/python/label_image.py
#
# I added my own method of drawing boxes and labels using OpenCV.
############ Credit to Evan for writing this script. I modified it to work with the PoseNet model.##### 

# Import packages
import os
import argparse
import cv2
import numpy as np
import sys
import pdb
import time
import math
import pathlib
from threading import Thread
import importlib.util
import datetime

import time
import RPi.GPIO as GPIO

GPIO.setmode(GPIO.BCM)
#led
GPIO.setup(4, GPIO.OUT)
#button
GPIO.setup(17, GPIO.IN, pull_up_down=GPIO.PUD_UP)


# Define VideoStream class to handle streaming of video from webcam in separate processing thread
# Source - Adrian Rosebrock, PyImageSearch: https://www.pyimagesearch.com/2015/12/28/increasing-raspberry-pi-fps-with-python-and-opencv/
class VideoStream:
    """Camera object that controls video streaming from the Picamera"""
    def __init__(self,resolution=(640,480),framerate=30):
        # Initialize the PiCamera and the camera image stream
        #breakpoint()
        
        self.stream = cv2.VideoCapture(0)
        print("Camera initiated.")
        ret = self.stream.set(cv2.CAP_PROP_FOURCC, cv2.VideoWriter_fourcc(*'MJPG'))
        ret = self.stream.set(3,resolution[0])
        ret = self.stream.set(4,resolution[1])
            
        # Read first frame from the stream
        (self.grabbed, self.frame) = self.stream.read()

    # Variable to control when the camera is stopped
        self.stopped = False

    def start(self):
    # Start the thread that reads frames from the video stream
        Thread(target=self.update,args=()).start()
        return self

    def update(self):
        # Keep looping indefinitely until the thread is stopped
        while True:
            # If the camera is stopped, stop the thread
            if self.stopped:
                # Close camera resources
                self.stream.release()
                return

            # Otherwise, grab the next frame from the stream
            (self.grabbed, self.frame) = self.stream.read()

    def read(self):
    # Return the most recent frame
        return self.frame

    def stop(self):
    # Indicate that the camera and thread should be stopped
        self.stopped = True

# Define and parse input arguments
parser = argparse.ArgumentParser()
parser.add_argument('--modeldir', help='Folder the .tflite file is located in',
                    required=True)
parser.add_argument('--graph', help='Name of the .tflite file, if different than detect.tflite',
                    default='detect.tflite')
parser.add_argument('--labels', help='Name of the labelmap file, if different than labelmap.txt',
                    default='labelmap.txt')
parser.add_argument('--threshold', help='Minimum confidence threshold for displaying detected keypoints (specify between 0 and 1).',
                    default=0.5)
parser.add_argument('--resolution', help='Desired webcam resolution in WxH. If the webcam does not support the resolution entered, errors may occur.',
                    default='1280x720')
parser.add_argument('--edgetpu', help='Use Coral Edge TPU Accelerator to speed up detection',
                    action='store_true')
parser.add_argument('--output_path', help="Where to save processed imges from pi.",
                    required=True)

args = parser.parse_args()

MODEL_NAME = args.modeldir
GRAPH_NAME = args.graph
LABELMAP_NAME = args.labels
min_conf_threshold = float(args.threshold)
resW, resH = args.resolution.split('x')
imW, imH = int(resW), int(resH)
use_TPU = args.edgetpu

# Import TensorFlow libraries
# If tensorflow is not installed, import interpreter from tflite_runtime, else import from regular tensorflow
# If using Coral Edge TPU, import the load_delegate library
pkg = importlib.util.find_spec('tensorflow')
if pkg is None:
    from tflite_runtime.interpreter import Interpreter
    if use_TPU:
        from tflite_runtime.interpreter import load_delegate
else:
    from tensorflow.lite.python.interpreter import Interpreter
    if use_TPU:
        from tensorflow.lite.python.interpreter import load_delegate

# If using Edge TPU, assign filename for Edge TPU model
if use_TPU:
    # If user has specified the name of the .tflite file, use that name, otherwise use default 'edgetpu.tflite'
    if (GRAPH_NAME == 'detect.tflite'):
        GRAPH_NAME = 'edgetpu.tflite'       

# Get path to current working directory
CWD_PATH = os.getcwd()

# Path to .tflite file, which contains the model that is used for object detection
PATH_TO_CKPT = os.path.join(CWD_PATH,MODEL_NAME)


# If using Edge TPU, use special load_delegate argument
if use_TPU:
    interpreter = Interpreter(model_path=PATH_TO_CKPT,
                              experimental_delegates=[load_delegate('libedgetpu.so.1.0')])
    print(PATH_TO_CKPT)
else:
    interpreter = Interpreter(model_path=PATH_TO_CKPT)
interpreter.allocate_tensors()

# Get model details
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()
height = input_details[0]['shape'][1]
width = input_details[0]['shape'][2]
#set stride to 32 based on model size
output_stride = 32

led_on = False
floating_model = (input_details[0]['dtype'] == np.float32)

input_mean = 127.5
input_std = 127.5

def mod(a, b):
    """find a % b"""
    floored = np.floor_divide(a, b)
    return np.subtract(a, np.multiply(floored, b))

def sigmoid(x):
    """apply sigmoid actiation to numpy array"""
    return 1/ (1 + np.exp(-x))
    
def sigmoid_and_argmax2d(inputs, threshold):
    """return y,x coordinates from heatmap"""
    #v1 is 9x9x17 heatmap
    v1 = interpreter.get_tensor(output_details[0]['index'])[0]
    height = v1.shape[0]
    width = v1.shape[1]
    depth = v1.shape[2]
    reshaped = np.reshape(v1, [height * width, depth])
    reshaped = sigmoid(reshaped)
    #apply threshold
    reshaped = (reshaped > threshold) * reshaped
    coords = np.argmax(reshaped, axis=0)
    yCoords = np.round(np.expand_dims(np.divide(coords, width), 1)) 
    xCoords = np.expand_dims(mod(coords, width), 1) 
    return np.concatenate([yCoords, xCoords], 1)

def get_offset_point(y, x, offsets, keypoint, num_key_points):
    """get offset vector from coordinate"""
    y_off = offsets[y,x, keypoint]
    x_off = offsets[y,x, keypoint+num_key_points]
    return np.array([y_off, x_off])
    

def get_offsets(output_details, coords, num_key_points=17):
    """get offset vectors from all coordinates"""
    offsets = interpreter.get_tensor(output_details[1]['index'])[0]
    offset_vectors = np.array([]).reshape(-1,2)
    for i in range(len(coords)):
        heatmap_y = int(coords[i][0])
        heatmap_x = int(coords[i][1])
        #make sure indices aren't out of range
        if heatmap_y >8:
            heatmap_y = heatmap_y -1
        if heatmap_x > 8:
            heatmap_x = heatmap_x -1
        offset_vectors = np.vstack((offset_vectors, get_offset_point(heatmap_y, heatmap_x, offsets, i, num_key_points)))  
    return offset_vectors

def draw_lines(keypoints, image, bad_pts):
    """connect important body part keypoints with lines"""
    #color = (255, 0, 0)
    color = (0, 255, 0)
    thickness = 2
    #refernce for keypoint indexing: https://www.tensorflow.org/lite/models/pose_estimation/overview
    body_map = [[5,6], [5,7], [7,9], [5,11], [6,8], [8,10], [6,12], [11,12], [11,13], [13,15], [12,14], [14,16]]
    for map_pair in body_map:
        #print(f'Map pair {map_pair}')
        if map_pair[0] in bad_pts or map_pair[1] in bad_pts:
            continue
        start_pos = (int(keypoints[map_pair[0]][1]), int(keypoints[map_pair[0]][0]))
        end_pos = (int(keypoints[map_pair[1]][1]), int(keypoints[map_pair[1]][0]))
        image = cv2.line(image, start_pos, end_pos, color, thickness)
    return image

#flag for debugging
debug = True

try:
    print("Program started. Type 'capture' in the terminal to trigger capture:")
    while True:
        # Wait for user input in the terminal
        command = input("Enter command: ").strip().lower()
        if command == 'capture':
            print("Capture command received.")
            # Timestamp and create an output directory
            outdir = pathlib.Path(args.output_path) / time.strftime('%Y-%m-%d_%H-%M-%S-%Z')
            outdir.mkdir(parents=True)
            f = []
            
            # Initialize frame rate calculation
            frame_rate_calc = 1
            freq = cv2.getTickFrequency()
            videostream = VideoStream(resolution=(imW, imH), framerate=30).start()
            time.sleep(1)

            while True:
                print('running loop')
                
                # Start timer (for calculating frame rate)
                t1 = cv2.getTickCount()

                # Grab frame from video stream
                frame1 = videostream.read()
                # Acquire frame and resize to expected shape [1xHxWx3]
                frame = frame1.copy()
                frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                frame_resized = cv2.resize(frame_rgb, (width, height))
                input_data = np.expand_dims(frame_resized, axis=0)
                
                frame_resized = cv2.cvtColor(frame_resized, cv2.COLOR_BGR2RGB)

                # Normalize pixel values if using a floating model (i.e. if model is non-quantized)
                if floating_model:
                    input_data = (np.float32(input_data) - input_mean) / input_std

                # Perform the actual detection by running the model with the image as input
                interpreter.set_tensor(input_details[0]['index'], input_data)
                interpreter.invoke()
                
                # Get y,x positions from heatmap
                coords = sigmoid_and_argmax2d(output_details, min_conf_threshold)
                # Keep track of keypoints that don't meet threshold
                drop_pts = list(np.unique(np.where(coords == 0)[0]))
                # Get offsets from positions
                offset_vectors = get_offsets(output_details, coords)
                # Use stride to get coordinates in image coordinates
                keypoint_positions = coords * output_stride + offset_vectors

                # Loop over all detections and draw detection box if confidence is above minimum threshold
                for i in range(len(keypoint_positions)):
                    # Don't draw low confidence points
                    if i in drop_pts:
                        continue
                    # Center coordinates
                    x = int(keypoint_positions[i][1])
                    y = int(keypoint_positions[i][0])
                    center_coordinates = (x, y)
                    radius = 2
                    color = (0, 255, 0)
                    thickness = 2
                    cv2.circle(frame_resized, center_coordinates, radius, color, thickness)
                    if debug:
                        cv2.putText(frame_resized, str(i), (x-4, y-4), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 1)  # Draw label text
                
                frame_resized = draw_lines(keypoint_positions, frame_resized, drop_pts)

                # Calculate framerate
                t2 = cv2.getTickCount()
                time1 = (t2 - t1) / freq
                frame_rate_calc = 1 / time1

                # Display frame rate on frame
                cv2.putText(frame_resized, f'FPS: {frame_rate_calc:.2f}', (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 0, 0), 2, cv2.LINE_AA)

                # Show the processed frame
                cv2.imshow("Pose Estimation", frame_resized)
                
                # Display a grid
                cv2.line(frame_resized, (256 // 2, 0), (256 // 2, 256), (255, 0, 0), 1)
                cv2.line(frame_resized, (0, 256 // 2), (256, 256 // 2), (255, 0, 0), 1)
                
                # Vert/Hor offset -> for motor control 
                keypoint_11 = keypoint_positions[11]  # left hip [y, x]
                keypoint_12 = keypoint_positions[12]  # right hip [y, x]

                # calc the midpoint of hipsß
                detected_x = (keypoint_11[1] + keypoint_12[1]) / 2  # avg x
                detected_y = (keypoint_11[0] + keypoint_12[0]) / 2  # avg y
                
                if (keypoint_11[1] > 0 and keypoint_12[1] > 0 and keypoint_11[0] > 0 and keypoint_12[0] > 0):
                
                    center_coordinates = (int(detected_x), int(detected_y))  # Convert to integers
                    radius = 8
                    color = (255, 0, 0)  # Red
                    thickness = 8
                    
                    cv2.circle(frame_resized, center_coordinates, radius, color, thickness)
                    
                    print("hip: ", keypoint_positions[11])
                    
                    offset_x = detected_x - (256) / 2
                    offset_y = detected_y - (256) / 2

                    print(f"OFFSET- x: {offset_x}, y: {offset_y}")

                # Press 'q' to quit
                                # Press 'q' to quit
                if cv2.waitKey(1) == ord('q') or led_on and not GPIO.input(17):
                    print(f"Saved images to: {outdir}")
                    GPIO.output(4, False)
                    led_on = False
                    # Clean up
                    cv2.destroyAllWindows()
                    videostream.stop()
                    time.sleep(2)
                    break

            videostream.stop()

except KeyboardInterrupt:
     # Clean up
    cv2.destroyAllWindows()
    videostream.stop()
    print('Stopped video stream.')