package com.example.jieunworkouttracker;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.Manifest;


import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class WorkoutService extends Service {
    // Change the value to any number you prefer
    private static final int FOREGROUND_SERVICE_PERMISSION_REQUEST_CODE = 123;

    private static final String LOG_TAG = "ForegroundService";
    private static final String LOG_TAG_BOUND = "BoundService";
    //TODO Use this instead of hardcoded values
    private static final String NOTIFICATION_CHANNEL_ID = "1";

    //Chronometer is used for the counter timer
    private Chronometer chronometer;

    // interface for clients that bind
    private final IBinder mBinder = new MyBinder();

    //Used with pausing the chronometer
    private boolean mIsPaused;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(LOG_TAG, "in onCreate");

        mIsPaused = false;

        chronometer = new Chronometer(this);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        Log.d(LOG_TAG, "Chronometer Started");



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                ActivityCompat.requestPermissions((Activity) getApplicationContext(),
                        new String[]{Manifest.permission.FOREGROUND_SERVICE},
                        123);
            }
        }


        // Start the foreground service
        startForegroundService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Your existing onStartCommand code here
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "in onDestroy");
        chronometer.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG_BOUND, "in onBind");
        // A client is binding to the service with bindService()
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG_BOUND, "in onUnbind");
        return super.onUnbind(intent);
    }

    public class MyBinder extends Binder {
        WorkoutService getService() {
            return WorkoutService.this;
        }
    }

    // Is used to return the value of the chronometer
    public long getTime() {
        return chronometer.getBase();
    }

    private void startForegroundService() {
        // Creates the notification channel
        createNotificationChannel();

        // Creates the notification intent
        Intent notificationIntent = new Intent(this, StartWorkoutActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Creates and shows the notification
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Workout In Progress")
                .setContentText("Click here to update your workout log")
                .setSmallIcon(R.drawable.shield_heart_icon)
                .setContentIntent(pendingIntent)
                .setTicker("Workout in Progress")
                .build();

        // Starts the service as a foreground service
        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel name";
            String description = "channel description";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
