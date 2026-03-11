package com.mycashbook.app.model;

import java.util.List;

public class BackupFile {

    private List<Book> books;
    private List<SubBook> subBooks;
    private List<Transaction> transactions;
    private long backupTimestamp;

    public BackupFile(List<Book> books, List<SubBook> subBooks, List<Transaction> transactions, long backupTimestamp) {
        this.books = books;
        this.subBooks = subBooks;
        this.transactions = transactions;
        this.backupTimestamp = backupTimestamp;
    }

    public List<Book> getBooks() {
        return books;
    }

    public List<SubBook> getSubBooks() {
        return subBooks;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public long getBackupTimestamp() {
        return backupTimestamp;
    }
}
