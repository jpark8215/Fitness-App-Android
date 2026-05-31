package com.developerjp.jieunworkouttracker;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CalendarShowSelectedWorkout extends AppCompatActivity {


    // Item List
    private final List<com.developerjp.jieunworkouttracker.ExerciseItem> ExerciseItem = new ArrayList<>();
    private final NumberFormat nf = new DecimalFormat("##.#");
    private final boolean rotate = false;
    private Double exerciseWeight;
    private String id;
    private String title;
    private String date;
    private Parcelable recyclerViewState;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get a reference to the Shared Preferences object
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);

        // Get the value of the "dark_mode" key, or "false" if it doesn't exist
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);

        // If dark mode is enabled then do the following
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            setTheme(R.style.DarkAppTheme_NoActionBar);
            // Otherwise do this
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            setTheme(R.style.AppTheme_NoActionBar);
        }
        setContentView(R.layout.activity_menu_drawer_simple_light);

        //Use view stubs to programmatically change the include view at runtime
        ViewStub stub = findViewById(R.id.main_view_stub);
        stub.setLayoutResource(R.layout.activity_main_exercise_list);
        stub.inflate();

        //Gets the values of the intent sent in the previous activity
        //Passes the values through to the public variables defined earlier
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");
        date = intent.getStringExtra("date");

        //Sets up the toolbar and navigation menu
        initToolbar();
        initNavigationMenu();

        //Loads the Exercise logs data using recyclerview and the custom adapter
        loadExerciseData();

        FloatingActionButton fab_add = findViewById(R.id.fab_add);
        if (fab_add != null) {
            fab_add.hide();
        }
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("");

        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(String.format("%s    %s", title, date));

        //Hides the chronometer as we don't need it for this activity
        Chronometer simpleChronometer = findViewById(R.id.simpleChronometer);
        simpleChronometer.setVisibility(View.GONE);
    }

    private void initNavigationMenu() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                Intent intent = null;
                if (itemId == R.id.nav_home) {
                    intent = new Intent(this, HomeDashboardActivity.class);
                } else if (itemId == R.id.nav_exercises) {
                    intent = new Intent(this, MainActivityExerciseList.class);
                } else if (itemId == R.id.nav_workout) {
                    // Check if there's an ongoing workout to resume
                    if (StartWorkoutActivity.isWorkoutOngoing) {
                        intent = new Intent(this, StartWorkoutActivity.class);
                        intent.putExtra("ongoing_workout", true);
                    } else {
                        // No ongoing workout, tell user to select exercises first
                        android.widget.Toast.makeText(this, "Please select exercises to start", android.widget.Toast.LENGTH_SHORT).show();
                        return true;
                    }
                } else if (itemId == R.id.nav_progress) {
                    intent = new Intent(this, ShowProgressActivity.class);
                } else if (itemId == R.id.nav_settings) {
                    intent = new Intent(this, SettingsActivity.class);
                } else if (itemId == R.id.nav_archived) {
                    intent = new Intent(this, ArchivedExerciseList.class);
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
                return true;
            });
        }
    }


    public void loadExerciseData() {
        // Log parameters for debugging
        Log.d("CalendarShowSelectedWorkout", "Loading data with ID: " + id + ", Title: " + title + ", Date: " + date);

        DBManager dbManager = new DBManager(this);
        dbManager.open();

        // Since workout_id is no longer used, we'll directly query by date
        Cursor cursor = null;

        try {
            cursor = dbManager.fetchExerciseDetailsForDate(date);
            Log.d("CalendarShowSelectedWorkout", "Query returned " +
                    (cursor != null ? cursor.getCount() : 0) + " rows");
        } catch (Exception e) {
            Log.e("CalendarShowSelectedWorkout", "Error querying database", e);
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Clear existing items
        ExerciseItem.clear();

        Log.d("CalendarShowSelectedWorkout", "Fetching details for specific log ID: " + id);
        try {
            cursor = dbManager.fetchExerciseLogsForDateAndLog(date, id);
            Log.d("CalendarShowSelectedWorkout", "Query returned " +
                    (cursor != null ? cursor.getCount() : 0) + " rows");
        } catch (Exception e) {
            Log.e("CalendarShowSelectedWorkout", "Error querying database by log ID", e);
        }

        // Get current weight unit preference
        boolean isKgUnit = WeightUnitManager.isKgUnit(this);

        // Process cursor data if we have results
        if (cursor != null && cursor.getCount() > 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                ExerciseItem exerciseItem = new ExerciseItem();

                // Extract log ID and exercise ID
                int logIdIndex = cursor.getColumnIndex(DatabaseHelper.LOG_ID);
                if (logIdIndex != -1) {
                    exerciseItem.setId(cursor.getString(logIdIndex));
                }

                int exerciseIdIndex = cursor.getColumnIndex(DatabaseHelper.EXERCISE_ID);
                if (exerciseIdIndex != -1) {
                    // If your ExerciseItem class has a setExerciseId method, you could store this
                    String exerciseId = cursor.getString(exerciseIdIndex);
                    Log.d("CalendarShowSelectedWorkout", "Found exercise with ID: " + exerciseId);
                }

                int exerciseNameIndex = cursor.getColumnIndex(DatabaseHelper.EXERCISE);
                if (exerciseNameIndex != -1) {
                    exerciseItem.setTitle(cursor.getString(exerciseNameIndex));
                }

                // Extract set values
                int set1Index = cursor.getColumnIndex(DatabaseHelper.SET1);
                if (set1Index != -1 && !cursor.isNull(set1Index)) {
                    exerciseItem.setButton1(cursor.getString(set1Index));
                } else {
                    exerciseItem.setButton1("0");
                }

                int set2Index = cursor.getColumnIndex(DatabaseHelper.SET2);
                if (set2Index != -1 && !cursor.isNull(set2Index)) {
                    exerciseItem.setButton2(cursor.getString(set2Index));
                } else {
                    exerciseItem.setButton2("0");
                }

                int set3Index = cursor.getColumnIndex(DatabaseHelper.SET3);
                if (set3Index != -1 && !cursor.isNull(set3Index)) {
                    exerciseItem.setButton3(cursor.getString(set3Index));
                } else {
                    exerciseItem.setButton3("0");
                }

                int set4Index = cursor.getColumnIndex(DatabaseHelper.SET4);
                if (set4Index != -1 && !cursor.isNull(set4Index)) {
                    exerciseItem.setButton4(cursor.getString(set4Index));
                } else {
                    exerciseItem.setButton4("0");
                }

                int set5Index = cursor.getColumnIndex(DatabaseHelper.SET5);
                if (set5Index != -1 && !cursor.isNull(set5Index)) {
                    exerciseItem.setButton5(cursor.getString(set5Index));
                } else {
                    exerciseItem.setButton5("0");
                }

                // Extract improvement indicators
                int set1ImprovementIndex = cursor.getColumnIndex(DatabaseHelper.SET1_IMPROVEMENT);
                if (set1ImprovementIndex != -1 && !cursor.isNull(set1ImprovementIndex)) {
                    int intSet1Improvement = cursor.getInt(set1ImprovementIndex);
                    setButtonColor(exerciseItem, 1, intSet1Improvement);
                } else {
                    setButtonColor(exerciseItem, 1, 0);
                }

                int set2ImprovementIndex = cursor.getColumnIndex(DatabaseHelper.SET2_IMPROVEMENT);
                if (set2ImprovementIndex != -1 && !cursor.isNull(set2ImprovementIndex)) {
                    int intSet2Improvement = cursor.getInt(set2ImprovementIndex);
                    setButtonColor(exerciseItem, 2, intSet2Improvement);
                } else {
                    setButtonColor(exerciseItem, 2, 0);
                }

                int set3ImprovementIndex = cursor.getColumnIndex(DatabaseHelper.SET3_IMPROVEMENT);
                if (set3ImprovementIndex != -1 && !cursor.isNull(set3ImprovementIndex)) {
                    int intSet3Improvement = cursor.getInt(set3ImprovementIndex);
                    setButtonColor(exerciseItem, 3, intSet3Improvement);
                } else {
                    setButtonColor(exerciseItem, 3, 0);
                }

                int set4ImprovementIndex = cursor.getColumnIndex(DatabaseHelper.SET4_IMPROVEMENT);
                if (set4ImprovementIndex != -1 && !cursor.isNull(set4ImprovementIndex)) {
                    int intSet4Improvement = cursor.getInt(set4ImprovementIndex);
                    setButtonColor(exerciseItem, 4, intSet4Improvement);
                } else {
                    setButtonColor(exerciseItem, 4, 0);
                }

                int set5ImprovementIndex = cursor.getColumnIndex(DatabaseHelper.SET5_IMPROVEMENT);
                if (set5ImprovementIndex != -1 && !cursor.isNull(set5ImprovementIndex)) {
                    int intSet5Improvement = cursor.getInt(set5ImprovementIndex);
                    setButtonColor(exerciseItem, 5, intSet5Improvement);
                } else {
                    setButtonColor(exerciseItem, 5, 0);
                }

                // Extract and format weight
                int weightIndex = cursor.getColumnIndex(DatabaseHelper.WEIGHT);
                if (weightIndex != -1 && !cursor.isNull(weightIndex)) {
                    double exerciseWeight = cursor.getDouble(weightIndex);

                    // Store original weight in kg
                    exerciseItem.setWeight(exerciseWeight);

                    // Format weight with appropriate unit
                    String formattedWeight;
                    if (isKgUnit) {
                        formattedWeight = WeightUtils.formatWeight(exerciseWeight, true);
                    } else {
                        double weightInLbs = WeightUtils.kgToLbs(exerciseWeight);
                        formattedWeight = WeightUtils.formatWeight(weightInLbs, false);
                    }
                    exerciseItem.setDisplayWeight(formattedWeight);
                } else {
                    exerciseItem.setWeight(0.0);
                    exerciseItem.setDisplayWeight(isKgUnit ? "0.0 kg" : "0.0 lbs");
                }

                // Add the exercise to our list
                ExerciseItem.add(exerciseItem);
            }
        }

        // Create and configure the adapter
        ExerciseRecyclerViewAdapter adapter = new ExerciseRecyclerViewAdapter(ExerciseItem, this, null, null);
        adapter.setReadOnly(true);  // Make it read-only since this is historical data
        recyclerView.setAdapter(adapter);

        // Clean up
        if (cursor != null) {
            cursor.close();
        }
        dbManager.close();
    }


    // Helper method to set button colors based on improvement value
    private void setButtonColor(ExerciseItem exerciseItem, int buttonNumber, int improvementValue) {
        int colorResource;

        switch (improvementValue) {
            case 1:
                colorResource = R.drawable.button_shape_red;
                break;
            case 2:
                colorResource = R.drawable.button_shape_blue;
                break;
            default:
                colorResource = R.drawable.button_shape_default;
                break;
        }

        switch (buttonNumber) {
            case 1:
                exerciseItem.setButton1Colour(colorResource);
                break;
            case 2:
                exerciseItem.setButton2Colour(colorResource);
                break;
            case 3:
                exerciseItem.setButton3Colour(colorResource);
                break;
            case 4:
                exerciseItem.setButton4Colour(colorResource);
                break;
            case 5:
                exerciseItem.setButton5Colour(colorResource);
                break;
        }
    }


    public void bottomNavigationHomeClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivityExerciseList.class);
        startActivity(intent);
    }

    public void bottomNavigationCalendarClick(View view) {
        Intent intent = new Intent(getApplicationContext(), ShowCalendarActivity.class);
        startActivity(intent);
    }
}


