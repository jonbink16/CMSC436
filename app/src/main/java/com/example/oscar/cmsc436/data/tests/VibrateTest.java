package com.example.oscar.cmsc436.data.tests;

/**
 * Created by jonbink on 5/9/17.
 */

public class VibrateTest {

    private int level;
    private double duration;

    public VibrateTest(int level, double duration){
        this.level = level;
        this.duration = duration;
    }

    public int getLevel(){
        return level;
    }

    public double getDuration(){
        return duration;
    }
}
