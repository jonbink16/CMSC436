package com.example.oscar.cmsc436.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.example.oscar.cmsc436.R;
import com.example.oscar.cmsc436.data.Coordinate;

public class BubbleView extends View {
    private int x, y, diameter, adjHeight, statusBarHeight, actionBarHeight, storeX, storeY;
    private final int WIDTH = getContext()
            .getResources()
            .getDisplayMetrics()
            .widthPixels;
    private final int HEIGHT = getContext()
            .getResources()
            .getDisplayMetrics()
            .heightPixels;
    private ShapeDrawable bubble;
    private Rect rect;
    private Drawable background;
    private TypedValue tv;
    private int centerX, centerY;

    public BubbleView(Context context) {
        super(context);
        createBubble(context);
    }
    public BubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        createBubble(context);
    }
    public BubbleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        createBubble(context);
    }
    public void createBubble(Context context) {

        background = ContextCompat.getDrawable(context, R.drawable
                .level_background_crosshair);

        // Account for actionbar height
        tv = new TypedValue();
        if (context.getTheme().resolveAttribute(R.attr.actionBarSize, tv,
                true)){
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
                    getResources().getDisplayMetrics());
        }

        // Account for status bar height
        int resourceId = getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        adjHeight = HEIGHT - actionBarHeight - statusBarHeight; // Offset Height

        // Calculate Diameter based on screen pixels in inches / screen diagonal
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        double screenInches = Math.sqrt(Math.pow(dm.widthPixels / dm.xdpi, 2) +
                Math.pow(dm.heightPixels / dm.ydpi, 2));
        double screenPixels = Math.sqrt(Math.pow(dm.widthPixels, 2) +
                Math.pow(dm.heightPixels, 2));
        Log.d("debug","Screen inches: " + screenInches);
        double ppi = screenPixels / screenInches;
        Log.d("debug","PPI: " + ppi);
        diameter = (int) ppi / 3;

        // Start ball in center
        /*x = WIDTH / 2 - diameter / 2;
        y = x;*/
        centerX = WIDTH / 2 - diameter / 2;
        centerY = centerX;
        x = centerX;
        y = centerY;
        //y = adjHeight / 2 - diameter * 2;

        //paint = new Paint();

        // Start ball in bottom center of picture
        bubble = new ShapeDrawable(new OvalShape());
        //bubble.getShape().getOutline(outline);
        //shapeSize = (int) outline.getRadius();
        System.out.println(bubble.getShape());
        bubble.setBounds(x, y, x + diameter, y + diameter);
        bubble.getPaint().setColor(0xff00ff00);
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
        /* Move background to bottom of screen
        background.setBounds(0, (adjHeight - WIDTH), WIDTH, adjHeight);*/

        background.draw(canvas);
        bubble.draw(canvas);
    }
    public void move(float a, float b) {
        // Change bounds of circle
        if ((x + a) >= 0 && (x + a) <= WIDTH - diameter)
            x = (int) (x + a);
        // Set bounds of ball to top and bottom of screen
        if ((y - b) >= 0 && (y - b) <=
                WIDTH - diameter)
            y = (int) (y - b);

        // Changes ball to red if the center of the ball passes the innermost ring
        double radius = WIDTH / 2;
        double d = distance(x, y);
        double distToRadius = d / radius;
        double ballToRadius = diameter / radius;
        if (distToRadius > ballToRadius)
            bubble.getPaint().setColor(0xffff0000); // Ball set to Red
        else
            bubble.getPaint().setColor(0xff00ff00); // Ball set to green

        // Set bounds of ball to stay within an image located in center
        bubble.setBounds(x, y, x + diameter, y + diameter);
        rect = bubble.getBounds();
        storeX = rect.centerX();
        storeY = rect.centerY();
    }
    public Coordinate getCenter() {
        return new Coordinate(centerX, centerY);
    }
    public void setCenter(int setX, int setY) {
        centerX = setX;
        centerY = setY;
        bubble.setBounds(centerX, centerY, centerX + diameter, centerY + diameter);
    }
    public double distance (float x1, float y1) {
        return Math.sqrt(Math.pow((centerX - x1), 2) + Math.pow((centerY-y1), 2));
    }
    public int getXPos() {
        return storeX;
    }
    public int getYPos() {
        return storeY;
    }
    public int getBubbleRadius() {
        return diameter / 2;
    }
}
