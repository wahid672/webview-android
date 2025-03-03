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
import com.google.android.material.snackbar.Snackbar;
import com.siakadponpes.mysiakad.config.AppConfig;
import com.siakadponpes.mysiakad.utils.AdManager;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    
    private SwipeRefreshLayout swipeRefreshLayout;
    private WebView webView;
    private ProgressBar progressBar;
    private ValueCallback<Uri[]> filePathCallback;
    private long backPressedTime;
    private ConnectivityManager.NetworkCallback networkCallback;
    private AdManager adManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        initializeViews();
        
        // Setup WebView
        setupWebView();
        
        // Setup download listener
        setupDownloadListener();
        
        // Register network callback
        registerNetworkCallback();
        
        // Initialize AdMob
        adManager = AdManager.getInstance();
        
        // Request permissions
        requestPermissions();

        // Handle notification URL if opened from notification
        handleNotificationUrl(getIntent());
    }

    private void initializeViews() {
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorAccent
        );
        
        webView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            swipeRefreshLayout.setEnabled(scrollY == 0);
        });

        // Setup share button
        findViewById(R.id.shareButton).setOnClickListener(v -> shareCurrentPage());
    }

    private void shareCurrentPage() {
        String url = webView.getUrl();
        if (url != null && !url.isEmpty()) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message, url));
            
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)));
        }
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        
        // Enable JavaScript
        webSettings.setJavaScriptEnabled(AppConfig.isJavaScriptEnabled());
        
        // Enable WebGL
        if (AppConfig.isWebGLEnabled()) {
            webSettings.setJavaScriptEnabled(true);
            webSettings.setAllowContentAccess(true);
            webSettings.setAllowFileAccess(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setDatabaseEnabled(true);
            webSettings.setMediaPlaybackRequiresUserGesture(false);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
        }
        
        // Enable Geolocation
        if (AppConfig.isLocationEnabled()) {
            webSettings.setGeolocationEnabled(true);
            webSettings.setGeolocationDatabasePath(getFilesDir().getPath());
        }
        
        // Enable Cookies
        if (AppConfig.isCookiesEnabled()) {
            CookieManager.getInstance().setAcceptCookie(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
            }
        }
        
        // Enable Cache
        if (AppConfig.isCacheEnabled()) {
            webSettings.setAppCacheEnabled(true);
            webSettings.setAppCachePath(getApplicationContext().getCacheDir().getPath());
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
            webSettings.setDatabaseEnabled(true);
            webSettings.setDomStorageEnabled(true);
        }
        
        // Set WebViewClient and WebChromeClient
        webView.setWebViewClient(new CustomWebViewClient(this, swipeRefreshLayout));
        webView.setWebChromeClient(new CustomWebChromeClient(this));
        
        // Load initial URL
        String url = getIntent().getStringExtra("notification_url");
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        } else {
            webView.loadUrl(AppConfig.getBaseUrl());
        }
    }

    private void setupDownloadListener() {
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            if (url.startsWith("blob:")) {
                handleBlobDownload(url, mimetype, contentDisposition);
                return;
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                    Constants.STORAGE_PERMISSION_REQUEST_CODE);
                return;
            }
            
            String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
            
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setMimeType(mimetype);
            request.setTitle(fileName);
            request.setDescription(getString(R.string.downloading_file));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, 
                AppConfig.getDownloadDirectory() + File.separator + fileName
            );
            request.addRequestHeader("User-Agent", userAgent);
            
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (dm != null) {
                try {
                    dm.enqueue(request);
                    showMessage(getString(R.string.download_started, fileName));
                } catch (Exception e) {
                    showMessage(getString(R.string.download_error));
                }
            }
        });
    }

    private void handleBlobDownload(String url, String mimetype, String contentDisposition) {
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
    }

    @JavascriptInterface
    public void processBase64Download(String base64, String mimeType, String contentDisposition) {
        String fileName = URLUtil.guessFileName("file", contentDisposition, mimeType);
        String dataString = base64.split(",")[1];
        byte[] data = android.util.Base64.decode(dataString, android.util.Base64.DEFAULT);
        
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path, AppConfig.getDownloadDirectory() + File.separator + fileName);
        
        try {
            FileOutputStream os = new FileOutputStream(file);
            os.write(data);
            os.close();
            showMessage(getString(R.string.download_complete));
        } catch (Exception e) {
            showMessage(getString(R.string.download_error));
        }
    }

    private void registerNetworkCallback() {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        
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

    private void handleNotificationUrl(Intent intent) {
        String url = intent.getStringExtra("notification_url");
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        }
    }

    @Override
    public void onRefresh() {
        webView.reload();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            if (System.currentTimeMillis() - backPressedTime < Constants.BACK_PRESS_INTERVAL) {
                super.onBackPressed();
            } else {
                backPressedTime = System.currentTimeMillis();
                showMessage(getString(R.string.exit_message));
            }
        }
    }

    private void showMessage(String message) {
        Snackbar.make(webView, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNotificationUrl(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        
        // Load interstitial ad
        if (AppConfig.isAdMobEnabled()) {
            adManager.loadInterstitialAd(this);
        }
    }

    @Override
    protected void onPause() {
        webView.onPause();
        super.onPause();
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
        if (adManager != null) {
            adManager.destroy();
        }
        super.onDestroy();
    }

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

    private void requestPermissions() {
        String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        };

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, 
                permissionsToRequest.toArray(new String[0]), 
                Constants.PERMISSION_REQUEST_CODE);
        }
    }
}
