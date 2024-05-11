package com.developerjp.jieunworkouttracker;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivityWorkoutList extends AppCompatActivity implements RecyclerViewAdapter.OnItemSelectedListener, RecyclerViewAdapter.OnItemLongSelectedListener {

    private DBManager dbManager;
    private RecyclerView recyclerView;
    // Item List
    private final List<Item> listItem = new ArrayList<>();
    // Custom Recycler View Adaptor
    private RecyclerViewAdapter adapter;
    private View back_drop;
    private boolean rotate = false;
    private View lyt_add_workout;
    private FloatingActionButton fab_add;
    private Parcelable recyclerViewState;
    private Toolbar toolbar;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get a reference to the Shared Preferences object
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);

        // Get the value of the "dark_mode" key, or "false" if it doesn't exist
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);

        // If dark mode is enabled then do the following
        if (darkModeEnabled){
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
        stub.setLayoutResource(R.layout.activity_main_workout_list);
        stub.inflate();


        //Sets up the toolbar and navigation menu
        initToolbar();
        initNavigationMenu();


        View parent_view = findViewById(android.R.id.content);
        back_drop = findViewById(R.id.back_drop);
        lyt_add_workout = findViewById(R.id.lyt_add_workout);

        dbManager = new DBManager(this);
        dbManager.open();

        //Loads the Exercise logs data using recyclerview and the custom adapter
        loadWorkoutData();

        fab_add = findViewById(R.id.fab_add);
        FloatingActionButton fab_add_workout = findViewById(R.id.fab_add_workout);
        CardView cv_add_workout = findViewById(R.id.cv_add_workout);
        back_drop.setVisibility(View.GONE);
        ViewAnimation.initShowOut(lyt_add_workout);

        fab_add.setOnClickListener(this::toggleFabMode);

        back_drop.setOnClickListener(v -> toggleFabMode(fab_add));

        fab_add_workout.setOnClickListener(this::addWorkout);

        cv_add_workout.setOnClickListener(this::addWorkout);

        MobileAds.initialize(this, initializationStatus -> {
            Log.d("Ads", "Initialization status: " + initializationStatus);

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
        txtTitle.setText("Workouts");

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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbManager.close();
    }

    @Override
    public void OnBackPressedDispatcher() {
        //When the user clicks on the back button we want to exit the application
        super.getOnBackPressedDispatcher();
        this.finish();
    }

    @Override
    public void onItemSelected(String itemId, String itemTitle) {

        //Passes through the workout title and id
        //Starts the exercise list class
        Intent modify_intent = new Intent(getApplicationContext(), ExerciseList.class);
        modify_intent.putExtra("title", itemTitle);
        modify_intent.putExtra("id", itemId);
        startActivity(modify_intent);
    }

    @Override
    public void onItemLongSelected(String itemId, String itemTitle) {
        modifyWorkout(itemId, itemTitle);
    }

    public void bottomNavigationHomeClick(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivityWorkoutList.class);
        startActivity(intent);
    }

    public void bottomNavigationCalendarClick(View view) {
        Intent intent = new Intent(getApplicationContext(), ShowCalendarActivity.class);
        startActivity(intent);
    }

    private void loadWorkoutData() {
        Cursor cursor = dbManager.fetchActiveWorkouts();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        TextView empty = findViewById(R.id.empty);

        // Initialize the adapter if it's null
        if (adapter == null) {
            adapter = new RecyclerViewAdapter(listItem, this, this, this);
            recyclerView.setAdapter(adapter);
        }

        // If the cursor has a value in it then hide the empty textview
        if (cursor != null && cursor.getCount() > 0) {
            empty.setVisibility(View.GONE);

            listItem.clear(); // Clear the list to avoid duplicates when refreshing
            // Iterate through the cursor and populate the list
            cursor.moveToFirst(); // Move to the first row

            while (!cursor.isAfterLast()) {
                Item item = new Item();

                // Get column indices
                int workoutIdIndex = cursor.getColumnIndex(DatabaseHelper.WORKOUT_ID);
                int workoutNameIndex = cursor.getColumnIndex(DatabaseHelper.WORKOUT);

                // Check if indices are valid
                if (workoutIdIndex != -1 && workoutNameIndex != -1) {
                    // Retrieve data from cursor
                    String workoutId = cursor.getString(workoutIdIndex);
                    String workoutName = cursor.getString(workoutNameIndex);

                    // Populate the item
                    item.setId(workoutId);
                    item.setTitle(workoutName);

                    // Add the item to the list
                    listItem.add(item);
                }

                cursor.moveToNext(); // Move to the next row
            }

            // Close the cursor when done
            cursor.close();

            // Notify the adapter that the data has changed
            adapter.notifyDataSetChanged();
        }
    }


    private void toggleFabMode(View v) {
        rotate = ViewAnimation.rotateFab(v, !rotate);
        if (rotate) {
            ViewAnimation.showIn(lyt_add_workout);
            back_drop.setVisibility(View.VISIBLE);
        } else {
            ViewAnimation.showOut(lyt_add_workout);
            back_drop.setVisibility(View.GONE);
        }
    }

    private void addWorkout(View v){
        //minimises the floating action button
        toggleFabMode(fab_add);
        showCustomAddDialog();
    }

    private void modifyWorkout(String itemId, String itemTitle){
        showCustomModifyDialog(itemId, itemTitle);
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

        final EditText workoutEditText = dialog.findViewById(R.id.name_edittext);
        final EditText weightEditText = dialog.findViewById(R.id.weight_edittext);
        final ToggleButton toggleWeightUnit = dialog.findViewById(R.id.toggle_weight_unit);

        TextView txtTitle  = dialog.findViewById(R.id.txt_title);
        Button btnAdd = dialog.findViewById(R.id.btn_add);

        btnAdd.setText("Add Workout");
        txtTitle.setText("Add a Workout");
        workoutEditText.setHint("Workout");
        weightEditText.setVisibility(View.GONE);
        toggleWeightUnit.setVisibility(View.GONE);

        //Does not allow duplicated workout name
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(workoutEditText.getText())) {
                    Toast.makeText(MainActivityWorkoutList.this,
                            "You must give a workout name", Toast.LENGTH_LONG).show();
                } else {
                    String workoutName = workoutEditText.getText().toString();

                    // Keep prompting until a unique workout name is provided
                    while (dbManager.isDuplicateWorkout(workoutName)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityWorkoutList.this);
                        builder.setTitle("Duplicate Workout Name");
                        builder.setMessage("A workout with the same name already exists. Please choose another name.");
                        builder.setPositiveButton("OK", (dialogInterface, i) -> {
                            // Dismiss the dialog
                        });
                        builder.show();

                        // Clear the EditText for user to input a new name
                        workoutEditText.setText("");

                        // Exit the loop if user cancels or dismisses the dialog
                        if (!builder.create().isShowing()) {
                            return;
                        }

                        // Wait for user to input a new name
                        synchronized (this) {
                            try {
                                wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        // Retrieve the new workout name from EditText
                        workoutName = workoutEditText.getText().toString();
                    }

                    // No duplicate found, insert the workout directly
                    dbManager.insertWorkout(workoutName);

                    // Refresh the list
                    listItem.clear();
                    loadWorkoutData();
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
        });

//        btnAdd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (TextUtils.isEmpty(workoutEditText.getText())) {
//                    Toast.makeText(MainActivityWorkoutList.this,
//                            "You must give a workout name", Toast.LENGTH_LONG).show();
//                } else {
//                    if (v.getId() == R.id.btn_add) {
//                        final String workoutName = workoutEditText.getText().toString();
//
//                        // Check if it's a duplicate
//                        if (dbManager.isDuplicateWorkout(workoutName)) {
//                            // It's a duplicate, ask for confirmation
//                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityWorkoutList.this);
//                            builder.setTitle("Duplicate Workout Name");
//                            builder.setMessage("A workout with the same name already exists. By clicking Add, you will have two workouts with the same name. Still Add??");
//                            builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    // User confirmed, add the workout
//                                    dbManager.insertWorkout(workoutName);
//
//                                    // Refresh the list
//                                    listItem.clear();
//                                    loadWorkoutData();
//                                    adapter.notifyDataSetChanged();
//                                    dialog.dismiss();
//                                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
//                                }
//                            });
//                            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    // User declined, do nothing or show a message
//                                    dialog.dismiss();
//                                }
//                            });
//                            builder.show();
//                        } else {
//                            // Not a duplicate, insert the workout directly
//                            dbManager.insertWorkout(workoutName);
//
//                            // Refresh the list
//                            listItem.clear();
//                            loadWorkoutData();
//                            adapter.notifyDataSetChanged();
//                            dialog.dismiss();
//                            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
//                        }
//                    }
//                }
//            }
//        });

        (dialog.findViewById(R.id.bt_close)).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }


    private void showCustomModifyDialog(final String itemId, String itemTitle) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_modify_light);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final EditText workoutEditText = dialog.findViewById(R.id.name_edittext);
        final EditText weightEditText = dialog.findViewById(R.id.weight_edittext);
        final ToggleButton toggleWeightUnit = dialog.findViewById(R.id.toggle_weight_unit);

        TextView txtTitle  = dialog.findViewById(R.id.txt_title);
        Button btnUpdate = dialog.findViewById(R.id.btn_update);
        Button btnDelete = dialog.findViewById(R.id.btn_delete);
        Button btnArchive = dialog.findViewById(R.id.btn_archive);
        Button btnPlaceholder = dialog.findViewById(R.id.btn_placeholder);


        //Hides the placeholder button
        btnPlaceholder.setVisibility(View.INVISIBLE);

        txtTitle.setText("Modify Workout");
        workoutEditText.setText(itemTitle);


        //Sets the cursor position to the end of text, rather than at the start
        workoutEditText.setSelection(workoutEditText.getText().length());

        weightEditText.setVisibility(View.GONE);
        toggleWeightUnit.setVisibility(View.GONE);

        //Does not allow duplicated workout name
        btnUpdate.setOnClickListener(v -> {
            // Validate if the user has entered a workout name
            if (TextUtils.isEmpty(workoutEditText.getText())) {
                Toast.makeText(MainActivityWorkoutList.this,
                        "You must give a workout name", Toast.LENGTH_LONG).show();
            } else {
                String newWorkoutName = workoutEditText.getText().toString();
                long _id = Long.parseLong(itemId);

                // Check if the new workout name already exists
                if (dbManager.isDuplicateWorkout(newWorkoutName)) {
                    // Handle the case where the workout name already exists
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityWorkoutList.this);
                    builder.setTitle("Duplicate Workout Name");
                    builder.setMessage("A workout with this name already exists. Please choose another name.");

                    // Add buttons for user choice
                    builder.setPositiveButton("OK", (dialogInterface, i) -> {
                        // Dismiss the dialog
                    });
                    builder.show();
                } else {
                    // No duplicate found, update the workout name directly
                    dbManager.updateWorkout(_id, newWorkoutName);

                    // Remember the position of the recycler view when modifying a workout
                    final Parcelable recyclerViewState = Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState();

                    // Show the update by clearing the recyclerview and re-adding all the items
                    // This works better as we don't have to re-create the entire activity
                    listItem.clear();
                    loadWorkoutData();
                    adapter.notifyDataSetChanged();

                    // Place the user back at the same position in the recycler view rather than scrolling to the top
                    recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);

                    // Close the dialog
                    dialog.dismiss();
                }
            }
        });


//        btnUpdate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Validate if the user has entered a workout name
//                if (TextUtils.isEmpty(workoutEditText.getText())) {
//                    Toast.makeText(MainActivityWorkoutList.this,
//                            "You must give a workout name", Toast.LENGTH_LONG).show();
//                } else {
//                    String newWorkoutName = workoutEditText.getText().toString();
//                    Long _id = Long.parseLong(itemId);
//
//                    // Check if the new workout name already exists
//                    if (dbManager.isDuplicateWorkout(newWorkoutName)) {
//                        // Handle the case where the workout name already exists
//                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityWorkoutList.this);
//                        builder.setTitle("Duplicate Workout Name");
//                        builder.setMessage("A workout with this name already exists. By clicking Update, you will have two workouts with the same name. Still Update?");
//
//                        // Add buttons for user choice
//                        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                // Update the workout name in the database
//                                dbManager.updateWorkout(_id, newWorkoutName);
//
//                                // Remember the position of the recycler view when modifying a workout
//                                final Parcelable recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
//
//                                // Show the update by clearing the recyclerview and re-adding all the items
//                                // This works better as we don't have to re-create the entire activity
//                                listItem.clear();
//                                loadWorkoutData();
//                                adapter.notifyDataSetChanged();
//
//                                // Place the user back at the same position in the recycler view rather than scrolling to the top
//                                recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
//
//                                // Close the dialog
//                                dialog.dismiss();
//                            }
//                        });
//
//                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                // User chose to cancel the update, do nothing
//                                dialog.dismiss();
//                            }
//                        });
//
//                        AlertDialog dialog = builder.create();
//                        dialog.show();
//                    } else {
//                        // No duplicate found, update the workout name directly
//                        dbManager.updateWorkout(_id, newWorkoutName);
//
//                        // Remember the position of the recycler view when modifying a workout
//                        final Parcelable recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
//
//                        // Show the update by clearing the recyclerview and re-adding all the items
//                        // This works better as we don't have to re-create the entire activity
//                        listItem.clear();
//                        loadWorkoutData();
//                        adapter.notifyDataSetChanged();
//
//                        // Place the user back at the same position in the recycler view rather than scrolling to the top
//                        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
//
//                        // Close the dialog
//                        dialog.dismiss();
//                    }
//                }
//            }
//        });

        btnDelete.setOnClickListener(v -> {
            long _id = Long.parseLong(itemId);

            //Deletes the selected Workout
            dbManager.deleteWorkout(_id);

            //Remembers the position of the recycler view when modify workout or delete workout is called
            final Parcelable recyclerViewState;
            recyclerViewState = Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState();

            //Shows the update made by clearing the recyclerview and re-adding all the items
            //Works better this way as we don't have to re-create the entire activity
            listItem.clear();
            loadWorkoutData();
            adapter.notifyDataSetChanged();

            //places the user back at the same position in the recycler view rather than scrolling all the way back up to the top
            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);

            //Closes the dialog
            dialog.dismiss();

        });


        btnArchive.setOnClickListener(v -> {
            Long _id = Long.parseLong(itemId);

            //Deletes the selected Workout
            dbManager.archiveWorkout(_id);

            //Remembers the position of the recycler view when modify workout or delete workout is called
            final Parcelable recyclerViewState;
            recyclerViewState = Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState();

            //Shows the update made by clearing the recyclerview and re-adding all the items
            //Works better this way as we don't have to re-create the entire activity
            listItem.clear();
            loadWorkoutData();
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