package com.example.physiokneeds_v3;

import java.util.List;

public class RoutineComponentData {
    private ExerciseDetail exerciseDetail ;
    private List<RepData> repData;
    private int rating;

    // Constructor
    public RoutineComponentData(ExerciseDetail exerciseDetail, List<RepData> repData) {
        this.exerciseDetail = exerciseDetail;
        this.repData = repData;
    }

    // Getters
    public ExerciseDetail getRoutineComponent() { return exerciseDetail; }
    public List<RepData> getRepData() { return repData; }
}
