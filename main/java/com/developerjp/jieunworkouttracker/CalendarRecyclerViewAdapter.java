package com.developerjp.jieunworkouttracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CalendarRecyclerViewAdapter extends RecyclerView.Adapter<CalendarRecyclerViewAdapter.ViewHolder> {

    private static final String LOG_TAG = "CalendarAdapter";
    private final List<CalendarItem> list;
    private final Context mContext;
    private final RecyclerView rView;
    private final OnItemSelectedListener onItemSelectedListener;

    //Constructor for this class. Takes the list of calendar items, the current context, the recyclerView, and the OnItemSelectedListener
    public CalendarRecyclerViewAdapter(List<CalendarItem> list, Context context, RecyclerView rView, OnItemSelectedListener onItemSelectedListener) {
        this.list = list;
        this.mContext = context;
        this.rView = rView;
        this.onItemSelectedListener = onItemSelectedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        if (list == null || list.isEmpty()) {
            Log.d(LOG_TAG, "List is empty in onBindViewHolder");
            return;
        }
        
        CalendarItem calendarItem = list.get(position);
        Log.d(LOG_TAG, "Binding item at position " + position + ": " + calendarItem.getTitle() + 
              ", workout_id: " + calendarItem.getWorkoutId() + ", date: " + calendarItem.getDate());
              
        holder.title.setText(calendarItem.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (onItemSelectedListener != null) {
                String workoutId = calendarItem.getWorkoutId();
                String title = calendarItem.getTitle();
                String date = calendarItem.getDate();
                
                // Check if we have a valid workout ID
                if (workoutId == null || workoutId.isEmpty()) {
                    Log.w(LOG_TAG, "No workout_id for item: " + title);
                    return;
                }
                
                Log.d(LOG_TAG, "Item clicked: " + title + ", passing workout_id: " + workoutId + ", date: " + date);
                onItemSelectedListener.onItemSelected(workoutId, title, date);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected final TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
        }
    }

    // Define the listener interface
    public interface OnItemSelectedListener {
        void onItemSelected(String itemId, String itemTitle, String itemDate);
    }
}