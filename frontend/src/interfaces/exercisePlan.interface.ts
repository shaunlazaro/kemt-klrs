// Exercise Plan = Routine Config from the python code:
import { ExerciseDetail, TrackingDetail } from "./exerciseDetail.interface"

export interface RoutineConfig {
    id: string,
    name: string,
    exercises: RoutineComponent[],
    injury: string,
}

// Not actually an object on the server, so it has no ID...  TODO: that's bad, reimplement this as its own object in the backend.
export interface RoutineComponent {
    exercise: ExerciseDetail,
    reps: number,
    customTrackingDetails?: TrackingDetail[]
}