package com.example.oscar.cmsc436.data;

/**
 * Created by oscar on 3/14/17.
 */

import java.util.ArrayList;


public class Coordinate {
    private float x, y;
    public Coordinate(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }

    public float euclidDist(Coordinate p2){

        float x2 = p2.getX();
        float y2 = p2.getY();
        float dx =  Math.abs(x-x2);
        float dy =  Math.abs(y-y2);

        return (float) Math.sqrt(dx*dx + dy*dy);
    }

    @Override
    public String toString(){
       return "(" + this.getX() + ", " + this.getY() + ")";
    }


    public float hausdorffDist(ArrayList<Coordinate> mSpiral){
        float h1 = 0;
        //HashMap<Coordinate, HashMap<Coordinate, Float>> distances = new HashMap<>();
       // HashMap<Coordinate, Float> temp = new HashMap<>();

        float shortest = Float.POSITIVE_INFINITY;
        for(Coordinate c1: mSpiral){
            float norm = this.euclidDist(c1);
          //  temp.put(c1,norm);
            if (norm < shortest){
                shortest = norm;
            }
        }

       // distances.put(this,temp);
        if (shortest > h1){
            h1 = shortest;
        }

        return h1;
    }
}
