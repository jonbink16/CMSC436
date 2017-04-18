package com.example.oscar.cmsc436.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.os.Vibrator;

import com.example.oscar.cmsc436.R;
import com.example.oscar.cmsc436.data.Database;
import com.example.oscar.cmsc436.data.tests.ArmTest;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ArmTestActivity extends Activity {
    private final int NUM_TESTS = 1;
    private int currTest = 0;
    private double[][] LH = new double[2][NUM_TESTS];
    private double[][] RH = new double[2][NUM_TESTS];
    private boolean leftTest = true;

    private TextView instructions;
    private TextView elapsedtime;
    private TextView xcoord;
    private TextView ycoord;
    private TextView zcoord;
    private TextView inclination;
    private TextView max_incline;
    private long startTime;
    private long endTime;
    private long startThresholdTime;
    private long endThresholdTime;

    private int maxIncline;
    private int prevMax;
    private int startIncline;
    private long totalTime;


    private boolean recStartIncline;
    private boolean testStart;

    private CountDownTimer t;

    private Sensor accelerometerSensor;
    private boolean accelerometerPresent;
    private SensorManager sensorManager;
    private Vibrator vibrator;

    private Database db = Database.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arm_test);

        maxIncline = 0;
        instructions = (TextView) findViewById(R.id.instructions);
        elapsedtime = (TextView) findViewById(R.id.elapsedtime);
        /*xcoord = (TextView) findViewById(R.id.xcoord);
        ycoord = (TextView) findViewById(R.id.ycoord);
        zcoord = (TextView) findViewById(R.id.zcoord);
        inclination = (TextView) findViewById(R.id.inclination); */
        max_incline = (TextView) findViewById(R.id.max_inclination);
        max_incline.setVisibility(View.INVISIBLE);

        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        t = new CountDownTimer(3000,100) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                startTest();
            }
        };


        findViewById(R.id.startCurl).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                t.start();
            }

        });

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if(sensorList.size() > 0){
            accelerometerPresent = true;
            accelerometerSensor = sensorList.get(0);
        }
        else{
            accelerometerPresent = false;
        }

    }

    /*@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN && faceUp) {
                    startTest();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    endTest();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }


    public int calcIncline(SensorEvent event){
        float[] g;
        g = event.values.clone();
        float x_value = g[0];
        float y_value = g[1];
        float z_value = g[2];


        float norm = (float) Math.sqrt(x_value * x_value + y_value * y_value + z_value * z_value);

        g[0] = g[0] / norm;
        g[1] = g[1] / norm;
        g[2] = g[2] / norm;

        return (int) Math.round(Math.toDegrees(Math.acos(g[2])));

    }

    private SensorEventListener accelerometerListener = new SensorEventListener(){

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }


        /* if these z values do not work try:
            Face down: 9 < z < 10
            Face up: -10 < z < -9
         */

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(testStart) {
                if(recStartIncline){
                    startIncline = calcIncline(event);
                }

                prevMax = Math.max(maxIncline, calcIncline(event));

                /* xcoord.setText(Float.toString(x_value));
                ycoord.setText(Float.toString(y_value));
                zcoord.setText(Float.toString(z_value));
                inclination.setText(Integer.toString(incline));*/


                if (prevMax != maxIncline) {
                    maxIncline = prevMax;
                    startThresholdTime = System.currentTimeMillis();
                }

                if (prevMax == maxIncline) {
                    endThresholdTime = System.currentTimeMillis();
                    /*
                    System.out.println(startThresholdTime);
                    System.out.println(endThresholdTime);
                    System.out.println(endThresholdTime - startThresholdTime);*/
                    if (endThresholdTime - startThresholdTime >= 3000) {
                        endTest();
                    }
                }

             /*   if (z_value <= -9) {
                    endTest();
                }*/
            }
        }};


    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(accelerometerListener);
    }

    public void startTest(){
        vibrator.vibrate(500);

        findViewById(R.id.activity_curl_test).setBackgroundColor(Color.GREEN);
        findViewById(R.id.startCurl).setEnabled(false);
        testStart = true;
        startTime = System.currentTimeMillis();
    }

    public void endTest() {
        testStart = false;
        vibrator.vibrate(500);
        maxIncline = 0;
        max_incline.setText("Max Incline: " + Integer.toString(maxIncline));
        max_incline.setVisibility(View.VISIBLE);
        findViewById(R.id.activity_curl_test).setBackgroundColor(Color.WHITE);
        findViewById(R.id.startCurl).setEnabled(true);
        endTime = System.currentTimeMillis();
        totalTime = endTime - startTime;

        double time1 = (double) totalTime / 1000;
        elapsedtime.setText(time1 + " seconds elapsed.");
        long seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime);
        elapsedtime.setText(Long.toString(seconds));
        /*
        if (leftTest) {
            LH[currTest][0] = maxIncline;
            LH[currTest][1] = time1;
        } else {
            RH[currTest][0] = maxIncline;
            RH[currTest++][1] = time1;
        }
        if(currTest == NUM_TESTS) { // Finished all tests, export data.
            db.addCurlTest(new CurlTest(LH, RH, new Date()));
        }
        */
    }
}