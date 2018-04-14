package uk.ac.reading.xh025226.stepcounta;


import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class myService extends Service implements SensorEventListener, StepListener {
    private Cursor data;
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private static final String TAG = "myService", textTitle ="New Achievement", textContent ="You have reached your step goal, good job";
    private int numSteps,refStep, newStepCount, stepGoal;
    private long startTime,timeInterval,firstStepTime, nextStepTime, walkingTime, miliHour,miliMin;
    private double height, distance, stepLength;
    DatabaseHelper mDatabaseHelper;
    Intent intentA;
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder mBuilder;
    Timer updateTimer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "service starting");

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

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        Log.d(TAG, "service created");
        //Set the notification's tap action
        intentA = new Intent(this, AlertDetails.class);
        intentA.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//helps preserve the user's expected navigation experience after they open the app via the notification.
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentA, 0);

        //Sets the notification content
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.trophy)
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);//automatically removes the notification when the user taps it.



        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(myService.this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        mDatabaseHelper = DatabaseHelper.getInstance(this);

        notificationManager = NotificationManagerCompat.from(this);

        timeInterval = 0;
        firstStepTime = 0;
        nextStepTime = 0;
        walkingTime = 0;
        height = 0;
        stepGoal = 0;
        miliMin = 60000L;
        miliHour = miliMin * 60L;

        updateTimer = new Timer();
        // setting up the number of steps
        if (mDatabaseHelper.isEmpty()){
            //Data Base is empty
            numSteps = 0;
            refStep = 0;
            distance = 0;
            stepLength = 0;

            Log.d(TAG, "The database is empty");
        }
        else{
            //Data base has something in it
            // set the numSteps to the value.
            data = mDatabaseHelper.getFirstRow();
            data.moveToFirst();
            numSteps = data.getInt(2); //getInt(2) column 2 has total number of steps
            distance = data.getDouble(4); //Column 4 is the distance
            refStep = numSteps;
            height = data.getDouble(3); //Column 3 has the height
            walkingTime = data.getLong(6); //Coumn 6 has the walking time
            stepLength = height * 0.415;//ratio of height to stepLength
        }
        super.onCreate();
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
    @Override
    public void onDestroy() {
        Log.d(TAG, "service done");
    }

    @Override
    public void step(long timeNs) {
        data = mDatabaseHelper.getFirstRow();
        data.moveToFirst();
        if(data.getString(7).equals("y")) { //Database has been cleared
            numSteps = 0;
            mDatabaseHelper.addToDelete("N");
        }
        numSteps++;
        addData(numSteps, 2); // add steps to totalSteps in the data base
        sendBroadcast(new Intent("REFRESH_DATA_INTENT"));
        data.close();
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

    /**
     * customizable toast
     * @param message the message to be displayed
     */
    private void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
