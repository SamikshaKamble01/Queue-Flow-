package com.example.queuemanagementsystem.utils;

import android.content.Context;
import android.widget.Toast;

public final class UiUtils {

    private UiUtils() {
    }

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}