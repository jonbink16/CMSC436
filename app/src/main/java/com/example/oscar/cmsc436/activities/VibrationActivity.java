package com.example.oscar.cmsc436.activities;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.example.oscar.cmsc436.R;

import java.util.ArrayList;

public class VibrationActivity extends AppCompatActivity {

    private boolean interrupted, validDevice, startedTest;
    private Vibrator v;
    private int vNum;
    private Thread vibrateThread;
    private static final long VIB_LENGTH = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibration);
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        if(!v.hasVibrator()){
            validDevice = false;
            Toast.makeText(getApplicationContext(), "Your device does not support vibration or an error occurred. " +
                    "Please try a different device or restarting the application.", Toast.LENGTH_SHORT).show();
            return;
        }
        //final List<long[]> pattern = new ArrayList<>();
        //pattern.add(genVibratorPattern(.1f,10000));
        validDevice = true;
        interrupted = false;
        vNum = 1;
        vibrateThread= new Thread(new Runnable() {
            public void run() {
                try {
                    while(!interrupted) {
                        while(vNum < 10){
                            float vib = (float)vNum/(float)10;
                            long[] pattern = genVibratorPattern(vib,VIB_LENGTH);
                                    /*{0, 50 ,50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
                                             100,100,100,100,100,100,100,100,100,100,100,100,100,
                                              50,100, 50,100, 50,100, 50,100, 50,100, 50,100, 50,
                                             50,100, 50,100, 50,100, 50,100, 50,100, 50,100, 50, 100,
                                             25,100, 25,100, 25,100, 25,100, 25,100, 25,100, 25, 100};*/
                            //genVibratorPattern(vib,VIB_LENGTH);

                            v.vibrate(pattern,-1);
                            Thread.sleep(VIB_LENGTH);
                            vNum++;
                            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                        }
                    }
                }catch (InterruptedException e){
                    startedTest = false;
                }
                catch (Throwable t) {
                    Log.i("Vibration", "Thread  exception "+t);
                }
            }
        });


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(validDevice) {
            int eventaction = event.getAction();
            switch (eventaction) {

                case MotionEvent.ACTION_DOWN:

                    if(!startedTest) {
                        System.out.println(true);
                        startTest();
                        interrupted = false;
                    }
                    break;

                case MotionEvent.ACTION_MOVE:

                    break;

                case MotionEvent.ACTION_UP:
                    interrupted();
                    break;
            }
            return true;
        }
        return false;
    }

    private void startTest(){
        startedTest = true;
        vibrateThread.start();
    }

    private void interrupted(){
        interrupted = true;
        vibrateThread.interrupt();
        v.cancel();
        Toast.makeText(getApplicationContext(), "Please keep touching the screen.", Toast.LENGTH_SHORT).show();
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
