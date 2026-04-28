package com.example.queuemanagementsystem.ui.user;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queuemanagementsystem.R;
import com.example.queuemanagementsystem.data.model.ServiceItem;
import com.example.queuemanagementsystem.data.model.SlotItem;
import com.example.queuemanagementsystem.data.model.TokenItem;
import com.example.queuemanagementsystem.data.repository.QueueRepository;
import com.example.queuemanagementsystem.ui.adapter.SlotAdapter;
import com.example.queuemanagementsystem.utils.Constants;
import com.example.queuemanagementsystem.utils.SimpleCallback;
import com.example.queuemanagementsystem.utils.SimpleListCallback;
import com.example.queuemanagementsystem.utils.ThemeManager;
import com.example.queuemanagementsystem.utils.UiUtils;

import java.util.List;

public class SlotBookingFragment extends Fragment {

    private final QueueRepository queueRepository = new QueueRepository();
    private String serviceId;
    private String serviceName;
    private long avgMinutes;
    private String rescheduleTokenId;

    public SlotBookingFragment() {
        super(R.layout.fragment_slot_booking);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        serviceId = args != null ? args.getString(Constants.ARG_SERVICE_ID) : null;
        serviceName = args != null ? args.getString(Constants.ARG_SERVICE_NAME, "Service") : "Service";
        avgMinutes = args != null ? args.getLong(Constants.ARG_AVG_MINUTES, 5) : 5;
        rescheduleTokenId = args != null ? args.getString(Constants.ARG_RESCHEDULE_TOKEN_ID) : null;

        TextView title = view.findViewById(R.id.text_booking_title);
        TextView subtitle = view.findViewById(R.id.text_booking_subtitle);
        ProgressBar progressBar = view.findViewById(R.id.progress_slots);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_slots);
        ThemeManager.applyLargeTextPreference(requireContext(), title, subtitle);

        title.setText(serviceName + " slots");
        subtitle.setText(rescheduleTokenId == null
                ? "Pick the best time window and we will generate your token instantly."
                : "Select a new time window to move your existing booking.");

        SlotAdapter adapter = new SlotAdapter(true, slot -> {
            progressBar.setVisibility(View.VISIBLE);
            if (rescheduleTokenId == null) {
                queueRepository.bookToken(serviceId, slot.getSlotId(), buildBookingCallback(view, progressBar));
            } else {
                queueRepository.getTokenById(rescheduleTokenId, new SimpleCallback<TokenItem>() {
                    @Override
                    public void onSuccess(TokenItem token) {
                        queueRepository.rescheduleToken(token, slot.getSlotId(), buildBookingCallback(view, progressBar));
                    }

                    @Override
                    public void onError(Exception exception) {
                        progressBar.setVisibility(View.GONE);
                        UiUtils.toast(requireContext(), exception.getMessage());
                    }
                });
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);



        progressBar.setVisibility(View.VISIBLE);
        queueRepository.getSlots(serviceId, new SimpleListCallback<SlotItem>() {
            @Override
            public void onSuccess(List<SlotItem> data) {
                progressBar.setVisibility(View.GONE);
                adapter.submitList(data);
            }

            @Override
            public void onError(Exception exception) {
                progressBar.setVisibility(View.GONE);
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        });
    }

    private SimpleCallback<TokenItem> buildBookingCallback(View view, ProgressBar progressBar) {
        return new SimpleCallback<TokenItem>() {
            @Override
            public void onSuccess(TokenItem data) {
                progressBar.setVisibility(View.GONE);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.ARG_TOKEN_ID, data.getTokenId());
                bundle.putLong(Constants.ARG_AVG_MINUTES, avgMinutes);
                Navigation.findNavController(view).navigate(R.id.action_slots_to_token, bundle);
            }

            @Override
            public void onError(Exception exception) {
                progressBar.setVisibility(View.GONE);
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        };
    }
}