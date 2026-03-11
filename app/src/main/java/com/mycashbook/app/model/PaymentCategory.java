package com.mycashbook.app.model;

import java.util.List;

public class PaymentCategory {
    private String name;
    private int iconResId;
    private List<PaymentOption> options;
    private boolean isExpanded = false;

    public PaymentCategory(String name, int iconResId, List<PaymentOption> options) {
        this.name = name;
        this.iconResId = iconResId;
        this.options = options;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public List<PaymentOption> getOptions() {
        return options;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
