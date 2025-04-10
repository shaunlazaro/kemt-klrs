package com.example.physiokneeds_v3;

import java.util.List;

public class RoutineData {
    private RoutineConfig routineConfig;
    private List<RoutineComponentData> routineComponentData;
    private String notes;

    // Constructor
    public RoutineData(RoutineConfig routineConfig, List<RoutineComponentData> routineComponentData) {
        this.routineConfig = routineConfig;
        this.routineComponentData = routineComponentData;
    }

    // Getters
    public RoutineConfig getRoutineConfig() { return routineConfig; }
    public List<RoutineComponentData> getRoutineComponentData() { return routineComponentData; }
}
