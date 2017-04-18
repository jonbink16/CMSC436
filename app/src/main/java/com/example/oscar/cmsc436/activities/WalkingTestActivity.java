package com.example.oscar.cmsc436.activities;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import com.example.oscar.cmsc436.R;
import com.example.oscar.cmsc436.data.StepDetector;
import com.example.oscar.cmsc436.data.StepListener;

import java.util.concurrent.TimeUnit;

public class WalkingTestActivity extends Activity implements SensorEventListener, StepListener {
    private TextView etime;
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private static final String TEXT_NUM_STEPS = "Number of Steps: ";
    private int numSteps;
    private TextView TvSteps;

    private CountDownTimer t;

    private long startTime;
    private long endTime;
    private long elapsedTime;
    private ToneGenerator toneG;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking_test);

        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        TvSteps = (TextView) findViewById(R.id.tv_steps);
        etime = (TextView) findViewById(R.id.e_time);
        findViewById(R.id.end_test).setEnabled(false);

        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        t = new CountDownTimer(5000,100) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                startPedometer();
            }
        };
    }

    public void startTest(View view){
        findViewById(R.id.btn_start).setEnabled(false);
        t.start();
    }
    public void endTest(View view){
        findViewById(R.id.end_test).setEnabled(false);
        stopPedometer();
    }

    public void startPedometer(){
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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (numSteps == 25){
            stopPedometer();
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void step(long timeNs) {
        numSteps++;
        TvSteps.setText(TEXT_NUM_STEPS + numSteps);
    }
}


