package com.example.physiokneeds_v3;

import java.io.Serializable;
import java.util.List;

public class RoutineComponentDataUpload implements Serializable {
    private String exercise_detail_id;
    private List<RepData> rep_data;
    private int rating;

    // Constructor
    public RoutineComponentDataUpload(String exerciseDetail, List<RepData> repData) {
        this.exercise_detail_id = exerciseDetail;
        this.rep_data = repData;
    }

    // Getters
    public String getRoutineComponent() { return exercise_detail_id; }
    public List<RepData> getRepData() { return rep_data; }
}
