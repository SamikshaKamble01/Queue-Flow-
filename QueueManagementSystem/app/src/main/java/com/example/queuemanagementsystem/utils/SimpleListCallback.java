package com.example.queuemanagementsystem.utils;

import java.util.List;

public interface SimpleListCallback<T> {
    void onSuccess(List<T> data);

    void onError(Exception exception);
}