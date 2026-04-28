package com.example.queuemanagementsystem.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.example.queuemanagementsystem.R;
import com.example.queuemanagementsystem.data.model.ServiceItem;

import java.util.ArrayList;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    public interface OnServiceClickListener {
        void onServiceClick(ServiceItem item);
    }

    private final List<ServiceItem> items = new ArrayList<>();
    private final OnServiceClickListener listener;
    private String selectedServiceId;

    public ServiceAdapter(OnServiceClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ServiceItem> services) {
        items.clear();
        items.addAll(services);
        notifyDataSetChanged();
    }

    public void setSelectedServiceId(String selectedServiceId) {
        this.selectedServiceId = selectedServiceId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceItem item = items.get(position);
        holder.name.setText(item.getName());
        holder.prefix.setText(item.getPrefix() + " Queue");
        holder.meta.setText("Capacity " + item.getSlotCapacity() + " per slot  •  ETA " + item.getAvgServiceMinutes() + " min/person");
        holder.card.setStrokeWidth(item.getServiceId().equals(selectedServiceId) ? 4 : 1);
        holder.card.setStrokeColor(holder.card.getContext().getColor(
                item.getServiceId().equals(selectedServiceId) ? R.color.brand_primary : android.R.color.transparent
        ));
        holder.itemView.setOnClickListener(v -> listener.onServiceClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView name;
        TextView prefix;
        TextView meta;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            name = itemView.findViewById(R.id.text_service_name);
            prefix = itemView.findViewById(R.id.text_service_prefix);
            meta = itemView.findViewById(R.id.text_service_meta);
        }
    }
}