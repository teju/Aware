package com.watch.aware.app.models;

public class TempRate {
    String date = "0";

    public Double getTempRate() {
        return TempRate;
    }

    public void setTempRate(Double tempRate) {
        TempRate = tempRate;
    }

    Double TempRate = 0.0;



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
}


