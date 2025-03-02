package com.example.physiokneeds_v3;

import java.io.Serializable;
import java.util.List;

public class RoutineConfig implements Serializable {
    private String name;
    private List<RoutineComponent> exercises;
    private String injury;

    public RoutineConfig() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RoutineComponent> getExercises() {
        return exercises;
    }

    public void setExercises(List<RoutineComponent> exercises) {
        this.exercises = exercises;
    }

    public String getInjury() {
        return injury;
    }

    public void setInjury(String injury) {
        this.injury = injury;
    }
}
