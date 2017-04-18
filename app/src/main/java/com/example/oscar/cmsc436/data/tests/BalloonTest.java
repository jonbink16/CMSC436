package com.example.oscar.cmsc436.data.tests;

import java.util.Date;

/**
 * Created by jonbink on 3/28/17.
 */

public class BalloonTest {
    private double lMetric, rMetric;
    private int rHits, lHits;
    private Date time;

    public BalloonTest(double lMetric, int lHits, double rMetric, int rHits, Date time){
        this.lMetric = lMetric;
        this.rMetric = rMetric;
        this.lHits = lHits;
        this.rHits = rHits;
        this.time = time;
    }

    public Date getTime(){
        return time;
    }

    public float getLMetric(){
        return (float)lMetric;
    }
    public float getRMetric(){
        return (float)rMetric;
    }

    public String toString(){
        return time + " : " + lMetric;
    }
}
