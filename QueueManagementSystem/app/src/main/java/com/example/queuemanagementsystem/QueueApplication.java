package com.example.queuemanagementsystem;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class QueueApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}