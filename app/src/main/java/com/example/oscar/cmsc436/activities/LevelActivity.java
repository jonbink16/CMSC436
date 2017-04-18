package com.example.oscar.cmsc436.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.oscar.cmsc436.R;
import com.example.oscar.cmsc436.data.Coordinate;
import com.example.oscar.cmsc436.data.Database;
import com.example.oscar.cmsc436.data.tests.LevelTest;
import com.example.oscar.cmsc436.views.BubbleView;
import com.example.oscar.cmsc436.views.HeatmapView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LevelActivity extends AppCompatActivity implements
        SensorEventListener{
    private BubbleView bubble;
    private HeatmapView heatmap;
    private SensorManager sensorMgr;
    private Sensor accel;
    private FrameLayout frameLayout;
    private float xCoord, yCoord;
    private ArrayList<Coordinate> coords;
    private CountDownTimer timer;
    private boolean captureCoordinates = false;
    private int time, secondsLeft;
    private TextView timerView;
    private Button leftHandButton, rightHandButton;
    private int left, right;
    private Context c;
    private int handSelected;
    private Map<String, String> data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);
        frameLayout = (FrameLayout) findViewById(R.id.bubble_container);
        c = this;
        bubble = new BubbleView(c);
        heatmap = new HeatmapView(this);
        frameLayout.addView(bubble);
        //setContentView(bubble); // Creates bubble/accel listener
        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //Log.d("sensor", "sensor");
        sensorMgr.registerListener(this, accel, sensorMgr.SENSOR_DELAY_GAME);
        //Log.d("sensor", "sensor");
        coords = new ArrayList<>();
        timerView = (TextView) findViewById(R.id.timerView);
        leftHandButton = (Button) findViewById(R.id.left_button);
        rightHandButton = (Button) findViewById(R.id.right_button);

        leftHandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handSelected = 1;
                startLevelTest();
            }
        });
        rightHandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handSelected = 2;
                startLevelTest();
            }
        });

        timer = new CountDownTimer(10000, 100) { //countdown from 10 seconds
            @Override
            public void onTick(long millisUntilFinished) {
                time = Math.round((float) millisUntilFinished / 1000.0f);
                if (time != secondsLeft) { // Fixes inaccuracy of countdown method
                    secondsLeft = time;
                    timerView.setText("Seconds remaining: " + secondsLeft);
                } else
                    timerView.setText("Seconds remaining: " + millisUntilFinished / 1000);
            }
            @Override
            public void onFinish() {
                captureCoordinates = false;

                //this is the "heatmap" of the positions where the bubble moved.
                //should be saved to gallery and/or sent to the doctor as data.
                heatmap.insertCoordinates(coords);
                heatmap.insertRadius(bubble.getBubbleRadius());
                heatmap.invalidate();

                //resets the bubbleView
                frameLayout.removeView(bubble);
                bubble = new BubbleView(c);
                frameLayout.addView(bubble);

                int finalResult = calculateResults();

                timerView.setText("Your result: " + finalResult);

                if(handSelected == 1) { //send results for left hand
                    right = finalResult;

                } else { //send results for right hand
                    left = finalResult;
                    Database.getInstance().addLevelTest(new LevelTest(left,right,new Date()));
                }

                leftHandButton.setVisibility(View.VISIBLE);
                rightHandButton.setVisibility(View.VISIBLE);

            }
        };
    }
    public void startLevelTest() {
        coords = new ArrayList<>();
        leftHandButton.setVisibility(View.INVISIBLE);
        rightHandButton.setVisibility(View.INVISIBLE);
        captureCoordinates = true;
        timer.start();
    }
    public void onSensorChanged(SensorEvent event) {
        //Log.d("x val", "" + event.values[0]);
        //Log.d("y val", "" + event.values[1]);
        //Log.d("z val", "" + event.values[2]);
        xCoord = event.values[0];
        yCoord = event.values[1];

        if (captureCoordinates) {
            bubble.move(xCoord, yCoord);
            coords.add(new Coordinate(bubble.getXPos(), bubble.getYPos()));
            bubble.invalidate(); // Mark area to be redrawn by onDraw()
        }
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    protected void onResume() { // Register listener when resuming
        super.onResume();
        sensorMgr.registerListener(this, accel,
                SensorManager.SENSOR_DELAY_GAME);
    }
    protected void onPause() { // Turn off listener to save battery
        super.onPause();
        sensorMgr.unregisterListener(this);
    }
    //calculates the Euclidean distance of every coordinate captured
    //against the center position.
    public int calculateResults() {
        int centerX = (int) coords.get(0).getX();
        int centerY = (int) coords.get(0).getY();
        int end = coords.size();
        double xDist, yDist;
        int totalDistance = 0;
        for(int i = 1; i < end; i++) {
            xDist = Math.pow((centerX - coords.get(i).getX()), 2);
            yDist = Math.pow((centerY - coords.get(i).getY()), 2);
            totalDistance += Math.sqrt(xDist + yDist);
        }
        return totalDistance / end;
    }
}
