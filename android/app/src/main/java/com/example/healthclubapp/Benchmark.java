package com.example.healthclubapp;

import java.io.Serializable;

public class Benchmark implements Serializable {

    public String aggregatedDate;
    public String hours;
    public String burned;
    public String cdc_sleep_status;
    public String cdc_calories_burned_status;



    public Benchmark() {

    }

    public Benchmark(String aggregatedDate, String hours, String burned, String cdc_sleep_status, String cdc_calories_burned_status) {
        this.aggregatedDate = aggregatedDate;
        this.hours = hours;
        this.burned = burned;
        this.cdc_sleep_status = cdc_sleep_status;
        this.cdc_calories_burned_status = cdc_calories_burned_status;
    }
}