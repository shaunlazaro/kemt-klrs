import { Patient } from "../interfaces/patient.interface";
import { TestRoutineConfig, TestRoutineConfig2 } from "./exercisePlans";

export const MockPatients: Patient[] = [
    {
        userId: "1",
        name: "Al Liver",
        age: 48,
        sex: "M",
        condition: "Knee",
        exercises: TestRoutineConfig,
        weeklyProgress: 7
    },
    {
        userId: "2",
        name: "Bourne Happy",
        age: 70,
        sex: "F",
        condition: "Ankle",
        exercises: TestRoutineConfig2,
        weeklyProgress: 2
    },
    {
        userId: "3",
        name: "Craig Gnee",
        age: 24,
        sex: "M",
        condition: "Knee",
        exercises: TestRoutineConfig2,
        weeklyProgress: 7
    },
    {
        userId: "4",
        name: "Destry Bone",
        age: 60,
        sex: "F",
        condition: "Knee",
        exercises: TestRoutineConfig,
        weeklyProgress: 7
    },
    {
        userId: "1",
        name: "El Fizzio",
        age: 13,
        sex: "F",
        condition: "Ankle",
        weeklyProgress: 7
    },
]