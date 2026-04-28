package com.example.queuemanagementsystem.utils;

public interface VoidCallback {
    void onSuccess();

    void onError(Exception exception);
}