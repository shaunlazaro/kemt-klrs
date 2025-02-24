import { Patient } from "../interfaces/patient.interface";

export const MockPatients: Patient[] = [
    {
        userId: "1",
        name: "Al Liver",
        age: 48,
        sex: "M",
        condition: ["Knee"],
        exercises: ["1"],
        weeklyProgress: 7
    },
    {
        userId: "2",
        name: "Bourne Happy",
        age: 70,
        sex: "F",
        condition: ["Ankle"],
        exercises: ["2"],
        weeklyProgress: 2
    },
    {
        userId: "3",
        name: "Craig Gnee",
        age: 24,
        sex: "M",
        condition: ["Knee"],
        exercises: ["1", "2"],
        weeklyProgress: 7
    },
    {
        userId: "4",
        name: "Destry Bone",
        age: 60,
        sex: "F",
        condition: ["Knee"],
        exercises: ["3"],
        weeklyProgress: 7
    },
    {
        userId: "1",
        name: "El Fizzio",
        age: 13,
        sex: "F",
        condition: ["Knee", "Ankle"],
        exercises: ["1", "2", "3"],
        weeklyProgress: 7
    },
]