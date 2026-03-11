package com.mycashbook.app.utils;

import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Centralized error handling utility
 * Converts exceptions to user-friendly messages and handles errors gracefully
 * UPDATED: Now accepts Throwable to handle all error types
 */
public class ErrorHandlingUtils {

    private static final String TAG = "ErrorHandlingUtils";

    /**
     * Get user-friendly error message from Throwable (accepts all error types)
     */
    public static String getErrorMessage(Throwable throwable) {
        if (throwable == null) {
            return "An unknown error occurred";
        }

        String exceptionType = throwable.getClass().getSimpleName();
        String message = throwable.getMessage();

        // Database errors
        if (throwable instanceof SQLiteException || throwable instanceof SQLException) {
            LogUtils.e(TAG, "Database error: " + message, throwable);
            return "Database error. Please try again later.";
        }

        // Network errors
        if (throwable instanceof SocketTimeoutException) {
            LogUtils.e(TAG, "Network timeout: " + message, throwable);
            return "Request timed out. Please check your connection.";
        }

        if (throwable instanceof SocketException) {
            LogUtils.e(TAG, "Network error: " + message, throwable);
            return "Network connection error. Please try again.";
        }

        if (throwable instanceof IOException) {
            LogUtils.e(TAG, "IO error: " + message, throwable);
            return "Connection error. Please check your internet.";
        }

        // Null pointer exception
        if (throwable instanceof NullPointerException) {
            LogUtils.e(TAG, "Null pointer error: " + message, throwable);
            return "Data error. Please try again.";
        }

        // Number format exception
        if (throwable instanceof NumberFormatException) {
            LogUtils.e(TAG, "Number format error: " + message, throwable);
            return "Invalid number format.";
        }

        // Illegal argument exception
        if (throwable instanceof IllegalArgumentException) {
            LogUtils.e(TAG, "Invalid argument: " + message, throwable);
            return "Invalid data provided.";
        }

        // Generic error message
        LogUtils.e(TAG, "Unknown error (" + exceptionType + "): " + message, throwable);
        return "An unexpected error occurred. Please try again.";
    }

    /**
     * Overloaded method for backward compatibility with Exception
     */
    public static String getErrorMessage(Exception e) {
        return getErrorMessage((Throwable) e);
    }

    /**
     * Safe database operation wrapper
     */
    public static <T> T safeDbOperation(String operationName, DatabaseOperation<T> operation) {
        try {
            LogUtils.d(TAG, "Starting DB operation: " + operationName);
            T result = operation.execute();
            LogUtils.d(TAG, "DB operation succeeded: " + operationName);
            return result;
        } catch (SQLException e) {
            LogUtils.e(TAG, "Database operation failed: " + operationName, e);
            throw new RuntimeException("Database error: " + getErrorMessage(e));
        } catch (Exception e) {
            LogUtils.e(TAG, "Unexpected error in DB operation: " + operationName, e);
            throw new RuntimeException("Operation failed: " + getErrorMessage(e));
        }
    }

    /**
     * Validate critical data before processing
     */
    public static boolean validateCriticalData(String... values) {
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                LogUtils.w(TAG, "Critical data validation failed: empty/null value");
                return false;
            }
        }
        return true;
    }

    /**
     * Safely handle null objects
     */
    public static <T> T getOrNull(T object, String fieldName) {
        if (object == null) {
            LogUtils.w(TAG, "Null object for field: " + fieldName);
        }
        return object;
    }

    /**
     * Safely get ID from object
     */
    public static long getSafeId(long id, String objectName) {
        if (id <= 0) {
            LogUtils.w(TAG, "Invalid ID for " + objectName + ": " + id);
            return -1;
        }
        return id;
    }

    /**
     * Safely parse double
     */
    public static double safeParseDouble(String value, double defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            LogUtils.w(TAG, "Failed to parse double: " + value, e);
            return defaultValue;
        }
    }

    /**
     * Safely parse long
     */
    public static long safeParseLong(String value, long defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            LogUtils.w(TAG, "Failed to parse long: " + value, e);
            return defaultValue;
        }
    }

    /**
     * Safely parse integer
     */
    public static int safeParseInt(String value, int defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            LogUtils.w(TAG, "Failed to parse int: " + value, e);
            return defaultValue;
        }
    }

    /**
     * Safely execute operation with fallback
     */
    public static <T> T executeWithFallback(String operationName, SafeOperation<T> operation, T fallbackValue) {
        try {
            LogUtils.d(TAG, "Executing: " + operationName);
            return operation.execute();
        } catch (Exception e) {
            LogUtils.e(TAG, operationName + " failed, using fallback", e);
            return fallbackValue;
        }
    }

    /**
     * Check if error is recoverable
     */
    public static boolean isRecoverableError(Throwable throwable) {
        return throwable instanceof SocketException
                || throwable instanceof SocketTimeoutException
                || throwable instanceof IOException
                || throwable instanceof SQLException;
    }

    /**
     * Get error code from exception
     */
    public static int getErrorCode(Throwable throwable) {
        if (throwable instanceof SQLException) {
            return 1001; // Database error
        } else if (throwable instanceof SocketTimeoutException) {
            return 2001; // Timeout
        } else if (throwable instanceof SocketException) {
            return 2002; // Network error
        } else if (throwable instanceof IOException) {
            return 2003; // IO error
        } else if (throwable instanceof NullPointerException) {
            return 3001; // Null pointer
        } else if (throwable instanceof NumberFormatException) {
            return 4001; // Number format
        }
        return 9999; // Unknown error
    }

    /**
     * Log and rethrow exception
     */
    public static void logAndRethrow(String operation, Throwable throwable) {
        LogUtils.e(TAG, "Operation failed: " + operation, throwable);
        throw new RuntimeException("Operation failed: " + getErrorMessage(throwable), throwable);
    }

    /**
     * Functional interface for database operations
     */
    public interface DatabaseOperation<T> {
        T execute() throws SQLException;
    }

    /**
     * Functional interface for safe operations
     */
    public interface SafeOperation<T> {
        T execute() throws Exception;
    }

    /**
     * Check if object is valid
     */
    public static <T> boolean isValid(T object) {
        return object != null;
    }

    /**
     * Get collection size safely
     */
    public static int getSafeSize(java.util.Collection<?> collection) {
        return collection != null ? collection.size() : 0;
    }
}