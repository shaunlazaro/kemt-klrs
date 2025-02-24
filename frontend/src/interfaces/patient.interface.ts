export interface Patient{
    userId: string;
    name: string;
    age: number;
    sex: string;
    condition: string[];
    exercises: string[];
    weeklyProgress: number; // out of 7?
}