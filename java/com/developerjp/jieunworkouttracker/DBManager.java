package com.developerjp.jieunworkouttracker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DBManager {

    private final Context context;
    SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public DBManager(Context c) {
        context = c;
    }

    public void open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Cursor getAllExercises() {
        String[] columns = new String[]{
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
        String[] columns = new String[]{DatabaseHelper.WEIGHT, DatabaseHelper.DATE};

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

    public Cursor fetchAllExerciseLogsForCalendar() {
        // Query to get all unique dates that have completed exercises
        String query = "SELECT DISTINCT " + DatabaseHelper.DATE +
                " FROM " + DatabaseHelper.TABLE_NAME_LOGS +
                " WHERE " + DatabaseHelper.DURATION + " IS NOT NULL" +
                " ORDER BY " + DatabaseHelper.DATE + " DESC";

        Log.d("DBManager", "Fetching calendar events with query: " + query);
        return database.rawQuery(query, null);
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
            String[] columns = new String[]{DatabaseHelper.LOG_ID, DatabaseHelper.EXERCISE_ID};
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

    /**
     * Updates the weight of an exercise in the database
     *
     * @param exerciseId The ID of the exercise to update
     * @param weight     The weight value to set (in kg)
     */
    public void updateExerciseWeight(String exerciseId, double weight) {
        // Format to one decimal place
        DecimalFormat df = new DecimalFormat("#.#");
        weight = Double.parseDouble(df.format(weight));

        // First, we need to get the current workout_id for this exercise
        // Query the most recent log entry for this exercise to get its workout_id
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_NAME_LOGS,
                new String[]{DatabaseHelper.WORKOUT_ID},
                DatabaseHelper.EXERCISE_ID + " = ?",
                new String[]{exerciseId},
                null,
                null,
                DatabaseHelper.DATETIME + " DESC",
                "1"
        );

        long workoutId;
        if (cursor.moveToFirst()) {
            workoutId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.WORKOUT_ID));
            cursor.close();
        } else {

            cursor.close();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date());

        // Create a new log entry with the current date/time
        ContentValues logValues = new ContentValues();
        logValues.put(DatabaseHelper.EXERCISE_ID, exerciseId);
        logValues.put(DatabaseHelper.WEIGHT, weight);
        logValues.put(DatabaseHelper.DATE, date);
        logValues.put(DatabaseHelper.DATETIME, String.valueOf(Calendar.getInstance().getTime()));
        logValues.put(DatabaseHelper.WORKOUT_ID, workoutId);

        // Insert the new log entry
        long newRowId = database.insert(
                DatabaseHelper.TABLE_NAME_LOGS,
                null,
                logValues
        );

    }

    public void updateExerciseLogs(long log_id, String setSelected, Integer intReps) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(setSelected, intReps);
        int i = database.update(DatabaseHelper.TABLE_NAME_LOGS, contentValues, DatabaseHelper.LOG_ID + " = " + log_id, null);
    }

    public void updateExerciseLogsWithImprovement(long log_id, String setSelected, Integer intReps, Integer intImprovement) {
        try {
            ContentValues contentValues = new ContentValues();
            String setImprovement = setSelected + "_improvement";

            Log.d("DBManager", "Updating log_id: " + log_id +
                    ", set: " + setSelected +
                    ", reps: " + intReps +
                    ", improvement: " + intImprovement);

            // First check if the log_id exists
            Cursor checkCursor = database.query(
                    DatabaseHelper.TABLE_NAME_LOGS,
                    new String[]{DatabaseHelper.LOG_ID},
                    DatabaseHelper.LOG_ID + " = ?",
                    new String[]{String.valueOf(log_id)},
                    null, null, null
            );

            if (checkCursor.moveToFirst()) {
                Log.d("DBManager", "Found log_id " + log_id + " in the database");
                checkCursor.close();
            } else {
                checkCursor.close();
                Log.e("DBManager", "log_id " + log_id + " NOT found in the database!");

                // Try to get the most recent log for this exercise instead
                Cursor exerciseCursor = database.query(
                        DatabaseHelper.TABLE_NAME_LOGS,
                        new String[]{DatabaseHelper.EXERCISE_ID},
                        DatabaseHelper.LOG_ID + " = ?",
                        new String[]{String.valueOf(log_id)},
                        null, null, null
                );

                if (exerciseCursor.moveToFirst()) {
                    int exerciseIdIndex = exerciseCursor.getColumnIndex(DatabaseHelper.EXERCISE_ID);
                    if (exerciseIdIndex != -1) {
                        String exerciseId = exerciseCursor.getString(exerciseIdIndex);
                        exerciseCursor.close();

                        Log.d("DBManager", "Attempting to find most recent log for exercise_id: " + exerciseId);

                        // Get today's date
                        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                        // Query for the most recent log for this exercise today
                        Cursor recentCursor = database.rawQuery(
                                "SELECT " + DatabaseHelper.LOG_ID + " FROM " + DatabaseHelper.TABLE_NAME_LOGS +
                                        " WHERE " + DatabaseHelper.EXERCISE_ID + " = ? AND " +
                                        DatabaseHelper.DATE + " = ? ORDER BY " + DatabaseHelper.DATETIME +
                                        " DESC LIMIT 1",
                                new String[]{exerciseId, todayDate}
                        );

                        if (recentCursor.moveToFirst()) {
                            int logIdIndex = recentCursor.getColumnIndex(DatabaseHelper.LOG_ID);
                            if (logIdIndex != -1) {
                                long newLogId = recentCursor.getLong(logIdIndex);
                                recentCursor.close();

                                Log.d("DBManager", "Found most recent log_id: " + newLogId + " for exercise_id: " + exerciseId);
                                log_id = newLogId;
                            } else {
                                recentCursor.close();
                            }
                        } else {
                            recentCursor.close();
                            Log.e("DBManager", "Could not find a recent log for exercise_id: " + exerciseId);
                            return;
                        }
                    } else {
                        exerciseCursor.close();
                        Log.e("DBManager", "EXERCISE_ID column not found in cursor");
                        return;
                    }
                } else {
                    exerciseCursor.close();
                    Log.e("DBManager", "Could not find the exercise ID for log_id: " + log_id);
                    return;
                }
            }

            contentValues.put(setSelected, intReps);
            contentValues.put(setImprovement, intImprovement);

            int rowsUpdated = database.update(DatabaseHelper.TABLE_NAME_LOGS, contentValues,
                    DatabaseHelper.LOG_ID + " = " + log_id, null);

            if (rowsUpdated > 0) {
                Log.d("DBManager", "Successfully updated exercise log: " + log_id + ", set: " + setSelected +
                        ", reps: " + intReps + ", improvement: " + intImprovement);
            } else {
                Log.w("DBManager", "No rows updated for log_id: " + log_id + ". Possible invalid ID.");

                // Dump table schema and contents for debugging
                Cursor schemaCursor = database.rawQuery("PRAGMA table_info(" + DatabaseHelper.TABLE_NAME_LOGS + ")", null);
                Log.d("DBManager", "LOGS table schema:");
                while (schemaCursor.moveToNext()) {
                    String colName = schemaCursor.getString(1);
                    String colType = schemaCursor.getString(2);
                    Log.d("DBManager", "Column: " + colName + ", Type: " + colType);
                }
                schemaCursor.close();

                // Sample a few rows
                Cursor debugCursor = database.query(
                        DatabaseHelper.TABLE_NAME_LOGS,
                        null,
                        null,
                        null,
                        null,
                        null,
                        DatabaseHelper.LOG_ID + " DESC",
                        "5"
                );

                Log.d("DBManager", "Sample LOGS table rows:");
                String row;
                int logIdIndex = debugCursor.getColumnIndex(DatabaseHelper.LOG_ID);
                int exerciseIdIndex = debugCursor.getColumnIndex(DatabaseHelper.EXERCISE_ID);

                if (logIdIndex != -1 && exerciseIdIndex != -1) {
                    row = "LOG_ID: " + debugCursor.getLong(logIdIndex) +
                            ", EXERCISE_ID: " + debugCursor.getLong(exerciseIdIndex);
                } else {
                    row = "Error: Required columns not found in cursor";
                }
                Log.d("DBManager", row);
                debugCursor.close();
            }
        } catch (Exception e) {
            Log.e("DBManager", "Error updating exercise log: " + e.getMessage(), e);
        }
    }

    public void recordExerciseLogDuration(String log_id, long workoutDuration) {
        try {
            // Make sure log_id is valid
            if (log_id == null || log_id.trim().isEmpty()) {
                Log.e("DBManager", "Invalid log_id provided: null or empty");
                return;
            }

            // Update only the specific log entry
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
            } else {
                Log.w("DBManager", "No rows updated for log_id: " + log_id + ". Log entry not found.");
            }

        } catch (Exception e) {
            Log.e("DBManager", "Error updating exercise log duration: " + e.getMessage());
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
            AlertDialog.Builder builder = getBuilder(exerciseIds);
            builder.setNegativeButton("No", (dialog1, which) -> {
                // Do nothing
            });

            // Create the AlertDialog
            AlertDialog confirmationDialog = builder.create();
            // Set the custom background
            confirmationDialog.setOnShowListener(dialogInterface -> Objects.requireNonNull(confirmationDialog.getWindow()).setBackgroundDrawableResource(R.drawable.modern_dialog_background));
            // Show the dialog
            confirmationDialog.show();
        } else {
            // No exercise IDs found - show an error
            Toast.makeText(context, "Could not find exercise to delete", Toast.LENGTH_SHORT).show();
            Log.e("DBManager", "No exercise IDs found for deletion with ID: " + _id);
        }
    }

    private AlertDialog.Builder getBuilder(List<String> exerciseIds) {
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
        return builder;
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
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        String query = "SELECT e." + DatabaseHelper.EXERCISE_ID + ", e." + DatabaseHelper.EXERCISE +
                ", l." + DatabaseHelper.WEIGHT + ", l." + DatabaseHelper.LOG_ID +
                ", l." + DatabaseHelper.SET1 + ", l." + DatabaseHelper.SET2 +
                ", l." + DatabaseHelper.SET3 + ", l." + DatabaseHelper.SET4 +
                ", l." + DatabaseHelper.SET5 + ", l." + DatabaseHelper.SET1_IMPROVEMENT +
                ", l." + DatabaseHelper.SET2_IMPROVEMENT + ", l." + DatabaseHelper.SET3_IMPROVEMENT +
                ", l." + DatabaseHelper.SET4_IMPROVEMENT + ", l." + DatabaseHelper.SET5_IMPROVEMENT +
                " FROM " + DatabaseHelper.TABLE_NAME_EXERCISES + " e " +
                " JOIN " + DatabaseHelper.TABLE_NAME_LOGS + " l " +
                " ON e." + DatabaseHelper.EXERCISE_ID + " = l." + DatabaseHelper.EXERCISE_ID +
                " WHERE e." + DatabaseHelper.EXERCISE_ID + "=? " +
                " AND l." + DatabaseHelper.DATE + "=? " +
                " ORDER BY l." + DatabaseHelper.DATETIME + " DESC LIMIT 1";

        Log.d("DBManager", "Fetching exercise details for exercise_id: " + exerciseId + " on date: " + todayDate);
        Cursor cursor = database.rawQuery(query, new String[]{exerciseId, todayDate});

        if (cursor.moveToFirst()) {
            int logIdIndex = cursor.getColumnIndex(DatabaseHelper.LOG_ID);
            if (logIdIndex != -1) {
                String logId = cursor.getString(logIdIndex);
                Log.d("DBManager", "Found log_id: " + logId + " for exercise_id: " + exerciseId);
            } else {
                Log.w("DBManager", "LOG_ID column not found in cursor");
            }
        } else {
            Log.w("DBManager", "No logs found for exercise_id: " + exerciseId + " on date: " + todayDate);
        }

        return cursor;
    }

    /**
     * Updates a single exercise set (button click data)
     *
     * @param exerciseId  The ID of the exercise
     * @param setSelected Which set was selected (SET1, SET2, etc.)
     * @param intReps     The number of reps performed
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
     *
     * @param exerciseId     The ID of the exercise
     * @param exerciseName   The new name of the exercise
     * @param exerciseWeight The new weight for the exercise
     */
    public void updateExercise(String exerciseId, String exerciseName, Double exerciseWeight) {
        try {
            long longId = Long.parseLong(exerciseId);

            // Update the exercise name
            updateExerciseName(longId, exerciseName);

            // Update the exercise weight if provided
            if (exerciseWeight != null) {
                updateExerciseWeight(exerciseId, exerciseWeight);
            }
        } catch (NumberFormatException e) {
            Log.e("DBManager", "Error parsing exercise ID: " + e.getMessage());
        }
    }

    /**
     * Deletes an exercise by its ID (String version for convenience)
     *
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
     *
     * @return Cursor containing archived exercises
     */
    public Cursor fetchArchivedExercises() {
        try {
            // Use the ARCHIVE column in the EXERCISES table directly
            String query = "SELECT " +
                    "e." + DatabaseHelper.EXERCISE_ID + " AS " + DatabaseHelper.EXERCISE_ID + ", " +
                    "e." + DatabaseHelper.EXERCISE + " AS " + DatabaseHelper.EXERCISE + ", " +
                    "latestLog." + DatabaseHelper.WEIGHT + " AS " + DatabaseHelper.WEIGHT + " " +
                    "FROM " + DatabaseHelper.TABLE_NAME_EXERCISES + " e " +
                    "LEFT JOIN (SELECT " + DatabaseHelper.EXERCISE_ID + ", " + DatabaseHelper.WEIGHT + ", " +
                    "MAX(" + DatabaseHelper.LOG_ID + ") AS max_log_id " +
                    "FROM " + DatabaseHelper.TABLE_NAME_LOGS + " " +
                    "GROUP BY " + DatabaseHelper.EXERCISE_ID + ") latestLog " +
                    "ON e." + DatabaseHelper.EXERCISE_ID + " = latestLog." + DatabaseHelper.EXERCISE_ID + " " +
                    "WHERE e." + DatabaseHelper.ARCHIVE + " = 1 " +
                    "ORDER BY e." + DatabaseHelper.EXERCISE;

            return database.rawQuery(query, null);
        } catch (Exception e) {
            Log.e("DBManager", "Error fetching archived exercises: " + e.getMessage());
            // Return an empty cursor in case of error
            return database.rawQuery("SELECT 0 AS _id, '' AS exercise, 0 AS weight WHERE 0", null);
        }
    }

    /**
     * Checks if the database is open
     *
     * @return true if the database is open, false otherwise
     */
    public boolean isOpen() {
        return database == null || !database.isOpen();
    }

    /**
     * Archives an exercise by updating its archive status directly
     *
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
     *
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
     *
     * @return Cursor containing unarchived exercises
     */
    public Cursor fetchUnarchivedExercises() {
        try {
            // This query uses a subquery to get the latest log entry (highest log_id) for each exercise
            String query = "SELECT " +
                    "e." + DatabaseHelper.EXERCISE_ID + " AS " + DatabaseHelper.EXERCISE_ID + ", " +
                    "e." + DatabaseHelper.EXERCISE + " AS " + DatabaseHelper.EXERCISE + ", " +
                    "latestLog." + DatabaseHelper.WEIGHT + " AS " + DatabaseHelper.WEIGHT + " " +
                    "FROM " + DatabaseHelper.TABLE_NAME_EXERCISES + " e " +
                    "LEFT JOIN (SELECT " + DatabaseHelper.EXERCISE_ID + ", " + DatabaseHelper.WEIGHT + ", " +
                    "MAX(" + DatabaseHelper.LOG_ID + ") AS max_log_id " +
                    "FROM " + DatabaseHelper.TABLE_NAME_LOGS + " " +
                    "GROUP BY " + DatabaseHelper.EXERCISE_ID + ") latestLog " +
                    "ON e." + DatabaseHelper.EXERCISE_ID + " = latestLog." + DatabaseHelper.EXERCISE_ID + " " +
                    "WHERE e." + DatabaseHelper.ARCHIVE + " = 0 OR e." + DatabaseHelper.ARCHIVE + " IS NULL " +
                    "ORDER BY e." + DatabaseHelper.EXERCISE;

            return database.rawQuery(query, null);
        } catch (Exception e) {
            Log.e("DBManager", "Error fetching unarchived exercises: " + e.getMessage());
            // Return an empty cursor in case of error
            return database.rawQuery("SELECT 0 AS _id, '' AS exercise, 0 AS weight WHERE 0", null);
        }
    }

    /**
     * Fetches detailed exercise information for a specific date
     *
     * @param strDate The date in the format used in the DATETIME column
     * @return Cursor with exercise details, showing only the latest entry for each exercise
     */
    public Cursor fetchExerciseDetailsForDate(String strDate) {
        Log.d("DBManager", "Fetching exercise details for date: " + strDate);

        // The date should already be in yyyy-MM-dd format from ShowCalendarActivity
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
                "l." + DatabaseHelper.SET5_IMPROVEMENT + ", " +
                "l." + DatabaseHelper.WORKOUT_ID + " " +
                "FROM " + DatabaseHelper.TABLE_NAME_LOGS + " l " +
                "JOIN " + DatabaseHelper.TABLE_NAME_EXERCISES + " e ON l." + DatabaseHelper.EXERCISE_ID + " = e." + DatabaseHelper.EXERCISE_ID + " " +
                "WHERE l." + DatabaseHelper.DATE + "=? " +
                " AND l." + DatabaseHelper.DURATION + " IS NOT NULL " +
                " ORDER BY l." + DatabaseHelper.DATETIME + " ASC";

        Log.d("DBManager", "Executing query: " + query + " with date: " + strDate);
        return database.rawQuery(query, new String[]{strDate});
    }

    /**
     * Fetches detailed exercise information for all log entries matching a specific date and exercise ID.
     *
     * @param strDate    The date to filter logs by (in a format compatible with your database storage).
     * @param exerciseId The ID of the exercise to filter logs by.
     * @return Cursor with exercise details for all matching log entries.
     */
    public Cursor fetchExerciseLogsForDateAndExercise(String strDate, String exerciseId) {
        Log.d("DBManager", "Fetching exercise logs for date: " + strDate + " and exercise ID: " + exerciseId);

        String query = "SELECT l.*, e.* " +
                "FROM " + DatabaseHelper.TABLE_NAME_LOGS + " l " +
                "JOIN " + DatabaseHelper.TABLE_NAME_EXERCISES + " e ON l." + DatabaseHelper.EXERCISE_ID + " = e." + DatabaseHelper.EXERCISE_ID + " " +
                "WHERE l." + DatabaseHelper.DATE + " = ? " +
                "AND l." + DatabaseHelper.EXERCISE_ID + " = ? " +
                "AND l." + DatabaseHelper.DURATION + " IS NOT NULL " +
                "ORDER BY l." + DatabaseHelper.DATETIME + " ASC";


        String[] params = new String[]{strDate, exerciseId}; // Use both date and exerciseId as parameters

        Log.d("DBManager", "Executing query: " + query + " with params: " + Arrays.toString(params));
        return database.rawQuery(query, params);
    }


    /**
     * Fetches detailed exercise information for a specific log entry.
     *
     * @param strDate The date to filter logs by (in a format compatible with your database storage).
     * @param logId   The ID of the log entry to fetch.
     * @return Cursor with exercise details for the specified log entry.
     */
    public Cursor fetchExerciseLogsForDateAndLog(String strDate, String logId) {
        Log.d("DBManager", "Fetching exercise log for date: " + strDate + " and log ID: " + logId);

        String query = "SELECT l.*, e.* " +
                "FROM " + DatabaseHelper.TABLE_NAME_LOGS + " l " +
                "JOIN " + DatabaseHelper.TABLE_NAME_EXERCISES + " e ON l." + DatabaseHelper.EXERCISE_ID + " = e." + DatabaseHelper.EXERCISE_ID + " " +
                "WHERE l." + DatabaseHelper.DATE + " = ? " +
                "AND l." + DatabaseHelper.LOG_ID + " = ? " +
                "AND l." + DatabaseHelper.DURATION + " IS NOT NULL " +
                "ORDER BY l." + DatabaseHelper.DATETIME + " ASC";

        String[] params = new String[]{strDate, logId};

        Log.d("DBManager", "Executing query: " + query + " with params: " + Arrays.toString(params));
        return database.rawQuery(query, params);
    }

}