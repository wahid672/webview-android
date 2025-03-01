package com.siakadponpes.mysiakad;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.siakadponpes.mysiakad.config.AppConfig;

public class CustomWebChromeClient extends WebChromeClient {
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private Activity mActivity;
    private int mOriginalOrientation;
    private int mOriginalSystemUiVisibility;

    public CustomWebChromeClient(Activity activity) {
        this.mActivity = activity;
    }

    // For file upload and camera capture
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                   FileChooserParams fileChooserParams) {
        if (AppConfig.isFileUploadEnabled() && mActivity instanceof MainActivity) {
            ((MainActivity) mActivity).setFilePathCallback(filePathCallback);
            
            Intent intent;
            if (fileChooserParams.isCaptureEnabled()) {
                // This is a camera capture request
                if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) 
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(mActivity, 
                        new String[]{Manifest.permission.CAMERA}, 1001);
                    filePathCallback.onReceiveValue(null);
                    return false;
                }
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            } else {
                intent = fileChooserParams.createIntent();
            }
            
            try {
                mActivity.startActivityForResult(intent, Constants.FILE_CHOOSER_RESULT_CODE);
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
        if (AppConfig.isFullscreenEnabled()) {
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
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                mActivity.getWindow().setDecorFitsSystemWindows(false);
                android.view.WindowInsetsController controller = mActivity.getWindow().getInsetsController();
                if (controller != null) {
                    controller.hide(android.view.WindowInsets.Type.systemBars());
                    controller.setSystemBarsBehavior(android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            } else {
                mActivity.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE);
            }
        }
    }

    @Override
    public void onHideCustomView() {
        if (AppConfig.isFullscreenEnabled() && mCustomView != null) {
            FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
            decor.removeView(mCustomView);
            mCustomView = null;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                mActivity.getWindow().setDecorFitsSystemWindows(true);
                android.view.WindowInsetsController controller = mActivity.getWindow().getInsetsController();
                if (controller != null) {
                    controller.show(android.view.WindowInsets.Type.systemBars());
                }
            } else {
                mActivity.getWindow().getDecorView().setSystemUiVisibility(mOriginalSystemUiVisibility);
            }
            mActivity.setRequestedOrientation(mOriginalOrientation);
            
            mCustomViewCallback.onCustomViewHidden();
            mCustomViewCallback = null;
        }
    }

    // For progress bar
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if (mActivity instanceof MainActivity) {
            ((MainActivity) mActivity).updateProgressBar(newProgress);
        }
    }

    // For handling HTML5 permissions (camera, microphone, etc.)
    @Override
    public void onPermissionRequest(final android.webkit.PermissionRequest request) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] resources = request.getResources();
                for (String resource : resources) {
                    if (resource.equals(android.webkit.PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            request.grant(new String[]{resource});
                        } else {
                            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CAMERA}, 1001);
                        }
                    }
                }
            }
        });
    }

    // For handling Geolocation
    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, android.webkit.GeolocationPermissions.Callback callback) {
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            callback.invoke(origin, true, false);
        } else {
            callback.invoke(origin, false, false);
        }
    }
}
