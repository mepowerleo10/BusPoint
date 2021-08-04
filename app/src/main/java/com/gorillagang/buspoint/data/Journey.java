package com.gorillagang.buspoint.data;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Journey implements Serializable {
    long id;
    @SerializedName(value = "date")
    Date dateTime;
    @SerializedName(value = "start_stop")
    Stop startStop;
    @SerializedName(value = "final_stop")
    Stop finalStop;
    @SerializedName(value = "mid_stop")
    Stop midStop;
    @SerializedName(value = "notify_stops")
    List<Stop> notifyStops;
    @SerializedName(value = "routing_stops")
    List<Stop> routingStops;
    List<Route> routes;
    float cost;

    @SerializedName(value = "from_location")
    String fromDescription;
    @SerializedName(value = "to_location")
    String toDescription;

    public Journey(long id,
                   Date dateTime,
                   Stop startStop,
                   Stop finalStop,
                   Stop midStop,
                   List<Stop> notifyStops,
                   List<Stop> routingStops,
                   List<Route> routes,
                   float cost,
                   String fromDescription,
                   String toDescription) {
        this.id = id;
        this.dateTime = dateTime;
        this.startStop = startStop;
        this.finalStop = finalStop;
        this.midStop = midStop;
        this.notifyStops = notifyStops;
        this.routingStops = routingStops;
        this.routes = routes;
        this.cost = cost;
        this.fromDescription = fromDescription;
        this.toDescription = toDescription;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public Stop getStartStop() {
        return startStop;
    }

    public void setStartStop(Stop startStop) {
        this.startStop = startStop;
    }

    public Stop getFinalStop() {
        return finalStop;
    }

    public void setFinalStop(Stop finalStop) {
        this.finalStop = finalStop;
    }

    public Stop getMidStop() {
        return midStop;
    }

    public void setMidStop(Stop midStop) {
        this.midStop = midStop;
    }

    public List<Stop> getNotifyStops() {
        return notifyStops;
    }

    public void setNotifyStops(List<Stop> notifyStops) {
        this.notifyStops = notifyStops;
    }

    public List<Stop> getRoutingStops() {
        return routingStops;
    }

    public void setRoutingStops(List<Stop> routingStops) {
        this.routingStops = routingStops;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public String getFromDescription() {
        return fromDescription;
    }

    public void setFromDescription(String fromDescription) {
        this.fromDescription = fromDescription;
    }

    public String getToDescription() {
        return toDescription;
    }

    public void setToDescription(String toDescription) {
        this.toDescription = toDescription;
    }

    @NonNull
    @NotNull
    @Override
    public String toString() {
        return super.toString();
    }

}
