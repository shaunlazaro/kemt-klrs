from typing import List
from .workout_config import RoutineComponent, RoutineConfig, ExerciseDetail

class RepData:
    def __init__(
        self,
        rep_number: int,
        max_flexion: float,
        max_extension: float,
        concentric_time: float,
        eccentric_time: float,
        total_time: float,
        goal_flexion_met: bool,
        goal_extension_met: bool,
        max_score: float,
        alerts: List[str],
        poses: List[str] = []
    ):
        self.rep_number = rep_number
        self.max_flexion = max_flexion
        self.max_extension = max_extension
        self.concentric_time = concentric_time
        self.eccentric_time = eccentric_time
        self.total_time = total_time
        self.goal_flexion_met = goal_flexion_met
        self.goal_extension_met = goal_extension_met
        self.max_score = max_score
        self.alerts = alerts
        self.poses = poses
        
    def to_dict(self):
        return {
            "rep_number": self.rep_number,
            "max_flexion": self.max_flexion,
            "max_extension": self.max_extension,
            "concentric_time": self.concentric_time,
            "eccentric_time": self.eccentric_time,
            "total_time": self.total_time,
            "goal_flexion_met": bool(self.goal_flexion_met),
            "goal_extension_met": bool(self.goal_extension_met),
            "max_score": self.max_score,
            "alerts": self.alerts,
            "poses": self.poses
        }
        
class RoutineComponentData:
    def __init__(
        self,
        exercise_detail: ExerciseDetail,
        rep_data: List[RepData]
    ):
        self.exercise_detail = exercise_detail
        self.rep_data = rep_data if rep_data is not None else []
        
    def to_dict(self):
        return {
            "exercise_detail": self.exercise_detail.to_dict(),
            "rep_data": [rep.to_dict() for rep in self.rep_data if rep is not None]
        }


class RoutineData:
    def __init__(
        self,
        routine_config: RoutineConfig,
        routine_component_data: List[RoutineComponentData]
    ):
        self.routine_config = routine_config
        self.routine_component_data = routine_component_data

    def to_dict(self):
        return {
            "routine_config": self.routine_config.to_dict(),
            "routine_component_data": [rcd.to_dict() for rcd in self.routine_component_data]
        }
