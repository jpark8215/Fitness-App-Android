package com.developerjp.jieunworkouttracker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DBManager {

    private DatabaseHelper dbHelper;
    private final Context context;
    private SQLiteDatabase database;

    public DBManager(Context c) {
        context = c;
    }

    public DBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }


    public void insertWorkout(String name) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.WORKOUT, name);
        contentValue.put(DatabaseHelper.ARCHIVE, 0);

        database.insert(DatabaseHelper.TABLE_NAME_WORKOUTS, null, contentValue);
    }

//TODO Find a way to de-primary key Exercise ID so duplicated exerciseID can be generated
    public void insertExercise(String id, String exerciseName, Double exerciseWeight) {
        ContentValues contentValue = new ContentValues();

        contentValue.put(DatabaseHelper.WORKOUT_ID, id);
        contentValue.put(DatabaseHelper.EXERCISE, exerciseName);
        long exerciseId = database.insert(DatabaseHelper.TABLE_NAME_EXERCISES, null, contentValue);

        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(DatabaseHelper.EXERCISE_ID, exerciseId);
        contentValues2.put(DatabaseHelper.WORKOUT_ID, id);
        contentValues2.put(DatabaseHelper.SET1, 5);
        contentValues2.put(DatabaseHelper.SET2, 5);
        contentValues2.put(DatabaseHelper.SET3, 5);
        contentValues2.put(DatabaseHelper.SET4, 5);
        contentValues2.put(DatabaseHelper.SET5, 5);
        contentValues2.put(DatabaseHelper.WEIGHT, exerciseWeight);

        //Is used to put the current datetime into the LOGS table datetime field
        Date datetime = Calendar.getInstance().getTime();
        contentValues2.put(DatabaseHelper.DATETIME, datetime.toString());

        //Is used to put the current date into the LOGS table date field
        //We had to record the date by itself separate from the datetime to make querying the database easier for some of the calendar queries
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date());
        contentValues2.put(DatabaseHelper.DATE, date);

        database.insert(DatabaseHelper.TABLE_NAME_LOGS, null, contentValues2);
    }


    //Is called when the user starts a workout.
    //Creates a new exercise log for them to track the workout.
    public void insertExerciseLogs(String id, String numOfExercises) {

        ContentValues contentValues = new ContentValues();
        Cursor cursor = fetchExerciseLogs(id, numOfExercises);

        if (cursor != null) {
            if (cursor.moveToLast()) {
                do {
                    int exerciseIdColumnIndex = cursor.getColumnIndex("exercise_id");
                    if (exerciseIdColumnIndex != -1) {
                        contentValues.put(DatabaseHelper.EXERCISE_ID, cursor.getString(exerciseIdColumnIndex));
                    }

                    int workoutIdColumnIndex = cursor.getColumnIndex("workout_id");
                    if (workoutIdColumnIndex != -1) {
                        contentValues.put(DatabaseHelper.WORKOUT_ID, cursor.getString(workoutIdColumnIndex));
                    }

                    int set1ColumnIndex = cursor.getColumnIndex("set1");
                    if (set1ColumnIndex != -1) {
                        contentValues.put(DatabaseHelper.SET1, cursor.getString(set1ColumnIndex));
                    }

                    int set2ColumnIndex = cursor.getColumnIndex("set2");
                    if (set2ColumnIndex != -1) {
                        contentValues.put(DatabaseHelper.SET2, cursor.getString(set2ColumnIndex));
                    }

                    int set3ColumnIndex = cursor.getColumnIndex("set3");
                    if (set3ColumnIndex != -1) {
                        contentValues.put(DatabaseHelper.SET3, cursor.getString(set3ColumnIndex));
                    }

                    int set4ColumnIndex = cursor.getColumnIndex("set4");
                    if (set4ColumnIndex != -1) {
                        contentValues.put(DatabaseHelper.SET4, cursor.getString(set4ColumnIndex));
                    }

                    int set5ColumnIndex = cursor.getColumnIndex("set5");
                    if (set5ColumnIndex != -1) {
                        contentValues.put(DatabaseHelper.SET5, cursor.getString(set5ColumnIndex));
                    }

                    int weightColumnIndex = cursor.getColumnIndex("weight");
                    if (weightColumnIndex != -1) {
                        contentValues.put(DatabaseHelper.WEIGHT, cursor.getDouble(weightColumnIndex));
                    }


                    //Is used to put the current datetime into the LOGS table datetime field
                    Date datetime = Calendar.getInstance().getTime();
                    contentValues.put(DatabaseHelper.DATETIME, datetime.toString());

                    //Is used to put the current date into the LOGS table date field
                    //We had to record the date by itself separate from the datetime to make querying the database easier for some of the calendar queries
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String date = sdf.format(new Date());
                    contentValues.put(DatabaseHelper.DATE, date);

                    database.insert(DatabaseHelper.TABLE_NAME_LOGS, null, contentValues);

                    // Clear the contentValues for the next iteration
                    contentValues.clear();

                } while (cursor.moveToPrevious());
            }
            cursor.close();

        }
    }


    public Cursor fetchActiveWorkouts() {
        String[] columns = new String[] { DatabaseHelper.WORKOUT_ID, DatabaseHelper.WORKOUT};


        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_WORKOUTS, columns, "WORKOUTS.ARCHIVE = 0", null, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor fetchArchivedWorkouts() {
        String[] columns = new String[] { DatabaseHelper.WORKOUT_ID, DatabaseHelper.WORKOUT};

        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_WORKOUTS, columns, "WORKOUTS.ARCHIVE = 1", null, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }


    public String countExercises(String id){

        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_EXERCISES, null, "EXERCISES.WORKOUT_ID = ?", new String[]{id}, null, null, null);
        int numOfExercises = cursor.getCount();

        //Our query needs the value as a String so we convert it here
        return Integer.toString(numOfExercises);
    }


    public String getExerciseId(String name){

        String exerciseId = "";
        String[] columns = new String[] {"EXERCISES.EXERCISE_ID"};
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_EXERCISES, columns, "EXERCISES.EXERCISE = ?", new String[]{name}, null, null, null);

        cursor.moveToFirst();
        exerciseId = cursor.getString(0);
        return exerciseId;
    }


    public Cursor getAllExercises(){
        String[] columns = new String[] {
                DatabaseHelper.EXERCISE_ID,
                DatabaseHelper.EXERCISE,
                DatabaseHelper.WEIGHT
        };
        
        // This query joins the EXERCISES table with the LOGS table to get the most recent weight for each exercise
        String query = "SELECT e." + DatabaseHelper.EXERCISE_ID + ", e." + DatabaseHelper.EXERCISE + 
                ", l." + DatabaseHelper.WEIGHT + 
                " FROM " + DatabaseHelper.TABLE_NAME_EXERCISES + " e " +
                " JOIN (SELECT " + DatabaseHelper.EXERCISE_ID + ", MAX(" + DatabaseHelper.DATETIME + ") as max_datetime " +
                "FROM " + DatabaseHelper.TABLE_NAME_LOGS + " GROUP BY " + DatabaseHelper.EXERCISE_ID + ") latest " +
                "ON e." + DatabaseHelper.EXERCISE_ID + " = latest." + DatabaseHelper.EXERCISE_ID + 
                " JOIN " + DatabaseHelper.TABLE_NAME_LOGS + " l " +
                "ON latest.max_datetime = l." + DatabaseHelper.DATETIME + 
                " AND e." + DatabaseHelper.EXERCISE_ID + " = l." + DatabaseHelper.EXERCISE_ID;
                
        return database.rawQuery(query, null);
    }


    public Cursor getExerciseLogProgress(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            // Return an empty cursor if no IDs are provided
            return database.rawQuery("SELECT weight, date FROM " + DatabaseHelper.TABLE_NAME_LOGS + " WHERE 0", null);
        }

        StringBuilder selectionBuilder = new StringBuilder(DatabaseHelper.EXERCISE_ID + " IN (");
        for (int i = 0; i < ids.size(); i++) {
            selectionBuilder.append("?");
            if (i < ids.size() - 1) {
                selectionBuilder.append(", ");
            }
        }
        selectionBuilder.append(")");
        String selection = selectionBuilder.toString();

        String[] selectionArgs = ids.toArray(new String[0]);

        // Fix: Specify columns as separate strings in the array, not as a single string
        String[] columns = new String[] {DatabaseHelper.WEIGHT, DatabaseHelper.DATE};
        
        try {
            return database.query(
                DatabaseHelper.TABLE_NAME_LOGS, 
                columns, 
                selection, 
                selectionArgs, 
                null, 
                null, 
                DatabaseHelper.DATE + " ASC"  // Order by date ascending
            );
        } catch (Exception e) {
            Log.e("DBManager", "Error in getExerciseLogProgress: " + e.getMessage());
            // Return an empty cursor in case of error
            return database.rawQuery("SELECT weight, date FROM " + DatabaseHelper.TABLE_NAME_LOGS + " WHERE 0", null);
        }
    }


    public Cursor fetchExerciseLogs(String id, String numOfExercises) {

        //Uses a prepared statement to help protect against SQL injection attacks
        //Read more here --> http://www.informit.com/articles/article.aspx?p=2268753&seqNum=5

        String[] columns = new String[] { "EXERCISES.WORKOUT_ID", "LOGS.EXERCISE_ID", DatabaseHelper.LOG_ID, DatabaseHelper.EXERCISE, "MAX(datetime)", DatabaseHelper.SET1, DatabaseHelper.SET1_IMPROVEMENT, DatabaseHelper.SET2, DatabaseHelper.SET2_IMPROVEMENT, DatabaseHelper.SET3, DatabaseHelper.SET3_IMPROVEMENT, DatabaseHelper.SET4, DatabaseHelper.SET4_IMPROVEMENT, DatabaseHelper.SET5, DatabaseHelper.SET5_IMPROVEMENT, DatabaseHelper.WEIGHT};

        Cursor cursor = database.query( true,DatabaseHelper.TABLE_NAME_LOGS + " LEFT OUTER JOIN " + DatabaseHelper.TABLE_NAME_EXERCISES + " ON " + "LOGS.EXERCISE_ID" + "=" + "EXERCISES.EXERCISE_ID", columns, "LOGS.WORKOUT_ID = ?", new String[]{id}, "LOGS.LOG_ID", null, "LOGS.LOG_ID DESC", numOfExercises);
        //Cursor cursor2 = database.rawQuery()

        // Cursor cursor = database.query( true,DatabaseHelper.TABLE_NAME_LOGS + " LEFT OUTER JOIN " + DatabaseHelper.TABLE_NAME_EXERCISES + " ON " + "LOGS.EXERCISE_ID" + "=" + "EXERCISES.EXERCISE_ID", columns, "EXERCISES.WORKOUT_ID = ?" + " AND " + "LOGS.DATE=(SELECT MAX(date) FROM LOGS)", new String[]{id}, "LOGS.EXERCISE_ID", null, DatabaseHelper.LOG_ID, null);

        cursor.moveToFirst();
        return cursor;
    }


    public Cursor fetchExerciseLogsForSelectedDate(String id, String date) {

        //Uses a prepared statement to help protect against SQL injection attacks
        //Read more here --> http://www.informit.com/articles/article.aspx?p=2268753&seqNum=5

        String[] columns = new String[] { "EXERCISES.WORKOUT_ID", "LOGS.EXERCISE_ID", DatabaseHelper.LOG_ID, DatabaseHelper.EXERCISE, "MAX(datetime)", DatabaseHelper.SET1, DatabaseHelper.SET1_IMPROVEMENT, DatabaseHelper.SET2, DatabaseHelper.SET2_IMPROVEMENT, DatabaseHelper.SET3, DatabaseHelper.SET3_IMPROVEMENT, DatabaseHelper.SET4, DatabaseHelper.SET4_IMPROVEMENT, DatabaseHelper.SET5, DatabaseHelper.SET5_IMPROVEMENT, DatabaseHelper.WEIGHT};
        // Cursor cursor = database.query( DatabaseHelper.TABLE_NAME_EXERCISES + " LEFT OUTER JOIN " + DatabaseHelper.TABLE_NAME_LOGS + " ON " + "EXERCISES.EXERCISE_ID" + "=" + "LOGS.EXERCISE_ID", columns, "EXERCISES.WORKOUT_ID = ?", new String[]{id}, "LOGS.EXERCISE_ID", null, null);

        Cursor cursor = database.query( true,DatabaseHelper.TABLE_NAME_LOGS + " LEFT OUTER JOIN " + DatabaseHelper.TABLE_NAME_EXERCISES + " ON " + "LOGS.EXERCISE_ID" + "=" + "EXERCISES.EXERCISE_ID", columns, "EXERCISES.WORKOUT_ID = ?" + " AND " + "LOGS.DATE = ?", new String[]{id, date}, "LOGS.EXERCISE_ID", null, DatabaseHelper.LOG_ID, null);

        cursor.moveToFirst();
        return cursor;
    }


    public Cursor fetchAllExerciseLogsForCalendar() {
        String[] columns = new String[] { DatabaseHelper.WORKOUT_ID, DatabaseHelper.DATE};
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_LOGS, columns, "LOGS.DURATION IS NOT NULL", null, DatabaseHelper.WORKOUT_ID + "," + DatabaseHelper.DATE, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    //The logs table only gives us the workout ID
    public Cursor fetchWorkoutsOnSelectedDateForCalendar(String strDate) {
        String[] columns = new String[] { DatabaseHelper.WORKOUT_ID, DatabaseHelper.DATE};
        Cursor cursor = database.query(true, DatabaseHelper.TABLE_NAME_LOGS, columns, "LOGS.DATETIME LIKE ? AND LOGS.DURATION IS NOT NULL", new String[]{(strDate)}, DatabaseHelper.WORKOUT_ID, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }


    //We then need to call this cursor to query the workouts table based on the ID given
    public Cursor fetchWorkoutNameOnSelectedDateForCalendar(String workout_id) {
        String[] columns = new String[] { DatabaseHelper.WORKOUT};
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_WORKOUTS, columns, "WORKOUTS.WORKOUT_ID = ?", new String[]{(workout_id)}, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }


    public void updateWorkout(long _id, String workoutName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.WORKOUT, workoutName);
        int i = database.update(DatabaseHelper.TABLE_NAME_WORKOUTS, contentValues, DatabaseHelper.WORKOUT_ID + " = " + _id, null);
    }


    public void updateExerciseName(long _id, String exerciseName) {
        // Check if this ID exists directly in the exercises table first
        Cursor exerciseCursor = database.query(
            DatabaseHelper.TABLE_NAME_EXERCISES,
            new String[]{DatabaseHelper.EXERCISE_ID},
            DatabaseHelper.EXERCISE_ID + " = ?",
            new String[]{Long.toString(_id)},
            null, null, null
        );
        
        String exerciseId = "";
        boolean isExerciseId = false;
        
        // If we found it in exercises table, use it directly
        if (exerciseCursor.moveToFirst()) {
            exerciseId = Long.toString(_id);
            isExerciseId = true;
            exerciseCursor.close();
        } else {
            exerciseCursor.close();
        }
        
        // If it's not an exercise ID, look it up from logs
        if (!isExerciseId) {
            String[] columns = new String[] {DatabaseHelper.LOG_ID, DatabaseHelper.EXERCISE_ID};
            Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_LOGS, columns, "LOGS.LOG_ID = ?", new String[]{Long.toString(_id)}, null, null, null, null);

            if (cursor.moveToFirst()) {
                int exerciseIdColumnIndex = cursor.getColumnIndex("exercise_id");
                if (exerciseIdColumnIndex != -1) {
                    exerciseId = cursor.getString(exerciseIdColumnIndex);
                }
                cursor.close();
            }
        }

        // Only proceed if we found a valid exercise ID
        if (!exerciseId.isEmpty()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.EXERCISE, exerciseName);

            //Updates the Exercise Name using proper parameterized query
            int i = database.update(
                DatabaseHelper.TABLE_NAME_EXERCISES, 
                contentValues, 
                DatabaseHelper.EXERCISE_ID + " = ?", 
                new String[]{exerciseId}
            );
            
            // Log the update for debugging
            Log.d("DBManager", "Updated exercise name. Rows affected: " + i);
        } else {
            Log.e("DBManager", "Failed to find exercise ID for update");
        }
    }

    public void updateExerciseWeight(long _id, Double exerciseWeight) {
        // First check if this is an exercise ID directly in the exercises table
        Cursor exerciseCursor = database.query(
            DatabaseHelper.TABLE_NAME_EXERCISES,
            new String[]{DatabaseHelper.EXERCISE_ID},
            DatabaseHelper.EXERCISE_ID + " = ?",
            new String[]{Long.toString(_id)},
            null, null, null
        );
        
        boolean isExerciseId = false;
        long exerciseId = -1;
        
        // If found in exercises table, use it directly
        if (exerciseCursor.moveToFirst()) {
            exerciseId = _id;
            isExerciseId = true;
            exerciseCursor.close();
        } else {
            exerciseCursor.close();
        }
        
        // If not an exercise ID, try to find it from logs
        if (!isExerciseId) {
            // Get today's date in the format "YYYY-MM-DD"
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            // Prepare the SQL query to fetch the exercise ID associated with the provided log ID
            String[] projection = { DatabaseHelper.EXERCISE_ID };
            String selection = DatabaseHelper.LOG_ID + " = ?";
            String[] selectionArgs = { String.valueOf(_id) };

            // Execute the query to fetch the exercise ID
            Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_LOGS, projection, selection, selectionArgs, null, null, null);

            if (cursor.moveToFirst()) {
                int exerciseIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.EXERCISE_ID);
                if (exerciseIdColumnIndex != -1) {
                    exerciseId = cursor.getLong(exerciseIdColumnIndex);
                }
                cursor.close();
            }
        }
        
        // Only proceed if we have a valid exercise ID
        if (exerciseId != -1) {
            // Get today's date for filtering
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            
            // Update any log entries for today with this exercise ID
            String selectionUpdate = "LOGS.EXERCISE_ID = ? AND strftime('%Y-%m-%d', LOGS.DATE) = ?";
            String[] selectionArgsUpdate = { String.valueOf(exerciseId), todayDate };
            
            ContentValues logValues = new ContentValues();
            logValues.put(DatabaseHelper.WEIGHT, exerciseWeight);
            
            int logsUpdated = database.update(DatabaseHelper.TABLE_NAME_LOGS, logValues, selectionUpdate, selectionArgsUpdate);
            
            Log.d("DBManager", "Updated exercise weight. Logs table rows: " + logsUpdated);
        } else {
            Log.e("DBManager", "Failed to find exercise ID for weight update");
        }
    }



    public void updateExerciseLogs(long log_id, String setSelected, Integer intReps){
        ContentValues contentValues = new ContentValues();
        contentValues.put(setSelected, intReps);
        int i = database.update(DatabaseHelper.TABLE_NAME_LOGS, contentValues, DatabaseHelper.LOG_ID + " = " + log_id, null);
    }


    public void updateExerciseLogsWithImprovement(long log_id, String setSelected, Integer intReps, Integer intImprovement){
        try {
            ContentValues contentValues = new ContentValues();
            String setImprovement = setSelected + "_improvement";
    
            contentValues.put(setSelected, intReps);
            contentValues.put(setImprovement, intImprovement);
            
            int rowsUpdated = database.update(DatabaseHelper.TABLE_NAME_LOGS, contentValues, 
                                          DatabaseHelper.LOG_ID + " = " + log_id, null);
            
            if (rowsUpdated > 0) {
                Log.d("DBManager", "Successfully updated exercise log: " + log_id + ", set: " + setSelected + 
                      ", reps: " + intReps + ", improvement: " + intImprovement);
            } else {
                Log.w("DBManager", "No rows updated for log_id: " + log_id + ". Possible invalid ID.");
            }
        } catch (Exception e) {
            Log.e("DBManager", "Error updating exercise log: " + e.getMessage());
        }
    }


    /**
     * Records the duration of an exercise log
     * @param log_id The ID of the log to update
     * @param workoutDuration The duration in seconds
     */
    public void recordExerciseLogDuration(String log_id, long workoutDuration) {
        try {
            // Make sure log_id is valid
            if (log_id == null || log_id.trim().isEmpty()) {
                Log.e("DBManager", "Invalid log_id provided: null or empty");
                return;
            }
            
            // First, attempt direct update by log_id
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.DURATION, workoutDuration);
            
            int rowsUpdated = database.update(
                DatabaseHelper.TABLE_NAME_LOGS, 
                contentValues, 
                DatabaseHelper.LOG_ID + " = ?", 
                new String[]{log_id}
            );
            
            if (rowsUpdated > 0) {
                Log.d("DBManager", "Successfully updated duration for log_id: " + log_id);
                return; // Exit if update was successful
            }
            
            // If direct update failed, try to find the most recent log for this exercise
            // First, get the exercise_id from the log_id
            String exerciseId = null;
            Cursor logCursor = database.query(
                DatabaseHelper.TABLE_NAME_LOGS,
                new String[]{DatabaseHelper.EXERCISE_ID},
                DatabaseHelper.LOG_ID + " = ?",
                new String[]{log_id},
                null, null, null
            );
            
            if (logCursor.moveToFirst()) {
                int exerciseIdColumnIndex = logCursor.getColumnIndex(DatabaseHelper.EXERCISE_ID);
                if (exerciseIdColumnIndex != -1) {
                    exerciseId = logCursor.getString(exerciseIdColumnIndex);
                }
                logCursor.close();
            }
            
            // If we found an exercise_id, try to update the most recent log for that exercise
            if (exerciseId != null) {
                // Get the most recent log for this exercise
                Cursor recentLogCursor = database.query(
                    DatabaseHelper.TABLE_NAME_LOGS,
                    new String[]{DatabaseHelper.LOG_ID},
                    DatabaseHelper.EXERCISE_ID + " = ?",
                    new String[]{exerciseId},
                    null, null,
                    DatabaseHelper.DATETIME + " DESC",
                    "1" // Limit to most recent
                );
                
                if (recentLogCursor.moveToFirst()) {
                    int recentLogIdColumnIndex = recentLogCursor.getColumnIndex(DatabaseHelper.LOG_ID);
                    if (recentLogIdColumnIndex != -1) {
                        String recentLogId = recentLogCursor.getString(recentLogIdColumnIndex);
                        
                        // Try to update this log
                        int recentRowsUpdated = database.update(
                            DatabaseHelper.TABLE_NAME_LOGS, 
                            contentValues, 
                            DatabaseHelper.LOG_ID + " = ?", 
                            new String[]{recentLogId}
                        );
                        
                        if (recentRowsUpdated > 0) {
                            Log.d("DBManager", "Updated most recent log instead. Original log_id: " + log_id + ", Updated log_id: " + recentLogId);
                            recentLogCursor.close();
                            return;
                        }
                    }
                    recentLogCursor.close();
                }
            }
            
            // If we still haven't updated anything, log the failure
            Log.w("DBManager", "No rows updated for log_id: " + log_id + ". Unable to find suitable log to update.");
            
        } catch (Exception e) {
            Log.e("DBManager", "Error updating exercise log duration: " + e.getMessage());
        }
    }


    public void archiveWorkout(Long workout_id){
        ContentValues contentValues = new ContentValues();
        contentValues.put("archive", 1);

        database.update(DatabaseHelper.TABLE_NAME_WORKOUTS, contentValues, DatabaseHelper.WORKOUT_ID + " = " + workout_id, null);
    }


    public void unarchiveWorkout(Long workout_id){
        ContentValues contentValues = new ContentValues();
        contentValues.put("archive", 0);

        database.update(DatabaseHelper.TABLE_NAME_WORKOUTS, contentValues, DatabaseHelper.WORKOUT_ID + " = " + workout_id, null);
    }


    public void deleteWorkout(long _id) {
        // Count the associated exercises and logs
        int associatedExerciseCount = countAssociatedExercises(_id);
        int associatedLogCount = countAssociatedLogs(_id);

        // Check if there are associated exercises or logs
        if (associatedExerciseCount > 0 || associatedLogCount > 0) {
            String message = "This workout has " + associatedExerciseCount + " associated exercise(s) and " +
                    associatedLogCount + " associated log(s). Are you sure you want to delete it and its associated data?";
            
            showStyledConfirmationDialog(message, (dialogInterface, i) -> {
                // User confirmed, delete the workout, its exercises, and logs
                try {
                    database.beginTransaction();
                    database.delete(DatabaseHelper.TABLE_NAME_LOGS, DatabaseHelper.WORKOUT_ID + "=?", new String[]{String.valueOf(_id)});
                    database.delete(DatabaseHelper.TABLE_NAME_EXERCISES, DatabaseHelper.WORKOUT_ID + "=?", new String[]{String.valueOf(_id)});
                    database.delete(DatabaseHelper.TABLE_NAME_WORKOUTS, DatabaseHelper.WORKOUT_ID + "=?", new String[]{String.valueOf(_id)});
                    database.setTransactionSuccessful();
                    
                    Toast.makeText(context, "Workout deleted successfully", Toast.LENGTH_SHORT).show();
                    
                } catch (Exception e) {
                    Log.e("DBManager", "Error deleting workout: " + e.getMessage());
                    Toast.makeText(context, "Error deleting workout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    database.endTransaction();
                }

                ((Activity) context).recreate();
            });
        } else {
            // If no associations, delete directly
            database.delete(DatabaseHelper.TABLE_NAME_WORKOUTS, DatabaseHelper.WORKOUT_ID + "=?", new String[]{String.valueOf(_id)});
            Toast.makeText(context, "Workout deleted successfully", Toast.LENGTH_SHORT).show();
            ((Activity) context).recreate();
        }
    }


    public void deleteExercise(long _id) {
        // Check if this is an exercise ID directly in the exercises table
        Cursor exerciseCursor = database.query(
                DatabaseHelper.TABLE_NAME_EXERCISES,
                new String[]{DatabaseHelper.EXERCISE_ID},
                DatabaseHelper.EXERCISE_ID + " = ?",
                new String[]{Long.toString(_id)},
                null, null, null
        );

        boolean isExerciseId = false;
        List<String> exerciseIds = new ArrayList<>();

        // If found in exercises table, use it directly
        if (exerciseCursor.moveToFirst()) {
            exerciseIds.add(Long.toString(_id));
            isExerciseId = true;
            exerciseCursor.close();
        } else {
            exerciseCursor.close();
        }

        // If not an exercise ID, try to find it from logs table
        if (!isExerciseId) {
            // Directly query the exercise ID from the selected log ID
            String[] columns = new String[]{DatabaseHelper.EXERCISE_ID};
            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_NAME_LOGS,
                    columns,
                    DatabaseHelper.LOG_ID + " = ?",
                    new String[]{String.valueOf(_id)},
                    null,
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                do {
                    int exerciseIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.EXERCISE_ID);
                    if (exerciseIdColumnIndex != -1) {
                        exerciseIds.add(cursor.getString(exerciseIdColumnIndex));
                    }
                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        // Only proceed if we found exercise IDs
        if (!exerciseIds.isEmpty()) {
            // Create the confirmation dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete Exercise");
            builder.setMessage("Are you sure you want to delete this exercise and its associated log(s)?");
            builder.setPositiveButton("Yes", (dialog1, which) -> {
                try {
                    // Delete logs associated with each exercise ID
                    for (String exerciseId : exerciseIds) {
                        Log.d("DBManager", "Deleting exercise ID: " + exerciseId);
                        database.delete(DatabaseHelper.TABLE_NAME_LOGS, DatabaseHelper.EXERCISE_ID + "=?", new String[]{exerciseId});
                        database.delete(DatabaseHelper.TABLE_NAME_EXERCISES, DatabaseHelper.EXERCISE_ID + "=?", new String[]{exerciseId});
                    }

                    // Show a success message
                    Toast.makeText(context, "Exercise deleted successfully", Toast.LENGTH_SHORT).show();

                    // Refresh the activity to renew the page
                    ((Activity) context).recreate();

                } catch (Exception e) {
                    // Log an error if an exception occurs during deletion
                    Log.e("DeleteExercise", "Error deleting exercise and logs.", e);
                    Toast.makeText(context, "Error deleting exercise: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("No", (dialog1, which) -> {
                // Do nothing
            });

            // Create the AlertDialog
            AlertDialog confirmationDialog = builder.create();
            // Set the custom background
            confirmationDialog.setOnShowListener(dialogInterface -> {
                Objects.requireNonNull(confirmationDialog.getWindow()).setBackgroundDrawableResource(R.drawable.modern_dialog_background);
            });
            // Show the dialog
            confirmationDialog.show();
        } else {
            // No exercise IDs found - show an error
            Toast.makeText(context, "Could not find exercise to delete", Toast.LENGTH_SHORT).show();
            Log.e("DBManager", "No exercise IDs found for deletion with ID: " + _id);
        }
    }



    //Checking exercise duplicates
    public boolean doesExerciseExist(String exerciseName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Query the database to see if the exercise name exists
            String[] projection = {DatabaseHelper.EXERCISE_ID}; // Assuming you have an exercise ID column
            String selection = DatabaseHelper.EXERCISE + " = ?";
            String[] selectionArgs = {exerciseName};

            cursor = db.query(DatabaseHelper.TABLE_NAME_EXERCISES, projection, selection, selectionArgs, null, null, null);

            return cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    public boolean isDuplicateWorkout(String workoutName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_NAME_WORKOUTS + " WHERE " +
                DatabaseHelper.WORKOUT + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{workoutName});
        int count = 0;
        cursor.moveToFirst();
        count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }


    public int countAssociatedExercises(long workoutId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;

        try {
            // Define the columns to be returned in the query
            String[] projection = {DatabaseHelper.EXERCISE_ID};

            // Define the selection criteria to find exercises associated with the specified workout ID
            String selection = DatabaseHelper.WORKOUT_ID + " = ?";
            String[] selectionArgs = { String.valueOf(workoutId) };

            // Execute the query to count associated exercises
            cursor = db.query(DatabaseHelper.TABLE_NAME_EXERCISES, projection, selection, selectionArgs, null, null, null);

            // Count the rows returned by the query
            count = cursor.getCount();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }


    public int countAssociatedLogs(long workoutId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Query the database to count logs associated with the given workout ID
            String[] projection = {DatabaseHelper.LOG_ID};
            String selection = DatabaseHelper.WORKOUT_ID + " = ?";
            String[] selectionArgs = {String.valueOf(workoutId)};

            cursor = db.query(DatabaseHelper.TABLE_NAME_LOGS, projection, selection, selectionArgs, null, null, null);

            return cursor.getCount();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    public double getMostRecentWeightForExercise(String exerciseName) {
    double mostRecentWeight = 0.0;
    SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {DatabaseHelper.WEIGHT};
        String selection = DatabaseHelper.EXERCISE + " = ?";
        String[] selectionArgs = {exerciseName};
        String sortOrder = DatabaseHelper.DATETIME + " DESC";

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_NAME_LOGS + " JOIN " + DatabaseHelper.TABLE_NAME_EXERCISES +
                        " ON " + DatabaseHelper.TABLE_NAME_LOGS + "." + DatabaseHelper.EXERCISE_ID +
                        " = " + DatabaseHelper.TABLE_NAME_EXERCISES + "." + DatabaseHelper.EXERCISE_ID,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        if (cursor.moveToFirst()) {
            int weightColumnIndex = cursor.getColumnIndex(DatabaseHelper.WEIGHT);
            if (weightColumnIndex != -1) {
                mostRecentWeight = cursor.getDouble(weightColumnIndex);
            }
        }

        cursor.close();
        return mostRecentWeight;
}

    public void insertExerciseDirectly(String exerciseName, Double exerciseWeight) {
        // Create a dummy workout ID for compatibility
        String dummyWorkoutId = "1";
        
        // Check if our dummy workout exists, if not create it
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_WORKOUTS, 
                new String[]{DatabaseHelper.WORKOUT_ID}, 
                DatabaseHelper.WORKOUT_ID + "=?", 
                new String[]{dummyWorkoutId}, null, null, null);
                
        if (cursor.getCount() == 0) {
            // Create dummy workout
            ContentValues workoutValue = new ContentValues();
            workoutValue.put(DatabaseHelper.WORKOUT_ID, dummyWorkoutId);
            workoutValue.put(DatabaseHelper.WORKOUT, "Default");
            workoutValue.put(DatabaseHelper.ARCHIVE, 0);
            database.insert(DatabaseHelper.TABLE_NAME_WORKOUTS, null, workoutValue);
        }

        cursor.close();

        // Now insert the exercise
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.WORKOUT_ID, dummyWorkoutId);
        contentValue.put(DatabaseHelper.EXERCISE, exerciseName);
        long exerciseId = database.insert(DatabaseHelper.TABLE_NAME_EXERCISES, null, contentValue);

        // Insert initial log entry
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(DatabaseHelper.EXERCISE_ID, exerciseId);
        contentValues2.put(DatabaseHelper.WORKOUT_ID, dummyWorkoutId);
        contentValues2.put(DatabaseHelper.SET1, 5);
        contentValues2.put(DatabaseHelper.SET2, 5);
        contentValues2.put(DatabaseHelper.SET3, 5);
        contentValues2.put(DatabaseHelper.SET4, 5);
        contentValues2.put(DatabaseHelper.SET5, 5);
        contentValues2.put(DatabaseHelper.WEIGHT, exerciseWeight);

        //Is used to put the current datetime into the LOGS table datetime field
        Date datetime = Calendar.getInstance().getTime();
        contentValues2.put(DatabaseHelper.DATETIME, datetime.toString());

        //Is used to put the current date into the LOGS table date field
        //We had to record the date by itself separate from the datetime to make querying the database easier for some of the calendar queries
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date());
        contentValues2.put(DatabaseHelper.DATE, date);

        database.insert(DatabaseHelper.TABLE_NAME_LOGS, null, contentValues2);
    }
    
    public void startSelectedExercises(List<String> exerciseIds) {
        // Create a dummy workout ID for compatibility
        String dummyWorkoutId = "1";
        
        ContentValues contentValues = new ContentValues();
        
        for (String exerciseId : exerciseIds) {
            contentValues.put(DatabaseHelper.EXERCISE_ID, exerciseId);
            contentValues.put(DatabaseHelper.WORKOUT_ID, dummyWorkoutId);
            contentValues.put(DatabaseHelper.SET1, 5);
            contentValues.put(DatabaseHelper.SET2, 5);
            contentValues.put(DatabaseHelper.SET3, 5);
            contentValues.put(DatabaseHelper.SET4, 5);
            contentValues.put(DatabaseHelper.SET5, 5);
            
            // Get the most recent weight for this exercise
            Cursor cursor = database.rawQuery(
                "SELECT " + DatabaseHelper.WEIGHT + 
                " FROM " + DatabaseHelper.TABLE_NAME_LOGS + 
                " WHERE " + DatabaseHelper.EXERCISE_ID + "=?" +
                " ORDER BY " + DatabaseHelper.DATETIME + " DESC LIMIT 1", 
                new String[]{exerciseId});
                
            double weight = 0.0;
            if (cursor.moveToFirst()) {
                int weightColumnIndex = cursor.getColumnIndex(DatabaseHelper.WEIGHT);
                if (weightColumnIndex != -1) {
                    weight = cursor.getDouble(weightColumnIndex);
                }
                cursor.close();
            }
            
            contentValues.put(DatabaseHelper.WEIGHT, weight);

            //Is used to put the current datetime into the LOGS table datetime field
            Date datetime = Calendar.getInstance().getTime();
            contentValues.put(DatabaseHelper.DATETIME, datetime.toString());

            //Is used to put the current date into the LOGS table date field
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String date = sdf.format(new Date());
            contentValues.put(DatabaseHelper.DATE, date);

            database.insert(DatabaseHelper.TABLE_NAME_LOGS, null, contentValues);
        }
    }

    public Cursor getExerciseDetails(String exerciseId) {
        String query = "SELECT e." + DatabaseHelper.EXERCISE_ID + ", e." + DatabaseHelper.EXERCISE + 
                ", l." + DatabaseHelper.WEIGHT + ", l." + DatabaseHelper.LOG_ID +
                " FROM " + DatabaseHelper.TABLE_NAME_EXERCISES + " e " +
                " JOIN (SELECT " + DatabaseHelper.EXERCISE_ID + ", MAX(" + DatabaseHelper.DATETIME + ") as max_datetime " +
                "FROM " + DatabaseHelper.TABLE_NAME_LOGS + 
                " WHERE " + DatabaseHelper.EXERCISE_ID + "=? " +
                "GROUP BY " + DatabaseHelper.EXERCISE_ID + ") latest " +
                "ON e." + DatabaseHelper.EXERCISE_ID + " = latest." + DatabaseHelper.EXERCISE_ID + 
                " JOIN " + DatabaseHelper.TABLE_NAME_LOGS + " l " +
                "ON latest.max_datetime = l." + DatabaseHelper.DATETIME + 
                " AND e." + DatabaseHelper.EXERCISE_ID + " = l." + DatabaseHelper.EXERCISE_ID + 
                " WHERE e." + DatabaseHelper.EXERCISE_ID + "=?";
                
        return database.rawQuery(query, new String[] {exerciseId, exerciseId});
    }

    /**
     * Updates a single exercise set (button click data)
     * @param exerciseId The ID of the exercise
     * @param setSelected Which set was selected (SET1, SET2, etc.)
     * @param intReps The number of reps performed
     */
    public void updateExerciseSet(String exerciseId, String setSelected, Integer intReps) {
        try {
            long longId = Long.parseLong(exerciseId);
            updateExerciseLogs(longId, setSelected, intReps);
        } catch (NumberFormatException e) {
            Log.e("DBManager", "Error parsing exercise ID: " + e.getMessage());
        }
    }
    
    /**
     * Updates both the name and weight of an exercise
     * @param exerciseId The ID of the exercise
     * @param exerciseName The new name of the exercise
     * @param exerciseWeight The new weight for the exercise
     */
    public void updateExercise(String exerciseId, String exerciseName, Double exerciseWeight) {
        try {
            long longId = Long.parseLong(exerciseId);
            
            // Update the exercise name
            updateExerciseName(longId, exerciseName);
            
            // Update the exercise weight if provided
            if (exerciseWeight != null) {
                updateExerciseWeight(longId, exerciseWeight);
            }
        } catch (NumberFormatException e) {
            Log.e("DBManager", "Error parsing exercise ID: " + e.getMessage());
        }
    }

    /**
     * Deletes an exercise by its ID (String version for convenience)
     * @param exerciseId The ID of the exercise to delete as a String
     */
    public void deleteExercise(String exerciseId) {
        try {
            long longId = Long.parseLong(exerciseId);
            deleteExercise(longId);
        } catch (NumberFormatException e) {
            Log.e("DBManager", "Error parsing exercise ID for deletion: " + e.getMessage());
            Toast.makeText(context, "Error: Invalid exercise ID format", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Fetches all archived exercises from the database
     * @return Cursor containing archived exercises
     */
    public Cursor fetchArchivedExercises() {
        try {
            // Use the ARCHIVE column in the EXERCISES table directly
            String query = "SELECT " + 
                "e." + DatabaseHelper.EXERCISE_ID + " AS " + DatabaseHelper.EXERCISE_ID + ", " + 
                "e." + DatabaseHelper.EXERCISE + " AS " + DatabaseHelper.EXERCISE + ", " + 
                "l." + DatabaseHelper.WEIGHT + " AS " + DatabaseHelper.WEIGHT + " " +
                "FROM " + DatabaseHelper.TABLE_NAME_EXERCISES + " e " +
                "LEFT JOIN " + DatabaseHelper.TABLE_NAME_LOGS + " l ON " +
                "e." + DatabaseHelper.EXERCISE_ID + " = l." + DatabaseHelper.EXERCISE_ID + " " +
                "WHERE e." + DatabaseHelper.ARCHIVE + " = 1 " +
                "GROUP BY e." + DatabaseHelper.EXERCISE_ID;
            
            return database.rawQuery(query, null);
        } catch (Exception e) {
            Log.e("DBManager", "Error fetching archived exercises: " + e.getMessage());
            // Return an empty cursor in case of error
            return database.rawQuery("SELECT 0 AS _id, '' AS exercise, 0 AS weight WHERE 0", null);
        }
    }

    /**
     * Checks if the database is open
     * @return true if the database is open, false otherwise
     */
    public boolean isOpen() {
        return database != null && database.isOpen();
    }

    /**
     * Archives an exercise by updating its archive status directly
     * @param exerciseId The ID of the exercise to archive
     */
    public void archiveExercise(String exerciseId) {
        try {
            // Update the exercise's archive status directly
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.ARCHIVE, 1);
            
            database.update(
                DatabaseHelper.TABLE_NAME_EXERCISES, 
                contentValues, 
                DatabaseHelper.EXERCISE_ID + "=?", 
                new String[]{exerciseId}
            );
            
            Log.d("DBManager", "Exercise archived: " + exerciseId);
        } catch (Exception e) {
            Log.e("DBManager", "Error archiving exercise: " + e.getMessage());
        }
    }
    
    /**
     * Unarchives an exercise by updating its archive status directly
     * @param exerciseId The ID of the exercise to unarchive
     */
    public void unarchiveExercise(String exerciseId) {
        try {
            // Update the exercise's archive status directly
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.ARCHIVE, 0);
            
            database.update(
                DatabaseHelper.TABLE_NAME_EXERCISES, 
                contentValues, 
                DatabaseHelper.EXERCISE_ID + "=?", 
                new String[]{exerciseId}
            );
            
            Log.d("DBManager", "Exercise unarchived: " + exerciseId);
        } catch (Exception e) {
            Log.e("DBManager", "Error unarchiving exercise: " + e.getMessage());
        }
    }

    /**
     * Fetches all unarchived exercises
     * @return Cursor containing unarchived exercises
     */
    public Cursor fetchUnarchivedExercises() {
        try {
            // Use the ARCHIVE column in the EXERCISES table directly
            String query = "SELECT " + 
                "e." + DatabaseHelper.EXERCISE_ID + " AS " + DatabaseHelper.EXERCISE_ID + ", " + 
                "e." + DatabaseHelper.EXERCISE + " AS " + DatabaseHelper.EXERCISE + ", " + 
                "l." + DatabaseHelper.WEIGHT + " AS " + DatabaseHelper.WEIGHT + " " +
                "FROM " + DatabaseHelper.TABLE_NAME_EXERCISES + " e " +
                "LEFT JOIN " + DatabaseHelper.TABLE_NAME_LOGS + " l ON " +
                "e." + DatabaseHelper.EXERCISE_ID + " = l." + DatabaseHelper.EXERCISE_ID + " " +
                "WHERE e." + DatabaseHelper.ARCHIVE + " = 0 OR e." + DatabaseHelper.ARCHIVE + " IS NULL " +
                "GROUP BY e." + DatabaseHelper.EXERCISE_ID;
            
            return database.rawQuery(query, null);
        } catch (Exception e) {
            Log.e("DBManager", "Error fetching unarchived exercises: " + e.getMessage());
            // Return an empty cursor in case of error
            return database.rawQuery("SELECT 0 AS _id, '' AS exercise, 0 AS weight WHERE 0", null);
        }
    }

    /**
     * Fetches the latest exercise logs for today
     * @param exerciseIds List of exercise IDs to fetch logs for
     * @return Cursor with the log data
     */
    public Cursor fetchExerciseLogsForToday(List<String> exerciseIds) {
        if (exerciseIds == null || exerciseIds.isEmpty()) {
            return null;
        }
        
        // Get today's date in the format "YYYY-MM-DD"
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        // Build the SQL placeholders for the IN clause
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < exerciseIds.size(); i++) {
            placeholders.append("?");
            if (i < exerciseIds.size() - 1) {
                placeholders.append(",");
            }
        }
        
        // Build the query
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_NAME_LOGS +
                       " WHERE " + DatabaseHelper.EXERCISE_ID + " IN (" + placeholders.toString() + ")" +
                       " AND " + DatabaseHelper.DATE + " = ?" +
                       " ORDER BY " + DatabaseHelper.DATETIME + " DESC";
        
        // Convert the list to an array and add the date as the last parameter
        String[] selectionArgs = new String[exerciseIds.size() + 1];
        for (int i = 0; i < exerciseIds.size(); i++) {
            selectionArgs[i] = exerciseIds.get(i);
        }
        selectionArgs[exerciseIds.size()] = todayDate;
        
        return database.rawQuery(query, selectionArgs);
    }

    /**
     * Fetches detailed exercise information for a specific date
     * @param strDate The date in the format used in the DATETIME column
     * @return Cursor with exercise details, showing only the latest entry for each exercise
     */
    public Cursor fetchExerciseDetailsForDate(String strDate) {
        // This query selects only the most recent completed exercise log for each unique exercise on a given date
        String query = "SELECT l." + DatabaseHelper.LOG_ID + ", " +
                "e." + DatabaseHelper.EXERCISE_ID + ", " +
                "e." + DatabaseHelper.EXERCISE + ", " +
                "l." + DatabaseHelper.WEIGHT + ", " +
                "l." + DatabaseHelper.DATE + ", " +
                "l." + DatabaseHelper.DATETIME + ", " +
                "l." + DatabaseHelper.DURATION + ", " +
                "l." + DatabaseHelper.SET1 + ", " +
                "l." + DatabaseHelper.SET2 + ", " +
                "l." + DatabaseHelper.SET3 + ", " +
                "l." + DatabaseHelper.SET4 + ", " +
                "l." + DatabaseHelper.SET5 + ", " +
                "l." + DatabaseHelper.SET1_IMPROVEMENT + ", " +
                "l." + DatabaseHelper.SET2_IMPROVEMENT + ", " +
                "l." + DatabaseHelper.SET3_IMPROVEMENT + ", " +
                "l." + DatabaseHelper.SET4_IMPROVEMENT + ", " +
                "l." + DatabaseHelper.SET5_IMPROVEMENT + " " +
                "FROM " + DatabaseHelper.TABLE_NAME_LOGS + " l " +
                "JOIN " + DatabaseHelper.TABLE_NAME_EXERCISES + " e ON l." + DatabaseHelper.EXERCISE_ID + " = e." + DatabaseHelper.EXERCISE_ID + " " +
                "INNER JOIN ( " +
                "   SELECT " + DatabaseHelper.EXERCISE_ID + ", MAX(" + DatabaseHelper.DATETIME + ") as max_datetime " +
                "   FROM " + DatabaseHelper.TABLE_NAME_LOGS + " " +
                "   WHERE " + DatabaseHelper.DATETIME + " LIKE ? " +
                "   AND " + DatabaseHelper.DURATION + " IS NOT NULL " +
                "   GROUP BY " + DatabaseHelper.EXERCISE_ID +
                ") latest ON l." + DatabaseHelper.EXERCISE_ID + " = latest." + DatabaseHelper.EXERCISE_ID + " " +
                "AND l." + DatabaseHelper.DATETIME + " = latest.max_datetime " +
                "ORDER BY l." + DatabaseHelper.DATETIME + " DESC";
                
        return database.rawQuery(query, new String[]{strDate});
    }

    /**
     * Checks if dark mode is enabled
     * @param context The context to check
     * @return true if dark mode is enabled, false otherwise
     */
    private boolean isDarkModeEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("dark_mode", false);
    }

    /**
     * Creates a styled confirmation dialog with consistent colors
     *
     * @param message        The dialog message
     * @param positiveAction Action to perform on positive button click
     */
    private void showStyledConfirmationDialog(String message, DialogInterface.OnClickListener positiveAction) {
        // Create the AlertDialog with explicit context and style
        Context dialogContext = new ContextThemeWrapper(context, 
            isDarkModeEnabled(context) ? R.style.ModernAlertDialogDark : R.style.ModernAlertDialog);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(dialogContext);
        builder.setTitle("Confirm Deletion");
        builder.setMessage(message);

        // Set up buttons
        builder.setPositiveButton("Yes", positiveAction);
        builder.setNegativeButton("No", (dialogInterface, i) -> {
            // User canceled, do nothing
        });

        // Show the dialog with improved styling
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Manually set button colors for better visibility after dialog is shown
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        
        if (positiveButton != null) {
            positiveButton.setTextColor(ContextCompat.getColor(context, R.color.colorError));
            positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            positiveButton.setTypeface(positiveButton.getTypeface(), Typeface.BOLD);
        }
        
        if (negativeButton != null) {
            negativeButton.setTextColor(ContextCompat.getColor(context, R.color.colorInfo));
            negativeButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        }

    }

    /**
     * Get the latest exercise log entry with improvements for a specific exercise
     * @param exerciseId The ID of the exercise to get logs for
     * @return Cursor containing the latest log with set reps and improvements
     */
    public Cursor getLatestLogForExercise(String exerciseId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_NAME_LOGS +
                      " WHERE " + DatabaseHelper.EXERCISE_ID + " = ?" +
                      " ORDER BY " + DatabaseHelper.DATETIME + " DESC, " +
                      DatabaseHelper.LOG_ID + " DESC LIMIT 1";
        
        return db.rawQuery(query, new String[]{exerciseId});
    }

}