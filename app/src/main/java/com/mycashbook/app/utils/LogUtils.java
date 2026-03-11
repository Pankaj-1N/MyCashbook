package com.mycashbook.app.utils;

import android.util.Log;
import com.mycashbook.app.constants.AppConstants;

/**
 * Utility class for logging throughout the app
 * Centralizes all logging with consistent format and debug control
 * Only logs when DEBUG_MODE is enabled in AppConstants
 */
public class LogUtils {

    private static final String LOG_PREFIX = "MyCashBook-";
    private static final boolean DEBUG_MODE = AppConstants.DEBUG_MODE;

    /**
     * Debug level log
     * Use for detailed diagnostic information
     */
    public static void d(String tag, String message) {
        if (DEBUG_MODE) {
            Log.d(getTag(tag), message);
        }
    }

    /**
     * Debug level log with exception
     */
    public static void d(String tag, String message, Throwable t) {
        if (DEBUG_MODE) {
            Log.d(getTag(tag), message, t);
        }
    }

    /**
     * Info level log
     * Use for informational messages
     */
    public static void i(String tag, String message) {
        if (DEBUG_MODE) {
            Log.i(getTag(tag), message);
        }
    }

    /**
     * Info level log with exception
     */
    public static void i(String tag, String message, Throwable t) {
        if (DEBUG_MODE) {
            Log.i(getTag(tag), message, t);
        }
    }

    /**
     * Warning level log
     * Use for warning messages that indicate potential issues
     */
    public static void w(String tag, String message) {
        if (DEBUG_MODE) {
            Log.w(getTag(tag), message);
        }
    }

    /**
     * Warning level log with exception
     */
    public static void w(String tag, String message, Throwable t) {
        if (DEBUG_MODE) {
            Log.w(getTag(tag), message, t);
        }
    }

    /**
     * Error level log
     * Use for error messages
     * Note: Error logs are ALWAYS shown regardless of DEBUG_MODE for critical issues
     */
    public static void e(String tag, String message) {
        Log.e(getTag(tag), message);
    }

    /**
     * Error level log with exception
     * Note: Error logs are ALWAYS shown regardless of DEBUG_MODE for critical issues
     */
    public static void e(String tag, String message, Throwable t) {
        Log.e(getTag(tag), message, t);
    }

    /**
     * WTF (What a Terrible Failure) level log
     * Use for severe errors that should never happen
     */
    public static void wtf(String tag, String message) {
        Log.wtf(getTag(tag), message);
    }

    /**
     * WTF level log with exception
     */
    public static void wtf(String tag, String message, Throwable t) {
        Log.wtf(getTag(tag), message, t);
    }

    /**
     * Verbose level log
     */
    public static void v(String tag, String message) {
        if (DEBUG_MODE) {
            Log.v(getTag(tag), message);
        }
    }

    /**
     * Verbose level log with exception
     */
    public static void v(String tag, String message, Throwable t) {
        if (DEBUG_MODE) {
            Log.v(getTag(tag), message, t);
        }
    }

    /**
     * Log method entry (useful for tracing)
     */
    public static void methodEntry(String tag, String methodName) {
        if (DEBUG_MODE) {
            Log.d(getTag(tag), ">>> " + methodName);
        }
    }

    /**
     * Log method exit (useful for tracing)
     */
    public static void methodExit(String tag, String methodName) {
        if (DEBUG_MODE) {
            Log.d(getTag(tag), "<<< " + methodName);
        }
    }

    /**
     * Log method execution with result
     */
    public static void methodResult(String tag, String methodName, String result) {
        if (DEBUG_MODE) {
            Log.d(getTag(tag), methodName + " result: " + result);
        }
    }

    /**
     * Log transaction details
     */
    public static void logTransaction(String tag, String action, String amount, String description) {
        if (DEBUG_MODE) {
            Log.d(getTag(tag), "Transaction " + action + " - Amount: " + amount + ", Desc: " + description);
        }
    }

    /**
     * Log database operation
     */
    public static void logDatabaseOperation(String tag, String operation, String tableName, boolean success) {
        if (DEBUG_MODE) {
            String status = success ? "SUCCESS" : "FAILED";
            Log.d(getTag(tag), "DB " + operation + " on " + tableName + " - " + status);
        }
    }

    /**
     * Log API call
     */
    public static void logApiCall(String tag, String endpoint, String method) {
        if (DEBUG_MODE) {
            Log.d(getTag(tag), "API Call - " + method + " " + endpoint);
        }
    }

    /**
     * Log API response
     */
    public static void logApiResponse(String tag, String endpoint, int statusCode) {
        if (DEBUG_MODE) {
            Log.d(getTag(tag), "API Response - " + endpoint + " Status: " + statusCode);
        }
    }

    /**
     * Get tag with MyCashBook prefix
     */
    private static String getTag(String tag) {
        if (tag == null || tag.isEmpty()) {
            return LOG_PREFIX + "General";
        }
        return LOG_PREFIX + tag;
    }

    /**
     * Log crash information (ALWAYS logged)
     */
    public static void logCrash(String tag, String message, Throwable t) {
        Log.e(getTag(tag), "CRASH: " + message, t);
    }

    /**
     * Create a formatted log message
     */
    public static String createLogMessage(String action, String details) {
        return "[" + System.currentTimeMillis() + "] " + action + " - " + details;
    }

    /**
     * Log performance metric
     */
    public static void logPerformance(String tag, String operation, long timeInMillis) {
        if (DEBUG_MODE) {
            Log.d(getTag(tag), "Performance - " + operation + " took " + timeInMillis + "ms");
        }
    }

    /**
     * Get current timestamp for logging
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * Separator line for log organization
     */
    public static void logSeparator(String tag) {
        if (DEBUG_MODE) {
            Log.d(getTag(tag), "======================================");
        }
    }

    /**
     * Log initialization
     */
    public static void logAppInitialization() {
        if (DEBUG_MODE) {
            Log.d(LOG_PREFIX + "App", "Application initialized - DEBUG_MODE: " + DEBUG_MODE);
        }
    }

    /**
     * Check if debug mode is enabled
     */
    public static boolean isDebugModeEnabled() {
        return DEBUG_MODE;
    }
}