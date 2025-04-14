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
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

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
        stub.setLayoutResource(R.layout.activity_exercise_list);
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

        View parent_view = findViewById(android.R.id.content);
        View back_drop = findViewById(R.id.back_drop);
        View lyt_add_exercise = findViewById(R.id.lyt_add_exercise);
        View lyt_start_workout = findViewById(R.id.lyt_start_workout);

        //Loads the Exercise logs data using recyclerview and the custom adapter
        loadExerciseData();

        FloatingActionButton fab_add = findViewById(R.id.fab_add);
        fab_add.hide();

        FloatingActionButton fab_add_exercise = findViewById(R.id.fab_add_exercise);
        FloatingActionButton fab_start_workout = findViewById(R.id.fab_start_workout);
        CardView cv_add_exercise = findViewById(R.id.cv_add_exercise);
        CardView cv_start_workout = findViewById(R.id.cv_start_workout);

        back_drop.setVisibility(View.GONE);
        ViewAnimation.initShowOut(lyt_add_exercise);
        ViewAnimation.initShowOut(lyt_start_workout);
        
    }


    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("");

        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(title + "    " + date);

        //Hides the chronometer as we don't need it for this activity
        Chronometer simpleChronometer = findViewById(R.id.simpleChronometer);
        simpleChronometer.setVisibility(View.GONE);
    }

    private void initNavigationMenu() {
        NavigationView nav_view = findViewById(R.id.nav_view);
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // open drawer at start
        //drawer.openDrawer(GravityCompat.START);

        //Handles side navigation menu clicks
        nav_view.setNavigationItemSelectedListener(item -> {
            String itemCLicked = Objects.requireNonNull(item.getTitle()).toString();
            Intent intent;

            switch (itemCLicked) {

                case "Workouts":
                    Log.d("menu item clicked", "Workouts");
                    //Starts the MainActivityExerciseList activity
                    intent = new Intent(getApplicationContext(), MainActivityExerciseList.class);
                    startActivity(intent);
                    break;
                case "Archived":
                    Log.d("menu item clicked", "Archived");
                    intent = new Intent(getApplicationContext(), ArchivedExerciseList.class);
                    startActivity(intent);
                    break;
                case "Progress":
                    Log.d("menu item clicked", "Progress");
                    intent = new Intent(getApplicationContext(), ShowProgressActivity.class);
                    startActivity(intent);
                    break;
                case "Calendar":
                    Log.d("menu item clicked", "Calendar");
                    //Starts the Calendar activity
                    intent = new Intent(getApplicationContext(), ShowCalendarActivity.class);
                    startActivity(intent);
                    break;
                case "Color Scheme":
                    Log.d("menu item clicked", "Color Scheme");
                    intent = new Intent(getApplicationContext(), ColorSchemeActivity.class);
                    startActivity(intent);
                    break;
            }

            drawer.closeDrawers();
            return true;
        });
    }


    public void loadExerciseData() {
        // Log the parameters for debugging
        Log.d("CalendarShowSelectedWorkout", "Loading data with ID: " + id + ", Title: " + title + ", Date: " + date);
        
        DBManager dbManager = new DBManager(this);
        dbManager.open();
        
        // Prepare the date for querying if needed
        String formattedDate = date;
        if (!formattedDate.contains("%")) {
            formattedDate = formattedDate + "%";  // Add wildcard for time part
        }
        
        // First try to use fetchExerciseDetailsForDate which is designed for this purpose
        Cursor cursor = dbManager.fetchExerciseDetailsForDate(formattedDate);
        Log.d("CalendarShowSelectedWorkout", "fetchExerciseDetailsForDate returned " + 
              (cursor != null ? cursor.getCount() : 0) + " rows");
        
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Clear existing items
        ExerciseItem.clear();
        
        boolean hasExercises = cursor != null && cursor.getCount() > 0;
        
        // Get current weight unit preference
        boolean isKgUnit = WeightUnitManager.isKgUnit(this);
        
        // Show appropriate message based on whether we have exercises
        TextView empty = findViewById(R.id.empty);
        if (empty != null) {
            if (!hasExercises) {
                empty.setVisibility(View.VISIBLE);
                empty.setText("No exercises found for " + date);
            } else {
                empty.setVisibility(View.GONE);
            }
        }
        
        // Process cursor data if we have results
        if (hasExercises) {
            // Track improvements for coloring
            int intSet1Improvement = 0;
            int intSet2Improvement = 0;
            int intSet3Improvement = 0;
            int intSet4Improvement = 0;
            int intSet5Improvement = 0;
            
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                ExerciseItem exerciseItem = new ExerciseItem();
                
                // Extract log ID and exercise name
                int logIdIndex = cursor.getColumnIndex(DatabaseHelper.LOG_ID);
                if (logIdIndex != -1) {
                    exerciseItem.setId(cursor.getString(logIdIndex));
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
                    intSet1Improvement = cursor.getInt(set1ImprovementIndex);
                } else {
                    intSet1Improvement = 0;
                }
                
                int set2ImprovementIndex = cursor.getColumnIndex(DatabaseHelper.SET2_IMPROVEMENT);
                if (set2ImprovementIndex != -1 && !cursor.isNull(set2ImprovementIndex)) {
                    intSet2Improvement = cursor.getInt(set2ImprovementIndex);
                } else {
                    intSet2Improvement = 0;
                }
                
                int set3ImprovementIndex = cursor.getColumnIndex(DatabaseHelper.SET3_IMPROVEMENT);
                if (set3ImprovementIndex != -1 && !cursor.isNull(set3ImprovementIndex)) {
                    intSet3Improvement = cursor.getInt(set3ImprovementIndex);
                } else {
                    intSet3Improvement = 0;
                }
                
                int set4ImprovementIndex = cursor.getColumnIndex(DatabaseHelper.SET4_IMPROVEMENT);
                if (set4ImprovementIndex != -1 && !cursor.isNull(set4ImprovementIndex)) {
                    intSet4Improvement = cursor.getInt(set4ImprovementIndex);
                } else {
                    intSet4Improvement = 0;
                }
                
                int set5ImprovementIndex = cursor.getColumnIndex(DatabaseHelper.SET5_IMPROVEMENT);
                if (set5ImprovementIndex != -1 && !cursor.isNull(set5ImprovementIndex)) {
                    intSet5Improvement = cursor.getInt(set5ImprovementIndex);
                } else {
                    intSet5Improvement = 0;
                }
                
                // Set button colors based on improvement
                switch (intSet1Improvement) {
                    case 0:
                        exerciseItem.setButton1Colour(R.drawable.button_shape_default);
                        break;
                    case 1:
                        exerciseItem.setButton1Colour(R.drawable.button_shape_red);
                        break;
                    case 2:
                        exerciseItem.setButton1Colour(R.drawable.button_shape_blue);
                        break;
                }
                
                // ... similar code for other set buttons ...
                
                // Extract and format weight
                int weightIndex = cursor.getColumnIndex(DatabaseHelper.WEIGHT);
                if (weightIndex != -1 && !cursor.isNull(weightIndex)) {
                    exerciseWeight = cursor.getDouble(weightIndex);
                    
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

    public void bottomNavigationHomeClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivityExerciseList.class);
        startActivity(intent);
    }

    public void bottomNavigationCalendarClick(View view) {
        Intent intent = new Intent(getApplicationContext(), ShowCalendarActivity.class);
        startActivity(intent);
    }
}


