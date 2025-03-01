package com.siakadponpes.mysiakad;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.siakadponpes.mysiakad.config.AppConfig;

public class CustomWebViewClient extends WebViewClient {
    private Context context;
    private SwipeRefreshLayout swipeRefreshLayout;

    public CustomWebViewClient(Context context, SwipeRefreshLayout swipeRefreshLayout) {
        this.context = context;
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        
        // Handle telephone numbers
        if (url.startsWith("tel:")) {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, R.string.error_no_phone_app, Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        
        // Handle email links
        else if (url.startsWith("mailto:")) {
            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, R.string.error_no_email_app, Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        
        // Handle SMS links
        else if (url.startsWith("sms:")) {
            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, R.string.error_no_sms_app, Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        
        // Handle WhatsApp links
        else if (url.startsWith("whatsapp:")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, R.string.error_no_whatsapp, Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        
        // Handle geo location links
        else if (url.startsWith("geo:")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, R.string.error_no_maps_app, Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        
        // Handle market/play store links
        else if (url.startsWith("market:") || url.startsWith("https://play.google.com")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                // If Play Store app is not installed, open in browser
                view.loadUrl(url);
                return true;
            }
        }
        
        // Handle PDF files
        else if (url.toLowerCase().endsWith(".pdf")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url), "application/pdf");
                context.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                // If no PDF viewer is installed, try to open in browser
                view.loadUrl(url);
                return true;
            }
        }
        
        // Handle other unknown schemes
        else if (!url.startsWith("http://") && !url.startsWith("https://")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, R.string.error_no_app_for_link, Toast.LENGTH_SHORT).show();
                return true;
            }
        }

        // Handle regular URLs in WebView
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        swipeRefreshLayout.setRefreshing(false);
        
        // Enable JavaScript interface for offline mode
        view.evaluateJavascript(
            "localStorage.setItem('isOfflineMode', 'false');",
            null
        );
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        if (request.isForMainFrame()) {
            // Only show offline page for main frame errors
            if (error.getErrorCode() == ERROR_HOST_LOOKUP ||
                error.getErrorCode() == ERROR_CONNECT ||
                error.getErrorCode() == ERROR_TIMEOUT) {
                view.loadUrl("file:///android_asset/offline.html");
            }
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (failingUrl.equals(view.getUrl())) {
            // Only show offline page for main frame errors
            if (errorCode == ERROR_HOST_LOOKUP ||
                errorCode == ERROR_CONNECT ||
                errorCode == ERROR_TIMEOUT) {
                view.loadUrl("file:///android_asset/offline.html");
            }
        }
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        if (request.isForMainFrame()) {
            int statusCode = errorResponse.getStatusCode();
            if (statusCode >= 400) {
                view.loadUrl("file:///android_asset/offline.html");
            }
        }
    }
}
