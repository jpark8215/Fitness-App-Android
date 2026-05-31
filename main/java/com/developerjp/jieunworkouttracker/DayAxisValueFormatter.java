package com.developerjp.jieunworkouttracker;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.Calendar;

public class DayAxisValueFormatter extends ValueFormatter {

    public DayAxisValueFormatter(BarLineChartBase<?> chart) {
    }

    @Override
    public String getFormattedValue(float value) {
        int days = (int) value;

        // Handle negative days (dates before 1970)
        if (days < 0) {
            return "";
        }

        // Convert days since epoch (1970-01-01) back to actual date using timezone-neutral formula
        int[] daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        
        int year = 1970;
        int remainingDays = days;
        
        // Find the year
        while (remainingDays >= 365) {
            boolean isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
            int daysInYear = isLeapYear ? 366 : 365;
            if (remainingDays < daysInYear) break;
            remainingDays -= daysInYear;
            year++;
        }
        
        // Check if current year is leap year
        boolean isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
        if (isLeapYear) {
            daysInMonth[1] = 29;
        }
        
        // Find the month
        int month = 0;
        while (month < 12 && remainingDays >= daysInMonth[month]) {
            remainingDays -= daysInMonth[month];
            month++;
        }
        
        // Day is remaining days + 1 (since we count from 0)
        int day = remainingDays + 1;
        
        // Format as M/d/yy
        String monthStr = String.valueOf(month + 1); // month is 0-based
        String dayStr = String.valueOf(day);
        String yearStr = String.valueOf(year).substring(2); // Get last 2 digits

        return monthStr + "/" + dayStr + "/" + yearStr;
    }
}
