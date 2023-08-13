package com.example.jieunworkouttracker;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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

public class ShowCalendarActivity extends AppCompatActivity implements CalendarRecyclerViewAdaptor.OnItemSelectedListener {

    private static final String LOG_TAG = "Calendar";
    private DBManager dbManager;

    // Item List
    private final List<CalendarItem> listItem = new ArrayList();
    private final List<CalendarItem> workoutListItem = new ArrayList();

    // Custom Recycler View Adaptor
    private CalendarRecyclerViewAdaptor adapter;

    private CompactCalendarView compactCalendarView;
    private RecyclerView recyclerView;

    private final SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMM - yyyy", Locale.getDefault());

    private ActionBar actionBar;
    private Toolbar toolbar;
    private TextView txtTitle;
    private Chronometer simpleChronometer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
        stub.setLayoutResource(R.layout.activity_calendar);
        stub.inflate();

        //Sets up the toolbar and navigation menu
        initToolbar();
        initNavigationMenu();


        compactCalendarView = findViewById(R.id.compactcalendar_view);
        compactCalendarView.shouldDrawIndicatorsBelowSelectedDays(true);

        recyclerView = findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));


        //Shows the current month and year
        txtTitle.setText(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));

        dbManager = new DBManager(this);
        dbManager.open();
        Cursor cursor = dbManager.fetchAllExerciseLogsForCalendar();
        dbManager.close();

        //Adds all of the events to the calendar
        for( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() ) {
            CalendarItem calendarItem = new CalendarItem();

            //uses the cursor to populate the calendar WORKOUT_ID value
            int workoutIdColumnIndex = cursor.getColumnIndex("workout_id");
            if (workoutIdColumnIndex != -1) {
                calendarItem.setWorkoutId(cursor.getString(workoutIdColumnIndex));
            }

            //uses the cursor to populate the calendar DATE value
            int dateColumnIndex = cursor.getColumnIndex("date");
            if (dateColumnIndex != -1) {
                calendarItem.setDate(cursor.getString(dateColumnIndex));
            }            listItem.add(calendarItem);


            //String log_id = calendarItem.getLogId();
            String workout_id = calendarItem.getWorkoutId();
            String date = calendarItem.getDate();

            Log.d(LOG_TAG, "Original date value: " + date);


            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


            try {
                //Gets the date/time into the milliseconds value
                Date d = sdf.parse(date);
                long milliseconds = d.getTime();

                //Add the workout log events to the calendar
                Event newEvent = new Event(Color.argb(255, 0, 191, 255), milliseconds, "workout_id: " + workout_id);
                compactCalendarView.addEvent(newEvent);

                //Useful logs if app needs debugging
                //Log.d(LOG_TAG, "Converted date value: " + d.toString());
                //Log.d(LOG_TAG, "date value in milliseconds: " + Long.toString(milliseconds));

            } catch (ParseException pe) {
                Log.d(LOG_TAG, "ERROR Parsing date time");
                pe.printStackTrace();

            }
            //Log.d(LOG_TAG, "CURSOR - WORKOUT_ID: " + workout_id + " LOG_ID: " + log_id + " DATETIME " + date);
        }

        Log.d(LOG_TAG, "TODAYS DATE: " + Calendar.getInstance().getTime());

        String calendartest = Calendar.getInstance().getTime().toString();
        calendartest = calendartest.substring(0,10);
        Integer calendaryear = Calendar.getInstance().get(Calendar.YEAR);
        Log.d(LOG_TAG, "TODAYS DATE TRANSFORMED: " + calendartest + " " + calendaryear);


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
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("");

        txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText("Workouts");


        //Hides the chronometer as we don't need it for this activity
        simpleChronometer = findViewById(R.id.simpleChronometer);
        simpleChronometer.setVisibility(View.GONE);
    }

    private void initNavigationMenu() {
        NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // open drawer at start
        //drawer.openDrawer(GravityCompat.START);


        //Handles side navigation menu clicks
        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem item) {
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





    @Override
    public void onItemSelected(String itemId, String itemTitle, String itemDate) {

        //Passes through the workout title and id
        //Starts the exercise list class
        Intent modify_intent = new Intent(getApplicationContext(), CalendarShowSelectedWorkout.class);
        modify_intent.putExtra("title", itemTitle);
        modify_intent.putExtra("id", itemId);
        modify_intent.putExtra("date", itemDate);
        startActivity(modify_intent);
    }


    public void showEvents(Date dateClicked){
        List<Event> events = compactCalendarView.getEvents(dateClicked);
        Log.d(LOG_TAG, "Day was clicked: " + dateClicked + " with events " + events);


        String strDate = dateClicked.toString();
        strDate = strDate.substring(0, 10) + "%" + strDate.substring(24, 28);

        Log.d("*********strDate", strDate);


        dbManager.open();
        Cursor dbCursor = dbManager.fetchWorkoutsOnSelectedDateForCalendar(strDate);

        //Clears the workoutListItem so that it doesn't keep the values from previous days in it
        workoutListItem.clear();

        for( dbCursor.moveToFirst(); !dbCursor.isAfterLast(); dbCursor.moveToNext() ) {
            CalendarItem item = new CalendarItem();
            //uses the cursor to populate the item WORKOUT_ID value
            int workoutIdColumnIndex = dbCursor.getColumnIndex("workout_id");
            if (workoutIdColumnIndex != -1) {
                item.setWorkoutId(dbCursor.getString(workoutIdColumnIndex));
            }

            //uses the cursor to populate the item Workout Names
            if (workoutIdColumnIndex != -1) {
                String workoutId = dbCursor.getString(workoutIdColumnIndex);
                Cursor dbCursor2 = dbManager.fetchWorkoutNameOnSelectedDateForCalendar(workoutId);
                item.setWorkoutId(workoutId);

                if (dbCursor2 != null) {
                    int workoutTitleColumnIndex = dbCursor2.getColumnIndex("workout");
                    if (workoutTitleColumnIndex != -1) {
                        String workoutTitle = dbCursor2.getString(workoutTitleColumnIndex);
                        item.setTitle(workoutTitle);
                    } else {
                        // Handle the case where "workout" column doesn't exist in dbCursor2
                        Log.e(LOG_TAG, "Column 'workout' not found in dbCursor2");
                    }
                    dbCursor2.close(); // Close the cursor when you're done with it
                } else {
                    // Handle the case where dbCursor2 is null
                    Log.e(LOG_TAG, "dbCursor2 is null");
                }
            } else {
                // Handle the case where "workout_id" column doesn't exist in dbCursor
                Log.e(LOG_TAG, "Column 'workout_id' not found in dbCursor");
            }

            int dateColumnIndex = dbCursor.getColumnIndex("date");
            if (dateColumnIndex != -1) {
                item.setDate(dbCursor.getString(dateColumnIndex));
            }            workoutListItem.add(item);
        }

        dbManager.close();
        adapter = new CalendarRecyclerViewAdaptor(workoutListItem, ShowCalendarActivity.this, null, ShowCalendarActivity.this);
        recyclerView.setAdapter(adapter);
    }

    public void bottomNavigationHomeClick(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivityWorkoutList.class);
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
