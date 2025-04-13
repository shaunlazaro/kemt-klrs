package com.example.physiokneeds_v3;

import java.io.Serializable;
import java.util.List;

public class RoutineData implements Serializable {
    private String id;
    private RoutineConfig routine_config;
    private List<RoutineComponentData> routine_component_data;
    private String created_at;
    private String notes;

    // Constructor
    public RoutineData() {
    }

    // Getters
    public RoutineConfig getRoutineConfig() { return routine_config; }
    public List<RoutineComponentData> getRoutineComponentData() { return routine_component_data; }
    public String getId() { return id; }
    public String getCreated_at() { return created_at; }
}
