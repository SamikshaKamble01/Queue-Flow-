package com.example.queuemanagementsystem.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public final class FirebaseManager {

    private static final FirebaseAuth AUTH = FirebaseAuth.getInstance();
    private static final FirebaseFirestore FIRESTORE = FirebaseFirestore.getInstance();

    private FirebaseManager() {
    }

    public static FirebaseAuth auth() {
        return AUTH;
    }

    public static FirebaseFirestore db() {
        return FIRESTORE;
    }
}