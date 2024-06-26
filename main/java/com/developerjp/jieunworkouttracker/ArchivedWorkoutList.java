package com.developerjp.jieunworkouttracker;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArchivedWorkoutList extends AppCompatActivity implements RecyclerViewAdapter.OnItemSelectedListener, RecyclerViewAdapter.OnItemLongSelectedListener {

    // Item List
    private final List<Item> listItem = new ArrayList<>();
    private DBManager dbManager;
    private RecyclerView recyclerView;
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
        txtTitle.setText("Archived Workouts");

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
        Intent modify_intent = new Intent(getApplicationContext(), ArchivedExerciseList.class);
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
        Cursor cursor = dbManager.fetchArchivedWorkouts();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // If the cursor has a value in it then hide the empty textview
        // In English, if there is a workout returned, then remove the text saying no workouts found
        if (cursor != null && cursor.getCount() > 0) {
            TextView empty = findViewById(R.id.empty);
            empty.setVisibility(View.GONE);

            for( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() ) {
                Item item = new Item();
                // Uses the cursor to populate the item WORKOUT_ID value
                int workoutIdIndex = cursor.getColumnIndex("workout_id");
                item.setId(cursor.getString(workoutIdIndex));
                // Uses the cursor to populate the item Workout Names
                int workoutIndex = cursor.getColumnIndex("workout");
                item.setTitle(cursor.getString(workoutIndex));
                listItem.add(item);
            }

            // Close the cursor when done
            cursor.close();

            adapter = new RecyclerViewAdapter(listItem, this, this, this);
            recyclerView.setAdapter(adapter);
            try {
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            } catch (NullPointerException e) {
                // Handle the exception, e.g., log an error message
                e.printStackTrace();
            }
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

        btnAdd.setOnClickListener(v -> {

            //Does a validation check to make sure the user has entered in a value for the exercise name
            if (TextUtils.isEmpty(workoutEditText.getText())) {
                Toast.makeText(ArchivedWorkoutList.this,
                        "You must give a workout name", Toast.LENGTH_LONG).show();

                //If the user has given an exercise name then we will insert the exercise into the database
            } else {
                if (v.getId() == R.id.btn_add) {
                    final String workoutName = workoutEditText.getText().toString();
                    dbManager.insertWorkout(workoutName);


                    //Shows the new workout by clearing the recyclerview and re-adding all the items
                    //Works better this way as we don't have to re-create the entire activity
                    listItem.clear();
                    loadWorkoutData();
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();

                    //Will automatically scroll down to the position of the new workout which was added
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
        });

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
        //Common component, in this case we will be using this button to unarchive an exercise
        Button btnArchive = dialog.findViewById(R.id.btn_archive);
        btnArchive.setText("Unarchive");
        Button btnPlaceholder = dialog.findViewById(R.id.btn_placeholder);

        //Hides the placeholder button
        btnPlaceholder.setVisibility(View.INVISIBLE);

        txtTitle.setText("Modify Workout");
        workoutEditText.setText(itemTitle);

        //Sets the cursor position to the end of text, rather than at the start
        workoutEditText.setSelection(workoutEditText.getText().length());

        weightEditText.setVisibility(View.GONE);
        toggleWeightUnit.setVisibility(View.GONE);

        btnUpdate.setOnClickListener(v -> {

            //Does a validation check to make sure the user has entered in a value for the workout name
            if (TextUtils.isEmpty(workoutEditText.getText())) {
                Toast.makeText(ArchivedWorkoutList.this,
                        "You must give a workout name", Toast.LENGTH_LONG).show();

                //If the user has given a workout name then we will update the workout name in the database
            } else {
                String newWorkoutName = workoutEditText.getText().toString();
                long _id = Long.parseLong(itemId);

                //Updates with the new value
                dbManager.updateWorkout(_id, newWorkoutName);

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
            }
        });

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
            dbManager.unarchiveWorkout(_id);

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