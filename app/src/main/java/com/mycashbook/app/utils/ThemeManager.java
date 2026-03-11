package com.mycashbook.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * ThemeManager - Manages app theme switching between Dark and Light modes
 * 
 * Features:
 * - Persists user preference using SharedPreferences
 * - Uses AppCompatDelegate for seamless runtime theme switching
 * - Default theme: Dark mode
 */
public class ThemeManager {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";

    /**
     * Check if dark mode is currently enabled
     * 
     * @param context Application context
     * @return true if dark mode, false if light mode
     */
    public static boolean isDarkMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DARK_MODE, true); // Default: dark mode
    }

    /**
     * Set the theme mode and apply it immediately
     * 
     * @param context Application context
     * @param isDark  true for dark mode, false for light mode
     */
    public static void setDarkMode(Context context, boolean isDark) {
        // Save preference
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DARK_MODE, isDark).apply();

        // Apply theme immediately
        applyThemeMode(isDark);
    }

    /**
     * Apply the saved theme preference
     * Call this on app startup (Application class or SplashActivity)
     * 
     * @param context Application context
     */
    public static void applyTheme(Context context) {
        boolean isDark = isDarkMode(context);
        applyThemeMode(isDark);
    }

    /**
     * Internal method to set the night mode
     */
    private static void applyThemeMode(boolean isDark) {
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Toggle between dark and light mode
     * 
     * @param context Application context
     * @return new state (true = dark, false = light)
     */
    public static boolean toggleTheme(Context context) {
        boolean currentMode = isDarkMode(context);
        boolean newMode = !currentMode;
        setDarkMode(context, newMode);
        return newMode;
    }
}
