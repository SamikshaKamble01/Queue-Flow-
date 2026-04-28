package com.example.queuemanagementsystem.utils;

public final class Constants {

    public static final String USERS = "users";
    public static final String SERVICES = "services";
    public static final String SLOTS = "slots";
    public static final String TOKENS = "tokens";
    public static final String QUEUE_STATUS = "queue_status";

    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";

    public static final String STATUS_WAITING = "waiting";
    public static final String STATUS_CALLED = "called";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_SKIPPED = "skipped";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String STATUS_RESCHEDULED = "rescheduled";

    public static final String ARG_SERVICE_ID = "service_id";
    public static final String ARG_SERVICE_NAME = "service_name";
    public static final String ARG_SERVICE_PREFIX = "service_prefix";
    public static final String ARG_AVG_MINUTES = "avg_minutes";
    public static final String ARG_TOKEN_ID = "token_id";

    public static final String ARG_RESCHEDULE_TOKEN_ID = "reschedule_token_id";
    public static final String ARG_FILTER_DAYS = "filter_days";
    public static final String ARG_EXPORT_FORMAT = "export_format";

    public static final String PREFS = "queueflow_prefs";
    public static final String PREF_DARK_MODE = "pref_dark_mode";
    public static final String PREF_LARGE_TEXT = "pref_large_text";
    public static final String PREF_LAST_ALERT_TOKEN = "pref_last_alert_token";
    public static final String PREF_LAST_ALERT_STAGE = "pref_last_alert_stage";

    private Constants() {
    }
}