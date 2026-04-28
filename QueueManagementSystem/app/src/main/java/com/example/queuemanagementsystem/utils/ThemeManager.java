package com.example.queuemanagementsystem.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeManager {

    private ThemeManager() {
    }

    public static void applySavedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean(Constants.PREF_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    public static void setDarkMode(Context context, boolean enabled) {
        context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(Constants.PREF_DARK_MODE, enabled)
                .apply();
        applySavedTheme(context);
    }

    public static boolean isLargeTextEnabled(Context context) {
        return context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE)
                .getBoolean(Constants.PREF_LARGE_TEXT, false);
    }

    public static void setLargeTextEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(Constants.PREF_LARGE_TEXT, enabled)
                .apply();
    }

    public static void applyLargeTextPreference(Context context, TextView... textViews) {
        float scale = isLargeTextEnabled(context) ? 1.15f : 1f;
        for (TextView textView : textViews) {
            if (textView != null) {
                textView.setTextSize(textView.getTextSize() / context.getResources().getDisplayMetrics().scaledDensity * scale);
            }
        }
    }
}
