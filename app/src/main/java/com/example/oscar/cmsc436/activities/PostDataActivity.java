package com.example.oscar.cmsc436.activities;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.oscar.cmsc436.R;
import com.example.oscar.cmsc436.data.Database;
import com.example.oscar.cmsc436.data.tests.BalloonTest;
import com.example.oscar.cmsc436.data.tests.LevelTest;
import com.example.oscar.cmsc436.data.tests.SpiralTest;
import com.example.oscar.cmsc436.data.tests.TapTest;
import com.google.android.gms.tasks.RuntimeExecutionException;

import java.util.HashMap;

import cmsc436.tharri16.googlesheetshelper.CMSC436Sheet;


public class PostDataActivity extends AppCompatActivity implements CMSC436Sheet.Host{

    private Database db = Database.getInstance();
    private CMSC436Sheet group_sheet, com_sheet;
    private static final int LIB_ACCOUNT_NAME_REQUEST_CODE = 1001;
    private static final int LIB_AUTHORIZATION_REQUEST_CODE = 1002;
    private static final int LIB_PERMISSION_REQUEST_CODE = 1003;
    private static final int LIB_PLAY_SERVICES_REQUEST_CODE = 1004;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_data);
        group_sheet = new CMSC436Sheet(this, getString(R.string.app_name), getString(R.string.CMSC436Sheet_id_group));
        com_sheet = new CMSC436Sheet(this, getString(R.string.app_name), getString(R.string.CMSC436Sheet_id_shared));
        (findViewById(R.id.postButton)).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                postDataToSpreadSheet();
            }

        });
    }


    public void postDataToSpreadSheet(){
        String ID = db.getID();
        HashMap<String,Database.DataStore> hashStore = db.getHashStore();
        for(TapTest t : hashStore.get(ID).getTapStore()){
            for(int i : t.getLeft()) {
                group_sheet.writeData(CMSC436Sheet.TestType.LH_TAP, ID, i);
            }
            com_sheet.writeData(CMSC436Sheet.TestType.LH_TAP, ID, (float)t.getLtAvg());
            for(int i : t.getRight()) {
                group_sheet.writeData(CMSC436Sheet.TestType.RH_TAP, ID, i);
            }
            com_sheet.writeData(CMSC436Sheet.TestType.RH_TAP, ID, (float)t.getRtAvg());
        }
        for(SpiralTest s : hashStore.get(ID).getSpiralStore()){
            com_sheet.writeData(CMSC436Sheet.TestType.LH_SPIRAL, ID, s.getLeftMetric());
            group_sheet.writeData(CMSC436Sheet.TestType.LH_SPIRAL, ID, s.getLeftMetric());
            com_sheet.writeData(CMSC436Sheet.TestType.RH_SPIRAL, ID, s.getRightMetric());
            group_sheet.writeData(CMSC436Sheet.TestType.RH_SPIRAL, ID, s.getRightMetric());
        }
        for(LevelTest l : hashStore.get(ID).getLevelStore()){
            com_sheet.writeData(CMSC436Sheet.TestType.LH_LEVEL, ID, l.getLeftMetric());
            group_sheet.writeData(CMSC436Sheet.TestType.LH_LEVEL, ID, l.getLeftMetric());
            com_sheet.writeData(CMSC436Sheet.TestType.RH_LEVEL, ID, l.getRightMetric());
            group_sheet.writeData(CMSC436Sheet.TestType.RH_LEVEL, ID, l.getRightMetric());
        }
        for(BalloonTest b : hashStore.get(ID).getBalloonStore()){
            com_sheet.writeData(CMSC436Sheet.TestType.LH_POP, ID, b.getLMetric());
            group_sheet.writeData(CMSC436Sheet.TestType.LH_POP, ID, b.getLMetric());
            com_sheet.writeData(CMSC436Sheet.TestType.RH_POP, ID, b.getRMetric());
            group_sheet.writeData(CMSC436Sheet.TestType.RH_POP, ID, b.getRMetric());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        group_sheet.onActivityResult(requestCode, resultCode, data);
        com_sheet.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public int getRequestCode(CMSC436Sheet.Action action) {
        switch (action) {
            case REQUEST_ACCOUNT_NAME:
                return LIB_ACCOUNT_NAME_REQUEST_CODE;
            case REQUEST_AUTHORIZATION:
                return LIB_AUTHORIZATION_REQUEST_CODE;
            case REQUEST_PERMISSIONS:
                return LIB_PERMISSION_REQUEST_CODE;
            case REQUEST_PLAY_SERVICES:
                return LIB_PLAY_SERVICES_REQUEST_CODE;
            default:
                return -1; // boo java doesn't know we exhausted the enum
        }
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void notifyFinished(Exception e) {
        if(e != null){
            throw new RuntimeException(e);
        }
        System.out.println("DONE");
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        group_sheet.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
