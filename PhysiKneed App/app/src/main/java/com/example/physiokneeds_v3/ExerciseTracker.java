package com.example.physiokneeds_v3;

import android.util.Log;

import java.util.*;

public class ExerciseTracker {
    private int repCount;
    private String state;
    private boolean justCompletedRep;
    private long repStartTime;
    private long lastRepDuration;
    private double flexionThreshold;
    private double extensionThreshold;
    private boolean startInFlexion;
    private double currentMaxFlexion;
    private double currentMaxExtension;
    private Double goalFlexion;
    private Double goalExtension;
    private Set<String> alerts;
    private Set<String> lastRepAlerts;
    private long phase1StartTime;
    private long phase2StartTime;
    private long lastConcentricTime;
    private long lastEccentricTime;
    private List<Pose> posesBuffer;
    private long lastPoseCaptureTime;
    private Float score;

    public ExerciseTracker(ExerciseDetail exerciseDetail) {
        this.repCount = 0;
        this.state = "rest";
        this.justCompletedRep = false;
        this.repStartTime = 0;
        this.lastRepDuration = 0;
        this.flexionThreshold = exerciseDetail.getThresholdFlexion();
        this.extensionThreshold = exerciseDetail.getThresholdExtension();
        this.startInFlexion = exerciseDetail.getStartInFlexion();
        this.currentMaxFlexion = Double.POSITIVE_INFINITY;
        this.currentMaxExtension = Double.NEGATIVE_INFINITY;
        this.goalFlexion = exerciseDetail.getRepTracking().getGoalFlexion();
        this.goalExtension = exerciseDetail.getRepTracking().getGoalExtension();
        this.alerts = new HashSet<>();
        this.lastRepAlerts = new HashSet<>();
        this.phase1StartTime = 0;
        this.phase2StartTime = 0;
        this.lastConcentricTime = 0;
        this.lastEccentricTime = 0;
        this.posesBuffer = new ArrayList<>();
        this.lastPoseCaptureTime = 0;
        this.score = 0f;
    }

    public RepData detectReps(List<TrackingResult> trackingResults, ExerciseDetail exerciseDetail, Pose poseData) {
        RepData repEntry = null;
        TrackingDetail mainTrackingDetail = exerciseDetail.getRepTracking();

        Double primaryAngle = getPrimaryAngle(trackingResults, mainTrackingDetail);

        if (primaryAngle == null) {
            return null;
        }

        long currentTime = System.currentTimeMillis() / 1000;
        updateMaxAngles(primaryAngle);
        processAlerts(trackingResults, mainTrackingDetail);
        updateProgressScore(primaryAngle, exerciseDetail.getStartAngle());

        if (currentTime - lastPoseCaptureTime >= 100) {
            posesBuffer.add(poseData);
            lastPoseCaptureTime = currentTime;
        }

        updateState(primaryAngle, currentTime);

        if ("rest".equals(state) && justCompletedRep) {
            repEntry = finalizeRep(currentTime, mainTrackingDetail, exerciseDetail.getMinRepTime());
            justCompletedRep = false;
        }
        return repEntry;
    }

    private Double getPrimaryAngle(List<TrackingResult> trackingResults, TrackingDetail mainTrackingDetail) {
        for (TrackingResult entry : trackingResults) {
            Log.d("ROUTINE_DEBUG", "Entry Detail:" + entry.getDetail().getKeypoints().toString());
            Log.d("ROUTINE_DEBUG", "Main Detail:" + mainTrackingDetail.getKeypoints().toString());
            Log.d("ROUTINE_DEBUG", "Angle: " + entry.getAngle().toString());

            // REVISIT
            if (entry.getDetail().getTrackingType().equals(mainTrackingDetail.getTrackingType())) {
                return entry.getAngle();
            }
        }
        return null;
    }

    private void updateMaxAngles(double primaryAngle) {
        currentMaxFlexion = Math.min(currentMaxFlexion, primaryAngle);
        currentMaxExtension = Math.max(currentMaxExtension, primaryAngle);
    }

    private void processAlerts(List<TrackingResult> trackingResults, TrackingDetail mainTrackingDetail) {
        for (TrackingResult entry : trackingResults) {
            TrackingDetail detail = entry.getDetail();
            double value = entry.getAngle();

            if (entry.getDetail().getTrackingType().equals(mainTrackingDetail.getTrackingType())) continue;
            if (detail.getShowAlertIfAbove() != null && value > detail.getShowAlertIfAbove()) {
                alerts.add(detail.getAlertMessage());
            }
            if (detail.getShowAlertIfBelow() != null && value < detail.getShowAlertIfBelow()) {
                alerts.add(detail.getAlertMessage());
            }
        }
    }

    private void updateProgressScore(double primaryAngle, double startAngle) {
        double progress;
        if (startInFlexion) {
            progress = (startAngle - primaryAngle) / (startAngle - goalExtension);
        } else {
            progress = (primaryAngle - startAngle) / (goalFlexion - startAngle);
        }

        score = (float) Math.max(0, Math.min(1, progress));
    }

    private void updateState(double primaryAngle, long currentTime) {
        switch (state) {
            case "rest":
                if (isStartOfRep(primaryAngle)) startNewRep(currentTime);
                break;
            case "phase_1":
                if (isHalfRepCompleted(primaryAngle)) startPhase2(currentTime);
                break;
            case "phase_2":
                if (isFullRepCompleted(primaryAngle)) completeRep(currentTime);
                break;
        }
    }

    private boolean isStartOfRep(double angle) {
        return (startInFlexion && angle > flexionThreshold) || (!startInFlexion && angle < extensionThreshold);
    }

    private boolean isHalfRepCompleted(double angle) {
        return (startInFlexion && angle > extensionThreshold) || (!startInFlexion && angle < flexionThreshold);
    }

    private boolean isFullRepCompleted(double angle) {
        return (startInFlexion && angle < flexionThreshold) || (!startInFlexion && angle > extensionThreshold);
    }

    private void startNewRep(long currentTime) {
        state = "phase_1";
        phase1StartTime = currentTime;
        currentMaxFlexion = Double.POSITIVE_INFINITY;
        currentMaxExtension = Double.NEGATIVE_INFINITY;
        posesBuffer.clear();
        justCompletedRep = false;
    }

    private void startPhase2(long currentTime) {
        state = "phase_2";
        phase2StartTime = currentTime;
        lastConcentricTime = currentTime - phase1StartTime;
    }

    private void completeRep(long currentTime) {
        state = "rest";
        lastEccentricTime = currentTime - phase2StartTime;
        lastRepDuration = lastConcentricTime + lastEccentricTime;
        repCount++;
        justCompletedRep = true;
    }

    private RepData finalizeRep(long currentTime, TrackingDetail mainTrackingDetail, Float minRepTime) {
        boolean flexionGoalMet = goalFlexion == null || currentMaxFlexion <= goalFlexion;
        boolean extensionGoalMet = goalExtension == null || currentMaxExtension >= goalExtension;

        if (!flexionGoalMet || !extensionGoalMet) alerts.add(mainTrackingDetail.getAlertMessage());
        if (lastConcentricTime + lastEccentricTime < minRepTime) alerts.add("Slow down your movement");

        lastRepAlerts = new HashSet<>(alerts);

        RepData repEntry = new RepData(repCount, currentMaxFlexion, currentMaxExtension, lastConcentricTime, lastEccentricTime, lastRepDuration, flexionGoalMet, extensionGoalMet, score, new ArrayList<>(lastRepAlerts), posesBuffer);

        printRepFeedback();
        resetRepTracking(currentTime);

        return repEntry;
    }

    public void printRepFeedback() {
        String FEEDBACK_TAG = "FeedbackForEachRep";
        Log.d(FEEDBACK_TAG, "Rep "+ repCount + " completed in " + lastRepDuration + " sec");
        Log.d(FEEDBACK_TAG, "Concentric: " + lastConcentricTime+ " sec, Eccentric: " + lastEccentricTime + " sec");
        Log.d(FEEDBACK_TAG, "Max Flexion: " + currentMaxFlexion + "°, Max Extension: "+ currentMaxExtension + "°");

        for (String alert : alerts) {
            Log.d(FEEDBACK_TAG,"⚠️ " + alert);
        }
    }

    public void resetRepTracking(long currentTime) {
        currentMaxFlexion = Float.POSITIVE_INFINITY;
        currentMaxExtension = Float.NEGATIVE_INFINITY;
        phase1StartTime = currentTime;
        alerts.clear();
        posesBuffer.clear();
        lastPoseCaptureTime = 0;
    }

    public int getRepCount() {
        return repCount;
    }

    public Set<String> getLastRepAlerts() {
        return lastRepAlerts;
    }
}
