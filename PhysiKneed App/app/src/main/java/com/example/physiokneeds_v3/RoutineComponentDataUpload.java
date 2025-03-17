package com.example.physiokneeds_v3;

import java.util.List;

public class RoutineComponentDataUpload {
    private String exercise_detail;
    private List<RepData> rep_data;

    // Constructor
    public RoutineComponentDataUpload(String exerciseDetail, List<RepData> repData) {
        this.exercise_detail = exerciseDetail;
        this.rep_data = repData;
    }

    // Getters
    public String getRoutineComponent() { return exercise_detail; }
    public List<RepData> getRepData() { return rep_data; }
}
