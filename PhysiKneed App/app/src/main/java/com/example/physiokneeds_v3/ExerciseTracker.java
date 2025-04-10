package com.example.physiokneeds_v3;

import android.util.Log;

import java.util.*;

public class ExerciseTracker {
    private int repCount;
    private String state;
    private boolean repReady;
    private boolean justCompletedRep;
    private long repStartTime;
    private double lastRepDuration;
    private double flexionThreshold;
    private double extensionThreshold;
    private boolean startInFlexion;
    private double currentMaxFlexion;
    private double currentMaxExtension;
    private Double goalFlexion;
    private Double goalExtension;
    private Set<String> alerts;
    private Set<String> alertTrigger;
    private Set<String> lastRepAlerts;
    private double phase1StartTime;
    private double phase2StartTime;
    private double lastConcentricTime;
    private double lastEccentricTime;
    private List<Pose> posesBuffer;
    private double lastPoseCaptureTime;
    private double score;
    private double max_score;
    private double startAngle;


    public ExerciseTracker(ExerciseDetail exerciseDetail) {
        this.repCount = 0;
        this.state = "rest";
        this.repReady = true;
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
        this.alertTrigger = new HashSet<>();
        this.lastRepAlerts = new HashSet<>();
        this.phase1StartTime = 0;
        this.phase2StartTime = 0;
        this.lastConcentricTime = 0;
        this.lastEccentricTime = 0;
        this.posesBuffer = new ArrayList<>();
        this.lastPoseCaptureTime = 0;
        this.score = 0.0;
        this.max_score = 0.0;
        this.startAngle = exerciseDetail.getStartAngle();
    }

    public Set<String> getAlerts() {
        return alerts;
    }

    public Set<String> getAlertTriggers() {
        return alertTrigger;
    }

    public RepData detectReps(List<TrackingResult> trackingResults, ExerciseDetail exerciseDetail, Pose poseData) {
        RepData repEntry = null;
        TrackingDetail mainTrackingDetail = exerciseDetail.getRepTracking();

        Double primaryAngle = getPrimaryAngle(trackingResults, mainTrackingDetail);

        if (primaryAngle == null) {
            return null;
        }

        double currentTime = (double) System.currentTimeMillis() / 1000;
        updateMaxAngles(primaryAngle);
        processAlerts(trackingResults, mainTrackingDetail);
        updateProgressScore(primaryAngle, exerciseDetail.getStartAngle());

        if (currentTime*1000 - lastPoseCaptureTime >= 100) {
            Log.d("POSE_FRAME_DEBUG", "Pose Captured at: " + System.currentTimeMillis());
//            Log.d("ROUTINE_DEBUG", "Pose Example X:" + poseData.getLandmarks().get(0).getX());
//            Log.d("ROUTINE_DEBUG", "Poses Size:" + poseData.getLandmarks().size());
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

        alertTrigger.clear();

        for (TrackingResult entry : trackingResults) {
            TrackingDetail detail = entry.getDetail();
            double value = entry.getAngle();

            if (entry.getDetail().getTrackingType().equals(mainTrackingDetail.getTrackingType())) continue;
            if (detail.getShowAlertIfAbove() != null && value > detail.getShowAlertIfAbove()) {
                if (!alerts.contains(detail.getAlertMessage())) {
                    alertTrigger.add(detail.getAlertMessage());
                }
                alerts.add(detail.getAlertMessage());
                lastRepAlerts.add(detail.getAlertMessage());
            }
            else if (detail.getShowAlertIfBelow() != null && value < detail.getShowAlertIfBelow()) {
                if (!alerts.contains(detail.getAlertMessage())) {
                    alertTrigger.add(detail.getAlertMessage());
                }
                alerts.add(detail.getAlertMessage());
                lastRepAlerts.add(detail.getAlertMessage());
            } else {
                // self.alerts.discard(detail.alert_message)
                alerts.remove(detail.getAlertMessage());
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

        Log.d("ROUTINE_DEBUG_", "Progress: " + String.valueOf(progress));
        score = Math.max(0.0, Math.min(1.0, progress));
        max_score = Math.max(score, max_score);

        Log.d("ROUTINE_DEBUG_", "Score: " + String.valueOf(score));
        Log.d("ROUTINE_DEBUG_", "Max Score: " + String.valueOf(max_score));
    }

    private void updateState(double primaryAngle, double currentTime) {
        switch (state) {
            case "rest":
                if (isFullRepCompleted(primaryAngle)) {
                    repReady = true;
                    startNewRep(currentTime);
                }

                break;
            case "phase_1":
                if (repReady) {
                    if (isHalfRepCompleted(primaryAngle)) startPhase2(currentTime);
                }
                break;
            case "phase_2":
                if (repReady) {
                    if (isFullRepCompleted(primaryAngle)) {
                        completeRep(currentTime);
                        repReady = false;
                    }
                }
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

    private void startNewRep(double currentTime) {
        state = "phase_1";
        phase1StartTime = currentTime;
        currentMaxFlexion = Double.POSITIVE_INFINITY;
        currentMaxExtension = Double.NEGATIVE_INFINITY;
        posesBuffer.clear();
        justCompletedRep = false;
    }

    private void startPhase2(double currentTime) {
        state = "phase_2";
        phase2StartTime = currentTime;
        lastConcentricTime = currentTime - phase1StartTime;
    }

    private void completeRep(double currentTime) {
        state = "rest";
        lastEccentricTime = currentTime - phase2StartTime;
        lastRepDuration = lastConcentricTime + lastEccentricTime;
        repCount++;
        justCompletedRep = true;
    }

    private RepData finalizeRep(double currentTime, TrackingDetail mainTrackingDetail, Float minRepTime) {
        boolean flexionGoalMet = goalFlexion == null || currentMaxFlexion <= goalFlexion;
        boolean extensionGoalMet = goalExtension == null || currentMaxExtension >= goalExtension;

        if (!flexionGoalMet || !extensionGoalMet){
            alertTrigger.add(mainTrackingDetail.getAlertMessage());
            alerts.add(mainTrackingDetail.getAlertMessage());
        }
        if (lastConcentricTime + lastEccentricTime < minRepTime){
            alertTrigger.add("Slow down your movement");
            alerts.add("Slow down your movement");
        }

        lastRepAlerts.addAll(alerts);

        Log.d("POSE_FRAME_DEBUG", "poseBufferSize: " + posesBuffer.size());
        RepData repEntry = new RepData(repCount, currentMaxFlexion, currentMaxExtension, lastConcentricTime, lastEccentricTime, lastRepDuration, flexionGoalMet, extensionGoalMet, max_score, new ArrayList<>(lastRepAlerts), posesBuffer);
        Log.d("ROUTINE_DEBUG", "Pose Size: " + repEntry.getPoses().size());

        printRepFeedback();
        resetRepTracking(currentTime);

        Log.d("ROUTINE_DEBUG", "Pose Size After: " + repEntry.getPoses().size());

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

    public void resetRepTracking(double currentTime) {
        currentMaxFlexion = Float.POSITIVE_INFINITY;
        currentMaxExtension = Float.NEGATIVE_INFINITY;
        phase1StartTime = currentTime;
        alerts.clear();
        posesBuffer = new ArrayList<>();
        lastPoseCaptureTime = 0;
        score = 0;
        max_score = 0;
        lastRepAlerts.clear();
    }

    public int getRepCount() {
        return repCount;
    }

    public Set<String> getLastRepAlerts() {
        return lastRepAlerts;
    }

    public Double getMaxForBar() {
        if (startInFlexion) {
            return goalExtension;
        } else {
            return (startAngle - goalFlexion);
        }
    }

    public Double getMinForBar() {
        if (startInFlexion) {
            return startAngle;
        } else {
            return 0.0;
        }
    }

    public Double getAngleBarVal(List<TrackingResult> trackingResults, ExerciseDetail exerciseDetail) {
        TrackingDetail mainTrackingDetail = exerciseDetail.getRepTracking();

        Double primaryAngle = getPrimaryAngle(trackingResults, mainTrackingDetail);

        if (primaryAngle == null) {
            return 0.0;
        }

        if (startInFlexion) {
            return primaryAngle;
        } else {
            return (180 - primaryAngle);
        }
    }
    public Double getAngleValGreen() {
        if (startInFlexion) {
            return extensionThreshold;
        } else {
            return (startAngle - flexionThreshold);
        }
    }
}
