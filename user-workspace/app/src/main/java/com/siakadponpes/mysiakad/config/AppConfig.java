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
    private static boolean ENABLE_WEBGL = true;
    private static boolean ENABLE_COOKIES = true;
    private static boolean ENABLE_CACHE = true;
    private static boolean ENABLE_LOCATION = true;
    private static boolean ENABLE_PAYMENT_GATEWAY = true;

    // Base URL
    private static String BASE_URL = "https://dev.siakadponpes.com";

    // AdMob Configuration
    private static final String ADMOB_APP_ID = "ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy";
    private static final String ADMOB_INTERSTITIAL_ID = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy";

    // Download Configuration
    private static final String DOWNLOAD_DIRECTORY = "MyWebApp";

    // Cache Configuration
    private static final int CACHE_SIZE = 30 * 1024 * 1024; // 30MB

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

    public static boolean isWebGLEnabled() {
        return ENABLE_WEBGL;
    }

    public static void setWebGLEnabled(boolean enabled) {
        ENABLE_WEBGL = enabled;
    }

    public static boolean isCookiesEnabled() {
        return ENABLE_COOKIES;
    }

    public static void setCookiesEnabled(boolean enabled) {
        ENABLE_COOKIES = enabled;
    }

    public static boolean isCacheEnabled() {
        return ENABLE_CACHE;
    }

    public static void setCacheEnabled(boolean enabled) {
        ENABLE_CACHE = enabled;
    }

    public static boolean isLocationEnabled() {
        return ENABLE_LOCATION;
    }

    public static void setLocationEnabled(boolean enabled) {
        ENABLE_LOCATION = enabled;
    }

    public static boolean isPaymentGatewayEnabled() {
        return ENABLE_PAYMENT_GATEWAY;
    }

    public static void setPaymentGatewayEnabled(boolean enabled) {
        ENABLE_PAYMENT_GATEWAY = enabled;
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static void setBaseUrl(String url) {
        BASE_URL = url;
    }

    public static String getAdMobAppId() {
        return ADMOB_APP_ID;
    }

    public static String getAdMobInterstitialId() {
        return ADMOB_INTERSTITIAL_ID;
    }

    public static String getDownloadDirectory() {
        return DOWNLOAD_DIRECTORY;
    }

    public static int getCacheSize() {
        return CACHE_SIZE;
    }
}
