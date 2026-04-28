package com.example.queuemanagementsystem.data.repository;

import androidx.annotation.NonNull;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.example.queuemanagementsystem.data.model.UserProfile;
import com.example.queuemanagementsystem.firebase.FirebaseManager;
import com.example.queuemanagementsystem.utils.Constants;
import com.example.queuemanagementsystem.utils.SimpleCallback;
import com.example.queuemanagementsystem.utils.VoidCallback;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.HashMap;
import java.util.Map;


public class AuthRepository {

    public boolean isLoggedIn() {
        return FirebaseManager.auth().getCurrentUser() != null;
    }

    public String currentUserId() {
        FirebaseUser user = FirebaseManager.auth().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public void login(String email, String password, SimpleCallback<UserProfile> callback) {
        FirebaseManager.auth()
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> fetchCurrentUserProfile(callback))
                .addOnFailureListener(callback::onError);
    }

    public void register(
            String name,
            String phone,
            String email,
            String password,
            String role,
            SimpleCallback<UserProfile> callback
    ) {
        FirebaseManager.auth()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> saveProfile(result, name, phone, email, role, callback))
                .addOnFailureListener(callback::onError);
    }

    public void fetchCurrentUserProfile(SimpleCallback<UserProfile> callback) {
        String userId = currentUserId();
        if (userId == null) {
            callback.onError(new IllegalStateException("User not logged in."));
            return;
        }

        FirebaseManager.db()
                .collection(Constants.USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    UserProfile profile = snapshot.toObject(UserProfile.class);
                    if (profile == null) {
                        callback.onError(new IllegalStateException("User profile not found."));
                        return;
                    }
                    profile.setUserId(userId);
                    callback.onSuccess(profile);
                })
                .addOnFailureListener(callback::onError);
    }

    public void logout(VoidCallback callback) {
        FirebaseManager.auth().signOut();
        callback.onSuccess();
    }

    private void saveProfile(
            @NonNull AuthResult result,
            String name,
            String phone,
            String email,
            String role,
            SimpleCallback<UserProfile> callback
    ) {
        FirebaseUser firebaseUser = result.getUser();
        if (firebaseUser == null) {
            callback.onError(new IllegalStateException("Could not create Firebase user."));
            return;
        }

        String userId = firebaseUser.getUid();
        UserProfile profile = new UserProfile(userId, name, phone, email, role, true, false, false);

        FirebaseManager.db()
                .collection(Constants.USERS)
                .document(userId)
                .set(profile)
                .addOnSuccessListener(unused -> callback.onSuccess(profile))
                .addOnFailureListener(callback::onError);
    }

    public void updateProfile(
            String name,
            String phone,
            boolean notificationsEnabled,
            boolean darkModeEnabled,
            boolean largeTextEnabled,
            VoidCallback callback
    ) {
        String userId = currentUserId();
        if (userId == null) {
            callback.onError(new IllegalStateException("User not logged in."));
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("notificationsEnabled", notificationsEnabled);
        updates.put("darkModeEnabled", darkModeEnabled);
        updates.put("largeTextEnabled", largeTextEnabled);

        FirebaseManager.db()
                .collection(Constants.USERS)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void syncFcmToken(VoidCallback callback) {
        String userId = currentUserId();
        if (userId == null) {
            callback.onError(new IllegalStateException("User not logged in."));
            return;
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> FirebaseManager.db()
                        .collection(Constants.USERS)
                        .document(userId)
                        .update("fcmToken", token)
                        .addOnSuccessListener(unused -> callback.onSuccess())
                        .addOnFailureListener(callback::onError))
                .addOnFailureListener(callback::onError);
    }
}