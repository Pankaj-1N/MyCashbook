package com.mycashbook.app.utils;

import com.mycashbook.app.model.Book;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Utility class for currency formatting and calculations
 * Centralizes all currency-related operations globally.
 */
public class CurrencyUtils {

    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    private static final String DECIMAL_FORMAT = "#,##0.00";
    private static final String AMOUNT_FORMAT = "0.00";

    /**
     * Get default currency symbol based on locale
     */
    private static String getDefaultSymbol() {
        try {
            return Currency.getInstance(DEFAULT_LOCALE).getSymbol();
        } catch (Exception e) {
            return "$";
        }
    }

    /**
     * Get default currency code based on locale
     */
    public static String getDefaultCurrencyCode() {
        try {
            return Currency.getInstance(DEFAULT_LOCALE).getCurrencyCode();
        } catch (Exception e) {
            return "USD"; // Ultimate fallback
        }
    }

    /**
     * Returns "Symbol CODE" (e.g. "$ USD")
     */
    public static String getCurrencyDisplay(String code) {
        if (code == null || code.isEmpty())
            return "";
        try {
            Currency currency = Currency.getInstance(code);
            return currency.getSymbol() + " " + currency.getCurrencyCode();
        } catch (Exception e) {
            return code;
        }
    }

    /**
     * Format amount with fixed symbol (e.g., "$ 500.00")
     */
    public static String formatCurrency(double amount, String symbol) {
        try {
            NumberFormat nf = NumberFormat.getInstance(Locale.US); // Use US for numeric formatting (dots/commas)
            DecimalFormat df = (DecimalFormat) nf;
            df.applyPattern(DECIMAL_FORMAT);
            String formatted = df.format(Math.abs(amount));

            String displaySymbol = (symbol != null && !symbol.isEmpty()) ? symbol : getDefaultSymbol();

            if (amount < 0) {
                return "- " + displaySymbol + " " + formatted;
            }
            return displaySymbol + " " + formatted;
        } catch (Exception e) {
            LogUtils.e("CurrencyUtils", "Error formatting currency", e);
            return (symbol != null ? symbol : "") + " 0.00";
        }
    }

    /**
     * Format amount using Book metadata
     */
    public static String formatCurrency(double amount, Book book) {
        if (book == null)
            return formatAmount(amount); // Fix: Avoids flashing wrong currency symbol (e.g. £)
        return formatCurrency(amount, book.getCurrencySymbol());
    }

    /**
     * Backward compatible method (uses device locale)
     */
    public static String formatCurrency(double amount) {
        return formatCurrency(amount, getDefaultSymbol());
    }

    /**
     * Format amount without currency symbol (e.g., "500.00")
     */
    public static String formatAmount(double amount) {
        try {
            NumberFormat nf = NumberFormat.getInstance(Locale.US);
            DecimalFormat df = (DecimalFormat) nf;
            df.applyPattern(AMOUNT_FORMAT);
            return df.format(amount);
        } catch (Exception e) {
            LogUtils.e("CurrencyUtils", "Error formatting amount", e);
            return "0.00";
        }
    }

    /**
     * Format amount from string with currency symbol
     */
    public static String formatCurrency(String amount, String symbol) {
        if (amount == null || amount.isEmpty()) {
            return (symbol != null ? symbol : "") + " 0.00";
        }

        try {
            double value = Double.parseDouble(amount);
            return formatCurrency(value, symbol);
        } catch (NumberFormatException e) {
            LogUtils.e("CurrencyUtils", "Error parsing amount: " + amount, e);
            return (symbol != null ? symbol : "") + " 0.00";
        }
    }

    /**
     * Parse string amount to double (removes provided symbol)
     */
    public static double parseAmount(String amount, String symbol) {
        if (amount == null || amount.isEmpty()) {
            return 0.0;
        }

        try {
            String cleaned = amount;
            if (symbol != null && !symbol.isEmpty()) {
                cleaned = cleaned.replace(symbol, "");
            }
            cleaned = cleaned.replace("-", "").trim();
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            LogUtils.e("CurrencyUtils", "Error parsing amount string: " + amount, e);
            return 0.0;
        }
    }

    public static double parseAmount(String amount) {
        return parseAmount(amount, null);
    }

    /**
     * Format absolute value with currency formatting
     */
    public static String formatAbsoluteAmount(double amount, String symbol) {
        return formatCurrency(Math.abs(amount), symbol);
    }

    /**
     * Format amount for display in transaction list
     */
    public static String formatTransactionAmount(double amount, String symbol) {
        String displaySymbol = (symbol != null && !symbol.isEmpty()) ? symbol : getDefaultSymbol();
        if (amount < 0) {
            return "- " + displaySymbol + " " + formatAmount(Math.abs(amount));
        }
        return "+ " + displaySymbol + " " + formatAmount(amount);
    }

    /**
     * Round amount to 2 decimal places
     */
    public static double roundToTwoDecimals(double amount) {
        try {
            return Math.round(amount * 100.0) / 100.0;
        } catch (Exception e) {
            LogUtils.e("CurrencyUtils", "Error rounding amount", e);
            return 0.0;
        }
    }

    /**
     * Get short currency format (e.g., "5K")
     */
    public static String formatShortCurrency(double amount) {
        if (amount >= 1_000_000) {
            return formatAmount(amount / 1_000_000) + "M";
        } else if (amount >= 1_000) {
            return formatAmount(amount / 1_000) + "K";
        }
        return formatAmount(amount);
    }

    /**
     * Format amount with custom decimal places
     */
    public static String formatWithDecimalPlaces(double amount, int decimalPlaces, String symbol) {
        try {
            StringBuilder format = new StringBuilder("#,##0");
            if (decimalPlaces > 0) {
                format.append(".");
                for (int i = 0; i < decimalPlaces; i++) {
                    format.append("0");
                }
            }
            NumberFormat nf = NumberFormat.getInstance(Locale.US);
            DecimalFormat df = (DecimalFormat) nf;
            df.applyPattern(format.toString());
            return (symbol != null ? symbol : "") + " " + df.format(amount);
        } catch (Exception e) {
            LogUtils.e("CurrencyUtils", "Error formatting with decimal places", e);
            return formatCurrency(amount, symbol);
        }
    }
}
