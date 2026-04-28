package com.example.queuemanagementsystem.ui.auth;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.queuemanagementsystem.R;
import com.example.queuemanagementsystem.data.model.UserProfile;
import com.example.queuemanagementsystem.data.repository.AuthRepository;
import com.example.queuemanagementsystem.utils.Constants;
import com.example.queuemanagementsystem.utils.SimpleCallback;

public class SplashFragment extends Fragment {

    private final AuthRepository authRepository = new AuthRepository();

    public SplashFragment() {
        super(R.layout.fragment_splash);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController navController = Navigation.findNavController(view);

        if (!authRepository.isLoggedIn()) {
            navController.navigate(R.id.action_splash_to_login);
            return;
        }

        authRepository.fetchCurrentUserProfile(new SimpleCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile data) {
                if (!isAdded()) {
                    return;
                }
                if (Constants.ROLE_ADMIN.equals(data.getRole())) {
                    navController.navigate(R.id.action_splash_to_admin_home);
                } else {
                    navController.navigate(R.id.action_splash_to_user_home);
                }
            }

            @Override
            public void onError(Exception exception) {
                if (isAdded()) {
                    navController.navigate(R.id.action_splash_to_login);
                }
            }
        });
    }
}