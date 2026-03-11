package com.mycashbook.app.ui.backup;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.services.drive.DriveScopes;
import com.mycashbook.app.R;
import com.mycashbook.app.backup.DriveServiceHelper;
import com.mycashbook.app.ui.home.HomeActivity; // Import your Home/Dashboard activity

import java.io.File;
import java.security.MessageDigest;

public class BackupActivity extends AppCompatActivity {

    private static final String TAG = "BackupActivity";
    private static final String DB_NAME = "mycashbook_database.db";

    private DriveServiceHelper mDriveServiceHelper;
    private boolean isBackupMode = true;
    private TextView statusTextView;

    // --- NEW: Modern Result Launcher (Fixes silent failures) ---
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // This runs immediately after the Google Sign-In screen closes
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d(TAG, "Sign-In Result: OK. Processing token...");
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleSignInResult(task);
                } else {
                    // CRITICAL: This catches the "Silent Failure"
                    Log.e(TAG, "Sign-In Result: CANCELLED/FAILED (Code: " + result.getResultCode() + ")");
                    statusTextView.setText("Status: Sign-In Cancelled by System");
                    Toast.makeText(this, "Sign-In was cancelled or blocked by Google.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        // UI References
        statusTextView = findViewById(R.id.textStatus);

        // Setup Action Cards
        setupCard(findViewById(R.id.cardBackup),
                "Backup to Drive",
                "Save your entire database securely to Google Drive.",
                R.drawable.ic_cloud,
                true, // isBackup
                v -> {
                    statusTextView.setText("Status: Starting Backup...");
                    requestSignIn(true);
                });

        setupCard(findViewById(R.id.cardRestore),
                "Restore from Drive",
                "Replace current data with the version on Drive.",
                R.drawable.ic_cloud, // Will be rotated or tinted if needed
                false, // isRestore
                v -> {
                    statusTextView.setText("Status: Starting Restore...");
                    // Confirm before restore
                    new android.app.AlertDialog.Builder(this)
                            .setTitle("Overwrite Data?")
                            .setMessage(
                                    "This will replace all current books and transactions with the backup from Drive.\n\nAre you sure?")
                            .setPositiveButton("Restore", (d, w) -> requestSignIn(false))
                            .setNegativeButton("Cancel", null)
                            .show();
                });

        // 2. Log SHA-1 (Check Logcat for this!)
        logSha1();
    }

    private void setupCard(android.view.View rootView, String title, String subtitle, int iconRes, boolean isBackup,
            android.view.View.OnClickListener listener) {
        TextView tvTitle = rootView.findViewById(R.id.textTitle);
        TextView tvSubtitle = rootView.findViewById(R.id.textSubtitle);
        android.widget.ImageView ivIcon = rootView.findViewById(R.id.iconAction);

        tvTitle.setText(title);
        tvSubtitle.setText(subtitle);
        ivIcon.setImageResource(iconRes);

        // Tint icon based on action
        int colorAttr = isBackup ? androidx.appcompat.R.attr.colorPrimary : androidx.appcompat.R.attr.colorError;
        if (!isBackup) {
            // Restore = Orange/Warning color usually
            android.util.TypedValue typedValue = new android.util.TypedValue();
            getTheme().resolveAttribute(androidx.appcompat.R.attr.colorError, typedValue, true);
            ivIcon.setColorFilter(typedValue.data);
            // Also maybe rotate icon for restore (down)?
            ivIcon.setRotation(180);
        }

        rootView.setOnClickListener(listener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Auto-check if already logged in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && GoogleSignIn.hasPermissions(account, new Scope(DriveScopes.DRIVE_FILE))) {
            Log.d(TAG, "Already signed in.");
            updateUI(account);
            mDriveServiceHelper = DriveServiceHelper.getDriveService(this, account);
        }
    }

    private void requestSignIn(boolean isBackup) {
        isBackupMode = isBackup;

        // SIMPLEST REQUEST: No requestIdToken.
        // This reduces the chance of "Developer Console" errors.
        // Get the logged-in user email to force that account
        com.mycashbook.app.utils.SessionManager sessionManager = new com.mycashbook.app.utils.SessionManager(this);
        String userEmail = sessionManager.getUserEmail();

        GoogleSignInOptions.Builder builder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // Privacy: Do NOT request email or profile
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE));

        // CRITICAL: If we have the email, pre-select it to skip account chooser
        if (userEmail != null && !userEmail.isEmpty()) {
            builder.setAccountName(userEmail);
        }

        GoogleSignInOptions signInOptions = builder.build();

        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        // Sign out first to clear any bad states
        client.signOut().addOnCompleteListener(task -> {
            Log.d(TAG, "Launching Sign-In Intent...");
            // Use the new launcher
            signInLauncher.launch(client.getSignInIntent());
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account == null) {
                statusTextView.setText("Status: Error (Account is null)");
                return;
            }

            Log.d(TAG, "Sign-In Successful: " + account.getEmail());
            updateUI(account);

            mDriveServiceHelper = DriveServiceHelper.getDriveService(this, account);

            if (isBackupMode) {
                performBackup();
            } else {
                performRestore();
            }

        } catch (ApiException e) {
            // This prints the REAL error code if Google returns one
            Log.e(TAG, "Sign-in FAILED. Code: " + e.getStatusCode(), e);

            String message = "Sign-In Error: " + e.getStatusCode();
            if (e.getStatusCode() == 10)
                message += " (SHA-1 Mismatch)";
            if (e.getStatusCode() == 12500)
                message += " (Configuration Error)";

            statusTextView.setText(message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void performBackup() {
        if (mDriveServiceHelper == null)
            return;
        statusTextView.setText("Status: Uploading...");

        File dbFile = getDatabasePath(DB_NAME);
        if (!dbFile.exists()) {
            statusTextView.setText("Status: No DB Found");
            Toast.makeText(this, "Database file not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        mDriveServiceHelper.uploadFile(dbFile)
                .addOnSuccessListener(fileId -> {
                    statusTextView.setText("Status: Backup Complete!");
                    Toast.makeText(this, "Backup Successful!", Toast.LENGTH_SHORT).show();
                    // Optional: Go to Dashboard after Backup?
                    // goToDashboard();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Backup Failed", e);
                    statusTextView.setText("Status: Backup Error");
                    Toast.makeText(this, "Backup Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void performRestore() {
        if (mDriveServiceHelper == null)
            return;
        statusTextView.setText("Status: Downloading...");

        File dbFile = getDatabasePath(DB_NAME);

        mDriveServiceHelper.downloadFile(dbFile)
                .addOnSuccessListener(aVoid -> {
                    statusTextView.setText("Status: Restore Done");
                    Toast.makeText(this, "Restore Successful!", Toast.LENGTH_SHORT).show();
                    // CRITICAL: Restart/Go to Dashboard after Restore
                    restartApp();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Restore Failed", e);
                    statusTextView.setText("Status: Restore Error");
                    Toast.makeText(this, "Restore Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // --- Navigation Logic ---
    private void goToDashboard() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void restartApp() {
        // Restart brings you back to Splash/Main, refreshing the DB connection
        Intent intent = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Runtime.getRuntime().exit(0);
        }
    }

    // --- UI Helpers ---
    private void setupStatusLabel() {
        ViewGroup rootView = findViewById(android.R.id.content);
        if (rootView == null)
            return;

        statusTextView = new TextView(this);
        statusTextView.setText("Status: Not Connected");
        statusTextView.setTextSize(14);
        statusTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        statusTextView.setPadding(20, 20, 20, 50);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (rootView.getChildAt(0) instanceof ViewGroup) {
            ((ViewGroup) rootView.getChildAt(0)).addView(statusTextView, params);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if (statusTextView != null) {
            // Privacy: Do NOT show email. Just confirm connection.
            statusTextView.setText("Connected to Google Drive");
            statusTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private void logSha1() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                StringBuilder hexString = new StringBuilder();
                for (byte b : md.digest()) {
                    String hex = Integer.toHexString(0xFF & b);
                    if (hex.length() == 1)
                        hexString.append('0');
                    hexString.append(hex).append(":");
                }
                Log.d(TAG, "RUNTIME SHA-1: " + hexString.toString().toUpperCase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
