import { RoutineConfig } from "./exercisePlan.interface";

export interface Patient {
    userId: string;
    name: string;
    age: number;
    sex: string;
    condition: string; // TODO
    exercises?: RoutineConfig; // TODO
    weeklyProgress: number; // out of 7?
}