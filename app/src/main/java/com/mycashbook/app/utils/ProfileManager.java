package com.mycashbook.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages user profile data and completion tracking
 * Stores profile data locally in SharedPreferences
 */
public class ProfileManager {

    private static final String PREFS_NAME = "profile_prefs";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_PHONE = "user_phone";
    private static final String KEY_PHOTO_URL = "user_photo_url";

    private final SharedPreferences prefs;

    public ProfileManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ============ Getters ============

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "");
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, "");
    }

    public String getPhotoUrl() {
        return prefs.getString(KEY_PHOTO_URL, "");
    }

    // ============ Setters ============

    public void setEmail(String email) {
        prefs.edit().putString(KEY_EMAIL, email).apply();
    }

    public void setName(String name) {
        prefs.edit().putString(KEY_NAME, name).apply();
    }

    public void setPhone(String phone) {
        prefs.edit().putString(KEY_PHONE, phone).apply();
    }

    public void setPhotoUrl(String photoUrl) {
        prefs.edit().putString(KEY_PHOTO_URL, photoUrl).apply();
    }

    // ============ Profile Completion ============

    /**
     * Calculate profile completion percentage (0-100)
     * Fields: Email (auto from Google), Name, Phone
     */
    public int getCompletionPercentage() {
        int completed = 0;
        int total = 3; // email, name, phone

        if (!getEmail().isEmpty())
            completed++;
        if (!getName().isEmpty())
            completed++;
        if (!getPhone().isEmpty())
            completed++;

        return (completed * 100) / total;
    }

    /**
     * Check if profile is complete
     */
    public boolean isProfileComplete() {
        return getCompletionPercentage() == 100;
    }

    /**
     * Get message about what's missing
     */
    public String getCompletionMessage() {
        int percentage = getCompletionPercentage();
        if (percentage == 100) {
            return "Profile complete!";
        } else if (percentage >= 66) {
            return "Almost there! Add your phone number";
        } else if (percentage >= 33) {
            return "Add your name to complete profile";
        } else {
            return "Complete your profile";
        }
    }

    /**
     * Clear all profile data (for logout)
     */
    public void clearProfile() {
        prefs.edit().clear().apply();
    }
}
