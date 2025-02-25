from typing import List, Optional, Dict, Union
from .tracking import TrackingDetail, TrackingType

# Think of this as a high-level overview of the exercise
class ExerciseDetail:
    def __init__(
        self,
        rep_keypoints: List[str],
        rep_tracking: TrackingDetail,
        threshold_flexion: float,
        threshold_extension: float,
        display_name: str,
        start_in_flexion: bool,
        body_alignment: str,
        default_tracking_details: List[TrackingDetail],
        instruction: Optional[str] = None
    ):
        self.rep_keypoints = rep_keypoints
        self.rep_tracking = rep_tracking
        self.threshold_flexion = threshold_flexion
        self.threshold_extension = threshold_extension
        self.display_name = display_name
        self.start_in_flexion = start_in_flexion
        self.body_alignment = body_alignment
        self.default_tracking_details = default_tracking_details
        self.instruction = instruction


class RoutineComponent:
    def __init__(
        self,
        name: str,
        exercise: ExerciseDetail,
        reps: float,        
    ):
        self.name = name
        self.exercise = exercise
        self.reps = reps
        

        
class RoutineConfig:
    def __init__(
        self,
        name: str,
        exercises: List[RoutineComponent],
        injury: str,
    ):
        self.name = name
        self.exercises = exercises
        self.injury = injury

    def add_exercise(
        self, 
        name: str,
        exercise_detail: ExerciseDetail, 
        reps: Optional[float] = None, 
    ):
        self.exercises.append(RoutineComponent(name, exercise_detail, reps))
    
    def get_workout(self, exercise_name: str) -> Optional[RoutineComponent]:
        for component in self.exercises:
            if component.name == exercise_name:
                return component
        return None

