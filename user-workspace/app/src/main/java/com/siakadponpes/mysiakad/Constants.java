package com.siakadponpes.mysiakad;

public class Constants {
    // Request codes
    public static final int PERMISSION_REQUEST_CODE = 1001;
    public static final int FILE_CHOOSER_RESULT_CODE = 1002;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 1003;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1004;
    public static final int STORAGE_PERMISSION_REQUEST_CODE = 1005;

    // Time constants
    public static final int SPLASH_DELAY = 2000; // 2 seconds
    public static final int BACK_PRESS_INTERVAL = 2000; // 2 seconds
    public static final int CACHE_MAX_AGE = 60 * 60 * 24 * 7; // 7 days in seconds

    // WebView configurations
    public static final int WEBVIEW_CACHE_SIZE = 30 * 1024 * 1024; // 30MB
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String DEFAULT_MIME_TYPE = "text/html";
    public static final String OFFLINE_PAGE = "file:///android_asset/offline.html";

    // File handling
    public static final String DOWNLOADS_FOLDER = "Downloads";
    public static final String TEMP_FILE_PREFIX = "temp_";
    public static final String TEMP_FILE_SUFFIX = ".tmp";

    // Content types
    public static final String[] SUPPORTED_IMAGE_TYPES = {
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp"
    };

    public static final String[] SUPPORTED_VIDEO_TYPES = {
        "video/mp4",
        "video/3gpp",
        "video/webm"
    };

    public static final String[] SUPPORTED_AUDIO_TYPES = {
        "audio/mpeg",
        "audio/mp3",
        "audio/ogg",
        "audio/wav"
    };

    // URL schemes
    public static final String[] EXTERNAL_URL_SCHEMES = {
        "tel:",
        "mailto:",
        "geo:",
        "whatsapp:",
        "market:",
        "intent:",
        "sms:"
    };

    // File extensions
    public static final String[] DOWNLOADABLE_EXTENSIONS = {
        ".pdf",
        ".doc",
        ".docx",
        ".xls",
        ".xlsx",
        ".ppt",
        ".pptx",
        ".txt",
        ".csv",
        ".zip",
        ".rar"
    };

    // Error messages
    public static final String ERROR_NO_CAMERA = "Camera not available";
    public static final String ERROR_NO_STORAGE = "Storage not available";
    public static final String ERROR_NO_INTERNET = "No internet connection";
    public static final String ERROR_DOWNLOAD_FAILED = "Download failed";
    public static final String ERROR_UPLOAD_FAILED = "Upload failed";

    // Success messages
    public static final String SUCCESS_DOWNLOAD = "Download completed";
    public static final String SUCCESS_UPLOAD = "Upload successful";

    private Constants() {
        // Private constructor to prevent instantiation
    }
}
