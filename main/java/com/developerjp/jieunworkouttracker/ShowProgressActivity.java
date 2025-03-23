package com.developerjp.jieunworkouttracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.navigation.NavigationView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ShowProgressActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // Item List
    private final List<Item> listItem = new ArrayList<>();
    private final List<String> exerciseNames = new ArrayList<>();
    private final Map<String, String> exerciseIdMap = new HashMap<>();
    private Toolbar toolbar;
    private BarChart chart;
    private DBManager dbManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);

        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            setTheme(R.style.DarkAppTheme_NoActionBar);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            setTheme(R.style.AppTheme_NoActionBar);
        }

        setContentView(R.layout.activity_menu_drawer_simple_light);

        ViewStub stub = findViewById(R.id.main_view_stub);
        stub.setLayoutResource(R.layout.activity_progress);
        stub.inflate();

        initToolbar();
        initNavigationMenu();
        initChart();
    }

    private void initChart() {
        chart = findViewById(R.id.chart1);
        chart.getDescription().setEnabled(false);

        Spinner spinner = findViewById(R.id.progress_spinner);
        spinner.setOnItemSelectedListener(this);

        dbManager = new DBManager(this);
        dbManager.open();
        Cursor cursor = dbManager.getAllExercises();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String exerciseName = cursor.getString(1);
                String exerciseId = cursor.getString(0);

                Log.d("Exercise", "Name: " + exerciseName + ", ID: " + exerciseId);
                exerciseNames.add(exerciseName);
                exerciseIdMap.put(exerciseId, exerciseName);
            } while (cursor.moveToNext());
        }


        dbManager.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>(new LinkedHashSet<>(exerciseNames)));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        chart.setMaxVisibleValueCount(60);
        chart.setPinchZoom(false);
        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        ValueFormatter xAxisFormatter = new DayAxisValueFormatter(chart);
        xAxis.setValueFormatter(xAxisFormatter);

        chart.getAxisLeft().setGranularity(1f);
        chart.getAxisRight().setGranularity(1f);
        chart.getAxisLeft().setDrawGridLines(false);

        chart.animateY(500);
        chart.getLegend().setEnabled(false);
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("");

        Chronometer simpleChronometer = findViewById(R.id.simpleChronometer);
        simpleChronometer.setVisibility(View.GONE);

        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText("Progress");
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

        nav_view.setNavigationItemSelectedListener(item -> {
            String itemCLicked = Objects.requireNonNull(item.getTitle()).toString();
            Intent intent;

            switch (itemCLicked) {
                case "Exercises":
                    intent = new Intent(getApplicationContext(), MainActivityExerciseList.class);
                    startActivity(intent);
                    break;
                case "Archived Exercises":
                    intent = new Intent(getApplicationContext(), ArchivedExerciseList.class);
                    startActivity(intent);
                    break;
                case "Progress":
                    intent = new Intent(getApplicationContext(), ShowProgressActivity.class);
                    startActivity(intent);
                    break;
                case "Calendar":
                    intent = new Intent(getApplicationContext(), ShowCalendarActivity.class);
                    startActivity(intent);
                    break;
                case "Color Scheme":
                    intent = new Intent(getApplicationContext(), ColorSchemeActivity.class);
                    startActivity(intent);
                    break;
            }

            drawer.closeDrawers();
            return true;
        });
    }


    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        try {
            chart.clear();
            dbManager.open();

            String selectedExerciseName = parent.getItemAtPosition(pos).toString();
            Log.d("Selected Exercise", "Name: " + selectedExerciseName);

            // Retrieve all exercise IDs from the map
            List<String> selectedExerciseIds = new ArrayList<>(exerciseIdMap.keySet());
            Log.d("Selected Exercise", "IDs: " + selectedExerciseIds);

            // Check if we have valid exercise IDs
            if (selectedExerciseIds.isEmpty()) {
                Log.w("ShowProgressActivity", "No exercise IDs found");
                chart.setNoDataText("No exercise data available");
                chart.invalidate();
                return;
            }

            Cursor cursor = dbManager.getExerciseLogProgress(selectedExerciseIds);
            ArrayList<BarEntry> values = new ArrayList<>();

            // Log cursor data for debugging before processing it
            logCursor(cursor);

            // Iterate through the cursor to retrieve log data
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int weightColumnIndex = cursor.getColumnIndex("weight");
                    int dateColumnIndex = cursor.getColumnIndex("date");

                    if (weightColumnIndex != -1 && dateColumnIndex != -1) {
                        String exerciseWeight = cursor.getString(weightColumnIndex);
                        String exerciseDate = cursor.getString(dateColumnIndex);

                        // Skip if either value is null
                        if (exerciseWeight == null || exerciseDate == null) {
                            Log.w("ShowProgressActivity", "Skipping null data: weight=" + exerciseWeight + ", date=" + exerciseDate);
                            continue;
                        }

                        String dayOfTheYear = convertDate(exerciseDate);

                        try {
                            // Check that converted values are not null before parsing
                            if (dayOfTheYear != null && !dayOfTheYear.isEmpty()) {
                                float weight = Float.parseFloat(exerciseWeight);
                                int day = Integer.parseInt(dayOfTheYear);
                                values.add(new BarEntry(day, weight));
                            } else {
                                Log.w("ShowProgressActivity", "Invalid date conversion: " + exerciseDate + " -> " + dayOfTheYear);
                            }
                        } catch (NumberFormatException e) {
                            Log.e("ShowProgressActivity", "Error parsing data: " + e.getMessage());
                        }
                    }
                } while (cursor.moveToNext());

                cursor.close(); // Close the cursor after processing
            }

            dbManager.close();

            // If no data was found, add a placeholder
            if (values.isEmpty()) {
                Log.i("ShowProgressActivity", "No data available for the selected exercise");
                chart.setNoDataText("No progress data available for this exercise");
                chart.invalidate();
                return;
            }

            BarDataSet set1 = new BarDataSet(values, "Data Set");

            // Set additional configurations for the BarDataSet
            set1.setColors(ColorTemplate.VORDIPLOM_COLORS);
            set1.setDrawValues(false);

            // Create a BarData object and add the BarDataSet to it
            BarData data = new BarData(set1);

            // Set the data to your chart
            chart.setData(data);

            // Refresh the chart to reflect the changes
            chart.invalidate();
        } catch (Exception e) {
            Log.e("ShowProgressActivity", "Error in onItemSelected: " + e.getMessage());
            e.printStackTrace();
            chart.setNoDataText("Error loading progress data");
            chart.invalidate();
        }
    }


    private void logCursor(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            Log.d("Cursor Data", "Cursor is null or closed");
            return;
        }

        StringBuilder cursorData = new StringBuilder();
        cursorData.append("Cursor data:\n");

        // Save the current position to restore it later
        int originalPosition = cursor.getPosition();
        cursor.moveToFirst();

        // Iterate over each row of the cursor
        while (!cursor.isAfterLast()) {
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                String columnName = cursor.getColumnName(i);
                String columnValue = cursor.getString(i);
                cursorData.append(columnName).append(": ").append(columnValue).append("\n");
            }
            cursor.moveToNext();
            cursorData.append("\n");
        }

        // Restore the cursor to its original position
        if (!cursor.isClosed()) {
            cursor.moveToPosition(originalPosition);
        }

        Log.d("Cursor Data", cursorData.toString());
    }


    private String convertDate(String dateToConvert) {
        try {
            // Check if the date is null or not in the expected format
            if (dateToConvert == null || dateToConvert.length() < 10) {
                Log.e("ShowProgressActivity", "Invalid date format: " + dateToConvert);
                return null;
            }

            //Splits the date to convert out into Year, Month and Date
            Log.d("Date to convert", dateToConvert);
            String strYear = dateToConvert.substring(0, 4);
            String strMonth = dateToConvert.substring(5, 7);
            String strDay = dateToConvert.substring(8, 10);
            Log.d("Converted", strYear + "," + strMonth + "," + strDay);

            int year = Integer.parseInt(strYear);
            int monthNumber = Integer.parseInt(strMonth);
            int dayNumber = Integer.parseInt(strDay);

            //Converts the date to a numbered day of the year
            LocalDate date = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                date = LocalDate.of(year, monthNumber, dayNumber);
            }
            int dayOfYear = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                dayOfYear = date.getDayOfYear();
            }

            return String.valueOf(dayOfYear);
        } catch (Exception e) {
            Log.e("ShowProgressActivity", "Error converting date: " + e.getMessage());
            return null;
        }
    }


    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
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
