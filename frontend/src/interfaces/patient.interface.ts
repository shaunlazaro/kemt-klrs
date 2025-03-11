import { RoutineConfig } from "./exercisePlan.interface";

export interface Patient {
    userId: string;
    // name: string;
    firstName: string;
    lastName: string;
    email: string;
    dateOfBirth: Date;
    // age: number; replaced by DOB
    sex: string;
    condition: string; // TODO
    exercises?: RoutineConfig; // TODO
    weeklyProgress: number; // out of 7?
}