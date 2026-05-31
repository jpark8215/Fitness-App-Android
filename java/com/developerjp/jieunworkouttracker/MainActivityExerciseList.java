package com.developerjp.jieunworkouttracker;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
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
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivityExerciseList extends AppCompatActivity implements ExerciseRecyclerViewAdapter.OnItemLongSelectedListener, ExerciseRecyclerViewAdapter.OnButtonClickListener {

    // Item List
    private final List<ExerciseItem> exerciseItems = new ArrayList<>();
    private final List<ExerciseItem> displayExerciseItems = new ArrayList<>();
    private final NumberFormat nf = new DecimalFormat("##.#");
    private DBManager dbManager;
    private RecyclerView recyclerView;
    // Custom Recycler View Adaptor
    private ExerciseRecyclerViewAdapter adapter;
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
        
        // Clear any lingering notifications when returning to main activity
        ServiceUtils.clearAllNotifications(this);
        
        // Refresh data when returning to this activity
        if (dbManager == null || dbManager.isOpen()) {
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

        // Set up predictive back gesture support
        setupBackCallback();
    }

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


        // Initialize the database
        dbManager = new DBManager(this);
        dbManager.open();

        EditText searchBar = findViewById(R.id.search_bar);
        if (searchBar != null) {
            searchBar.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterExerciseList(s.toString());
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize FAB
        fab_add = findViewById(R.id.fab_add);

        // Display instructions for selecting exercises
        TextView empty = findViewById(R.id.empty);
        if (empty != null) {
            empty.setText("Tap on exercises to select them, then use the 'Workout' tab button to begin your workout.");
        }

        // Set up click listener
        if (fab_add != null) {
            fab_add.setOnClickListener(v -> showCustomAddDialog());
        }

        // Load exercises
        adapter = new ExerciseRecyclerViewAdapter(displayExerciseItems, this, this, this);
        recyclerView.setAdapter(adapter);
        loadExerciseData();
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("");

        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText("Exercises");

        //Hides the chronometer as we don't need it for this activity
        Chronometer simpleChronometer = findViewById(R.id.simpleChronometer);
        simpleChronometer.setVisibility(View.GONE);
    }

    private void initNavigationMenu() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_exercises);
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
                        // Pass workout data from WorkoutService
                        intent.putExtra("id", WorkoutService.getWorkoutId());
                        intent.putExtra("title", WorkoutService.getWorkoutTitle());
                        intent.putStringArrayListExtra("selected_exercise_ids", WorkoutService.getWorkoutExerciseIds());
                    } else {
                        // No ongoing workout, check if exercises are selected
                        List<String> selectedExerciseIds = adapter.getSelectedExerciseIds();
                        if (selectedExerciseIds.isEmpty()) {
                            // No exercises selected, tell user to select exercises first
                            Toast.makeText(this, "Please select exercises to start", Toast.LENGTH_SHORT).show();
                            return true;
                        } else {
                            // Start the selected exercises
                            if (dbManager != null) {
                                dbManager.startSelectedExercises(selectedExerciseIds);
                            }
                            intent = new Intent(this, StartWorkoutActivity.class);
                            intent.putStringArrayListExtra("selected_exercise_ids", new ArrayList<>(selectedExerciseIds));
                            // Clear selections after starting
                            if (adapter != null) {
                                adapter.clearSelections();
                            }
                        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
            dbManager = null;
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
        if (dbManager == null || dbManager.isOpen()) {
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

        EditText searchBar = findViewById(R.id.search_bar);
        String currentQuery = searchBar != null ? searchBar.getText().toString() : "";
        filterExerciseList(currentQuery);

        // Update the display based on current weight unit settings
        updateWeightDisplay();

        // Important: Don't close the database here, as it might be needed by other functions
        // We'll close it in onDestroy
    }

    private void filterExerciseList(String query) {
        displayExerciseItems.clear();
        if (TextUtils.isEmpty(query)) {
            displayExerciseItems.addAll(exerciseItems);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (ExerciseItem item : exerciseItems) {
                if (item.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                    displayExerciseItems.add(item);
                }
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        
        TextView empty = findViewById(R.id.empty);
        if (empty != null) {
            if (displayExerciseItems.isEmpty() && exerciseItems.isEmpty()) {
                empty.setVisibility(View.VISIBLE);
                empty.setText(R.string.empty_exercise_list_text);
            } else if (displayExerciseItems.isEmpty()) {
                empty.setVisibility(View.VISIBLE);
                empty.setText("No exercises found matching '" + query + "'");
            } else {
                empty.setVisibility(View.GONE);
            }
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
        dialog.setContentView(ThemeManager.isDarkModeEnabled(this) ? R.layout.dialog_add_dark : R.layout.dialog_add_light);
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

        // Set toggle state based on system preference
        boolean isKgUnit = WeightUnitManager.isKgUnit(this);
        weightUnitToggle.setChecked(isKgUnit);

        // Add toggle button listener
        weightUnitToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!TextUtils.isEmpty(weightInput.getText())) {
                try {
                    double currentWeight = Double.parseDouble(weightInput.getText().toString());
                    double convertedWeight = isChecked ?
                            WeightUtils.lbsToKg(currentWeight) :
                            WeightUtils.kgToLbs(currentWeight);
                    weightInput.setText(new DecimalFormat("#.#").format(convertedWeight));
                } catch (NumberFormatException e) {
                    Log.e("MainActivityExerciseList", "Invalid weight format: " + e.getMessage());
                }
            }
        });

        Button addButton = dialog.findViewById(R.id.btn_add);
        addButton.setOnClickListener(v -> {
            if (TextUtils.isEmpty(exerciseNameInput.getText())) {
                Toast.makeText(getApplicationContext(), "Exercise name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String exerciseName = exerciseNameInput.getText().toString();

            // Check if exercise already exists
            if (dbManager.doesExerciseExist(exerciseName)) {
                // Show themed alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this,
                        ThemeManager.isDarkModeEnabled(this) ? R.style.ModernAlertDialogDark : R.style.ModernAlertDialog);
                builder.setTitle("Duplicate Exercise");
                builder.setMessage("An exercise with this name already exists. \nPlease choose a different name.");
                builder.setPositiveButton("OK", (dialog1, which) -> {
                    // Do nothing, just dismiss the dialog
                });
                builder.create().show();
                return;
            }

            // Get the weight if provided
            Double weight = null;
            if (!TextUtils.isEmpty(weightInput.getText())) {
                try {
                    weight = Double.parseDouble(weightInput.getText().toString());

                    // Convert to kg if needed (if toggle is set to lbs)
                    if (!weightUnitToggle.isChecked()) {
                        // Convert lbs to kg using the utility method
                        weight = WeightUtils.lbsToKg(weight);
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

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }


    private void showCustomModifyDialog(final String itemId, String itemTitle, Double itemWeight) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(ThemeManager.isDarkModeEnabled(this) ? R.layout.dialog_modify_dark : R.layout.dialog_modify_light);
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
        final ToggleButton toggleWeightUnit = dialog.findViewById(R.id.toggle_weight_unit);

        // Set toggle state based on system preference
        boolean isKgUnit = WeightUnitManager.isKgUnit(this);
        toggleWeightUnit.setChecked(isKgUnit);

        // Convert and display weight in the selected unit
        if (itemWeight != null) {
            double displayWeight = isKgUnit ? itemWeight : WeightUtils.kgToLbs(itemWeight);
            weightInput.setText(new DecimalFormat("#.#").format(displayWeight));
        }

        Button modifyButton = dialog.findViewById(R.id.btn_update);
        modifyButton.setOnClickListener(v -> {
            if (TextUtils.isEmpty(exerciseNameInput.getText())) {
                Toast.makeText(getApplicationContext(), "Exercise name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the updated exercise name
            String newTitle = exerciseNameInput.getText().toString();

            // Check if the new name is different from the current name
            if (!newTitle.equals(itemTitle)) {
                // Check if the new name already exists
                if (dbManager.doesExerciseExist(newTitle)) {
                    // Show themed alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(this,
                            ThemeManager.isDarkModeEnabled(this) ? R.style.ModernAlertDialogDark : R.style.ModernAlertDialog);
                    builder.setTitle("Duplicate Exercise");
                    builder.setMessage("An exercise with this name already exists. \nPlease choose a different name.");
                    builder.setPositiveButton("OK", (dialog1, which) -> {
                        // Do nothing, just dismiss the dialog
                    });
                    builder.create().show();
                    return;
                }
            }

            // Get the updated weight
            Double newWeight = null;
            if (weightInput != null && !TextUtils.isEmpty(weightInput.getText())) {
                try {
                    double inputWeight = Double.parseDouble(weightInput.getText().toString());
                    // Convert to kg for storage if currently showing lbs
                    newWeight = toggleWeightUnit.isChecked() ? inputWeight : WeightUtils.lbsToKg(inputWeight);
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

        // Add toggle button listener
        toggleWeightUnit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (weightInput != null && !TextUtils.isEmpty(weightInput.getText())) {
                try {
                    double currentWeight = Double.parseDouble(weightInput.getText().toString());
                    double convertedWeight = isChecked ?
                            WeightUtils.lbsToKg(currentWeight) :
                            WeightUtils.kgToLbs(currentWeight);
                    weightInput.setText(new DecimalFormat("#.#").format(convertedWeight));
                } catch (NumberFormatException e) {
                    Log.e("MainActivityExerciseList", "Invalid weight format: " + e.getMessage());
                }
            }
        });

        Button deleteButton = dialog.findViewById(R.id.btn_delete);
        deleteButton.setOnClickListener(v -> {
            // Confirm deletion
            AlertDialog.Builder builder = new AlertDialog.Builder(this,
                    ThemeManager.isDarkModeEnabled(this) ? R.style.ModernAlertDialogDark : R.style.ModernAlertDialog);
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

            // Create and show the AlertDialog
            builder.create().show();
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

        dialog.show();
        dialog.getWindow().setAttributes(lp);
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

    private void setupBackCallback() {
        // Handle back navigation with predictive back gesture support
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}