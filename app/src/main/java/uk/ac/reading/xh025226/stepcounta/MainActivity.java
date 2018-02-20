package uk.ac.reading.xh025226.stepcounta;

import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements SensorEventListener, StepListener {
    private TextView TvSteps, TvTime, TvDistance;
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private static final String TEXT_NUM_STEPS = "Number of Steps: "; // output
    private static final String TEXT_DISTANCE = "Distance: ";
    private static final String TAG = "MainActivity";
    private int numSteps,refStep, newStepCount;
    private long startTime,timeInterval,firstStepTime, nextStepTime, walkingTime;
    private double height, distance, stepLength;
    DatabaseHelper mDatabaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



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
        Button BtnClear = (Button) findViewById(R.id.btn_clear);
        Button BtnGraph = (Button) findViewById(R.id.btn_graph);
        Button fakeWalk = (Button) findViewById(R.id.button);

        timeInterval = 0;
        firstStepTime = 0;
        nextStepTime = 0;
        walkingTime = 0;
        height = 0;

        Timer updateTimer = new Timer();


        // setting up the number of steps
        if (mDatabaseHelper.isEmpty()){
            //Data Base is empty
            numSteps = 0;
            refStep = 0;
            distance = 0;
            TvSteps.setText(TEXT_NUM_STEPS + numSteps);
            TvDistance.setText(TEXT_DISTANCE + distance);
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
            //-- Remove ST
            Log.d(TAG, "Height: " + height);
            //-- Remove End
            stepLength = height * 0.415;//ratio of height to stepLength
            TvSteps.setText(TEXT_NUM_STEPS + numSteps);
            TvDistance.setText(TEXT_DISTANCE + distance);



        }


        sensorManager.registerListener(MainActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);


        BtnClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mDatabaseHelper.clearDataBase();
                numSteps = 0;
                refStep = 0;
                distance = 0;
                toastMessage("Database cleared");
                Log.d(TAG, "The dataBase was deleted.");
                TvSteps.setText(TEXT_NUM_STEPS + numSteps);
                TvDistance.setText(TEXT_DISTANCE + distance);

            }
        });

        BtnGraph.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0){
                Intent intent = new Intent(MainActivity.this,GraphActivity.class);
                startActivity(intent);
            }
        });

        fakeWalk.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0){
                numSteps++;
                distance =  distance + 45;//stepLength;

                DecimalFormat df = new DecimalFormat("0.00");
                String dist2 = df.format(distance); // transforms the data to 2 Decimal places

                TvSteps.setText(TEXT_NUM_STEPS + numSteps);
                TvDistance.setText(TEXT_DISTANCE + distance +"M"); // converts to meters
                // send value to the data base here
                addData(numSteps, 2);
                mDatabaseHelper.addDistance(distance);

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

                        Log.d(TAG, "Walking time: " + walkingTime);
                        TvTime.setText("Walking time: " + walkingTime);
                    }
                    else{
                        //toastMessage("Too slow fam");
                        Log.d(TAG, "" + timeInterval);
                    }

                }


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
        // Calculates the intervals between steps and the elapsed time
        //timeInterval = SystemClock.elapsedRealtime() - startTime;
        //toastMessage("Time: " + timeInterval);
        //Log.d(TAG, "Time: " + timeInterval);
        numSteps++;
        TvSteps.setText(TEXT_NUM_STEPS + numSteps);
        // send value to the data base here
        addData(numSteps, 2);

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
                Log.d(TAG, "Walking time: " + walkingTime);
                TvTime.setText("Walking time: " + walkingTime);
            }
            else{
                //toastMessage("Too slow fam");
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
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}



