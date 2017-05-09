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
import java.util.List;

public class VibrationActivity extends AppCompatActivity {

    private boolean interrupted, validDevice, startedTest;
    private Vibrator v;
    private int vNum;
    private Thread vibrateThread;

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
                            System.out.println(vib);
                            long[] pattern = genVibratorPattern(vib,10000);
                            for(int i = 0; i < pattern.length; i++){
                                System.out.println(pattern[i]);
                            }
                            v.vibrate(pattern,-1);
                            Thread.sleep(10000);
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
            System.out.println(event.toString());
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
        /*System.out.println("hi");
        while(!interrupted) {
            while(vNum < 10){
                long[] pattern = genVibratorPattern((vNum/10), 10);
                v.vibrate(pattern, -1);
                vNum++;
            }
        }*/

    }

    private void interrupted(){
        interrupted = true;
        vibrateThread.interrupt();
        v.cancel();
        Toast.makeText(getApplicationContext(), "Please keep touching the screen.", Toast.LENGTH_SHORT).show();
    }

    public long[] genVibratorPattern( float intensity, long duration )
    {

        long count = 0;
        ArrayList<Long> longList = new ArrayList<>();
        longList.add((long)0);
        while(count <= duration){
            long toAdd = (long)(duration*intensity*intensity);
            count+=toAdd*2;
            if(count >= duration){
                count -= toAdd*2;
                longList.add(duration-count);
                break;
            }
            longList.add(toAdd*2);

            count+=toAdd/4;
            if(count >= duration){
                count -= toAdd/4;
                longList.add(duration-count);
                break;
            }
            longList.add(toAdd/4);
        }
        long[] l = new long[longList.size()];
        for(int i = 0; i < l.length; i++){
            l[i] = longList.get(i);
        }
        return l;

    }
}
