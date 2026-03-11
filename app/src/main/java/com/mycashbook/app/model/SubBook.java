package com.mycashbook.app.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(
        tableName = "sub_books",
        foreignKeys = {
                @ForeignKey(
                        entity = Book.class,
                        parentColumns = "id",
                        childColumns = "bookId",
                        onDelete = ForeignKey.CASCADE  // Delete SubBook when Book is deleted
                )
        },
        indices = {
                @Index(value = {"bookId"}, unique = false),
                @Index(value = {"name"}, unique = false)
        }
)
public class SubBook {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private long bookId;  // Foreign key to Book

    @NonNull
    private String name;

    private String description;

    private double balance;  // Running balance/total

    private int transactionCount;  // Number of transactions in this subbook

    private Date createdAt;

    private Date updatedAt;

    // Constructor
    public SubBook(@NonNull long bookId, @NonNull String name, String description, Date createdAt, Date updatedAt) {
        this.bookId = bookId;
        this.name = name;
        this.description = description;
        this.balance = 0.0;
        this.transactionCount = 0;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ---------- Getters & Setters ----------

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBookId() {
        return bookId;
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
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

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
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

    @NonNull
    @Override
    public String toString() {
        return "SubBook{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                ", transactionCount=" + transactionCount +
                '}';
    }
}