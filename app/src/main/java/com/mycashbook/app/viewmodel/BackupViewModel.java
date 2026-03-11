package com.mycashbook.app.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mycashbook.app.backup.DriveServiceHelper;
import com.mycashbook.app.model.BackupFile;
import com.mycashbook.app.repository.BackupRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackupViewModel extends AndroidViewModel {

    private final BackupRepository repository;
    private DriveServiceHelper driveHelper;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> backupSuccess = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> restoreSuccess = new MutableLiveData<>(null);

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public BackupViewModel(@NonNull Application application) {
        super(application);
        repository = new BackupRepository(application);
    }

    // Setup Drive helper after Google Sign-In
    public void setDriveService(DriveServiceHelper helper) {
        this.driveHelper = helper;
    }

    // ------------------------- BACKUP -------------------------------
    public void startBackup() {
        if (driveHelper == null) {
            message.postValue("Google Drive not connected");
            return;
        }

        loading.postValue(true);

        // Run in background thread
        executor.execute(() -> {
            try {
                // 1. Get JSON from DB (using Future from Repository, blocking wait)
                String json = repository.createBackupJson().get();

                // 2. Upload to Drive (Synchronous call)
                boolean success = driveHelper.uploadBackupJson(json);

                // 3. Update UI
                mainHandler.post(() -> {
                    loading.setValue(false);
                    if (success) {
                        backupSuccess.setValue(true);
                        message.setValue("Backup uploaded successfully");
                    } else {
                        backupSuccess.setValue(false);
                        message.setValue("Upload failed");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    loading.setValue(false);
                    message.setValue("Backup failed: " + e.getMessage());
                    backupSuccess.setValue(false);
                });
            }
        });
    }

    // ------------------------- RESTORE -------------------------------
    public void startRestore() {

        if (driveHelper == null) {
            message.postValue("Google Drive not connected");
            return;
        }

        loading.postValue(true);

        executor.execute(() -> {
            try {
                // 1. Download from Drive (Synchronous)
                String json = driveHelper.downloadBackupJson();

                if (json == null || json.trim().isEmpty()) {
                    mainHandler.post(() -> {
                        loading.setValue(false);
                        message.setValue("No backup file found in Drive");
                        restoreSuccess.setValue(false);
                    });
                    return;
                }

                // 2. Parse
                BackupFile data = gson.fromJson(json, BackupFile.class);

                // 3. Restore to DB (Future blocking wait)
                boolean dbSuccess = repository.restoreBackup(data).get();

                mainHandler.post(() -> {
                    loading.setValue(false);
                    restoreSuccess.setValue(dbSuccess);
                    if (dbSuccess) {
                        message.setValue("Restore completed successfully");
                    } else {
                        message.setValue("Restore failed to save to DB");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    loading.setValue(false);
                    message.setValue("Restore failed: " + e.getMessage());
                    restoreSuccess.setValue(false);
                });
            }
        });
    }

    // ------------------------- GETTERS -------------------------------
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getBackupSuccess() { return backupSuccess; }
    public LiveData<Boolean> getRestoreSuccess() { return restoreSuccess; }
}
