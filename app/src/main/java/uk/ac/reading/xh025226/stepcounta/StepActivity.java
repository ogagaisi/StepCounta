package uk.ac.reading.xh025226.stepcounta;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;

import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;


public class StepActivity extends AppCompatActivity implements SensorEventListener, StepListener {
    private TextView TvSteps, TvTime, TvDistance;
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private static final String TEXT_NUM_STEPS = "Number of Steps: "; // output
    private static final String REFRESH = "REFRESH_DATA_INTENT";
    private static final String TEXT_DISTANCE = "Distance: ";
    private static final String TEXT_TIME = "Walking time: ";
    private static final String TAG = "StepActivity", textTitle ="New Achievement", textContent ="You have reached your step goal, good job";
    private int numSteps,refStep, newStepCount, stepGoal;
    private static final int notificationId =1000;
    private long startTime,timeInterval,firstStepTime, nextStepTime, walkingTime, miliHour,miliMin;
    private double height, distance, stepLength;
    private DataUpdateReceiver dataUpdateReceiver;
    DatabaseHelper mDatabaseHelper;
    Intent intentA;
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder mBuilder;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, myService.class));
        setContentView(R.layout.step_activity);


        //Set the notification's tap action
        intentA = new Intent(this, AlertDetails.class);
        intentA.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//helps preserve the user's expected navigation experience after they open the app via the notification.
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentA, 0);

        //Sets the notification content
                mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.trophy)
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);//automatically removes the notification when the user taps it.


        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        mDatabaseHelper = DatabaseHelper.getInstance(this);
        startTime = SystemClock.elapsedRealtime();// get the system time
        TvSteps = (TextView) findViewById(R.id.tv_steps);
        TvTime = (TextView)findViewById(R.id.tv_walkTime);
        TvDistance = (TextView) findViewById(R.id.tv_distance);
        Button btnClear = (Button) findViewById(R.id.btn_clear);
        Button btnGraph = (Button) findViewById(R.id.btn_graph);
        Button btnSetGoal = (Button) findViewById(R.id.btn_setGoal);
        notificationManager = NotificationManagerCompat.from(this);

        timeInterval = 0;
        firstStepTime = 0;
        nextStepTime = 0;
        walkingTime = 0;
        height = 0;
        stepGoal = 0;
        miliMin = 60000L;
        miliHour = miliMin * 60L;




        Timer updateTimer = new Timer();


        // setting up the number of steps
        if (mDatabaseHelper.isEmpty()){
            //Data Base is empty
            numSteps = 0;
            refStep = 0;
            distance = 0;
            stepLength = 0;
            TvSteps.setText(TEXT_NUM_STEPS + numSteps);
            displayDistance(distance);
            TvTime.setText(TEXT_TIME+ "0h:0m");

            Log.d(TAG, "The database is empty");
        }
        else{
            //Data base has something in it
            // set the numSteps to the value.
            Cursor data = mDatabaseHelper.getFirstRow();
            data.moveToFirst();
            numSteps = data.getInt(2); //getInt(2) column 2 has total number of steps
            distance = data.getDouble(4); //Column 4 is the distance
            refStep = numSteps;
            height = data.getDouble(3); //Column 3 has the height
            walkingTime = data.getLong(6); //Coumn 6 has the walking time
            stepLength = height * 0.415;//ratio of height to stepLength
            TvSteps.setText(TEXT_NUM_STEPS + numSteps);
            displayWalkingTime(walkingTime,miliHour,miliMin);
            displayDistance(distance);


        }


        sensorManager.registerListener(StepActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);


        btnClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mDatabaseHelper.clearDataBase();
                mDatabaseHelper = DatabaseHelper.getInstance(StepActivity.this);
                numSteps = 0;
                refStep = 0;
                distance = 0;
                walkingTime = 0;
                toastMessage("Database cleared");
                Log.d(TAG, "The dataBase was deleted.");
                TvSteps.setText(TEXT_NUM_STEPS + numSteps);
                TvTime.setText(TEXT_TIME + walkingTime);
                TvTime.setText(TEXT_TIME+ "0h:0m");
                displayDistance(distance);

            }
        });

        btnGraph.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(StepActivity.this, GraphActivity.class);
                startActivity(intent);
            }
        });

        btnSetGoal.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(StepActivity.this, SetStepGoal.class);
                startActivity(intent);
            }
        });


        updateTimer.schedule(new TimerTask() {
            public void run() {
                try {
                    Log.d(TAG, "2 mins");
                    /* the code below is used to update the step graph every 2 mins (it will be changed to 15)
                    * when the app is started refStep is set to the value of numSteps (number of steps done)
                    * if numstep > refstep, the user has taking new steps within the time frame and this is added to the graph, if not add zero (no steps) into the graph
                    * refStep is set to the new value of numStep again to ensure continuous comparison
                    */
                    if (numSteps > refStep) {
                        newStepCount = numSteps - refStep;
                        addData(newStepCount, 1);
                        Log.d(TAG, newStepCount + " new step(s) have been added to the step graph");
                        refStep = numSteps;
                    } else {
                        addData(0, 1);
                        Log.d(TAG, "Zero has been added to the step graph");
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }, 0, 120000); //60,000 milli secs = 60 secs = 1 minute

    }

    @Override
    public void onResume(){
        super.onResume();
        if(dataUpdateReceiver == null) dataUpdateReceiver = new DataUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter(REFRESH);
        registerReceiver(dataUpdateReceiver, intentFilter);
    }

    @Override
    public void onPause(){
        super.onPause();
        if (dataUpdateReceiver != null) unregisterReceiver(dataUpdateReceiver);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void step(long timeNs) {

        numSteps++;
        distance =  distance + stepLength;
        TvSteps.setText(TEXT_NUM_STEPS + numSteps);
        displayDistance(distance);
        // send value to the data base here
        addData(numSteps, 2);
        mDatabaseHelper.addDistance(distance);

        Cursor data = mDatabaseHelper.getFirstRow();
        data.moveToFirst();
        stepGoal = data.getInt(5);// Get data at column 5
        data.close();

        if(numSteps == stepGoal){
            // show the notification
            Log.d(TAG, "Step goal has been reached: " + stepGoal);

            notificationManager.notify(notificationId, mBuilder.build());
        }

        if (firstStepTime == 0){ // makes sure this is the first step
            firstStepTime = SystemClock.elapsedRealtime();
        }
        else{
            // Compare the time of the first step to the next step
            nextStepTime = SystemClock.elapsedRealtime();
            timeInterval = nextStepTime - firstStepTime;
            firstStepTime = nextStepTime;
            if(timeInterval<= 2500){
                walkingTime = walkingTime + timeInterval;
                distance = distance + stepLength;
                Log.d(TAG, TEXT_TIME + walkingTime);
                if(walkingTime >= miliMin){ // displays the walking time in the format h:m
                    displayWalkingTime(walkingTime,miliHour,miliMin);
                    mDatabaseHelper.addWalkingTime(walkingTime);
                }
                else{
                    TvTime.setText(TEXT_TIME+ "0h:0m");
                }

            }
            else{
                Log.d(TAG, "" + timeInterval);
            }

        }



}




    // Adds data to the database
    public void addData(int newEntry, int option){
        // option == 1 adds the data to the graph
        // option == 2 adds the data to the total steps
        boolean insertData = false;
        if(option == 1){
            insertData = mDatabaseHelper.addToStepGraph(newEntry);
        }
        else if (option == 2){
            insertData = mDatabaseHelper.addTotalSteps(newEntry);

        }
        else{

        }


        if (insertData){
        }
        else{
            toastMessage("Something went wrong");
        }

    }
    public void updateTSteps(int newEntry){
        boolean updateData = mDatabaseHelper.addTotalSteps(newEntry);

        if (updateData){
        }
        else{
            toastMessage("Something went wrong");
        }

    }

    /**
     * Converts and displays the distance in Meters(M) or kilometers(KM)
     * @param distance The distance in centimeter (CM)
     * */
    private void displayDistance(double distance){
        String distUnit = "";
        DecimalFormat df = new DecimalFormat("0.00");
        String dist2dp = df.format(distance/100); // transforms the data to 2 Decimal places.

        if(distance >= 0 && distance < 99999.50){ // distance is less than 1KM
            distUnit = "M";
        }
        else if (distance >= 99999.50){ // distance is >= 1KM
            distUnit = "KM";
            dist2dp = df.format(distance/100000);
        }
        else{

        }
        TvDistance.setText(TEXT_DISTANCE + dist2dp + distUnit); // converts to meters

    }
    /**
     * customizable toast
     * @param message the message to be displayed
     */
    private void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays the walling time in h:m format
     * @param walkingTime the walking time
     * @param  miliHour an hour in mili seconds
     * @param miliMin a minute in mili seconds
     */
    
    private void displayWalkingTime(long walkingTime,long miliHour, long miliMin){
        long hours = walkingTime/ miliHour; //converts time in nanosec to hours and drops the decimal part
        long remainder = walkingTime - hours * miliHour;
        long mins = remainder/ miliMin;

        TvTime.setText(TEXT_TIME+ hours + "h:" + mins +"m");
    }
    private class DataUpdateReceiver extends BroadcastReceiver{
        @Override
        public  void onReceive(Context context, Intent intent){
            if(intent.getAction().equals(REFRESH)){
                Log.d(TAG, "The service says hello.");
            }
        }
    }
}



