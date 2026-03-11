package com.mycashbook.app.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "books", indices = {
        @Index(value = { "name" }, unique = false)
})
public class Book {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String name;

    private String description;

    private Date createdAt;

    private Date updatedAt;

    // Currency fields (Added in v4)
    private String currencyCode; // ISO 4217 (e.g., USD, INR)
    private String currencySymbol; // e.g., $, ₹
    private String currencyName; // e.g., US Dollar, Indian Rupee

    public Book(@NonNull String name, String description, Date createdAt, Date updatedAt) {
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        // Default to INR for backward compatibility if not specified
        this.currencyCode = "INR";
        this.currencySymbol = "₹";
        this.currencyName = "Indian Rupee";
    }

    // ---------- Getters & Setters ----------
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    // ============ Lock/Security Fields (Added in v5) ============
    private boolean isLocked = false;
    private String lockPin; // Encrypted/hashed 4-digit PIN
    private boolean useBiometric = false;

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public String getLockPin() {
        return lockPin;
    }

    public void setLockPin(String lockPin) {
        this.lockPin = lockPin;
    }

    public boolean isUseBiometric() {
        return useBiometric;
    }

    public void setUseBiometric(boolean useBiometric) {
        this.useBiometric = useBiometric;
    }
}
