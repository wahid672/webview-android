package com.siakadponpes.mysiakad.config;

public class AppConfig {
    // Feature flags
    private static boolean ENABLE_JAVASCRIPT = true;
    private static boolean ENABLE_FILE_UPLOAD = true;
    private static boolean ENABLE_DOWNLOAD = true;
    private static boolean ENABLE_NOTIFICATIONS = true;
    private static boolean ENABLE_OFFLINE_MODE = true;
    private static boolean ENABLE_ADMOB = true;
    private static boolean ENABLE_FULLSCREEN = true;
    private static boolean ENABLE_EXTERNAL_LINKS = true;

    // Getters and setters for feature flags
    public static boolean isJavaScriptEnabled() {
        return ENABLE_JAVASCRIPT;
    }

    public static void setJavaScriptEnabled(boolean enabled) {
        ENABLE_JAVASCRIPT = enabled;
    }

    public static boolean isFileUploadEnabled() {
        return ENABLE_FILE_UPLOAD;
    }

    public static void setFileUploadEnabled(boolean enabled) {
        ENABLE_FILE_UPLOAD = enabled;
    }

    public static boolean isDownloadEnabled() {
        return ENABLE_DOWNLOAD;
    }

    public static void setDownloadEnabled(boolean enabled) {
        ENABLE_DOWNLOAD = enabled;
    }

    public static boolean isNotificationsEnabled() {
        return ENABLE_NOTIFICATIONS;
    }

    public static void setNotificationsEnabled(boolean enabled) {
        ENABLE_NOTIFICATIONS = enabled;
    }

    public static boolean isOfflineModeEnabled() {
        return ENABLE_OFFLINE_MODE;
    }

    public static void setOfflineModeEnabled(boolean enabled) {
        ENABLE_OFFLINE_MODE = enabled;
    }

    public static boolean isAdMobEnabled() {
        return ENABLE_ADMOB;
    }

    public static void setAdMobEnabled(boolean enabled) {
        ENABLE_ADMOB = enabled;
    }

    public static boolean isFullscreenEnabled() {
        return ENABLE_FULLSCREEN;
    }

    public static void setFullscreenEnabled(boolean enabled) {
        ENABLE_FULLSCREEN = enabled;
    }

    public static boolean isExternalLinksEnabled() {
        return ENABLE_EXTERNAL_LINKS;
    }

    public static void setExternalLinksEnabled(boolean enabled) {
        ENABLE_EXTERNAL_LINKS = enabled;
    }
}
