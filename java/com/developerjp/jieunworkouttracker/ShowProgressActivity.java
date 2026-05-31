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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ShowProgressActivity extends AppCompatActivity {

    // Exercise data
    private final List<String> exerciseNames = new ArrayList<>();
    private final Map<String, String> exerciseIdToNameMap = new HashMap<>();
    private final Map<String, String> exerciseNameToIdMap = new HashMap<>();
    private Toolbar toolbar;
    private LineChart chart;
    private DBManager dbManager;
    private Spinner exerciseSpinner;
    private android.widget.RadioGroup dateFilterRadioGroup;
    private TextView txtMaxWeight;
    private TextView txtTotalSets;
    private TextView txtMaxLabel;

    private enum DateFilter {
        DAYS_7,
        DAYS_30,
        MONTHS_3,
        ALL_TIME
    }

    private enum MetricMode {
        WEIGHT,
        SETS,
        REPS
    }

    private DateFilter selectedDateFilter = DateFilter.DAYS_7;
    private MetricMode selectedMetricMode = MetricMode.WEIGHT;
    private String selectedExerciseId = null;

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

        exerciseSpinner = findViewById(R.id.exercise_spinner);
        dateFilterRadioGroup = findViewById(R.id.date_filter_radio_group);
        android.widget.RadioGroup metricToggleGroup = findViewById(R.id.metric_toggle_group);
        txtMaxWeight = findViewById(R.id.txt_max_weight);
        txtTotalSets = findViewById(R.id.txt_total_sets);
        txtMaxLabel = findViewById(R.id.txt_max_label);

        if (dateFilterRadioGroup != null) {
            dateFilterRadioGroup.check(R.id.btn_filter_7d);
        }
        selectedDateFilter = DateFilter.DAYS_7;

        if (dateFilterRadioGroup != null) {
            dateFilterRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.btn_filter_7d) selectedDateFilter = DateFilter.DAYS_7;
                else if (checkedId == R.id.btn_filter_30d) selectedDateFilter = DateFilter.DAYS_30;
                else if (checkedId == R.id.btn_filter_3m) selectedDateFilter = DateFilter.MONTHS_3;
                else selectedDateFilter = DateFilter.ALL_TIME;

                // Update button backgrounds based on selection
                updateDateFilterButtonBackgrounds(checkedId);

                refreshChart();
            });

            // Set initial background for checked button
            updateDateFilterButtonBackgrounds(R.id.btn_filter_7d);
        }

        if (metricToggleGroup != null) {
            metricToggleGroup.check(R.id.btn_metric_weight);
            metricToggleGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.btn_metric_weight) selectedMetricMode = MetricMode.WEIGHT;
                else if (checkedId == R.id.btn_metric_sets) selectedMetricMode = MetricMode.SETS;
                else if (checkedId == R.id.btn_metric_reps) selectedMetricMode = MetricMode.REPS;

                // Update button backgrounds based on selection
                updateMetricButtonBackgrounds(checkedId);

                refreshChart();
            });

            // Set initial background for checked button
            updateMetricButtonBackgrounds(R.id.btn_metric_weight);
        }

        dbManager = new DBManager(this);
        loadExerciseList();

        chart.setPinchZoom(false);
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
        int labelColor = darkModeEnabled ? Color.parseColor("#E2E2E2") : Color.parseColor("#3E4E56");
        int gridColor = darkModeEnabled ? Color.parseColor("#424242") : Color.parseColor("#E0E0E0");
        xAxis.setTextColor(labelColor);
        leftAxis.setTextColor(labelColor);
        xAxis.setGridColor(gridColor);
        leftAxis.setGridColor(gridColor);

        // Set chart background based on theme
        if (darkModeEnabled) {
            chart.setBackgroundColor(Color.parseColor("#252836"));
            chart.setGridBackgroundColor(Color.parseColor("#252836"));
        } else {
            chart.setBackgroundColor(Color.WHITE);
            chart.setGridBackgroundColor(Color.WHITE);
        }

        chart.animateY(500);
        chart.getLegend().setEnabled(false);

        // Set initial no data text with proper dark mode colors
        chart.setNoDataText("Select an exercise to view progress");
        int noDataTextColor = darkModeEnabled ? Color.parseColor("#ADADAD") : Color.parseColor("#5F7380");
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

    private void loadExerciseList() {
        exerciseNames.clear();
        exerciseIdToNameMap.clear();
        exerciseNameToIdMap.clear();

        dbManager.open();
        Cursor cursor = dbManager.getAllExercises();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String exerciseName = cursor.getString(1);
                String exerciseId = cursor.getString(0);

                Log.d("Exercise", "Name: " + exerciseName + ", ID: " + exerciseId);
                exerciseNames.add(exerciseName);
                exerciseIdToNameMap.put(exerciseId, exerciseName);
                exerciseNameToIdMap.put(exerciseName, exerciseId);
            } while (cursor.moveToNext());
            cursor.close();
        }

        dbManager.close();

        setupExerciseSpinner(new ArrayList<>(new LinkedHashSet<>(exerciseNames)));
    }

    private void updateDateFilterButtonBackgrounds(int checkedId) {
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);

        int[] buttonIds = {R.id.btn_filter_7d, R.id.btn_filter_30d, R.id.btn_filter_3m, R.id.btn_filter_all};

        for (int buttonId : buttonIds) {
            android.widget.RadioButton rb = findViewById(buttonId);
            if (rb != null) {
                if (buttonId == checkedId) {
                    if (darkModeEnabled) {
                        rb.setBackgroundResource(R.drawable.chip_selector_selected_dark);
                    } else {
                        rb.setBackgroundResource(R.drawable.chip_selector_selected_light);
                    }
                } else {
                    if (darkModeEnabled) {
                        rb.setBackgroundResource(R.drawable.chip_selector_dark);
                    } else {
                        rb.setBackgroundResource(R.drawable.chip_selector_light);
                    }
                }
            }
        }
    }

    private void updateMetricButtonBackgrounds(int checkedId) {
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);

        int[] buttonIds = {R.id.btn_metric_weight, R.id.btn_metric_sets, R.id.btn_metric_reps};

        for (int buttonId : buttonIds) {
            android.widget.RadioButton rb = findViewById(buttonId);
            if (rb != null) {
                if (buttonId == checkedId) {
                    if (darkModeEnabled) {
                        rb.setBackgroundResource(R.drawable.chip_selector_selected_dark);
                    } else {
                        rb.setBackgroundResource(R.drawable.chip_selector_selected_light);
                    }
                } else {
                    if (darkModeEnabled) {
                        rb.setBackgroundResource(R.drawable.chip_selector_dark);
                    } else {
                        rb.setBackgroundResource(R.drawable.chip_selector_light);
                    }
                }
            }
        }
    }

    private void setupExerciseSpinner(List<String> uniqueExerciseNames) {
        if (exerciseSpinner == null) return;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            uniqueExerciseNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exerciseSpinner.setAdapter(adapter);

        exerciseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = uniqueExerciseNames.get(position);
                selectedExerciseId = exerciseNameToIdMap.get(name);
                refreshChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        if (!uniqueExerciseNames.isEmpty()) {
            selectedExerciseId = exerciseNameToIdMap.get(uniqueExerciseNames.get(0));
        }
    }

    private void refreshChart() {
        if (chart == null) return;
        chart.clear();

        if (selectedExerciseId == null) {
            chart.setNoDataText("Select an exercise to view progress");
            chart.invalidate();
            updateStats(null, 0);
            return;
        }

        Cursor cursor = null;
        ArrayList<Entry> lineEntries = new ArrayList<>();
        int totalSets = 0;
        int totalReps = 0;
        Float maxWeight = null;
        Integer maxSets = null;
        Integer maxReps = null;
        float weightSum = 0;
        int weightCount = 0;
        boolean isKgUnit = WeightUnitManager.isKgUnit(this);
        
        try {
            dbManager.open();
            List<String> selectedExerciseIds = new ArrayList<>();
            selectedExerciseIds.add(selectedExerciseId);

            cursor = dbManager.getExerciseLogProgress(selectedExerciseIds);

            long cutoffMillis = getCutoffMillis(selectedDateFilter);

            // Aggregate data by date to handle multiple entries per day
            Map<Integer, Float> weightByDay = new HashMap<>();
            Map<Integer, Integer> setsByDay = new HashMap<>();
            Map<Integer, Integer> repsByDay = new HashMap<>();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int weightColumnIndex = cursor.getColumnIndex(DatabaseHelper.WEIGHT);
                    int dateColumnIndex = cursor.getColumnIndex(DatabaseHelper.DATE);

                    if (weightColumnIndex == -1 || dateColumnIndex == -1) continue;

                    String exerciseWeight = cursor.getString(weightColumnIndex);
                    String exerciseDate = cursor.getString(dateColumnIndex);
                    if (exerciseWeight == null || exerciseDate == null) continue;

                    long dateMillis = parseDateMillis(exerciseDate);
                    if (cutoffMillis > 0 && dateMillis > 0 && dateMillis < cutoffMillis) {
                        continue;
                    }

                    String dayOfTheYear = convertDate(exerciseDate);
                    if (dayOfTheYear == null || dayOfTheYear.isEmpty()) continue;

                    int day = Integer.parseInt(dayOfTheYear);

                    // Count sets for this entry
                    int setsForEntry = countCompletedSets(cursor);
                    totalSets += setsForEntry;

                    // Count reps for this entry
                    int repsForEntry = countTotalReps(cursor);
                    totalReps += repsForEntry;

                    // Aggregate weight by day (keep max weight for each day)
                    float weight = Float.parseFloat(exerciseWeight);
                    if (!isKgUnit) {
                        weight = (float) WeightUtils.kgToLbs(weight);
                    }
                    if (!weightByDay.containsKey(day) || weight > weightByDay.get(day)) {
                        weightByDay.put(day, weight);
                    }

                    // Track weight sum and count for average calculation
                    weightSum += weight;
                    weightCount++;

                    // Aggregate sets by day (sum sets for each day)
                    setsByDay.put(day, setsByDay.getOrDefault(day, 0) + setsForEntry);

                    // Aggregate reps by day (sum reps for each day)
                    repsByDay.put(day, repsByDay.getOrDefault(day, 0) + repsForEntry);

                    // Track max weight
                    if (maxWeight == null || weight > maxWeight) {
                        maxWeight = weight;
                    }

                    // Track max sets
                    if (maxSets == null || setsForEntry > maxSets) {
                        maxSets = setsForEntry;
                    }

                    // Track max reps
                    if (maxReps == null || repsForEntry > maxReps) {
                        maxReps = repsForEntry;
                    }
                } while (cursor.moveToNext());
            }

            // Create entries from aggregated data
            if (selectedMetricMode == MetricMode.WEIGHT) {
                for (Map.Entry<Integer, Float> entry : weightByDay.entrySet()) {
                    lineEntries.add(new Entry(entry.getKey(), entry.getValue()));
                }
            } else if (selectedMetricMode == MetricMode.SETS) {
                for (Map.Entry<Integer, Integer> entry : setsByDay.entrySet()) {
                    lineEntries.add(new Entry(entry.getKey(), entry.getValue()));
                }
            } else {
                for (Map.Entry<Integer, Integer> entry : repsByDay.entrySet()) {
                    lineEntries.add(new Entry(entry.getKey(), entry.getValue()));
                }
            }

            dbManager.close();

            if (lineEntries.isEmpty()) {
                chart.setNoDataText("No progress data available for this exercise");
                applyNoDataTextColor();
                chart.invalidate();
                updateStats(null, 0);
                return;
            }
        } catch (Exception e) {
            Log.e("ShowProgressActivity", "Error refreshing chart: " + e.getMessage(), e);
            chart.setNoDataText("Error loading progress data");
            applyNoDataTextColor();
            chart.invalidate();
            updateStats(null, 0);
            try {
                dbManager.close();
            } catch (Exception ignored) {
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // Main line dataset
        String label = selectedMetricMode == MetricMode.WEIGHT ? "Weight" : 
                      selectedMetricMode == MetricMode.SETS ? "Sets" : "Reps";
        LineDataSet main = new LineDataSet(lineEntries, label);
        main.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        main.setCubicIntensity(0.2f);
        
        // Enable circles for single points or when there are few points
        if (lineEntries.size() == 1) {
            main.setDrawCircles(true);
            main.setCircleRadius(8f);
            main.setCircleHoleRadius(4f);
        } else {
            main.setDrawCircles(false);
        }
        
        main.setDrawValues(false);
        main.setLineWidth(2.5f);

        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);
        int mainColor = darkModeEnabled ? Color.parseColor("#98D8C8") : Color.parseColor("#7F9DBC");

        main.setColor(mainColor);
        main.setCircleColor(mainColor);
        main.setFillColor(mainColor);
        main.setDrawFilled(true);
        main.setFillAlpha(30);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(main);

        chart.setData(new LineData(dataSets));

        // Y axis label formatter
        YAxis leftAxis = chart.getAxisLeft();
        if (selectedMetricMode == MetricMode.WEIGHT) {
            leftAxis.setValueFormatter(new WeightAxisValueFormatter(isKgUnit));
        } else {
            leftAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.format("%.0f", value);
                }
            });
        }

        // Axis ranges
        setChartRanges(lineEntries);

        // Dark mode text colors
        int labelColor = darkModeEnabled ? Color.parseColor("#E2E2E2") : Color.parseColor("#3E4E56");
        chart.getXAxis().setTextColor(labelColor);
        leftAxis.setTextColor(labelColor);

        chart.notifyDataSetChanged();
        chart.invalidate();

        if (selectedMetricMode == MetricMode.WEIGHT) {
            float avgWeight = weightCount > 0 ? weightSum / weightCount : 0;
            updateStatsForWeight(maxWeight, avgWeight, totalSets);
        } else if (selectedMetricMode == MetricMode.SETS) {
            updateStatsForSets(maxSets, totalSets);
        } else {
            updateStatsForReps(maxReps, totalReps);
        }
    }

    private void applyNoDataTextColor() {
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);
        int noDataTextColor = darkModeEnabled ? Color.parseColor("#ADADAD") : Color.parseColor("#5F7380");
        chart.setNoDataTextColor(noDataTextColor);
    }

    private void updateStatsForWeight(@Nullable Float maxWeight, float avgWeight, int totalSets) {
        boolean isKgUnit = WeightUnitManager.isKgUnit(this);
        TextView txtTotalLabel = findViewById(R.id.txt_total_label);
        if (txtMaxLabel != null) {
            txtMaxLabel.setText("Max Weight");
        }
        if (txtMaxWeight != null) {
            if (maxWeight == null) txtMaxWeight.setText("—");
            else txtMaxWeight.setText(String.format("%.1f %s", maxWeight, isKgUnit ? "kg" : "lbs"));
        }
        if (txtTotalLabel != null) {
            txtTotalLabel.setText("Avg Weight");
        }
        if (txtTotalSets != null) {
            if (avgWeight == 0) txtTotalSets.setText("—");
            else txtTotalSets.setText(String.format("%.1f %s", avgWeight, isKgUnit ? "kg" : "lbs"));
        }
    }

    private void updateStats(@Nullable Float maxWeight, int totalSets) {
        boolean isKgUnit = WeightUnitManager.isKgUnit(this);
        if (txtMaxLabel != null) {
            txtMaxLabel.setText("Max Weight");
        }
        if (txtMaxWeight != null) {
            if (maxWeight == null) txtMaxWeight.setText("—");
            else txtMaxWeight.setText(String.format("%.1f %s", maxWeight, isKgUnit ? "kg" : "lbs"));
        }
        if (txtTotalSets != null) {
            txtTotalSets.setText(String.valueOf(totalSets));
        }
    }

    private void updateStatsForSets(@Nullable Integer maxSets, int totalSets) {
        TextView txtTotalLabel = findViewById(R.id.txt_total_label);
        if (txtMaxLabel != null) {
            txtMaxLabel.setText("Max Sets");
        }
        if (txtMaxWeight != null) {
            if (maxSets == null) txtMaxWeight.setText("—");
            else txtMaxWeight.setText(String.valueOf(maxSets));
        }
        if (txtTotalLabel != null) {
            txtTotalLabel.setText("Total Sets");
        }
        if (txtTotalSets != null) {
            txtTotalSets.setText(String.valueOf(totalSets));
        }
    }

    private void updateStatsForReps(@Nullable Integer maxReps, int totalReps) {
        TextView txtTotalLabel = findViewById(R.id.txt_total_label);
        if (txtMaxLabel != null) {
            txtMaxLabel.setText("Max Reps");
        }
        if (txtMaxWeight != null) {
            if (maxReps == null) txtMaxWeight.setText("—");
            else txtMaxWeight.setText(String.valueOf(maxReps));
        }
        if (txtTotalLabel != null) {
            txtTotalLabel.setText("Total Reps");
        }
        if (txtTotalSets != null) {
            txtTotalSets.setText(String.valueOf(totalReps));
        }
    }

    private int countCompletedSets(Cursor cursor) {
        int count = 0;
        count += isSetCompleted(cursor, DatabaseHelper.SET1) ? 1 : 0;
        count += isSetCompleted(cursor, DatabaseHelper.SET2) ? 1 : 0;
        count += isSetCompleted(cursor, DatabaseHelper.SET3) ? 1 : 0;
        count += isSetCompleted(cursor, DatabaseHelper.SET4) ? 1 : 0;
        count += isSetCompleted(cursor, DatabaseHelper.SET5) ? 1 : 0;
        return count;
    }

    private int countTotalReps(Cursor cursor) {
        int total = 0;
        total += getSetRepCount(cursor, DatabaseHelper.SET1);
        total += getSetRepCount(cursor, DatabaseHelper.SET2);
        total += getSetRepCount(cursor, DatabaseHelper.SET3);
        total += getSetRepCount(cursor, DatabaseHelper.SET4);
        total += getSetRepCount(cursor, DatabaseHelper.SET5);
        return total;
    }

    private int getSetRepCount(Cursor cursor, String columnName) {
        int idx = cursor.getColumnIndex(columnName);
        if (idx == -1) return 0;
        if (cursor.isNull(idx)) return 0;
        try {
            return cursor.getInt(idx);
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean isSetCompleted(Cursor cursor, String columnName) {
        int idx = cursor.getColumnIndex(columnName);
        if (idx == -1) return false;
        if (cursor.isNull(idx)) return false;
        try {
            int value = cursor.getInt(idx);
            return value > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private long parseDateMillis(String yyyyMmDd) {
        try {
            // yyyy-MM-dd -> Calendar millis
            int year = Integer.parseInt(yyyyMmDd.substring(0, 4));
            int month = Integer.parseInt(yyyyMmDd.substring(5, 7)) - 1;
            int day = Integer.parseInt(yyyyMmDd.substring(8, 10));
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTimeInMillis();
        } catch (Exception e) {
            return -1;
        }
    }

    private long getCutoffMillis(DateFilter filter) {
        if (filter == DateFilter.ALL_TIME) return -1;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        switch (filter) {
            case DAYS_7:
                cal.add(Calendar.DAY_OF_YEAR, -7);
                break;
            case DAYS_30:
                cal.add(Calendar.DAY_OF_YEAR, -30);
                break;
            case MONTHS_3:
                cal.add(Calendar.MONTH, -3);
                break;
            default:
                return -1;
        }
        return cal.getTimeInMillis();
    }

    private void setChartRanges(ArrayList<Entry> entries) {
        if (entries.isEmpty()) return;

        float minWeight = Float.MAX_VALUE;
        float maxWeight = Float.MIN_VALUE;
        float minDay = Float.MAX_VALUE;
        float maxDay = Float.MIN_VALUE;

        for (Entry entry : entries) {
            minWeight = Math.min(minWeight, entry.getY());
            maxWeight = Math.max(maxWeight, entry.getY());
            minDay = Math.min(minDay, entry.getX());
            maxDay = Math.max(maxDay, entry.getX());
        }

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(Math.max(0, minWeight - 5));
        leftAxis.setAxisMaximum(maxWeight + 5);

        XAxis xAxis = chart.getXAxis();
        xAxis.setAxisMinimum(minDay - 1);
        xAxis.setAxisMaximum(maxDay + 1);

        // Keep the chart readable: show at least ~7 days, and cap zoom-out to ~60 days.
        chart.setVisibleXRangeMinimum(7f);
        chart.setVisibleXRangeMaximum(60f);
        chart.moveViewToX(maxDay + 1);
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("");

        Chronometer simpleChronometer = findViewById(R.id.simpleChronometer);
        simpleChronometer.setVisibility(View.GONE);

        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText("Progress");
    }

    private void initNavigationMenu() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_progress);
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
                        // Pass workout data from WorkoutService
                        intent.putExtra("id", WorkoutService.getWorkoutId());
                        intent.putExtra("title", WorkoutService.getWorkoutTitle());
                        intent.putStringArrayListExtra("selected_exercise_ids", WorkoutService.getWorkoutExerciseIds());
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

            // Calculate days since epoch using a timezone-neutral formula
            // This avoids timezone offset issues
            int[] daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
            
            // Check for leap year
            boolean isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
            if (isLeapYear) {
                daysInMonth[1] = 29;
            }
            
            // Calculate days from 1970-01-01 to the target date
            long totalDays = 0;
            
            // Add days for complete years
            for (int y = 1970; y < year; y++) {
                boolean yIsLeap = (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0);
                totalDays += yIsLeap ? 366 : 365;
            }
            
            // Add days for complete months in the target year
            for (int m = 0; m < monthNumber - 1; m++) {
                totalDays += daysInMonth[m];
            }
            
            // Add days in the target month
            totalDays += dayNumber - 1; // -1 because we count from 0
            
            Log.d("ShowProgressActivity", "Date: " + dateToConvert + ", daysBetween: " + totalDays);
            return String.valueOf(totalDays);
        } catch (Exception e) {
            Log.e("ShowProgressActivity", "Error converting date: " + e.getMessage());
            return null;
        }
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

        // Reload exercise list to include newly added exercises
        loadExerciseList();

        // Check if weight unit preference has changed and refresh chart if needed
        if (chart != null && chart.getData() != null) {
            boolean isKgUnit = WeightUnitManager.isKgUnit(this);
            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setValueFormatter(new WeightAxisValueFormatter(isKgUnit));

            refreshChart();
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
                // Finish the activity
                finish();
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
