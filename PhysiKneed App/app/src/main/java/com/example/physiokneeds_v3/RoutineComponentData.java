package com.example.physiokneeds_v3;

import java.io.Serializable;
import java.util.List;

public class RoutineComponentData implements Serializable {
    private ExerciseDetail exercise_detail ;
    private List<RepData> rep_data;
    private int rating;

    // Constructor
    public RoutineComponentData(ExerciseDetail exerciseDetail, List<RepData> repData) {
        this.exercise_detail = exerciseDetail;
        this.rep_data = repData;
    }

    // Getters
    public ExerciseDetail getRoutineComponent() { return exercise_detail; }
    public List<RepData> getRepData() { return rep_data; }
}
