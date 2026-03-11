package com.mycashbook.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * SessionManager - Handles user login state persistence
 * Stores login status to enable auto-login feature
 */
public class SessionManager {

    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "MyCashBookSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_LOGIN_TIME = "loginTime";
    private static final String KEY_MANUAL_LOGOUT = "manual_logout";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    // Constructor
    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ============================================================
    // LOGIN SESSION MANAGEMENT
    // ============================================================

    /**
     * Save login session
     * Call this after successful Google Sign-In
     *
     * @param email User's email
     * @param name User's name (optional)
     */
    public void createLoginSession(String email, String name) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
        editor.putBoolean(KEY_MANUAL_LOGOUT, false);  // Clear manual logout flag
        editor.apply();

        Log.d(TAG, "Login session created for: " + email);
    }

    /**
     * Check if user is logged in
     *
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        Log.d(TAG, "Checking login status: " + isLoggedIn);
        return isLoggedIn;
    }

    /**
     * Get logged-in user's email
     *
     * @return User email or null if not logged in
     */
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Get logged-in user's name
     *
     * @return User name or null if not available
     */
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    /**
     * Get login timestamp
     *
     * @return Login time in milliseconds
     */
    public long getLoginTime() {
        return prefs.getLong(KEY_LOGIN_TIME, 0);
    }

    /**
     * Logout user - with manual logout flag
     * Clears all session data and marks as manual logout
     */
    public void logout() {
        String email = getUserEmail();
        editor.clear();
        editor.putBoolean(KEY_MANUAL_LOGOUT, true);  // Mark as manual logout
        editor.apply();

        Log.d(TAG, "User logged out manually: " + email);
    }

    /**
     * Check if user logged out manually
     * Used to prevent auto-login after manual logout
     *
     * @return true if user logged out manually, false otherwise
     */
    public boolean wasManualLogout() {
        return prefs.getBoolean(KEY_MANUAL_LOGOUT, false);
    }

    /**
     * Check if session is expired (optional - for security)
     * Session expires after 30 days
     *
     * @return true if expired, false if still valid
     */
    public boolean isSessionExpired() {
        long loginTime = getLoginTime();
        long currentTime = System.currentTimeMillis();
        long daysSinceLogin = (currentTime - loginTime) / (1000 * 60 * 60 * 24);

        boolean expired = daysSinceLogin > 30;
        if (expired) {
            Log.w(TAG, "Session expired (" + daysSinceLogin + " days old)");
        }
        return expired;
    }

    /**
     * Update user info (for profile updates)
     *
     * @param name Updated user name
     */
    public void updateUserName(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
        Log.d(TAG, "User name updated: " + name);
    }
}