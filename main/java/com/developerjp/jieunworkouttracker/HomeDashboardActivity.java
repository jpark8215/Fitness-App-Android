package com.developerjp.jieunworkouttracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeDashboardActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_menu_drawer_simple_light);

        ViewStub stub = findViewById(R.id.main_view_stub);
        stub.setLayoutResource(R.layout.activity_home_dashboard);
        stub.inflate();

        initToolbar();
        initNavigationMenu();

        // Initialize database manager
        dbManager = new DBManager(this);
        dbManager.open();

        setupDashboard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
        }

        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText("Home");
        
        View simpleChronometer = findViewById(R.id.simpleChronometer);
        if (simpleChronometer != null) {
            simpleChronometer.setVisibility(View.GONE);
        }
    }

    private void initNavigationMenu() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
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

    private void setupDashboard() {
        // Set Date
        TextView tvDate = findViewById(R.id.tvDate);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
        tvDate.setText(sdf.format(new Date()));

        // Set time-aware greeting
        TextView tvGreeting = findViewById(R.id.tvGreeting);
        if (tvGreeting != null) {
            Calendar calendar = Calendar.getInstance();
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            String greeting;

            if (hourOfDay >= 5 && hourOfDay < 12) {
                greeting = "Good morning";
            } else if (hourOfDay >= 12 && hourOfDay < 17) {
                greeting = "Good afternoon";
            } else if (hourOfDay >= 17 && hourOfDay < 21) {
                greeting = "Good evening";
            } else {
                greeting = "Good night";
            }

            // Get user name from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
            String userName = sharedPreferences.getString("user_name", "Champion");

            tvGreeting.setText(greeting + ", " + userName + " 💪");
        }

        // Start Workout Button
        Button btnStartWorkoutCTA = findViewById(R.id.btnStartWorkoutCTA);
        btnStartWorkoutCTA.setOnClickListener(v -> {
            // Get all exercise IDs and start workout with all exercises
            Cursor cursor = dbManager.getAllExercises();
            ArrayList<String> allExerciseIds = new ArrayList<>();
            
            while (cursor.moveToNext()) {
                int idIndex = cursor.getColumnIndex(DatabaseHelper.EXERCISE_ID);
                if (idIndex != -1) {
                    String exerciseId = cursor.getString(idIndex);
                    allExerciseIds.add(exerciseId);
                }
            }
            cursor.close();
            
            if (allExerciseIds.isEmpty()) {
                // No exercises exist, go to exercise list to add one
                Intent intent = new Intent(this, MainActivityExerciseList.class);
                startActivity(intent);
            } else {
                // Start workout with all exercises
                dbManager.startSelectedExercises(allExerciseIds);
                
                Intent intent = new Intent(this, StartWorkoutActivity.class);
                intent.putStringArrayListExtra("selected_exercise_ids", allExerciseIds);
                startActivity(intent);
            }
        });

        // Setup Streak Days with real data
        setupStreakDays();

        // Load Stats with real data
        loadStats();

        // Load Recent Activities
        loadRecentActivities();
    }

    private void setupStreakDays() {
        LinearLayout llStreakDays = findViewById(R.id.llStreakDays);
        String[] days = {"M", "T", "W", "T", "F", "S", "S"};
        
        // Get the current week's workout days
        Calendar calendar = Calendar.getInstance();
        // Set to the Monday of the current week
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dayOfWeek - Calendar.MONDAY + 7) % 7;
        calendar.add(Calendar.DAY_OF_MONTH, -daysToSubtract);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        boolean[] activeDays = new boolean[7];
        for (int i = 0; i < 7; i++) {
            String dateStr = dateFormat.format(calendar.getTime());
            Cursor cursor = dbManager.fetchExerciseDetailsForDate(dateStr);
            activeDays[i] = cursor.getCount() > 0;
            cursor.close();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Check if dark mode is enabled
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);

        for (int i = 0; i < days.length; i++) {
            View circle = getLayoutInflater().inflate(R.layout.item_streak_day, llStreakDays, false);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            circle.setLayoutParams(params);
            
            TextView tvDay = circle.findViewById(R.id.tvDayChar);
            tvDay.setText(days[i]);
            if (activeDays[i]) {
                circle.setBackgroundResource(R.drawable.bg_streak_circle_active);
                tvDay.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                circle.setBackgroundResource(R.drawable.bg_streak_circle_inactive);
                // Set darker text color for inactive days in dark mode for readability
                if (darkModeEnabled) {
                    tvDay.setTextColor(getResources().getColor(R.color.textPrimaryDarkColor));
                }
            }
            llStreakDays.addView(circle);
        }
    }

    private void loadStats() {
        // Get real stats from database
        int totalWorkouts = dbManager.getTotalWorkouts();
        int streak = dbManager.getCurrentStreak();

        TextView tvTotalWorkouts = findViewById(R.id.tvTotalWorkouts);
        tvTotalWorkouts.setText(String.valueOf(totalWorkouts));
        
        TextView tvStreak = findViewById(R.id.tvStreak);
        tvStreak.setText(String.valueOf(streak));
    }

    private void loadRecentActivities() {
        LinearLayout llRecentActivity = findViewById(R.id.llRecentActivity);
        
        // Get recent activities from database (limit to 5)
        Cursor cursor = dbManager.getRecentActivities(5);
        
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("d", Locale.getDefault());
        
        while (cursor.moveToNext()) {
            String dateStr = cursor.getString(0);
            Long duration = cursor.getLong(1);
            int exerciseCount = cursor.getInt(2);
            
            try {
                Date date = inputFormat.parse(dateStr);
                String dayName = dayFormat.format(date);
                String dayNumber = dateFormat.format(date);
                
                // Format duration
                String durationText;
                if (duration != null && duration > 0) {
                    long minutes = duration / (1000 * 60);
                    durationText = minutes + " min";
                } else {
                    durationText = "N/A";
                }
                
                // Inflate recent activity item
                View activityItem = getLayoutInflater().inflate(R.layout.item_recent_activity, llRecentActivity, false);
                
                TextView tvActivityDay = activityItem.findViewById(R.id.tvActivityDay);
                TextView tvActivityDate = activityItem.findViewById(R.id.tvActivityDate);
                TextView tvActivityTitle = activityItem.findViewById(R.id.tvActivityTitle);
                TextView tvActivityDetails = activityItem.findViewById(R.id.tvActivityDetails);
                
                tvActivityDay.setText(dayName.toUpperCase(Locale.getDefault()));
                tvActivityDate.setText(dayNumber);
                tvActivityTitle.setText("Workout Completed");
                tvActivityDetails.setText(exerciseCount + " exercises • " + durationText);
                
                llRecentActivity.addView(activityItem);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        
        // Show empty state if no activities
        if (llRecentActivity.getChildCount() == 0) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No recent activities yet. Start your first workout!");
            emptyText.setTextColor(getResources().getColor(R.color.textSecondaryColor));
            emptyText.setTextSize(14);
            emptyText.setPadding(16, 32, 16, 32);
            llRecentActivity.addView(emptyText);
        }
    }
}
