package com.example.physiokneeds_v3;

import java.io.Serializable;
import java.util.List;

public class TrackingDetail implements Serializable {
    private String tracking_type;
    private List<String> keypoints;
    private String dimensionality;
    private Integer goal_flexion;
    private Integer goal_extension;
    private Integer show_alert_if_above;
    private Integer show_alert_if_below;
    private String alert_message;

    public TrackingDetail() {}

    public String getTrackingType() {
        return tracking_type;
    }

    public void setTrackingType(String trackingType) {
        this.tracking_type = trackingType;
    }

    public List<String> getKeypoints() {
        return keypoints;
    }

    public void setKeypoints(List<String> keypoints) {
        this.keypoints = keypoints;
    }

    public String getDimensionality() {
        return dimensionality;
    }

    public void setDimensionality(String dimensionality) {
        this.dimensionality = dimensionality;
    }

    public Integer getGoalFlexion() {
        return goal_flexion;
    }

    public void setGoalFlexion(Integer goalFlexion) {
        this.goal_flexion = goalFlexion;
    }

    public Integer getGoalExtension() {
        return goal_extension;
    }

    public void setGoalExtension(Integer goalExtension) {
        this.goal_extension = goalExtension;
    }

    public Integer getShowAlertIfAbove() {
        return show_alert_if_above;
    }

    public void setShowAlertIfAbove(Integer showAlertIfAbove) {
        this.show_alert_if_above = showAlertIfAbove;
    }

    public Integer getShowAlertIfBelow() {
        return show_alert_if_below;
    }

    public void setShowAlertIfBelow(Integer showAlertIfBelow) {
        this.show_alert_if_below = showAlertIfBelow;
    }

    public String getAlertMessage() {
        return alert_message;
    }

    public void setAlertMessage(String alertMessage) {
        this.alert_message = alertMessage;
    }
}
