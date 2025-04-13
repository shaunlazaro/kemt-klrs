package com.example.physiokneeds_v3;

public class TrackingResult {
    private TrackingDetail detail;
    private Double angle;

    public TrackingResult(TrackingDetail detail, Double angle) {
        this.detail = detail;
        this.angle = angle;
    }

    public TrackingDetail getDetail() {
        return detail;
    }

    public void setDetail(TrackingDetail detail) {
        this.detail = detail;
    }

    public Double getAngle() {
        return angle;
    }

    public void setAngle(Double angle) {
        this.angle = angle;
    }
}
