package com.example.physiokneeds_v3;

import java.io.Serializable;
import java.util.List;

public class ExerciseDetail implements Serializable {
    private TrackingDetail rep_tracking;
    private List<String> rep_keypoints;
    private Float start_angle;
    private Float min_rep_time;
    private Integer threshold_flexion;
    private Integer threshold_extension;
    private String display_name;
    private Boolean start_in_flexion;
    private String body_alignment;
    private List<TrackingDetail> default_tracking_details;
    private String instruction;

    // Default constructor
    public ExerciseDetail() {}

    public TrackingDetail getRepTracking() {
        return rep_tracking;
    }

    public void setRepTracking(TrackingDetail repTracking) {
        this.rep_tracking = repTracking;
    }

    public List<String> getRepKeypoints() {
        return rep_keypoints;
    }

    public void setRepKeypoints(List<String> repKeypoints) {
        this.rep_keypoints = repKeypoints;
    }

    public Integer getThresholdFlexion() {
        return threshold_flexion;
    }

    public void setStartAngle(Float startAngle) {
        start_angle = startAngle;
    }

    public Float getStartAngle() {
        return start_angle;
    }
    public void setMinRepTime(Float minRepTime) {
        min_rep_time = minRepTime;
    }

    public Float getMinRepTime() {
        return min_rep_time;
    }

    public void setThresholdFlexion(Integer thresholdFlexion) {
        this.threshold_flexion = thresholdFlexion;
    }

    public Integer getThresholdExtension() {
        return threshold_extension;
    }

    public void setThresholdExtension(Integer thresholdExtension) {
        this.threshold_extension = thresholdExtension;
    }

    public String getDisplayName() {
        return display_name;
    }

    public void setDisplayName(String displayName) {
        this.display_name = displayName;
    }

    public Boolean getStartInFlexion() {
        return start_in_flexion;
    }

    public void setStartInFlexion(Boolean startInFlexion) {
        this.start_in_flexion = startInFlexion;
    }

    public String getBodyAlignment() {
        return body_alignment;
    }

    public void setBodyAlignment(String bodyAlignment) {
        this.body_alignment = bodyAlignment;
    }

    public List<TrackingDetail> getDefaultTrackingDetails() {
        return default_tracking_details;
    }

    public void setDefaultTrackingDetails(List<TrackingDetail> defaultTrackingDetails) {
        this.default_tracking_details = defaultTrackingDetails;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }
}
