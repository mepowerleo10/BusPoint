package com.gorillagang.buspoint.data.navigation;

import java.util.List;

public class ComputedDirections {
    List<List<Double>> geometry;
    long distance;
    long duration;

    public ComputedDirections(List<List<Double>> geometry, long distance, long duration) {
        this.geometry = geometry;
        this.distance = distance;
        this.duration = duration;
    }

    public List<List<Double>> getGeometry() {
        return geometry;
    }

    public void setGeometry(List<List<Double>> geometry) {
        this.geometry = geometry;
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
