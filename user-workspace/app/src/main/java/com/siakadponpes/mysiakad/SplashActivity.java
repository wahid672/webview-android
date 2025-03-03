package com.siakadponpes.mysiakad;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.siakadponpes.mysiakad.utils.AdManager;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize AdMob
        AdManager.getInstance().initialize(this);

        // Delayed transition to MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Start MainActivity
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            
            // Close splash activity
            finish();
            
            // Apply fade transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, SPLASH_DELAY);
    }

    @Override
    public void onBackPressed() {
        // Disable back button during splash screen
    }
}
