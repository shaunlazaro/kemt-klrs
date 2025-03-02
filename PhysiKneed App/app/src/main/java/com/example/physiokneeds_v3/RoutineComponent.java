package com.example.physiokneeds_v3;

import java.io.Serializable;
import java.util.List;

public class RoutineComponent implements Serializable {
    private ExerciseDetail exercise;
    private Integer reps;
    private List<TrackingDetail> customTrackingDetails;

    public RoutineComponent() {}

    public ExerciseDetail getExercise() {
        return exercise;
    }

    public void setExercise(ExerciseDetail exercise) {
        this.exercise = exercise;
    }

    public Integer getReps() {
        return reps;
    }

    public void setReps(Integer reps) {
        this.reps = reps;
    }

    public List<TrackingDetail> getCustomTrackingDetails() {
        return customTrackingDetails;
    }

    public void setCustomTrackingDetails(List<TrackingDetail> customTrackingDetails) {
        this.customTrackingDetails = customTrackingDetails;
    }
}
