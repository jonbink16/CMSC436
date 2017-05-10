package com.example.oscar.cmsc436.data.tests;

/**
 * Created by jonbink on 5/9/17.
 */

public class VibrateTest {

    private int level, timesLifted;
    private double duration;

    public VibrateTest(int level, double duration, int timesLifted){
        this.level = level;
        this.duration = duration;
        this.timesLifted = timesLifted;
    }

    public int getLevel(){
        return level;
    }

    public double getDuration(){
        return duration;
    }

    public float[] getRawData(){
        return new float[]{level, (float)duration, timesLifted};
    }
}
