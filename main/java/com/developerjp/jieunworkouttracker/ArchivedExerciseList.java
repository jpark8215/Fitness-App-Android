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
    //Public variables which are used across classes/voids
    public String id;
    public String title;
    private DBManager dbManager;
    private RecyclerView recyclerView;
    // Custom Recycler View Adaptor
    private ExerciseRecyclerViewAdapter adapter;
    private Double exerciseWeight;
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
        //We want to grey it out to indicate that you cant start a workout on an archived workout
        FloatingActionButton fab_start_workout = findViewById(R.id.fab_start_workout);
        fab_start_workout.hide();
        CardView cv_add_exercise = findViewById(R.id.cv_add_exercise);
        //We want to grey it out to indicate that you cant start a workout on an archived workout
        CardView cv_start_workout = findViewById(R.id.cv_start_workout);
        cv_start_workout.setVisibility(View.GONE);

        back_drop.setVisibility(View.GONE);
        ViewAnimation.initShowOut(lyt_add_exercise);
        ViewAnimation.initShowOut(lyt_start_workout);

        fab_add.setOnClickListener(this::toggleFabMode);

        back_drop.setOnClickListener(v -> toggleFabMode(fab_add));

        fab_add_exercise.setOnClickListener(this::addExercise);

        cv_add_exercise.setOnClickListener(this::addExercise);
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
        txtTitle.setText("Archived " + title);

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
        });
    }

    public void loadExerciseData(){
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

        //fetchExerciseLogs returns the data in reverse order. So we start at the end of the cursor and work our way
        //backwards. This way the data appear is the correct order.
        for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {
            ExerciseItem exerciseItem = new ExerciseItem();

            // Get the column indices
            int logIdColumnIndex = cursor.getColumnIndex("log_id");
            int exerciseColumnIndex = cursor.getColumnIndex("exercise");
            int set1ColumnIndex = cursor.getColumnIndex("set1");
            int set2ColumnIndex = cursor.getColumnIndex("set2");
            int set3ColumnIndex = cursor.getColumnIndex("set3");
            int set4ColumnIndex = cursor.getColumnIndex("set4");
            int set5ColumnIndex = cursor.getColumnIndex("set5");

            // Check if the columns exist in the cursor
            if (logIdColumnIndex != -1) {
                exerciseItem.setId(cursor.getString(logIdColumnIndex));
            } else {
                // Handle the case where "log_id" column doesn't exist in the cursor
                Log.e(LOG_TAG, "Column 'log_id' not found in the cursor");
            }

            if (exerciseColumnIndex != -1) {
                exerciseItem.setTitle(cursor.getString(exerciseColumnIndex));
            } else {
                // Handle the case where "exercise" column doesn't exist in the cursor
                Log.e(LOG_TAG, "Column 'exercise' not found in the cursor");
            }

            if (set1ColumnIndex != -1) {
                exerciseItem.setButton1(cursor.getString(set1ColumnIndex));
            }

            if (set2ColumnIndex != -1) {
                exerciseItem.setButton2(cursor.getString(set2ColumnIndex));
            }

            if (set3ColumnIndex != -1) {
                exerciseItem.setButton3(cursor.getString(set3ColumnIndex));
            }

            if (set4ColumnIndex != -1) {
                exerciseItem.setButton4(cursor.getString(set4ColumnIndex));
            }

            if (set5ColumnIndex != -1) {
                exerciseItem.setButton5(cursor.getString(set5ColumnIndex));
            }

            //Sets all of the buttons to the default colour
            exerciseItem.setButton1Colour(R.drawable.button_shape_default);
            exerciseItem.setButton2Colour(R.drawable.button_shape_default);
            exerciseItem.setButton3Colour(R.drawable.button_shape_default);
            exerciseItem.setButton4Colour(R.drawable.button_shape_default);
            exerciseItem.setButton5Colour(R.drawable.button_shape_default);

            int weightColumnIndex = cursor.getColumnIndex("weight");

            if (weightColumnIndex != -1) {
                exerciseWeight = cursor.getDouble(weightColumnIndex);
            } else {
                // Handle the case where "weight" column doesn't exist in the cursor
                Log.e(LOG_TAG, "Column 'weight' not found in the cursor");
            }
            exerciseItem.setWeight(exerciseWeight);
            ExerciseItem.add(exerciseItem);
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
    protected void onPause() {
        //When the user navigates away from this screen we will minimise the floating action menu
        //floatingActionsMenu.collapse();

        // Save state - used when user clicks on an item far down the recycler view list
        // It remembers the state or position
        recyclerViewState = Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState();
        super.onPause();
    }

    @Override
    public void onResume(){
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
        if (intReps < 0) { intReps = 0;}

        //We pass through the itemId, set selected and number of reps
        dbManager.updateExerciseLogs(intItemId, setSelected ,intReps);

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

    public void bottomNavigationHomeClick(View view){
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

    private void addExercise(View v){
        //minimises the floating action button
        toggleFabMode(fab_add);
        showCustomAddDialog();
    }

    private void modifyExercise(String itemId, String itemTitle, Double itemWeight){
        showCustomModifyDialog(itemId, itemTitle, itemWeight);
    }

    private void startWorkout(View v){
        //minimises the floating action button
        toggleFabMode(fab_add);

        //Inserts a new exercise log for our workout that we are about to begin
        dbManager.insertExerciseLogs(id, strNumberOfExercises);

        //Passes through the workout title and id
        //Starts the startworkoutactivity class
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

        // ((TextView) dialog.findViewById(R.id.title)).setText("title");
        final EditText exerciseEditText = dialog.findViewById(R.id.name_edittext);
        final EditText weightEditText = dialog.findViewById(R.id.weight_edittext);
        final ToggleButton toggleWeightUnit = dialog.findViewById(R.id.toggle_weight_unit);

        TextView txtTitle  = dialog.findViewById(R.id.txt_title);
        Button btnAdd = dialog.findViewById(R.id.btn_add);

        btnAdd.setText("Add Exercise");
        txtTitle.setText("Add an Exercise");
        exerciseEditText.setHint("Exercise");


        btnAdd.setOnClickListener(v -> {
            final String exerciseName = exerciseEditText.getText().toString();

            // Check if the exercise name already exists in the database
            if (dbManager.doesExerciseExist(exerciseName)) {
                // Exercise with the same name exists
                double mostRecentWeight = dbManager.getMostRecentWeightForExercise(exerciseName);
                String message;
                if (mostRecentWeight > 0.0) {
                    message = "An exercise with the same name already exists. The most recent weight recorded for this exercise is: " + mostRecentWeight + ". \nStill add as you entered?";
                } else {
                    message = "An exercise with the same name already exists. \nStill add as you entered?";
                }

                // Ask the user if they want to proceed
                AlertDialog.Builder builder = new AlertDialog.Builder(ArchivedExerciseList.this);
                builder.setTitle("Exercise Already Exists");
                builder.setMessage(message);
                builder.setPositiveButton("Add", (dialogInterface, i) -> {
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
        });


//TODO Find a way to de-primary key Exercise ID so duplicated exerciseID can be generated
//        btnAdd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                //Does a validation check to make sure the user has entered in a value for the exercise name
//                if (TextUtils.isEmpty(exerciseEditText.getText())) {
//                    Toast.makeText(ArchivedExerciseList.this,
//                            "You must give an exercise name", Toast.LENGTH_LONG).show();
//
//                    //If the user has given an exercise name then we will insert the exercise into the database
//                } else {
//                    if (v.getId() == R.id.btn_add) {
//                        final String exerciseName = exerciseEditText.getText().toString();
//                        double exerciseWeight = 0.0;
//
//                        //Edit text field only accepts numbers
//                        //Was crashing when weight was left blank - so we make sure it has a value in it
//                        if (weightEditText.getText().toString().trim().length() > 0) {
//                            exerciseWeight = Double.parseDouble(weightEditText.getText().toString());
//
//                            // Convert kilograms to pounds if lbs is selected
//                            if (!toggleWeightUnit.isChecked()) {
//                                exerciseWeight = Double.parseDouble(nf.format(exerciseWeight / 2.20462));
//                            }
//                        }
//
//                        Intent intent = getIntent();
//                        dbManager.insertExercise(intent.getStringExtra("id"), exerciseName, exerciseWeight);
//                        Intent main = new Intent(v.getContext(), ExerciseList.class);
//                        main.putExtra("title", intent.getStringExtra("title"));
//                        main.putExtra("id", intent.getStringExtra("id"));
//                        main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(main);
//                    }
//                }
//            }
//
//        });

        (dialog.findViewById(R.id.bt_close)).setOnClickListener(v -> dialog.dismiss());

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
//        btnUpdate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                //Does a validation check to make sure the user has entered in a value for the exercise name
//                if (TextUtils.isEmpty(exerciseEditText.getText())) {
//                    Toast.makeText(ArchivedExerciseList.this,
//                            "You must give an exercise name", Toast.LENGTH_LONG).show();
//
//                    //If the user has given an exercise name then we will update the exercise name in the database
//                } else {
//                    String newWorkoutName = exerciseEditText.getText().toString();
//                    long _id = Long.parseLong(itemId);
//
//                    //Updates with the new value
//                    String newExerciseName = exerciseEditText.getText().toString();
//                    dbManager.updateExerciseName(_id, newExerciseName);
//
//                    // If there is a weight given, update the database
//                    if (weightEditText.getText().toString().trim().length() > 0) {
//                        double newExerciseWeight = Double.parseDouble(weightEditText.getText().toString());
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
//                    //Remembers the position of the recycler view when modify exercise or delete exercise is called
//                    final Parcelable recyclerViewState;
//                    recyclerViewState = Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState();
//
//                    //Shows the update made by clearing the recyclerview and re-adding all the items
//                    //Works better this way as we don't have to re-create the entire activity
//                    ExerciseItem.clear();
//                    loadExerciseData();
//                    adapter.notifyDataSetChanged();
//
//                    //places the user back at the same position in the recycler view rather than scrolling all the way back up to the top
//                    recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
//
//                    //Closes the dialog
//                    dialog.dismiss();
//                }
//            }
//        });

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