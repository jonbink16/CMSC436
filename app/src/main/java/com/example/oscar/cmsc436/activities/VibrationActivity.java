package com.example.oscar.cmsc436.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Vibrator;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.oscar.cmsc436.R;
import com.example.oscar.cmsc436.data.Database;
import com.example.oscar.cmsc436.data.tests.VibrateTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;


public class VibrationActivity extends AppCompatActivity implements RecognitionListener{

    private boolean interrupted, validDevice, startedTest;
    private Vibrator v;
    private int vNum, timesLifted;
    private Thread vibrateThread;
    private static final long VIB_LENGTH = 10000, LEVELS = 8;
    private Button yesB, noB;
    private boolean threadRunning, FINISHEDEXECUTE, testDone, listening;
    Handler h;
    private long timeStart, timeEnd;
    private static final String FELT_VIB = "yes";
    private SpeechRecognizer recognizer;
    private static final int PERM_REQUEST_REC_AUDIO = 1;
    private static final int VSTART = 3;

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
        vNum = VSTART;
        timesLifted = 0;
        threadRunning = false;
        listening = false;

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
                    if(!interrupted && !startedTest){
                        startTest();
                    }
                }
                catch (Throwable t) {
                    Log.i("Vibration", "Thread  exception "+t);
                }
            }

        });
        h = new Handler();

        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERM_REQUEST_REC_AUDIO);
            return;
        }
        FINISHEDEXECUTE = false;
        testDone = false;
        runRecognizerSetup();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(validDevice && vNum < LEVELS && FINISHEDEXECUTE && !testDone) {
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
    long l1 = 0, l2;
    private void startTest(){
        startedTest = true;
        if(vNum == VSTART && !listening){
            recognizer.startListening(FELT_VIB);
            timeStart = System.currentTimeMillis();
            listening = true;
        }
        if(state == 0 && vNum < LEVELS) {
            float vib = (float)vNum/(float)10;
            long[] pattern = genVibratorPattern(vib,VIB_LENGTH);
            v.vibrate(pattern,-1);
            l1 = System.currentTimeMillis();
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
        if(vNum != LEVELS && !testDone) {
            Toast.makeText(getApplicationContext(), "Please keep touching the screen.", Toast.LENGTH_SHORT).show();
            timesLifted++;
        }
    }

    private void endLevel(){
        ((TextView)(findViewById(R.id.vibrateLevelText))).setText("Level: " + (vNum-2));
    }

    private void finishTest(){
        timeEnd = System.currentTimeMillis();
        testDone = true;
        ((TextView)(findViewById(R.id.vibrateLevelText))).setText("FINISHED AT LEVEL: " + (vNum-VSTART+1));
        int level = vNum - VSTART + 1;
        double totalTime = (timeEnd-timeStart)/1000;
        System.out.println("Level: " + level + "\nTime: " + totalTime + "\nLifted: " + timesLifted);
        Database.getInstance().addVibrateTest(new VibrateTest(level, totalTime, timesLifted));
    }

    @Override
    protected void onPause() {
        super.onPause();
        h.removeCallbacks(vibrateThread);
        v.cancel();
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERM_REQUEST_REC_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runRecognizerSetup();
            } else {
                finish();
            }
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        h.removeCallbacks(vibrateThread);
        v.cancel();
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    private void runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(VibrationActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    Toast.makeText(getApplicationContext(), "Error. Failed to initialize voice input.",
                            Toast.LENGTH_SHORT).show();
                    System.out.println(result.toString());
                    System.out.println("FAILED");
                } else {
                    FINISHEDEXECUTE = true;
                }
            }
        }.execute();
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                //.setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)

                .getRecognizer();
        recognizer.addListener(this);

        File yesGrammar = new File(assetsDir, "yes.gram");
        recognizer.addGrammarSearch(FELT_VIB, yesGrammar);

    }



    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if(hypothesis == null) return;
        if(hypothesis.getHypstr().equals(FELT_VIB)){
            v.cancel();
            if(!testDone)
                finishTest();
            interrupted();
            if (recognizer != null) {
                recognizer.cancel();
                recognizer.shutdown();
            }
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if(hypothesis == null) return;
        if(hypothesis.getHypstr().equals(FELT_VIB)){
            v.cancel();
            if(!testDone)
                finishTest();
            interrupted();
            if (recognizer != null) {
                recognizer.cancel();
                recognizer.shutdown();
            }
        }
    }

    @Override
    public void onError(Exception e) {
        System.err.println(e.toString());
    }

    @Override
    public void onTimeout() {

    }


}
