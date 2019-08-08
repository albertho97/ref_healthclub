package com.example.healthclubapp;

import java.io.Serializable;

public class DailyStat implements Serializable {

    public String aggregatedDate;
    public String weight;
    public String hours;
    public String consumed;
    public String burned;


    public DailyStat() {

    }

    public DailyStat(String aggregatedDate, String weight, String hours, String consumed, String burned) {
        this.aggregatedDate = aggregatedDate;
        this.weight = weight;
        this.hours = hours;
        this.consumed = consumed;
        this.burned = burned;
    }
}