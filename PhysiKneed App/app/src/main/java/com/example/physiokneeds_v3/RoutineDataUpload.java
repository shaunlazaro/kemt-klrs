package com.example.physiokneeds_v3;

import java.io.Serializable;
import java.util.List;

public class RoutineDataUpload implements Serializable {
    private String routineConfig_id;
    private List<RoutineComponentDataUpload> routine_component_data;
    private String notes;

    // Constructor
    public RoutineDataUpload(String routineConfig, List<RoutineComponentDataUpload> routineComponentData) {
        this.routineConfig_id = routineConfig;
        this.routine_component_data = routineComponentData;
    }

    // Getters
    public String getRoutineConfig() { return routineConfig_id; }
    public List<RoutineComponentDataUpload> getRoutineComponentData() { return routine_component_data; }
}
