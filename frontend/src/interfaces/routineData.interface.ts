import { ExerciseDetail } from "./exerciseDetail.interface";
import { RoutineComponent, RoutineConfig } from "./exercisePlan.interface";

export interface Pose {
    // id: number; // Assuming DRF includes an ID field
    landmarks: any[]; // List of landmarks (structure depends on your data)
    // created_at: string; // ISO timestamp
}

export interface RepData {
    id: string;
    rep_number: number;
    max_flexion: number;
    max_extension: number;
    concentric_time: number;
    eccentric_time: number;
    total_time: number;
    goal_flexion_met: boolean;
    goal_extension_met: boolean;
    max_score: number;
    alerts: string[]; // List of alert messages
    poses: Pose[]; // Full Pose objects
    // poses: any[]; // TODO: Fix this
}

export interface RoutineComponentData {
    id: string;
    exercise_detail: ExerciseDetail;
    // routine_component: RoutineComponent;
    rep_data: RepData[];
}

export interface RoutineData {
    id: string;
    routine_config: RoutineConfig; // RoutineConfig ID or obj
    routine_component_data: RoutineComponentData[];
    created_at: string; // ISO timestamp
}