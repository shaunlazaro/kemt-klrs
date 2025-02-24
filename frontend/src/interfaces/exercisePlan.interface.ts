// Equivalent to routine config from the python code:
// class RoutineConfig:
//     def __init__(self):
//         self.exercises: Dict[str, Dict[str, Union[ExerciseDetail, float]]] = {}

import { ExerciseDetail, TrackingDetail } from "./exerciseDetail.interface"

//     def add_exercise(
//         self, 
//         exercise_name: str, 
//         exercise_detail: ExerciseDetail, 
//         reps: Optional[float] = None, 
//         custom_tracking_details: Optional[List["TrackingDetail"]] = None
//     ):
//         self.exercises[exercise_name] = {
//             "Workout": exercise_detail,
//             "Reps": reps,
//             "CustomTrackingDetails": custom_tracking_details or exercise_detail.default_tracking_details,
//         }

//     def get_workout(self, exercise_name: str):
//         return self.exercises.get(exercise_name, None)

export interface RoutineConfig{
    name: string
    exercises: RoutineComponent[]
}
export interface RoutineComponent{
    exercise: ExerciseDetail,
    reps: number,
    customTrackingDetails?: TrackingDetail[]
}