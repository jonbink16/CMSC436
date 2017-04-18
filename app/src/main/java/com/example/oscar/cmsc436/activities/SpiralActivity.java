package com.example.oscar.cmsc436.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.oscar.cmsc436.R;
import com.example.oscar.cmsc436.data.Coordinate;
import com.example.oscar.cmsc436.data.Database;
import com.example.oscar.cmsc436.data.tests.SpiralTest;
import com.example.oscar.cmsc436.views.DrawingView;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.oscar.cmsc436.R.id.drawing;

public class SpiralActivity extends AppCompatActivity implements OnClickListener {
    private DrawingView drawView;
    private ImageButton currPaint, newBtn, saveBtn;
    private int handSelected;
    private float smallBrush, hLeft, hRight;
    //private Drawable spiralImg;
    private TextView tv;
    private Map<String, String> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spiral);

        newBtn = (ImageButton)findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        saveBtn = (ImageButton)findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        smallBrush = getResources().getInteger(R.integer.small_size);

        drawView = (DrawingView)findViewById(drawing);
        //LinearLayout paintLayout = (LinearLayout)findViewById(R.id.hand_selected);
        //handSelected = (ImageButton)paintLayout.getChildAt(0);
        //handSelected.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
        handSelected = 1;

        tv = (TextView) findViewById(R.id.hausdorff);

        drawView.setBrushSize(smallBrush);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.left_hand_radio:
                if (checked)
                    handSelected = 1;
                break;
            case R.id.right_hand_radio:
                if (checked)
                    handSelected = 2;
                break;
        }
    }

    @Override
    public void onClick(View view){
        if(view.getId()==R.id.new_btn){
            //new button
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("New drawing");
            newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    tv.setText("Score:");
                    drawView.destroyDrawingCache();
                    drawView.resetValues();
                    drawView.startNew();
                    dialog.dismiss();
                }
            });
            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            newDialog.show();
        } else if(view.getId()==R.id.save_btn){

            float h = -1;

            for (Coordinate c1: drawView.getMasterSpiral()){
                h = Math.max(h, c1.hausdorffDist(drawView.getTouches()));
            }

            h = Math.max(h, drawView.getHausdorff());

            if(handSelected == 1) { //send results for left hand
                hLeft = h;
            } else { //send results for right hand
                hRight = h;
                Database.getInstance().addSpiralTest(new SpiralTest(hLeft, hRight, new Date()));
            }


            tv.setText("Results saved successfully\nScore: " + Float.toString(h));

            //save drawing
            /*
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("Save drawing");
            saveDialog.setMessage("Save drawing to device Gallery?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    //save drawing
                    drawView.setDrawingCacheEnabled(true);
                    String imgSaved = MediaStore.Images.Media.insertImage(
                            getContentResolver(), drawView.getDrawingCache(),
                            UUID.randomUUID().toString()+".png", "drawing");
                    if(imgSaved!=null){



                        Toast savedToast = Toast.makeText(getApplicationContext(),
                                "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                        savedToast.show();
                    }
                    else{
                        Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                        unsavedToast.show();
                    }
                    drawView.destroyDrawingCache();
                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            saveDialog.show();
            */
        }
    }
}



/*
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.UUID;
import android.provider.MediaStore;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.oscar.cmsc436.data.Coordinate;
import com.example.oscar.cmsc436.data.Database;
import com.example.oscar.cmsc436.data.tests.SpiralTest;
import com.example.oscar.cmsc436.views.DrawingView;
import com.example.oscar.cmsc436.R;

import static com.example.oscar.cmsc436.R.id.drawing;

public class SpiralActivity extends AppCompatActivity implements OnClickListener {
    private DrawingView drawView;
    private ImageButton currPaint, newBtn, saveBtn;
    private float smallBrush, mediumBrush, largeBrush;
    //private Drawable spiralImg;
    private TextView tv;
    Database db = Database.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spiral);

        newBtn = (ImageButton)findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        saveBtn = (ImageButton)findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        drawView = (DrawingView)findViewById(drawing);
        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
        currPaint = (ImageButton)paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        tv = (TextView) findViewById(R.id.hausdorff);

        drawView.setBrushSize(smallBrush);
    }


    public void paintClicked(View view){
        //use chosen color
        if(view!=currPaint){
            //update color
            ImageButton imgView = (ImageButton)view;
            String color = view.getTag().toString();
            drawView.setColor(color);
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint=(ImageButton)view;
        }
    }

    @Override
    public void onClick(View view){
        if(view.getId()==R.id.new_btn){
            //new button
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("New drawing");
            newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    tv.setText("hausdorff");
                    drawView.destroyDrawingCache();
                    drawView.resetValues();
                    drawView.startNew();
                    dialog.dismiss();
                }
            });
            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            newDialog.show();
        } else if(view.getId()==R.id.save_btn){

            float h = -1;

            for (Coordinate c1: drawView.getMasterSpiral()){
                h = Math.max(h, c1.hausdorffDist(drawView.getTouches()));
            }

            h = Math.max(h, drawView.getHausdorff());

            db.addSpiralTest(new SpiralTest(h, new Date()));

            tv.setText(Float.toString(h));

            //save drawing
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("Save drawing");
            saveDialog.setMessage("Save drawing to device Gallery?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    //save drawing
                    drawView.setDrawingCacheEnabled(true);
                    if(isExternalStorageWritable()) {
                        try {
                            int screenshot = db.getScreenshot();
                            String sName = "sc_" + screenshot;
                            //save the date/time in the database
                            db.putScreenshotDate();

                            //create our app's Directory: /sdcard/DiagnosticApp/
                            File gallery = new File("/sdcard/DiagnosticApp");
                            if (!gallery.exists())
                                gallery.mkdir();
                            String mPath = Environment.getExternalStorageDirectory().toString() +
                                    "/DiagnosticApp/" + sName + ".jpg";

                            // create bitmap screen capture
                            View v1 = getWindow().getDecorView().getRootView();
                            v1.setDrawingCacheEnabled(true);
                            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
                            v1.setDrawingCacheEnabled(false);

                            File imageFile = new File(mPath);
                            if (!imageFile.exists()) {
                                //save the file
                                FileOutputStream outputStream = new FileOutputStream(imageFile);
                                int quality = 100;
                                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                                outputStream.flush();
                                outputStream.close();
                                db.incrementScreenshot();
                                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Error. Try Again.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            //openScreenshot(imageFile);
                        } catch (Throwable e) {
                            // Several error may come out with file handling or OOM
                            Toast.makeText(getApplicationContext(), "Error: Drawing could not be saved",
                                    Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Error: Problem with external drive",
                                Toast.LENGTH_SHORT).show();
                    }
                    drawView.destroyDrawingCache();
                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            saveDialog.show();

        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
*/