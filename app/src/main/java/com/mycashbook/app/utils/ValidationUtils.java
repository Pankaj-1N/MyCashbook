package com.mycashbook.app.utils;

import java.util.Date;
import java.util.regex.Pattern;

/**
 * Utility class for input validation
 * Centralizes all validation logic for the app
 */
public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$"
    );

    private static final double MINIMUM_AMOUNT = 0.01;

    /**
     * Validate if email format is correct
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate if amount is valid (positive number > 0)
     */
    public static boolean isValidAmount(String amount) {
        if (amount == null || amount.trim().isEmpty()) {
            return false;
        }

        try {
            double parsedAmount = Double.parseDouble(amount.trim());
            return parsedAmount >= MINIMUM_AMOUNT;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate if amount is valid (positive double > 0)
     */
    public static boolean isValidAmount(double amount) {
        return amount >= MINIMUM_AMOUNT;
    }

    /**
     * Validate if name is not empty
     */
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.trim().length() >= 2;
    }

    /**
     * Validate if description is not empty
     */
    public static boolean isValidDescription(String description) {
        return description != null && !description.trim().isEmpty();
    }

    /**
     * Validate if date is not null
     */
    public static boolean isValidDate(Date date) {
        return date != null;
    }

    /**
     * Get error message for invalid amount
     */
    public static String getAmountErrorMessage(String amount) {
        if (amount == null || amount.trim().isEmpty()) {
            return "Amount is required";
        }

        try {
            double parsedAmount = Double.parseDouble(amount.trim());
            if (parsedAmount < MINIMUM_AMOUNT) {
                return "Amount must be greater than 0";
            }
        } catch (NumberFormatException e) {
            return "Amount must be a valid number";
        }

        return "Invalid amount";
    }

    /**
     * Get error message for invalid name
     */
    public static String getNameErrorMessage(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Name is required";
        }
        if (name.trim().length() < 2) {
            return "Name must be at least 2 characters";
        }
        return "Invalid name";
    }

    /**
     * Get error message for invalid email
     */
    public static String getEmailErrorMessage(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email is required";
        }
        return "Invalid email format";
    }

    /**
     * Get error message for invalid description
     */
    public static String getDescriptionErrorMessage(String description) {
        if (description == null || description.trim().isEmpty()) {
            return "Description is required";
        }
        return "Invalid description";
    }

    /**
     * Get error message for invalid date
     */
    public static String getDateErrorMessage() {
        return "Date is required";
    }

    /**
     * Trim and clean user input
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input.trim();
    }

    /**
     * Check if all fields are valid
     */
    public static boolean validateTransactionFields(String amount, String description) {
        return isValidAmount(amount) && isValidDescription(description);
    }

    /**
     * Check if string is numeric
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}