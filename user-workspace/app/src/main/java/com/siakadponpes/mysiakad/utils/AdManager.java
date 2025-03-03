package com.siakadponpes.mysiakad.utils;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.siakadponpes.mysiakad.config.AppConfig;

public class AdManager {
    private static AdManager instance;
    private InterstitialAd interstitialAd;
    private boolean isLoading = false;

    private AdManager() {
        // Private constructor to enforce singleton pattern
    }

    public static AdManager getInstance() {
        if (instance == null) {
            instance = new AdManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        if (AppConfig.isAdMobEnabled()) {
            MobileAds.initialize(context, initializationStatus -> {
                // Load initial interstitial ad
                loadInterstitialAd(context);
            });
        }
    }

    public void loadInterstitialAd(Context context) {
        if (!AppConfig.isAdMobEnabled() || isLoading || interstitialAd != null) {
            return;
        }

        isLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(context, AppConfig.getAdMobInterstitialId(), adRequest,
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd ad) {
                    interstitialAd = ad;
                    isLoading = false;
                    setupInterstitialCallback();
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    interstitialAd = null;
                    isLoading = false;
                }
            });
    }

    private void setupInterstitialCallback() {
        if (interstitialAd == null) return;

        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                interstitialAd = null;
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                interstitialAd = null;
            }
        });
    }

    public void showInterstitialAd(Activity activity) {
        if (!AppConfig.isAdMobEnabled() || interstitialAd == null || activity == null) {
            return;
        }

        interstitialAd.show(activity);
    }

    public boolean isInterstitialAdLoaded() {
        return interstitialAd != null;
    }

    public void destroy() {
        interstitialAd = null;
        instance = null;
    }
}
