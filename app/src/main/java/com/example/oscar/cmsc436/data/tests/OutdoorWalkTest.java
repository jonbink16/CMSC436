package com.example.oscar.cmsc436.data.tests;

/**
 * Created by jonbink on 4/24/17.
 */

public class OutdoorWalkTest {


    private long time;
    private float meters, mps;

    public OutdoorWalkTest(long time, float meters, float mps){
        this.time = time;
        this.meters = meters;
        this.mps = mps;
    }

    public float getTime(){
        return (float)time;
    }

    public float getMps(){
        return mps;
    }

    public float getMeters(){
        return meters;
    }
}
