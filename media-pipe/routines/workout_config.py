from typing import List, Optional, Dict, Union
from .tracking import TrackingDetail, TrackingType

# Think of this as a high-level overview of the exercise
class ExerciseDetail:
    def __init__(
        self,
        rep_keypoints: List[str],
        threshold_flexion: float,
        threshold_extension: float,
        display_name: str,
        default_tracking_details: List["TrackingDetail"]
    ):
        self.rep_keypoints = rep_keypoints
        self.threshold_flexion = threshold_flexion
        self.threshold_extension = threshold_extension
        self.display_name = display_name
        self.default_tracking_details = default_tracking_details


class RoutineConfig:
    def __init__(self):
        self.exercises: Dict[str, Dict[str, Union[ExerciseDetail, float]]] = {}

    def add_exercise(
        self, 
        exercise_name: str, 
        exercise_detail: ExerciseDetail, 
        reps: Optional[float] = None, 
        custom_tracking_details: Optional[List["TrackingDetail"]] = None
    ):
        self.exercises[exercise_name] = {
            "Workout": exercise_detail,
            "Reps": reps,
            "CustomTrackingDetails": custom_tracking_details or exercise_detail.default_tracking_details,
        }

    def get_workout(self, exercise_name: str):
        return self.exercises.get(exercise_name, None)
