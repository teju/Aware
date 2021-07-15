package com.watch.aware.app.models;

public class Steps {
    String ID;
    String stepCount = "0";
    String distance;

    public int getMaxStepCount() {
        return maxStepCount;
    }

    public void setMaxStepCount(int maxStepCount) {
        this.maxStepCount = maxStepCount;
    }

    int maxStepCount = 0;

    public String getLogs() {
        return Logs;
    }

    public void setLogs(String logs) {
        Logs = logs;
    }

    String Logs;

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getCal() {
        return cal;
    }

    public void setCal(String cal) {
        this.cal = cal;
    }

    String cal;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getStepCount() {
        return stepCount;
    }

    public void setStepCount(String stepCount) {
        this.stepCount = stepCount;
    }

    String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    String time;

    public String getTotal_count() {
        return total_count;
    }

    public void setTotal_count(String total_count) {
        this.total_count = total_count;
    }

    String total_count;
    String total_cal;

    public String getTotal_cal() {
        return total_cal;
    }

    public void setTotal_cal(String total_cal) {
        this.total_cal = total_cal;
    }

    public String getTotal_dist() {
        return total_dist;
    }

    public void setTotal_dist(String total_dist) {
        this.total_dist = total_dist;
    }

    String total_dist;
}
