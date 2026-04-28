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
import com.example.queuemanagementsystem.data.model.TokenItem;
import com.example.queuemanagementsystem.data.repository.QueueRepository;
import com.example.queuemanagementsystem.ui.adapter.TokenHistoryAdapter;
import com.example.queuemanagementsystem.utils.Constants;
import com.example.queuemanagementsystem.utils.SimpleListCallback;
import com.example.queuemanagementsystem.utils.ThemeManager;
import com.example.queuemanagementsystem.utils.UiUtils;

import java.util.List;

public class TokenHistoryFragment extends Fragment {

    private final QueueRepository queueRepository = new QueueRepository();

    public TokenHistoryFragment() {
        super(R.layout.fragment_token_history);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProgressBar progressBar = view.findViewById(R.id.progress_history);
        TextView emptyView = view.findViewById(R.id.text_history_empty);
        TextView title = view.findViewById(R.id.text_history_title);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_history);
        ThemeManager.applyLargeTextPreference(requireContext(), title, emptyView);

        TokenHistoryAdapter adapter = new TokenHistoryAdapter(item -> {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.ARG_TOKEN_ID, item.getTokenId());
            Navigation.findNavController(view).navigate(R.id.action_history_to_token, bundle);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        progressBar.setVisibility(View.VISIBLE);
        queueRepository.getUserTokenHistory(new SimpleListCallback<TokenItem>() {
            @Override
            public void onSuccess(List<TokenItem> data) {
                progressBar.setVisibility(View.GONE);
                adapter.submitList(data);
                emptyView.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception exception) {
                progressBar.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        });
    }
}