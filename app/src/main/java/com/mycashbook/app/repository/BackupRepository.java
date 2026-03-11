package com.mycashbook.app.repository;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mycashbook.app.database.AppDatabase;
import com.mycashbook.app.model.BackupFile;
import com.mycashbook.app.model.Book;
import com.mycashbook.app.model.SubBook;
import com.mycashbook.app.model.Transaction;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class BackupRepository {

    private final AppDatabase db;
    private final Gson gson;

    public BackupRepository(Context context) {
        this.db = AppDatabase.getInstance(context);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    // --------- Create JSON Backup ---------
    public CompletableFuture<String> createBackupJson() {
        return CompletableFuture.supplyAsync(() -> {

            List<Book> books = db.bookDao().getAllBooks();

            // Collect all subbooks across all books
            List<SubBook> subBooks = null;
            if (books != null && !books.isEmpty()) {
                subBooks = new java.util.ArrayList<>();
                for (Book b : books) {
                    if (db.subBookDao().getSubBookCount(b.getId()) > 0) {
                        subBooks.addAll(db.subBookDao().getSubBooksByBookId(b.getId()));
                    }
                }
            }

            List<Transaction> transactions = db.transactionDao().getAllTransactionsForBackup();

            BackupFile backup = new BackupFile(
                    books,
                    subBooks,
                    transactions,
                    System.currentTimeMillis()
            );

            return gson.toJson(backup);

        }, Executors.newSingleThreadExecutor());
    }

    // --------- Restore Backup ---------
    public CompletableFuture<Boolean> restoreBackup(BackupFile data) {
        return CompletableFuture.supplyAsync(() -> {

            db.clearAllTables();

            // Insert books
            if (data.getBooks() != null) {
                for (Book b : data.getBooks()) db.bookDao().insertBook(b);
            }

            // Insert subbooks
            if (data.getSubBooks() != null) {
                for (SubBook s : data.getSubBooks()) db.subBookDao().insertSubBook(s);
            }

            // Insert transactions
            if (data.getTransactions() != null) {
                for (Transaction t : data.getTransactions()) db.transactionDao().insertTransaction(t);
            }

            return true;

        }, Executors.newSingleThreadExecutor());
    }
}
