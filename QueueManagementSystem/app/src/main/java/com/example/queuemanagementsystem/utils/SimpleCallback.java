package com.example.queuemanagementsystem.utils;

public interface SimpleCallback<T> {
    void onSuccess(T data);

    void onError(Exception exception);
}