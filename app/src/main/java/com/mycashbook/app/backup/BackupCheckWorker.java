package com.mycashbook.app.backup;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.mycashbook.app.MainApplication;
import com.mycashbook.app.R;

public class BackupCheckWorker extends Worker {

    private static final long MAX_INTERVAL = 24 * 60 * 60 * 1000; // 24 hours
    private static final String CHANNEL_ID = "backup_channel";

    public BackupCheckWorker(@NonNull Context context,
                             @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        BackupManager manager = new BackupManager(getApplicationContext());
        long lastBackup = manager.getLastBackupTime();

        long current = System.currentTimeMillis();
        long diff = current - lastBackup;

        boolean internetAvailable = MainApplication.getInstance().isInternetAvailable();

        if (internetAvailable) {
            // Do nothing → automatic backup will run in MainApplication
            return Result.success();
        }

        // If no internet & more than 24 hours passed → notify user
        if (diff > MAX_INTERVAL) {
            sendNotification();
        }

        return Result.success();
    }

    private void sendNotification() {

        NotificationManager nm = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "Backup Reminder",
                    NotificationManager.IMPORTANCE_HIGH
            );
            nm.createNotificationChannel(ch);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_backup)
                .setContentTitle("Connect to Internet")
                .setContentText("Your data hasn't been backed up in 24 hours.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        nm.notify(2001, builder.build());
    }
}
