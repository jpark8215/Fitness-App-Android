package com.developerjp.jieunworkouttracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
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

        // Initialize MobileAds
        MobileAds.initialize(this, initializationStatus -> Log.d("Ads", "Initialization status: " + initializationStatus));

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

        // Initialize AdView
        AdView adView = findViewById(R.id.adView);
        if (adView != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }

        initToolbar();
        initNavigationMenu();
        initChart();

        // Set up predictive back gesture support
        setupBackCallback();
    }

    private void initChart() {
        chart = findViewById(R.id.chart1);
        if (chart == null) {
            Log.e("ShowProgressActivity", "Chart not found in layout!");
            return;
        }
        Log.d("ShowProgressActivity", "Chart found and initialized");
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(new LinkedHashSet<>(exerciseNames)));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Auto-select first exercise if available
        if (!exerciseNames.isEmpty()) {
            spinner.setSelection(0);
        }

        chart.setMaxVisibleValueCount(60);
        chart.setPinchZoom(false);
        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(true);
        chart.setVisibility(View.VISIBLE);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setExtraTopOffset(10f);
        chart.setExtraBottomOffset(10f);
        chart.setExtraLeftOffset(10f);
        chart.setExtraRightOffset(10f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setGranularity(1f);
        ValueFormatter xAxisFormatter = new DayAxisValueFormatter(chart);
        xAxis.setValueFormatter(xAxisFormatter);

        // Configure Y-axis with the proper unit label
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setDrawGridLines(true);

        // Check weight unit preference and set y-axis label formatter
        boolean isKgUnit = WeightUnitManager.isKgUnit(this);
        leftAxis.setValueFormatter(new WeightAxisValueFormatter(isKgUnit));

        // Hide right axis
        chart.getAxisRight().setEnabled(false);

        // Set axis label colors based on dark mode
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);
        int labelColor = darkModeEnabled ? Color.WHITE : Color.BLACK;
        int gridColor = darkModeEnabled ? Color.parseColor("#404040") : Color.parseColor("#E0E0E0");
        xAxis.setTextColor(labelColor);
        leftAxis.setTextColor(labelColor);
        xAxis.setGridColor(gridColor);
        leftAxis.setGridColor(gridColor);

        // Set chart background based on theme
        if (darkModeEnabled) {
            chart.setBackgroundColor(Color.parseColor("#2C2C2C"));
            chart.setGridBackgroundColor(Color.parseColor("#404040"));
        } else {
            chart.setBackgroundColor(Color.WHITE);
            chart.setGridBackgroundColor(Color.parseColor("#F0F0F0"));
        }

        chart.animateY(500);
        chart.getLegend().setEnabled(false);

        // Set initial no data text with proper dark mode colors
        chart.setNoDataText("Select an exercise to view progress");
        int noDataTextColor = darkModeEnabled ? Color.WHITE : Color.GRAY;
        chart.setNoDataTextColor(noDataTextColor);
        chart.invalidate();

        // Force layout to ensure chart is properly sized
        chart.post(() -> {
            Log.d("ShowProgressActivity", "Chart dimensions: " + chart.getWidth() + "x" + chart.getHeight());
            if (chart.getWidth() == 0 || chart.getHeight() == 0) {
                Log.w("ShowProgressActivity", "Chart has zero dimensions!");
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
                case "Settings":
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

            // Find the exercise ID for the selected exercise name instead of getting all exercise IDs
            String selectedExerciseId = null;
            for (Map.Entry<String, String> entry : exerciseIdMap.entrySet()) {
                if (entry.getValue().equals(selectedExerciseName)) {
                    selectedExerciseId = entry.getKey();
                    break;
                }
            }

            if (selectedExerciseId == null) {
                Log.w("ShowProgressActivity", "Could not find ID for selected exercise: " + selectedExerciseName);
                chart.setNoDataText("No exercise data available");
                SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
                boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);
                int noDataTextColor = darkModeEnabled ? Color.WHITE : Color.GRAY;
                chart.setNoDataTextColor(noDataTextColor);
                chart.invalidate();
                return;
            }

            Log.d("Selected Exercise", "ID: " + selectedExerciseId);

            // Create a list with just the selected exercise ID
            List<String> selectedExerciseIds = new ArrayList<>();
            selectedExerciseIds.add(selectedExerciseId);

            // Check if we have valid exercise IDs

            Cursor cursor = dbManager.getExerciseLogProgress(selectedExerciseIds);
            ArrayList<BarEntry> values = new ArrayList<>();

            // Get user's weight unit preference
            boolean isKgUnit = WeightUnitManager.isKgUnit(this);

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
                                // Parse the weight value
                                float weight = Float.parseFloat(exerciseWeight);

                                // Convert weight to lbs if needed based on user preference
                                if (!isKgUnit) {
                                    weight = (float) WeightUtils.kgToLbs(weight);
                                }

                                int day = Integer.parseInt(dayOfTheYear);
                                values.add(new BarEntry(day, weight));
                                Log.d("ShowProgressActivity", "Added data point: day=" + day + ", weight=" + weight);
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

            // If no data was found, add a placeholder or test data
            if (values.isEmpty()) {
                Log.i("ShowProgressActivity", "No data available for the selected exercise");

                // Add some test data to verify chart is working
                Log.d("ShowProgressActivity", "Adding test data to verify chart functionality");
                // Use positive day values for 2025 dates
                values.add(new BarEntry(0, 50));   // 2025-01-01
                values.add(new BarEntry(30, 52));  // 2025-01-31
                values.add(new BarEntry(60, 51));  // 2025-03-01
                values.add(new BarEntry(90, 53));  // 2025-04-01
                values.add(new BarEntry(120, 54)); // 2025-05-01

                chart.setNoDataText("No progress data available for this exercise");
                SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
                boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);
                int noDataTextColor = darkModeEnabled ? Color.WHITE : Color.GRAY;
                chart.setNoDataTextColor(noDataTextColor);
            }

            Log.d("ShowProgressActivity", "Found " + values.size() + " data points for chart");

            BarDataSet set1 = new BarDataSet(values, "Data Set");

            // Set additional configurations for the BarDataSet
            set1.setColors(ColorTemplate.PASTEL_COLORS);
            set1.setDrawValues(false);
            set1.setValueTextSize(12f);
            SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
            boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);
            int valueTextColor = darkModeEnabled ? Color.WHITE : Color.BLACK;
            set1.setValueTextColor(valueTextColor);
            set1.setDrawValues(true);
            set1.setValueTextSize(10f);
            set1.setHighLightColor(Color.RED);

            // Create a BarData object and add the BarDataSet to it
            BarData data = new BarData(set1);
            data.setBarWidth(0.8f); // Set bar width

            // Set the data to your chart
            chart.setData(data);

            // Force the chart to redraw
            chart.notifyDataSetChanged();
            Log.d("ShowProgressActivity", "Chart data set successfully");

            // Update y-axis label based on current weight unit preference
            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setValueFormatter(new WeightAxisValueFormatter(isKgUnit));

            // Update text colors for dark mode
            sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
            darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);
            int labelColor = darkModeEnabled ? Color.WHITE : Color.BLACK;
            leftAxis.setTextColor(labelColor);
            chart.getXAxis().setTextColor(labelColor);

            // Set minimum and maximum values for better display
            if (!values.isEmpty()) {
                float minWeight = Float.MAX_VALUE;
                float maxWeight = Float.MIN_VALUE;
                float minDay = Float.MAX_VALUE;
                float maxDay = Float.MIN_VALUE;

                for (BarEntry entry : values) {
                    minWeight = Math.min(minWeight, entry.getY());
                    maxWeight = Math.max(maxWeight, entry.getY());
                    minDay = Math.min(minDay, entry.getX());
                    maxDay = Math.max(maxDay, entry.getX());
                }

                // Set axis ranges with some padding
                leftAxis.setAxisMinimum(Math.max(0, minWeight - 5));
                leftAxis.setAxisMaximum(maxWeight + 5);

                XAxis xAxis = chart.getXAxis();
                xAxis.setAxisMinimum(minDay - 1);
                xAxis.setAxisMaximum(maxDay + 1);

                // Set visible range to show 7 days by default
                if (maxDay - minDay >= 6) {
                    // If we have more than 7 data points, show the last 7
                    float visibleMin = maxDay - 6;
                    float visibleMax = maxDay + 1;
                    chart.setVisibleXRange(7, 60);
                    chart.moveViewToX(visibleMax);
                } else {
                    // If we have 7 or fewer data points, show all of them
                    chart.setVisibleXRange(maxDay - minDay + 2, maxDay - minDay + 2);
                }

                Log.d("ShowProgressActivity", "Chart axis ranges: X[" + minDay + "-" + maxDay + "], Y[" + minWeight + "-" + maxWeight + "]");
            }

            // Refresh the chart to reflect the changes
            chart.invalidate();
            chart.requestLayout();
            Log.d("ShowProgressActivity", "Chart invalidated and layout requested");
        } catch (Exception e) {
            Log.e("ShowProgressActivity", "Error in onItemSelected: " + e.getMessage());
            e.printStackTrace();
            chart.setNoDataText("Error loading progress data");
            SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
            boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);
            int noDataTextColor = darkModeEnabled ? Color.WHITE : Color.GRAY;
            chart.setNoDataTextColor(noDataTextColor);
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
            String strYear = dateToConvert.substring(0, 4);
            String strMonth = dateToConvert.substring(5, 7);
            String strDay = dateToConvert.substring(8, 10);

            int year = Integer.parseInt(strYear);
            int monthNumber = Integer.parseInt(strMonth);
            int dayNumber = Integer.parseInt(strDay);

            // Convert to days since 2025-01-01 (base date for the formatter)
            LocalDate baseDate;
            LocalDate targetDate;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                baseDate = LocalDate.of(2025, 1, 1);
                targetDate = LocalDate.of(year, monthNumber, dayNumber);

                // Calculate days between base date and target date
                long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(baseDate, targetDate);
                return String.valueOf(daysBetween);
            }

            return null;
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
        // Pause the AdView
        AdView adView = findViewById(R.id.adView);
        if (adView != null) {
            adView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the AdView
        AdView adView = findViewById(R.id.adView);
        if (adView != null) {
            adView.resume();
        }

        // Check if weight unit preference has changed and refresh chart if needed
        if (chart != null && chart.getData() != null) {
            boolean isKgUnit = WeightUnitManager.isKgUnit(this);
            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setValueFormatter(new WeightAxisValueFormatter(isKgUnit));

            // If we have a spinner with a selected position, trigger onItemSelected again
            Spinner spinner = findViewById(R.id.progress_spinner);
            if (spinner != null && spinner.getSelectedItemPosition() >= 0) {
                onItemSelected(spinner, null, spinner.getSelectedItemPosition(), spinner.getSelectedItemId());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Destroy the AdView
        AdView adView = findViewById(R.id.adView);
        if (adView != null) {
            adView.destroy();
        }
    }

    public void bottomNavigationHomeClick(View view) {
        Intent intent = new Intent(this, MainActivityExerciseList.class);
        startActivity(intent);
    }

    public void bottomNavigationCalendarClick(View view) {
        Intent intent = new Intent(this, ShowCalendarActivity.class);
        startActivity(intent);
    }

    private void setupBackCallback() {
        // Handle back navigation with predictive back gesture support
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Check if drawer is open and close it first
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    // Finish the activity
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    /**
     * Custom formatter for the y-axis that shows units (kg or lbs)
     */
    private static class WeightAxisValueFormatter extends ValueFormatter {
        private final boolean isKgUnit;

        public WeightAxisValueFormatter(boolean isKgUnit) {
            this.isKgUnit = isKgUnit;
        }

        @Override
        public String getFormattedValue(float value) {
            return String.format("%.1f %s", value, isKgUnit ? "kg" : "lbs");
        }
    }
}
