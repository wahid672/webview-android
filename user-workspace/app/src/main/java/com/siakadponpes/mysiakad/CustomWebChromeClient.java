package com.siakadponpes.mysiakad;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;

public class CustomWebChromeClient extends WebChromeClient {
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private Activity mActivity;
    private int mOriginalOrientation;
    private int mOriginalSystemUiVisibility;

    public CustomWebChromeClient(Activity activity) {
        this.mActivity = activity;
    }

    // For file upload
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                   FileChooserParams fileChooserParams) {
        if (mActivity instanceof MainActivity) {
            ((MainActivity) mActivity).setFilePathCallback(filePathCallback);
            
            Intent intent = fileChooserParams.createIntent();
            try {
                mActivity.startActivityForResult(intent, MainActivity.FILE_CHOOSER_RESULT_CODE);
            } catch (Exception e) {
                filePathCallback.onReceiveValue(null);
                return false;
            }
            return true;
        }
        return false;
    }

    // For full screen support
    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        if (mCustomView != null) {
            onHideCustomView();
            return;
        }

        mCustomView = view;
        mOriginalSystemUiVisibility = mActivity.getWindow().getDecorView().getSystemUiVisibility();
        mOriginalOrientation = mActivity.getRequestedOrientation();

        mCustomViewCallback = callback;

        FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
        decor.addView(mCustomView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        
        mActivity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    @Override
    public void onHideCustomView() {
        FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
        decor.removeView(mCustomView);
        mCustomView = null;

        mActivity.getWindow().getDecorView().setSystemUiVisibility(mOriginalSystemUiVisibility);
        mActivity.setRequestedOrientation(mOriginalOrientation);
        
        mCustomViewCallback.onCustomViewHidden();
        mCustomViewCallback = null;
    }

    // For progress bar
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if (mActivity instanceof MainActivity) {
            ((MainActivity) mActivity).updateProgressBar(newProgress);
        }
    }

    // For handling HTML5 notifications
    @Override
    public void onPermissionRequest(final android.webkit.PermissionRequest request) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                request.grant(request.getResources());
            }
        });
    }
}
