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
export const NEW_PATIENT_ID = "new"
export const BLANK_PATIENT: Patient = {
    id: "TEMP",
    first_name: "",
    last_name: "",
    email: "",
    date_of_birth: new Date(),
    sex: "M",
    condition: "",
    exercises: undefined,
}