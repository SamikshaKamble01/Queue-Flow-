package com.example.queuemanagementsystem.ui.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queuemanagementsystem.R;
import com.example.queuemanagementsystem.data.model.SlotItem;
import com.example.queuemanagementsystem.data.repository.QueueRepository;
import com.example.queuemanagementsystem.ui.adapter.SlotAdapter;
import com.example.queuemanagementsystem.utils.Constants;
import com.example.queuemanagementsystem.utils.SimpleListCallback;
import com.example.queuemanagementsystem.utils.UiUtils;
import com.example.queuemanagementsystem.utils.VoidCallback;

import java.util.List;

public class SlotManagementFragment extends Fragment {

    private final QueueRepository queueRepository = new QueueRepository();
    private String serviceId;
    private SlotAdapter adapter;

    public SlotManagementFragment() {
        super(R.layout.fragment_slot_management);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        serviceId = args != null ? args.getString(Constants.ARG_SERVICE_ID) : null;
        String serviceName = args != null ? args.getString(Constants.ARG_SERVICE_NAME, "Service") : "Service";

        TextView title = view.findViewById(R.id.text_slot_admin_title);
        EditText dateInput = view.findViewById(R.id.input_slot_date);
        EditText startInput = view.findViewById(R.id.input_slot_start);
        EditText endInput = view.findViewById(R.id.input_slot_end);
        EditText capacityInput = view.findViewById(R.id.input_slot_capacity);
        Button createButton = view.findViewById(R.id.button_create_slot);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_admin_slots);

        title.setText(serviceName + " slot management");
        adapter = new SlotAdapter(false, slot -> {
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        loadSlots();

        createButton.setOnClickListener(v -> {
            String date = dateInput.getText().toString().trim();
            String start = startInput.getText().toString().trim();
            String end = endInput.getText().toString().trim();
            String capacityText = capacityInput.getText().toString().trim();

            if (TextUtils.isEmpty(date) || TextUtils.isEmpty(start)
                    || TextUtils.isEmpty(end) || TextUtils.isEmpty(capacityText)) {
                UiUtils.toast(requireContext(), "Fill all slot fields.");
                return;
            }

            createButton.setEnabled(false);
            queueRepository.createSlot(serviceId, date, start, end, Long.parseLong(capacityText), new VoidCallback() {
                @Override
                public void onSuccess() {
                    createButton.setEnabled(true);
                    dateInput.setText("");
                    startInput.setText("");
                    endInput.setText("");
                    capacityInput.setText("");
                    UiUtils.toast(requireContext(), "Slot created.");
                    loadSlots();
                }

                @Override
                public void onError(Exception exception) {
                    createButton.setEnabled(true);
                    UiUtils.toast(requireContext(), exception.getMessage());
                }
            });
        });
    }

    private void loadSlots() {
        queueRepository.getSlots(serviceId, new SimpleListCallback<SlotItem>() {
            @Override
            public void onSuccess(List<SlotItem> data) {
                adapter.submitList(data);
            }

            @Override
            public void onError(Exception exception) {
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        });
    }
}