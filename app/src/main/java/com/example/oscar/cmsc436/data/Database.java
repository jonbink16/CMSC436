package com.example.oscar.cmsc436.data;

import android.content.SharedPreferences;


import com.example.oscar.cmsc436.data.tests.ArmTest;
import com.example.oscar.cmsc436.data.tests.BalloonTest;
import com.example.oscar.cmsc436.data.tests.LevelTest;
import com.example.oscar.cmsc436.data.tests.SpiralTest;
import com.example.oscar.cmsc436.data.tests.TapTest;

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

    private final String TRIAL_SEPARATOR = "!^!", NUM_SEPARATOR = "^$^", TEST_SEPARATOR = "@#@",
                        ID_SEPARATOR = "||", SEPARATOR = "--.--";

    //getInstance() method for singleton class
    public static Database getInstance(){ return db; }

    //data stores
    private SharedPreferences prefs;
    private HashSet<String> times;
    private MyContextWrapper context;

    private String curr_ID;

    //keys for shared preference values
    //sc_key is used to increment the filename of the screenshot bmp
    //tset_key saves the set of times that screenshots were taken
    private final String sc_key = "screenshot_count",
                         tset_key = "time_set",
                         tap_key = "tap_test_count";

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
        times = (HashSet<String>)prefs.getStringSet(tset_key,new HashSet<String>());
        this.context = context;
        //readFromInternalStorage();
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
     * Returns the next int to be used in screenshot name concatenation.
     */
    public int getScreenshot(){
        return prefs.getInt(sc_key, 0);
    }

    /**
     * Increment the screenshot value in the database. Call this after saving a screenshot.
     */
    public void incrementScreenshot(){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(sc_key,(prefs.getInt(sc_key,0)+1));
        editor.apply();
    }

    /**
     * Save the screenshot date
     * TODO: Figure out how to save the relevant screenshot information (location, time, score)
     */
    public void putScreenshotDate(){
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        times.add(formattedDate);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putStringSet(tset_key, times);
        edit.apply();
    }

    public void doq(){
        //System.out.println(serializeAll().toString());
        //readFromInternalStorage();
    }

    /**
     * Get the tap test number from the database.
     * @return
     */
    public int getTapNum(){
        return prefs.getInt(tap_key, 0);
    }

    /**
     * Increment the tap test number in the database.
     */
    public void incrementTapNum(){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(tap_key,(prefs.getInt(tap_key,0)+1));
        editor.apply();
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
            writeToInternalStorage();

        }
    }

    public HashMap<String,DataStore> getHashStore(){
        return hashStore;
    }

    public String getTapText(){
        String s = "";
        for(int i = 0; i < hashStore.get(curr_ID).tapStore.size(); i++){
            s += hashStore.get(curr_ID).tapStore.get(i).toString() + "\n";
        }
        return s;
    }

    public String getSpiralText(){
        String s = "";
        for(int i = 0; i < hashStore.get(curr_ID).spiralStore.size(); i++){
            s += hashStore.get(curr_ID).spiralStore.get(i).toString() + "\n";
        }
        return s;
    }

    public String getLevelText(){
        String s = "";
        for(int i = 0; i < hashStore.get(curr_ID).levelStore.size(); i++){
            s += hashStore.get(curr_ID).levelStore.get(i).toString() + "\n";
        }
        return s;
    }

    public String getBalloonText(){
        String s = "";
        for(int i = 0; i < hashStore.get(curr_ID).balloonStore.size(); i++){
            s += hashStore.get(curr_ID).balloonStore.get(i).toString() + "\n";
        }
        return s;
    }

    public String getArmText(){
        String s = "";
        for(int i = 0; i < hashStore.get(curr_ID).armStore.size(); i++){
            s += hashStore.get(curr_ID).armStore.get(i).toString() + "\n";
        }
        return s;
    }

    public void addArmTest(ArmTest t){
        if(isActive()) {

            if (!hashStore.containsKey(curr_ID)) {
                hashStore.put(curr_ID, new DataStore());
            }

            hashStore.get(curr_ID).armStore.add(t);
            writeToInternalStorage();

        }
    }

    public void addSpiralTest(SpiralTest t){
        if(isActive()) {

            if (!hashStore.containsKey(curr_ID)) {
                hashStore.put(curr_ID, new DataStore());
            }

            hashStore.get(curr_ID).spiralStore.add(t);
            writeToInternalStorage();

        }
    }

    public void addLevelTest(LevelTest t){
        if(isActive()) {

            if (!hashStore.containsKey(curr_ID)) {
                hashStore.put(curr_ID, new DataStore());
            }

            hashStore.get(curr_ID).levelStore.add(t);
            writeToInternalStorage();

        }
    }

    public void addBalloonTest(BalloonTest t){
        if(isActive()) {

            if (!hashStore.containsKey(curr_ID)) {
                hashStore.put(curr_ID, new DataStore());
            }

            hashStore.get(curr_ID).balloonStore.add(t);
            writeToInternalStorage();

        }
    }


    private void writeToInternalStorage(){
        /**
         * Write the Data to storage.
         */
        String helper = "";
        FileOutputStream file;
        try{
            file = context.openFileOutput(tap_file, context.MODE_PRIVATE);
            BufferedOutputStream buffer = new BufferedOutputStream(file);
            //System.out.println(new Gson().toJson(hashStore));
            /*for(Integer i : hashStore.keySet()){
                helper += i + SEPARATOR + hashStore.get(i).serialize();
            }*/
            //helper = new Gson().toJson(hashStore);
           // hashStore = new Gson().fromJson(helper, HashMap.class);
            //buffer.write(new Gson().toJson(hashStore).getBytes());
            buffer.flush();
            buffer.close();
            file.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void readFromInternalStorage(){
        /*
         * Load the tap data
         */

        FileInputStream file;
        try{
            file = context.openFileInput(tap_file);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(file, "UTF-8"));
            String line;
            while((line = buffer.readLine()) != null){
                System.out.println(line);
                //hashStore = new Gson().fromJson(line, new TypeToken<HashMap<Integer, DataStore>>(){}.getType());
            }
            buffer.close();
            file.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }


/**
    private void parseLine(String line){
        String id_s = line.split(ID_SEPARATOR)[1];
        System.out.println(id_s);
        int id = Integer.parseInt(id_s);
        String[] tapTests = (line.split(TEST_SEPARATOR)[1]).split(ID_SEPARATOR)[0]
                .split(TRIAL_SEPARATOR);
        int p = 0;
        for(String s : tapTests){
            System.out.print(p + ": ");
            System.out.println(s);
            p++;
        }

        System.out.println(tapTests.length);

        int[] right = new int[3], left = new int[3];

        if(!hashStore.containsKey(id)){
            hashStore.put(id, new DataStore());
        }else{
            System.err.print("FATAL ERROR. STOP");
            Log.d("FATAL ERROR", "ERROR");
        }
        for(int i = 0; i < tapTests.length; i+=9){
            left[i] = Integer.parseInt(tapTests[i]);
            left[i+1] = Integer.parseInt(tapTests[i+1]);
            left[i+2] = Integer.parseInt(tapTests[i+2]);

            right[i+4] = Integer.parseInt(tapTests[i+4]);
            right[i+5] = Integer.parseInt(tapTests[i+5]);
            right[i+6] = Integer.parseInt(tapTests[i+6]);
            hashStore.get(id).tapStore.add(new TapTest(left, right, new Date()));
        }


    }

    private String serializeAll(){
        StringBuilder sb = new StringBuilder();
        for(Integer i : hashStore.keySet()){
            //line.split(#)[0]
            sb.append(i).append(ID_SEPARATOR).append(TEST_SEPARATOR);
            //line.split(#)[1]
            sb.append(hashStore.get(i).serializeTap()).append(TEST_SEPARATOR);
            //line.split(#)[2]
            sb.append(hashStore.get(i).serializeSpiral()).append(TEST_SEPARATOR);
            //line.split(#)[3]
            sb.append(hashStore.get(i).serializeBubble()).append(TEST_SEPARATOR);
            sb.append("\n");
        }
        return sb.toString();
    }**/

    /**
     * A DataStore class that holds data structures storing the data of
     */
    public class DataStore{
        private ArrayList<TapTest> tapStore;
        private ArrayList<ArmTest> armStore;
        private ArrayList<BalloonTest> balloonStore;
        private ArrayList<SpiralTest> spiralStore;
        private ArrayList<LevelTest> levelStore;
        //Data Structures for Spiral and Bubble

        private DataStore(){
            tapStore = new ArrayList<>();
            armStore = new ArrayList<>();
            balloonStore = new ArrayList<>();
            spiralStore = new ArrayList<>();
            levelStore = new ArrayList<>();
            //initialize other structures
        }

        private String serialize(){
            return "";
            //return new Gson().toJson(tapStore);
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

    }

}
