package com.example.oscar.cmsc436.activities;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.oscar.cmsc436.R;

import java.util.ArrayList;


public class VibrationActivity extends AppCompatActivity {

    private boolean interrupted, validDevice, startedTest;
    private Vibrator v;
    private int vNum;
    private Thread vibrateThread;
    private static final long VIB_LENGTH = 10000, LEVELS = 8;
    private Button yesB, noB;
    private boolean threadRunning;
    Handler h;
    private long timeStart, timeEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibration);
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        if(!v.hasVibrator()){
            //validDevice = false;
            Toast.makeText(getApplicationContext(), "Your device does not support vibration or an error occurred. " +
                    "Please try a different device or restarting the application.", Toast.LENGTH_SHORT).show();
            //return;
        }
        yesB = (Button)findViewById(R.id.vibrateYes);
        yesB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishTest();
            }
        });
        noB = (Button)findViewById(R.id.vibrateNo);
        noB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yesB.setVisibility(Button.INVISIBLE);
                noB.setVisibility(Button.INVISIBLE);
                yesB.setClickable(false);
                noB.setClickable(false);
                startedTest = false;
            }
        });
        yesB.setVisibility(Button.INVISIBLE);
        noB.setVisibility(Button.INVISIBLE);
        yesB.setClickable(false);
        noB.setClickable(false);

        validDevice = true;
        interrupted = false;
        vNum = 3;
        threadRunning = false;

        vibrateThread= new Thread(new Runnable() {
            public void run() {
                try {
                    vNum++;
                    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                    toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                    startedTest = false;
                    if (vNum != LEVELS) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                endLevel();
                            }
                        });
                    }
                }
                catch (Throwable t) {
                    Log.i("Vibration", "Thread  exception "+t);
                }
            }

        });
        h = new Handler();

        /*vibrateThread= new Thread(new Runnable() {
            public void run() {
                threadRunning = true;
                try {
                    while(!interrupted) {
                        while(vNum < LEVELS) {
                            long[] pattern = genVibratorPattern(VIB_LENGTH);
                            System.out.println(vNum);
                            v.vibrate(pattern, -1);
                            Thread.sleep(VIB_LENGTH);
                            v.cancel();
                            vNum++;
                            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                            if(vNum != LEVELS) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        endLevel();
                                    }
                                });
                            }

                        }
                    }
                }catch (InterruptedException e){
                    startedTest = false;
                }
                catch (Throwable t) {
                    Log.i("Vibration", "Thread  exception "+t);
                }
                threadRunning = false;
            }
        });*/
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(validDevice && vNum < LEVELS) {
            int eventaction = event.getAction();
            switch (eventaction) {

                case MotionEvent.ACTION_DOWN:
                    if(!startedTest) {
                        interrupted = false;
                        startTest();
                    }

                    break;

                case MotionEvent.ACTION_MOVE:
                    if(!startedTest) {
                        interrupted = false;
                        startTest();
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    System.out.println("interr");
                    if(!interrupted) {
                        interrupted();
                    }
                    break;
            }
            return true;
        }
        return false;
    }
    int state = 0;
    private void startTest(){
        startedTest = true;
        if(state == 0 && !threadRunning && vNum < LEVELS) {
            float vib = (float)vNum/(float)10;
            long[] pattern = genVibratorPattern(vib,VIB_LENGTH);
            v.vibrate(pattern,-1);
            System.out.println(interrupted);
            h.postDelayed(vibrateThread, VIB_LENGTH);
        }else{
            finishTest();
        }
    }

    private void interrupted(){
        interrupted = true;
        startedTest = false;
        h.removeCallbacks(vibrateThread);
        //vibrateThread.interrupt();
        v.cancel();
        threadRunning = false;
        if(vNum != LEVELS)
            Toast.makeText(getApplicationContext(), "Please keep touching the screen.", Toast.LENGTH_SHORT).show();
    }

    private void endLevel(){
        //interrupted = true;
        ((TextView)(findViewById(R.id.vibrateLevelText))).setText("Level: " + (vNum-2));
        //yesB.setVisibility(Button.VISIBLE);
        //noB.setVisibility(Button.VISIBLE);
        //yesB.setClickable(true);
        //noB.setClickable(true);
    }

    private void finishTest(){
        System.out.println("DATA TO BE SAVED: " + vNum);
    }

    @Override
    protected void onPause() {
        super.onPause();
        h.removeCallbacks(vibrateThread);
        //v.cancel();
        //vibrateThread.interrupt();
    }

    public long[] genVibratorPattern( float intensity, long duration )
    {
        //set the break value, if intensity is <= .3, it will be 100, otherwise, it will decrease
        //set the vibration value, it will be intensity*75, which makes it grow
        long BREAK = intensity <= .31 ? 100: (duration/100 - (long)(120*intensity)), VIB = (long)(75*intensity);
        //if break goes below 0, just vibrate for the whole time
        if(BREAK < 0) return new long[]{0,duration};
        System.out.println("Intensity: " + intensity);
        System.out.println("Break: " + BREAK + "\nVib: " + VIB);
        long count = 0;
        ArrayList<Long> longList = new ArrayList<>();
        //start with a break of 0ms
        longList.add((long)0);
        while(count <= duration){
            //add the vibration, if it goes over the time, take it back and add the difference
            count+=VIB;
            if(count >= duration){
                count -= VIB;
                longList.add(duration-count);
                break;
            }
            longList.add(VIB);
            //add the break, if it goes over the time, take it back and add the difference
            count+=BREAK;
            if(count >= duration){
                count -= BREAK;
                longList.add(duration-count);
                break;
            }
            longList.add(BREAK);
        }
        long[] l = new long[longList.size()];
        for(int i = 0; i < l.length; i++){
            l[i] = longList.get(i);
        }
        return l;

    }

}
