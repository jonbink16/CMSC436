package com.example.oscar.cmsc436.data.tests;

import java.util.Date;

/**
 * Created by jonbink on 3/28/17.
 */

public class ArmTest {

    private Date time;
    private double metric;

    public ArmTest(double metric, Date time){
        this.metric = metric;
        this.time = time;
    }

    public Date getTime(){
        return time;
    }

    public double getMetric(){
        return metric;
    }

    public String toString(){
        return time + " : " + metric + " seconds.";
    }
}
