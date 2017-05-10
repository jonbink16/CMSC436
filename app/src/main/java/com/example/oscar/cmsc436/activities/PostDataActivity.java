package com.example.oscar.cmsc436.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.oscar.cmsc436.R;
import com.example.oscar.cmsc436.data.Database;
import com.example.oscar.cmsc436.data.tests.BalloonTest;
import com.example.oscar.cmsc436.data.tests.LevelTest;
import com.example.oscar.cmsc436.data.tests.MemoryTest;
import com.example.oscar.cmsc436.data.tests.OutdoorWalkTest;
import com.example.oscar.cmsc436.data.tests.SpiralTest;
import com.example.oscar.cmsc436.data.tests.TapTest;
import com.example.oscar.cmsc436.data.tests.VibrateTest;


import java.util.HashMap;

import edu.umd.cmsc436.sheets.Sheets;


public class PostDataActivity extends AppCompatActivity implements Sheets.Host{

    private Database db = Database.getInstance();
    private Sheets sheet;
    private static final int LIB_ACCOUNT_NAME_REQUEST_CODE = 1001;
    private static final int LIB_AUTHORIZATION_REQUEST_CODE = 1002;
    private static final int LIB_PERMISSION_REQUEST_CODE = 1003;
    private static final int LIB_PLAY_SERVICES_REQUEST_CODE = 1004;
    private static final int LIB_CONNECTION_REQUEST_CODE = 1005;

    private String ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_data);
        sheet = new Sheets(this, this, getString(R.string.app_name),
                getString(R.string.CMSC436Sheet_id_shared), getString(R.string.CMSC436Sheet_id_group));
        (findViewById(R.id.postButton)).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                postDataToSpreadSheet();
            }

        });
        ID = db.getID();
        if(!Database.getInstance().getHashStore().containsKey(ID) || Database.getInstance().getHashStore().get(ID).isEmpty()){
            findViewById(R.id.postButton).setClickable(false);
            findViewById(R.id.postButton).setEnabled(false);
            Toast.makeText(getApplicationContext(), "Please finish some tests while entering a valid ID",
                    Toast.LENGTH_SHORT).show();
        }
    }


    public void postDataToSpreadSheet(){

        HashMap<String,Database.DataStore> hashStore = db.getHashStore();
        for(TapTest t : hashStore.get(ID).getTapStore()){
            sheet.writeTrials(Sheets.TestType.LH_TAP, ID, t.getLeft());
            sheet.writeData(Sheets.TestType.LH_TAP, ID, t.getLtAvg());
            sheet.writeTrials(Sheets.TestType.RH_TAP, ID, t.getRight());
            sheet.writeData(Sheets.TestType.RH_TAP, ID, t.getRtAvg());
        }
        for(SpiralTest s : hashStore.get(ID).getSpiralStore()){
            sheet.writeData(Sheets.TestType.LH_SPIRAL, ID, s.getLeftMetric());
            sheet.writeTrials(Sheets.TestType.LH_SPIRAL, ID, new float[]{s.getLeftMetric()});
            sheet.writeData(Sheets.TestType.RH_SPIRAL, ID, s.getRightMetric());
            sheet.writeTrials(Sheets.TestType.RH_SPIRAL, ID,  new float[]{s.getRightMetric()});
        }
        for(LevelTest l : hashStore.get(ID).getLevelStore()){
            sheet.writeData(Sheets.TestType.LH_LEVEL, ID, l.getLeftMetric());
            sheet.writeTrials(Sheets.TestType.LH_LEVEL, ID,  new float[]{l.getLeftMetric()});
            sheet.writeData(Sheets.TestType.RH_LEVEL, ID, l.getRightMetric());
            sheet.writeTrials(Sheets.TestType.RH_LEVEL, ID,  new float[]{l.getRightMetric()});
        }
        for(BalloonTest b : hashStore.get(ID).getBalloonStore()){
            sheet.writeData(Sheets.TestType.LH_POP, ID, b.getLMetric());
            sheet.writeTrials(Sheets.TestType.LH_POP, ID,  new float[]{b.getLMetric()});
            sheet.writeData(Sheets.TestType.RH_POP, ID, b.getRMetric());
            sheet.writeTrials(Sheets.TestType.RH_POP, ID,  new float[]{b.getRMetric()});
        }
        for(OutdoorWalkTest o : hashStore.get(ID).getOutdoorWalkStore()){
            System.out.println(o.getMps());
            sheet.writeData(Sheets.TestType.OUTDOOR_WALKING, ID, o.getMps());
        }
        for(MemoryTest m : hashStore.get(ID).getMemoryStore()){

        }
        for(VibrateTest v : hashStore.get(ID).getVibrateStore()){
            sheet.writeData(Sheets.TestType.VIBRATION, ID, v.getLevel());
            sheet.writeTrials(Sheets.TestType.VIBRATION, ID, v.getRawData());
        }
        for(String str : db.getImages().keySet()){
            sheet.uploadToDrive(getString(R.string.folder_name), str, db.getImages().get(str));
        }
        //clear data so you cannot upload twice
        Database.getInstance().clearID(ID);
        if(!Database.getInstance().getHashStore().containsKey(ID) || Database.getInstance().getHashStore().get(ID).isEmpty()) {
            findViewById(R.id.postButton).setClickable(false);
            findViewById(R.id.postButton).setEnabled(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        sheet.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public int getRequestCode(Sheets.Action action) {
        switch (action) {
            case REQUEST_ACCOUNT_NAME:
                return LIB_ACCOUNT_NAME_REQUEST_CODE;
            case REQUEST_AUTHORIZATION:
                return LIB_AUTHORIZATION_REQUEST_CODE;
            case REQUEST_PERMISSIONS:
                return LIB_PERMISSION_REQUEST_CODE;
            case REQUEST_PLAY_SERVICES:
                return LIB_PLAY_SERVICES_REQUEST_CODE;
            case REQUEST_CONNECTION_RESOLUTION:
                return LIB_CONNECTION_REQUEST_CODE;
            default:
                return -1;
        }
    }

    @Override
    public void notifyFinished(Exception e) {
        if(e != null){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        sheet.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
