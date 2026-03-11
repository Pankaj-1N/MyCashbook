package com.mycashbook.app.model;

public class PaymentOption {
    private String name;
    private int iconResId;
    private String category; // Parent category name

    public PaymentOption(String name, int iconResId, String category) {
        this.name = name;
        this.iconResId = iconResId;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return category;
    }
}
