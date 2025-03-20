import numpy as np
import time
from collections import deque

from java import jclass

# Import Android Log class
Log = jclass('android.util.Log')

# Parameters
flexion_threshold = 80  # Fully bent position
extension_threshold = 140  # Fully extended position
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
        if rep_count < 10:
            rep_count += 1  # Count a full rep
    elif state == "extending" and knee_angle < flexion_threshold:
        state = "flexing"
        rep_start_time = current_time  # Restart the timer for the next rep

    return rep_count

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

def get_motor_coords_offset(midpoint, frame_size):
    MOTOR_RATIO = 23/(frame_size / 2) # distance from center to edge of screen in motor coords (angle)
    return (midpoint - frame_size / 2)*MOTOR_RATIO

def get_coords(all_points, h, w, current):

    FRAME_WIDTH = w
    FRAME_HEIGHT = h

    Log.d("COORDS_TAG", "w = " + str(w))
    Log.d("COORDS_TAG", "h = " + str(h))

    TOLERANCE_X = 80*23/(FRAME_WIDTH / 2)  # Motor coords from the center where no updates are sent
    TOLERANCE_Y = 80*23/(FRAME_HEIGHT / 2)  # Motor coords from the center where no updates are sent

    Log.d("COORDS_TAG", "TOLERANCE_X = " + str(TOLERANCE_X))
    Log.d("COORDS_TAG", "TOLERANCE_Y = " + str(TOLERANCE_Y))

    MAX_MOVE_DISTANCE = 25 # Move no more than 10 units.
    # DENOMINATOR = 20

    current_x, current_y = map(int, current[1:-1].split(','))


    Log.d("COORDS_TAG", "current_x = " + str(current_x))
    Log.d("COORDS_TAG", "current_y = " + str(current_y))

    # Initialize motors at center positions
    motor_x = current_x  # Horizontal motor
    motor_y = current_y  # Vertical motor

    # Calculate the midpoint of all landmarks
    x_coords, y_coords = zip(*all_points)  # Separate x and y coordinates
    midpoint = (sum(x_coords) / len(x_coords), sum(y_coords) / len(y_coords))

    Log.d("COORDS_TAG", "midpoint = " + str(midpoint))

# Convert midpoint to pixel coordinates
    pixel_midpoint = (int(midpoint[0] * w), int(midpoint[1] * h))

    Log.d("COORDS_TAG", "pixel_midpoint = " + str(pixel_midpoint))

    midpoint_x = pixel_midpoint[0]
    midpoint_y = pixel_midpoint[1]

    # Calculate offsets from frame center
    offset_x = get_motor_coords_offset(midpoint_x, FRAME_WIDTH)
    offset_y = get_motor_coords_offset(midpoint_y, FRAME_HEIGHT)

    Log.d("COORDS_TAG", "offset_x = " + str(offset_x))
    Log.d("COORDS_TAG", "offset_y = " + str(offset_y))

    center_frame = (FRAME_WIDTH / 2, FRAME_HEIGHT / 2)

    # Check if offsets exceed tolerance
    if abs(offset_x) > TOLERANCE_X or abs(offset_y) > TOLERANCE_Y:
        # Update motor positions
        if offset_x > TOLERANCE_X and motor_x > 0:
            Log.d("COORDS_TAG", "Branch 1 " + str(offset_x))
            motor_x += min(round(offset_x), MAX_MOVE_DISTANCE)
        elif offset_x < -TOLERANCE_X and motor_x < 180:
            Log.d("COORDS_TAG", "Branch 2 " + str(offset_x))
            motor_x += max(round(offset_x), -MAX_MOVE_DISTANCE)

        if offset_y > TOLERANCE_Y and motor_y > 0:
            Log.d("COORDS_TAG", "Branch 3 = " + str(offset_y))
            motor_y -= min(round(offset_y), MAX_MOVE_DISTANCE)
        elif offset_y < -TOLERANCE_Y and motor_y < 180:
            Log.d("COORDS_TAG", "Branch 4 = " + str(offset_y))
            motor_y -= max(round(offset_y), -MAX_MOVE_DISTANCE)

        if motor_y > 180:
            motor_y = current_y
        if motor_y < 0:
            motor_y = current_y
        if motor_x > 180:
            motor_x = current_x
        if motor_x < 0:
            motor_x = current_x

        # Send motor commands (replace with actual communication code)
        msg = "[{},{}]".format(motor_x, motor_y)

        Log.d("COORDS_TAG", "msg = " + str(msg))


        return msg
    else:
        return "DNM"

def calculate_three_point_angle(a, b, c):
    a = np.array(a)  # Point 1
    b = np.array(b)  # Vertex point
    c = np.array(c)  # Point 2

    # Calculate vectors
    ba = a - b
    bc = c - b

    # Calculate cosine of angle
    cosine_angle = np.dot(ba, bc) / (np.linalg.norm(ba) * np.linalg.norm(bc))
    angle = np.arccos(np.clip(cosine_angle, -1.0, 1.0))  # Radians

    return np.degrees(angle)  # Convert to degrees

def calculate_two_point_vertical_angle(a, b):
    vertical = np.array([0, 1])
    a = np.array(a)  # Point 1
    b = np.array(b)  # Point 2

    ba = a - b
    exercise_angle = np.degrees(
        np.arccos(np.clip(np.dot(ba, vertical) / np.linalg.norm(ba), -1.0, 1.0))
    )

    return exercise_angle

def calculate_two_point_horizontal_angle(a, b):
    horizontal = np.array([1, 0])
    vec = a - b
    exercise_angle = np.degrees(
        np.arccos(np.clip(np.dot(vec, horizontal) / np.linalg.norm(vec), -1.0, 1.0))
    )
    return exercise_angle

def smooth_angle(angle, angle_history):
    angle_history.append(angle)
    return sum(angle_history) / len(angle_history)