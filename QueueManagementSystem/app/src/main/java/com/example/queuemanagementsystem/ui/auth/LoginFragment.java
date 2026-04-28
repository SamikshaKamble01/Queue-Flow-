package com.example.queuemanagementsystem.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

public class LoginFragment extends Fragment {

    private final AuthRepository authRepository = new AuthRepository();

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText emailInput = view.findViewById(R.id.input_email);
        EditText passwordInput = view.findViewById(R.id.input_password);
        Button loginButton = view.findViewById(R.id.button_login);
        TextView registerLink = view.findViewById(R.id.text_create_account);

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                UiUtils.toast(requireContext(), "Enter email and password.");
                return;
            }

            loginButton.setEnabled(false);
            authRepository.login(email, password, new SimpleCallback<UserProfile>() {
                @Override
                public void onSuccess(UserProfile data) {
                    loginButton.setEnabled(true);
                    if (!isAdded()) {
                        return;
                    }
                    int destination = Constants.ROLE_ADMIN.equals(data.getRole())
                            ? R.id.action_login_to_admin_home
                            : R.id.action_login_to_user_home;
                    Navigation.findNavController(view).navigate(destination);
                }

                @Override
                public void onError(Exception exception) {
                    loginButton.setEnabled(true);
                    UiUtils.toast(requireContext(), exception.getMessage());
                }
            });
        });

        registerLink.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_login_to_register));
    }
}