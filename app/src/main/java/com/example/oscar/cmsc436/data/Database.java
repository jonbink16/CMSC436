package com.example.oscar.cmsc436.data;

import android.content.SharedPreferences;
import android.graphics.Bitmap;


import com.example.oscar.cmsc436.data.tests.ArmTest;
import com.example.oscar.cmsc436.data.tests.BalloonTest;
import com.example.oscar.cmsc436.data.tests.LevelTest;
import com.example.oscar.cmsc436.data.tests.MemoryTest;
import com.example.oscar.cmsc436.data.tests.OutdoorWalkTest;
import com.example.oscar.cmsc436.data.tests.SpiralTest;
import com.example.oscar.cmsc436.data.tests.TapTest;
import com.example.oscar.cmsc436.data.tests.VibrateTest;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by jonbink on 2/23/17.
 *
 * This is a singleton Database class that holds all the relevant information that we have to save.
 * Call the methods by calling "Database.getInstance().methodName()" or by creating a database
 * object "Database db = Database.getInstance()" and "db.methodName()".
 *
 */

public class Database {

    //singleton object
    private static final Database db;
    static{
        db = new Database();
    }
    private static final String tap_file = "tap_data_file";

    //getInstance() method for singleton class
    public static Database getInstance(){ return db; }

    //data stores
    private SharedPreferences prefs;
    private HashSet<String> times;
    private MyContextWrapper context;

    private String curr_ID;

    //has setPreferences() been called
    private boolean active1, active2;


    private HashMap<String, DataStore> hashStore;

    /**
     * Initialize data stores/fields.
     */
    private Database(){
        active1 = false;
        active2 = false;
        hashStore = new HashMap<>();
    }

    /**
     * Initialize the database by specifying a ContextWrapper, SharedPreferences location, and ID.
     * @param preferences
     */
    public void initialize(SharedPreferences preferences, MyContextWrapper context){
        prefs = preferences;
        active1 = true;
        this.context = context;
    }

    public String getID(){
        return curr_ID;
    }

    public void setID(String ID){
        this.curr_ID = ID;
        active2 = true;
    }


    /**
     * Returns whether the database has been activated yet by setting the preferences.
     * @return
     */
    public boolean isActive(){
        return active1 && active2;
    }


    /**
     * Clears all the data from the database.
     */
    public void clear(){
        FileOutputStream file;
        try{
            file = context.openFileOutput(tap_file, MODE_PRIVATE);
            BufferedOutputStream buffer = new BufferedOutputStream(file);
            buffer.write(0);
            buffer.flush();
            buffer.close();
            file.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        prefs.edit().clear().apply();
    }

    public void addTapTest(TapTest t){
        if(isActive()) {

            if (!hashStore.containsKey(curr_ID)) {
                hashStore.put(curr_ID, new DataStore());
            }

            hashStore.get(curr_ID).tapStore.add(t);

        }
    }

    public HashMap<String,DataStore> getHashStore(){
        return hashStore;
    }

    public void addSpiralTest(SpiralTest t){
        if(isActive()) {

            if (!hashStore.containsKey(curr_ID)) {
                hashStore.put(curr_ID, new DataStore());
            }

            hashStore.get(curr_ID).spiralStore.add(t);

        }
    }

    public void addLevelTest(LevelTest t){
        if(isActive()) {

            if (!hashStore.containsKey(curr_ID)) {
                hashStore.put(curr_ID, new DataStore());
            }

            hashStore.get(curr_ID).levelStore.add(t);

        }
    }

    public void addBalloonTest(BalloonTest t){
        if(isActive()) {

            if (!hashStore.containsKey(curr_ID)) {
                hashStore.put(curr_ID, new DataStore());
            }

            hashStore.get(curr_ID).balloonStore.add(t);

        }
    }

    public void addOutdoorWalkTest(OutdoorWalkTest t){
        if(isActive()) {

            if (!hashStore.containsKey(curr_ID)) {
                hashStore.put(curr_ID, new DataStore());
            }

            hashStore.get(curr_ID).outdoorWalkStore.add(t);

        }
    }

    public void addVibrateTest(VibrateTest t){
        if(isActive()) {

            if (!hashStore.containsKey(curr_ID)) {
                hashStore.put(curr_ID, new DataStore());
            }

            hashStore.get(curr_ID).vibrateStore.add(t);

        }
    }

    public void addMemoryTest(MemoryTest t){
        if(isActive()) {

            if (!hashStore.containsKey(curr_ID)) {
                hashStore.put(curr_ID, new DataStore());
            }

            hashStore.get(curr_ID).memoryStore.add(t);

        }
    }

    private HashMap<String,Bitmap> images = new HashMap<>();

    public void addImage(String name, Bitmap bmp){
        images.put(name,bmp);
    }

    public HashMap<String,Bitmap> getImages(){
        return images;
    }



    /**
     * A DataStore class that holds data structures storing the data of
     */
    public class DataStore{
        private ArrayList<TapTest> tapStore;
        private ArrayList<ArmTest> armStore;
        private ArrayList<BalloonTest> balloonStore;
        private ArrayList<SpiralTest> spiralStore;
        private ArrayList<LevelTest> levelStore;
        private ArrayList<OutdoorWalkTest> outdoorWalkStore;
        private ArrayList<VibrateTest> vibrateStore;
        private ArrayList<MemoryTest> memoryStore;
        //Data Structures for Spiral and Bubble

        private DataStore(){
            tapStore = new ArrayList<>();
            armStore = new ArrayList<>();
            balloonStore = new ArrayList<>();
            spiralStore = new ArrayList<>();
            levelStore = new ArrayList<>();
            outdoorWalkStore = new ArrayList<>();
            vibrateStore = new ArrayList<>();
            memoryStore = new ArrayList<>();
            //initialize other structures
        }

        public ArrayList<TapTest> getTapStore(){
            return tapStore;
        }

        public ArrayList<ArmTest> getArmStore(){
            return armStore;
        }

        public ArrayList<BalloonTest> getBalloonStore(){
            return balloonStore;
        }

        public ArrayList<SpiralTest> getSpiralStore(){
            return spiralStore;
        }

        public ArrayList<LevelTest> getLevelStore(){
            return levelStore;
        }

        public ArrayList<OutdoorWalkTest> getOutdoorWalkStore() { return outdoorWalkStore; }

        public ArrayList<VibrateTest> getVibrateStore() { return vibrateStore; }

        public ArrayList<MemoryTest> getMemoryStore() { return memoryStore; }


    }

}
