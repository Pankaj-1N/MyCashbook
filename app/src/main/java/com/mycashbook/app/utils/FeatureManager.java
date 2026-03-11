package com.mycashbook.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Centralized feature access control based on subscription tier
 */
public class FeatureManager {

    // =====================================================
    // TESTING MODE - Set to true to unlock ALL features
    // =====================================================
    private static final boolean TESTING_MODE = true;

    private static final String PREF_NAME = "subscription_prefs";
    private static final String KEY_CURRENT_PLAN = "current_plan";

    public static final String PLAN_FREE = "FREE";
    public static final String PLAN_BASIC = "BASIC";
    public static final String PLAN_PREMIUM = "PREMIUM";
    public static final String PLAN_BUSINESS = "BUSINESS";

    private static FeatureManager instance;
    private final SharedPreferences prefs;

    private FeatureManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            prefs = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize FeatureManager", e);
        }
    }

    public static synchronized FeatureManager getInstance(Context context) {
        if (instance == null) {
            instance = new FeatureManager(context.getApplicationContext());
        }
        return instance;
    }

    public String getCurrentPlan() {
        if (TESTING_MODE)
            return PLAN_BUSINESS; // All features enabled
        return prefs.getString(KEY_CURRENT_PLAN, PLAN_FREE);
    }

    public void setCurrentPlan(String plan) {
        prefs.edit().putString(KEY_CURRENT_PLAN, plan).apply();
    }

    // Main Book Limits
    public boolean canCreateMainBook(int currentCount) {
        if (TESTING_MODE)
            return true;
        String plan = getCurrentPlan();
        if (PLAN_FREE.equals(plan)) {
            return currentCount < 3;
        }
        return true; // Unlimited for paid plans
    }

    public int getMainBookLimit() {
        if (TESTING_MODE)
            return Integer.MAX_VALUE;
        return PLAN_FREE.equals(getCurrentPlan()) ? 3 : Integer.MAX_VALUE;
    }

    // Feature Checks - All return true in TESTING_MODE
    public boolean hasUnlimitedBooks() {
        if (TESTING_MODE)
            return true;
        return !PLAN_FREE.equals(getCurrentPlan());
    }

    public boolean hasNoAds() {
        if (TESTING_MODE)
            return true;
        return !PLAN_FREE.equals(getCurrentPlan());
    }

    public boolean hasGoogleDriveBackup() {
        if (TESTING_MODE)
            return true;
        String plan = getCurrentPlan();
        return PLAN_BASIC.equals(plan) || PLAN_PREMIUM.equals(plan) || PLAN_BUSINESS.equals(plan);
    }

    public boolean hasExcelExport() {
        if (TESTING_MODE)
            return true;
        String plan = getCurrentPlan();
        return PLAN_BASIC.equals(plan) || PLAN_PREMIUM.equals(plan) || PLAN_BUSINESS.equals(plan);
    }

    public boolean hasAdvancedAnalytics() {
        if (TESTING_MODE)
            return true;
        String plan = getCurrentPlan();
        return PLAN_PREMIUM.equals(plan) || PLAN_BUSINESS.equals(plan);
    }

    public boolean hasPDFExport() {
        if (TESTING_MODE)
            return true;
        String plan = getCurrentPlan();
        return PLAN_PREMIUM.equals(plan) || PLAN_BUSINESS.equals(plan);
    }

    public boolean hasMultiCurrency() {
        if (TESTING_MODE)
            return true;
        String plan = getCurrentPlan();
        return PLAN_PREMIUM.equals(plan) || PLAN_BUSINESS.equals(plan);
    }

    public boolean hasPaymentReminders() {
        if (TESTING_MODE)
            return true;
        String plan = getCurrentPlan();
        return PLAN_PREMIUM.equals(plan) || PLAN_BUSINESS.equals(plan);
    }

    public boolean hasPrioritySupport() {
        if (TESTING_MODE)
            return true;
        String plan = getCurrentPlan();
        return PLAN_PREMIUM.equals(plan) || PLAN_BUSINESS.equals(plan);
    }

    public boolean hasCustomCategories() {
        if (TESTING_MODE)
            return true;
        String plan = getCurrentPlan();
        return PLAN_PREMIUM.equals(plan) || PLAN_BUSINESS.equals(plan);
    }

    // Business Plan Features
    public boolean hasTeamCollaboration() {
        if (TESTING_MODE)
            return true;
        return PLAN_BUSINESS.equals(getCurrentPlan());
    }

    public boolean hasMultipleUserAccounts() {
        if (TESTING_MODE)
            return true;
        return PLAN_BUSINESS.equals(getCurrentPlan());
    }

    public boolean hasAPIAccess() {
        if (TESTING_MODE)
            return true;
        return PLAN_BUSINESS.equals(getCurrentPlan());
    }

    public boolean hasCustomExportTemplates() {
        if (TESTING_MODE)
            return true;
        return PLAN_BUSINESS.equals(getCurrentPlan());
    }

    public boolean hasWhiteLabel() {
        if (TESTING_MODE)
            return true;
        return PLAN_BUSINESS.equals(getCurrentPlan());
    }

    // Helper method to show upgrade prompt
    public String getUpgradeMessage(String feature) {
        if (TESTING_MODE)
            return "All features enabled (Testing Mode)";

        String currentPlan = getCurrentPlan();

        if (PLAN_FREE.equals(currentPlan)) {
            return "Upgrade to Basic plan to unlock " + feature;
        } else if (PLAN_BASIC.equals(currentPlan)) {
            return "Upgrade to Premium plan to unlock " + feature;
        } else if (PLAN_PREMIUM.equals(currentPlan)) {
            return "Upgrade to Business plan to unlock " + feature;
        }

        return "This feature is available in your plan";
    }

    /**
     * Check if testing mode is enabled
     */
    public static boolean isTestingMode() {
        return TESTING_MODE;
    }
}
