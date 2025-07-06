package com.developerjp.jieunworkouttracker;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.formatter.ValueFormatter;


public class DayAxisValueFormatter extends ValueFormatter {

    private final String[] mMonths = new String[]{
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private final BarLineChartBase<?> chart;

    private static final int BASE_YEAR = 2025;
    private static final int NUM_YEARS = 10;
    private static final int DAYS_PER_YEAR = 366; // Leap year safe

    public DayAxisValueFormatter(BarLineChartBase<?> chart) {
        this.chart = chart;
    }

    @Override
    public String getFormattedValue(float value) {
        int days = (int) value;
        
        // Handle negative days (dates before 2025)
        if (days < 0) {
            return "";
        }

        int year = determineYear(days);
        int month = determineMonth(days);
        int dayOfMonth = determineDayOfMonth(days, month);
        
        // Format as M/d/yy
        String monthStr = String.valueOf(month + 1); // month is 0-based
        String dayStr = String.valueOf(dayOfMonth);
        String yearStr = String.valueOf(year).substring(2); // Get last 2 digits
        
        return monthStr + "/" + dayStr + "/" + yearStr;
    }

    private int getDaysForMonth(int month, int year) {

        // month is 0-based

        if (month == 1) {
            boolean is29Feb = false;

            if (year < 1582)
                is29Feb = (year < 1 ? year + 1 : year) % 4 == 0;
            else if (year > 1582)
                is29Feb = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);

            return is29Feb ? 29 : 28;
        }

        if (month == 3 || month == 5 || month == 8 || month == 10)
            return 30;
        else
            return 31;
    }

    private int determineMonth(int dayOfYear) {
        // Calculate the month for the given day offset from BASE_YEAR-01-01
        int year = determineYear(dayOfYear);
        int daysIntoYear = dayOfYear - ((year - BASE_YEAR) * DAYS_PER_YEAR);
        int month = 0;
        int days = 0;
        while (month < 12) {
            int daysForMonth = getDaysForMonth(month, year);
            if (days + daysForMonth > daysIntoYear) {
                break;
            }
            days += daysForMonth;
            month++;
        }
        return month;
    }

    private int determineDayOfMonth(int days, int month) {
        // Calculate the day of the month for the given day offset from BASE_YEAR-01-01
        int year = determineYear(days);
        int daysIntoYear = days - ((year - BASE_YEAR) * DAYS_PER_YEAR);
        int daysForMonths = 0;
        for (int i = 0; i < month; i++) {
            daysForMonths += getDaysForMonth(i, year);
        }
        return daysIntoYear - daysForMonths + 1;
    }

    private int determineYear(int days) {
        // Calculate the year based on the number of days since BASE_YEAR-01-01
        int year = BASE_YEAR + (days / DAYS_PER_YEAR);
        if (year > BASE_YEAR + NUM_YEARS - 1) {
            year = BASE_YEAR + NUM_YEARS - 1;
        }
        return year;
    }
}
