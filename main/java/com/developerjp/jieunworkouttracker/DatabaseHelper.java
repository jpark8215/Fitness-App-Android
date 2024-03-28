package com.developerjp.jieunworkouttracker;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Table Name
    public static final String TABLE_NAME_WORKOUTS = "WORKOUTS";
    public static final String TABLE_NAME_EXERCISES = "EXERCISES";
    public static final String TABLE_NAME_LOGS = "LOGS";

    // Table columns
    public static final String WORKOUT_ID = "workout_id";
    public static final String WORKOUT = "workout";
    public static final String EXERCISE ="exercise";
    public static final String EXERCISE_ID = "exercise_id";
    public static final String LOG_ID = "log_id";
    public static final String SET1 = "set1";
    public static final String SET1_IMPROVEMENT = "set1_improvement";
    public static final String SET2 = "set2";
    public static final String SET2_IMPROVEMENT = "set2_improvement";
    public static final String SET3 = "set3";
    public static final String SET3_IMPROVEMENT = "set3_improvement";
    public static final String SET4 = "set4";
    public static final String SET4_IMPROVEMENT = "set4_improvement";
    public static final String SET5 = "set5";
    public static final String SET5_IMPROVEMENT = "set5_improvement";
    public static final String WEIGHT = "weight";
    public static final String DATE = "date";
    public static final String DATETIME = "datetime";
    public static final String DURATION = "duration";
    public static final String NOTES = "notes";
    public static final String ARCHIVE = "archive";


    // Database Information
    static final String DB_NAME = "JIEUN WORKOUTS.DB";

    // database version
    static final int DB_VERSION = 23;

    // Creating WORKOUTS table query
    private static final String CREATE_WORKOUTS_TABLE = "create table " + TABLE_NAME_WORKOUTS + "(" + WORKOUT_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + WORKOUT + " TEXT NOT NULL, " + ARCHIVE + " INTEGER);";


//TODO Find a way to de-primary key Exercise ID so duplicated exerciseID can be generated
    //Create EXERCISES table query
    private static final String CREATE_EXERCISES_TABLE = "CREATE TABLE " + TABLE_NAME_EXERCISES + " (" +
            EXERCISE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            WORKOUT_ID + " INTEGER NOT NULL, " +
            EXERCISE + " TEXT);";


    //Create LOGS table query
    private static final String CREATE_LOGS_TABLE = "create table " + TABLE_NAME_LOGS + "(" + LOG_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + EXERCISE_ID + " INTEGER NOT NULL, " + WORKOUT_ID + " INTEGER NOT NULL, " + SET1 + " INTEGER, " + SET1_IMPROVEMENT + " INTEGER, " + SET2 + " INTEGER, " + SET2_IMPROVEMENT + " INTEGER, " + SET3 + " INTEGER, " + SET3_IMPROVEMENT + " INTEGER, " + SET4 + " INTEGER, " + SET4_IMPROVEMENT + " INTEGER, " + SET5 + " INTEGER, " + SET5_IMPROVEMENT + " INTEGER, " + WEIGHT + " DOUBLE, " + DATE + " DATE, " + DATETIME + " DEFAULT CURRENT_TIMESTAMP, " + DURATION + " TIME, " + NOTES + " TEXT);";

    //TODO - Check this out
    //private static final String CREATE_LOG_TABLE = String.Format("create table %s$1 ", TABLE_NAME_LOGS)

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_WORKOUTS_TABLE);
        db.execSQL(CREATE_EXERCISES_TABLE);
        db.execSQL(CREATE_LOGS_TABLE);

    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion == 22) {
            //Inserts the new column if you're on an older version of the database
            db.execSQL("ALTER TABLE " + TABLE_NAME_WORKOUTS + " ADD COLUMN " + ARCHIVE + " TEXT;");
            //Updates the new column with data - populates with 0 value which means the workout is not archived
            db.execSQL("UPDATE " + TABLE_NAME_WORKOUTS + " SET " + ARCHIVE + " = 0 WHERE 1=1;");
        }
        else {

            /* TODO Make a plan for upgraded SQLite version
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_WORKOUTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_EXERCISES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_LOGS);
            onCreate(db);
             */
        }

    }


}