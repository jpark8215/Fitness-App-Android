package com.developerjp.jieunworkouttracker;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.material.navigation.NavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ShowCalendarActivity extends AppCompatActivity implements CalendarRecyclerViewAdapter.OnItemSelectedListener {

    private static final String LOG_TAG = "Calendar";
    // Item List
    private final List<CalendarItem> listItem = new ArrayList<>();
    private final List<CalendarItem> workoutListItem = new ArrayList<>();
    private final SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMM - yyyy", Locale.getDefault());
    private DBManager dbManager;
    private CompactCalendarView compactCalendarView;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private TextView txtTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply theme using ThemeManager
        ThemeManager.applyTheme(this);

        setContentView(R.layout.activity_menu_drawer_simple_light);

        //Use view stubs to programmatically change the include view at runtime
        ViewStub stub = findViewById(R.id.main_view_stub);
        stub.setLayoutResource(R.layout.activity_calendar);
        stub.inflate();

        //Sets up the toolbar and navigation menu
        initToolbar();
        initNavigationMenu();

        compactCalendarView = findViewById(R.id.compactcalendar_view);
        compactCalendarView.shouldDrawIndicatorsBelowSelectedDays(true);

        recyclerView = findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        //Shows the current month and year
        txtTitle.setText(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));

        dbManager = new DBManager(this);
        dbManager.open();
        Cursor cursor = dbManager.fetchAllExerciseLogsForCalendar();

        //Adds all of the events to the calendar
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Get the date from the cursor
                int dateColumnIndex = cursor.getColumnIndex(DatabaseHelper.DATE);
                if (dateColumnIndex != -1) {
                    String date = cursor.getString(dateColumnIndex);
                    Log.d(LOG_TAG, "Processing date: " + date);

                    try {
                        // Parse the date string to a Date object
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        Date d = dateFormat.parse(date);
                        if (d != null) {
                            long milliseconds = d.getTime();
                            // Add the event to the calendar with a blue color
                            Event newEvent = new Event(Color.argb(255, 0, 191, 255), milliseconds, "Workout completed");
                            compactCalendarView.addEvent(newEvent);
                            Log.d(LOG_TAG, "Added event for date: " + date);
                        }
                    } catch (ParseException pe) {
                        Log.e(LOG_TAG, "Error parsing date: " + date, pe);
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        dbManager.close();

        Log.d(LOG_TAG, "TODAY'S DATE: " + Calendar.getInstance().getTime());

        String calendartest = Calendar.getInstance().getTime().toString();
        calendartest = calendartest.substring(0, 10);
        int calendaryear = Calendar.getInstance().get(Calendar.YEAR);
        Log.d(LOG_TAG, "TODAY'S DATE TRANSFORMED: " + calendartest + " " + calendaryear);

        //Shows workouts which were completed today
        Date todaysDate = Calendar.getInstance().getTime();
        showEvents(todaysDate);

        // define a listener to receive callbacks when certain events happen.
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {

                showEvents(dateClicked);

            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                Log.d(LOG_TAG, "Month was scrolled to: " + firstDayOfNewMonth);

                //Updates to show the selected month and year
                txtTitle.setText(dateFormatForMonth.format(firstDayOfNewMonth));

                //Shows workouts which were completed on the selected first day of the month
                showEvents(firstDayOfNewMonth);
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

        txtTitle = findViewById(R.id.txtTitle);
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

    @Override
    public void onItemSelected(String itemId, String itemTitle, String itemDate) {
        //Passes through the workout title and id
        //Starts the exercise list class
        Intent modify_intent = new Intent(getApplicationContext(), CalendarShowSelectedWorkout.class);
        modify_intent.putExtra("title", itemTitle);
        modify_intent.putExtra("id", itemId);  // This is now the logId
        modify_intent.putExtra("date", itemDate);
        startActivity(modify_intent);
    }

    public void showEvents(Date dateClicked) {
        List<Event> events = compactCalendarView.getEvents(dateClicked);
        Log.d(LOG_TAG, "Day was clicked: " + dateClicked + " with events " + events);

        // Format the date in the correct format for the database (yyyy-MM-dd)
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String strDate = dbDateFormat.format(dateClicked);
        Log.d(LOG_TAG, "Formatted date for database query: " + strDate);

        dbManager.open();

        Cursor exerciseCursor = dbManager.fetchExerciseDetailsForDate(strDate);

        //Clears the workoutListItem so that it doesn't keep the values from previous days in it
        workoutListItem.clear();

        if (exerciseCursor != null && exerciseCursor.getCount() > 0) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            // Use a format that can parse the full datetime string from the database
            SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

            for (exerciseCursor.moveToFirst(); !exerciseCursor.isAfterLast(); exerciseCursor.moveToNext()) {
                CalendarItem item = new CalendarItem();

                // Get exercise details
                int exerciseIdColumnIndex = exerciseCursor.getColumnIndex(DatabaseHelper.EXERCISE_ID);
                int exerciseNameColumnIndex = exerciseCursor.getColumnIndex(DatabaseHelper.EXERCISE);
                int logIdColumnIndex = exerciseCursor.getColumnIndex(DatabaseHelper.LOG_ID);
                int durationColumnIndex = exerciseCursor.getColumnIndex(DatabaseHelper.DURATION);
                int dateColumnIndex = exerciseCursor.getColumnIndex(DatabaseHelper.DATE);
                int datetimeColumnIndex = exerciseCursor.getColumnIndex(DatabaseHelper.DATETIME);

                String exerciseName = "";
                String exerciseId = "";
                String logId = "";
                String date = "";
                String datetime = "";
                long duration = 0;

                if (exerciseNameColumnIndex != -1) {
                    exerciseName = exerciseCursor.getString(exerciseNameColumnIndex);
                }

                if (exerciseIdColumnIndex != -1) {
                    exerciseId = exerciseCursor.getString(exerciseIdColumnIndex);
                }

                if (logIdColumnIndex != -1) {
                    logId = exerciseCursor.getString(logIdColumnIndex);
                }

                if (dateColumnIndex != -1) {
                    date = exerciseCursor.getString(dateColumnIndex);
                }

                if (datetimeColumnIndex != -1) {
                    datetime = exerciseCursor.getString(datetimeColumnIndex);
                }

                if (durationColumnIndex != -1) {
                    duration = exerciseCursor.getLong(durationColumnIndex);
                }

                // Set basic properties
                item.setTitle(exerciseName);
                item.setWorkoutId(exerciseId);
                item.setLogId(logId);
                item.setDate(date);

                try {
                    // Format time and duration
                    if (datetime != null && !datetime.isEmpty()) {
                        // Parse date with proper format including timezone
                        Date parsedDate = fullDateFormat.parse(datetime);
                        if (parsedDate != null) {
                            String timeStr = timeFormat.format(parsedDate);

                            // Format duration in minutes
                            int minutes = (int) (duration / 60);
                            String durationStr = minutes + " min";

                            // Append time and duration to the title
                            String displayTitle = exerciseName + " (" + timeStr + ", " + durationStr + ")";
                            item.setTitle(displayTitle);
                        }
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error formatting display: " + e.getMessage() + " for value: " + datetime);
                    // Use the exercise name without time if we can't parse the datetime
                    item.setTitle(exerciseName + " (time unknown)");
                }

                workoutListItem.add(item);
            }

            exerciseCursor.close();
        } else {
            // No exercises found for this date
            Log.d(LOG_TAG, "No exercises found for date: " + strDate);
        }

        dbManager.close();

        // Custom Recycler View Adaptor
        CalendarRecyclerViewAdapter adapter = new CalendarRecyclerViewAdapter(workoutListItem, ShowCalendarActivity.this, null, ShowCalendarActivity.this);
        recyclerView.setAdapter(adapter);
    }

    public void bottomNavigationHomeClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivityExerciseList.class);
        startActivity(intent);
    }

    public void bottomNavigationCalendarClick(View view) {
        Intent intent = new Intent(getApplicationContext(), ShowCalendarActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}