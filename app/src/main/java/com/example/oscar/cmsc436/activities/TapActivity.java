package com.example.oscar.cmsc436.activities;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.TextView;

import com.example.oscar.cmsc436.data.Database;
import com.example.oscar.cmsc436.R;
import com.example.oscar.cmsc436.data.tests.TapTest;

import java.util.Date;

import static com.example.oscar.cmsc436.R.id.tapAreaButton;


public class TapActivity extends AppCompatActivity {
    //test parameters: TestTime --> time of each test, NumTests --> number of trials
    private final int numTests = 3,
            testTime = 1;
    private Database db = Database.getInstance();
    private TextView timer;
    private Button startTest;
    private CountDownTimer t1;
    private Button tapArea;
    private int[] left, right;
    private int testNum, numTaps, leftTaps, rightTaps, tempNum, secLeft, time, diameter;
    private double rAvg, lAvg;
    private boolean leftTest;
    private String hand = "left";
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.5F);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tap);
        System.out.println(numTests);
        //System.out.println(R.integer.numTapTests);
        //System.out.println(R.integer.tapTestTime);
        leftTest = true;
        //left[0] --> first test
        //...
        //left[numTests-1] --> last test
        left = new int[numTests];
        //same structure as above for right hand data
        right = new int[numTests];
        testNum = 0;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        double screenInches = Math.sqrt(Math.pow(dm.widthPixels / dm.xdpi, 2) +
                Math.pow(dm.heightPixels / dm.ydpi, 2));
        double screenPixels = Math.sqrt(Math.pow(dm.widthPixels, 2) +
                Math.pow(dm.heightPixels, 2));
        Log.d("debug","Screen inches: " + screenInches);
        double ppi = screenPixels / screenInches;
        Log.d("debug","PPI: " + ppi);
        diameter = (int) ppi;
        startTest = (Button) findViewById(R.id.startTap);
        tapArea = (Button) findViewById(tapAreaButton);
        tapArea.setVisibility(View.INVISIBLE);
        tapArea.setWidth(diameter);

        timer = (TextView) findViewById(R.id.timerText);
        timer.setText("Seconds remaining: " + testTime);
        secLeft = 0;
        t1 = new CountDownTimer((testTime*1000), 100) {// Countdown from 10 seconds
            @Override
            public void onTick(long ms) {
                time = Math.round((float) ms / 1000.0f);
                if (time != secLeft) { // Fixes inaccuracy of countdown method
                    secLeft = time;
                    timer.setText("Seconds remaining: " + secLeft);
                } else
                    timer.setText("Seconds remaining: " + ms / 1000);
            }
            @Override
            public void onFinish() {
                tapArea.setVisibility(View.INVISIBLE);

                if (leftTest) { //Left hand
                    left[testNum] = numTaps;
                } else {//Right hand
                    right[testNum] = numTaps;
                }
                timer.setText("Number of taps achieved: " +
                        numTaps);
                numTaps = 0;//Reset number of taps per round
                // Manages the information shown on the start button
                if (!leftTest && testNum == numTests-1) {
                    rightTaps = 0;
                    for(int i = 0; i < right.length; i++){
                        rightTaps += right[i];
                    }
                    rAvg = rightTaps/numTests;
                    timer.setText("Tests finished. Avg number of taps for"
                            + "\nLeft hand: " + lAvg + "\nRight hand: " + rAvg);

                    db.addTapTest(new TapTest(left, right, new Date()));

                }else{

                    testNum++;

                    if (testNum == numTests) {
                        testNum = 0;
                        leftTest = false;
                        hand = "right";

                        leftTaps = 0;
                        for(int i = 0; i < right.length; i++){
                            leftTaps += left[i];
                        }
                        lAvg = leftTaps/numTests;
                        timer.setText("Number of total taps for the left hand: " +
                                leftTaps + "\nAvg number of taps for the left hand: " + lAvg +
                                "\nNow we will perform tasks on the right hand.");
                    }
                    // Hide button for 3 seconds after test finishes so user doesn't accidentally
                    // press it.
                    startTest.setVisibility(View.VISIBLE);
                    startTest.setEnabled(false);
                    secLeft = 0;
                    tempNum = testNum+1;
                    new CountDownTimer(4000, 100) {
                        @Override
                        public void onTick(long ms) {
                            time = Math.round((float) ms / 1000.0f);
                            if (time != secLeft) { // Fixes inaccuracy of countdown method
                                secLeft = time;
                                startTest.setText("Start " + hand + " hand test # " + tempNum +
                                        "\n(" + secLeft + ")");
                            } else
                                startTest.setText("Start " + hand + " hand test # " + tempNum +
                                        "\n(" + (ms / 1000) + ")");
                        }
                        @Override
                        public void onFinish() { // Enables button to be clicked for next test
                            startTest.setText("Start " + hand + " hand test # " + tempNum);
                            startTest.setEnabled(true);
                        }
                    }.start();
                    for(int i = 0; i < 3; i++){
                        Log.d("l"+i, Integer.toString(left[i]));
                    }
                    for(int i = 0; i < 3; i++){
                        Log.d("r"+i, Integer.toString(right[i]));
                    }
                }
            }
        };
    }
    public void startTest(View view) { // Counts the number of taps in a single test.
        //tapArea.setBackgroundColor(Color.parseColor("#66ff66"));
        startTest.setVisibility(View.INVISIBLE);
        tapArea.setVisibility(View.VISIBLE);
        t1.start();

        tapArea.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                tapArea.startAnimation(buttonClick);
                numTaps++;
            }
        });

    }
}