package com.developerjp.jieunworkouttracker;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
                .inflate(R.layout.exercise_item_style_simple, parent, false);

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
            if (holder.checkboxSelect != null) {
                holder.checkboxSelect.setVisibility(View.GONE);
            }
            // In read-only mode, disable all interactions
            holder.itemView.setClickable(false);
            holder.itemView.setLongClickable(false);

            // Exit early since we don't need to set up click listeners in read-only mode
            return;
        }

        // The rest of the method remains the same for interactive mode
        final String currentId = myList.getId();
        
        if (holder.checkboxSelect != null) {
            holder.checkboxSelect.setVisibility(View.VISIBLE);
            holder.checkboxSelect.setOnCheckedChangeListener(null);
            holder.checkboxSelect.setChecked(selectedExerciseIds.contains(currentId));
        }

        if (selectedExerciseIds.contains(currentId)) {
            // Use a more noticeable background for selected items
            holder.itemView.setBackgroundResource(R.drawable.bg_gradient_soft);
            holder.textViewExercise.setText(myList.getTitle());
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent);
            holder.textViewExercise.setText(myList.getTitle());
        }

        // Set up click listener for selection
        View.OnClickListener selectionListener = v -> {
            toggleSelection(currentId);
            // Show a toast when an item is selected or unselected
            if (selectedExerciseIds.contains(currentId)) {
                Toast.makeText(v.getContext(), "Exercise selected", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(v.getContext(), "Exercise unselected", Toast.LENGTH_SHORT).show();
            }
        };

        holder.itemView.setOnClickListener(selectionListener);
        
        if (holder.checkboxSelect != null) {
            holder.checkboxSelect.setOnClickListener(v -> {
                // Revert internal state change, let adapter manage state
                holder.checkboxSelect.setChecked(!holder.checkboxSelect.isChecked());
                selectionListener.onClick(v);
            });
        }

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
        public final CheckBox checkboxSelect;
        public final TextView textViewWeight;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewExercise = itemView.findViewById(R.id.exercise);
            checkboxSelect = itemView.findViewById(R.id.checkbox_select);
            textViewWeight = itemView.findViewById(R.id.weight);
        }
    }
}