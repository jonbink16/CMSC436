package com.example.oscar.cmsc436.activities;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import com.example.oscar.cmsc436.R;
import com.example.oscar.cmsc436.data.StepDetector;
import com.example.oscar.cmsc436.data.StepListener;

import java.util.concurrent.TimeUnit;

public class WalkingTestActivity extends Activity implements SensorEventListener {
   // private TextView etime;
   // private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
   /* private static final String TEXT_NUM_STEPS = "Number of Steps: ";
    private int numSteps;*/
    //private TextView avgspd;

    private CountDownTimer t;

    private long startTime;
    private long endTime;
    private long elapsedTime;
    private float totlAcl;
    private float avgAcl;
    private int aclReadings;

    //private ToneGenerator toneG;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking_test);

        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        //simpleStepDetector = new StepDetector();
        //simpleStepDetector.registerListener(this);

        findViewById(R.id.end_test).setEnabled(false);
        aclReadings = 0;
        totlAcl = 0;
        //toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        /*t = new CountDownTimer(5000,100) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                startPedometer();
            }
        };*/
    }

    public void startTest(View view){
        findViewById(R.id.btn_start).setEnabled(false);
        findViewById(R.id.end_test).setEnabled(true);
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        startTime = System.currentTimeMillis();


       // t.start();
    }
    public void endTest(View view){
        findViewById(R.id.end_test).setEnabled(false);
        findViewById(R.id.btn_start).setEnabled(true);
        sensorManager.unregisterListener(this);
        endTime = System.currentTimeMillis();
        elapsedTime = endTime - startTime;

        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
        avgAcl = totlAcl/aclReadings;
        //((TextView) findViewById(R.id.avgacl)).setText(Float.toString(avgAcl));
        //avgspd.setText(Float.toString(avgAcl));
        totlAcl = 0 ;
        avgAcl = 0;
        aclReadings = 0;

        //etime.setText(Long.toString(seconds));
        //stopPedometer();
    }

    /*public void startPedometer(){
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        numSteps = 0;
        startTime = System.currentTimeMillis();

        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        findViewById(R.id.end_test).setEnabled(true);
    }

    public void stopPedometer(){
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        endTime = System.currentTimeMillis();
        elapsedTime = endTime - startTime;
        findViewById(R.id.btn_start).setEnabled(true);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
        etime.setText(Long.toString(seconds));
        TvSteps.setText(Integer.toString(numSteps));
        sensorManager.unregisterListener(this);
    }*/

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        aclReadings++;
        /*if (numSteps == 25){
            stopPedometer();
        }*/

        elapsedTime = endTime - startTime;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime);

        float[] g;
        g = event.values.clone();
        float x_value = g[0];
        float y_value = g[1];
        float z_value = g[2];

        /*capturing acceleration at every function call*/
        totlAcl += (float) Math.sqrt(x_value * x_value + y_value * y_value + z_value * z_value);


       /* if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }*/
    }
/*
    @Override
    public void step(long timeNs) {
        numSteps++;
        TvSteps.setText(TEXT_NUM_STEPS + numSteps);
    }*/
}


