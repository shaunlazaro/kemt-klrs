package com.example.physiokneeds_v3;

import java.util.List;

public class Routine {
    private String name;
    private String description;
    private List<Pose> poses;
    private String created_at;

    // Default constructor
    public Routine() {}

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Pose> getPoses() { return poses; }
    public void setPoses(List<Pose> poses) { this.poses = poses; }
}
