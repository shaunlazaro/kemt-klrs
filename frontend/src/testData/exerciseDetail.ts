import { ExerciseDetail } from "../interfaces/exerciseDetail.interface";

export const ExerciseMockA: ExerciseDetail = {
    id: "1",
    rep_keypoints: ["a", "b", "c"],
    threshold_flexion: 0,
    threshold_extension: 90,
    display_name: "Mock Exercise A",
    start_in_flexion: true,
}
export const ExerciseMockB: ExerciseDetail = {
    id: "2",
    rep_keypoints: ["a", "b", "c"],
    threshold_flexion: 0,
    threshold_extension: 90,
    display_name: "Mock Exercise B",
    start_in_flexion: true,
}
export const ExerciseMockC: ExerciseDetail = {
    id: "3",
    rep_keypoints: ["a", "b", "c"],
    threshold_flexion: 0,
    threshold_extension: 90,
    display_name: "Mock Exercise C",
    start_in_flexion: true,
}
export const ExerciseMockD: ExerciseDetail = {
    id: "4",
    rep_keypoints: ["a", "b", "c"],
    threshold_flexion: 0,
    threshold_extension: 90,
    display_name: "Mock Exercise D",
    start_in_flexion: true,
}

export const ExerciseList: ExerciseDetail[] = [ExerciseMockA, ExerciseMockB, ExerciseMockC, ExerciseMockD]