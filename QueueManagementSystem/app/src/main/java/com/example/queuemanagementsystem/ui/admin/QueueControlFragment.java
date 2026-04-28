package com.example.queuemanagementsystem.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.ListenerRegistration;
import com.example.queuemanagementsystem.R;
import com.example.queuemanagementsystem.data.model.QueueStatus;
import com.example.queuemanagementsystem.data.model.TokenItem;
import com.example.queuemanagementsystem.data.repository.QueueRepository;
import com.example.queuemanagementsystem.ui.adapter.PendingTokenAdapter;
import com.example.queuemanagementsystem.utils.Constants;
import com.example.queuemanagementsystem.utils.SimpleCallback;
import com.example.queuemanagementsystem.utils.SimpleListCallback;
import com.example.queuemanagementsystem.utils.UiUtils;
import com.example.queuemanagementsystem.utils.VoidCallback;

import java.util.List;

public class QueueControlFragment extends Fragment {

    private final QueueRepository queueRepository = new QueueRepository();
    private ListenerRegistration queueListener;
    private PendingTokenAdapter adapter;
    private String serviceId;
    private boolean paused;

    public QueueControlFragment() {
        super(R.layout.fragment_queue_control);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        serviceId = args != null ? args.getString(Constants.ARG_SERVICE_ID) : null;
        String serviceName = args != null ? args.getString(Constants.ARG_SERVICE_NAME, "Queue") : "Queue";

        TextView title = view.findViewById(R.id.text_queue_title);
        TextView currentToken = view.findViewById(R.id.text_queue_current_token);
        TextView queueState = view.findViewById(R.id.text_queue_state);
        Button nextButton = view.findViewById(R.id.button_next_token);
        Button skipButton = view.findViewById(R.id.button_skip_token);
        Button pauseButton = view.findViewById(R.id.button_pause_queue);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_pending_tokens);

        title.setText(serviceName + " control");
        adapter = new PendingTokenAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        loadPendingTokens();
        queueListener = queueRepository.observeQueueStatus(serviceId, new SimpleCallback<QueueStatus>() {
            @Override
            public void onSuccess(QueueStatus data) {
                paused = data.isPaused();
                currentToken.setText(data.getCurrentTokenNumber());
                queueState.setText(paused ? "Queue paused" : "Queue active");
                pauseButton.setText(paused ? R.string.queue_btn_resume : R.string.queue_btn_pause);
            }

            @Override
            public void onError(Exception exception) {
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        });

        nextButton.setOnClickListener(v -> queueRepository.callNextToken(serviceId, new VoidCallback() {
            @Override
            public void onSuccess() {
                UiUtils.toast(requireContext(), "Moved to next token.");
                loadPendingTokens();
            }

            @Override
            public void onError(Exception exception) {
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        }));

        skipButton.setOnClickListener(v -> queueRepository.skipCurrentToken(serviceId, new VoidCallback() {
            @Override
            public void onSuccess() {
                UiUtils.toast(requireContext(), "Skipped current token.");
                loadPendingTokens();
            }

            @Override
            public void onError(Exception exception) {
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        }));

        pauseButton.setOnClickListener(v -> queueRepository.togglePauseQueue(serviceId, !paused, new VoidCallback() {
            @Override
            public void onSuccess() {
                paused = !paused;
                pauseButton.setText(paused ? R.string.queue_btn_resume : R.string.queue_btn_pause);
                queueState.setText(paused ? "Queue paused" : "Queue active");
            }

            @Override
            public void onError(Exception exception) {
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        }));
    }

    private void loadPendingTokens() {
        queueRepository.getPendingTokensForService(serviceId, new SimpleListCallback<TokenItem>() {
            @Override
            public void onSuccess(List<TokenItem> data) {
                adapter.submitList(data);
            }

            @Override
            public void onError(Exception exception) {
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (queueListener != null) {
            queueListener.remove();
        }
    }
}