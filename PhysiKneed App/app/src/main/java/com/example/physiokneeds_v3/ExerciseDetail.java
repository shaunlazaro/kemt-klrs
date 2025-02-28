package com.example.physiokneeds_v3;

import java.io.Serializable;
import java.util.List;

public class ExerciseDetail implements Serializable {
    private List<String> rep_keypoints;
    private Integer threshold_flexion;
    private Integer threshold_extension;
    private String display_name;

    // Default constructor
    public ExerciseDetail() {}
}
