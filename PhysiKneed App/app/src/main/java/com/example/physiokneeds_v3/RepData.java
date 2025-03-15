package com.example.physiokneeds_v3;

import java.util.List;

public class RepData {
    private int repNumber;
    private double maxFlexion;
    private double maxExtension;
    private double concentricTime;
    private double eccentricTime;
    private double totalTime;
    private boolean goalFlexionMet;
    private boolean goalExtensionMet;
    private double score;
    private List<String> alerts;
    private List<Pose> poses;

    // Constructor
    public RepData(int repNumber, double maxFlexion, double maxExtension,
                   double concentricTime, double eccentricTime, double totalTime,
                   boolean goalFlexionMet, boolean goalExtensionMet, double score,
                   List<String> alerts, List<Pose> poses) {
        this.repNumber = repNumber;
        this.maxFlexion = maxFlexion;
        this.maxExtension = maxExtension;
        this.concentricTime = concentricTime;
        this.eccentricTime = eccentricTime;
        this.totalTime = totalTime;
        this.goalFlexionMet = goalFlexionMet;
        this.goalExtensionMet = goalExtensionMet;
        this.score = score;
        this.alerts = alerts;
        this.poses = (poses != null) ? poses : List.of(); // Default empty list
    }

    // Getters
    public int getRepNumber() { return repNumber; }
    public double getMaxFlexion() { return maxFlexion; }
    public double getMaxExtension() { return maxExtension; }
    public double getConcentricTime() { return concentricTime; }
    public double getEccentricTime() { return eccentricTime; }
    public double getTotalTime() { return totalTime; }
    public boolean isGoalFlexionMet() { return goalFlexionMet; }
    public boolean isGoalExtensionMet() { return goalExtensionMet; }
    public double getScore() { return score; }
    public List<String> getAlerts() { return alerts; }
    public List<Pose> getPoses() { return poses; }
}

