package com.developerjp.jieunworkouttracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
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
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
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
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetchArchivedWorkouts() {
        String[] columns = new String[] { DatabaseHelper.WORKOUT_ID, DatabaseHelper.WORKOUT};

        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_WORKOUTS, columns, "WORKOUTS.ARCHIVE = 1", null, null, null, null);
        if (cursor != null ) {
            cursor.moveToFirst();
        }
        return cursor;
    }


    public String countExercises(String id){

        @SuppressLint("Recycle") Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_EXERCISES, null, "EXERCISES.WORKOUT_ID = ?", new String[]{id}, null, null, null);
        int numOfExercises = cursor.getCount();

        //Our query needs the value as a String so we convert it here
        return Integer.toString(numOfExercises);
    }


    public String getExerciseId(String name){

        String exerciseId = "";
        String[] columns = new String[] {"EXERCISES.EXERCISE_ID"};
        @SuppressLint("Recycle") Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_EXERCISES, columns, "EXERCISES.EXERCISE = ?", new String[]{name}, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            exerciseId = cursor.getString(0);
        }
        return exerciseId;
    }


    public Cursor getAllExercises(){

        String[] columns = new String[] {"EXERCISES.EXERCISE_ID, EXERCISES.EXERCISE"};
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_EXERCISES, columns, null, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;

    }


    public Cursor getExerciseLogProgress(List<String> ids) {
        StringBuilder selectionBuilder = new StringBuilder("LOGS.EXERCISE_ID IN (");
        for (int i = 0; i < ids.size(); i++) {
            selectionBuilder.append("?");
            if (i < ids.size() - 1) {
                selectionBuilder.append(", ");
            }
        }
        selectionBuilder.append(")");
        String selection = selectionBuilder.toString();

        String[] selectionArgs = ids.toArray(new String[0]);

        String[] columns = new String[] {"LOGS.WEIGHT, LOGS.DATE"};
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_LOGS, columns, selection, selectionArgs, null, null, null);

        return cursor;
    }


//        public Cursor getExerciseLogProgress(String id){
//
//        String[] columns = new String[] {"LOGS.WEIGHT, LOGS.DATE"};
//        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_LOGS, columns, "LOGS.EXERCISE_ID = ?", new String[] {id}, null, null, null);
//
//
//        if (cursor != null) {
//            cursor.moveToFirst();
//        }
//        return cursor;
//
//    }


    public Cursor fetchExerciseLogs(String id, String numOfExercises) {

        //Uses a prepared statement to help protect against SQL injection attacks
        //Read more here --> http://www.informit.com/articles/article.aspx?p=2268753&seqNum=5

        String[] columns = new String[] { "EXERCISES.WORKOUT_ID", "LOGS.EXERCISE_ID", DatabaseHelper.LOG_ID, DatabaseHelper.EXERCISE, "MAX(datetime)", DatabaseHelper.SET1, DatabaseHelper.SET1_IMPROVEMENT, DatabaseHelper.SET2, DatabaseHelper.SET2_IMPROVEMENT, DatabaseHelper.SET3, DatabaseHelper.SET3_IMPROVEMENT, DatabaseHelper.SET4, DatabaseHelper.SET4_IMPROVEMENT, DatabaseHelper.SET5, DatabaseHelper.SET5_IMPROVEMENT, DatabaseHelper.WEIGHT};

        Cursor cursor = database.query( true,DatabaseHelper.TABLE_NAME_LOGS + " LEFT OUTER JOIN " + DatabaseHelper.TABLE_NAME_EXERCISES + " ON " + "LOGS.EXERCISE_ID" + "=" + "EXERCISES.EXERCISE_ID", columns, "LOGS.WORKOUT_ID = ?", new String[]{id}, "LOGS.LOG_ID", null, "LOGS.LOG_ID DESC", numOfExercises);
        //Cursor cursor2 = database.rawQuery()

        // Cursor cursor = database.query( true,DatabaseHelper.TABLE_NAME_LOGS + " LEFT OUTER JOIN " + DatabaseHelper.TABLE_NAME_EXERCISES + " ON " + "LOGS.EXERCISE_ID" + "=" + "EXERCISES.EXERCISE_ID", columns, "EXERCISES.WORKOUT_ID = ?" + " AND " + "LOGS.DATE=(SELECT MAX(date) FROM LOGS)", new String[]{id}, "LOGS.EXERCISE_ID", null, DatabaseHelper.LOG_ID, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }


    public Cursor fetchExerciseLogsForSelectedDate(String id, String date) {

        //Uses a prepared statement to help protect against SQL injection attacks
        //Read more here --> http://www.informit.com/articles/article.aspx?p=2268753&seqNum=5

        String[] columns = new String[] { "EXERCISES.WORKOUT_ID", "LOGS.EXERCISE_ID", DatabaseHelper.LOG_ID, DatabaseHelper.EXERCISE, "MAX(datetime)", DatabaseHelper.SET1, DatabaseHelper.SET1_IMPROVEMENT, DatabaseHelper.SET2, DatabaseHelper.SET2_IMPROVEMENT, DatabaseHelper.SET3, DatabaseHelper.SET3_IMPROVEMENT, DatabaseHelper.SET4, DatabaseHelper.SET4_IMPROVEMENT, DatabaseHelper.SET5, DatabaseHelper.SET5_IMPROVEMENT, DatabaseHelper.WEIGHT};
        // Cursor cursor = database.query( DatabaseHelper.TABLE_NAME_EXERCISES + " LEFT OUTER JOIN " + DatabaseHelper.TABLE_NAME_LOGS + " ON " + "EXERCISES.EXERCISE_ID" + "=" + "LOGS.EXERCISE_ID", columns, "EXERCISES.WORKOUT_ID = ?", new String[]{id}, "LOGS.EXERCISE_ID", null, null);

        Cursor cursor = database.query( true,DatabaseHelper.TABLE_NAME_LOGS + " LEFT OUTER JOIN " + DatabaseHelper.TABLE_NAME_EXERCISES + " ON " + "LOGS.EXERCISE_ID" + "=" + "EXERCISES.EXERCISE_ID", columns, "EXERCISES.WORKOUT_ID = ?" + " AND " + "LOGS.DATE = ?", new String[]{id, date}, "LOGS.EXERCISE_ID", null, DatabaseHelper.LOG_ID, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }


    public Cursor fetchAllExerciseLogsForCalendar() {
        String[] columns = new String[] { DatabaseHelper.WORKOUT_ID, DatabaseHelper.DATE};
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_LOGS, columns, "LOGS.DURATION IS NOT NULL", null, DatabaseHelper.WORKOUT_ID + "," + DatabaseHelper.DATE, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    //The logs table only gives us the workout ID
    public Cursor fetchWorkoutsOnSelectedDateForCalendar(String strDate) {
        String[] columns = new String[] { DatabaseHelper.WORKOUT_ID, DatabaseHelper.DATE};
        Cursor cursor = database.query(true, DatabaseHelper.TABLE_NAME_LOGS, columns, "LOGS.DATETIME LIKE ? AND LOGS.DURATION IS NOT NULL", new String[]{(strDate)}, DatabaseHelper.WORKOUT_ID, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }


    //We then need to call this cursor to query the workouts table based on the ID given
    public Cursor fetchWorkoutNameOnSelectedDateForCalendar(String workout_id) {
        String[] columns = new String[] { DatabaseHelper.WORKOUT};
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_WORKOUTS, columns, "WORKOUTS.WORKOUT_ID = ?", new String[]{(workout_id)}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }


    public void updateWorkout(long _id, String workoutName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.WORKOUT, workoutName);
        int i = database.update(DatabaseHelper.TABLE_NAME_WORKOUTS, contentValues, DatabaseHelper.WORKOUT_ID + " = " + _id, null);
    }


    public void updateExerciseName(long _id, String exerciseName) {
        //For exercises it is passing across the log id when a list item is long selected.
        //We First work out which exercise correlates to the log id selected and then we update that exercise name
        String[] columns = new String[] {DatabaseHelper.LOG_ID, DatabaseHelper.EXERCISE_ID};
        String exerciseId = "";
        Cursor cursor = database.query( DatabaseHelper.TABLE_NAME_LOGS, columns, "LOGS.LOG_ID = ?", new String[]{Long.toString(_id)}, null, null, null, null);


        if (cursor.moveToFirst()) {
            int exerciseIdColumnIndex = cursor.getColumnIndex("exercise_id");
            if (exerciseIdColumnIndex != -1) {
                exerciseId = cursor.getString(exerciseIdColumnIndex);
            }
        }
        cursor.close();


        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.EXERCISE, exerciseName);

        //Updates the Exercise Name
        int i = database.update(DatabaseHelper.TABLE_NAME_EXERCISES, contentValues, DatabaseHelper.EXERCISE_ID + " = " + exerciseId, null);
    }

    public void updateExerciseWeight(long _id, Double exerciseWeight) {
        // Get today's date in the format "YYYY-MM-DD"
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Prepare the SQL query to fetch the exercise ID associated with the provided log ID
        String[] projection = { DatabaseHelper.EXERCISE_ID };
        String selection = DatabaseHelper.LOG_ID + " = ?";
        String[] selectionArgs = { String.valueOf(_id) };

        // Execute the query to fetch the exercise ID
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_LOGS, projection, selection, selectionArgs, null, null, null);
        long exerciseId = -1; // Default value

        if (cursor != null && cursor.moveToFirst()) {
            int exerciseIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.EXERCISE_ID);
            if (exerciseIdColumnIndex != -1) {
                exerciseId = cursor.getLong(exerciseIdColumnIndex);
            }
            cursor.close();
        }

        // Prepare the selection and selection arguments to filter by exercise ID and today's date
        String selectionUpdate = "LOGS.EXERCISE_ID = ? AND strftime('%Y-%m-%d', LOGS.DATE) = ?";
        String[] selectionArgsUpdate = { String.valueOf(exerciseId), todayDate };

        // Prepare the content values to update the exercise weight
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.WEIGHT, exerciseWeight);

        // Update the Exercise Weight for today's date and the exercise associated with the provided log ID
        int i = database.update(DatabaseHelper.TABLE_NAME_LOGS, contentValues, selectionUpdate, selectionArgsUpdate);
    }


//    public void updateExerciseWeight(long _id, Double exerciseWeight) {
//
//        //For exercises it is passing across the log id when a list item is long selected.
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(DatabaseHelper.WEIGHT, exerciseWeight);
//
//        //Updates the Exercise Weight
//        int i = database.update(DatabaseHelper.TABLE_NAME_LOGS, contentValues, "LOGS.LOG_ID = ?", new String[]{Long.toString(_id)});
//    }


    public void updateExerciseLogs(long log_id, String setSelected, Integer intReps){
        ContentValues contentValues = new ContentValues();
        contentValues.put(setSelected, intReps);
        int i = database.update(DatabaseHelper.TABLE_NAME_LOGS, contentValues, DatabaseHelper.LOG_ID + " = " + log_id, null);
    }


    public void updateExerciseLogsWithImprovement(long log_id, String setSelected, Integer intReps, Integer intImprovement){
        ContentValues contentValues = new ContentValues();
        String setImprovement = setSelected + "_improvement";

        contentValues.put(setSelected, intReps);
        contentValues.put(setImprovement, intImprovement);
        int i = database.update(DatabaseHelper.TABLE_NAME_LOGS, contentValues, DatabaseHelper.LOG_ID + " = " + log_id, null);
    }


    public void recordExerciseLogDuration(String log_id, long workoutDuration){

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.DURATION, workoutDuration);
        int i = database.update(DatabaseHelper.TABLE_NAME_LOGS, contentValues, DatabaseHelper.LOG_ID + " = " + log_id, null);
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
            // If there are associated exercises or logs, ask for confirmation
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Confirm Deletion");

            String message = "This workout has " + associatedExerciseCount + " associated exercise(s) and " +
                    associatedLogCount + " associated log(s). Are you sure you want to delete it and its associated data?";

            builder.setMessage(message);

            builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                // User confirmed, delete the workout, its exercises, and logs
                try {
                    database.beginTransaction();
                    database.delete(DatabaseHelper.TABLE_NAME_LOGS, DatabaseHelper.WORKOUT_ID + "=?", new String[]{String.valueOf(_id)});
                    database.delete(DatabaseHelper.TABLE_NAME_EXERCISES, DatabaseHelper.WORKOUT_ID + "=?", new String[]{String.valueOf(_id)});
                    database.delete(DatabaseHelper.TABLE_NAME_WORKOUTS, DatabaseHelper.WORKOUT_ID + "=?", new String[]{String.valueOf(_id)});
                    database.setTransactionSuccessful();
                    ((Activity) context).recreate();

                } catch (Exception e) {
                    // Log the exception
                    Log.e("DeleteWorkout", "Error deleting workout with ID " + _id, e);
                } finally {
                    database.endTransaction();
                }
            });

            builder.setNegativeButton("No", (dialogInterface, i) -> {
                // User canceled the deletion, do nothing
            });

            builder.show();

        } else {
            // If there are no associated exercises or logs, directly delete the workout
            try {
                database.delete(DatabaseHelper.TABLE_NAME_WORKOUTS, DatabaseHelper.WORKOUT_ID + "=?", new String[]{String.valueOf(_id)});

            } catch (Exception e) {
                // Log the exception
                Log.e("DeleteWorkout", "Error deleting workout with ID " + _id, e);
            }
        }
    }


    public void deleteExercise(long _id) {
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

        List<String> exerciseIds = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                int exerciseIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.EXERCISE_ID);
                if (exerciseIdColumnIndex != -1) {
                    exerciseIds.add(cursor.getString(exerciseIdColumnIndex));
                }
            } while (cursor.moveToNext());
        }

        cursor.close();

        // Confirm with the user before deleting
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to delete this exercise and its associated log(s)?");

        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            try {
                // Delete logs associated with each exercise ID
                for (String exerciseId : exerciseIds) {
                    database.delete(DatabaseHelper.TABLE_NAME_EXERCISES, DatabaseHelper.EXERCISE_ID + "=?", new String[]{exerciseId});
                    database.delete(DatabaseHelper.TABLE_NAME_LOGS, DatabaseHelper.EXERCISE_ID + "=?", new String[]{exerciseId});
                }
                // Refresh the activity to renew the page
                ((Activity) context).recreate();

            } catch (Exception e) {
                // Log an error if an exception occurs during deletion
                Log.e("DeleteExercise", "Error deleting exercise and logs.", e);
            }
        });

        builder.setNegativeButton("No", (dialogInterface, i) -> {
            // User canceled the deletion, do nothing
        });

        builder.show();
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
        if (cursor != null) {
            cursor.moveToFirst();
            count = cursor.getInt(0);
            cursor.close();
        }
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

}