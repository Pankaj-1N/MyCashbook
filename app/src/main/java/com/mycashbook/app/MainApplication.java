package com.mycashbook.app;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mycashbook.app.backup.BackupCheckWorker;
import com.mycashbook.app.backup.BackupManager;
import com.mycashbook.app.backup.DriveServiceHelper;
import com.mycashbook.app.repository.AuthRepository;

import java.util.concurrent.TimeUnit;

public class MainApplication extends Application {

    private static MainApplication instance;

    private SignInClient oneTapClient;
    private BeginSignInRequest oneTapRequest;

    private DriveServiceHelper driveServiceHelper;
    private BackupManager backupManager;

    // Refactored: Moved isSignedIn to AuthRepository

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        initGoogleOneTap();
        com.mycashbook.app.utils.ThemeManager.applyTheme(this);
        initBackupSystem();
        scheduleBackupCheckWorker();
    }

    public static MainApplication getInstance() {
        return instance;
    }

    // ---------------------------------------------------------
    // GOOGLE ONE TAP INITIALIZATION
    // ---------------------------------------------------------
    private void initGoogleOneTap() {
        oneTapClient = Identity.getSignInClient(this);

        oneTapRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId(getString(R.string.server_client_id))
                                .setFilterByAuthorizedAccounts(false)
                                .build())
                .build();
    }

    public SignInClient getOneTapClient() {
        return oneTapClient;
    }

    public BeginSignInRequest getOneTapRequest() {
        return oneTapRequest;
    }

    // ---------------------------------------------------------
    // BACKUP MANAGER INITIALIZATION
    // ---------------------------------------------------------
    private void initBackupSystem() {

        backupManager = new BackupManager(getApplicationContext());

        // Observe login state
        AuthRepository.getInstance().getIsSignedIn().observeForever(signedIn -> {
            if (signedIn != null && signedIn) {

                // Once signed in → create Drive helper
                DriveServiceHelper.build(getApplicationContext(),
                        service -> {
                            driveServiceHelper = service;
                            backupManager.setDriveServiceHelper(service);

                            // Auto-restore when signed-in on new device
                            backupManager.restoreFromDrive();

                            // If internet → auto backup
                            triggerAutomaticBackup();
                        });
            }
        });
    }

    private void triggerAutomaticBackup() {
        if (isInternetAvailable()) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> backupManager.createAndUploadBackup(), 3000);
        }
    }

    // ---------------------------------------------------------
    // INTERNET CHECK
    // ---------------------------------------------------------
    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null)
            return false;

        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return nc != null &&
                (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }

    // ---------------------------------------------------------
    // 24 HOUR BACKUP CHECK WORKER
    // ---------------------------------------------------------
    private void scheduleBackupCheckWorker() {

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                BackupCheckWorker.class,
                24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "backup_check_worker",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest);
    }
}
