package com.example.queuemanagementsystem.ui.user;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.queuemanagementsystem.R;
import com.example.queuemanagementsystem.data.model.UserProfile;
import com.example.queuemanagementsystem.data.repository.AuthRepository;
import com.example.queuemanagementsystem.utils.ThemeManager;
import com.example.queuemanagementsystem.utils.SimpleCallback;
import com.example.queuemanagementsystem.utils.UiUtils;
import com.example.queuemanagementsystem.utils.VoidCallback;

public class UserProfileFragment extends Fragment {

    private final AuthRepository authRepository = new AuthRepository();

    public UserProfileFragment() {
        super(R.layout.fragment_user_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText nameInput = view.findViewById(R.id.input_profile_name);
        EditText phoneInput = view.findViewById(R.id.input_profile_phone);
        TextView emailValue = view.findViewById(R.id.text_profile_email_value);
        TextView roleValue = view.findViewById(R.id.text_profile_role_value);
        TextView title = view.findViewById(R.id.text_profile_title);
        Switch notificationsSwitch = view.findViewById(R.id.switch_profile_notifications);
        Switch darkModeSwitch = view.findViewById(R.id.switch_profile_dark_mode);
        Switch largeTextSwitch = view.findViewById(R.id.switch_profile_large_text);
        Button saveButton = view.findViewById(R.id.button_save_profile);
        Button syncTokenButton = view.findViewById(R.id.button_sync_push_token);
        Button historyButton = view.findViewById(R.id.button_open_history_profile);
        ThemeManager.applyLargeTextPreference(requireContext(), title);

        authRepository.fetchCurrentUserProfile(new SimpleCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile data) {
                nameInput.setText(data.getName());
                phoneInput.setText(data.getPhone());
                emailValue.setText(data.getEmail());
                roleValue.setText(data.getRole());
                notificationsSwitch.setChecked(data.isNotificationsEnabled());
                darkModeSwitch.setChecked(data.isDarkModeEnabled());
                largeTextSwitch.setChecked(data.isLargeTextEnabled());
            }

            @Override
            public void onError(Exception exception) {
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        });

        saveButton.setOnClickListener(v -> authRepository.updateProfile(
                nameInput.getText().toString().trim(),
                phoneInput.getText().toString().trim(),
                notificationsSwitch.isChecked(),
                darkModeSwitch.isChecked(),
                largeTextSwitch.isChecked(),
                new VoidCallback() {
                    @Override
                    public void onSuccess() {
                        ThemeManager.setDarkMode(requireContext(), darkModeSwitch.isChecked());
                        ThemeManager.setLargeTextEnabled(requireContext(), largeTextSwitch.isChecked());
                        UiUtils.toast(requireContext(), "Profile updated.");
                    }

                    @Override
                    public void onError(Exception exception) {
                        UiUtils.toast(requireContext(), exception.getMessage());
                    }
                }
        ));

        syncTokenButton.setOnClickListener(v -> authRepository.syncFcmToken(new VoidCallback() {
            @Override
            public void onSuccess() {
                UiUtils.toast(requireContext(), "Push token synced.");
            }

            @Override
            public void onError(Exception exception) {
                UiUtils.toast(requireContext(), exception.getMessage());
            }
        }));

        historyButton.setOnClickListener(v -> Navigation.findNavController(view)
                .navigate(R.id.action_profile_to_history));
    }
}