package com.example.queuemanagementsystem.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.queuemanagementsystem.R;
import com.example.queuemanagementsystem.data.model.UserProfile;
import com.example.queuemanagementsystem.data.repository.AuthRepository;
import com.example.queuemanagementsystem.utils.Constants;
import com.example.queuemanagementsystem.utils.SimpleCallback;
import com.example.queuemanagementsystem.utils.UiUtils;

public class RegisterFragment extends Fragment {

    private final AuthRepository authRepository = new AuthRepository();

    public RegisterFragment() {
        super(R.layout.fragment_register);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText nameInput = view.findViewById(R.id.input_name);
        EditText phoneInput = view.findViewById(R.id.input_phone);
        EditText emailInput = view.findViewById(R.id.input_register_email);
        EditText passwordInput = view.findViewById(R.id.input_register_password);
        RadioGroup roleGroup = view.findViewById(R.id.group_role);
        Button registerButton = view.findViewById(R.id.button_register);

        registerButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String role = roleGroup.getCheckedRadioButtonId() == R.id.radio_admin
                    ? Constants.ROLE_ADMIN
                    : Constants.ROLE_USER;

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)
                    || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                UiUtils.toast(requireContext(), "Please fill all fields.");
                return;
            }

            registerButton.setEnabled(false);
            authRepository.register(name, phone, email, password, role, new SimpleCallback<UserProfile>() {
                @Override
                public void onSuccess(UserProfile data) {
                    registerButton.setEnabled(true);
                    if (!isAdded()) {
                        return;
                    }
                    int destination = Constants.ROLE_ADMIN.equals(data.getRole())
                            ? R.id.action_register_to_admin_home
                            : R.id.action_register_to_user_home;
                    Navigation.findNavController(view).navigate(destination);
                }

                @Override
                public void onError(Exception exception) {
                    registerButton.setEnabled(true);
                    UiUtils.toast(requireContext(), exception.getMessage());
                }
            });
        });
    }
}