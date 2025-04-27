package com.developerjp.jieunworkouttracker;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivityExerciseList extends AppCompatActivity implements ExerciseRecyclerViewAdapter.OnItemLongSelectedListener, ExerciseRecyclerViewAdapter.OnButtonClickListener {

    // Item List
    private final List<ExerciseItem> exerciseItems = new ArrayList<>();
    private final NumberFormat nf = new DecimalFormat("##.#");
    private DBManager dbManager;
    private RecyclerView recyclerView;
    // Custom Recycler View Adaptor
    private ExerciseRecyclerViewAdapter adapter;
    private View back_drop;
    private boolean rotate = false;
    private View lyt_add_exercise;
    private View lyt_start_selected_exercises;
    private FloatingActionButton fab_add;
    private Parcelable recyclerViewState;
    private Toolbar toolbar;

    @Override
    protected void onPause() {
        super.onPause();
        if (recyclerView.getLayoutManager() != null) {
            recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        if (dbManager == null || !dbManager.isOpen()) {
            dbManager = new DBManager(this);
            dbManager.open();
        }
        
        // Get the current weight unit preference
        // Default to kg
        boolean isKgUnit = WeightUnitManager.isKgUnit(this);
        
        // Load exercise data from database
        loadExerciseData();
        
        // Update the display based on current weight unit settings
        updateWeightDisplay();

        if (recyclerViewState != null && recyclerView.getLayoutManager() != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize MobileAds
        MobileAds.initialize(this, initializationStatus -> {
        });

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
        View inflatedView = stub.inflate();

        // Make sure the layout is visible
        if (inflatedView != null) {
            inflatedView.setVisibility(View.VISIBLE);
        }

        //Sets up the toolbar and navigation menu
        initToolbar();
        initNavigationMenu();

        // Initialize UI components
        View parent_view = findViewById(android.R.id.content);
        back_drop = findViewById(R.id.back_drop);
        lyt_add_exercise = findViewById(R.id.lyt_add_exercise);
        lyt_start_selected_exercises = findViewById(R.id.lyt_start_selected_exercises);

        // Make sure the back_drop is initially hidden
        if (back_drop != null) {
            back_drop.setVisibility(View.GONE);
        }

        // Initialize the add button layouts initially to GONE
        if (lyt_add_exercise != null) {
            lyt_add_exercise.setVisibility(View.GONE);
        }

        if (lyt_start_selected_exercises != null) {
            lyt_start_selected_exercises.setVisibility(View.GONE);
        }

        // Initialize the database
        dbManager = new DBManager(this);
        dbManager.open();

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize CardViews and FABs
        fab_add = findViewById(R.id.fab_add);
        CardView cv_add_exercise = findViewById(R.id.cv_add_exercise);
        FloatingActionButton fab_add_exercise = findViewById(R.id.fab_add_exercise);
        CardView cv_start_selected = findViewById(R.id.cv_start_selected);
        FloatingActionButton fab_start_selected = findViewById(R.id.fab_start_selected);

        // Display instructions for selecting exercises
        TextView empty = findViewById(R.id.empty);
        if (empty != null) {
            empty.setText("Tap on exercises to select them, then use the 'Start Selected Exercises' button to begin your workout.");
        }

        // Make sure animations are properly initialized
        if (lyt_add_exercise != null) {
            ViewAnimation.initShowOut(lyt_add_exercise);
        }

        if (lyt_start_selected_exercises != null) {
            ViewAnimation.initShowOut(lyt_start_selected_exercises);
        }

        // Set up click listeners
        if (fab_add != null) {
            fab_add.setOnClickListener(v -> toggleFabMenu());
        }

        if (cv_add_exercise != null) {
            cv_add_exercise.setOnClickListener(v -> showCustomAddDialog());
        }
        if (fab_add_exercise != null) {
            fab_add_exercise.setOnClickListener(v -> showCustomAddDialog());
        }
        if (cv_start_selected != null) {
            cv_start_selected.setOnClickListener(this::startSelectedExercises);
        }
        if (fab_start_selected != null) {
            fab_start_selected.setOnClickListener(this::startSelectedExercises);
        }

        // Load exercises
        adapter = new ExerciseRecyclerViewAdapter(exerciseItems, this, this, this);
        recyclerView.setAdapter(adapter);
        loadExerciseData();
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
        txtTitle.setText("Exercises");

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
                case "Exercises":
                    Log.d("menu item clicked", "Exercises");
                    //We're already in the exercises view
                    drawer.closeDrawers();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
            dbManager = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (rotate) {
            // If the FAB menu is open, close it
            toggleFabMenu();
        } else {
            // Otherwise, finish the activity
            super.onBackPressed();
        }
    }

    public void onItemSelected(String itemId, String itemTitle) {
        // Handle when an item is selected (not long-selected)
        toggleSelection(itemId);
    }

    @Override
    public void onItemLongSelected(String itemId, String itemTitle, Double itemWeight) {
        showCustomModifyDialog(itemId, itemTitle, itemWeight);
    }

    @Override
    public void OnBackPressedDispatcher() {

    }

    @Override
    public void onButtonClick(String itemId, String itemTitle, String setSelected, Integer intReps) {
        // Implement button click handling for exercise set buttons
        Log.d("Exercise", "Button clicked: " + setSelected + " for exercise " + itemTitle);
        // Update the database with the new set information
        dbManager.updateExerciseSet(itemId, setSelected, intReps);
        // Refresh the exercise data
        loadExerciseData();
    }

    public void bottomNavigationHomeClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivityExerciseList.class);
        startActivity(intent);
    }

    public void bottomNavigationCalendarClick(View view) {
        Intent intent = new Intent(getApplicationContext(), ShowCalendarActivity.class);
        startActivity(intent);
    }

    private void loadExerciseData() {
        // Make sure we have a valid database connection
        if (dbManager == null || !dbManager.isOpen()) {
            dbManager = new DBManager(this);
            dbManager.open();
        }

        // Clear existing items
        exerciseItems.clear();

        // Get all unarchived exercises - this way archived ones won't show in the main list
        try (Cursor cursor = dbManager.fetchUnarchivedExercises()) {
            //If the cursor has a value in it then hide the empty textview
            if (cursor != null && cursor.getCount() > 0) {
                TextView empty = findViewById(R.id.empty);
                if (empty != null) {
                    empty.setVisibility(View.GONE);
                }

                // Iterate through the cursor and populate the list
                cursor.moveToFirst(); // Move to the first row

                while (!cursor.isAfterLast()) {
                    ExerciseItem item = new ExerciseItem();

                    // Get column indices
                    int exerciseIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.EXERCISE_ID);
                    int exerciseColumnIndex = cursor.getColumnIndex(DatabaseHelper.EXERCISE);
                    int weightColumnIndex = cursor.getColumnIndex(DatabaseHelper.WEIGHT);

                    if (exerciseIdColumnIndex != -1 && exerciseColumnIndex != -1) {
                        // Retrieve values from the cursor
                        String exerciseId = cursor.getString(exerciseIdColumnIndex);
                        String exercise = cursor.getString(exerciseColumnIndex);
                        double weight = 0;

                        if (weightColumnIndex != -1) {
                            weight = cursor.getDouble(weightColumnIndex);
                        }

                        // Set values to the item
                        item.setId(exerciseId);
                        item.setTitle(exercise);
                        item.setWeight(weight);

                        // Set default reps
                        item.setButton1("5");
                        item.setButton2("5");
                        item.setButton3("5");
                        item.setButton4("5");
                        item.setButton5("5");

                        // Set default colors
                        item.setButton1Colour(R.drawable.button_shape_default);
                        item.setButton2Colour(R.drawable.button_shape_default);
                        item.setButton3Colour(R.drawable.button_shape_default);
                        item.setButton4Colour(R.drawable.button_shape_default);
                        item.setButton5Colour(R.drawable.button_shape_default);

                        // Add the item to the list
                        exerciseItems.add(item);
                    }

                    cursor.moveToNext(); // Move to the next row
                }
            } else {
                // Show empty message if no exercises found
                TextView empty = findViewById(R.id.empty);
                if (empty != null) {
                    empty.setVisibility(View.VISIBLE);
                    empty.setText(R.string.empty_exercise_list_text);
                }
            }
        }
        // Always close cursor when done

        // Notify adapter of data changes if it exists
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        // Important: Don't close the database here, as it might be needed by other functions
        // We'll close it in onDestroy
    }

    private void startSelectedExercises(View v) {
        // Get selected exercises
        List<String> selectedExerciseIds = adapter.getSelectedExerciseIds();

        if (selectedExerciseIds.isEmpty()) {
            Toast.makeText(this, "Please select at least one exercise to start", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start the selected exercises
        if (dbManager != null) {
            dbManager.startSelectedExercises(selectedExerciseIds);
        }

        // Navigate to StartWorkoutActivity with the selected exercises
        Intent intent = new Intent(getApplicationContext(), StartWorkoutActivity.class);
        intent.putStringArrayListExtra("selected_exercise_ids", new ArrayList<>(selectedExerciseIds));
        startActivity(intent);

        // Clear selections after starting
        if (adapter != null) {
            adapter.clearSelections();
        }
    }

    /**
     * Toggle selection of an exercise in the list
     *
     * @param itemId ID of the exercise to toggle selection
     */
    private void toggleSelection(String itemId) {
        // Use adapter's method to toggle selection
        adapter.toggleSelection(itemId);
    }

    private void showCustomAddDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_light);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(Objects.requireNonNull(dialog.getWindow()).getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        // Get references to dialog elements
        final TextView dialogTitle = dialog.findViewById(R.id.txt_title);
        dialogTitle.setText("Add Exercise");

        // Get reference to exercise name input
        final EditText exerciseNameInput = dialog.findViewById(R.id.name_edittext);
        // Clear any previous text
        exerciseNameInput.setText("");

        // Get reference to weight input
        final EditText weightInput = dialog.findViewById(R.id.weight_edittext);
        weightInput.setText("");

        // Get reference to weight unit toggle
        final ToggleButton weightUnitToggle = dialog.findViewById(R.id.toggle_weight_unit);

        Button addButton = dialog.findViewById(R.id.btn_add);
        addButton.setOnClickListener(v -> {
            if (TextUtils.isEmpty(exerciseNameInput.getText())) {
                Toast.makeText(getApplicationContext(), "Exercise name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String exerciseName = exerciseNameInput.getText().toString();

            // Get the weight if provided
            Double weight = null;
            if (!TextUtils.isEmpty(weightInput.getText())) {
                try {
                    weight = Double.parseDouble(weightInput.getText().toString());

                    // Convert to kg if needed (if toggle is set to lbs)
                    if (weightUnitToggle != null && !weightUnitToggle.isChecked()) {
                        // Convert lbs to kg: kg = lbs * 0.453592
                        weight = weight * 0.453592;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Invalid weight format", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Insert the exercise directly
            dbManager.insertExerciseDirectly(exerciseName, weight);

            // Refresh the exercise list
            exerciseItems.clear();
            loadExerciseData();
            adapter.notifyDataSetChanged();

            dialog.dismiss();
            Toast.makeText(getApplicationContext(), "Exercise added", Toast.LENGTH_SHORT).show();
        });

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.modern_dialog_background);
        dialog.show();
        dialog.getWindow().setAttributes(lp);
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

        final TextView dialogTitle = dialog.findViewById(R.id.txt_title);
        dialogTitle.setText("Modify Exercise");

        final EditText exerciseNameInput = dialog.findViewById(R.id.name_edittext);
        exerciseNameInput.setText(itemTitle);

        final EditText weightInput = dialog.findViewById(R.id.weight_edittext);
        if (itemWeight != null) {
            weightInput.setText(String.valueOf(itemWeight));
        }

        Button modifyButton = dialog.findViewById(R.id.btn_update);
        modifyButton.setOnClickListener(v -> {
            if (TextUtils.isEmpty(exerciseNameInput.getText())) {
                Toast.makeText(getApplicationContext(), "Exercise name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the updated exercise name
            String newTitle = exerciseNameInput.getText().toString();

            // Get the updated weight
            Double newWeight = null;
            if (weightInput != null && !TextUtils.isEmpty(weightInput.getText())) {
                try {
                    newWeight = Double.parseDouble(weightInput.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Invalid weight format", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Update the exercise in the database
            dbManager.updateExercise(itemId, newTitle, newWeight);

            // Refresh the exercise list
            loadExerciseData();

            dialog.dismiss();
            Toast.makeText(getApplicationContext(), "Exercise updated", Toast.LENGTH_SHORT).show();
        });

        Button deleteButton = dialog.findViewById(R.id.btn_delete);
        deleteButton.setOnClickListener(v -> {
            // Confirm deletion
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete Exercise");
            builder.setMessage("Are you sure you want to delete this exercise?");
            builder.setPositiveButton("Yes", (dialog1, which) -> {
                // Delete the exercise
                dbManager.deleteExercise(itemId);
                loadExerciseData();
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "Exercise deleted", Toast.LENGTH_SHORT).show();
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
        });

        // Add archive button functionality
        Button archiveButton = dialog.findViewById(R.id.btn_archive);
        if (archiveButton != null) {
            archiveButton.setOnClickListener(v -> {
                // Archive the exercise
                dbManager.archiveExercise(itemId);
                loadExerciseData();
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "Exercise archived", Toast.LENGTH_SHORT).show();
            });
        }

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.modern_dialog_background);
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void toggleFabMenu() {
        rotate = !rotate;

        // If we're showing the menu, make sure the backdrop is visible
        if (rotate) {
            if (back_drop != null) {
                back_drop.setVisibility(View.VISIBLE);
            }

            // Show the option layouts with animation
            if (lyt_add_exercise != null) {
                ViewAnimation.showIn(lyt_add_exercise);
            }
            if (lyt_start_selected_exercises != null) {
                ViewAnimation.showIn(lyt_start_selected_exercises);
            }

            // Rotate the FAB button
            ViewAnimation.rotateForward(fab_add);
        } else {
            // Hide the option layouts with animation
            if (lyt_add_exercise != null) {
                ViewAnimation.showOut(lyt_add_exercise);
            }
            if (lyt_start_selected_exercises != null) {
                ViewAnimation.showOut(lyt_start_selected_exercises);
            }

            // Rotate the FAB button back
            ViewAnimation.rotateBackward(fab_add);

            // Hide the backdrop after a delay
            new Handler().postDelayed(() -> {
                if (back_drop != null) {
                    back_drop.setVisibility(View.GONE);
                }
            }, 300);
        }

        // Make the menu items clickable
        if (back_drop != null) {
            back_drop.setOnClickListener(v -> toggleFabMenu());
        }
    }

    private void updateWeightDisplay() {
        if (exerciseItems != null && adapter != null) {
            boolean isKgUnit = WeightUnitManager.isKgUnit(this);
            for (ExerciseItem exercise : exerciseItems) {
                double weight = exercise.getWeight();
                String formattedWeight;
                
                if (isKgUnit) {
                    // Already in kg, just format it
                    formattedWeight = WeightUtils.formatWeight(weight, true);
                } else {
                    // Convert to lbs and format
                    double weightInLbs = WeightUtils.kgToLbs(weight);
                    formattedWeight = WeightUtils.formatWeight(weightInLbs, false);
                }
                
                exercise.setDisplayWeight(formattedWeight);
            }
            adapter.notifyDataSetChanged();
        }
    }
}