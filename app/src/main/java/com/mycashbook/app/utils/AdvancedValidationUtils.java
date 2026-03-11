package com.mycashbook.app.utils;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Advanced input validation utilities
 * Comprehensive validation for all app operations
 */
public class AdvancedValidationUtils {

    private static final String TAG = "AdvancedValidationUtils";

    // Regex patterns
    private static final Pattern BOOK_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-._'()&]{2,50}$");
    private static final Pattern SUBBOOK_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-._'()&]{1,30}$");
    private static final Pattern NOTE_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-.,_'()&\\n]{0,200}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$"
    );

    // Constants
    private static final double MIN_AMOUNT = 0.01;
    private static final double MAX_AMOUNT = 999999999.99;
    private static final int MIN_BOOK_NAME_LENGTH = 2;
    private static final int MAX_BOOK_NAME_LENGTH = 50;
    private static final int MIN_DESCRIPTION_LENGTH = 0;
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    /**
     * Validate book name
     */
    public static ValidationResult validateBookName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "Book name is required");
        }

        String trimmed = name.trim();

        if (trimmed.length() < MIN_BOOK_NAME_LENGTH) {
            return new ValidationResult(false, "Book name must be at least " + MIN_BOOK_NAME_LENGTH + " characters");
        }

        if (trimmed.length() > MAX_BOOK_NAME_LENGTH) {
            return new ValidationResult(false, "Book name cannot exceed " + MAX_BOOK_NAME_LENGTH + " characters");
        }

        if (!BOOK_NAME_PATTERN.matcher(trimmed).matches()) {
            return new ValidationResult(false, "Book name contains invalid characters");
        }

        LogUtils.d(TAG, "Book name validated: " + trimmed);
        return new ValidationResult(true, "");
    }

    /**
     * Validate sub-book name
     */
    public static ValidationResult validateSubBookName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "Sub-book name is required");
        }

        String trimmed = name.trim();

        if (trimmed.length() < 1) {
            return new ValidationResult(false, "Sub-book name cannot be empty");
        }

        if (trimmed.length() > 30) {
            return new ValidationResult(false, "Sub-book name cannot exceed 30 characters");
        }

        if (!SUBBOOK_NAME_PATTERN.matcher(trimmed).matches()) {
            return new ValidationResult(false, "Sub-book name contains invalid characters");
        }

        LogUtils.d(TAG, "Sub-book name validated: " + trimmed);
        return new ValidationResult(true, "");
    }

    /**
     * Validate transaction amount
     */
    public static ValidationResult validateAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return new ValidationResult(false, "Amount is required");
        }

        try {
            double amount = Double.parseDouble(amountStr.trim());

            if (amount < MIN_AMOUNT) {
                return new ValidationResult(false, "Amount must be greater than " + MIN_AMOUNT);
            }

            if (amount > MAX_AMOUNT) {
                return new ValidationResult(false, "Amount exceeds maximum limit");
            }

            // Check decimal places (max 2)
            DecimalFormat df = new DecimalFormat("0.00");
            String formatted = df.format(amount);
            double parsed = Double.parseDouble(formatted);

            if (parsed != amount && Math.abs(parsed - amount) > 0.001) {
                return new ValidationResult(false, "Amount can have maximum 2 decimal places");
            }

            LogUtils.d(TAG, "Amount validated: " + amount);
            return new ValidationResult(true, "");

        } catch (NumberFormatException e) {
            LogUtils.w(TAG, "Invalid amount format: " + amountStr, e);
            return new ValidationResult(false, "Invalid amount format");
        }
    }

    /**
     * Validate transaction amount (double)
     */
    public static ValidationResult validateAmount(double amount) {
        if (amount < MIN_AMOUNT) {
            return new ValidationResult(false, "Amount must be greater than " + MIN_AMOUNT);
        }

        if (amount > MAX_AMOUNT) {
            return new ValidationResult(false, "Amount exceeds maximum limit");
        }

        return new ValidationResult(true, "");
    }

    /**
     * Validate transaction note
     */
    public static ValidationResult validateNote(String note) {
        if (note == null) {
            return new ValidationResult(true, ""); // Note is optional
        }

        String trimmed = note.trim();

        if (trimmed.isEmpty()) {
            return new ValidationResult(true, ""); // Optional
        }

        if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
            return new ValidationResult(false, "Note cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters");
        }

        if (!NOTE_PATTERN.matcher(trimmed).matches()) {
            return new ValidationResult(false, "Note contains invalid characters");
        }

        LogUtils.d(TAG, "Note validated");
        return new ValidationResult(true, "");
    }

    /**
     * Validate transaction type
     */
    public static ValidationResult validateTransactionType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return new ValidationResult(false, "Transaction type is required");
        }

        String trimmed = type.trim().toUpperCase();

        if (!("DEBIT".equals(trimmed) || "CREDIT".equals(trimmed))) {
            return new ValidationResult(false, "Invalid transaction type. Must be DEBIT or CREDIT");
        }

        LogUtils.d(TAG, "Transaction type validated: " + trimmed);
        return new ValidationResult(true, "");
    }

    /**
     * Validate date
     */
    public static ValidationResult validateDate(Date date) {
        if (date == null) {
            return new ValidationResult(false, "Date is required");
        }

        Date now = new Date();
        if (date.after(new Date(System.currentTimeMillis() + 86400000))) { // Next day
            return new ValidationResult(false, "Date cannot be in the future");
        }

        LogUtils.d(TAG, "Date validated");
        return new ValidationResult(true, "");
    }

    /**
     * Validate ID
     */
    public static ValidationResult validateId(long id, String entityName) {
        if (id <= 0) {
            return new ValidationResult(false, "Invalid " + entityName + " ID");
        }

        return new ValidationResult(true, "");
    }

    /**
     * Validate email
     */
    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ValidationResult(false, "Email is required");
        }

        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return new ValidationResult(false, "Invalid email format");
        }

        return new ValidationResult(true, "");
    }

    /**
     * Validate description
     */
    public static ValidationResult validateDescription(String description) {
        if (description == null) {
            return new ValidationResult(true, ""); // Optional
        }

        String trimmed = description.trim();

        if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
            return new ValidationResult(false, "Description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters");
        }

        LogUtils.d(TAG, "Description validated");
        return new ValidationResult(true, "");
    }

    /**
     * Comprehensive book validation
     */
    public static ValidationResult validateBook(String name, String description) {
        ValidationResult nameResult = validateBookName(name);
        if (!nameResult.isValid) {
            return nameResult;
        }

        ValidationResult descResult = validateDescription(description);
        if (!descResult.isValid) {
            return descResult;
        }

        LogUtils.d(TAG, "Book validation passed");
        return new ValidationResult(true, "");
    }

    /**
     * Comprehensive transaction validation
     */
    public static ValidationResult validateTransaction(String type, String amountStr, String note, Date date) {
        ValidationResult typeResult = validateTransactionType(type);
        if (!typeResult.isValid) {
            return typeResult;
        }

        ValidationResult amountResult = validateAmount(amountStr);
        if (!amountResult.isValid) {
            return amountResult;
        }

        ValidationResult noteResult = validateNote(note);
        if (!noteResult.isValid) {
            return noteResult;
        }

        ValidationResult dateResult = validateDate(date);
        if (!dateResult.isValid) {
            return dateResult;
        }

        LogUtils.d(TAG, "Transaction validation passed");
        return new ValidationResult(true, "");
    }

    /**
     * Validation result class
     */
    public static class ValidationResult {
        public final boolean isValid;
        public final String errorMessage;

        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }

        @Override
        public String toString() {
            return "ValidationResult{" +
                    "isValid=" + isValid +
                    ", errorMessage='" + errorMessage + '\'' +
                    '}';
        }
    }

    /**
     * Batch validation
     */
    public static ValidationResult validateBatch(java.util.List<ValidationResult> results) {
        for (ValidationResult result : results) {
            if (!result.isValid) {
                return result;
            }
        }
        return new ValidationResult(true, "");
    }

    /**
     * Safe string format
     */
    public static String safeFormat(String format, Object... args) {
        try {
            return String.format(format, args);
        } catch (Exception e) {
            LogUtils.e(TAG, "Error formatting string", e);
            return format;
        }
    }
}