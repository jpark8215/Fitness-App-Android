package com.developerjp.jieunworkouttracker;


import static com.github.mikephil.charting.charts.Chart.LOG_TAG;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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

public class ArchivedExerciseList extends AppCompatActivity implements ExerciseRecyclerViewAdapter.OnItemLongSelectedListener, ExerciseRecyclerViewAdapter.OnButtonClickListener  {

    // Item List
    private final List<ExerciseItem> ExerciseItem = new ArrayList<>();
    private final NumberFormat nf = new DecimalFormat("##.##");
    private DBManager dbManager;
    private RecyclerView recyclerView;
    private ExerciseRecyclerViewAdapter adapter;
    private FloatingActionButton fab_add;
    private Parcelable recyclerViewState;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_drawer_simple_light);

        //Use view stubs to programmatically change the include view at runtime
        ViewStub stub = findViewById(R.id.main_view_stub);
        stub.setLayoutResource(R.layout.activity_archived_exercise_list);
        View inflatedView = stub.inflate();
        
        // Make sure the layout is visible
        if (inflatedView != null) {
            inflatedView.setVisibility(View.VISIBLE);
        }

        //Sets up the toolbar and navigation menu
        initToolbar();
        initNavigationMenu();

        // Initialize database
        dbManager = new DBManager(this);
        dbManager.open();
        
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        // Set up restore button
        fab_add = findViewById(R.id.fab_add);
        if (fab_add != null) {
            fab_add.setOnClickListener(this::restoreExercise);
        }

        //Loads the Exercise logs data using recyclerview and the custom adapter
        loadExerciseData();
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle("");
        }

        TextView txtTitle = findViewById(R.id.txtTitle);
        if (txtTitle != null) {
            txtTitle.setText("Archived Exercises");
        }

        //Hides the chronometer as we don't need it for this activity
        Chronometer simpleChronometer = findViewById(R.id.simpleChronometer);
        if (simpleChronometer != null) {
            simpleChronometer.setVisibility(View.GONE);
        }
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
            String itemCLicked = item.getTitle().toString();
            Intent intent;

            switch (itemCLicked) {
                case "Exercises":
                    Log.d("menu item clicked", "Exercises");
                    //Navigate to the main exercise list
                    intent = new Intent(getApplicationContext(), MainActivityExerciseList.class);
                    startActivity(intent);
                    break;
                case "Archived Exercises":
                    Log.d("menu item clicked", "Archived Exercises");
                    //We're already in the archived exercises view
                    drawer.closeDrawers();
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
        // Initialize the database connection if needed
        if (dbManager == null || !dbManager.isOpen()) {
            dbManager = new DBManager(this);
            dbManager.open();
        }
        
        // Clear existing data
        ExerciseItem.clear();
        
        // Fetch archived exercises
        Cursor cursor = dbManager.fetchArchivedExercises();

        // Initialize the recycler view if it's not already initialized
        if (recyclerView == null) {
            recyclerView = findViewById(R.id.recycler_view);
            if (recyclerView != null) {
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
            }
        }

        // If the cursor has data, hide the empty text view
        if (cursor != null && cursor.getCount() > 0) {
            TextView empty = findViewById(R.id.empty);
            if (empty != null) {
                empty.setVisibility(View.GONE);
            }

            // Process the cursor data
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                ExerciseItem exerciseItem = new ExerciseItem();

                // Get the column indices
                int exerciseIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.EXERCISE_ID);
                int exerciseColumnIndex = cursor.getColumnIndex(DatabaseHelper.EXERCISE);
                int weightColumnIndex = cursor.getColumnIndex(DatabaseHelper.WEIGHT);

                // Set exercise properties
                if (exerciseIdColumnIndex != -1) {
                    exerciseItem.setId(cursor.getString(exerciseIdColumnIndex));
                }
                
                if (exerciseColumnIndex != -1) {
                    exerciseItem.setTitle(cursor.getString(exerciseColumnIndex));
                }
                
                if (weightColumnIndex != -1) {
                    exerciseItem.setWeight(cursor.getDouble(weightColumnIndex));
                }
                
                // Set default values for buttons
                exerciseItem.setButton1("5");
                exerciseItem.setButton2("5");
                exerciseItem.setButton3("5");
                exerciseItem.setButton4("5");
                exerciseItem.setButton5("5");
                
                // Set default colors
                exerciseItem.setButton1Colour(R.drawable.button_shape_default);
                exerciseItem.setButton2Colour(R.drawable.button_shape_default);
                exerciseItem.setButton3Colour(R.drawable.button_shape_default);
                exerciseItem.setButton4Colour(R.drawable.button_shape_default);
                exerciseItem.setButton5Colour(R.drawable.button_shape_default);

                // Add to the list
                ExerciseItem.add(exerciseItem);
            }
            
            // Close the cursor but NOT the database
            cursor.close();
        } else {
            // Show empty view if no data
            TextView empty = findViewById(R.id.empty);
            if (empty != null) {
                empty.setVisibility(View.VISIBLE);
                empty.setText("No archived exercises found");
            }
            
            // Close the cursor if it exists
            if (cursor != null) {
                cursor.close();
            }
        }
        
        // Create or update the adapter
        if (adapter == null) {
            adapter = new ExerciseRecyclerViewAdapter(ExerciseItem, this, this, this);
            if (recyclerView != null) {
                recyclerView.setAdapter(adapter);
            }
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Make sure database is open when returning to activity
        if (dbManager == null || !dbManager.isOpen()) {
            dbManager = new DBManager(this);
            dbManager.open();
        }
        loadExerciseData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save RecyclerView state
        if (recyclerView != null && recyclerView.getLayoutManager() != null) {
            recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database when activity is destroyed
        if (dbManager != null) {
            dbManager.close();
            dbManager = null;
        }
    }

    @Override
    public void OnBackPressedDispatcher() {
        //When the user clicks on the back button we want to take them back to the workout list page
        super.getOnBackPressedDispatcher();
        this.finish();
    }

    @Override
    public void onItemLongSelected(String itemId, String itemTitle, Double itemWeight) {
        modifyExercise(itemId, itemTitle, itemWeight);
    }

    @Override
    public void onButtonClick(String itemId, String itemTitle, String setSelected, Integer intReps) {
        // itemId is currently being stored as a string, convert it to an integer value
        int intItemId = Integer.parseInt(itemId);

        // Validation to make sure reps can never be less than zero
        if (intReps < 0) { intReps = 0; }

        // We pass through the itemId, set selected and number of reps
        dbManager.updateExerciseLogs(intItemId, setSelected, intReps);

        // Save state - used when user clicks on an item far down the recycler view list
        if (recyclerView != null && recyclerView.getLayoutManager() != null) {
            recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        }

        // Refresh the exercise list
        loadExerciseData();
        
        // Restore state
        if (recyclerViewState != null && recyclerView != null && recyclerView.getLayoutManager() != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        }
    }

    public void bottomNavigationHomeClick(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivityExerciseList.class);
        startActivity(intent);
    }

    public void bottomNavigationCalendarClick(View view) {
        Intent intent = new Intent(getApplicationContext(), ShowCalendarActivity.class);
        startActivity(intent);
    }

    private void restoreExercise(View v) {
        // Show dialog to select which exercise to restore
        if (ExerciseItem.isEmpty()) {
            Toast.makeText(this, "No archived exercises to restore", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create items array for the dialog
        final String[] items = new String[ExerciseItem.size()];
        final String[] ids = new String[ExerciseItem.size()];
        
        for (int i = 0; i < ExerciseItem.size(); i++) {
            items[i] = ExerciseItem.get(i).getTitle();
            ids[i] = ExerciseItem.get(i).getId();
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Exercise to Restore");
        builder.setItems(items, (dialog, which) -> {
            try {
                // Unarchive the selected exercise
                String exerciseId = ids[which];
                dbManager.unarchiveExercise(exerciseId);
                Toast.makeText(this, "Exercise '" + items[which] + "' restored", Toast.LENGTH_SHORT).show();
                
                // Refresh the exercise list
                loadExerciseData();
                
                // If no more archived exercises, show a message
                if (ExerciseItem.isEmpty()) {
                    TextView empty = findViewById(R.id.empty);
                    if (empty != null) {
                        empty.setVisibility(View.VISIBLE);
                        empty.setText("No archived exercises found");
                    }
                }
            } catch (Exception e) {
                Log.e("ArchivedExerciseList", "Error restoring exercise: " + e.getMessage());
                Toast.makeText(this, "Error restoring exercise", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void modifyExercise(String itemId, String itemTitle, Double itemWeight){
        showCustomModifyDialog(itemId, itemTitle, itemWeight);
    }

    private void showCustomAddDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_light);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        // Get references to dialog elements
        final EditText exerciseEditText = dialog.findViewById(R.id.name_edittext);
        final EditText weightEditText = dialog.findViewById(R.id.weight_edittext);
        final ToggleButton toggleWeightUnit = dialog.findViewById(R.id.toggle_weight_unit);

        TextView txtTitle = dialog.findViewById(R.id.txt_title);
        Button btnAdd = dialog.findViewById(R.id.btn_add);

        btnAdd.setText("Add Exercise");
        txtTitle.setText("Add an Exercise");
        exerciseEditText.setHint("Exercise");

        btnAdd.setOnClickListener(v -> {
            final String exerciseName = exerciseEditText.getText().toString();

            // Check if the exercise name is empty
            if (TextUtils.isEmpty(exerciseName)) {
                Toast.makeText(getApplicationContext(), "Please enter an exercise name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the weight if provided
            Double exerciseWeight = null;
            if (!TextUtils.isEmpty(weightEditText.getText())) {
                try {
                    exerciseWeight = Double.parseDouble(weightEditText.getText().toString());
                    
                    // Convert to kg if toggle is not checked (meaning it's in lbs)
                    if (toggleWeightUnit != null && !toggleWeightUnit.isChecked()) {
                        // Convert lbs to kg: kg = lbs * 0.453592
                        exerciseWeight = exerciseWeight * 0.453592;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Invalid weight format", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Insert the exercise directly
            dbManager.insertExerciseDirectly(exerciseName, exerciseWeight);
            
            // Refresh the exercise list
            loadExerciseData();
            
            dialog.dismiss();
            Toast.makeText(getApplicationContext(), "Exercise added", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void showCustomModifyDialog(final String itemId, String itemTitle, Double itemWeight) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_modify_light);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final EditText exerciseEditText = dialog.findViewById(R.id.name_edittext);
        final EditText weightEditText = dialog.findViewById(R.id.weight_edittext);
        final ToggleButton toggleWeightUnit = dialog.findViewById(R.id.toggle_weight_unit);

        TextView txtTitle  = dialog.findViewById(R.id.txt_title);
        Button btnUpdate = dialog.findViewById(R.id.btn_update);
        Button btnDelete = dialog.findViewById(R.id.btn_delete);
        Button btnArchive = dialog.findViewById(R.id.btn_archive);
        Button btnPlaceholder = dialog.findViewById(R.id.btn_placeholder);

        //Hides the archive and placeholder buttons
        btnArchive.setVisibility(View.GONE);
        btnPlaceholder.setVisibility(View.GONE);

        txtTitle.setText("Modify Exercise");
        exerciseEditText.setText(itemTitle);

        if (!toggleWeightUnit.isChecked()) {
            // Convert kg to lbs
            itemWeight = Double.parseDouble(nf.format(itemWeight / 2.20462));
        }
        weightEditText.setText(itemWeight.toString());

        //Sets the cursor position to the end of text, rather than at the start
        exerciseEditText.setSelection( exerciseEditText.getText().length());
        weightEditText.setSelection( weightEditText.getText().length());


        btnUpdate.setOnClickListener(v -> {
            if (TextUtils.isEmpty(exerciseEditText.getText())) {
                Toast.makeText(ArchivedExerciseList.this, "You must give an exercise name", Toast.LENGTH_LONG).show();
            } else {
                String newExerciseName = exerciseEditText.getText().toString();
                long _id = Long.parseLong(itemId);

                // Check for duplicate exercise name
                if (dbManager.doesExerciseExist(newExerciseName)) {
                    // Exercise with the same name already exists, ask the user what to do
                    AlertDialog.Builder builder = new AlertDialog.Builder(ArchivedExerciseList.this);
                    double mostRecentWeight = dbManager.getMostRecentWeightForExercise(newExerciseName);
                    String message = "The most recent weight was " + mostRecentWeight + ". \nStill update as you entered?";
                    builder.setMessage(message)
                            .setPositiveButton("Update", (dialogInterface, id) -> {
                                // Update exercise name
                                dbManager.updateExerciseName(_id, newExerciseName);

                                // If there is a weight given, update the database
                                if (weightEditText.getText().toString().trim().length() > 0) {
                                    double newExerciseWeight = Double.parseDouble(weightEditText.getText().toString());

                                    // Convert the entered weight to kg if lbs is selected
                                    if (!toggleWeightUnit.isChecked()) {
                                        newExerciseWeight = Double.parseDouble(nf.format(newExerciseWeight / 2.20462));
                                    }

                                    dbManager.updateExerciseWeight(_id, newExerciseWeight);
                                } else {
                                    // If no weight value was given, update with a default value of 0
                                    Double newExerciseWeight = 0.0;
                                    dbManager.updateExerciseWeight(_id, newExerciseWeight);
                                }

                                // Refresh the recycler view
                                ExerciseItem.clear();
                                loadExerciseData();
                                adapter.notifyDataSetChanged();

                                // Dismiss the main dialog
                                dialog.dismiss();
                                // Dismiss the inner dialog
                                dialogInterface.dismiss();
                            })

                            .setNegativeButton("Cancel", (dialog1, id) -> {
                                // User does not want to use the same name, do nothing
                                dialog1.dismiss();
                            });

                    AlertDialog dialog1 = builder.create();
                    dialog1.show();
                } else {
                    // No duplicate, proceed with the update
                    // Update exercise name
                    dbManager.updateExerciseName(_id, newExerciseName);

                    // If there is a weight given, update the database
                    if (weightEditText.getText().toString().trim().length() > 0) {
                        double newExerciseWeight = Double.parseDouble(weightEditText.getText().toString());

                        // Convert the entered weight to kg if lbs is selected
                        if (!toggleWeightUnit.isChecked()) {
                            newExerciseWeight = Double.parseDouble(nf.format(newExerciseWeight / 2.20462));
                        }

                        dbManager.updateExerciseWeight(_id, newExerciseWeight);
                    } else {
                        // If no weight value was given, update with a default value of 0
                        Double newExerciseWeight = 0.0;
                        dbManager.updateExerciseWeight(_id, newExerciseWeight);
                    }

                    // Refresh the recycler view
                    ExerciseItem.clear();
                    loadExerciseData();
                    adapter.notifyDataSetChanged();

                    dialog.dismiss();
                }
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
            loadExerciseData();
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
}