package uk.ac.reading.xh025226.stepcounta;


import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
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

public class myService extends Service implements SensorEventListener, StepListener {
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private static final String TAG = "myService";
    private int numSteps,refStep, newStepCount, stepGoal;
    private long startTime,timeInterval,firstStepTime, nextStepTime, walkingTime, miliHour,miliMin;
    private double height, distance, stepLength;
    DatabaseHelper mDatabaseHelper;
    Intent intentA;
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder mBuilder;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "service starting");

        // sensor listeners here
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
        super.onCreate();
    }
    @Override
    public void onDestroy() {
        Log.d(TAG, "service done");
    }

    @Override
    public void step(long timeNs) {

        sendBroadcast(new Intent("REFRESH_DATA_INTENT"));
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

}
