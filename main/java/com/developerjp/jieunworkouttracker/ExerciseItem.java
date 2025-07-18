package com.developerjp.jieunworkouttracker;

public class ExerciseItem {
    private String id;
    private String title;
    private String button1;
    private int button1colour;
    private String button2;
    private int button2colour;
    private String button3;
    private int button3colour;
    private String button4;
    private int button4colour;
    private String button5;
    private int button5colour;
    private double weight;
    private String displayWeight;

    public void setButton1Colour(int button1colour) {
        this.button1colour = button1colour;
    }

    public void setButton2Colour(int button2colour) {
        this.button2colour = button2colour;
    }

    public void setButton3Colour(int button3colour) {
        this.button3colour = button3colour;
    }

    public void setButton4Colour(int button4colour) {
        this.button4colour = button4colour;
    }

    public void setButton5Colour(int button5colour) {
        this.button5colour = button5colour;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getButton1() {
        return button1;
    }

    public void setButton1(String button1) {
        this.button1 = button1;
    }

    public int getButton1colour() {
        return button1colour;
    }

    public String getButton2() {
        return button2;
    }

    public void setButton2(String button2) {
        this.button2 = button2;
    }

    public int getButton2colour() {
        return button2colour;
    }

    public String getButton3() {
        return button3;
    }

    public void setButton3(String button3) {
        this.button3 = button3;
    }

    public int getButton3colour() {
        return button3colour;
    }

    public String getButton4() {
        return button4;
    }

    public void setButton4(String button4) {
        this.button4 = button4;
    }

    public int getButton4colour() {
        return button4colour;
    }

    public String getButton5() {
        return button5;
    }

    public void setButton5(String button5) {
        this.button5 = button5;
    }

    public int getButton5colour() {
        return button5colour;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        // Store the weight with exactly one decimal place
        this.weight = WeightUtils.formatToOneDecimal(weight);
        // Set a default display format in kg, will be updated by updateWeightDisplay if needed
        this.displayWeight = WeightUtils.formatWeight(this.weight, true);
    }

    public String getDisplayWeight() {
        return displayWeight;
    }

    public void setDisplayWeight(String displayWeight) {
        this.displayWeight = displayWeight;
    }
}