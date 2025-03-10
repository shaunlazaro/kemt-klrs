from typing import List
from .workout_config import RoutineComponent, RoutineConfig

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
        score: float,
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
        self.score = score
        self.alerts = alerts
        self.poses = poses
        
class RoutineComponentData:
    def __init__(
        self,
        routine_component: RoutineComponent,
        rep_data: List[RepData]
    ):
        self.routine_component = routine_component
        self.rep_data = rep_data


class RoutineData:
    def __init__(
        self,
        routineConfig: RoutineConfig,
        routineComponentData: List[RoutineComponentData]
    ):
        self.routineConfig = routineConfig
        self.routineComponentData = routineComponentData

