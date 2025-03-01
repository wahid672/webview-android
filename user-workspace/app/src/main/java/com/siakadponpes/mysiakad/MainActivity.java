package com.siakadponpes.mysiakad;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.siakadponpes.mysiakad.config.AppConfig;

/**
 * MainActivity handles:
 * 1. Camera and File Upload
 *    - Camera capture through WebChromeClient
 *    - File chooser implementation
 *    - Camera permission handling
 * 
 * 2. Download Support
 *    - Regular file downloads
 *    - Blob URL handling
 *    - PDF file support
 *    - Download progress notifications
 * 
 * 3. Permission Handling
 *    - Camera permissions
 *    - Storage permissions
 *    - Location permissions
 *    - Runtime permission requests
 * 
 * 4. Network Features
 *    - Network state monitoring
 *    - Offline page handling
 *    - Auto-reload when online
 * 
 * 5. Swipe Refresh
 *    - Pull to refresh
 *    - Only works at page top
 *    - Progress indicator
 * 
 * 6. URL Handling
 *    - External URLs
 *    - tel: links
 *    - mailto: links
 *    - WhatsApp links
 *    - Maps links
 */
public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    
    private static final int PERMISSION_REQUEST_CODE = 1;
    
    private SwipeRefreshLayout swipeRefreshLayout;
    private WebView webView;
    private ProgressBar progressBar;
    private ValueCallback<Uri[]> filePathCallback;
    private long backPressedTime;
    private ConnectivityManager.NetworkCallback networkCallback;
    private String[] permissions;

    public void setFilePathCallback(ValueCallback<Uri[]> callback) {
        filePathCallback = callback;
    }

    public void updateProgressBar(int progress) {
        if (progressBar != null) {
            if (progress == 100) {
                progressBar.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(progress);
            }
        }
    }

    @Override
    public void onRefresh() {
        if (webView != null) {
            webView.reload();
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void initializePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            };
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            };
        } else {
            permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize permissions
        initializePermissions();

        // Initialize Views
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        
        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorAccent
        );
        
        // Only enable refresh when page is at top
        webView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            swipeRefreshLayout.setEnabled(scrollY == 0);
        });
        
        // Setup WebView
        setupWebView();
        
        // Setup download listener and JavaScript interface
        setupDownloadListener();
        webView.addJavascriptInterface(this, "Android");
        
        // Register network callback
        registerNetworkCallback();
        
        // Request permissions
        requestPermissions();
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        
        // Enable JavaScript if enabled
        webSettings.setJavaScriptEnabled(AppConfig.isJavaScriptEnabled());
        
        // Enable Geolocation
        webSettings.setGeolocationEnabled(true);
        
        // Enable JavaScript Interface
        webView.addJavascriptInterface(this, "Android");
        
        // Enable DOM Storage
        webSettings.setDomStorageEnabled(true);
        
        // Enable Database
        webSettings.setDatabaseEnabled(true);
        
        // Set Cache Mode
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        
        // Enable Zoom
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        
        // Enable Mixed Content
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        
        // Enable Third Party Cookies
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }
        
        // Set WebViewClient
        webView.setWebViewClient(new CustomWebViewClient(this, swipeRefreshLayout));
        
        // Set WebChromeClient if fullscreen is enabled
        if (AppConfig.isFullscreenEnabled()) {
            webView.setWebChromeClient(new CustomWebChromeClient(this));
        }
        
        // Setup download listener
        setupDownloadListener();
        
        // Load initial URL
        String baseUrl = AppConfig.getBaseUrl();
        if (baseUrl != null && !baseUrl.isEmpty()) {
            webView.loadUrl(baseUrl);
        }
    }

    private void setupDownloadListener() {
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            // Handle blob URLs
            if (url.startsWith("blob:")) {
                // Inject JavaScript to convert blob to base64
                webView.evaluateJavascript(
                    "(function() {" +
                    "    var xhr = new XMLHttpRequest();" +
                    "    xhr.open('GET', '" + url + "', true);" +
                    "    xhr.responseType = 'blob';" +
                    "    xhr.onload = function(e) {" +
                    "        if (this.status == 200) {" +
                    "            var blob = this.response;" +
                    "            var reader = new FileReader();" +
                    "            reader.readAsDataURL(blob);" +
                    "            reader.onloadend = function() {" +
                    "                Android.processBase64Download(reader.result, '" + 
                                    mimetype + "', '" + contentDisposition + "');" +
                    "            }" +
                    "        }" +
                    "    };" +
                    "    xhr.send();" +
                    "})();",
                    null
                );
                return;
            }

            // Check storage permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                    PERMISSION_REQUEST_CODE);
                return;
            }
            
            // Get filename from URL or content disposition
            String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
            
            // Create download request
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setMimeType(mimetype);
            request.setTitle(fileName);
            request.setDescription(getString(R.string.downloading_file));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            request.addRequestHeader("User-Agent", userAgent);
            
            // Get download service and enqueue the request
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (dm != null) {
                try {
                    dm.enqueue(request);
                    Toast.makeText(this, getString(R.string.download_started, fileName), 
                        Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, getString(R.string.download_error), 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registerNetworkCallback() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    runOnUiThread(() -> {
                        if (webView != null && webView.getUrl() != null && 
                            webView.getUrl().contains("offline.html")) {
                            webView.reload();
                        }
                    });
                }
            };

            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } else {
            NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    runOnUiThread(() -> {
                        if (webView != null && webView.getUrl() != null && 
                            webView.getUrl().contains("offline.html")) {
                            webView.reload();
                        }
                    });
                }
            };

            connectivityManager.registerNetworkCallback(request, networkCallback);
        }
    }

    private void requestPermissions() {
        if (permissions == null || permissions.length == 0) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionsToRequest = new ArrayList<>();
            List<String> permissionsToExplain = new ArrayList<>();

            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        permissionsToExplain.add(permission);
                    }
                }
            }

            if (!permissionsToRequest.isEmpty()) {
                if (!permissionsToExplain.isEmpty()) {
                    String message = getString(R.string.permission_message) + "\n\n";
                    for (String permission : permissionsToExplain) {
                        if (permission.equals(Manifest.permission.CAMERA)) {
                            message += "• " + getString(R.string.permission_camera_reason) + "\n";
                        } else if (permission.equals(Manifest.permission.RECORD_AUDIO)) {
                            message += "• " + getString(R.string.permission_microphone_reason) + "\n";
                        } else if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            message += "• " + getString(R.string.permission_location_reason) + "\n";
                        } else if (permission.contains("STORAGE") || permission.contains("MEDIA")) {
                            message += "• " + getString(R.string.permission_storage_reason) + "\n";
                        }
                    }

                    new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle(R.string.permission_title)
                        .setMessage(message.trim())
                        .setPositiveButton(R.string.permission_ok, (dialog, which) -> {
                            ActivityCompat.requestPermissions(this, 
                                permissionsToRequest.toArray(new String[0]), 
                                PERMISSION_REQUEST_CODE);
                        })
                        .setNegativeButton(R.string.permission_cancel, (dialog, which) -> {
                            dialog.dismiss();
                            Toast.makeText(this, R.string.permission_denied_message, 
                                Toast.LENGTH_LONG).show();
                        })
                        .create()
                        .show();
                } else {
                    ActivityCompat.requestPermissions(this, 
                        permissionsToRequest.toArray(new String[0]), 
                        PERMISSION_REQUEST_CODE);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        if (networkCallback != null) {
            ConnectivityManager connectivityManager = 
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
        super.onDestroy();
    }
}
