package com.developerjp.jieunworkouttracker;


public class CalendarItem {
    private String title;        // The title associated with the calendar item.
    private String workout_id;   // The unique identifier of the associated workout.
    private String date;         // The date of the calendar item.

    // Setter method to set the title of the calendar item.
    public void setTitle(String title) {
        this.title = title;
    }

    // Setter method to set the workout ID of the calendar item.
    public void setWorkoutId(String workout_id) {
        this.workout_id = workout_id;
    }

    // Setter method to set the date of the calendar item.
    public void setDate(String date) {
        this.date = date;
    }

    // Getter method to retrieve the title of the calendar item.
    public String getTitle() {
        return title;
    }

    // Getter method to retrieve the workout ID of the calendar item.
    public String getWorkoutId() {
        return workout_id;
    }

    // Getter method to retrieve the date of the calendar item.
    public String getDate() {
        return date;
    }
}
