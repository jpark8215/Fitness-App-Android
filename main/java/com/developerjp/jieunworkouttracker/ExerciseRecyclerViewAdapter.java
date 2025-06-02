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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExerciseRecyclerViewAdapter extends RecyclerView.Adapter<ExerciseRecyclerViewAdapter.ViewHolder> {

    private static final String LOG_TAG = "ExerciseRecyclerViewAdapter";
    private final List<ExerciseItem> list;
    private final OnItemLongSelectedListener itemLongSelectedListener;
    private final OnButtonClickListener buttonClickListener;
    // Add a set to track selected exercises
    private final Set<String> selectedExerciseIds = new HashSet<>();
    private boolean readOnly = false;

    public ExerciseRecyclerViewAdapter(List<ExerciseItem> list,
                                       Context context,
                                       OnItemLongSelectedListener listener, OnButtonClickListener buttonlistener) {
        this.list = list;
        this.itemLongSelectedListener = listener;
        this.buttonClickListener = buttonlistener;
    }

    /**
     * Get the list of selected exercise IDs
     *
     * @return List of selected exercise IDs
     */
    public List<String> getSelectedExerciseIds() {
        return new ArrayList<>(selectedExerciseIds);
    }

    /**
     * Clear all selections
     */
    public void clearSelections() {
        selectedExerciseIds.clear();
        notifyDataSetChanged();
    }

    /**
     * Toggle selection of an exercise
     *
     * @param exerciseId ID of the exercise to toggle
     */
    public void toggleSelection(String exerciseId) {
        if (selectedExerciseIds.contains(exerciseId)) {
            selectedExerciseIds.remove(exerciseId);
        } else {
            selectedExerciseIds.add(exerciseId);
        }
        notifyDataSetChanged();
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.exercise_item_style, parent, false);

        return new ViewHolder(v);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onBindViewHolder(final ExerciseRecyclerViewAdapter.ViewHolder holder, final int position) {
        ExerciseItem myList = list.get(position);

        holder.textViewExercise.setText(myList.getTitle());

        // Display weight correctly using the displayWeight property 
        // which contains the properly formatted weight with units
        holder.textViewWeight.setText(myList.getDisplayWeight());

        // Handle read-only mode
        if (readOnly) {
            // In read-only mode, disable all interactions
            holder.itemView.setClickable(false);
            holder.itemView.setLongClickable(false);
            holder.button1.setClickable(false);
            holder.button2.setClickable(false);
            holder.button3.setClickable(false);
            holder.button4.setClickable(false);
            holder.button5.setClickable(false);

            // Just display the data with proper colors
            holder.textViewExercise.setText(myList.getTitle());
            holder.button1.setText(myList.getButton1());
            holder.button2.setText(myList.getButton2());
            holder.button3.setText(myList.getButton3());
            holder.button4.setText(myList.getButton4());
            holder.button5.setText(myList.getButton5());

            // Set colors based on improvements
            holder.button1.setBackgroundResource(myList.getButton1colour());
            holder.button2.setBackgroundResource(myList.getButton2colour());
            holder.button3.setBackgroundResource(myList.getButton3colour());
            holder.button4.setBackgroundResource(myList.getButton4colour());
            holder.button5.setBackgroundResource(myList.getButton5colour());
            
            // Exit early since we don't need to set up click listeners in read-only mode
            return;
        }

        // The rest of the method remains the same for interactive mode
        // Show selection state with a more visible indicator
        final String currentId = myList.getId();
        if (selectedExerciseIds.contains(currentId)) {
            // Use a more noticeable background for selected items
            holder.itemView.setBackgroundResource(R.drawable.bg_gradient_soft);
            // Add a checkmark or indicator text to show selection
            holder.textViewExercise.setText("✓ " + myList.getTitle());
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent);
            holder.textViewExercise.setText(myList.getTitle());
        }

        // Set up click listener for selection
        holder.itemView.setOnClickListener(v -> {
            toggleSelection(currentId);
            // Show a toast when an item is selected or unselected
            if (selectedExerciseIds.contains(currentId)) {
                Toast.makeText(v.getContext(), "Exercise selected", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(v.getContext(), "Exercise unselected", Toast.LENGTH_SHORT).show();
            }
        });

        holder.button1.setText(myList.getButton1());
        holder.button2.setText(myList.getButton2());
        holder.button3.setText(myList.getButton3());
        holder.button4.setText(myList.getButton4());
        holder.button5.setText(myList.getButton5());


        //Sets the background colour of the buttons
        holder.button1.setBackgroundResource(myList.getButton1colour());
        holder.button2.setBackgroundResource(myList.getButton2colour());
        holder.button3.setBackgroundResource(myList.getButton3colour());
        holder.button4.setBackgroundResource(myList.getButton4colour());
        holder.button5.setBackgroundResource(myList.getButton5colour());


        final String currentTitle = myList.getTitle();
        final Double currentWeight = myList.getWeight();

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
        holder.button1.setOnClickListener(v -> {
            if (buttonClickListener != null) {
                Log.d(LOG_TAG, " Button1");
                String setSelected = "set1";
                int intReps = Integer.parseInt(holder.button1.getText().toString());
                intReps += 1;
                buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps);
                holder.button1.setBackgroundResource(R.drawable.button_shape_blue);
                holder.button1.setText(Integer.toString(intReps));
            }
        });

        holder.button2.setOnClickListener(v -> {
            if (buttonClickListener != null) {
                Log.d(LOG_TAG, " Button2");
                String setSelected = "set2";
                int intReps = Integer.parseInt(holder.button2.getText().toString());
                intReps += 1;
                buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps);
                holder.button2.setBackgroundResource(R.drawable.button_shape_blue);
                holder.button2.setText(Integer.toString(intReps));
            }
        });

        holder.button3.setOnClickListener(v -> {
            if (buttonClickListener != null) {
                Log.d(LOG_TAG, " Button3");
                String setSelected = "set3";
                int intReps = Integer.parseInt(holder.button3.getText().toString());
                intReps += 1;
                buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps);
                holder.button3.setBackgroundResource(R.drawable.button_shape_blue);
                holder.button3.setText(Integer.toString(intReps));
            }
        });

        holder.button4.setOnClickListener(v -> {
            if (buttonClickListener != null) {
                Log.d(LOG_TAG, " Button4");
                String setSelected = "set4";
                int intReps = Integer.parseInt(holder.button4.getText().toString());
                intReps += 1;
                buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps);
                holder.button4.setBackgroundResource(R.drawable.button_shape_blue);
                holder.button4.setText(Integer.toString(intReps));
            }
        });

        holder.button5.setOnClickListener(v -> {
            if (buttonClickListener != null) {
                Log.d(LOG_TAG, " Button5");
                String setSelected = "set5";
                int intReps = Integer.parseInt(holder.button5.getText().toString());
                intReps += 1;
                buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps);
                holder.button5.setBackgroundResource(R.drawable.button_shape_blue);
                holder.button5.setText(Integer.toString(intReps));
            }
        });


        //Handle long button clicks
        //Have a look to see if there is a way to handle user action when the button is held down for some time
        holder.button1.setOnLongClickListener(v -> {
            if (buttonClickListener != null) {
                Log.d(LOG_TAG, " Button1 long");
                String setSelected = "set1";
                int intReps = Integer.parseInt(holder.button1.getText().toString());
                intReps -= 1;
                buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps);
                holder.button1.setText(Integer.toString(intReps));
            }
            return true;
        });

        holder.button2.setOnLongClickListener(v -> {
            if (buttonClickListener != null) {
                Log.d(LOG_TAG, " Button2 long");
                String setSelected = "set2";
                int intReps = Integer.parseInt(holder.button2.getText().toString());
                intReps -= 1;
                buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps);
                holder.button2.setText(Integer.toString(intReps));
            }
            return true;
        });

        holder.button3.setOnLongClickListener(v -> {
            if (buttonClickListener != null) {
                Log.d(LOG_TAG, " Button3 long");
                String setSelected = "set3";
                int intReps = Integer.parseInt(holder.button3.getText().toString());
                intReps -= 1;
                buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps);
                holder.button3.setText(Integer.toString(intReps));
            }
            return true;
        });

        holder.button4.setOnLongClickListener(v -> {
            if (buttonClickListener != null) {
                Log.d(LOG_TAG, " Button4 long");
                String setSelected = "set4";
                int intReps = Integer.parseInt(holder.button4.getText().toString());
                intReps -= 1;
                buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps);
                holder.button4.setText(Integer.toString(intReps));
            }
            return true;
        });

        holder.button5.setOnLongClickListener(v -> {
            if (buttonClickListener != null) {
                Log.d(LOG_TAG, " Button5 long");
                String setSelected = "set5";
                int intReps = Integer.parseInt(holder.button5.getText().toString());
                intReps -= 1;
                buttonClickListener.onButtonClick(currentId, currentTitle, setSelected, intReps);
                holder.button5.setText(Integer.toString(intReps));
            }
            return true;
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

        void onButtonClick(String itemId, String itemTitle, String setSelected, Integer intReps);
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