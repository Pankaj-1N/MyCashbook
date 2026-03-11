package com.mycashbook.app.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.DisplayMetrics;

import java.util.Locale;

/**
 * Utility to collect device diagnostics for feedback reports
 */
public class DeviceDiagnostics {

    public static String getDiagnostics(Context context) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n\n--- Device Diagnostics ---\n");
        sb.append("App Version: ").append(getAppVersion(context)).append("\n");
        sb.append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
        sb.append("Android OS: ").append(Build.VERSION.RELEASE).append(" (SDK ").append(Build.VERSION.SDK_INT)
                .append(")\n");
        sb.append("Locale: ").append(Locale.getDefault().toString()).append("\n");

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        sb.append("Screen: ").append(metrics.widthPixels).append("x").append(metrics.heightPixels).append("\n");

        return sb.toString();
    }

    private static String getAppVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName + " (" + pInfo.versionCode + ")";
        } catch (PackageManager.NameNotFoundException e) {
            return "Unknown";
        }
    }
}
