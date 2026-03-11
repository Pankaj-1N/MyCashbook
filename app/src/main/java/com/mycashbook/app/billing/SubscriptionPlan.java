package com.mycashbook.app.billing;

/**
 * SubscriptionPlan - Data model for subscription plans
 */
public class SubscriptionPlan {
    public String id;
    public String name;
    public int price;
    public String period; // "month", "year", or "one-time"
    public String[] features;
    public boolean isCurrent;
    public boolean isPopular; // For "MOST POPULAR" or "BEST VALUE" badge
    public String color; // Hex color for icon background
}
