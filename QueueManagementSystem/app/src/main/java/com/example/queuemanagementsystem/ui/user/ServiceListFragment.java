package com.example.queuemanagementsystem.ui.user;

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
import com.example.queuemanagementsystem.data.model.ServiceItem;
import com.example.queuemanagementsystem.data.model.TokenItem;
import com.example.queuemanagementsystem.data.repository.AuthRepository;
import com.example.queuemanagementsystem.data.repository.QueueRepository;
import com.example.queuemanagementsystem.ui.adapter.ServiceAdapter;
import com.example.queuemanagementsystem.utils.Constants;
import com.example.queuemanagementsystem.utils.SimpleCallback;
import com.example.queuemanagementsystem.utils.SimpleListCallback;
import com.example.queuemanagementsystem.utils.ThemeManager;
import com.example.queuemanagementsystem.utils.UiUtils;

import java.util.List;

public class ServiceListFragment extends Fragment {

    private final QueueRepository queueRepository = new QueueRepository();
    private final AuthRepository authRepository = new AuthRepository();
    private ServiceAdapter adapter;

    public ServiceListFragment() {
        super(R.layout.fragment_service_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_services);
        ProgressBar progressBar = view.findViewById(R.id.progress_services);
        Button myTokenButton = view.findViewById(R.id.button_my_token);
        Button logoutButton = view.findViewById(R.id.button_logout_user);
        Button historyButton = view.findViewById(R.id.button_history);
        Button profileButton = view.findViewById(R.id.button_profile);
        TextView title = view.findViewById(R.id.text_user_home_title);
        TextView subtitle = view.findViewById(R.id.text_user_home_subtitle);
        ThemeManager.applyLargeTextPreference(requireContext(), title, subtitle);

        adapter = new ServiceAdapter(item -> {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.ARG_SERVICE_ID, item.getServiceId());
            bundle.putString(Constants.ARG_SERVICE_NAME, item.getName());
            bundle.putString(Constants.ARG_SERVICE_PREFIX, item.getPrefix());
            bundle.putLong(Constants.ARG_AVG_MINUTES, item.getAvgServiceMinutes());
            Navigation.findNavController(view).navigate(R.id.action_services_to_slots, bundle);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        progressBar.setVisibility(View.VISIBLE);
        queueRepository.getServices(new SimpleListCallback<ServiceItem>() {
            @Override
            public void onSuccess(List<ServiceItem> data) {
                progressBar.setVisibility(View.GONE);
                adapter.submitList(data);
                subtitle.setText("Choose from " + data.size() + " active services and book instantly.");
            }

            @Override
            public void onError(Exception exception) {
                progressBar.setVisibility(View.GONE);
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        });

        historyButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_services_to_history));

        profileButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_services_to_profile));

        myTokenButton.setOnClickListener(v -> queueRepository.getLatestTokenForCurrentUser(new SimpleCallback<TokenItem>() {
            @Override
            public void onSuccess(TokenItem data) {
                Bundle bundle = new Bundle();
                bundle.putString(Constants.ARG_TOKEN_ID, data.getTokenId());
                Navigation.findNavController(view).navigate(R.id.action_services_to_token, bundle);
            }

            @Override
            public void onError(Exception exception) {
                UiUtils.toast(requireContext(), "Book a token first.");
            }
        }));

        logoutButton.setOnClickListener(v -> authRepository.logout(new com.example.queuemanagementsystem.utils.VoidCallback() {
            @Override
            public void onSuccess() {
                Navigation.findNavController(view).navigate(R.id.action_services_to_login);
            }

            @Override
            public void onError(Exception exception) {
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        }));
    }
}