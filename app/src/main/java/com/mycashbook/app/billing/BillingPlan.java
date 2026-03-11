package com.mycashbook.app.billing;

public class BillingPlan {

    private String planId;
    private String planName;
    private String priceText;
    private String description;
    // We add offerToken to support the actual Google Billing flow later
    private String offerToken;

    public BillingPlan(String planId, String planName, String priceText, String description, String offerToken) {
        this.planId = planId;
        this.planName = planName;
        this.priceText = priceText;
        this.description = description;
        this.offerToken = offerToken;
    }

    public String getPlanId() {
        return planId;
    }

    public String getPlanName() {
        return planName;
    }

    public String getPriceText() {
        return priceText;
    }

    public String getDescription() {
        return description;
    }

    public String getOfferToken() {
        return offerToken;
    }
}
