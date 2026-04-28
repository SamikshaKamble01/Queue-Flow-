package com.example.queuemanagementsystem.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.queuemanagementsystem.R;
import com.example.queuemanagementsystem.data.repository.AuthRepository;
import com.example.queuemanagementsystem.utils.NotificationHelper;
import com.example.queuemanagementsystem.utils.ThemeManager;
import com.example.queuemanagementsystem.utils.VoidCallback;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeManager.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationHelper.ensureChannel(this);
        requestNotificationPermissionIfNeeded();

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if (!navController.popBackStack()) {
                        finish();
                    }
                }
            });
        }

        new AuthRepository().syncFcmToken(new VoidCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(Exception exception) {
            }
        });
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                501
        );
    }
}
