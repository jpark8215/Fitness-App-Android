package com.developerjp.jieunworkouttracker;

import android.Manifest;
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

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class WorkoutService extends Service {
    // Change the value to any number you prefer
    private static final int FOREGROUND_SERVICE_PERMISSION_REQUEST_CODE = 123;
    private static final String LOG_TAG = "ForegroundService";
    private static final String LOG_TAG_BOUND = "BoundService";
    //TODO Use this instead of hardcoded values
    private static final String NOTIFICATION_CHANNEL_ID = "workout_progress_channel";
    // interface for clients that bind
    private final IBinder mBinder = new MyBinder();
    //Chronometer is used for the counter timer
    private Chronometer chronometer;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(LOG_TAG, "in onCreate");

        //Used with pausing the chronometer
        boolean mIsPaused = false;

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
        // Don't start foreground service here - wait for onStartCommand
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand called");
        
        // Handle notification dismissal
        if (intent != null && "STOP_WORKOUT".equals(intent.getAction())) {
            Log.d(LOG_TAG, "Notification dismissed, stopping service");
            stopSelf();
            return START_NOT_STICKY;
        }
        
        // Only start foreground service if we have workout data
        if (intent != null) {
            ArrayList<String> selectedExerciseIds = intent.getStringArrayListExtra("selected_exercise_ids");
            if (selectedExerciseIds != null && !selectedExerciseIds.isEmpty()) {
                Log.d(LOG_TAG, "Starting foreground service for workout");
                startForegroundService();
                return START_STICKY; // Restart service if killed
            }
        }
        
        Log.d(LOG_TAG, "No workout data, stopping service");
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "in onDestroy");
        chronometer.stop();
        
        // Clear the notification when service is destroyed
        stopForeground(true);
        
        // Cancel the notification
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.cancel(1);
            notificationManager.cancelAll(); // Cancel all notifications from this app
            Log.d(LOG_TAG, "Cleared all notifications");
        }
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

    // Is used to return the value of the chronometer
    public long getTime() {
        return chronometer.getBase();
    }
    
    // Method to update notification content
    public void updateNotification(String title, String text) {
        Intent notificationIntent = new Intent(this, StartWorkoutActivity.class);
        notificationIntent.putExtra("ongoing_workout", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.shield_heart_icon)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(createDeleteIntent())
                .build();
        
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.notify(1, notification);
        }
    }

    private void startForegroundService() {
        // Creates the notification channel
        createNotificationChannel();

        // Creates the notification intent
        Intent notificationIntent = new Intent(this, StartWorkoutActivity.class);
        notificationIntent.putExtra("ongoing_workout", true);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Creates and shows the notification
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Workout In Progress")
                .setContentText("Click here to go back to the workout")
                .setSmallIcon(R.drawable.shield_heart_icon)
                .setContentIntent(pendingIntent)
                .setTicker("Workout in Progress")
                .setAutoCancel(true) // Allow notification to be dismissed when tapped
                .setOngoing(false) // Allow notification to be dismissed by swiping
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(createDeleteIntent()) // Handle notification dismissal
                .build();

        // Start as foreground service but with dismissible notification
        startForeground(1, notification);
        
        // Also post as regular notification for better dismissibility
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.notify(1, notification);
        }
    }
    
    private PendingIntent createDeleteIntent() {
        Intent deleteIntent = new Intent(this, WorkoutService.class);
        deleteIntent.setAction("STOP_WORKOUT");
        return PendingIntent.getService(this, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            
            // Delete existing channel if it exists to ensure clean recreation
            if (notificationManager != null) {
                notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID);
            }
            
            CharSequence name = "Workout Progress";
            String description = "Shows ongoing workout progress";
            int importance = NotificationManager.IMPORTANCE_LOW; // Use LOW importance for dismissible notifications
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setShowBadge(false);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            
            // Register the channel with the system
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(LOG_TAG, "Created notification channel: " + NOTIFICATION_CHANNEL_ID);
            }
        }
    }

    public class MyBinder extends Binder {
        WorkoutService getService() {
            return WorkoutService.this;
        }
    }
}
