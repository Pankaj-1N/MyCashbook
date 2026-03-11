package com.mycashbook.app.repository;

import android.content.Context;

import com.mycashbook.app.database.AppDatabase;
import com.mycashbook.app.model.ImportMapping;
import com.mycashbook.app.model.Transaction;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class ImportRepository {

    private final AppDatabase db;

    public ImportRepository(Context context) {
        this.db = AppDatabase.getInstance(context);
    }

    // Insert parsed imported transactions
    public CompletableFuture<Boolean> importTransactions(long subBookId, List<Transaction> txnList) {
        return CompletableFuture.supplyAsync(() -> {
            for (Transaction t : txnList) {
                t.setSubBookId(subBookId);
                t.setCreatedAt(new Date());
                t.setUpdatedAt(new Date());
                db.transactionDao().insertTransaction(t);
            }
            return true;
        }, Executors.newSingleThreadExecutor());
    }
}
