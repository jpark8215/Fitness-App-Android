package com.example.jieunworkouttracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

    public void insertExercise(String id, String exerciseName, Double exerciseWeight) {
        ContentValues contentValue = new ContentValues();

        contentValue.put(DatabaseHelper.WORKOUT_ID, id);
        contentValue.put(DatabaseHelper.EXERCISE, exerciseName);
        long exerciseId =  database.insert(DatabaseHelper.TABLE_NAME_EXERCISES, null, contentValue);

        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(DatabaseHelper.EXERCISE_ID, exerciseId);
        contentValues2.put(DatabaseHelper.WORKOUT_ID, id);
        contentValues2.put(DatabaseHelper.SET1, 10);
        contentValues2.put(DatabaseHelper.SET2, 10);
        contentValues2.put(DatabaseHelper.SET3, 10);
        contentValues2.put(DatabaseHelper.SET4, 10);
        contentValues2.put(DatabaseHelper.SET5, 10);
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
        //  database.update(DatabaseHelper.TABLE_NAME_EXERCISES, contentValue, "EXERCISES.WORKOUT_ID = ?", new String[] {id});

    }

    //Is called when the user starts a workout.
    //Creates a new exercise log for them to track the workout.
    public void insertExerciseLogs(String id, String numOfExercises){

        ContentValues contentValues = new ContentValues();
        Cursor cursor = fetchExerciseLogs(id, numOfExercises);

        //fetchExerciseLogs returns the data in reverse order. So we start at the end of the cursor and work our way
        //backwards. This way the data appear is the correct order.
        for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {
            if (cursor != null && cursor.moveToFirst()) {
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
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }


    //Super hacky way of doing it this
    //Select MAX date time was not working correctly from fetchExerciseLogs()
    //But.. it did seem to work correctly when ordering by log_id DESC
    //So we are going to Count the amount of exercises in a workout.
    //Use that value in fetchExerciseLogs as a LIMIT value
    //This will then return the correct results we are after... but then everything is reversed
    //So we then have to flip it around to appear in the correct order
    //TODO - Make this better

    public String countExercises(String id){

        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_EXERCISES, null, "EXERCISES.WORKOUT_ID = ?", new String[]{id}, null, null, null);
        Integer numOfExercises = cursor.getCount();

        //Our query needs the value as a String so we convert it here
        String strNumOfExercises = numOfExercises.toString();
        return strNumOfExercises;
    }

    public String getExerciseId( String name){

        String exerciseId = "";
        String[] columns = new String[] {"EXERCISES.EXERCISE_ID"};
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_EXERCISES, columns, "EXERCISES.EXERCISE = ?", new String[]{name}, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            exerciseId = cursor.getString(0);
        }
        return exerciseId;
    }

    public Cursor getExerciseLogProgress(String id){

        String[] columns = new String[] {"LOGS.WEIGHT, LOGS.DATE"};
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_LOGS, columns, "LOGS.EXERCISE_ID = ?", new String[] {id}, null, null, null);


        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;

    }

    public Cursor getAllExercises(){

        String[] columns = new String[] {"EXERCISES.EXERCISE_ID, EXERCISES.EXERCISE"};
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME_EXERCISES, columns, null, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;

    }


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


    public int updateWorkout(long _id, String workoutName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.WORKOUT, workoutName);
        int i = database.update(DatabaseHelper.TABLE_NAME_WORKOUTS, contentValues, DatabaseHelper.WORKOUT_ID + " = " + _id, null);
        return i;
    }


    public int updateExerciseName(long _id, String exerciseName) {
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
        return i;
    }


    public int updateExerciseWeight(long _id, Double exerciseWeight) {

        //For exercises it is passing across the log id when a list item is long selected.
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.WEIGHT, exerciseWeight);

        //Updates the Exercise Weight
        int i = database.update(DatabaseHelper.TABLE_NAME_LOGS, contentValues, "LOGS.LOG_ID = ?",  new String[]{Long.toString(_id)});
        return i;
    }


    public int updateExerciseLogs(long log_id, String setSelected, Integer intReps){
        ContentValues contentValues = new ContentValues();
        contentValues.put(setSelected, intReps);
        int i = database.update(DatabaseHelper.TABLE_NAME_LOGS, contentValues, DatabaseHelper.LOG_ID + " = " + log_id, null);
        return i;
    }


    public int updateExerciseLogsWithImprovement(long log_id, String setSelected, Integer intReps, Integer intImprovement){
        ContentValues contentValues = new ContentValues();
        String setImprovement = setSelected + "_improvement";

        contentValues.put(setSelected, intReps);
        contentValues.put(setImprovement, intImprovement);
        int i = database.update(DatabaseHelper.TABLE_NAME_LOGS, contentValues, DatabaseHelper.LOG_ID + " = " + log_id, null);
        return i;
    }


    public int recordExerciseLogDuration(String log_id, long workoutDuration){

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.DURATION, workoutDuration);
        int i = database.update(DatabaseHelper.TABLE_NAME_LOGS, contentValues, DatabaseHelper.LOG_ID + " = " + log_id, null);
        return i;
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
        database.delete(DatabaseHelper.TABLE_NAME_WORKOUTS, DatabaseHelper.WORKOUT_ID + "=" + _id, null);
    }


    public void deleteExercise(long _id) {
        //For exercises it is passing across the log id when a list item is long selected.
        //We First work out which exercise correlates to the log id selected and then we delete that exercise id
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

        //Deletes the exercise based on the id we received earlier
        database.delete(DatabaseHelper.TABLE_NAME_EXERCISES,DatabaseHelper.EXERCISE_ID + "=" + exerciseId, null);

        //TODO - Have a think about this section... may need to remove the deletion of the exercise from EXERCISES table and just delete the log.
        //TODO - If you remove the exercise name the historical exercises which have previously been logged will also be lost/affected

        //Deletes the log associated with that exercise as well
        database.delete(DatabaseHelper.TABLE_NAME_LOGS,DatabaseHelper.LOG_ID + "=" + _id, null);
    }

//Added 8/16 checking exercise duplicates
    public boolean doesExerciseExist(String exerciseName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Query the database to see if the exercise name exists
            String[] projection = {dbHelper.EXERCISE_ID}; // Assuming you have an exercise ID column
            String selection = dbHelper.EXERCISE + " = ?";
            String[] selectionArgs = {exerciseName};

            cursor = db.query(dbHelper.TABLE_NAME_EXERCISES, projection, selection, selectionArgs, null, null, null);

            return cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}