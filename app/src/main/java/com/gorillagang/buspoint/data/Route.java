package com.gorillagang.buspoint.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Route implements Serializable {
    long id;
    String name;
    @SerializedName(value = "start_point")
    long startPoint;
    @SerializedName(value = "end_point")
    long endPoint;
    @SerializedName(value = "bus_stops")
    List<Long> busStops;
    @SerializedName(value = "first_stripe")
    String firstStripe;
    @SerializedName(value = "last_stripe")
    String lastStripe;

    public Route(long id,
                 String name,
                 long startPoint,
                 long endPoint,
                 List<Long> busStops,
                 String firstStripe,
                 String lastStripe) {
        this.id = id;
        this.name = name;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.busStops = busStops;
        this.firstStripe = firstStripe;
        this.lastStripe = lastStripe;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(long startPoint) {
        this.startPoint = startPoint;
    }

    public long getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(long endPoint) {
        this.endPoint = endPoint;
    }

    public List<Long> getBusStops() {
        return busStops;
    }

    public void setBusStops(List<Long> busStops) {
        this.busStops = busStops;
    }

    public String getFirstStripe() {
        return firstStripe;
    }

    public void setFirstStripe(String firstStripe) {
        this.firstStripe = firstStripe;
    }

    public String getLastStripe() {
        return lastStripe;
    }

    public void setLastStripe(String lastStripe) {
        this.lastStripe = lastStripe;
    }
}
