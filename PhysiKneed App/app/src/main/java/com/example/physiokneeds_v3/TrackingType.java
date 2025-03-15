package com.example.physiokneeds_v3;

public enum TrackingType {
    ANGLE_OF_THREE_POINTS("Angle of three points"),
    ANGLE_WITH_HORIZONTAL("Angle with horizontal"),
    ANGLE_WITH_VERTICAL("Angle with vertical");

    private final String type;

    // Constructor
    TrackingType(String type) {
        this.type = type;
    }

    // Getter method
    public String getType() {
        return type;
    }
}