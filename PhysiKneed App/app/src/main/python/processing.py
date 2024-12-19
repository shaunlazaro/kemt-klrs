import numpy as np
import time

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