package com.example.oscar.cmsc436.data.tests;

import java.util.Date;

/**
 * Created by jonbink on 2/28/17.
 */

public class TapTest {

    private int[] right, left;
    private float rAvg, lAvg;
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
        lAvg = (float)l/(float)left.length;
        rAvg = (float)r/(float)right.length;
    }

    public float getRtAvg(){
        return rAvg;
    }

    public float getLtAvg(){
        return lAvg;
    }

    public Date getDate(){
        return date;
    }

    public float[] getRight(){
        float[] f = new float[right.length];
        for(int i = 0; i < right.length; i++){
            f[i] = right[i];
        }
        return f;
    }

    public float[] getLeft(){
        float[] f = new float[left.length];
        for(int i = 0; i < left.length; i++){
            f[i] = left[i];
        }
        return f;
    }


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
