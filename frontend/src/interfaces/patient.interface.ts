import { RoutineConfig } from "./exercisePlan.interface";

export interface Patient {
    id: string;
    first_name: string;
    last_name: string;
    email: string;
    date_of_birth: Date;
    sex: string;
    condition: string;
    exercises?: RoutineConfig;
}