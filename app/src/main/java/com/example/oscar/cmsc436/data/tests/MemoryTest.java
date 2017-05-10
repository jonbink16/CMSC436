package com.example.oscar.cmsc436.data.tests;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by jonbink on 5/9/17.
 */

public class MemoryTest {
    private HashMap<Integer, ArrayList<Long>> testResults;
    private int numCorrect, numWrong, learningRate;
    private float avgReactionSpeed;
    private Date date;

    public MemoryTest(HashMap<Integer,ArrayList<Long>> testResults, int numCorrect, int numWrong, float avgReactionSpeed, int learningRate, Date date){
        this.testResults = new HashMap<Integer, ArrayList<Long>>();
        this.testResults.putAll(testResults);
        this.numCorrect = numCorrect;
        this.numWrong = numWrong;
        this.avgReactionSpeed = avgReactionSpeed;
        this.learningRate = learningRate;
        this.date = date;
    }
    public int getNumCorrect() {
        return numCorrect;
    }
    public int getNumWrong() {
        return numWrong;
    }
    public float getAvgReactionSpeed() {
        return avgReactionSpeed;
    }
    public int getLearningRate() {
        return learningRate;
    }
    public float[] getRawData(){
        return new float[]{numCorrect, numWrong, avgReactionSpeed, learningRate};
    }
    public String toString() {
        return date + " : " + numCorrect + ":" + numWrong + ":" + avgReactionSpeed + ":" + learningRate;
    }
}
