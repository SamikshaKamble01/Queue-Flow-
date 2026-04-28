package com.example.queuemanagementsystem.ui.user;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.navigation.Navigation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.ListenerRegistration;
import com.google.zxing.WriterException;
import com.example.queuemanagementsystem.R;
import com.example.queuemanagementsystem.data.model.QueueStatus;
import com.example.queuemanagementsystem.data.model.ServiceItem;
import com.example.queuemanagementsystem.data.model.TokenItem;
import com.example.queuemanagementsystem.data.repository.QueueRepository;
import com.example.queuemanagementsystem.utils.Constants;
import com.example.queuemanagementsystem.utils.NotificationHelper;
import com.example.queuemanagementsystem.utils.QrUtils;
import com.example.queuemanagementsystem.utils.SimpleCallback;
import com.example.queuemanagementsystem.utils.TokenUtils;
import com.example.queuemanagementsystem.utils.UiUtils;
import com.example.queuemanagementsystem.utils.VoidCallback;

public class MyTokenFragment extends Fragment {

    private final QueueRepository queueRepository = new QueueRepository();
    private ListenerRegistration tokenListener;
    private ListenerRegistration queueListener;
    private TokenItem tokenItem;
    private QueueStatus queueStatus;
    private long avgMinutes = 5;

    public MyTokenFragment() {
        super(R.layout.fragment_my_token);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            avgMinutes = args.getLong(Constants.ARG_AVG_MINUTES, 5);
        }

        ProgressBar progressBar = view.findViewById(R.id.progress_token);
        progressBar.setVisibility(View.VISIBLE);

        String tokenId = args != null ? args.getString(Constants.ARG_TOKEN_ID) : null;
        if (tokenId != null) {
            attachTokenListener(tokenId, view, progressBar);
        } else {
            queueRepository.getLatestTokenForCurrentUser(new SimpleCallback<TokenItem>() {
                @Override
                public void onSuccess(TokenItem data) {
                    attachTokenListener(data.getTokenId(), view, progressBar);
                }

                @Override
                public void onError(Exception exception) {
                    progressBar.setVisibility(View.GONE);
                    UiUtils.toast(requireContext(), "No token booked yet.");
                }
            });
        }
    }

    private void attachTokenListener(String tokenId, View view, ProgressBar progressBar) {
        if (tokenId == null) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        tokenListener = queueRepository.observeToken(tokenId, new SimpleCallback<TokenItem>() {
            @Override
            public void onSuccess(TokenItem data) {
                tokenItem = data;
                progressBar.setVisibility(View.GONE);
                queueRepository.getServiceById(data.getServiceId(), new SimpleCallback<ServiceItem>() {
                    @Override
                    public void onSuccess(ServiceItem service) {
                        avgMinutes = service.getAvgServiceMinutes();
                        renderToken(view);
                    }

                    @Override
                    public void onError(Exception exception) {
                        renderToken(view);
                    }
                });
                renderToken(view);
                attachQueueListener(data.getServiceId(), view);
            }

            @Override
            public void onError(Exception exception) {
                progressBar.setVisibility(View.GONE);
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        });
    }

    private void attachQueueListener(String serviceId, View view) {
        if (queueListener != null) {
            queueListener.remove();
        }
        queueListener = queueRepository.observeQueueStatus(serviceId, new SimpleCallback<QueueStatus>() {
            @Override
            public void onSuccess(QueueStatus data) {
                queueStatus = data;
                renderToken(view);
            }

            @Override
            public void onError(Exception exception) {
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        });
    }

    private void renderToken(View view) {
        if (tokenItem == null) {
            return;
        }

        TextView tokenNumber = view.findViewById(R.id.text_token_number);
        TextView tokenStatus = view.findViewById(R.id.text_token_status);
        TextView currentToken = view.findViewById(R.id.text_current_token);
        TextView peopleAhead = view.findViewById(R.id.text_people_ahead);
        TextView eta = view.findViewById(R.id.text_eta);
        ImageView qrImage = view.findViewById(R.id.image_qr);
        Button cancelButton = view.findViewById(R.id.button_cancel_token);
        Button rescheduleButton = view.findViewById(R.id.button_reschedule_token);

        tokenNumber.setText(tokenItem.getTokenNumber());
        tokenStatus.setText(tokenItem.getStatus().toUpperCase());

        if (queueStatus != null) {
            long ahead = TokenUtils.peopleAhead(tokenItem, queueStatus);
            currentToken.setText(queueStatus.getCurrentTokenNumber());
            peopleAhead.setText(String.valueOf(ahead));
            eta.setText(TokenUtils.estimateMinutes(tokenItem, queueStatus, avgMinutes) + " min");
            NotificationHelper.maybeNotifyQueueProgress(requireContext(), tokenItem, ahead);
        }

        try {
            Bitmap bitmap = QrUtils.createQrCode(tokenItem.getQrValue());
            qrImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            UiUtils.toast(requireContext(), "Could not generate QR.");
        }
        boolean canEdit = Constants.STATUS_WAITING.equals(tokenItem.getStatus());
        cancelButton.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        rescheduleButton.setVisibility(canEdit ? View.VISIBLE : View.GONE);

        cancelButton.setOnClickListener(v -> queueRepository.cancelToken(
                tokenItem.getTokenId(),
                "Cancelled by user",
                new VoidCallback() {
                    @Override
                    public void onSuccess() {
                        UiUtils.toast(requireContext(), "Token cancelled.");
                    }

                    @Override
                    public void onError(Exception exception) {
                        UiUtils.toast(requireContext(), exception.getMessage());
                    }
                }
        ));

        rescheduleButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.ARG_SERVICE_ID, tokenItem.getServiceId());
            bundle.putString(Constants.ARG_SERVICE_NAME, tokenItem.getServiceName());
            bundle.putLong(Constants.ARG_AVG_MINUTES, avgMinutes);
            bundle.putString(Constants.ARG_RESCHEDULE_TOKEN_ID, tokenItem.getTokenId());
            Navigation.findNavController(view).navigate(R.id.action_token_to_slots, bundle);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tokenListener != null) {
            tokenListener.remove();
        }
        if (queueListener != null) {
            queueListener.remove();
        }
    }
}