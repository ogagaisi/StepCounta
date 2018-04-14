package uk.ac.reading.xh025226.stepcounta;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ogaga isiavwe on 27-Nov-17.
 */
public class DatabaseHelper extends SQLiteOpenHelper{

    private static DatabaseHelper sInstance;
    private static final String TAG = "DatabaseHelper";
    private static final String TABLE_NAME = "steps_table";
    private static final String COL1 = "Steps";
    private static final String COL2 = "TotalSteps";
    private static final String COL3 = "Height";
    private static final String COL4 = "Distance";
    private static final String COL5 = "StepGoal";
    private static final String COL6 = "WalkingTime";


    private static final int DATABASE_VERSION = 6;

    public DatabaseHelper(Context context){
        super(context, TABLE_NAME, null, DATABASE_VERSION);
    }
    public static synchronized DatabaseHelper getInstance(Context context){

        if (sInstance == null){
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase Db){ // Creates the table.

        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL1 +" TEXT, " + COL2 + " INTEGER DEFAULT 0, " + COL3 + " REAL, " + COL4 + " REAL DEFAULT 0.0, " + COL5 + " INTEGER DEFAULT 0, " + COL6  + " INTEGER DEFAULT 0)";
        Db.execSQL(createTable);
        Log.d(TAG, "onCreate was called, Database has been updated");
    }

    @Override
    public void onUpgrade(SQLiteDatabase Db, int oldVersion, int newVersion){
        Db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(Db);
        Log.d(TAG, "onUpgrade was called, old version:" + oldVersion + " new version:" + newVersion);

    }

    public boolean addToStepGraph(int item){ //Adds to the step column
        SQLiteDatabase db = this.getWritableDatabase(); //Declares SQLite database object
        ContentValues contentValues = new ContentValues(); // Helps write to the database
        contentValues.put(COL1, item);

        Log.d(TAG, "addData: Adding " + item + " to " + TABLE_NAME);

        long result = db.insert(TABLE_NAME, null, contentValues); //Represents if data was inserted correctly or not . -1 if data was not inserted correctly or >0 if correct


        if (result == -1){
            return false;
        }
        else{
            return true;
        }
    }

    public boolean addTotalSteps(int item){ //Adds Data to the DataBase
        Log.d(TAG, "addTotalSteps() was called");
        SQLiteDatabase db = this.getWritableDatabase(); //Declares SQLite database object
        ContentValues contentValues = new ContentValues(); // Helps write to the database
        contentValues.put(COL2, item); // puts the data into column 2
        long result = -1;

        // used to check if the table is empty
        String count = "SELECT count(*) FROM " + TABLE_NAME;
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        mcursor.close();
        // if the table is populated, update the value of "totalSteps" else insert a value there
        if(icount>0){ // table is not empty
            Log.d(TAG, "addData: Adding " + item + " to " + COL2 + " in " + TABLE_NAME);

            result = db.update(TABLE_NAME, contentValues, "ID = 1", null); //Represents if data was inserted correctly or not . -1 if data was not inserted correctly or >0 if correct


        }
        else {//

            result = db.insert(TABLE_NAME, null, contentValues);
            Log.d(TAG, "Table was empty but " + item + " has been added to the table");

        }


        if (result == -1){
            return false;
        }
        else{
            return true;
        }
    }

    public boolean addHeigth(double item){ //Adds Data to the DataBase
        Log.d(TAG, "addHeigth() was called");
        SQLiteDatabase db = this.getWritableDatabase(); //Declares SQLite database object
        ContentValues contentValues = new ContentValues(); // Helps write to the database
        contentValues.put(COL3, item); // puts the data into column 3
        long result = -1;

        // used to check if the table is empty
        String count = "SELECT count(*) FROM " + TABLE_NAME;
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        mcursor.close();
        // if the table is populated, update the value of "height" else insert a value there
        if(icount>0){ // table is not empty
            Log.d(TAG, "addData: Adding " + item + " to " + COL3 + " in " + TABLE_NAME);

            result = db.update(TABLE_NAME, contentValues, "ID = 1", null); //Represents if data was inserted correctly or not . -1 if data was not inserted correctly or >0 if correct


        }
        else {//

            result = db.insert(TABLE_NAME, null, contentValues);
            Log.d(TAG, "Table was empty but " + item + " has been added to the table");

        }


        if (result == -1){
            return false;
        }
        else{
            return true;
        }
    }

    public boolean addDistance(double item){ //Adds Data to the DataBase
        Log.d(TAG, "addDistance() was called");
        SQLiteDatabase db = this.getWritableDatabase(); //Declares SQLite database object
        ContentValues contentValues = new ContentValues(); // Helps write to the database
        contentValues.put(COL4, item); // puts the data into column 4
        long result = -1;

        // used to check if the table is empty
        String count = "SELECT count(*) FROM " + TABLE_NAME;
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        mcursor.close();
        // if the table is populated, update the value of "distance" else insert a value there
        if(icount>0){ // table is not empty
            Log.d(TAG, "addData: Adding " + item + " to " + COL4 + " in " + TABLE_NAME);

            result = db.update(TABLE_NAME, contentValues, "ID = 1", null); //Represents if data was inserted correctly or not . -1 if data was not inserted correctly or >0 if correct


        }
        else {//

            result = db.insert(TABLE_NAME, null, contentValues);
            Log.d(TAG, "Table was empty but " + item + " has been added to the table");

        }


        if (result == -1){
            return false;
        }
        else{
            return true;
        }
    }
    public boolean setGoal(int item){ //Adds StepGoal to the database
        Log.d(TAG, "setGoal() was called");
        SQLiteDatabase db = this.getWritableDatabase(); //Declares SQLite database object
        ContentValues contentValues = new ContentValues(); // Helps write to the database
        contentValues.put(COL5, item); // puts the data into column 5 -- SetStepGoal
        long result = -1;

        // used to check if the table is empty
        String count = "SELECT count(*) FROM " + TABLE_NAME;
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        mcursor.close();
        // if the table is populated, update the value of "stepGoal" else insert a value there
        if(icount>0){ // table is not empty
            Log.d(TAG, "addData: Adding " + item + " to " + COL5 + " in " + TABLE_NAME);

            result = db.update(TABLE_NAME, contentValues, "ID = 1", null); //Represents if data was inserted correctly or not . -1 if data was not inserted correctly or >0 if correct


        }
        else {//

            result = db.insert(TABLE_NAME, null, contentValues);
            Log.d(TAG, "Table was empty but " + item + " has been added to the table");

        }


        if (result == -1){
            return false;
        }
        else{
            return true;
        }
    }
    public boolean addWalkingTime(long item){ //Adds Data to the DataBase
        Log.d(TAG, "addWalkingTime() was called");
        SQLiteDatabase db = this.getWritableDatabase(); //Declares SQLite database object
        ContentValues contentValues = new ContentValues(); // Helps write to the database
        contentValues.put(COL6, item); // puts the data into column 6
        long result = -1;

        // used to check if the table is empty
        String count = "SELECT count(*) FROM " + TABLE_NAME;
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        mcursor.close();
        // if the table is populated, update the value of "walkingTime" else insert a value there
        if(icount>0){ // table is not empty
            Log.d(TAG, "addData: Adding " + item + " to " + COL6 + " in " + TABLE_NAME);

            result = db.update(TABLE_NAME, contentValues, "ID = 1", null); //Represents if data was inserted correctly or not . -1 if data was not inserted correctly or >0 if correct


        }
        else {//

            result = db.insert(TABLE_NAME, null, contentValues);
            Log.d(TAG, "Table was empty but " + item + " has been added to the table");

        }


        if (result == -1){
            return false;
        }
        else{
            return true;
        }
    }

    public boolean isEmpty(){
        SQLiteDatabase db = this.getWritableDatabase();
        String count = "SELECT count(*) FROM " + TABLE_NAME; // returns the number of rows in the table
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        mcursor.close();
        if(icount>0){// The table is not empty
            return false;
        }

        else{ //table is empty
            return true;
        }

    }

    /***
     * Returns all the data from the database
     * @return
     */
    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    // returns the last row in the table
    public Cursor getLastRow(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY ID DESC LIMIT 1";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    // Returns the first row in the table
    public Cursor getFirstRow(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY ID ASC LIMIT 1"; // returns the first row on the table
        Cursor data = db.rawQuery(query, null);
        return data;

    }

    public void clearDataBase(){
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDBQuery = "DELETE FROM " + TABLE_NAME;
        String resetAutoIncrement = "DELETE FROM sqlite_sequence where name='"+ TABLE_NAME+"'";
        db.execSQL(clearDBQuery);
        db.execSQL(resetAutoIncrement);
    }

    public int getNoOfRows(){ // Returns the number of rows in the database
        SQLiteDatabase db = this.getWritableDatabase();
        String count = "SELECT count(*) FROM " + TABLE_NAME;
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        mcursor.close();

        return icount;
    }

}
