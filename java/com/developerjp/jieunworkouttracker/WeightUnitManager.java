package com.developerjp.jieunworkouttracker;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class WeightUnitManager {
    private static final String PREFS_NAME = "weight_prefs";
    private static final String KEY_IS_KG_UNIT = "is_kg_unit";
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("0.0");

    // Get the current weight unit preference (true for kg, false for lbs)
    public static boolean isKgUnit(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_KG_UNIT, true); // Default to kg
    }

    // Save weight unit preference
    public static void setKgUnit(Context context, boolean isKg) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_KG_UNIT, isKg);
        editor.apply();
    }

    // Convert weight to the current unit and format it
    public static String formatWeight(Context context, double weightInKg) {
        if (!isKgUnit(context)) {
            // Convert to lbs if needed
            double weightInLbs = weightInKg * 2.20462;
            return NUMBER_FORMAT.format(weightInLbs) + " lbs";
        } else {
            return NUMBER_FORMAT.format(weightInKg) + " kg";
        }
    }

    // Convert from display unit to kg for storage
    public static double toKilograms(Context context, double weight) {
        if (!isKgUnit(context)) {
            // Convert from lbs to kg
            return weight / 2.20462;
        }
        return weight; // Already in kg
    }
} 