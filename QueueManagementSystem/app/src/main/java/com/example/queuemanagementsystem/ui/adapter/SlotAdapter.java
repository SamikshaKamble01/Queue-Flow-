package com.example.queuemanagementsystem.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queuemanagementsystem.R;
import com.example.queuemanagementsystem.data.model.SlotItem;

import java.util.ArrayList;
import java.util.List;

public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.SlotViewHolder> {

    public interface OnSlotClickListener {
        void onSlotClick(SlotItem item);
    }

    private final List<SlotItem> items = new ArrayList<>();
    private final boolean showActionButton;
    private final OnSlotClickListener listener;

    public SlotAdapter(boolean showActionButton, OnSlotClickListener listener) {
        this.showActionButton = showActionButton;
        this.listener = listener;
    }

    public void submitList(List<SlotItem> slots) {
        items.clear();
        items.addAll(slots);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slot, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        SlotItem item = items.get(position);
        holder.date.setText(item.getDate());
        holder.time.setText(item.getStartTime() + " - " + item.getEndTime());
        holder.capacity.setText(item.getBookedCount() + " / " + item.getCapacity() + " booked");
        holder.button.setVisibility(showActionButton ? View.VISIBLE : View.GONE);
        holder.button.setOnClickListener(v -> listener.onSlotClick(item));
        holder.itemView.setOnClickListener(v -> {
            if (!showActionButton) {
                listener.onSlotClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SlotViewHolder extends RecyclerView.ViewHolder {
        TextView date;
        TextView time;
        TextView capacity;
        Button button;

        SlotViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.text_slot_date);
            time = itemView.findViewById(R.id.text_slot_time);
            capacity = itemView.findViewById(R.id.text_slot_capacity);
            button = itemView.findViewById(R.id.button_slot_action);
        }
    }
}
