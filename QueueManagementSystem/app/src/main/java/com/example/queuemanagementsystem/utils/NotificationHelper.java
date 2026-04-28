package com.example.queuemanagementsystem.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.queuemanagementsystem.R;
import com.example.queuemanagementsystem.data.model.TokenItem;

public final class NotificationHelper {

    public static final String CHANNEL_ID = "queue_updates";

    private NotificationHelper() {
    }

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Queue Alerts",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Alerts for near-turn and called-token updates");

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
    }

    public static void maybeNotifyQueueProgress(Context context, TokenItem tokenItem, long peopleAhead) {
        if (tokenItem == null) {
            return;
        }

        ensureChannel(context);
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
        String lastToken = prefs.getString(Constants.PREF_LAST_ALERT_TOKEN, "");
        String lastStage = prefs.getString(Constants.PREF_LAST_ALERT_STAGE, "");

        String nextStage = null;
        String message = null;
        if (Constants.STATUS_CALLED.equals(tokenItem.getStatus())) {
            nextStage = "called";
            message = tokenItem.getTokenNumber() + " is now active. Please reach the counter.";
        } else if (peopleAhead <= 3 && Constants.STATUS_WAITING.equals(tokenItem.getStatus())) {
            nextStage = "near";
            message = tokenItem.getTokenNumber() + " is only " + peopleAhead + " tokens away.";
        }

        if (nextStage == null || (tokenItem.getTokenId().equals(lastToken) && nextStage.equals(lastStage))) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Queue update")
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat.from(context)
                .notify((int) System.currentTimeMillis(), builder.build());

        prefs.edit()
                .putString(Constants.PREF_LAST_ALERT_TOKEN, tokenItem.getTokenId())
                .putString(Constants.PREF_LAST_ALERT_STAGE, nextStage)
                .apply();
    }
}