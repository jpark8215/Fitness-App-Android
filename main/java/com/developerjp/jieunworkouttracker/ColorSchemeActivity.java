package com.developerjp.jieunworkouttracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;


public class ColorSchemeActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private Toolbar toolbar;
    private Switch switchTheme;
    private RadioGroup weightUnitRadioGroup;
    private RadioButton radioKg;
    private RadioButton radioLbs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize MobileAds
        MobileAds.initialize(this, initializationStatus -> {
            Log.d("Ads", "Initialization status: " + initializationStatus);
        });

        // Get a reference to the Shared Preferences object
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);

        // Get the value of the "dark_mode" key, or "false" if it doesn't exist
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);

        // If dark mode is enabled then do the following
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            setTheme(R.style.DarkAppTheme_NoActionBar);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            setTheme(R.style.AppTheme_NoActionBar);
        }

        setContentView(R.layout.activity_menu_drawer_simple_light);

        //Use view stubs to programmatically change the include view at runtime
        ViewStub stub = findViewById(R.id.main_view_stub);
        stub.setLayoutResource(R.layout.activity_color_scheme_screen);
        stub.inflate();

        // Initialize and load the ad AFTER layout inflation
        try {
            AdView adView2 = findViewById(R.id.adView2);
            if (adView2 != null) {
                try {
                    AdRequest adRequest = new AdRequest.Builder().build();
                    adView2.setAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.e("Ads", "Ad failed to load: " + loadAdError.getMessage());
                        }

                        @Override
                        public void onAdLoaded() {
                            Log.d("Ads", "Ad loaded successfully");
                        }
                    });
                    adView2.loadAd(adRequest);
                } catch (Exception ex) {
                    Log.e("Ads", "Error setting up ad: " + ex.getMessage());
                }
            } else {
                Log.e("Ads", "AdView not found in layout");
            }
        } catch (Exception e) {
            Log.e("Ads", "Error finding AdView: " + e.getMessage());
        }

        //Sets up the toolbar, navigation menu and switch
        initToolbar();
        initNavigationMenu();
        initSwitch(darkModeEnabled);
        initWeightUnitControls();
    }

    private void initWeightUnitControls() {
        // Initialize the weight unit setting controls
        weightUnitRadioGroup = findViewById(R.id.weightUnitRadioGroup);
        radioKg = findViewById(R.id.radioKg);
        radioLbs = findViewById(R.id.radioLbs);

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
            
            // Notify user that the setting has been changed
            Toast.makeText(this, 
                "Weight unit changed to " + (newIsKgUnit ? "kilograms" : "pounds"), 
                Toast.LENGTH_SHORT).show();
        });
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
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("");

        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText("Color Scheme");

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
                case "Color Scheme":
                    Log.d("menu item clicked", "Color Scheme");
                    intent = new Intent(getApplicationContext(), ColorSchemeActivity.class);
                    startActivity(intent);
                    break;
            }

            drawer.closeDrawers();
            return true;
        });
    }

    public void bottomNavigationHomeClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivityExerciseList.class);
        startActivity(intent);
    }

    public void bottomNavigationCalendarClick(View view) {
        Intent intent = new Intent(getApplicationContext(), ShowCalendarActivity.class);
        startActivity(intent);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        // Get a reference to the Shared Preferences object
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);

        // Edit the shared preferences object
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (isChecked) {
            //do stuff when Switch is ON
            switchTheme.setText("Dark");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

            // Put the boolean value "true" in the "dark_mode" key
            editor.putBoolean("dark_mode", true);

            // Commit the changes
            editor.apply();


        } else {
            //do stuff when Switch if OFF
            switchTheme.setText("Light");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

            // Put the boolean value "true" in the "dark_mode" key
            editor.putBoolean("dark_mode", false);

            // Commit the changes
            editor.apply();
        }
    }

    @Override
    protected void onPause() {
        // Pause the AdView to prevent memory leaks
        try {
            AdView adView = findViewById(R.id.adView2);
            if (adView != null) {
                adView.pause();
            }
        } catch (Exception e) {
            Log.e("Ads", "Error pausing AdView: " + e.getMessage());
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the AdView
        try {
            AdView adView = findViewById(R.id.adView2);
            if (adView != null) {
                adView.resume();
            }
        } catch (Exception e) {
            Log.e("Ads", "Error resuming AdView: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        // Destroy the AdView to prevent memory leaks
        try {
            AdView adView = findViewById(R.id.adView2);
            if (adView != null) {
                adView.destroy();
            }
        } catch (Exception e) {
            Log.e("Ads", "Error destroying AdView: " + e.getMessage());
        }
        super.onDestroy();
    }
}