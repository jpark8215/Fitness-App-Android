package com.developerjp.jieunworkouttracker;


import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class StartWorkoutActivity extends AppCompatActivity implements WorkoutRecyclerViewAdapter.OnItemLongSelectedListener, WorkoutRecyclerViewAdapter.OnButtonClickListener {

    // Item List
    private final List<com.developerjp.jieunworkouttracker.ExerciseItem> ExerciseItem = new ArrayList<>();
    //Public variables which are used across classes/voids
    public String id;
    public String title;
    //Will set this to true when workout has been paused
    public boolean isPaused = false;
    //Will be used when the chronometer is paused.
    public long timeWhenStopped = 0;
    //Will be populated when we call the loadExerciseData() class
    public String log_id;
    //Related to service
    WorkoutService mBoundService;
    boolean mServiceBound = false;
    Intent serviceIntent;
    private DBManager dbManager;
    private RecyclerView recyclerView;
    // Custom Recycler View Adaptor
    private WorkoutRecyclerViewAdapter adapter;
    private View back_drop;
    private boolean rotate = false;
    private View lyt_pause_workout;
    private View lyt_finish_workout;
    private FloatingActionButton fab_add;
    private FloatingActionButton fab_pause_workout;
    private TextView txt_pause_workout;
    private Parcelable recyclerViewState;
    private String strNumberOfExercises;
    private Toolbar toolbar;
    private Chronometer simpleChronometer;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WorkoutService.MyBinder myBinder = (WorkoutService.MyBinder) service;
            mBoundService = myBinder.getService();
            mServiceBound = true;

            //Once the Bound service is connected it starts the timer for the workout
            startChronometer();
        }
    };

    // Add this as a class member variable
    private final List<String> workoutLogIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize MobileAds
        MobileAds.initialize(this, initializationStatus -> {
        });

        // Apply theme using ThemeManager
        ThemeManager.applyTheme(this);

        setContentView(R.layout.activity_menu_drawer_simple_light);

        //Use view stubs to programmatically change the include view at runtime
        ViewStub stub = findViewById(R.id.main_view_stub);
        stub.setLayoutResource(R.layout.activity_start_workout);
        stub.inflate();

        //Gets the values of the intent sent in the previous activity
        //Passes the values through to the public variables defined earlier
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");

        // Get the selected exercise IDs from the intent
        ArrayList<String> selectedExerciseIds = getIntent().getStringArrayListExtra("selected_exercise_ids");
        if (selectedExerciseIds == null || selectedExerciseIds.isEmpty()) {
//            Toast.makeText(this, "No exercises selected", Toast.LENGTH_SHORT).show();
            Log.d("StartWorkoutActivity", "No exercises selected");
            finish();
            return;
        }

        //Sets up the toolbar and navigation menu
        initToolbar();
        initNavigationMenu();

        // Initialize the database manager
        dbManager = new DBManager(this);
        dbManager.open();

        // Load the selected exercises
        loadSelectedExercises(selectedExerciseIds);

        View parent_view = findViewById(android.R.id.content);
        back_drop = findViewById(R.id.back_drop);
        lyt_pause_workout = findViewById(R.id.lyt_pause_workout);
        lyt_finish_workout = findViewById(R.id.lyt_finish_workout);

        fab_add = findViewById(R.id.fab_add);
        fab_pause_workout = findViewById(R.id.fab_pause_workout);
        FloatingActionButton fab_finish_workout = findViewById(R.id.fab_finish_workout);
        CardView cv_pause_workout = findViewById(R.id.cv_pause_workout);
        CardView cv_finish_workout = findViewById(R.id.cv_finish_workout);
        txt_pause_workout = findViewById(R.id.txt_pause_workout);

        back_drop.setVisibility(View.GONE);
        ViewAnimation.initShowOut(lyt_pause_workout);
        ViewAnimation.initShowOut(lyt_finish_workout);

        //Chronometer is used for the counter timer
        simpleChronometer = findViewById(R.id.simpleChronometer);

        //Starts the WorkoutService which keeps track of the workout time
        serviceIntent = new Intent(this, WorkoutService.class);
        serviceIntent.putExtra("id", id);
        serviceIntent.putExtra("title", title);
        startService(serviceIntent);
        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        // Database connection is already initialized above, no need to do it again
        //Loads the Exercise logs data using recyclerview and the custom adapter
        loadSelectedExercises(selectedExerciseIds);

        fab_add.setOnClickListener(this::toggleFabMode);

        back_drop.setOnClickListener(v -> toggleFabMode(fab_add));

        fab_pause_workout.setOnClickListener(v -> pauseWorkout());

        cv_pause_workout.setOnClickListener(v -> pauseWorkout());

        fab_finish_workout.setOnClickListener(v -> finishWorkout());

        cv_finish_workout.setOnClickListener(v -> finishWorkout());

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
        //Hide the textview as we will be using the chronometer instead to time the workout
        txtTitle.setVisibility(View.GONE);

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

                case "Exercises":
                    Log.d("menu item clicked", "Exercises");
                    //Starts the MainActivityExerciseList activity
                    intent = new Intent(getApplicationContext(), MainActivityExerciseList.class);
                    startActivity(intent);
                    break;
                case "Archived Exercises":
                    Log.d("menu item clicked", "Archived Exercises");
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
                case "Settings":
                    Log.d("menu item clicked", "Settings");
                    intent = new Intent(getApplicationContext(), ColorSchemeActivity.class);
                    startActivity(intent);
                    break;
            }

            drawer.closeDrawers();
            return true;
        });
    }

    public void startChronometer() {
        // Check if service is bound before accessing it
        if (mBoundService != null) {
            long timer = mBoundService.getTime();
            simpleChronometer.setBase(timer);
            simpleChronometer.start();
        } else {
            // If service is not yet bound, use system time
            simpleChronometer.setBase(SystemClock.elapsedRealtime());
            simpleChronometer.start();
        }
    }


    private void loadSelectedExercises(ArrayList<String> exerciseIds) {
        ExerciseItem.clear();
        workoutLogIds.clear(); // Clear previous log IDs

        // Get the selected exercise IDs from the intent
        ArrayList<String> selectedExerciseIds = getIntent().getStringArrayListExtra("selected_exercise_ids");

        if (exerciseIds != null && !exerciseIds.isEmpty()) {
            // Load exercises for each ID
            for (String id : exerciseIds) {
                // Get exercise details from the database
                Cursor cursor = dbManager.getExerciseDetails(id);

                if (cursor != null && cursor.moveToFirst()) {
                    // Create an ExerciseItem for each exercise
                    ExerciseItem item = new ExerciseItem();

                    // Get column indices
                    int exerciseIdIndex = cursor.getColumnIndex(DatabaseHelper.EXERCISE_ID);
                    int exerciseNameIndex = cursor.getColumnIndex(DatabaseHelper.EXERCISE);
                    int weightIndex = cursor.getColumnIndex(DatabaseHelper.WEIGHT);
                    int logIdIndex = cursor.getColumnIndex(DatabaseHelper.LOG_ID);

                    // Set exercise properties
                    if (exerciseIdIndex != -1) {
                        String exerciseId = cursor.getString(exerciseIdIndex);
                        // Don't set item.setId here, we'll set it with the log_id below
                    }

                    if (exerciseNameIndex != -1) {
                        item.setTitle(cursor.getString(exerciseNameIndex));
                    }

                    if (weightIndex != -1) {
                        item.setWeight(cursor.getDouble(weightIndex));
                    }

                    // Set the log_id from today's log
                    if (logIdIndex != -1) {
                        String logId = cursor.getString(logIdIndex);
                        item.setId(logId);
                        workoutLogIds.add(logId); // Store the log ID
                        Log.d("StartWorkoutActivity", "Loading exercise with log_id: " + logId);
                    }

                    // Now check for set values and improvements in the log
                    int set1Index = cursor.getColumnIndex(DatabaseHelper.SET1);
                    int set2Index = cursor.getColumnIndex(DatabaseHelper.SET2);
                    int set3Index = cursor.getColumnIndex(DatabaseHelper.SET3);
                    int set4Index = cursor.getColumnIndex(DatabaseHelper.SET4);
                    int set5Index = cursor.getColumnIndex(DatabaseHelper.SET5);

                    int imp1Index = cursor.getColumnIndex(DatabaseHelper.SET1_IMPROVEMENT);
                    int imp2Index = cursor.getColumnIndex(DatabaseHelper.SET2_IMPROVEMENT);
                    int imp3Index = cursor.getColumnIndex(DatabaseHelper.SET3_IMPROVEMENT);
                    int imp4Index = cursor.getColumnIndex(DatabaseHelper.SET4_IMPROVEMENT);
                    int imp5Index = cursor.getColumnIndex(DatabaseHelper.SET5_IMPROVEMENT);

                    // Set reps if available in the cursor
                    if (set1Index != -1) {
                        item.setButton1(cursor.getString(set1Index));
                    } else {
                        item.setButton1("5");
                    }

                    if (set2Index != -1) {
                        item.setButton2(cursor.getString(set2Index));
                    } else {
                        item.setButton2("5");
                    }

                    if (set3Index != -1) {
                        item.setButton3(cursor.getString(set3Index));
                    } else {
                        item.setButton3("5");
                    }

                    if (set4Index != -1) {
                        item.setButton4(cursor.getString(set4Index));
                    } else {
                        item.setButton4("5");
                    }

                    if (set5Index != -1) {
                        item.setButton5(cursor.getString(set5Index));
                    } else {
                        item.setButton5("5");
                    }

                    // Set colors based on improvement values
                    if (imp1Index != -1) {
                        int imp = cursor.getInt(imp1Index);
                        if (imp == 2) item.setButton1Colour(R.drawable.button_shape_green);
                        else if (imp == 1) item.setButton1Colour(R.drawable.button_shape_red);
                        else item.setButton1Colour(R.drawable.button_shape_default);
                    } else {
                        item.setButton1Colour(R.drawable.button_shape_default);
                    }

                    if (imp2Index != -1) {
                        int imp = cursor.getInt(imp2Index);
                        if (imp == 2) item.setButton2Colour(R.drawable.button_shape_green);
                        else if (imp == 1) item.setButton2Colour(R.drawable.button_shape_red);
                        else item.setButton2Colour(R.drawable.button_shape_default);
                    } else {
                        item.setButton2Colour(R.drawable.button_shape_default);
                    }

                    if (imp3Index != -1) {
                        int imp = cursor.getInt(imp3Index);
                        if (imp == 2) item.setButton3Colour(R.drawable.button_shape_green);
                        else if (imp == 1) item.setButton3Colour(R.drawable.button_shape_red);
                        else item.setButton3Colour(R.drawable.button_shape_default);
                    } else {
                        item.setButton3Colour(R.drawable.button_shape_default);
                    }

                    if (imp4Index != -1) {
                        int imp = cursor.getInt(imp4Index);
                        if (imp == 2) item.setButton4Colour(R.drawable.button_shape_green);
                        else if (imp == 1) item.setButton4Colour(R.drawable.button_shape_red);
                        else item.setButton4Colour(R.drawable.button_shape_default);
                    } else {
                        item.setButton4Colour(R.drawable.button_shape_default);
                    }

                    if (imp5Index != -1) {
                        int imp = cursor.getInt(imp5Index);
                        if (imp == 2) item.setButton5Colour(R.drawable.button_shape_green);
                        else if (imp == 1) item.setButton5Colour(R.drawable.button_shape_red);
                        else item.setButton5Colour(R.drawable.button_shape_default);
                    } else {
                        item.setButton5Colour(R.drawable.button_shape_default);
                    }

                    // Add the item to the list
                    ExerciseItem.add(item);

                    // Close the cursor
                    cursor.close();
                }
            }

            // Initialize the RecyclerView
            recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Set up the adapter
            adapter = new WorkoutRecyclerViewAdapter(ExerciseItem, this, this, this);
            recyclerView.setAdapter(adapter);

            // Log the loaded exercise items for debugging
            for (com.developerjp.jieunworkouttracker.ExerciseItem item : ExerciseItem) {
                Log.d("StartWorkoutActivity", "Loaded exercise: " + item.getTitle() + ", log_id: " + item.getId());
            }
        }
    }


    @Override
    protected void onDestroy() {
        try {
            // Check if dbManager is not null before closing it
            if (dbManager != null) {
                dbManager.close();
                dbManager = null;
            }

            // Unbind from the service if bound
            if (mServiceBound) {
                unbindService(mServiceConnection);
                mServiceBound = false;
            }
        } catch (Exception e) {
            Log.e("StartWorkoutActivity", "Error during onDestroy: " + e.getMessage());
        }

        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        // When backing out of the activity, make sure to clean up resources
        try {
            // Stop and unbind from the service if bound
            if (mServiceBound) {
                unbindService(mServiceConnection);
                mServiceBound = false;
            }

            // Stop the chronometer
            if (simpleChronometer != null) {
                simpleChronometer.stop();
            }
        } catch (Exception e) {
            Log.e("StartWorkoutActivity", "Error during onBackPressed: " + e.getMessage());
        }

        // Return to the workout list page
        super.onBackPressed();
        this.finish();
    }


    @Override
    public void onItemLongSelected(String itemId, String itemTitle, Double itemWeight) {
        modifyExercise(itemId, itemTitle, itemWeight);

    }

    private void modifyExercise(String itemId, String itemTitle, Double itemWeight) {
        showCustomModifyDialog(itemId, itemTitle, itemWeight);
    }

    private void showCustomModifyDialog(final String itemId, String itemTitle, Double itemWeight) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_modify_light);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(Objects.requireNonNull(dialog.getWindow()).getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final EditText exerciseEditText = dialog.findViewById(R.id.name_edittext);
        final EditText weightEditText = dialog.findViewById(R.id.weight_edittext);
        TextView txtTitle = dialog.findViewById(R.id.txt_title);
        Button btnUpdate = dialog.findViewById(R.id.btn_update);
        Button btnDelete = dialog.findViewById(R.id.btn_delete);
        Button btnArchive = dialog.findViewById(R.id.btn_archive);
        Button btnPlaceholder = dialog.findViewById(R.id.btn_placeholder);

        txtTitle.setText("Modify Exercise");
        exerciseEditText.setText(itemTitle);
        weightEditText.setText(itemWeight.toString());

        //Hides the archive and placeholder buttons
        btnArchive.setVisibility(View.GONE);
        btnPlaceholder.setVisibility(View.GONE);

        //Sets the cursor position to the end of text, rather than at the start
        exerciseEditText.setSelection(exerciseEditText.getText().length());
        weightEditText.setSelection(weightEditText.getText().length());


        btnUpdate.setOnClickListener(v -> {

            //Does a validation check to make sure the user has entered in a value for the exercise name
            if (TextUtils.isEmpty(exerciseEditText.getText())) {
                Toast.makeText(StartWorkoutActivity.this,
                        "You must give an exercise name", Toast.LENGTH_LONG).show();

                //If the user has given an exercise name then we will update the exercise name in the database
            } else {
                String newWorkoutName = exerciseEditText.getText().toString();
                long _id = Long.parseLong(itemId);

                //Updates with the new value
                String newExerciseName = exerciseEditText.getText().toString();
                dbManager.updateExerciseName(_id, newExerciseName);


                //If there is a weight given then update the database
                if (!weightEditText.getText().toString().trim().isEmpty()) {
                    double newExerciseWeight = Double.parseDouble(weightEditText.getText().toString());
                    dbManager.updateExerciseWeight(String.valueOf(_id), newExerciseWeight);
                } else {
                    //If no weight value was given then update with a default value of 0
                    double newExerciseWeight = 0.0;
                    dbManager.updateExerciseWeight(String.valueOf(_id), newExerciseWeight);
                }


                //Remembers the position of the recycler view when modify exercise or delete exercise is called
                final Parcelable recyclerViewState;
                recyclerViewState = Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState();

                //Shows the update made by clearing the recyclerview and re-adding all the items
                //Works better this way as we don't have to re-create the entire activity
                ExerciseItem.clear();
                // Get the original selected exercises from the intent
                ArrayList<String> selectedExerciseIds = getIntent().getStringArrayListExtra("selected_exercise_ids");
                if (selectedExerciseIds != null && !selectedExerciseIds.isEmpty()) {
                    // Remove the deleted exercise ID from the list
                    selectedExerciseIds.remove(itemId);
                    if (!selectedExerciseIds.isEmpty()) {
                        loadSelectedExercises(selectedExerciseIds);
                    } else {
                        // No exercises left, finish the activity
                        Toast.makeText(StartWorkoutActivity.this, "No exercises remaining", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    // Fallback to just this exercise if needed (though it was deleted)
                    finish();
                }
                adapter.notifyDataSetChanged();

                //places the user back at the same position in the recycler view rather than scrolling all the way back up to the top
                recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);

                //Closes the dialog
                dialog.dismiss();
            }
        });

        btnDelete.setOnClickListener(v -> {
            long _id = Long.parseLong(itemId);

            //Deletes the selected exercise
            dbManager.deleteExercise(_id);

            //Remembers the position of the recycler view when modify exercise or delete exercise is called
            final Parcelable recyclerViewState;
            recyclerViewState = Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState();

            //Shows the update made by clearing the recyclerview and re-adding all the items
            //Works better this way as we don't have to re-create the entire activity
            ExerciseItem.clear();
            // Get the original selected exercises from the intent
            ArrayList<String> selectedExerciseIds = getIntent().getStringArrayListExtra("selected_exercise_ids");
            if (selectedExerciseIds != null && !selectedExerciseIds.isEmpty()) {
                // Remove the deleted exercise ID from the list
                selectedExerciseIds.remove(itemId);
                if (!selectedExerciseIds.isEmpty()) {
                    loadSelectedExercises(selectedExerciseIds);
                } else {
                    // No exercises left, finish the activity
                    Toast.makeText(StartWorkoutActivity.this, "No exercises remaining", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                // Fallback to just this exercise if needed (though it was deleted)
                finish();
            }
            adapter.notifyDataSetChanged();

            //places the user back at the same position in the recycler view rather than scrolling all the way back up to the top
            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);

            //Closes the dialog
            dialog.dismiss();
        });

        (dialog.findViewById(R.id.bt_close)).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }


    @Override
    protected void onPause() {
        //When the user navigates away from this screen we will minimise the floating action menu
        //floatingActionsMenu.collapse();

        // Save state - used when user clicks on an item far down the recycler view list
        // It remembers the state or position
        recyclerViewState = Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState();
        super.onPause();
    }


    @Override
    public void onResume() {
        super.onResume();
        //Refreshes the data when the activity is resumed.
        //Mainly used for when an exercise is updated.

        // Make sure database is open
        if (dbManager == null) {
            dbManager = new DBManager(this);
        }
        if (dbManager.isOpen()) {
            dbManager.open();
        }

        // Get the selected exercise IDs from the intent
        ArrayList<String> selectedExerciseIds = getIntent().getStringArrayListExtra("selected_exercise_ids");
        if (selectedExerciseIds != null && !selectedExerciseIds.isEmpty() && adapter != null) {
            ExerciseItem.clear();
            loadSelectedExercises(selectedExerciseIds);
            adapter.notifyDataSetChanged();

            // Restore state only if recyclerViewState has been saved
            if (recyclerViewState != null && recyclerView != null && recyclerView.getLayoutManager() != null) {
                recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            }
        }
    }

    @Override
    public void OnBackPressedDispatcher() {

    }

    @Override
    public void onButtonClick(String itemId, String itemTitle, String setSelected, Integer intReps, Integer intImprovement) {
        try {
            // itemId is now the log_id, not the exercise_id
            long logId = Long.parseLong(itemId);

            //Validation to make sure reps can never be less than zero
            if (intReps < 0) {
                intReps = 0;
            }

            Log.d("StartWorkoutActivity", "Updating log: " + logId + ", set: " + setSelected +
                    ", reps: " + intReps + ", improvement: " + intImprovement);

            //We pass through the log_id, set selected, number of reps & integer value of if there was an improvement made
            dbManager.updateExerciseLogsWithImprovement(logId, setSelected, intReps, intImprovement);

            // Find the item in the list and update its button text value
            for (int i = 0; i < ExerciseItem.size(); i++) {
                ExerciseItem item = ExerciseItem.get(i);
                if (item.getId().equals(itemId)) {
                    // Update the button text based on which set was clicked
                    switch (setSelected) {
                        case "set1":
                            item.setButton1(String.valueOf(intReps));
                            // Also update the button color based on improvement
                            if (intImprovement == 2) {
                                item.setButton1Colour(R.drawable.button_shape_green);
                            } else if (intImprovement == 1) {
                                item.setButton1Colour(R.drawable.button_shape_red);
                            }
                            break;
                        case "set2":
                            item.setButton2(String.valueOf(intReps));
                            if (intImprovement == 2) {
                                item.setButton2Colour(R.drawable.button_shape_green);
                            } else if (intImprovement == 1) {
                                item.setButton2Colour(R.drawable.button_shape_red);
                            }
                            break;
                        case "set3":
                            item.setButton3(String.valueOf(intReps));
                            if (intImprovement == 2) {
                                item.setButton3Colour(R.drawable.button_shape_green);
                            } else if (intImprovement == 1) {
                                item.setButton3Colour(R.drawable.button_shape_red);
                            }
                            break;
                        case "set4":
                            item.setButton4(String.valueOf(intReps));
                            if (intImprovement == 2) {
                                item.setButton4Colour(R.drawable.button_shape_green);
                            } else if (intImprovement == 1) {
                                item.setButton4Colour(R.drawable.button_shape_red);
                            }
                            break;
                        case "set5":
                            item.setButton5(String.valueOf(intReps));
                            if (intImprovement == 2) {
                                item.setButton5Colour(R.drawable.button_shape_green);
                            } else if (intImprovement == 1) {
                                item.setButton5Colour(R.drawable.button_shape_red);
                            }
                            break;
                    }

                    Log.d("StartWorkoutActivity", "Updated ExerciseItem at position " + i +
                            ", set: " + setSelected + ", reps: " + intReps);
                    break;
                }
            }

            // Save state - used when user clicks on an item far down the recycler view list
            // It remembers the state or position
            final Parcelable recyclerViewState;
            if (recyclerView != null && recyclerView.getLayoutManager() != null) {
                recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();

                // Trigger data refresh but maintain colors
                // The colors are now stored in the ExerciseItem objects
                adapter.notifyDataSetChanged();

                // Restore state
                // Once the data is updated we load the same state or position
                // This stops the recycler view of scrolling all the way back to the top when a button is clicked
                recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            }
        } catch (Exception e) {
            Log.e("StartWorkoutActivity", "Error updating exercise: " + e.getMessage());
            Toast.makeText(this, "Failed to update exercise", Toast.LENGTH_SHORT).show();
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

    private void toggleFabMode(View v) {
        rotate = ViewAnimation.rotateFab(v, !rotate);
        if (rotate) {
            ViewAnimation.showIn(lyt_pause_workout);
            ViewAnimation.showIn(lyt_finish_workout);
            back_drop.setVisibility(View.VISIBLE);
        } else {
            ViewAnimation.showOut(lyt_pause_workout);
            ViewAnimation.showOut(lyt_finish_workout);
            back_drop.setVisibility(View.GONE);
        }
    }

    public void pauseWorkout() {
        //If workout isn't already paused then do the following
        if (!isPaused) {

            //minimises the floating action button
            toggleFabMode(fab_add);
            //Shows a snackbar message to the user letting them know the workout has been paused
            final Snackbar snackbar = Snackbar.make(findViewById(R.id.viewSnack), "", Snackbar.LENGTH_SHORT);
            //inflate view
            View custom_view = getLayoutInflater().inflate(R.layout.snackbar_icon_text, null);

            snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
            View snackBarView = snackbar.getView();
            snackBarView.setPadding(0, 0, 0, 0);

            ((TextView) custom_view.findViewById(R.id.message)).setText("Workout Paused!");
            ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_done);
            (custom_view.findViewById(R.id.parent_view)).setBackgroundColor(getResources().getColor(R.color.colorSuccess));
//            snackBarView.addView(custom_view, 0);
            snackbar.show();

            //Calculates the time when stopped
            timeWhenStopped = simpleChronometer.getBase() - SystemClock.elapsedRealtime();
            //Stops the timer
            simpleChronometer.stop();
            txt_pause_workout.setText("Resume Workout");
            fab_pause_workout.setImageResource(R.drawable.fab_resume_workout);
            isPaused = true;
        }

        //If workout is already paused then do the following
        else {

            //minimises the floating action button
            toggleFabMode(fab_add);
            //Shows a snackbar message to the user letting them know the workout has resumed
            final Snackbar snackbar = Snackbar.make(findViewById(R.id.viewSnack), "", Snackbar.LENGTH_SHORT);
            //inflate view
            View custom_view = getLayoutInflater().inflate(R.layout.snackbar_icon_text, null);

            snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
            View snackBarView = snackbar.getView();
            snackBarView.setPadding(0, 0, 0, 0);

            ((TextView) custom_view.findViewById(R.id.message)).setText("Workout Resumed!");
            ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_done);
            (custom_view.findViewById(R.id.parent_view)).setBackgroundColor(getResources().getColor(R.color.colorSuccess));
//            snackBarView.addView(custom_view, 0);
            snackbar.show();


            //Sets the correct timer time when you resume the chronometer
            simpleChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
            simpleChronometer.start();
            txt_pause_workout.setText("Pause Workout");
            fab_pause_workout.setImageResource(R.drawable.fab_pause_workout);
            isPaused = false;
        }
    }

    public void finishWorkout() {
        //minimises the floating action button
        toggleFabMode(fab_add);

        //General clean up tasks
        simpleChronometer.stop();

        //unbinds the service if bound
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
        stopService(serviceIntent);

        // Get the selected exercise IDs
        ArrayList<String> selectedExerciseIds = getIntent().getStringArrayListExtra("selected_exercise_ids");
        if (selectedExerciseIds == null || selectedExerciseIds.isEmpty()) {
            // No exercises to record, just show summary dialog
            showWorkoutSummaryDialog();
            return;
        }

        //Works out how many seconds have elapsed. It records it in milliseconds so we divide by 1000 to convert it to seconds
        long workoutDuration = (SystemClock.elapsedRealtime() - simpleChronometer.getBase()) / 1000;

        try {
            // Update duration for each log ID that was created during this workout
            for (String logId : workoutLogIds) {
                Log.d("FinishWorkout", "Updating duration for log ID: " + logId);
                dbManager.recordExerciseLogDuration(logId, workoutDuration);
            }
        } catch (Exception e) {
            Log.e("StartWorkoutActivity", "Error recording workout duration: " + e.getMessage());
        }

        //Shows the EndOfWorkoutDialog
        showWorkoutSummaryDialog();
    }

    private void showWorkoutSummaryDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_workout_summary);
        dialog.setCancelable(false);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // Initialize AdView and load ad
        AdView adView = dialog.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.findViewById(R.id.bt_ok).setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), MainActivityExerciseList.class);
            startActivity(i);
        });

        dialog.findViewById(R.id.bt_close).setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), MainActivityExerciseList.class);
            startActivity(i);
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }


    // ViewHolder for AdView
    public static class AdViewHolder extends RecyclerView.ViewHolder {
        final AdView adView;

        AdViewHolder(View itemView) {
            super(itemView);
            adView = itemView.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }
}