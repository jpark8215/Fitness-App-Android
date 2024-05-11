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

public class ExerciseRecyclerViewAdapter extends RecyclerView.Adapter<ExerciseRecyclerViewAdapter.ViewHolder> {

    private final List<ExerciseItem> list;
    private final OnItemLongSelectedListener itemLongSelectedListener;
    private final OnButtonClickListener buttonClickListener;
    private static final String LOG_TAG = "ExerciseRecyclerViewAdaptor";

//    private boolean isWeightInPounds = false; // Default weight unit is kg
//    public void setWeightUnitInPounds(boolean isInPounds) {
//        isWeightInPounds = isInPounds;
//        notifyDataSetChanged(); // Refresh the RecyclerView to update the displayed weight units
//    }

    public ExerciseRecyclerViewAdapter(List<ExerciseItem> list,
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

    @SuppressLint("LongLogTag")
    @Override
    public void onBindViewHolder(final ExerciseRecyclerViewAdapter.ViewHolder holder, final int position) {
        ExerciseItem myList = list.get(position);

        holder.textViewExercise.setText(myList.getTitle());
        holder.button1.setText(myList.getButton1());
        holder.button2.setText(myList.getButton2());
        holder.button3.setText(myList.getButton3());
        holder.button4.setText(myList.getButton4());
        holder.button5.setText(myList.getButton5());
        holder.textViewWeight.setText(myList.getWeight().toString() + "kg");

//        // Convert weight to the desired unit
//        String weightUnit = isWeightInPounds ? "lbs" : "kg";
//        holder.textViewWeight.setText(myList.getWeight().toString() + weightUnit);


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

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewExercise;
        //not sure what this id is used for, can probably be removed with some testing
        public TextView textViewId;
        public Button button1;
        public Button button2;
        public Button button3;
        public Button button4;
        public Button button5;

        public Boolean button1FirstClick = true;
        public Boolean button2FirstClick = true;
        public Boolean button3FirstClick = true;
        public Boolean button4FirstClick = true;
        public Boolean button5FirstClick = true;

        public TextView textViewWeight;

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

    public interface OnItemLongSelectedListener {
        void onItemLongSelected(String itemId, String itemTitle, Double itemWeight);
    }

    public interface OnButtonClickListener {
        void OnBackPressedDispatcher();

        void onButtonClick(String itemId, String itemTitle, String setSelected, Integer intReps);
    }
}