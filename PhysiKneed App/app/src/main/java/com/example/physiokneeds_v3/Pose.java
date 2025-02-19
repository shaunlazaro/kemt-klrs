package com.example.physiokneeds_v3;

import java.io.Serializable;
import java.util.List;

public class Pose implements Serializable {
    private Integer id;
//    private List<Landmark> landmarks;
    private List<String> landmarks;
    private String created_at;

    // Default constructor
    public Pose() {}

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(int id) { this.id = id; }

//    public List<Landmark> getLandmarks() { return landmarks; }
//    public void setLandmarks(List<Landmark> landmarks) { this.landmarks = landmarks; }

    public List<String> getLandmarks() { return landmarks; }
    public void setLandmarks(List<String> landmarks) { this.landmarks = landmarks; }
}
