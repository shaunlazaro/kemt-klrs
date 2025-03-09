from typing import List, Optional, Dict, Union
from .tracking import TrackingDetail, TrackingType

# Think of this as a high-level overview of the exercise
class ExerciseDetail:
    def __init__(
        self,
        rep_keypoints: List[str], # TODO: consider removing
        rep_tracking: TrackingDetail,
        start_angle: float,
        min_rep_time: float,
        threshold_flexion: float, # TODO: move to trackingDetail
        threshold_extension: float, # TODO: move to trackingDetail
        display_name: str,
        start_in_flexion: bool,
        body_alignment: str,
        default_tracking_details: List[TrackingDetail],
        instruction: Optional[str] = None
    ):
        self.rep_keypoints = rep_keypoints
        self.rep_tracking = rep_tracking
        self.start_angle = start_angle
        self.min_rep_time = min_rep_time
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
        exercise: ExerciseDetail,
        reps: float,
        custom_tracking_details: List[TrackingDetail] = []        
    ):
        self.exercise = exercise
        self.reps = reps
        self.custom_tracking_details = custom_tracking_details
        

        
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
        reps: Optional[float] = 0, 
        custom_tracking_details: List[TrackingDetail] = []        
    ):
        self.exercises.append(RoutineComponent(exercise_detail, reps))
    
    def get_workout(self, exercise_name: str) -> Optional[RoutineComponent]:
        for component in self.exercises:
            if component.exercise.display_name == exercise_name:
                return component
        return None

