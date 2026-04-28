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

import com.example.queuemanagementsystem.R;
import com.example.queuemanagementsystem.data.model.TokenItem;
import com.example.queuemanagementsystem.data.repository.QueueRepository;
import com.example.queuemanagementsystem.ui.adapter.TokenHistoryAdapter;
import com.example.queuemanagementsystem.utils.ReportExportHelper;
import com.example.queuemanagementsystem.utils.Constants;
import com.example.queuemanagementsystem.utils.SimpleListCallback;
import com.example.queuemanagementsystem.utils.ThemeManager;
import com.example.queuemanagementsystem.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class AdminReportsFragment extends Fragment {

    private final QueueRepository queueRepository = new QueueRepository();
    private final List<TokenItem> currentTokens = new ArrayList<>();
    private String serviceId;
    private String serviceName;
    private int selectedFilterDays = 1;

    public AdminReportsFragment() {
        super(R.layout.fragment_admin_reports);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        serviceId = args != null ? args.getString(Constants.ARG_SERVICE_ID) : null;
        serviceName = args != null ? args.getString(Constants.ARG_SERVICE_NAME, "Service") : "Service";

        TextView title = view.findViewById(R.id.text_reports_title);
        TextView summary = view.findViewById(R.id.text_reports_summary);
        Button todayButton = view.findViewById(R.id.button_filter_today);
        Button tenDaysButton = view.findViewById(R.id.button_filter_ten_days);
        Button monthButton = view.findViewById(R.id.button_filter_month);
        Button exportCsvButton = view.findViewById(R.id.button_export_csv);
        Button exportPdfButton = view.findViewById(R.id.button_export_pdf);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_reports);
        ThemeManager.applyLargeTextPreference(requireContext(), title, summary);

        title.setText(serviceName + " reports");
        TokenHistoryAdapter adapter = new TokenHistoryAdapter(item -> {
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        View.OnClickListener filterListener = v -> {
            if (v.getId() == R.id.button_filter_today) {
                selectedFilterDays = 1;
            } else if (v.getId() == R.id.button_filter_ten_days) {
                selectedFilterDays = 10;
            } else {
                selectedFilterDays = 30;
            }
            loadReports(adapter, summary);
        };

        todayButton.setOnClickListener(filterListener);
        tenDaysButton.setOnClickListener(filterListener);
        monthButton.setOnClickListener(filterListener);

        exportCsvButton.setOnClickListener(v -> {
            try {
                String fileName = serviceName.replace(" ", "_") + "_report.csv";
                ReportExportHelper.exportCsv(requireContext(), fileName, currentTokens);
                UiUtils.toast(requireContext(), "CSV saved to Downloads/QueueFlowReports");
            } catch (Exception exception) {
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        });

        exportPdfButton.setOnClickListener(v -> {
            try {
                String fileName = serviceName.replace(" ", "_") + "_report.pdf";
                ReportExportHelper.exportPdf(requireContext(), fileName, currentTokens);
                UiUtils.toast(requireContext(), "PDF saved to Downloads/QueueFlowReports");
            } catch (Exception exception) {
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        });

        loadReports(adapter, summary);
    }

    private void loadReports(TokenHistoryAdapter adapter, TextView summary) {
        queueRepository.getReportTokens(serviceId, selectedFilterDays, new SimpleListCallback<TokenItem>() {
            @Override
            public void onSuccess(List<TokenItem> data) {
                currentTokens.clear();
                currentTokens.addAll(data);
                adapter.submitList(data);
                summary.setText("Showing " + data.size() + " records from the last " + selectedFilterDays + " day(s).");
            }

            @Override
            public void onError(Exception exception) {
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        });
    }
}