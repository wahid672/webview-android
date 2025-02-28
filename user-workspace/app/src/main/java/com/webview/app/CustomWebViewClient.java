package com.webview.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CustomWebViewClient extends WebViewClient {
    private Context context;

    public CustomWebViewClient(Context context) {
        this.context = context;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        
        // Handle external links
        if (url.startsWith("tel:") || url.startsWith("mailto:") || 
            url.startsWith("whatsapp:") || url.startsWith("market:")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
            return true;
        }
        
        // Handle payment gateway URLs
        if (url.contains("paypal.com") || url.contains("stripe.com") || 
            url.contains("razorpay.com")) {
            view.loadUrl(url);
            return true;
        }

        // Handle social sharing
        if (url.startsWith("share:")) {
            String shareText = url.substring(6);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            context.startActivity(Intent.createChooser(shareIntent, "Share via"));
            return true;
        }

        // Load all other URLs in WebView
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        // Show loading indicator if needed
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        // Hide loading indicator if needed
        
        // Enable JavaScript interface for offline mode
        view.evaluateJavascript(
            "localStorage.setItem('isOfflineMode', 'false');",
            null
        );
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        // Load offline HTML file if network error occurs
        view.loadUrl("file:///android_asset/offline.html");
    }
}
