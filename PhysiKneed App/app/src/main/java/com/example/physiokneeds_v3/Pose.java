package com.example.physiokneeds_v3;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.io.Serializable;
import java.util.List;

public class Pose implements Serializable {
    private List<Landmark> landmarks;

    public Pose(List<Landmark> landmarks) {
        this.landmarks = landmarks;
    }

    public List<Landmark> getLandmarks() { return landmarks; }
    public void setLandmarks(List<Landmark> landmarks) { this.landmarks = landmarks; }
}
