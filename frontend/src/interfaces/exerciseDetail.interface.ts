// Represents a specific joint being tracked, with the option for 
export interface TrackingDetail {
    tracking_type: string,
    keypoints: string[],
    show_alert_if_above?: number,
    show_alert_if_below?: number,
    alert_message?: string,
}
export interface ExerciseDetail {
    // rep_tracking: TrackingDetail,
    rep_keypoints: string[],
    threshold_flexion: number,
    threshold_extension: number,
    display_name: string,
    default_tracking_details?: TrackingDetail[]
}