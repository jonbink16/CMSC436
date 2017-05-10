package com.example.oscar.cmsc436.activities;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.oscar.cmsc436.R;
import com.example.oscar.cmsc436.data.Database;
import com.example.oscar.cmsc436.data.tests.MemoryTest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class MemoryActivity extends AppCompatActivity{

    private ImageView current_symbol;
    private int symbols[];
    private Random random;
    private int score = 0, currentSymbolNumber, totalNumQuestions = 0;
    private boolean testRunning = true;
    private CountDownTimer timer;
    private TextView scoreText;
    private static SpeechRecognizer speech = null;
    private static final String TAG = "MemoryTest";
    private Context context;
    private HashMap<Integer, ArrayList<Long>> testResults;
    private TreeMap<Integer, ArrayList<Long>> metric;
    private DateFormat df;
    private Date prevTime, currTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory);

        context = this;
        random = new Random();
        scoreText = (TextView) findViewById(R.id.score);
        df =  new SimpleDateFormat("ss:SSS");

        current_symbol = (ImageView) findViewById(R.id.current_symbol);
        symbols = new int[]{R.drawable.symbol1, R.drawable.symbol2, R.drawable.symbol3,
                R.drawable.symbol4, R.drawable.symbol5, R.drawable.symbol6,
                R.drawable.symbol7, R.drawable.symbol8, R.drawable.symbol9};

        //metric = new HashMap<>();
        metric = new TreeMap<>();
        testResults = new HashMap<>();
        for(int i = 1; i <= 9; i++)
            testResults.put(i, new ArrayList<Long>());

        changeSymbol();
        listen();

        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //do nothing for now
            }
            @Override
            public void onFinish() {
                //metric of the number questions answered wrong.
                //The -1 at the end accounts for the last symbol being displayed but not having
                //time to click it
                int numWrong = totalNumQuestions - score - 1;
                scoreText.setText("Number Correct: " + score + "\nNumber Wrong: " + numWrong);
                testRunning = false;
                speech.destroy();

                long reactionTimeSum = 0;
                int numItems = 0;

                testResultsToMetric();
                ArrayList<Long> reactionAverages = new ArrayList<>();
                for (Map.Entry<Integer,ArrayList<Long>> entry : metric.entrySet()) {
                    reactionAverages.add(getAvg(entry.getValue()));
                    reactionTimeSum += getSum(entry.getValue());
                    numItems += entry.getValue().size();
                }

                //this metric measures average reaction speed for RIGHT answers
                long avgReactionSpeed = reactionTimeSum / numItems;

                //this is metric that measures learning rate (in milliseconds)
                int learningRate = leastSquares(new ArrayList<Integer>(metric.keySet()), reactionAverages);

                Database.getInstance().addMemoryTest(new MemoryTest(score, numWrong, avgReactionSpeed, learningRate, new Date()));

            }
        }.start();
    }
    //stores how many symbols we've seen x times
    public void testResultsToMetric() {
        for(int i = 1; i <= 9; i++) {
            int numTimes = testResults.get(i).size();
            if(metric.get(numTimes) == null && numTimes > 0)
                metric.put(numTimes, new ArrayList<Long>());
            if(numTimes > 0)
                metric.get(numTimes).addAll(testResults.get(i));
        }
    }
    public long getSum(ArrayList<Long> lst) {
        long sum = 0;
        for(long item: lst)
            sum += item;
        return sum;
    }
    public Long getAvg(ArrayList<Long> reactionTimes){
        long sum = 0;
        for(int i = 0; i < reactionTimes.size(); i++){
            sum += reactionTimes.get(i);
        }
        return sum/reactionTimes.size();
    }
    public int leastSquares(List<Integer> xValues, List<Long> yValues) {
        int sumX = 0, sumY = 0, sumXY = 0, sumXsquared = 0, sumYsquared = 0;
        int n = xValues.size();
        for(int i = 0; i < n; i++) {
            sumX += xValues.get(i);
            sumXsquared += (int) Math.pow((double) xValues.get(i), 2);
            sumYsquared += (int) Math.pow((double) yValues.get(i), 2);
            sumY += yValues.get(i);
            sumXY = xValues.get(i) * ((int) (long) yValues.get(i));
        }

        int sXY = sumXY - ((sumX * sumY) / n);
        int sXX = sumXsquared - (((int) Math.pow((double) sumX, 2)) / n);
        //int sYY = sumYsquared - (((int) Math.pow((double) sumY, 2)) / n);
        return sXY/sXX;

    }
    public void listen() {
        if (speech == null) {
            speech = SpeechRecognizer.createSpeechRecognizer(context);
            speech.setRecognitionListener(new listener());
        }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
        speech.startListening(intent);
        Log.i("RecognizerIntent: ","Now Listening");
    }
    public void buttonClicked(View view) {
        if(testRunning) {
            currTime = new Date();

            //time difference is in milliseconds
            long timeDiff = currTime.getTime() - prevTime.getTime();

            //use the view ID to get the button clicked and get the number it corresponds to
            Button buttonClicked = (Button) findViewById(view.getId());
            int numClicked = Integer.parseInt((String) buttonClicked.getText());

            //check if the button clicked corresponds to the current symbol displayed
            if((numClicked - 1) == currentSymbolNumber) {
                score++;
                testResults.get(currentSymbolNumber + 1).add(timeDiff);
            }

            scoreText.setText("Score: " + score);
            changeSymbol();
        }
    }
    public void changeSymbol() {
        prevTime = new Date();

        totalNumQuestions++;
        int r = random.nextInt(9);
        current_symbol.setImageResource(symbols[r]);
        currentSymbolNumber = r;

        //begin listening after we change the symbol
        listen();
    }
    public void checkRecording(String wordInput) {
        int numberSpoken = -1;
        if(wordInput.equals("1") || wordInput.equals("one") || wordInput.equals("won"))
            numberSpoken = 1;
        else if(wordInput.equals("2") || wordInput.equals("to") || wordInput.equals("too") || wordInput.equals("two"))
            numberSpoken = 2;
        else if(wordInput.equals("3") || wordInput.equals("three"))
            numberSpoken = 3;
        else if(wordInput.equals("4") || wordInput.equals("for") || wordInput.equals("four"))
            numberSpoken = 4;
        else if(wordInput.equals("5") || wordInput.equals("five"))
            numberSpoken = 5;
        else if(wordInput.equals("6") || wordInput.equals("six") || wordInput.equals("sex"))
            numberSpoken = 6;
        else if(wordInput.equals("7") || wordInput.equals("seven"))
            numberSpoken = 7;
        else if(wordInput.equals("8") || wordInput.equals("eight") || wordInput.equals("ate") || wordInput.equals("hate"))
            numberSpoken = 8;
        else if(wordInput.equals("9") || wordInput.equals("nine"))
            numberSpoken = 9;

        if(numberSpoken == (currentSymbolNumber + 1)) {
            score++;
        }
        scoreText.setText("Score: " + score);
        changeSymbol();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(speech != null) {
            speech.destroy();
            Log.i(TAG,"destroy");
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        speech.destroy();
    }
    class listener implements RecognitionListener
    {

        public void onReadyForSpeech(Bundle params)
        {
            //Log.d(TAG, "onReadyForSpeech");
        }
        public void onBeginningOfSpeech()
        {
            //Log.d(TAG, "onBeginningOfSpeech");
        }
        public void onRmsChanged(float rmsdB)
        {
            //Log.d(TAG, "onRmsChanged");
        }
        public void onBufferReceived(byte[] buffer)
        {
            //Log.d(TAG, "onBufferReceived");
        }
        public void onEndOfSpeech() {
            //Log.d(TAG, "onEndofSpeech");
        }
        public void onError(int error) {
            //Log.d(TAG,  "error " +  error);
        }
        public void onResults(Bundle results) {
            ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            checkRecording(data.get(0));
        }
        public void onPartialResults(Bundle partialResults)
        {
            //Log.d(TAG, "onPartialResults");
        }
        public void onEvent(int eventType, Bundle params)
        {
            //Log.d(TAG, "onEvent " + eventType);
        }
    }
}