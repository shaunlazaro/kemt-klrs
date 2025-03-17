package com.example.physiokneeds_v3;

import java.util.List;

public class RepData {
    private int rep_number;
    private double max_flexion;
    private double max_extension;
    private double concentric_time;
    private double eccentric_time;
    private double total_time;
    private boolean goal_flexion_met;
    private boolean goal_extension_met;
    private double max_score;
    private List<String> alerts;
    private List<Pose> poses;

    // Constructor
    public RepData(int repNumber, double maxFlexion, double maxExtension,
                   double concentricTime, double eccentricTime, double totalTime,
                   boolean goalFlexionMet, boolean goalExtensionMet, double score,
                   List<String> alerts, List<Pose> poses) {
        this.rep_number = repNumber;
        this.max_flexion = maxFlexion;
        this.max_extension = maxExtension;
        this.concentric_time = concentricTime;
        this.eccentric_time = eccentricTime;
        this.total_time = totalTime;
        this.goal_flexion_met = goalFlexionMet;
        this.goal_extension_met = goalExtensionMet;
        this.max_score = score;
        this.alerts = alerts;
        this.poses = (poses != null) ? poses : List.of(); // Default empty list
    }

    public RepData(int repNumber) {
        this.rep_number = repNumber;
    }

    // Getters
    public int getRepNumber() { return rep_number; }
    public double getMaxFlexion() { return max_flexion; }
    public double getMaxExtension() { return max_extension; }
    public double getConcentricTime() { return concentric_time; }
    public double getEccentricTime() { return eccentric_time; }
    public double getTotalTime() { return total_time; }
    public boolean isGoalFlexionMet() { return goal_flexion_met; }
    public boolean isGoalExtensionMet() { return goal_extension_met; }
    public double getScore() { return max_score; }
    public List<String> getAlerts() { return alerts; }
    public List<Pose> getPoses() { return poses; }
}

