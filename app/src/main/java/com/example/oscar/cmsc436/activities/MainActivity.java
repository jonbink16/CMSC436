package com.example.oscar.cmsc436.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.oscar.cmsc436.R;
import com.example.oscar.cmsc436.data.Database;
import com.example.oscar.cmsc436.data.MyContextWrapper;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        Database.getInstance().initialize(preferences,
                new MyContextWrapper(getApplicationContext()));
        changeID(getCurrentFocus());
    }
    public void tapTest(View view) {
        startActivity(new Intent(this, TapActivity.class));
    }
    public void spiralTest(View view) {
        startActivity(new Intent(this, SpiralActivity.class));
    }
    public void levelTest(View view) {
        startActivity(new Intent(this, LevelActivity.class));
    }
    public void balloonTest(View view) {
        startActivity(new Intent(this, BalloonActivity.class));
    }
    public void armTest(View view) {
        startActivity(new Intent(this, ArmTestActivity.class));
    }
    public void postData(View view){
        startActivity(new Intent(this, PostDataActivity.class));
    }
    public void walkingTest(View view){startActivity(new Intent(this, WalkingTestActivity.class));}
    public void swayTest(View view) {
        startActivity(new Intent(this, SwayActivity.class));
    }
    public void outdoorWalkingTest(View view){startActivity(new Intent(this, OutdoorWalkActivity.class));}
    public void memoryTest(View view){startActivity(new Intent(this, MemoryActivity.class));}
    public void vibrationTest(View view){startActivity(new Intent(this, VibrationActivity.class));}

    public void changeID(View view){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Enter Your ID");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        dialog.setView(input);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Database.getInstance().setID(input.getText().toString());
                System.out.println(input.getText().toString());
            }

        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.show();
    }
}
