// Represents a specific joint being tracked, with the option for 
export interface TrackingDetail {
    id: string
    tracking_type: string,
    keypoints: string[],
    dimensionality?: string,
    goal_flexion: number,
    goal_extension: number,
    show_alert_if_above?: number,
    show_alert_if_below?: number,
    alert_message?: string,
}
export interface ExerciseDetail {
    id: string,
    rep_tracking?: TrackingDetail, // Made optional in this version because we don't care about this on the front end probably
    // Rep tracking has been replaced by rep_tracking field, not these three below.
    rep_keypoints: string[], // Old, but kept in temporarily
    threshold_flexion: number, // Old, but kept in temporarily
    threshold_extension: number, // Old, but kept in temporarily
    display_name: string,
    start_in_flexion: boolean,
    body_alignment?: string, // Might be deprecated, made optional.
    default_tracking_details?: TrackingDetail[],
    instruction?: string
}