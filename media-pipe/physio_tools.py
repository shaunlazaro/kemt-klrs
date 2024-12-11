import numpy as np
import time

EXERCISES = {
    "leg_extension": {
        "sides": {
            "right": {
                "keypoints": ["right_hip", "right_knee", "right_ankle"],
            },
            "left": {
                "keypoints": ["left_hip", "left_knee", "left_ankle"],
            }
        },
        "threshold_flexion": 60,
        "threshold_extension": 160,
        "display_name": "Leg Extension",
        "show_angle": True,
        "show_reps": True,
        "user_body_alignment": "right"
    },
    "air_squat": {
        "keypoints": ["right_hip", "right_knee", "right_ankle"],  # No sides here
        "threshold_flexion": 90,
        "threshold_extension": 160,
        "display_name": "Air Squat",
        "show_angle": True,
        "show_reps": True,
        "user_body_alignment": "side",
    }
}

def get_exercise_params(exercise_name, side="right"):
    exercise_data = EXERCISES.get(exercise_name)
    
    # Check if the exercise has sides (e.g. left and right)
    if "sides" in exercise_data:
        return exercise_data["sides"].get(side, exercise_data["sides"]["right"])
    else:
        return exercise_data


# Function to calculate the angle between three, 3D points
def calculate_angle(a, b, c):
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


def smooth_angle(knee_angle, angle_history):
    angle_history.append(knee_angle)
    return sum(angle_history) / len(angle_history)


class ExerciseTracker:
    def __init__(self, exercise_params):
        self.rep_count = 0
        self.state = "rest"
        self.rep_start_time = None
        self.last_rep_duration = 0
        self.flexion_threshold = exercise_params["threshold_flexion"]
        self.extension_threshold = exercise_params["threshold_extension"]
    
    # TODO: Rethink how this should work...
    # Should it be controlled by threshold angles, or the user's movement regardless of threshold? ...
    def detect_reps(self, knee_angle):
        current_time = time.time()
        if self.state == "rest" and knee_angle < self.flexion_threshold:
            self.state = "flexing"
            self.rep_start_time = current_time
            print("Starting exercise rep...")
        elif self.state == "flexing" and knee_angle > self.extension_threshold:
            self.state = "extending"
            if self.rep_start_time is not None:
                self.last_rep_duration = current_time - self.rep_start_time
            self.rep_count += 1
            print(f"Rep {self.rep_count} completed in {self.last_rep_duration:.2f} seconds.")
        elif self.state == "extending" and knee_angle < self.flexion_threshold:
            self.state = "flexing"
            self.rep_start_time = current_time

        return self.rep_count, self.last_rep_duration