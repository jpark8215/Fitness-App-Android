package com.developerjp.jieunworkouttracker;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CalendarRecyclerViewAdapter extends RecyclerView.Adapter<CalendarRecyclerViewAdapter.ViewHolder> {

    private final List<CalendarItem> list;
    private final OnItemLongSelectedListener itemLongSelectedListener;
    private final OnItemSelectedListener itemSelectedListener;

    public CalendarRecyclerViewAdapter(List<CalendarItem> list,
                                       Context context,
                                       OnItemLongSelectedListener longlistener, OnItemSelectedListener listener) {
        this.list = list;
        this.itemLongSelectedListener = longlistener;
        this.itemSelectedListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_style, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final CalendarRecyclerViewAdapter.ViewHolder holder, final int position) {
        CalendarItem myList = list.get(position);

        holder.textViewHead.setText(myList.getTitle());
        final String currentId = myList.getWorkoutId();
        final String currentTitle = myList.getTitle();
        final String date = myList.getDate();

        holder.itemView.setOnClickListener(v -> {

            TextView text = v.findViewById(R.id.textViewHead);
            Context context = v.getContext();
            Intent intent = new Intent();
            if (itemSelectedListener != null) {
                itemSelectedListener.onItemSelected(currentId, currentTitle, date);
            }
        });


        holder.itemView.setOnLongClickListener(v -> {
            TextView text = v.findViewById(R.id.textViewHead);
            Context context = v.getContext();
            Intent intent = new Intent();
            if (itemLongSelectedListener != null) {
                itemLongSelectedListener.onItemLongSelected(currentId, currentTitle);
            }
            return true;
        });
    }


    @Override
    public int getItemCount() {

        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewHead;
        public TextView textViewId;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewHead = itemView.findViewById(R.id.textViewHead);
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(String itemId, String itemTitle, String itemDate);
    }

    public interface OnItemLongSelectedListener {
        void onItemLongSelected(String itemId, String itemTitle);
    }
}