package com.example.oscar.cmsc436.views;

/**
 * Created by oscar on 2/16/17.
 */
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.graphics.PorterDuff;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import com.example.oscar.cmsc436.data.Coordinate;
import com.example.oscar.cmsc436.R;

import java.util.ArrayList;
import java.util.HashMap;

public class DrawingView extends LinearLayout {
    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xFF660000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    private float brushSize, lastBrushSize;

    // has to be reset
    private float hausdorff;

    private ArrayList<Coordinate> masterSpiral;

    // has to be reset
    private boolean cap ;

    // has to be reset
    private ArrayList<Coordinate> touches;

    // has to be reset
    private HashMap<Coordinate, HashMap<Coordinate, Float>> distances = new HashMap<>();

    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }

    public void resetValues(){
        this.cap = true;
        this.hausdorff = -1;
        touches.clear();
        distances.clear();
    }

    public static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }


    private void setupDrawing(){
        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;

        //get drawing area setup for interaction
        drawPath = new Path();
        drawPaint = new Paint();

        drawPaint.setColor(Color.RED);

        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);

        masterSpiral = new ArrayList<>();

        touches = new ArrayList<>();

        cap = true;

        hausdorff = -1;

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //view given size
        super.onSizeChanged(w, h, oldw, oldh);

        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(cap){
            cap = false;

            Bitmap bm = getBitmapFromView(this);

            for(int i = 0; i < bm.getWidth(); i++) {
                for(int j = 0; j < bm.getHeight(); j++) {
                    if(bm.getPixel(i, j) != Color.WHITE) {
                        masterSpiral.add(new Coordinate(i, j));
                    }
                }
            }
        }

        //draw view
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //detect user touch
        float touchX = event.getX();
        float touchY = event.getY();

        Coordinate c = new Coordinate(touchX, touchY);

        touches.add(c);

        if (hausdorff == -1){
            hausdorff = c.hausdorffDist(masterSpiral);
        } else {
            hausdorff = Math.max(hausdorff, c.hausdorffDist(masterSpiral));
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void setColor(String newColor){
        //set color
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    public void setBrushSize(float newSize){
        //update size
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        brushSize=pixelAmount;
        drawPaint.setStrokeWidth(brushSize);
    }


    public void startNew(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    public ArrayList<Coordinate> getMasterSpiral(){
        return masterSpiral;
    }

    public ArrayList<Coordinate> getTouches(){
        return touches;
    }

    public HashMap<Coordinate, HashMap<Coordinate, Float>> getDistances(){
        return distances;
    }

    public float getHausdorff(){
        return hausdorff;
    }
}
