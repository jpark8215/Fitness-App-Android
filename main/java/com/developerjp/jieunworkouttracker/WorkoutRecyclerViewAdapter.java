package com.developerjp.jieunworkouttracker;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class
WorkoutRecyclerViewAdapter extends RecyclerView.Adapter<WorkoutRecyclerViewAdapter.ViewHolder> {

    private static final String LOG_TAG = "WorkoutRecyclerViewAdaptor";
    private final List<ExerciseItem> list;
    private final OnItemLongSelectedListener itemLongSelectedListener;
    private final OnButtonClickListener buttonClickListener;


    public WorkoutRecyclerViewAdapter(List<ExerciseItem> list,
                                      Context context,
                                      OnItemLongSelectedListener listener, OnButtonClickListener buttonlistener) {
        this.list = list;
        this.itemLongSelectedListener = listener;
        this.buttonClickListener = buttonlistener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.exercise_item_style, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final WorkoutRecyclerViewAdapter.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        ExerciseItem myList = list.get(position);

        holder.textViewExercise.setText(myList.getTitle());
        holder.button1.setText(myList.getButton1());
        holder.button2.setText(myList.getButton2());
        holder.button3.setText(myList.getButton3());
        holder.button4.setText(myList.getButton4());
        holder.button5.setText(myList.getButton5());
        holder.textViewWeight.setText(myList.getDisplayWeight());


        //Sets the background colour of the buttons
        holder.button1.setBackgroundResource(myList.getButton1colour());
        holder.button2.setBackgroundResource(myList.getButton2colour());
        holder.button3.setBackgroundResource(myList.getButton3colour());
        holder.button4.setBackgroundResource(myList.getButton4colour());
        holder.button5.setBackgroundResource(myList.getButton5colour());


        final String currentId = myList.getId();
        final String currentTitle = myList.getTitle();
        final Double currentWeight = myList.getWeight();

        holder.itemView.setOnClickListener(v -> {

        });


        holder.itemView.setOnLongClickListener(v -> {
            TextView text = v.findViewById(R.id.textViewHead);
            Context context = v.getContext();
            Intent intent = new Intent();
            if (itemLongSelectedListener != null) {
                itemLongSelectedListener.onItemLongSelected(currentId, currentTitle, currentWeight);
            }
            return true;
        });

        //Handle normal button clicks
        holder.button1.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                if (buttonClickListener != null) {

                    //If its the first button click we just want to change the button colour to keep track of the workout & record an improvement value in the LOGS table
                    //If its the second button click then we will increment the rep and record it to the database
                    if (holder.button1FirstClick) {
                        Log.d(LOG_TAG, "1st button1");
                        holder.button1FirstClick = false;
                        holder.button1.setBackgroundResource(R.drawable.button_shape_green);
                        // Save the color in the item
                        list.get(position).setButton1Colour(R.drawable.button_shape_green);

                        String setSelected = "set1";
                        Integer intReps = Integer.parseInt(holder.button1.getText().toString());
                        // Save the text back to the item
                        list.get(position).setButton1(String.valueOf(intReps));
                        buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps, 2);
                    } else {
                        Log.d(LOG_TAG, "2nd button1");
                        String setSelected = "set1";
                        int intReps = Integer.parseInt(holder.button1.getText().toString());
                        intReps += 1;
                        buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps, 2);
                        holder.button1.setBackgroundResource(R.drawable.button_shape_green);
                        // Save the color in the item
                        list.get(position).setButton1Colour(R.drawable.button_shape_green);
                        // Update UI and data model
                        holder.button1.setText(Integer.toString(intReps));
                        list.get(position).setButton1(Integer.toString(intReps));
                        Log.d(LOG_TAG, "Button1 updated to: " + intReps);
                    }
                }
            }
        });

        holder.button2.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                if (buttonClickListener != null) {

                    if (holder.button2FirstClick) {
                        Log.d(LOG_TAG, "1st button2");
                        holder.button2FirstClick = false;
                        holder.button2.setBackgroundResource(R.drawable.button_shape_green);
                        // Save the color in the item
                        list.get(position).setButton2Colour(R.drawable.button_shape_green);

                        String setSelected = "set2";
                        Integer intReps = Integer.parseInt(holder.button2.getText().toString());
                        // Save the text back to the item
                        list.get(position).setButton2(String.valueOf(intReps));
                        buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps, 2);
                    } else {
                        Log.d(LOG_TAG, "2nd button2");
                        String setSelected = "set2";
                        int intReps = Integer.parseInt(holder.button2.getText().toString());
                        intReps += 1;
                        buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps, 2);
                        holder.button2.setBackgroundResource(R.drawable.button_shape_green);
                        // Save the color in the item
                        list.get(position).setButton2Colour(R.drawable.button_shape_green);
                        // Update UI and data model
                        holder.button2.setText(Integer.toString(intReps));
                        list.get(position).setButton2(Integer.toString(intReps));
                        Log.d(LOG_TAG, "Button2 updated to: " + intReps);
                    }
                }
            }
        });

        holder.button3.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                if (buttonClickListener != null) {

                    if (holder.button3FirstClick) {
                        Log.d(LOG_TAG, "1st button3");
                        holder.button3FirstClick = false;
                        holder.button3.setBackgroundResource(R.drawable.button_shape_green);
                        // Save the color in the item
                        list.get(position).setButton3Colour(R.drawable.button_shape_green);

                        String setSelected = "set3";
                        Integer intReps = Integer.parseInt(holder.button3.getText().toString());
                        // Save the text back to the item
                        list.get(position).setButton3(String.valueOf(intReps));
                        buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps, 2);
                    } else {
                        Log.d(LOG_TAG, "2nd button3");
                        String setSelected = "set3";
                        int intReps = Integer.parseInt(holder.button3.getText().toString());
                        intReps += 1;
                        buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps, 2);
                        holder.button3.setBackgroundResource(R.drawable.button_shape_green);
                        // Save the color in the item
                        list.get(position).setButton3Colour(R.drawable.button_shape_green);
                        // Update UI and data model
                        holder.button3.setText(Integer.toString(intReps));
                        list.get(position).setButton3(Integer.toString(intReps));
                        Log.d(LOG_TAG, "Button3 updated to: " + intReps);
                    }
                }
            }
        });

        holder.button4.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                if (buttonClickListener != null) {

                    if (holder.button4FirstClick) {
                        Log.d(LOG_TAG, "1st button4");
                        holder.button4FirstClick = false;
                        holder.button4.setBackgroundResource(R.drawable.button_shape_green);
                        // Save the color in the item
                        list.get(position).setButton4Colour(R.drawable.button_shape_green);

                        String setSelected = "set4";
                        Integer intReps = Integer.parseInt(holder.button4.getText().toString());
                        // Save the text back to the item
                        list.get(position).setButton4(String.valueOf(intReps));
                        buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps, 2);
                    } else {
                        Log.d(LOG_TAG, "2nd button4");
                        String setSelected = "set4";
                        int intReps = Integer.parseInt(holder.button4.getText().toString());
                        intReps += 1;
                        buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps, 2);
                        holder.button4.setBackgroundResource(R.drawable.button_shape_green);
                        // Save the color in the item
                        list.get(position).setButton4Colour(R.drawable.button_shape_green);
                        // Update UI and data model
                        holder.button4.setText(Integer.toString(intReps));
                        list.get(position).setButton4(Integer.toString(intReps));
                        Log.d(LOG_TAG, "Button4 updated to: " + intReps);
                    }
                }
            }
        });

        holder.button5.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                if (buttonClickListener != null) {

                    if (holder.button5FirstClick) {
                        Log.d(LOG_TAG, "1st button5");
                        holder.button5FirstClick = false;
                        holder.button5.setBackgroundResource(R.drawable.button_shape_green);
                        // Save the color in the item
                        list.get(position).setButton5Colour(R.drawable.button_shape_green);

                        String setSelected = "set5";
                        Integer intReps = Integer.parseInt(holder.button5.getText().toString());
                        // Save the text back to the item
                        list.get(position).setButton5(String.valueOf(intReps));
                        buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps, 2);
                    } else {
                        Log.d(LOG_TAG, "2nd button5");
                        String setSelected = "set5";
                        int intReps = Integer.parseInt(holder.button5.getText().toString());
                        intReps += 1;
                        buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps, 2);
                        holder.button5.setBackgroundResource(R.drawable.button_shape_green);
                        // Save the color in the item
                        list.get(position).setButton5Colour(R.drawable.button_shape_green);
                        // Update UI and data model
                        holder.button5.setText(Integer.toString(intReps));
                        list.get(position).setButton5(Integer.toString(intReps));
                        Log.d(LOG_TAG, "Button5 updated to: " + intReps);
                    }
                }
            }
        });


        //Handle long button clicks
        //Have a look to see if there is a way to handle user action when the button is held down for some time
        holder.button1.setOnLongClickListener(new View.OnLongClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public boolean onLongClick(View v) {
                if (buttonClickListener != null) {
                    Log.d(LOG_TAG, " Button1 long");
                    holder.button1FirstClick = false;
                    String setSelected = "set1";
                    int intReps = Integer.parseInt(holder.button1.getText().toString());
                    intReps -= 1;
                    buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps, 1);
                    holder.button1.setBackgroundResource(R.drawable.button_shape_red);
                    // Save the color in the item
                    list.get(position).setButton1Colour(R.drawable.button_shape_red);
                    // Update UI and data model
                    holder.button1.setText(Integer.toString(intReps));
                    list.get(position).setButton1(Integer.toString(intReps));
                    Log.d(LOG_TAG, "Button1 updated to: " + intReps + " (long press)");
                }
                return true;
            }
        });

        holder.button2.setOnLongClickListener(new View.OnLongClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public boolean onLongClick(View v) {
                if (buttonClickListener != null) {
                    Log.d(LOG_TAG, " Button2 long");
                    holder.button2FirstClick = false;
                    String setSelected = "set2";
                    int intReps = Integer.parseInt(holder.button2.getText().toString());
                    intReps -= 1;
                    buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps, 1);
                    holder.button2.setBackgroundResource(R.drawable.button_shape_red);
                    // Save the color in the item
                    list.get(position).setButton2Colour(R.drawable.button_shape_red);
                    // Update UI and data model
                    holder.button2.setText(Integer.toString(intReps));
                    list.get(position).setButton2(Integer.toString(intReps));
                    Log.d(LOG_TAG, "Button2 updated to: " + intReps + " (long press)");
                }
                return true;
            }
        });

        holder.button3.setOnLongClickListener(new View.OnLongClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public boolean onLongClick(View v) {
                if (buttonClickListener != null) {
                    Log.d(LOG_TAG, " Button3 long");
                    holder.button3FirstClick = false;
                    String setSelected = "set3";
                    int intReps = Integer.parseInt(holder.button3.getText().toString());
                    intReps -= 1;
                    buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps, 1);
                    holder.button3.setBackgroundResource(R.drawable.button_shape_red);
                    // Save the color in the item
                    list.get(position).setButton3Colour(R.drawable.button_shape_red);
                    // Update UI and data model
                    holder.button3.setText(Integer.toString(intReps));
                    list.get(position).setButton3(Integer.toString(intReps));
                    Log.d(LOG_TAG, "Button3 updated to: " + intReps + " (long press)");
                }
                return true;
            }
        });

        holder.button4.setOnLongClickListener(new View.OnLongClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public boolean onLongClick(View v) {
                if (buttonClickListener != null) {
                    Log.d(LOG_TAG, " Button4 long");
                    holder.button4FirstClick = false;
                    String setSelected = "set4";
                    int intReps = Integer.parseInt(holder.button4.getText().toString());
                    intReps -= 1;
                    buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps, 1);
                    holder.button4.setBackgroundResource(R.drawable.button_shape_red);
                    // Save the color in the item
                    list.get(position).setButton4Colour(R.drawable.button_shape_red);
                    // Update UI and data model
                    holder.button4.setText(Integer.toString(intReps));
                    list.get(position).setButton4(Integer.toString(intReps));
                    Log.d(LOG_TAG, "Button4 updated to: " + intReps + " (long press)");
                }
                return true;
            }
        });

        holder.button5.setOnLongClickListener(new View.OnLongClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public boolean onLongClick(View v) {
                if (buttonClickListener != null) {
                    Log.d(LOG_TAG, " Button5 long");
                    holder.button5FirstClick = false;
                    String setSelected = "set5";
                    int intReps = Integer.parseInt(holder.button5.getText().toString());
                    intReps -= 1;
                    buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps, 1);
                    holder.button5.setBackgroundResource(R.drawable.button_shape_red);
                    // Save the color in the item
                    list.get(position).setButton5Colour(R.drawable.button_shape_red);
                    // Update UI and data model
                    holder.button5.setText(Integer.toString(intReps));
                    list.get(position).setButton5(Integer.toString(intReps));
                    Log.d(LOG_TAG, "Button5 updated to: " + intReps + " (long press)");
                }
                return true;
            }
        });

    }


    @Override
    public int getItemCount() {

        return list.size();
    }


    public interface OnItemLongSelectedListener {
        void onItemLongSelected(String itemId, String itemTitle, Double itemWeight);
    }

    public interface OnButtonClickListener {
        void OnBackPressedDispatcher();

        void onButtonClick(String itemId, String itemTitle, String setSelected, Integer intReps, Integer intImprovement);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView textViewExercise;
        //not sure what this id is used for, can probably be removed with some testing
        public TextView textViewId;
        public final Button button1;
        public final Button button2;
        public final Button button3;
        public final Button button4;
        public final Button button5;
        public Boolean button1FirstClick = true;
        public Boolean button2FirstClick = true;
        public Boolean button3FirstClick = true;
        public Boolean button4FirstClick = true;
        public Boolean button5FirstClick = true;
        public final TextView textViewWeight;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewExercise = itemView.findViewById(R.id.exercise);
            button1 = itemView.findViewById(R.id.button1);
            button2 = itemView.findViewById(R.id.button2);
            button3 = itemView.findViewById(R.id.button3);
            button4 = itemView.findViewById(R.id.button4);
            button5 = itemView.findViewById(R.id.button5);
            textViewWeight = itemView.findViewById(R.id.weight);
        }
    }

}