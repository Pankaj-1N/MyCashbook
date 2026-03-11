package com.mycashbook.app.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mycashbook.app.database.AppDatabase;
import com.mycashbook.app.model.Book;
import com.mycashbook.app.model.SubBook;
import com.mycashbook.app.model.Transaction;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackupManager {

    private final Context context;
    private DriveServiceHelper driveServiceHelper;

    private static final String PREF_NAME = "backup_prefs";
    private static final String LAST_BACKUP_TIME = "last_backup_time";

    public BackupManager(Context ctx) {
        this.context = ctx.getApplicationContext();
    }

    public void setDriveServiceHelper(DriveServiceHelper helper) {
        this.driveServiceHelper = helper;
    }

    // -----------------------------------------------------------
    // AUTO BACKUP ENTRY POINT
    // -----------------------------------------------------------
    public void createAndUploadBackup() {
        AsyncTask.execute(() -> {

            if (driveServiceHelper == null) {
                Log.e("BackupManager", "Drive helper null — cannot backup");
                return;
            }

            try {
                String json = createBackupJson();
                // FIX: Updated method name to match DriveServiceHelper
                boolean ok = driveServiceHelper.uploadBackupJson(json);

                if (ok) {
                    markBackupTime();
                    Log.i("BackupManager", "Backup uploaded successfully");
                } else {
                    Log.e("BackupManager", "Backup upload failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // -----------------------------------------------------------
    // AUTO RESTORE ENTRY POINT
    // -----------------------------------------------------------
    public void restoreFromDrive() {
        AsyncTask.execute(() -> {

            if (driveServiceHelper == null) {
                Log.e("BackupManager", "Drive helper null — cannot restore");
                return;
            }

            try {
                // FIX: Updated method name to match DriveServiceHelper
                String json = driveServiceHelper.downloadBackupJson();
                if (json == null) {
                    Log.e("BackupManager", "No backup file in Drive");
                    return;
                }

                applyBackupJson(json);
                Log.i("BackupManager", "Restore completed");

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // -----------------------------------------------------------
    // CREATE JSON BACKUP OF THE DB
    // -----------------------------------------------------------
    private String createBackupJson() {

        AppDatabase db = AppDatabase.getInstance(context);

        List<Book> books = db.bookDao().getAllBooksSync();
        List<SubBook> subBooks = db.subBookDao().getAllSubBooksSync();
        List<Transaction> transactions = db.transactionDao().getAllTransactionsSync();

        Map<String, Object> bundle = new HashMap<>();
        bundle.put("books", books);
        bundle.put("subbooks", subBooks);
        bundle.put("transactions", transactions);

        return new Gson().toJson(bundle);
    }

    // -----------------------------------------------------------
    // APPLY JSON BACKUP INTO ROOM DB (FULL RESTORE)
    // -----------------------------------------------------------
    private void applyBackupJson(String json) {

        AppDatabase db = AppDatabase.getInstance(context);

        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = new Gson().fromJson(json, type);

        List<Book> books = extractList(map, "books", Book.class);
        List<SubBook> subbooks = extractList(map, "subbooks", SubBook.class);
        List<Transaction> transactions = extractList(map, "transactions", Transaction.class);

        db.runInTransaction(() -> {
            // Clear old data
            db.bookDao().deleteAll();
            db.subBookDao().deleteAllSubBooks(); // <-- updated
            db.transactionDao().deleteAll();

            // Insert new data (checking for nulls to be safe)
            if (books != null && !books.isEmpty()) {
                db.bookDao().insertAll(books);
            }
            if (subbooks != null && !subbooks.isEmpty()) {
                db.subBookDao().insertAll(subbooks);
            }
            if (transactions != null && !transactions.isEmpty()) {
                db.transactionDao().insertAll(transactions);
            }
        });

    }

    @Nullable
    private <T> List<T> extractList(Map<String, Object> map, String key, Class<T> type) {
        if (!map.containsKey(key)) return new ArrayList<>();

        Gson gson = new Gson();
        String raw = gson.toJson(map.get(key));
        Type listType = TypeToken.getParameterized(List.class, type).getType();
        return gson.fromJson(raw, listType);
    }

    // -----------------------------------------------------------
    // STORE TIMESTAMP WHEN BACKUP SUCCEEDS
    // -----------------------------------------------------------
    private void markBackupTime() {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putLong(LAST_BACKUP_TIME, System.currentTimeMillis()).apply();
    }

    public long getLastBackupTime() {
        return context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getLong(LAST_BACKUP_TIME, 0);
    }
}
