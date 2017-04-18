package com.example.oscar.cmsc436.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.oscar.cmsc436.R;
import com.example.oscar.cmsc436.data.Database;
import com.example.oscar.cmsc436.data.tests.BalloonTest;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BalloonActivity extends AppCompatActivity {

    private Button mButton, leftButton, rightButton;
    private TextView mText;
    // private FrameLayout frameLayout;
    //private CountDownTimer t1;
    private int numPops, location, time_var;
    private long  display_time, pop_time, timeElapsed;
    private double avg_response;
    public Runnable runnable;
    private int handSelected, numHits;
    private int lHits, rHits;
    private double lResponse, rResponse;
    private LinearLayout.LayoutParams lllp;
    private Map<String, String> data;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balloon);

        mButton = (Button) findViewById(R.id.mBalloon);
        mButton.setVisibility(View.INVISIBLE);
        mText = (TextView) findViewById(R.id.text_area);

        leftButton = (Button) findViewById(R.id.leftHandButton);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handSelected = 1;
                startTest();
            }
        });
        rightButton = (Button) findViewById(R.id.rightHandButton);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handSelected = 2;
                startTest();
            }
        });

        numPops = 0;
        timeElapsed = 0;
        numHits++;

        runnable = new Runnable() {
            @Override
            public void run() {

                location = randInt(1, 9);
                time_var = randInt(1, 2);
                time_var *= 1000;

                mButton.setText("POP");
                mButton.setVisibility(View.INVISIBLE);
                mText.setVisibility(View.INVISIBLE);
                mButton.setEnabled(false);

                new CountDownTimer(time_var, 100){
                    @Override
                    public void onTick(long l){

                    }
                    @Override
                    public void onFinish(){
                        lllp=(LinearLayout.LayoutParams)mButton.getLayoutParams();
                        if(location == 1){
                            lllp.gravity=Gravity.LEFT | Gravity.TOP;
                        } else if(location == 2){
                            lllp.gravity=Gravity.CENTER_HORIZONTAL | Gravity.TOP;
                        } else if(location == 3){
                            lllp.gravity=Gravity.RIGHT | Gravity.TOP;
                        } else if(location == 4){
                            lllp.gravity=Gravity.LEFT | Gravity.CENTER_VERTICAL;
                        } else if(location == 5){
                            lllp.gravity=Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
                        } else if(location == 6){
                            lllp.gravity=Gravity.RIGHT | Gravity.CENTER_VERTICAL;
                        } else if(location == 7){
                            lllp.gravity=Gravity.LEFT | Gravity.BOTTOM;
                        } else if(location == 8){
                            lllp.gravity=Gravity.CENTER_HORIZONTAL| Gravity.BOTTOM;
                        } else {
                            lllp.gravity=Gravity.RIGHT | Gravity.BOTTOM;
                        }

                        mButton.setLayoutParams(lllp);
                        mButton.setEnabled(true);
                        mButton.setVisibility(View.VISIBLE);
                        display_time = System.nanoTime();
                        mButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pop_time = System.nanoTime();
                                numPops++;
                                numHits++;
                                timeElapsed += pop_time - display_time;

                                if(numPops < 10) {
                                    run();
                                } else {
                                    avg_response = (double)timeElapsed / 1000000000.0; //convert to secs
                                    mButton.setVisibility(View.INVISIBLE);

                                    DecimalFormat df = new DecimalFormat("#.####");
                                    df.setRoundingMode(RoundingMode.CEILING);
                                    mText.setText("ALL DONE! \n\n\n" + "Your average response time was: " + df.format(avg_response/10.0) +
                                            " seconds");
                                    mText.setVisibility(View.VISIBLE);

                                    if(handSelected == 1) { //send results for left hand

                                        lHits = numHits;
                                        lResponse = avg_response/10;

                                    } else { //send results for right hand
                                        rHits = numHits;
                                        rResponse = avg_response/10;
                                        Database.getInstance().addBalloonTest(new BalloonTest(lResponse,lHits,rResponse,rHits,new Date()));
                                    }

                                    numHits = 0;
                                    numPops = 0;
                                    timeElapsed = 0;
                                    leftButton.setVisibility(View.VISIBLE);
                                    rightButton.setVisibility(View.VISIBLE);

                                }
                            }
                        });
                    }

                }.start();

            }
        };

    }

    protected void onResume() { // Register listener when resuming
        super.onResume();
        //Log.d("height", "" + frameLayout.getMeasuredHeight());
        //Log.d("width", "" + frameLayout.getMeasuredWidth());
    }
    protected void onPause() { // Turn off listener to save battery
        super.onPause();
    }

    public static int randInt(int min, int max) {

        // Usually this can be a field rather than a method variable
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    public void startTest(){
       /* mButton.setVisibility(View.INVISIBLE);
        mText.setVisibility(View.INVISIBLE);
        mButton.setEnabled(false); */
        //so button isnt accidentally pressed
        leftButton.setVisibility(View.INVISIBLE);
        rightButton.setVisibility(View.INVISIBLE);
        mButton.setText("POP");
        runnable.run();
    }
}