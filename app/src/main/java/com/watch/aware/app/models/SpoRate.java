package com.watch.aware.app.models;

public class SpoRate {
    String date = "0";

    public int getSpoRate() {
        return SpoRate;
    }

    public void setSpoRate(int spoRate) {
        SpoRate = spoRate;
    }

    int SpoRate = 0;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    String ID = "0";



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


