package com.example.oscar.cmsc436.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.example.oscar.cmsc436.R;
import com.example.oscar.cmsc436.data.Coordinate;


import java.util.ArrayList;

public class HeatmapView extends View {
    /*private int x, y, diameter, adjHeight, statusBarHeight, actionBarHeight,
            time, secLeft;
    private static int moves;
    */
    private final int WIDTH = getContext()
            .getResources()
            .getDisplayMetrics()
            .widthPixels;
    private final int HEIGHT = getContext()
            .getResources()
            .getDisplayMetrics()
            .heightPixels;
    //private ShapeDrawable bubble;
    private Drawable background;
    private ArrayList<Coordinate> coords;
    //private TypedValue tv;
    private Paint paint;
    private int radius;

    public HeatmapView(Context context) {
        super(context);
        coords = new ArrayList<>();
        //coords = new ArrayList<>(coords);
        background = ContextCompat.getDrawable(context, R.drawable
                .level_background_crosshair);
    }
    public void insertCoordinates(ArrayList<Coordinate> inputCoords) {
        coords = new ArrayList<>(inputCoords);
    }
    public void insertRadius(int inputRadius) {
        //System.out.println("Radius received: " + inputRadius);
        radius = inputRadius;
    }
    protected void onDraw(Canvas canvas) { // Draws the circle based on bounds
        super.onDraw(canvas);
        /* setBounds(Left, Top, Right, Bottom)
         * Left is 0, Width is the width of the screen. The center of the screen
         * is calculated by subtracting the width from the length and dividing
         * the remaining length in two (half to offset the top and bottom
         * respectively.
         */
        background.setBounds(0, 0, WIDTH, WIDTH);

        background.draw(canvas);
        paint = new Paint(Color.RED);
        for(int i = 0; i < coords.size(); i++) {
            canvas.drawCircle((int) coords.get(i).getX(), (int) coords.get(i).getY(), radius, paint);
        }
    }
}
