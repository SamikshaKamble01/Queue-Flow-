package com.example.queuemanagementsystem.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queuemanagementsystem.R;
import com.example.queuemanagementsystem.data.model.TokenItem;

import java.util.ArrayList;
import java.util.List;

public class PendingTokenAdapter extends RecyclerView.Adapter<PendingTokenAdapter.PendingTokenViewHolder> {

    private final List<TokenItem> items = new ArrayList<>();

    public void submitList(List<TokenItem> tokens) {
        items.clear();
        items.addAll(tokens);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PendingTokenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_token, parent, false);
        return new PendingTokenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingTokenViewHolder holder, int position) {
        TokenItem item = items.get(position);
        holder.token.setText(item.getTokenNumber());
        holder.queueNumber.setText("Queue #" + item.getQueueNumber());
        holder.status.setText(item.getStatus().toUpperCase());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PendingTokenViewHolder extends RecyclerView.ViewHolder {
        TextView token;
        TextView queueNumber;
        TextView status;

        PendingTokenViewHolder(@NonNull View itemView) {
            super(itemView);
            token = itemView.findViewById(R.id.text_pending_token);
            queueNumber = itemView.findViewById(R.id.text_pending_queue_number);
            status = itemView.findViewById(R.id.text_pending_status);
        }
    }
}