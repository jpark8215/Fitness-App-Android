package com.developerjp.jieunworkouttracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;

import java.util.Objects;


public class SettingsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private Toolbar toolbar;
    private Switch switchTheme;
    private boolean isAdLoaded = false;
    private AdView adView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize MobileAds
        MobileAds.initialize(this, initializationStatus -> Log.d("Ads", "Initialization status: " + initializationStatus));

        // Apply theme using ThemeManager
        ThemeManager.applyTheme(this);

        setContentView(R.layout.activity_menu_drawer_simple_light);

        //Use view stubs to programmatically change the include view at runtime
        ViewStub stub = findViewById(R.id.main_view_stub);
        stub.setLayoutResource(R.layout.activity_settings_screen);
        stub.inflate();

        // Initialize and load the ad AFTER layout inflation
        try {
            adView2 = findViewById(R.id.adView2);
            if (adView2 != null) {
                try {
                    AdRequest adRequest = new AdRequest.Builder().build();
                    adView2.setAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.e("Ads", "Ad failed to load: " + loadAdError.getMessage());
                            isAdLoaded = false;
                        }

                        @Override
                        public void onAdLoaded() {
                            Log.d("Ads", "Ad loaded successfully");
                            isAdLoaded = true;
                        }
                    });
                    adView2.loadAd(adRequest);
                } catch (Exception ex) {
                    Log.e("Ads", "Error setting up ad: " + ex.getMessage());
                    isAdLoaded = false;
                }
            } else {
                Log.e("Ads", "AdView not found in layout");
                isAdLoaded = false;
            }
        } catch (Exception e) {
            Log.e("Ads", "Error finding AdView: " + e.getMessage());
            isAdLoaded = false;
        }

        //Sets up the toolbar, navigation menu and switch
        initToolbar();
        initNavigationMenu();
        initWeightUnitControls();
        initUserNameControl();
        initSwitch(ThemeManager.isDarkModeEnabled(this));
    }

    private void initWeightUnitControls() {
        // Initialize the weight unit setting controls
        RadioGroup weightUnitRadioGroup = findViewById(R.id.weightUnitRadioGroup);
        RadioButton radioKg = findViewById(R.id.radioKg);
        RadioButton radioLbs = findViewById(R.id.radioLbs);

        // Load current setting from WeightUnitManager
        boolean isKgUnit = WeightUnitManager.isKgUnit(this);

        // Set the radio button
        if (isKgUnit) {
            radioKg.setChecked(true);
        } else {
            radioLbs.setChecked(true);
        }

        // Handle changes in selection
        weightUnitRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean newIsKgUnit = (checkedId == R.id.radioKg);
            // Save the setting
            WeightUnitManager.setKgUnit(this, newIsKgUnit);
            Log.d("Weight Unit", "Changed to: " + (newIsKgUnit ? "kg" : "lbs"));

            // Toast disabled due to SystemUI APK I/O error on device
            // Toast.makeText(this,
            //         "Weight unit changed to " + (newIsKgUnit ? "kilograms" : "pounds"),
            //         Toast.LENGTH_SHORT).show();
        });
    }

    private void initUserNameControl() {
        EditText etUserName = findViewById(R.id.etUserName);
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
        String savedName = sharedPreferences.getString("user_name", "Champion");

        if (etUserName != null) {
            etUserName.setText(savedName);
            etUserName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    String name = s.toString().trim();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("user_name", name.isEmpty() ? "Champion" : name);
                    editor.apply();
                }
            });
        }
    }

    private void initSwitch(Boolean darkModeEnabled) {
        //Used for the light/dark theme switch
        switchTheme = findViewById(R.id.switchTheme);

        if (darkModeEnabled) {
            switchTheme.setOnCheckedChangeListener(null);
            switchTheme.setChecked(true);
            switchTheme.setText("Dark");
            switchTheme.setOnCheckedChangeListener(this);
        } else {
            switchTheme.setOnCheckedChangeListener(null);
            switchTheme.setChecked(false);
            switchTheme.setText("Light");
            switchTheme.setOnCheckedChangeListener(this);
        }
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("");

        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText("Settings");

        //Hides the chronometer as we don't need it for this activity
        Chronometer simpleChronometer = findViewById(R.id.simpleChronometer);
        simpleChronometer.setVisibility(View.GONE);
    }

    private void initNavigationMenu() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_settings);
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

    public void bottomNavigationHomeClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivityExerciseList.class);
        startActivity(intent);
    }

    public void bottomNavigationCalendarClick(View view) {
        Intent intent = new Intent(getApplicationContext(), ShowCalendarActivity.class);
        startActivity(intent);
    }

    public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
        if (!isAdLoaded) {
            // If ad is not loaded, show a message and don't change theme
            Toast.makeText(this, "Please wait while the app is initializing...", Toast.LENGTH_SHORT).show();
            // Reset the switch to its previous state
            switchTheme.setOnCheckedChangeListener(null);
            switchTheme.setChecked(!isChecked);
            switchTheme.setText(!isChecked ? "Dark" : "Light");
            switchTheme.setOnCheckedChangeListener(this);
            return;
        }

        // Check if there's an ongoing workout service and stop it to prevent unwanted notifications
        if (ServiceUtils.isWorkoutServiceRunning(this)) {
            Log.d("SettingsActivity", "Stopping ongoing workout service before theme change");
            ServiceUtils.stopWorkoutService(this);
        } else {
            // Even if service is not running, clear any lingering notifications
            ServiceUtils.clearAllNotifications(this);
        }

        if (isChecked) {
            //do stuff when Switch is ON
            switchTheme.setText("Dark");
            ThemeManager.setDarkMode(this, true);
        } else {
            //do stuff when Switch if OFF
            switchTheme.setText("Light");
            ThemeManager.setDarkMode(this, false);
        }
        ThemeManager.applyTheme(this);
        recreate();
    }

    @Override
    protected void onPause() {
        // Pause the AdView to prevent memory leaks
        if (adView2 != null) {
            adView2.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the AdView
        if (adView2 != null) {
            adView2.resume();
        }
    }

    @Override
    protected void onDestroy() {
        // Destroy the AdView to prevent memory leaks
        if (adView2 != null) {
            adView2.destroy();
        }
        super.onDestroy();
    }
}