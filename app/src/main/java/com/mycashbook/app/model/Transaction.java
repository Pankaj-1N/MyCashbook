package com.mycashbook.app.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "transactions", foreignKeys = {
        @ForeignKey(entity = SubBook.class, parentColumns = "id", childColumns = "subBookId", onDelete = ForeignKey.CASCADE)
}, indices = {
        @Index("subBookId"),
        @Index("type")
})
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long subBookId;

    @NonNull
    private String type; // DEBIT or CREDIT

    private double amount;

    private String note;

    private Date date;

    private Date createdAt;

    private Date updatedAt;

    // NEW FIELDS for payment tracking and team collaboration
    private String paymentMethod; // Cash, UPI, Card, etc.

    private String createdBy; // Email of user who created (for team collaboration)

    private String createdByName; // Display name of user (for team collaboration)

    private boolean synced; // Whether synced to cloud (for offline support)

    private String contact; // Contact name or phone

    private String paymentApp; // Specific app like GPay, PhonePe, etc.

    // --- Room uses this empty constructor by default ---
    public Transaction() {
    }

    @Ignore
    public Transaction(long subBookId, @NonNull String type, double amount, String note,
            Date date, Date createdAt, Date updatedAt) {

        this.subBookId = subBookId;
        this.type = type;
        this.amount = amount;
        this.note = note;
        this.date = date;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.paymentMethod = "CASH"; // Default
        this.synced = false; // Default to not synced
    }

    // ---------- Getters & Setters ----------
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSubBookId() {
        return subBookId;
    }

    public void setSubBookId(long subBookId) {
        this.subBookId = subBookId;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    // --- ADDED THIS METHOD TO FIX THE ERROR ---
    // This acts as an alias for getNote()
    public String getDescription() {
        return note;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

    // NEW GETTERS AND SETTERS

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getPaymentApp() {
        return paymentApp;
    }

    public void setPaymentApp(String paymentApp) {
        this.paymentApp = paymentApp;
    }
}
