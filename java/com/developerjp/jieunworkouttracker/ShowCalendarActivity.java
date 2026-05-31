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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private TextView summaryDuration;
    private TextView summaryExercises;

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
        summaryDuration = findViewById(R.id.calendar_summary_duration);
        summaryExercises = findViewById(R.id.calendar_summary_exercises);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        //Shows the current month and year
        txtTitle.setText(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));

        dbManager = new DBManager(this);
        dbManager.open();
        Cursor cursor = dbManager.fetchWorkoutDayCountsForCalendar();

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
                            int countIndex = cursor.getColumnIndex("cnt");
                            int cnt = (countIndex != -1 && !cursor.isNull(countIndex)) ? cursor.getInt(countIndex) : 1;

                            // More activity -> warmer color
                            int color;
                            if (cnt >= 8) color = Color.parseColor("#FF6D00");      // orange
                            else if (cnt >= 4) color = Color.parseColor("#2E7D32"); // green
                            else color = Color.argb(255, 0, 191, 255);              // blue

                            Event newEvent = new Event(color, milliseconds, "Workout completed");
                            compactCalendarView.addEvent(newEvent);
                            Log.d(LOG_TAG, "Added event for date: " + date + " cnt=" + cnt);
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
        actionBar.setTitle("");

        txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText("Workouts");

        //Hides the chronometer as we don't need it for this activity
        Chronometer simpleChronometer = findViewById(R.id.simpleChronometer);
        simpleChronometer.setVisibility(View.GONE);
    }

    private void initNavigationMenu() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_progress); // Maps Calendar screen to Progress for now
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
                    } else {
                        // No ongoing workout, tell user to select exercises first
                        android.widget.Toast.makeText(this, "Please select exercises to start", android.widget.Toast.LENGTH_SHORT).show();
                        return true;
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
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            // Use a format that can parse the full datetime string from the database
            SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

            Map<Long, SessionSummary> sessions = new LinkedHashMap<>();
            LinkedHashSet<String> allExercises = new LinkedHashSet<>();

            int exerciseNameColumnIndex = exerciseCursor.getColumnIndex(DatabaseHelper.EXERCISE);
            int logIdColumnIndex = exerciseCursor.getColumnIndex(DatabaseHelper.LOG_ID);
            int durationColumnIndex = exerciseCursor.getColumnIndex(DatabaseHelper.DURATION);
            int dateColumnIndex = exerciseCursor.getColumnIndex(DatabaseHelper.DATE);
            int datetimeColumnIndex = exerciseCursor.getColumnIndex(DatabaseHelper.DATETIME);

            for (exerciseCursor.moveToFirst(); !exerciseCursor.isAfterLast(); exerciseCursor.moveToNext()) {
                String exerciseName = exerciseNameColumnIndex != -1 ? exerciseCursor.getString(exerciseNameColumnIndex) : "";
                String logId = logIdColumnIndex != -1 ? exerciseCursor.getString(logIdColumnIndex) : "";
                String date = dateColumnIndex != -1 ? exerciseCursor.getString(dateColumnIndex) : strDate;
                String datetime = datetimeColumnIndex != -1 ? exerciseCursor.getString(datetimeColumnIndex) : "";
                long duration = durationColumnIndex != -1 ? exerciseCursor.getLong(durationColumnIndex) : 0;

                if (exerciseName != null && !exerciseName.isEmpty()) {
                    allExercises.add(exerciseName);
                }

                long ts = -1;
                try {
                    if (datetime != null && !datetime.isEmpty()) {
                        Date parsed = fullDateFormat.parse(datetime);
                        if (parsed != null) ts = parsed.getTime();
                    }
                } catch (Exception e) {
                    // Ignore parse errors; treat as unknown timestamp
                }

                long bucket = ts > 0 ? (ts / (30L * 60L * 1000L)) : -1;
                SessionSummary session = sessions.get(bucket);
                if (session == null) {
                    session = new SessionSummary();
                    session.bucket = bucket;
                    session.anyLogId = (logId == null || logId.isEmpty()) ? "0" : logId;
                    session.date = date;
                    session.startMillis = ts;
                    sessions.put(bucket, session);
                }

                if (session.startMillis <= 0 && ts > 0) session.startMillis = ts;
                session.durationSeconds = Math.max(session.durationSeconds, duration);
                if (exerciseName != null && !exerciseName.isEmpty()) {
                    session.exercises.add(exerciseName);
                }
            }

            exerciseCursor.close();

            // Summary card
            long totalDurationSeconds = 0;
            for (SessionSummary s : sessions.values()) {
                totalDurationSeconds += s.durationSeconds;
            }
            updateSummaryCard(totalDurationSeconds, allExercises);

            // List items: one per session
            int sessionNumber = 1;
            for (SessionSummary s : sessions.values()) {
                CalendarItem item = new CalendarItem();
                item.setLogId(s.anyLogId);
                item.setDate(s.date);

                String timeStr = (s.startMillis > 0) ? timeFormat.format(new Date(s.startMillis)) : "Time unknown";
                int minutes = (int) (s.durationSeconds / 60);
                String title = sessions.size() > 1
                        ? ("Session " + sessionNumber + " • " + timeStr + " • " + minutes + " min")
                        : (timeStr + " • " + minutes + " min");
                item.setTitle(title);
                item.setSubtitle(joinExercises(s.exercises));
                workoutListItem.add(item);
                sessionNumber++;
            }
        } else {
            // No exercises found for this date
            Log.d(LOG_TAG, "No exercises found for date: " + strDate);
            updateSummaryCard(0, new LinkedHashSet<>());
        }

        dbManager.close();

        // Custom Recycler View Adaptor
        CalendarRecyclerViewAdapter adapter = new CalendarRecyclerViewAdapter(workoutListItem, ShowCalendarActivity.this, null, ShowCalendarActivity.this);
        recyclerView.setAdapter(adapter);
    }

    private void updateSummaryCard(long totalDurationSeconds, LinkedHashSet<String> exercises) {
        if (summaryDuration != null) {
            int minutes = (int) (totalDurationSeconds / 60);
            summaryDuration.setText("Duration: " + (minutes > 0 ? (minutes + " min") : "—"));
        }
        if (summaryExercises != null) {
            String ex = exercises.isEmpty() ? "—" : joinExercises(exercises);
            summaryExercises.setText("Exercises: " + ex);
        }
    }

    private String joinExercises(LinkedHashSet<String> exercises) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String e : exercises) {
            if (e == null || e.trim().isEmpty()) continue;
            if (i > 0) sb.append(" • ");
            sb.append(e.trim());
            i++;
        }
        return sb.toString();
    }

    private static class SessionSummary {
        long bucket;
        String anyLogId;
        String date;
        long startMillis;
        long durationSeconds;
        LinkedHashSet<String> exercises = new LinkedHashSet<>();
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