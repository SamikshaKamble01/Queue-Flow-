package com.example.queuemanagementsystem.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queuemanagementsystem.R;
import com.example.queuemanagementsystem.data.model.AdminAnalytics;
import com.example.queuemanagementsystem.data.model.ServiceItem;
import com.example.queuemanagementsystem.data.repository.AuthRepository;
import com.example.queuemanagementsystem.data.repository.QueueRepository;
import com.example.queuemanagementsystem.ui.adapter.ServiceAdapter;
import com.example.queuemanagementsystem.utils.Constants;
import com.example.queuemanagementsystem.utils.SimpleListCallback;
import com.example.queuemanagementsystem.utils.ThemeManager;
import com.example.queuemanagementsystem.utils.UiUtils;
import com.example.queuemanagementsystem.utils.VoidCallback;

import java.util.List;

public class AdminDashboardFragment extends Fragment {

    private final QueueRepository queueRepository = new QueueRepository();
    private final AuthRepository authRepository = new AuthRepository();
    private ServiceAdapter adapter;
    private ServiceItem selectedService;
    private TextView selectedServiceText;
    private TextView totalTokensText;
    private TextView waitingTokensText;
    private TextView completedTokensText;
    private TextView averageWaitText;

    public AdminDashboardFragment() {
        super(R.layout.fragment_admin_dashboard);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectedServiceText = view.findViewById(R.id.text_selected_service);
        totalTokensText = view.findViewById(R.id.text_analytics_total);
        waitingTokensText = view.findViewById(R.id.text_analytics_waiting);
        completedTokensText = view.findViewById(R.id.text_analytics_completed);
        averageWaitText = view.findViewById(R.id.text_analytics_avg_wait);
        ProgressBar progressBar = view.findViewById(R.id.progress_admin_services);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_admin_services);
        Button queueControlButton = view.findViewById(R.id.button_open_queue_control);
        Button manageSlotsButton = view.findViewById(R.id.button_manage_slots);
        Button scannerButton = view.findViewById(R.id.button_open_scanner);
        Button reportsButton = view.findViewById(R.id.button_open_reports);
        Button logoutButton = view.findViewById(R.id.button_logout_admin);
        ThemeManager.applyLargeTextPreference(requireContext(), selectedServiceText);

        adapter = new ServiceAdapter(item -> {
            selectedService = item;
            adapter.setSelectedServiceId(item.getServiceId());
            selectedServiceText.setText("Selected service: " + item.getName());
            loadAnalytics();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        progressBar.setVisibility(View.VISIBLE);
        queueRepository.getServices(new SimpleListCallback<ServiceItem>() {
            @Override
            public void onSuccess(List<ServiceItem> data) {
                progressBar.setVisibility(View.GONE);
                adapter.submitList(data);
                if (!data.isEmpty()) {
                    selectedService = data.get(0);
                    adapter.setSelectedServiceId(selectedService.getServiceId());
                    selectedServiceText.setText("Selected service: " + selectedService.getName());
                    loadAnalytics();
                }
            }

            @Override
            public void onError(Exception exception) {
                progressBar.setVisibility(View.GONE);
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        });

        queueControlButton.setOnClickListener(v -> openSelected(view, R.id.action_admin_to_queue_control));
        manageSlotsButton.setOnClickListener(v -> openSelected(view, R.id.action_admin_to_slot_management));
        scannerButton.setOnClickListener(v -> openSelected(view, R.id.action_admin_to_scanner));
        reportsButton.setOnClickListener(v -> openSelected(view, R.id.action_admin_to_reports));
        logoutButton.setOnClickListener(v -> authRepository.logout(new VoidCallback() {
            @Override
            public void onSuccess() {
                Navigation.findNavController(view).navigate(R.id.action_admin_to_login);
            }

            @Override
            public void onError(Exception exception) {
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        }));
    }

    private void openSelected(View view, int destination) {
        if (selectedService == null) {
            UiUtils.toast(requireContext(), "Select a service first.");
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString(Constants.ARG_SERVICE_ID, selectedService.getServiceId());
        bundle.putString(Constants.ARG_SERVICE_NAME, selectedService.getName());
        bundle.putString(Constants.ARG_SERVICE_PREFIX, selectedService.getPrefix());
        bundle.putLong(Constants.ARG_AVG_MINUTES, selectedService.getAvgServiceMinutes());
        Navigation.findNavController(view).navigate(destination, bundle);
    }

    private void loadAnalytics() {
        if (selectedService == null || !isAdded()) {
            return;
        }
        queueRepository.getAnalytics(selectedService.getServiceId(), 30, new com.example.queuemanagementsystem.utils.SimpleCallback<AdminAnalytics>() {
            @Override
            public void onSuccess(AdminAnalytics data) {
                totalTokensText.setText(String.valueOf(data.getTotalTokens()));
                waitingTokensText.setText(String.valueOf(data.getWaitingTokens()));
                completedTokensText.setText(String.valueOf(data.getCompletedTokens()));
                averageWaitText.setText(data.getAverageWaitMinutes() + " min");
            }

            @Override
            public void onError(Exception exception) {
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        });
    }
}
