package com.example.physiokneeds_v3;

import java.util.List;

public class RoutineComponentData {
    private RoutineComponent routineComponent;
    private List<RepData> repData;

    // Constructor
    public RoutineComponentData(RoutineComponent routineComponent, List<RepData> repData) {
        this.routineComponent = routineComponent;
        this.repData = repData;
    }

    // Getters
    public RoutineComponent getRoutineComponent() { return routineComponent; }
    public List<RepData> getRepData() { return repData; }
}
