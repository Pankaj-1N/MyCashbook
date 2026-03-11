package com.mycashbook.app.backup;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper {

    private static final String TAG = "DriveServiceHelper";
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    // --- CONSTANTS FOR DB BACKUP ---
    private static final String DB_BACKUP_FILE_NAME = "MyCashBook_Backup.db";
    private static final String MIME_TYPE_DB = "application/x-sqlite3";

    // --- CONSTANTS FOR JSON BACKUP (Legacy Support) ---
    private static final String JSON_BACKUP_FILE_NAME = "mycashbook_backup.json";
    private static final String MIME_TYPE_JSON = "application/json";

    public DriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }

    /**
     * Interface for the callback used in the static build method.
     */
    public interface DriveHelperCallback {
        void onDriveHelperReady(DriveServiceHelper helper);
    }

    /**
     * Static builder method called by MainApplication.
     * Handles creating credentials and initializing the Drive service.
     */
    public static void build(Context context, DriveHelperCallback callback) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);

        if (account != null) {
            // Create the credential
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    context, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());

            // Build the Drive API client
            Drive googleDriveService = new Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new GsonFactory(),
                    credential)
                    .setApplicationName("MyCashBook")
                    .build();

            // Return the helper instance via the callback
            callback.onDriveHelperReady(new DriveServiceHelper(googleDriveService));
        }
    }

    /**
     * Static builder to create the helper easily from an Activity/Fragment
     */
    public static DriveServiceHelper getDriveService(Context context, GoogleSignInAccount account) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());

        Drive googleDriveService = new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName("MyCashBook")
                .build();

        return new DriveServiceHelper(googleDriveService);
    }

    // =================================================================================
    //  SECTION 1: DATABASE FILE BACKUP (WhatsApp Style - Recommended)
    // =================================================================================

    /**
     * Uploads the local database file to Google Drive.
     */
    public Task<String> uploadFile(java.io.File localFile) {
        return Tasks.call(mExecutor, () -> {
            Log.d(TAG, "Starting DB upload...");

            String existingFileId = findFileId(DB_BACKUP_FILE_NAME);
            File googleDriveFile;

            if (existingFileId != null) {
                Log.d(TAG, "Found existing DB backup. Updating...");
                File fileMetadata = new File();
                fileMetadata.setName(DB_BACKUP_FILE_NAME);

                FileContent mediaContent = new FileContent(MIME_TYPE_DB, localFile);
                googleDriveFile = mDriveService.files().update(existingFileId, fileMetadata, mediaContent).execute();
            } else {
                Log.d(TAG, "Creating new DB backup file...");
                File fileMetadata = new File();
                fileMetadata.setName(DB_BACKUP_FILE_NAME);
                fileMetadata.setParents(Collections.singletonList("root"));

                FileContent mediaContent = new FileContent(MIME_TYPE_DB, localFile);
                googleDriveFile = mDriveService.files().create(fileMetadata, mediaContent).execute();
            }

            if (googleDriveFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }
            return googleDriveFile.getId();
        });
    }

    /**
     * Downloads the database backup file from Drive.
     */
    public Task<Void> downloadFile(java.io.File targetFile) {
        return Tasks.call(mExecutor, () -> {
            Log.d(TAG, "Starting DB download...");

            String fileId = findFileId(DB_BACKUP_FILE_NAME);
            if (fileId == null) {
                throw new IOException("No backup found in Drive.");
            }

            try (OutputStream outputStream = new FileOutputStream(targetFile)) {
                mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            }
            return null;
        });
    }


    // =================================================================================
    //  SECTION 2: JSON BACKUP METHODS (Required by your BackupManager.java)
    // =================================================================================

    /**
     * Uploads a JSON string to Google Drive.
     * (Used by BackupManager.java)
     */
    public boolean uploadBackupJson(String jsonContent) {
        try {
            String fileId = searchFileIdSync(JSON_BACKUP_FILE_NAME);
            ByteArrayContent contentStream = new ByteArrayContent(MIME_TYPE_JSON, jsonContent.getBytes(StandardCharsets.UTF_8));

            if (fileId != null) {
                // Update existing
                mDriveService.files().update(fileId, null, contentStream).execute();
            } else {
                // Create new
                File fileMetadata = new File();
                fileMetadata.setName(JSON_BACKUP_FILE_NAME);
                fileMetadata.setMimeType(MIME_TYPE_JSON);
                // Optional: fileMetadata.setParents(Collections.singletonList("appDataFolder"));
                mDriveService.files().create(fileMetadata, contentStream).execute();
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "JSON Upload failed", e);
            return false;
        }
    }

    /**
     * Downloads the JSON backup content.
     * (Used by BackupManager.java)
     */
    public String downloadBackupJson() {
        try {
            String fileId = searchFileIdSync(JSON_BACKUP_FILE_NAME);
            if (fileId == null) {
                return null;
            }
            InputStream inputStream = mDriveService.files().get(fileId).executeMediaAsInputStream();
            return inputStreamToString(inputStream);
        } catch (Exception e) {
            Log.e(TAG, "JSON Download failed", e);
            return null;
        }
    }

    // =================================================================================
    //  SECTION 3: SHARED HELPER METHODS
    // =================================================================================

    /**
     * Helper method to search for a file by name in Drive (Used by DB methods)
     */
    @Nullable
    private String findFileId(String fileName) throws IOException {
        // Reusing the logic below for consistency
        return searchFileIdSync(fileName);
    }

    /**
     * Helper method to search for a file by name (Synchronous)
     */
    private String searchFileIdSync(String fileName) throws IOException {
        String query = "name = '" + fileName + "' and trashed = false";
        FileList result = mDriveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();

        List<File> files = result.getFiles();
        if (files != null && !files.isEmpty()) {
            return files.get(0).getId();
        }
        return null;
    }

    /**
     * Helper to convert InputStream to String (For JSON)
     */
    private String inputStreamToString(InputStream inputStream) throws IOException {
        StringBuilder textBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String c;
            while ((c = reader.readLine()) != null) {
                textBuilder.append(c);
            }
        }
        return textBuilder.toString();
    }
}
