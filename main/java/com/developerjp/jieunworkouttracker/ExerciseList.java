package com.developerjp.jieunworkouttracker;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
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

import androidx.annotation.NonNull;
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

public class ExerciseList extends AppCompatActivity implements ExerciseRecyclerViewAdapter.OnItemLongSelectedListener, ExerciseRecyclerViewAdapter.OnButtonClickListener {

    //Public variables which are used across classes/voids
    public String id;
    public String title;
    private DBManager dbManager;
    private RecyclerView recyclerView;
    // Item List
    private final List<ExerciseItem> ExerciseItem = new ArrayList<>();
    // Custom Recycler View Adaptor
    private ExerciseRecyclerViewAdapter adapter;
    private ToggleButton toggleWeightUnit;

    private final NumberFormat nf = new DecimalFormat("##.#");
    private View back_drop;
    private boolean rotate = false;
    private View lyt_add_exercise;
    private View lyt_start_workout;
    private FloatingActionButton fab_add;
    private Parcelable recyclerViewState;
    private String strNumberOfExercises;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        //Sets up the toolbar and navigation menu
        initToolbar();
        initNavigationMenu();


        View parent_view = findViewById(android.R.id.content);
        back_drop = findViewById(R.id.back_drop);
        lyt_add_exercise = findViewById(R.id.lyt_add_exercise);
        lyt_start_workout = findViewById(R.id.lyt_start_workout);


        //Loads the Exercise logs data using recyclerview and the custom adapter
        loadExerciseData();

        fab_add = findViewById(R.id.fab_add);
        FloatingActionButton fab_add_exercise = findViewById(R.id.fab_add_exercise);
        FloatingActionButton fab_start_workout = findViewById(R.id.fab_start_workout);
        CardView cv_add_exercise = findViewById(R.id.cv_add_exercise);
        CardView cv_start_workout = findViewById(R.id.cv_start_workout);

        back_drop.setVisibility(View.GONE);
        ViewAnimation.initShowOut(lyt_add_exercise);
        ViewAnimation.initShowOut(lyt_start_workout);

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFabMode(v);
            }
        });

        back_drop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFabMode(fab_add);
            }
        });

        fab_add_exercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addExercise(v);
            }
        });


        cv_add_exercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addExercise(v);
            }
        });

        fab_start_workout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startWorkout(v);
            }
        });

        cv_start_workout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWorkout(v);
            }
        });

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
        txtTitle.setText(title);

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
//        drawer.setDrawerListener(toggle);
        drawer.addDrawerListener(toggle);

        toggle.syncState();

//          open drawer at start
//        drawer.openDrawer(GravityCompat.START);

        //Handles side navigation menu clicks
        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
                String itemCLicked = item.getTitle().toString();
                Intent intent;

                switch (itemCLicked) {

                    case "Workouts":
                        Log.d("menu item clicked", "Workouts");
                        //Starts the MainActivityWorkout activity
                        intent = new Intent(getApplicationContext(), MainActivityWorkoutList.class);
                        startActivity(intent);
                        break;
                    case "Archived":
                        Log.d("menu item clicked", "Archived");
                        intent = new Intent(getApplicationContext(), ArchivedWorkoutList.class);
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
                    case "Settings":
                        Log.d("menu item clicked", "Settings");
                        //Do something
                        //TODO Create Settings Page
                        Toast.makeText(getApplicationContext(), "Coming Soon", Toast.LENGTH_LONG).show();
                        break;
                    case "About":
                        Log.d("menu item clicked", "About");
                        //Do something
                        //TODO Create About Page
                        Toast.makeText(getApplicationContext(), "Coming Soon", Toast.LENGTH_LONG).show();
                        break;
                }

                drawer.closeDrawers();
                return true;
            }
        });
    }


    public void loadExerciseData() {
        //We pass the database manager the id AND title variable in case the user has entered in two workouts which
        //have the same name. We obviously only want to return the one they clicked on rather than everything
        //with that duplicate workout name
        dbManager = new DBManager(this);
        dbManager.open();

        strNumberOfExercises = dbManager.countExercises(id);
        Log.d("countExercises Value", strNumberOfExercises);

        Cursor cursor = dbManager.fetchExerciseLogs(id, strNumberOfExercises);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //If the cursor has a value in it then hide the empty textview
        //In English. If there is a workout returned, then remove the text saying no workouts found
        if (cursor.getCount() > 0) {
            TextView empty = findViewById(R.id.empty);
            empty.setVisibility(View.GONE);
        }

        // Ensure the cursor is not null and contains data
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ExerciseItem exerciseItem = new ExerciseItem();

                // Retrieve column indexes for better code readability
                int logIdIndex = cursor.getColumnIndex("log_id");
                int exerciseIndex = cursor.getColumnIndex("exercise");
                int set1Index = cursor.getColumnIndex("set1");
                int set2Index = cursor.getColumnIndex("set2");
                int set3Index = cursor.getColumnIndex("set3");
                int set4Index = cursor.getColumnIndex("set4");
                int set5Index = cursor.getColumnIndex("set5");
                int weightIndex = cursor.getColumnIndex("weight");

                // Populate exerciseItem properties using the retrieved data
                exerciseItem.setId(cursor.getString(logIdIndex));
                exerciseItem.setTitle(cursor.getString(exerciseIndex));
                exerciseItem.setButton1(cursor.getString(set1Index));
                exerciseItem.setButton2(cursor.getString(set2Index));
                exerciseItem.setButton3(cursor.getString(set3Index));
                exerciseItem.setButton4(cursor.getString(set4Index));
                exerciseItem.setButton5(cursor.getString(set5Index));

                // Sets all of the buttons to the default colour
                exerciseItem.setButton1Colour(R.drawable.button_shape_default);
                exerciseItem.setButton2Colour(R.drawable.button_shape_default);
                exerciseItem.setButton3Colour(R.drawable.button_shape_default);
                exerciseItem.setButton4Colour(R.drawable.button_shape_default);
                exerciseItem.setButton5Colour(R.drawable.button_shape_default);

                // Retrieve and set the weight
                Double exerciseWeight = cursor.getDouble(weightIndex);
                exerciseItem.setWeight(exerciseWeight);

                // Add the populated ExerciseItem to the list
                ExerciseItem.add(exerciseItem);
            } while (cursor.moveToNext());

            // Close the cursor when done
            cursor.close();
        }

        adapter = new ExerciseRecyclerViewAdapter(ExerciseItem, this, this, this);
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbManager.close();
    }

    @Override
    public void onBackPressed() {
        //When the user clicks on the back button we want to take them back to the workout list page
        super.onBackPressed();
        this.finish();
    }

    @Override
    public void onItemLongSelected(String itemId, String itemTitle, Double itemWeight) {
        modifyExercise(itemId, itemTitle, itemWeight);
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
        ExerciseItem.clear();
        loadExerciseData();
        adapter.notifyDataSetChanged();

        // Restore state
        // Once the data is re-loaded we load the same state or position
        // This stops the recycler view of scrolling all the way back to the top when a button is clicked
        Objects.requireNonNull(recyclerView.getLayoutManager()).onRestoreInstanceState(recyclerViewState);
    }

    @Override
    public void onButtonClick(String itemId, String itemTitle, String setSelected, Integer intReps) {
        //itemId is currently being stored as a string, covert it to an integer value
        int intItemId = Integer.parseInt(itemId);

        //Validation to make sure reps can never be less than zero
        if (intReps < 0) {
            intReps = 0;
        }

        //We pass through the itemId, set selected and number of reps
        dbManager.updateExerciseLogs(intItemId, setSelected, intReps);

        // Save state - used when user clicks on an item far down the recycler view list
        // It remembers the state or position
        recyclerViewState = Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState();

        //Triggers the refresh of data in the recyclerview
        //Clears what's currently in the view, loads the new data, refreshes the recyclerview
        ExerciseItem.clear();
        loadExerciseData();
        adapter.notifyDataSetChanged();

        // Restore state
        // Once the data is re-loaded we load the same state or position
        // This stops the recycler view of scrolling all the way back to the top when a button is clicked
        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
    }


    public void bottomNavigationHomeClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivityWorkoutList.class);
        startActivity(intent);
    }

    public void bottomNavigationCalendarClick(View view) {
        Intent intent = new Intent(getApplicationContext(), ShowCalendarActivity.class);
        startActivity(intent);
    }

    private void toggleFabMode(View v) {
        rotate = ViewAnimation.rotateFab(v, !rotate);
        if (rotate) {
            ViewAnimation.showIn(lyt_add_exercise);
            ViewAnimation.showIn(lyt_start_workout);
            back_drop.setVisibility(View.VISIBLE);
        } else {
            ViewAnimation.showOut(lyt_add_exercise);
            ViewAnimation.showOut(lyt_start_workout);
            back_drop.setVisibility(View.GONE);
        }
    }

    private void addExercise(View v) {
        //minimises the floating action button
        toggleFabMode(fab_add);
        showCustomAddDialog();
    }

    private void modifyExercise(String itemId, String itemTitle, Double itemWeight) {
        showCustomModifyDialog(itemId, itemTitle, itemWeight);
    }

    private void startWorkout(View v) {
        //minimises the floating action button
        toggleFabMode(fab_add);

        //Inserts a new exercise log for our workout that we are about to begin
        dbManager.insertExerciseLogs(id, strNumberOfExercises);

        //Passes through the workout title and id
        //Starts the Startworkoutactivity class
        Intent modify_intent = new Intent(v.getContext(), StartWorkoutActivity.class);
        modify_intent.putExtra("id", id);
        modify_intent.putExtra("title", title);
        startActivity(modify_intent);
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

        final EditText exerciseEditText = dialog.findViewById(R.id.name_edittext);
        final EditText weightEditText = dialog.findViewById(R.id.weight_edittext);
        final ToggleButton toggleWeightUnit = dialog.findViewById(R.id.toggle_weight_unit);

        TextView txtTitle = dialog.findViewById(R.id.txt_title);
        Button btnAdd = dialog.findViewById(R.id.btn_add);

        btnAdd.setText("Add Exercise");
        txtTitle.setText("Add an Exercise");
        exerciseEditText.setHint("Exercise");

        btnAdd.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                final String exerciseName = exerciseEditText.getText().toString();

                // Check if the exercise name already exists in the database
                if (dbManager.doesExerciseExist(exerciseName)) {
                    // Exercise with the same name exists, ask the user if they want to proceed
                    AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseList.this);
                    builder.setTitle("Exercise Already Exists");
                    builder.setMessage("An exercise with the same name already exists. By clicking Add, you will have two exercises with the same name. Still Add?");
                    builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User wants to use the same exercise, so directly add it
                            double exerciseWeight = 0.0;
                            if (weightEditText.getText().toString().trim().length() > 0) {
                                exerciseWeight = Double.parseDouble(weightEditText.getText().toString());
                                if (!toggleWeightUnit.isChecked()) {
                                    exerciseWeight = Double.parseDouble(nf.format(exerciseWeight / 2.20462));
                                }
                            }
                            Intent intent = getIntent();
                            dbManager.insertExercise(intent.getStringExtra("id"), exerciseName, exerciseWeight);
                            Intent main = new Intent(v.getContext(), ExerciseList.class);
                            main.putExtra("title", intent.getStringExtra("title"));
                            main.putExtra("id", intent.getStringExtra("id"));
                            main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(main);
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("No", null);
                    builder.show();
                } else {
                    // Exercise name doesn't exist, proceed with adding it
                    double exerciseWeight = 0.0;
                    if (weightEditText.getText().toString().trim().length() > 0) {
                        exerciseWeight = Double.parseDouble(weightEditText.getText().toString());
                        if (!toggleWeightUnit.isChecked()) {
                            exerciseWeight = Double.parseDouble(nf.format(exerciseWeight / 2.20462));
                        }
                    }
                    Intent intent = getIntent();
                    dbManager.insertExercise(intent.getStringExtra("id"), exerciseName, exerciseWeight);
                    Intent main = new Intent(v.getContext(), ExerciseList.class);
                    main.putExtra("title", intent.getStringExtra("title"));
                    main.putExtra("id", intent.getStringExtra("id"));
                    main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(main);
                    dialog.dismiss();
                }
            }
//            @Override
//            public void onClick(View v) {
//
//
//                if (TextUtils.isEmpty(exerciseEditText.getText())) {
//                    Toast.makeText(ExerciseList.this,
//                            "You must give an exercise name", Toast.LENGTH_LONG).show();
//                } else {
//                    final String exerciseName = exerciseEditText.getText().toString();
//                    Double exerciseWeight = 0.0;
//
//
//                    // Edit text field only accepts numbers
//                    // Check if weight is provided and convert if lbs is selected
//                    // Update the weightTextView based on toggleWeightUnit state
//
//                    if (weightEditText.getText().toString().trim().length() > 0) {
//                        exerciseWeight = Double.parseDouble(weightEditText.getText().toString());
//
//                        // Convert kilograms to pounds if lbs is selected
//                        if (!toggleWeightUnit.isChecked()) {
//                            exerciseWeight = Double.parseDouble(nf.format(exerciseWeight / 2.20462));
//                        }
//                    }
//
//
//                    Intent intent = getIntent();
//                    dbManager.insertExercise(intent.getStringExtra("id"), exerciseName, exerciseWeight);
//                    Intent main = new Intent(v.getContext(), ExerciseList.class);
//                    main.putExtra("title", intent.getStringExtra("title"));
//                    main.putExtra("id", intent.getStringExtra("id"));
//                    main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(main);
//
//                    dialog.dismiss();
//                }
//            }
//        });

        });

        (dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
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

        TextView txtTitle = dialog.findViewById(R.id.txt_title);
        Button btnUpdate = dialog.findViewById(R.id.btn_update);
        Button btnDelete = dialog.findViewById(R.id.btn_delete);
        Button btnArchive = dialog.findViewById(R.id.btn_archive);
        Button btnPlaceholder = dialog.findViewById(R.id.btn_placeholder);

        // Hides the archive and placeholder buttons
        btnArchive.setVisibility(View.GONE);
        btnPlaceholder.setVisibility(View.GONE);

        txtTitle.setText("Modify Exercise");
        exerciseEditText.setText(itemTitle);

        // Convert the stored weight to the selected unit
        if (!toggleWeightUnit.isChecked()) {
            // Convert kg to lbs
            itemWeight = Double.parseDouble(nf.format(itemWeight / 2.20462));
        }

        weightEditText.setText(itemWeight.toString());

        // Sets the cursor position to the end of text, rather than at the start
        exerciseEditText.setSelection(exerciseEditText.getText().length());
        weightEditText.setSelection(weightEditText.getText().length());

        btnUpdate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(exerciseEditText.getText())) {
                    Toast.makeText(ExerciseList.this, "You must give an exercise name", Toast.LENGTH_LONG).show();
                } else {
                    String newExerciseName = exerciseEditText.getText().toString();
                    long _id = Long.parseLong(itemId);

                    // Check for duplicate exercise name
                    if (dbManager.doesExerciseExist(newExerciseName)) {
                        // Exercise with the same name already exists, ask the user what to do
                        AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseList.this);
                        builder.setMessage("Exercise with the same name already exists. By clicking Update, you will have two exercises with the same name. Still Update?")
                                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int id) {
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
                                    }
                                })

                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User does not want to use the same name, do nothing
                                        dialog.dismiss();
                                    }
                                });

                        AlertDialog dialog = builder.create();
                        dialog.show();
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
            }


//            @Override
//            public void onClick(View v) {
//                if (TextUtils.isEmpty(exerciseEditText.getText())) {
//                    Toast.makeText(ExerciseList.this,
//                            "You must give an exercise name", Toast.LENGTH_LONG).show();
//                } else {
//                    String newExerciseName = exerciseEditText.getText().toString();
//                    Long _id = Long.parseLong(itemId);
//
//                    // Update exercise name
//                    dbManager.updateExerciseName(_id, newExerciseName);
//
//                    // If there is a weight given, update the database
//                    if (weightEditText.getText().toString().trim().length() > 0) {
//                        Double newExerciseWeight = Double.parseDouble(weightEditText.getText().toString());
//
//                        // Convert the entered weight to kg if lbs is selected
//                        if (!toggleWeightUnit.isChecked()) {
//                            newExerciseWeight = Double.parseDouble(nf.format(newExerciseWeight / 2.20462));
//                        }
//
//                        dbManager.updateExerciseWeight(_id, newExerciseWeight);
//                    } else {
//                        // If no weight value was given, update with a default value of 0
//                        Double newExerciseWeight = 0.0;
//                        dbManager.updateExerciseWeight(_id, newExerciseWeight);
//                    }
//
//                    // Refresh the recycler view
//                    ExerciseItem.clear();
//                    loadExerciseData();
//                    adapter.notifyDataSetChanged();
//
//                    dialog.dismiss();
//                }
//            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long _id = Long.parseLong(itemId);

                // Delete the selected exercise
                dbManager.deleteExercise(_id);

                // Refresh the recycler view
                ExerciseItem.clear();
                loadExerciseData();
                adapter.notifyDataSetChanged();

                dialog.dismiss();
            }
        });

        (dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

}