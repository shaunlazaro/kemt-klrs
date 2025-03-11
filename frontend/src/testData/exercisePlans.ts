import { RoutineConfig } from "../interfaces/exercisePlan.interface";
import { ExerciseMockA, ExerciseMockB, ExerciseMockC, ExerciseMockD } from "./exerciseDetail";

// export interface RoutineConfig{
//     name: string
//     exercises: RoutineComponent[]
// }
// export interface RoutineComponent{
//     exercise: ExerciseDetail,
//     reps: number,
//     customTrackingDetails?: TrackingDetail[]
// }


export const TestRoutineConfig: RoutineConfig = {
    id: "1",
    name: "Abc of the Knee",
    exercises: [
        {
            exercise: ExerciseMockA,
            reps: 10,
        },
        {
            exercise: ExerciseMockA,
            reps: 10,
        },
        {
            exercise: ExerciseMockB,
            reps: 10,
        },
        {
            exercise: ExerciseMockC,
            reps: 10,
        },
        {
            exercise: ExerciseMockD,
            reps: 10,
        },
    ],
    injury: "Knee"
}
export const TestRoutineConfig2: RoutineConfig = {
    id: "2",
    name: "Ankle Turner",
    exercises: [
        {
            exercise: ExerciseMockD,
            reps: 100,
        }
    ],
    injury: "Ankle"
}

export const ExercisePlanListMock: RoutineConfig[] = [TestRoutineConfig, TestRoutineConfig2, TestRoutineConfig2, TestRoutineConfig2, TestRoutineConfig, TestRoutineConfig]
export const RoutineConfigList: RoutineConfig[] = [TestRoutineConfig, TestRoutineConfig2]
