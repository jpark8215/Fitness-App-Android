package com.developerjp.jieunworkouttracker;

public class CalendarItem {
    private String workoutId;
    private String title;
    private String date;
    private String logId;
    private String duration;
    private String time;

    // Setter method to set the workout ID of the calendar item.
    public void setWorkoutId(String workoutId) {
        this.workoutId = workoutId;
    }

    // Getter method to retrieve the workout ID of the calendar item.
    public String getWorkoutId() {
        return workoutId;
    }

    // Setter method to set the title of the calendar item.
    public void setTitle(String title) {
        this.title = title;
    }

    // Getter method to retrieve the title of the calendar item.
    public String getTitle() {
        return title;
    }

    // Setter method to set the date of the calendar item.
    public void setDate(String date) {
        this.date = date;
    }

    // Getter method to retrieve the date of the calendar item.
    public String getDate() {
        return date;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
