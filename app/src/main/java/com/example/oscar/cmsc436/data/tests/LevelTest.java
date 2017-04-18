package com.example.oscar.cmsc436.data.tests;

import java.util.Date;

/**
 * Created by jonbink on 3/28/17.
 */

public class LevelTest {

    private float lmetric, rmetric;
    private Date time;

    public LevelTest(float lmetric, float rmetric, Date time){
        this.lmetric = lmetric;
        this.rmetric = rmetric;
        this.time = time;
    }

    public Date getTime(){
        return time;
    }

    public float getRightMetric(){
        return rmetric;
    }

    public float getLeftMetric(){ return lmetric;}

    public String toString(){
        return time + " : " + lmetric + ":" + rmetric;
    }
}
