package com.example.oscar.cmsc436.data.tests;

import java.util.Date;

/**
 * Created by jonbink on 2/28/17.
 */

public class TapTest {

    private int[] right, left;
    private double rAvg, lAvg;
    private Date date;

    public TapTest(int[] left, int[] right, Date date) {
        this.left = left;
        this.right = right;
        this.date = date;
        int l = 0, r = 0;
        for(int i = 0; i < left.length; i++){
            l += left[i];
            r += right[i];
        }
        lAvg = (double)l/(double)left.length;
        rAvg = (double)r/(double)right.length;
    }

    public double getRtAvg(){
        return rAvg;
    }

    public double getLtAvg(){
        return lAvg;
    }

    public Date getDate(){
        return date;
    }

    public int[] getRight(){ return right; }

    public int[] getLeft(){ return left; }


    public String toString(){
        String s = date + ":\nl:";
        for(int i = 0; i < left.length; i++){
            s += left[i] + "|";
        }
        s += lAvg;
        s += "\nr:";
        for(int i = 0; i < left.length; i++){
            s += right[i] + "|";
        }
        s+= rAvg;
        return s;
    }
}
