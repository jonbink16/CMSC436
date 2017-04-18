package com.example.oscar.cmsc436.activities;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.oscar.cmsc436.R;

import java.util.ArrayList;

public class SwayActivity extends AppCompatActivity implements
        SensorEventListener {
    private TextView xTv, yTv, zTv, temp;
    private SensorManager sensorMgr;
    private Sensor accel;
    private float ogXvalue, ogYvalue, ogZvalue, x_value, y_value, z_value;
    private ToneGenerator toneG;
    private CountDownTimer soundTimer, trackTimer;
    private Button tempStartButton;
    private boolean storeOgValues = false, trackValues = false;
    private int time = 0;
    private ArrayList<Float>[] accelValues = new ArrayList[10];
    private double gravity = 9806.65, avgAcceleration = 0, frequency;
    private float[] totalAverages;
    private double totalDisplacement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sway);

        tempStartButton = (Button) findViewById(R.id.temp_start_btn);

        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

        xTv = (TextView) findViewById(R.id.x_textview);
        yTv = (TextView) findViewById(R.id.y_textview);
        zTv = (TextView) findViewById(R.id.z_textview);
        temp = (TextView) findViewById(R.id.temp_tv);

        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        soundTimer = new CountDownTimer(5000, 100) { //countdown from 10 seconds
            @Override
            public void onTick(long millisUntilFinished) {
                //do nothing
            }
            @Override
            public void onFinish() {
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                storeOgValues = true;
                for(int i = 0; i < 10; i++) {
                    accelValues[i] = new ArrayList<Float>();
                }
                totalAverages = new float[10];
                trackDisplacement();
            }
        };
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN)
                    //beginTest();
                    return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }
    public void beginTest(View view) {
        soundTimer.start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] g ;
        g = event.values.clone();
        x_value = g[0];
        y_value = g[1];
        z_value = g[2];

        if(storeOgValues) {
            ogXvalue = x_value;
            ogYvalue = y_value;
            ogZvalue = z_value;
            storeOgValues = false;
        }

        if(time < 10 && trackValues) {
            double tempX = x_value - ogXvalue;
            double tempY = y_value - ogYvalue;
            double tempZ = z_value - ogZvalue;
            xTv.setText(Double.toString(tempX));
            yTv.setText(Double.toString(tempY));
            zTv.setText(Double.toString(tempZ));
            double accelWaveForm = Math.sqrt(tempX*tempX + tempY*tempY + tempZ*tempZ);
            accelValues[time].add((float) accelWaveForm);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //do nothing
    }
    protected void onResume() { // Register listener when resuming
        super.onResume();
        sensorMgr.registerListener(this, accel,
                SensorManager.SENSOR_DELAY_GAME);
    }
    protected void onPause() { // Turn off listener to save battery
        super.onPause();
        sensorMgr.unregisterListener(this);
    }

    public void trackDisplacement() {
        trackValues = true;
        trackTimer = new CountDownTimer(10000, 100) { //countdown from 10 seconds
            @Override
            public void onTick(long millisUntilFinished) {
                time = 10 - (Math.round((float) millisUntilFinished / 1000.0f));
            }
            @Override
            public void onFinish() {
                totalDisplacement = 0;
                trackValues = false;
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                float avg;
                int size;
                for(int i = 0; i < 10; i++) {
                    avg = 0;
                    size = accelValues[i].size();
                    for(int j = 0; j < size; j++) {
                        avg += accelValues[i].get(j);
                    }
                    totalAverages[i] = avg / size;
                }
                for(int i = 0; i < 10; i++) {
                    totalDisplacement += accel2mms(totalAverages[i], 1);
                }
                totalDisplacement = Math.floor((totalDisplacement / 1000) * 100) / 100;
                temp.setText("The total displacement is: " + Double.toString(totalDisplacement));
            }
        }.start();
    }
    public double accel2mms(double acc, double freq){
        double result = 0;
        result = (gravity*acc)/(2*Math.PI*freq);
        return result;
    }
}