package com.mycashbook.app.model;

public class Plan {
    private String productId;
    private String title;
    private String description;
    private String price;
    private String offerToken;

    public Plan(String productId, String title, String description, String price, String offerToken) {
        this.productId = productId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.offerToken = offerToken;
    }

    public String getProductId() { return productId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPrice() { return price; }
    public String getOfferToken() { return offerToken; }
}
