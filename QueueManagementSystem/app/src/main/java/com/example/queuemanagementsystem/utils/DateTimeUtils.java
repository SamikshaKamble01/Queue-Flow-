package com.example.queuemanagementsystem.utils;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateTimeUtils {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT =
            new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    private DateTimeUtils() {
    }

    public static String formatDate(long timestamp) {
        return DATE_FORMAT.format(new Date(timestamp));
    }

    public static String formatDateTime(long timestamp) {
        return DATE_TIME_FORMAT.format(new Date(timestamp));
    }

    public static long daysAgo(int days) {
        return System.currentTimeMillis() - (days * 24L * 60L * 60L * 1000L);
    }
}