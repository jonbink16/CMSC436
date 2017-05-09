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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class MemoryActivity extends AppCompatActivity{

    private ImageView current_symbol;
    private int symbols[];
    private Random random;
    private int score = 0, currentSymbolNumber;
    private boolean testRunning = true;
    private CountDownTimer timer;
    private TextView scoreText;
    private static SpeechRecognizer speech = null;
    private static final String TAG = "MemoryTest";
    private Context context;
    private int totalNumQuestions = 0;
    private ArrayList<Integer> symbolsTested;
    private ArrayList<String> timeStamps;
    private DateFormat df;
    private Date date;
    private String prevTime, currTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory);

        random = new Random();
        scoreText = (TextView) findViewById(R.id.score);
        current_symbol = (ImageView) findViewById(R.id.current_symbol);
        symbols = new int[]{R.drawable.symbol1, R.drawable.symbol2, R.drawable.symbol3,
                R.drawable.symbol4, R.drawable.symbol5, R.drawable.symbol6,
                R.drawable.symbol7, R.drawable.symbol8, R.drawable.symbol9};
        symbolsTested = new ArrayList<>();
        df =  new SimpleDateFormat("ss:SSS");
        timeStamps = new ArrayList<>();

        changeSymbol();
        context = this;
        listen();

        timer = new CountDownTimer(90000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //do nothing for now

            }
            @Override
            public void onFinish() {
                int numWrong = totalNumQuestions - score;
                scoreText.setText("Number Correct: " + score + "\nNumber Wrong: " + numWrong);
                testRunning = false;
                speech.destroy();
            }
        }.start();
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
            Button buttonClicked = (Button) findViewById(view.getId());
            int numClicked = Integer.parseInt((String) buttonClicked.getText());
            if((numClicked - 1) == currentSymbolNumber)
                score++;

            scoreText.setText("Score: " + score);
            changeSymbol();
            listen();
        }
    }
    public void changeSymbol() {
        totalNumQuestions++;
        int r = random.nextInt(9);
        current_symbol.setImageResource(symbols[r]);
        currentSymbolNumber = r;

        prevTime = df.format(new Date());

        //System.out.println("TIME CLICKED: " + df.format(new Date()));
        //symbolsTested.add(currentSymbolNumber);
        //timeStamps.add(df.format(new Date()));
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
        int temp = currentSymbolNumber + 1;
        System.out.println("Compared " + numberSpoken + " to " + temp);
        scoreText.setText("Score: " + score);
        changeSymbol();
        listen();
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
            Log.d(TAG, "onReadyForSpeech");
        }
        public void onBeginningOfSpeech()
        {
            Log.d(TAG, "onBeginningOfSpeech");
        }
        public void onRmsChanged(float rmsdB)
        {
            Log.d(TAG, "onRmsChanged");
        }
        public void onBufferReceived(byte[] buffer)
        {
            Log.d(TAG, "onBufferReceived");
        }
        public void onEndOfSpeech() {
            Log.d(TAG, "onEndofSpeech");
        }
        public void onError(int error) {
            Log.d(TAG,  "error " +  error);
        }
        public void onResults(Bundle results) {
            String str = new String();
            ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++) {
                str += data.get(i);
            }
            System.out.println(data.get(0));
            checkRecording(data.get(0));
        }
        public void onPartialResults(Bundle partialResults)
        {
            Log.d(TAG, "onPartialResults");
        }
        public void onEvent(int eventType, Bundle params)
        {
            Log.d(TAG, "onEvent " + eventType);
        }
    }
}
//was used inside of buttonClicked function
            /*int buttonClicked = view.getId();
            if(buttonClicked == R.id.button1 && currentSymbolNumber == 0)
                score++;
            else if(buttonClicked == R.id.button2 && currentSymbolNumber == 1)
                score++;
            else if(buttonClicked == R.id.button3 && currentSymbolNumber == 2)
                score++;
            else if(buttonClicked == R.id.button4 && currentSymbolNumber == 3)
                score++;
            else if(buttonClicked == R.id.button5 && currentSymbolNumber == 4)
                score++;
            else if(buttonClicked == R.id.button6 && currentSymbolNumber == 5)
                score++;
            else if(buttonClicked == R.id.button7 && currentSymbolNumber == 6)
                score++;
            else if(buttonClicked == R.id.button8 && currentSymbolNumber == 7)
                score++;
            else if(buttonClicked == R.id.button9 && currentSymbolNumber == 8)
                score++;*/

