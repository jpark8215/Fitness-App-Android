package com.developerjp.jieunworkouttracker;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

public class ServiceUtils {

    /**
     * Checks if the WorkoutService is currently running
     *
     * @param context The application context
     * @return true if the service is running, false otherwise
     */
    public static boolean isWorkoutServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);
            for (ActivityManager.RunningServiceInfo service : services) {
                if (WorkoutService.class.getName().equals(service.service.getClassName())) {
                    Log.d("ServiceUtils", "WorkoutService is running");
                    return true;
                }
            }
        }
        Log.d("ServiceUtils", "WorkoutService is not running");
        return false;
    }

    /**
     * Stops the WorkoutService if it's running
     *
     * @param context The application context
     */
    public static void stopWorkoutService(Context context) {
        if (isWorkoutServiceRunning(context)) {
            Log.d("ServiceUtils", "Stopping WorkoutService");
            Intent serviceIntent = new Intent(context, WorkoutService.class);
            context.stopService(serviceIntent);
        }
        
        // Always clear all notifications regardless of service state
        clearAllNotifications(context);
    }
    
    /**
     * Clears all notifications from this app
     *
     * @param context The application context
     */
    public static void clearAllNotifications(Context context) {
        android.app.NotificationManager notificationManager = 
            (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
            Log.d("ServiceUtils", "Cleared all notifications");
        }
    }
} 