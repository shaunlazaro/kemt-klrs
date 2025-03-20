package com.example.physiokneeds_v3;

import java.io.Serializable;

public class Landmark implements Serializable {
    private int landmark_index;
    private float x;
    private float y;
    private float z;
    private float visibility;

    public Landmark(int landmark_index, float x, float y, float z, float visibility) {
        this.landmark_index = landmark_index;
        this.x = x;
        this.y = y;
        this.z = z;
        this.visibility = visibility;
    }

    public int getLandmarkIndex() {
        return landmark_index;
    }

    public void setLandmarkIndex(int landmark_index) {
        this.landmark_index = landmark_index;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getVisibility() {
        return visibility;
    }

    public void setVisibility(float visibility) {
        this.visibility = visibility;
    }
}
