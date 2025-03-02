package com.example.physiokneeds_v3;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class ExerciseTracker {
    private int repCount = 0;
    private String state = "rest";
    private Double repStartTime = null;
    private double lastRepDuration = 0;
    private double flexionThreshold;
    private double extensionThreshold;
    private boolean startInFlexion;
    private double currentMaxFlexion = Double.POSITIVE_INFINITY;
    private double currentMaxExtension = Double.NEGATIVE_INFINITY;
    private Integer goalFlexion;
    private Integer goalExtension;
    private String alertMessage;
    private String alert = null;
    private Double phase1StartTime = null;
    private Double phase2StartTime = null;
    private double lastConcentricTime = 0;
    private double lastEccentricTime = 0;
    private Deque<Map<String, Object>> repData = new ArrayDeque<>(50);

    public ExerciseTracker(ExerciseDetail exerciseDetail) {
        this.flexionThreshold = exerciseDetail.getThresholdFlexion();
        this.extensionThreshold = exerciseDetail.getThresholdExtension();
        this.startInFlexion = exerciseDetail.getStartInFlexion();

        TrackingDetail trackingDetail = exerciseDetail.getDefaultTrackingDetails().get(0);
        this.goalFlexion = trackingDetail.getGoalFlexion();
        this.goalExtension = trackingDetail.getGoalExtension();
        this.alertMessage = trackingDetail.getAlertMessage();
    }

    public int[] detectReps(double kneeAngle) {
        double currentTime = System.currentTimeMillis() / 1000.0;

        currentMaxFlexion = Math.min(currentMaxFlexion, kneeAngle);
        currentMaxExtension = Math.max(currentMaxExtension, kneeAngle);

        Log.e("ExerciseTracker", state);
        Log.e("ExerciseTracker", String.valueOf(kneeAngle));

        switch (state) {
            case "rest":
                Log.e("ExerciseTracker", "Here!");
                if ((startInFlexion && kneeAngle < flexionThreshold) || (!startInFlexion && kneeAngle > extensionThreshold)) {
                    state = "phase_1";
                    Log.e("ExerciseTracker", "Here Too!");
                    phase1StartTime = currentTime;
                    currentMaxFlexion = kneeAngle;
                    currentMaxExtension = kneeAngle;
                }
                break;

            case "phase_1":
                if ((startInFlexion && kneeAngle > extensionThreshold) || (!startInFlexion && kneeAngle < flexionThreshold)) {
                    state = "phase_2";
                    phase2StartTime = currentTime;
                    lastConcentricTime = phase2StartTime - phase1StartTime;
                }
                break;

            case "phase_2":
                if ((startInFlexion && kneeAngle < flexionThreshold) || (!startInFlexion && kneeAngle > extensionThreshold)) {
                    state = "phase_1";
                    lastEccentricTime = currentTime - phase2StartTime;
                    lastRepDuration = lastConcentricTime + lastEccentricTime;
                    repCount++;

                    boolean flexionGoalMet = goalFlexion == null || currentMaxFlexion <= goalFlexion;
                    boolean extensionGoalMet = goalExtension == null || currentMaxExtension >= goalExtension;

                    alert = (!flexionGoalMet || !extensionGoalMet) ? alertMessage : null;

                    Map<String, Object> repInfo = new HashMap<>();
                    repInfo.put("rep_number", repCount);
                    repInfo.put("max_flexion", currentMaxFlexion);
                    repInfo.put("max_extension", currentMaxExtension);
                    repInfo.put("concentric_time", lastConcentricTime);
                    repInfo.put("eccentric_time", lastEccentricTime);
                    repInfo.put("total_time", lastRepDuration);
                    repInfo.put("goal_flexion_met", flexionGoalMet);
                    repInfo.put("goal_extension_met", extensionGoalMet);
                    repInfo.put("alert", alert);
                    repData.add(repInfo);

                    currentMaxFlexion = Double.POSITIVE_INFINITY;
                    currentMaxExtension = Double.NEGATIVE_INFINITY;
                    phase1StartTime = currentTime;
                }
                break;
        }
        return new int[]{repCount, (int) lastRepDuration};
    }
}
