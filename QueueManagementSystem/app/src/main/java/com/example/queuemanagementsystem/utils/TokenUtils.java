package com.example.queuemanagementsystem.utils;

import com.example.queuemanagementsystem.data.model.QueueStatus;
import com.example.queuemanagementsystem.data.model.TokenItem;

public final class TokenUtils {

    private TokenUtils() {
    }

    public static String buildTokenNumber(String prefix, long queueNumber) {
        return prefix + "-" + String.format("%02d", queueNumber);
    }

    public static long peopleAhead(TokenItem token, QueueStatus status) {
        if (token == null || status == null) {
            return 0;
        }
        long diff = token.getQueueNumber() - status.getCurrentQueueNumber() - 1;
        return Math.max(diff, 0);
    }

    public static long estimateMinutes(TokenItem token, QueueStatus status, long avgServiceMinutes) {
        return peopleAhead(token, status) * Math.max(avgServiceMinutes, 1);
    }
}