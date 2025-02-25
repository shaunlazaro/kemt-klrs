// // Represents a specific joint being tracked, with the option for 
// export interface TrackingDetail
// {
//     tracking_type: string,
//     keypoints: string[],
//     show_alert_if_above?: number,
//     show_alert_if_below?: number,
//     alert_message?: string,
// }
// export interface ExerciseDetail
// {
//     rep_keypoints: string[],
//     threshold_flexion: number,
//     threshold_extension: number,
//     display_name: string,
//     default_tracking_details?: TrackingDetail[]
// }

import { ExerciseDetail } from "../interfaces/exerciseDetail.interface";

export const ExerciseMockA: ExerciseDetail = {
    rep_keypoints: ["a", "b", "c"],
    threshold_flexion: 0,
    threshold_extension: 90,
    display_name: "Mock Exercise A",
}
export const ExerciseMockB: ExerciseDetail = {
    rep_keypoints: ["a", "b", "c"],
    threshold_flexion: 0,
    threshold_extension: 90,
    display_name: "Mock Exercise B",
}
export const ExerciseMockC: ExerciseDetail = {
    rep_keypoints: ["a", "b", "c"],
    threshold_flexion: 0,
    threshold_extension: 90,
    display_name: "Mock Exercise C",
}
export const ExerciseMockD: ExerciseDetail = {
    rep_keypoints: ["a", "b", "c"],
    threshold_flexion: 0,
    threshold_extension: 90,
    display_name: "Mock Exercise D",
}