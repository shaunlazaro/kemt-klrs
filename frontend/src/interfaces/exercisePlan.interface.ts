// Exercise Plan = Routine Config from the python code:
import { ExerciseDetail, TrackingDetail } from "./exerciseDetail.interface"

export interface RoutineConfig{
    name: string // TODO: Name is in DB, not in python
    exercises: RoutineComponent[], // TODO: This is a dict instead of a list incorrectly in python version, fix
    injury: string // TODO: this is a new addition (not in any other subsystem yet), add everywhere else
}
export interface RoutineComponent{
    exercise: ExerciseDetail,
    reps: number,
    customTrackingDetails?: TrackingDetail[]
}