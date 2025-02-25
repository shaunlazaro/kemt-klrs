import numpy as np
import time
from routines.workout_config import ExerciseDetail
from collections import deque


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
    angle = np.arccos(np.clip(cosine_angle, -1.0, 1.0))  # Radians

    return np.degrees(angle)  # Convert to degrees


def smooth_angle(knee_angle, angle_history):
    angle_history.append(knee_angle)
    return sum(angle_history) / len(angle_history)


class ExerciseTracker:
    def __init__(self, exercise_detail):
        self.rep_count = 0
        self.state = "rest"
        self.rep_start_time = None
        self.last_rep_duration = 0
        self.flexion_threshold = exercise_detail.threshold_flexion
        self.extension_threshold = exercise_detail.threshold_extension

        # establish rep starting position (flexed or extended)
        self.start_in_flexion = exercise_detail.start_in_flexion
        
        # max/min angles for each rep
        self.current_max_flexion = float('inf')
        self.current_max_extension = float('-inf')

        # goal angles
        tracking_detail = exercise_detail.default_tracking_details[0]
        self.goal_flexion = tracking_detail.goal_flexion
        self.goal_extension = tracking_detail.goal_extension
        
        self.alert_message = tracking_detail.alert_message
        self.alert = None

        self.phase_1_start_time = None
        self.phase_2_start_time = None
        self.last_concentric_time = 0
        self.last_eccentric_time = 0

        self.rep_data = deque(maxlen=50)

    def detect_reps(self, knee_angle):
        current_time = time.time()

        # Track max flexion and extension angles
        self.current_max_flexion = min(self.current_max_flexion, knee_angle)
        self.current_max_extension = max(self.current_max_extension, knee_angle)

        if self.state == "rest":
            if (self.start_in_flexion and knee_angle < self.flexion_threshold) or \
               (not self.start_in_flexion and knee_angle > self.extension_threshold):
                # Start a new rep cycle
                self.state = "phase_1"
                self.phase_1_start_time = current_time
                self.current_max_flexion = knee_angle
                self.current_max_extension = knee_angle
                print("Starting new rep...")

        elif self.state == "phase_1":
            if (self.start_in_flexion and knee_angle > self.extension_threshold) or \
               (not self.start_in_flexion and knee_angle < self.flexion_threshold):
                # Transition to phase 2
                self.state = "phase_2"
                self.phase_2_start_time = current_time
                self.last_concentric_time = self.phase_2_start_time - self.phase_1_start_time

        elif self.state == "phase_2":
            if (self.start_in_flexion and knee_angle < self.flexion_threshold) or \
               (not self.start_in_flexion and knee_angle > self.extension_threshold):
                # Full rep cycle completed
                self.state = "phase_1"  # Ready for next rep
                self.last_eccentric_time = current_time - self.phase_2_start_time
                self.last_rep_duration = self.last_concentric_time + self.last_eccentric_time
                self.rep_count += 1

                # Check if goal flexion/extension was met
                flexion_goal_met = self.goal_flexion is None or self.current_max_flexion <= self.goal_flexion
                extension_goal_met = self.goal_extension is None or self.current_max_extension >= self.goal_extension

                # Determine if an alert should be given
                if not flexion_goal_met:
                    self.alert = self.alert_message
                elif not extension_goal_met:
                    self.alert = self.alert_message
                else:
                    self.alert = None
                    
                self.rep_data.append({
                    "rep_number": self.rep_count,
                    "max_flexion": self.current_max_flexion,
                    "max_extension": self.current_max_extension,
                    "concentric_time": self.last_concentric_time,
                    "eccentric_time": self.last_eccentric_time,
                    "total_time": self.last_rep_duration,
                    "goal_flexion_met": flexion_goal_met,
                    "goal_extension_met": extension_goal_met,
                    "alert": self.alert
                })

                # Print feedback
                print(f"Rep {self.rep_count} completed in {self.last_rep_duration:.2f} sec")
                print(f"Concentric: {self.last_concentric_time:.2f} sec, Eccentric: {self.last_eccentric_time:.2f} sec")
                print(f"Max Flexion: {self.current_max_flexion:.2f}°, Max Extension: {self.current_max_extension:.2f}°")
                print(f"Goal Flexion Met: {flexion_goal_met}, Goal Extension Met: {extension_goal_met}")
                if self.alert:
                    print(f"⚠️ {self.alert}")

                # Reset tracking values for next rep
                self.current_max_flexion = float('inf')
                self.current_max_extension = float('-inf')
                self.phase_1_start_time = current_time

        return self.rep_count, self.last_rep_duration

    def get_rep_data(self):
        """Returns all recorded rep data, including goal tracking and alerts."""
        return list(self.rep_data)
