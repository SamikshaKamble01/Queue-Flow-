package com.example.queuemanagementsystem.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queuemanagementsystem.R;
import com.example.queuemanagementsystem.data.model.TokenItem;
import com.example.queuemanagementsystem.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class TokenHistoryAdapter extends RecyclerView.Adapter<TokenHistoryAdapter.TokenHistoryViewHolder> {

    public interface OnTokenClickListener {
        void onTokenClick(TokenItem item);
    }

    private final List<TokenItem> items = new ArrayList<>();
    private final OnTokenClickListener listener;

    public TokenHistoryAdapter(OnTokenClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<TokenItem> tokens) {
        items.clear();
        items.addAll(tokens);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TokenHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_token_history, parent, false);
        return new TokenHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TokenHistoryViewHolder holder, int position) {
        TokenItem item = items.get(position);
        holder.tokenNumber.setText(item.getTokenNumber());
        holder.serviceName.setText(item.getServiceName());
        holder.slotInfo.setText(item.getSlotDate() + "  •  " + item.getSlotStartTime() + " - " + item.getSlotEndTime());
        holder.status.setText(item.getStatus().toUpperCase());
        holder.createdAt.setText(DateTimeUtils.formatDateTime(item.getCreatedAt()));
        holder.itemView.setOnClickListener(v -> listener.onTokenClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TokenHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tokenNumber;
        TextView serviceName;
        TextView slotInfo;
        TextView status;
        TextView createdAt;

        TokenHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tokenNumber = itemView.findViewById(R.id.text_history_token_number);
            serviceName = itemView.findViewById(R.id.text_history_service_name);
            slotInfo = itemView.findViewById(R.id.text_history_slot_info);
            status = itemView.findViewById(R.id.text_history_status);
            createdAt = itemView.findViewById(R.id.text_history_created_at);
        }
    }
}