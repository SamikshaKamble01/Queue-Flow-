package com.example.queuemanagementsystem.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.example.queuemanagementsystem.R;
import com.example.queuemanagementsystem.data.repository.QueueRepository;
import com.example.queuemanagementsystem.utils.UiUtils;
import com.example.queuemanagementsystem.utils.VoidCallback;

public class QrScannerFragment extends Fragment {

    private final QueueRepository queueRepository = new QueueRepository();

    private final ActivityResultLauncher<ScanOptions> scanLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result == null || result.getContents() == null) {
                    return;
                }

                View view = getView();
                if (view == null) {
                    return;
                }

                TextView resultText = view.findViewById(R.id.text_scan_result);
                resultText.setText("Scanned: " + result.getContents());

                queueRepository.markCompletedByQr(result.getContents(), new VoidCallback() {
                    @Override
                    public void onSuccess() {
                        UiUtils.toast(requireContext(), "Token marked completed.");
                    }

                    @Override
                    public void onError(Exception exception) {
                        UiUtils.toast(requireContext(), exception.getMessage());
                    }
                });
            });

    public QrScannerFragment() {
        super(R.layout.fragment_qr_scanner);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button scanButton = view.findViewById(R.id.button_start_scan);
        scanButton.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setOrientationLocked(false);
            options.setPrompt("Scan user token QR");
            options.setBeepEnabled(true);
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            scanLauncher.launch(options);
        });
    }
}