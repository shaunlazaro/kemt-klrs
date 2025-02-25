from enum import Enum
from typing import List, Optional

class TrackingType(Enum):
    ANGLE_OF_THREE_POINTS = "Angle of three points"
    VELOCITY_OF_POINT = "Velocity of point"
    ACCELERATION_OF_POINT = "Acceleration of point"
    DISTANCE_BETWEEN_POINTS = "Distance between points"
    ANGLE_WITH_HORIZONTAL = "Angle with horizontal"
    ANGLE_WITH_VERTICAL = "Angle with vertical"
    ANGLE_WITH_LINE = "Angle with line"

# Think of this as a low-level overview of the exercise's tracking details
class TrackingDetail:
    def __init__(
        self,
        tracking_type: TrackingType,
        keypoints: List[str],
        dimensionality: Optional[str] = "3D",
        goal_flexion: Optional[float] = None,
        goal_extension: Optional[float] = None,
        show_alert_if_above: Optional[float] = None,
        show_alert_if_below: Optional[float] = None,
        alert_message: Optional[str] = None
    ):
        self.tracking_type = tracking_type
        self.keypoints = keypoints
        self.dimensionality = dimensionality
        self.goal_flexion = goal_flexion  # Store the goal flexion/extension angle
        self.goal_extension = goal_extension  # Store the goal flexion/extension angle
        self.show_alert_if_above = show_alert_if_above
        self.show_alert_if_below = show_alert_if_below
        self.alert_message = alert_message
