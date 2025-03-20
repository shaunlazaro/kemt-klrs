import { Patient } from "../interfaces/patient.interface";
import { TestRoutineConfig, TestRoutineConfig2 } from "./exercisePlans";

const cutoffDate = new Date();
cutoffDate.setFullYear(cutoffDate.getFullYear() - 1);

const dateNYearsAgo = (n: number) => {
    const today = new Date()
    today.setFullYear(today.getFullYear() - n)
    return today
}

export const MockPatients: Patient[] = [
    {
        id: "1",
        first_name: "Al",
        last_name: "Liver",
        date_of_birth: dateNYearsAgo(40),
        email: "test@test.com",
        sex: "M",
        condition: "Knee",
        exercises: TestRoutineConfig,
        //weeklyProgress: 7
    },
    {
        id: "2",
        first_name: "Bourne",
        last_name: "Happy",
        date_of_birth: dateNYearsAgo(70),
        email: "test@test.com",
        sex: "F",
        condition: "Ankle",
        exercises: TestRoutineConfig2,
        //weeklyProgress: 2
    },
    {
        id: "3",
        first_name: "Craig",
        last_name: "Gnee",
        date_of_birth: dateNYearsAgo(24),
        email: "test@test.com",
        sex: "M",
        condition: "Knee",
        exercises: TestRoutineConfig2,
        //weeklyProgress: 7
    },
    {
        id: "4",
        first_name: "Destry",
        last_name: "Bone",
        date_of_birth: dateNYearsAgo(60),
        email: "test@test.com",
        sex: "F",
        condition: "Knee",
        exercises: TestRoutineConfig,
        //weeklyProgress: 7
    },
    {
        id: "5",
        first_name: "El",
        last_name: "Fizzio",
        date_of_birth: dateNYearsAgo(14),
        email: "test@test.com",
        sex: "F",
        condition: "Ankle",
        //weeklyProgress: 7
    },
]