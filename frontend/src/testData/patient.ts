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
        userId: "1",
        firstName: "Al",
        lastName: "Liver",
        dateOfBirth: dateNYearsAgo(40),
        email: "test@test.com",
        sex: "M",
        condition: "Knee",
        exercises: TestRoutineConfig,
        weeklyProgress: 7
    },
    {
        userId: "2",
        firstName: "Bourne",
        lastName: "Happy",
        dateOfBirth: dateNYearsAgo(70),
        email: "test@test.com",
        sex: "F",
        condition: "Ankle",
        exercises: TestRoutineConfig2,
        weeklyProgress: 2
    },
    {
        userId: "3",
        firstName: "Craig",
        lastName: "Gnee",
        dateOfBirth: dateNYearsAgo(24),
        email: "test@test.com",
        sex: "M",
        condition: "Knee",
        exercises: TestRoutineConfig2,
        weeklyProgress: 7
    },
    {
        userId: "4",
        firstName: "Destry",
        lastName: "Bone",
        dateOfBirth: dateNYearsAgo(60),
        email: "test@test.com",
        sex: "F",
        condition: "Knee",
        exercises: TestRoutineConfig,
        weeklyProgress: 7
    },
    {
        userId: "5",
        firstName: "El",
        lastName: "Fizzio",
        dateOfBirth: dateNYearsAgo(14),
        email: "test@test.com",
        sex: "F",
        condition: "Ankle",
        weeklyProgress: 7
    },
]