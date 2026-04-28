package com.example.queuemanagementsystem.data.model;
public class UserProfile {

    private String userId;
    private String name;
    private String phone;
    private String email;
    private String role;
    private boolean notificationsEnabled;
    private boolean darkModeEnabled;
    private boolean largeTextEnabled;
    private String fcmToken;

    public UserProfile() {
    }

    public UserProfile(
            String userId,
            String name,
            String phone,
            String email,
            String role,
            boolean notificationsEnabled,
            boolean darkModeEnabled,
            boolean largeTextEnabled
    ) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.notificationsEnabled = notificationsEnabled;
        this.darkModeEnabled = darkModeEnabled;
        this.largeTextEnabled = largeTextEnabled;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }

    public void setDarkModeEnabled(boolean darkModeEnabled) {
        this.darkModeEnabled = darkModeEnabled;
    }

    public boolean isLargeTextEnabled() {
        return largeTextEnabled;
    }

    public void setLargeTextEnabled(boolean largeTextEnabled) {
        this.largeTextEnabled = largeTextEnabled;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
