package com.example.queuemanagementsystem.firebase;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.example.queuemanagementsystem.R;
import com.example.queuemanagementsystem.data.repository.AuthRepository;
import com.example.queuemanagementsystem.utils.NotificationHelper;
import com.example.queuemanagementsystem.utils.VoidCallback;

public class QueueMessagingService extends FirebaseMessagingService {



    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        NotificationHelper.ensureChannel(this);

        String title = remoteMessage.getNotification() != null
                ? remoteMessage.getNotification().getTitle()
                : "Queue update";
        String body = remoteMessage.getNotification() != null
                ? remoteMessage.getNotification().getBody()
                : "Your token status has changed.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(this).notify((int) System.currentTimeMillis(), builder.build());
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        new AuthRepository().syncFcmToken(new VoidCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(Exception exception) {
            }
        });
    }
}