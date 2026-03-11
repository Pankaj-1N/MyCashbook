package com.mycashbook.app.utils;

/**
 * Enum for payment methods
 * Used to track how transactions were paid
 */
public enum PaymentMethod {
    CASH("Cash", "💵"),
    UPI("UPI", "📱"),
    DEBIT_CARD("Debit Card", "💳"),
    CREDIT_CARD("Credit Card", "💳"),
    NET_BANKING("Net Banking", "🏦"),
    CHEQUE("Cheque", "📝"),
    WALLET("Digital Wallet", "👛"),
    OTHER("Other", "📋");

    private final String displayName;
    private final String emoji;

    PaymentMethod(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getDisplayNameWithEmoji() {
        return emoji + " " + displayName;
    }

    public static PaymentMethod fromString(String value) {
        for (PaymentMethod method : values()) {
            if (method.name().equals(value)) {
                return method;
            }
        }
        return CASH; // Default
    }
}
