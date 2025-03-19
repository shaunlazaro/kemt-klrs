import numpy as np
import time
from routines.workout_data import RepData


# Function to calculate the angle between three, 3D points
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

class ExerciseTracker:
    def __init__(self, exercise_detail):
        self.rep_count = 0
        self.state = "rest"
        self.rep_ready = True
        self.just_completed_rep = False
        self.rep_start_time = None
        self.last_rep_duration = 0
        self.flexion_threshold = exercise_detail.threshold_flexion
        self.extension_threshold = exercise_detail.threshold_extension

        self.start_in_flexion = exercise_detail.start_in_flexion
        self.current_max_flexion = float('inf')
        self.current_max_extension = float('-inf')

        self.goal_flexion = exercise_detail.rep_tracking.goal_flexion
        self.goal_extension = exercise_detail.rep_tracking.goal_extension

        self.alerts = set()
        self.last_rep_alerts = set()

        self.phase_1_start_time = None
        self.phase_2_start_time = None
        self.last_concentric_time = 0
        self.last_eccentric_time = 0

        self.poses_buffer = []
        self.last_pose_capture_time = 0
        self.score = 0
        self.max_score = 0

    def detect_reps(self, tracking_results, exercise_detail, pose_data):
        """Detects repetitions based on tracking results."""
        rep_entry = None  
        main_tracking_detail = exercise_detail.rep_tracking
        
        primary_angle = self._get_primary_angle(tracking_results, main_tracking_detail)
        if primary_angle is None:
            return self.rep_count, self.last_rep_duration

        current_time = time.time()
        self._update_max_angles(primary_angle)
        self._process_alerts(tracking_results, main_tracking_detail)
        self._update_progress_score(primary_angle, exercise_detail.start_angle)

        if current_time - self.last_pose_capture_time >= 0.1:
            self.poses_buffer.append({"landmarks": pose_data})
            self.last_pose_capture_time = current_time

        self._update_state(primary_angle, current_time)

        if self.state == "rest" and self.just_completed_rep:
            rep_entry = self._finalize_rep(current_time, main_tracking_detail, exercise_detail.min_rep_time)
            self.just_completed_rep = False

        return rep_entry

    def _get_primary_angle(self, tracking_results, main_tracking_detail):
        return next(
            (entry["angle"] for entry in tracking_results if entry["detail"] == main_tracking_detail),
            None
        )

    def _update_max_angles(self, primary_angle):
        self.current_max_flexion = min(self.current_max_flexion, primary_angle)
        self.current_max_extension = max(self.current_max_extension, primary_angle)

    def _process_alerts(self, tracking_results, main_tracking_detail):
        """Checks for alert conditions based on tracking data."""
        for entry in tracking_results:
            detail, value = entry["detail"], entry["angle"]
            
            # Main tracking detail alerts are handled separately
            if detail == main_tracking_detail:
                continue
            if detail.show_alert_if_above and value > detail.show_alert_if_above:
                self.alerts.add(detail.alert_message)
                self.last_rep_alerts.add(detail.alert_message)
            elif detail.show_alert_if_below and value < detail.show_alert_if_below:
                self.alerts.add(detail.alert_message)
                self.last_rep_alerts.add(detail.alert_message)
            else:
                self.alerts.discard(detail.alert_message)

    def _update_progress_score(self, primary_angle, start_angle):
        """Computes a normalized rep completion score (0 to 1) based on progress from start_angle to goal."""
        if self.start_in_flexion:
            target_angle = self.goal_extension
            progress = (start_angle - primary_angle) / (start_angle - target_angle)
        else:
            target_angle = self.goal_flexion
            progress = (primary_angle - start_angle) / (target_angle - start_angle)

        self.score = max(0, min(1, progress))
        self.max_score = max(self.max_score, self.score)
        # print(f"Progress: {self.score:.2%}")

    def _update_state(self, primary_angle, current_time):
        """Handles state transitions based on detected movement."""
        if self.state == "rest":
            if self._is_full_rep_completed(primary_angle):
                self.rep_ready = True 

            if self.rep_ready and self._is_start_of_rep(primary_angle):
                self._start_new_rep(current_time)
                self.rep_ready = False

        elif self.state == "phase_1":
            if self._is_half_rep_completed(primary_angle):
                self._start_phase_2(current_time)

        elif self.state == "phase_2":
            if self._is_full_rep_completed(primary_angle):
                self._complete_rep(current_time)

    def _is_start_of_rep(self, angle):
        return (self.start_in_flexion and angle > self.flexion_threshold) or \
               (not self.start_in_flexion and angle < self.extension_threshold)

    def _is_half_rep_completed(self, angle):
        return (self.start_in_flexion and angle > self.extension_threshold) or \
               (not self.start_in_flexion and angle < self.flexion_threshold)

    def _is_full_rep_completed(self, angle):
        return (self.start_in_flexion and angle < self.flexion_threshold) or \
               (not self.start_in_flexion and angle > self.extension_threshold)

    def _start_new_rep(self, current_time):
        self.state = "phase_1"
        self.phase_1_start_time = current_time
        self.current_max_flexion = float('inf')
        self.current_max_extension = float('-inf')
        self.poses_buffer = []
        self.just_completed_rep = False
        print("Starting new rep...")

    def _start_phase_2(self, current_time):
        self.state = "phase_2"
        self.phase_2_start_time = current_time
        self.last_concentric_time = current_time - self.phase_1_start_time

    def _complete_rep(self, current_time):
        self.state = "rest"
        self.last_eccentric_time = current_time - self.phase_2_start_time
        self.last_rep_duration = self.last_concentric_time + self.last_eccentric_time
        self.rep_count += 1
        self.just_completed_rep = True

    def _finalize_rep(self, current_time, main_tracking_detail, min_rep_time):
        """Generates rep data and resets tracking for the next rep."""
        flexion_goal_met = self.goal_flexion is None or self.current_max_flexion <= self.goal_flexion
        extension_goal_met = self.goal_extension is None or self.current_max_extension >= self.goal_extension

        if not flexion_goal_met or not extension_goal_met:
            self.alerts.add(main_tracking_detail.alert_message)
            
        if self.last_concentric_time + self.last_eccentric_time < min_rep_time:
            self.alerts.add("Slow down your movement")

        self.last_rep_alerts.update(self.alerts)

        rep_entry = RepData(
            rep_number=self.rep_count,
            max_flexion=self.current_max_flexion,
            max_extension=self.current_max_extension,
            concentric_time=self.last_concentric_time,
            eccentric_time=self.last_eccentric_time,
            total_time=self.last_rep_duration,
            goal_flexion_met=flexion_goal_met,
            goal_extension_met=extension_goal_met,
            max_score=self.max_score,
            alerts=list(self.last_rep_alerts),
            poses=self.poses_buffer if self.poses_buffer else []
        )

        self._print_rep_feedback()
        self._reset_rep_tracking(current_time)

        return rep_entry

    def _print_rep_feedback(self):
        print(f"Rep {self.rep_count} completed in {self.last_rep_duration:.2f} sec")
        print(f"Concentric: {self.last_concentric_time:.2f} sec, Eccentric: {self.last_eccentric_time:.2f} sec")
        print(f"Max Flexion: {self.current_max_flexion:.2f}°, Max Extension: {self.current_max_extension:.2f}°")
        for alert in self.alerts:
            print(f"⚠️ {alert}")

    def _reset_rep_tracking(self, current_time):
        self.current_max_flexion = float('inf')
        self.current_max_extension = float('-inf')
        self.phase_1_start_time = current_time
        self.alerts.clear()
        self.poses_buffer = []
        self.last_pose_capture_time = 0
        self.max_score = 0
        self.score = 0
        self.last_rep_alerts.clear()