package com.developerjp.jieunworkouttracker;

import java.text.DecimalFormat;

/**
 * Utility class for weight conversions and formatting
 */
public class WeightUtils {
    private static final double KG_TO_LBS = 2.20462;
    private static final double LBS_TO_KG = 0.453592;
    private static final DecimalFormat ONE_DECIMAL_FORMAT = new DecimalFormat("0.0");

    /**
     * Convert kilograms to pounds and format to one decimal place
     */
    public static double kgToLbs(double kg) {
        double lbs = kg * KG_TO_LBS;
        // Format to exactly one decimal place and parse back to double
        return Double.parseDouble(ONE_DECIMAL_FORMAT.format(lbs));
    }

    /**
     * Convert pounds to kilograms and format to one decimal place
     */
    public static double lbsToKg(double lbs) {
        double kg = lbs * LBS_TO_KG;
        // Format to exactly one decimal place and parse back to double
        return Double.parseDouble(ONE_DECIMAL_FORMAT.format(kg));
    }

    /**
     * Format weight to one decimal place and append unit
     */
    public static String formatWeight(double weight, boolean isKg) {
        String formattedWeight = ONE_DECIMAL_FORMAT.format(weight);
        return formattedWeight + (isKg ? " kg" : " lbs");
    }

    /**
     * Format weight to one decimal place only (no unit)
     */
    public static double formatToOneDecimal(double value) {
        return Double.parseDouble(ONE_DECIMAL_FORMAT.format(value));
    }
} 