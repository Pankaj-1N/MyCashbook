package com.mycashbook.app.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mycashbook.app.R;
import com.mycashbook.app.auth.LoginActivity;
// TODO: Create BackupActivity and RestoreActivity
// import com.mycashbook.app.ui.backup.BackupActivity;
// import com.mycashbook.app.backup.RestoreActivity;
import com.mycashbook.app.billing.SubscriptionActivity;
import com.mycashbook.app.repository.AuthRepository;
import com.mycashbook.app.utils.FeatureManager;
import com.mycashbook.app.utils.ThemeManager;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.api.services.drive.DriveScopes;
import com.mycashbook.app.backup.DriveServiceHelper;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.app.Activity;
import android.app.ProgressDialog;
import java.io.File;

/**
 * SettingsActivity - Manages app settings with proper error handling
 * GLITCH PREVENTION:
 * - Null safety checks for all views
 * - Proper Google Sign-out handling
 * - Safe navigation with intent flags
 * - Feature gating for premium features
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    // Views
    private ImageButton btnBack;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvThemeLabel; // Theme label
    private SwitchMaterial switchDarkMode;
    private SwitchMaterial switchAppLock;
    private CardView btnGoogleDrive;
    private CardView btnRestoreData;
    private CardView btnUpgrade;
    private CardView btnAbout;
    private CardView btnFeedback; // Added Feedback button
    private CardView sectionAccount;

    // Managers
    private SignInClient signInClient;
    private FeatureManager featureManager;
    private com.mycashbook.app.utils.ProfileManager profileManager;

    // Profile completion views
    private View profileProgressContainer;
    private android.widget.ProgressBar progressProfile;
    private TextView tvProfileCompletion;

    // --- Drive Backup Fields ---
    private DriveServiceHelper mDriveServiceHelper;
    private boolean isBackupMode = true;
    private static final String DB_NAME = "mycashbook_database.db";

    private final ActivityResultLauncher<Intent> driveSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleDriveSignInResult(task);
                } else {
                    Log.e(TAG, "Drive Sign-In Cancelled.");
                    Toast.makeText(this, "Sign-In cancelled.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize managers
        signInClient = Identity.getSignInClient(this);
        featureManager = FeatureManager.getInstance(this);
        profileManager = new com.mycashbook.app.utils.ProfileManager(this);

        initViews();
        loadUserProfile();
        setupListeners();
    }

    /**
     * Initialize all views with null safety
     * GLITCH PREVENTION: Checks if views exist before using
     */
    private void initViews() {
        try {
            btnBack = findViewById(R.id.btnBack);
            tvUserName = findViewById(R.id.tvUserName);
            tvUserEmail = findViewById(R.id.tvUserEmail);
            tvThemeLabel = findViewById(R.id.tvThemeLabel); // Find theme label
            switchDarkMode = findViewById(R.id.switchDarkMode);
            switchAppLock = findViewById(R.id.switchAppLock);
            btnGoogleDrive = findViewById(R.id.btnGoogleDrive);
            btnRestoreData = findViewById(R.id.btnRestoreData);
            btnUpgrade = findViewById(R.id.btnUpgrade);
            btnAbout = findViewById(R.id.btnAbout);
            btnFeedback = findViewById(R.id.btnFeedback); // Find feedback button
            sectionAccount = findViewById(R.id.sectionAccount);

            // Set theme toggle state from saved preference
            if (switchDarkMode != null) {
                boolean isDarkMode = ThemeManager.isDarkMode(this);
                switchDarkMode.setChecked(isDarkMode);

                if (tvThemeLabel != null) {
                    tvThemeLabel.setText(isDarkMode ? "Dark Mode" : "Light Mode");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            Toast.makeText(this, "Error loading settings", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Load user profile from SessionManager and ProfileManager
     * Shows email from session and profile completion progress
     */
    private void loadUserProfile() {
        try {
            // Get data from SessionManager (set during login)
            com.mycashbook.app.utils.SessionManager sessionManager = new com.mycashbook.app.utils.SessionManager(this);
            String sessionEmail = sessionManager.getUserEmail();
            String sessionName = sessionManager.getUserName();

            // Sync session data to ProfileManager if needed
            if (sessionEmail != null && !sessionEmail.isEmpty()) {
                profileManager.setEmail(sessionEmail);
            }
            if (sessionName != null && !sessionName.isEmpty() && profileManager.getName().isEmpty()) {
                profileManager.setName(sessionName);
            }

            // Load from ProfileManager for display
            String name = profileManager.getName();
            String email = profileManager.getEmail();

            if (tvUserName != null) {
                tvUserName
                        .setText(name.isEmpty() ? (sessionName != null ? sessionName : "Complete your profile") : name);
            }
            if (tvUserEmail != null) {
                tvUserEmail.setText(email.isEmpty() ? "No email" : email);
            }

            // Update profile completion progress
            updateProfileCompletion();

        } catch (Exception e) {
            Log.e(TAG, "Error loading user profile", e);
        }
    }

    /**
     * Update profile completion progress indicator
     */
    private void updateProfileCompletion() {
        int percentage = profileManager.getCompletionPercentage();

        if (progressProfile != null) {
            progressProfile.setProgress(percentage);
        }

        if (tvProfileCompletion != null) {
            if (percentage == 100) {
                tvProfileCompletion.setVisibility(View.GONE);
            } else {
                tvProfileCompletion.setVisibility(View.VISIBLE);
                tvProfileCompletion.setText(profileManager.getCompletionMessage());
            }
        }
    }

    /**
     * Setup all click listeners
     * GLITCH PREVENTION: Null checks before setting listeners
     */
    private void setupListeners() {
        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Profile section (optional: navigate to profile edit)
        if (sectionAccount != null) {
            sectionAccount.setOnClickListener(v -> {
                Toast.makeText(this, "Profile editing coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Dark Mode toggle - Functional implementation
        if (switchDarkMode != null) {
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (tvThemeLabel != null) {
                    tvThemeLabel.setText(isChecked ? "Dark Mode" : "Light Mode");
                }
                // Apply theme change using ThemeManager
                ThemeManager.setDarkMode(this, isChecked);
                // Activity will recreate automatically due to config change
            });
        }

        // PIN Lock toggle
        if (switchAppLock != null) {
            switchAppLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Navigate to PIN setup
                    Toast.makeText(this, "PIN Lock setup coming soon", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "PIN Lock disabled", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Backup to Drive
        if (btnGoogleDrive != null) {
            btnGoogleDrive.setOnClickListener(v -> {
                // Seamless Backup Start
                requestDriveSignIn(true);
            });
        }

        // Restore from Backup
        if (btnRestoreData != null) {
            btnRestoreData.setOnClickListener(v -> {
                // Seamless Restore Start
                requestDriveSignIn(false);
            });
        }

        // Upgrade to Pro
        if (btnUpgrade != null) {
            btnUpgrade.setOnClickListener(v -> {
                Intent intent = new Intent(this, SubscriptionActivity.class);
                startActivity(intent);
            });
        }

        // About
        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> {
                startActivity(new Intent(this, AboutActivity.class));
            });
        }

        // Feedback
        if (btnFeedback != null) {
            btnFeedback.setOnClickListener(v -> {
                startActivity(new Intent(this, FeedbackActivity.class));
            });
        }
    }

    /**
     * Show upgrade prompt for premium features
     * GLITCH PREVENTION: Safe navigation to subscription activity
     */
    private void showUpgradePrompt(String feature) {
        String message = featureManager.getUpgradeMessage(feature);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Navigate to subscription after short delay
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(this, SubscriptionActivity.class);
            startActivity(intent);
        }, 1500);
    }

    /**
     * Handle logout with proper cleanup
     * GLITCH PREVENTION: Clears all app state and navigates safely
     */
    private void logout() {
        try {
            signInClient.signOut()
                    .addOnSuccessListener(unused -> {
                        // Update AuthRepository
                        AuthRepository.getInstance().setSignedIn(false);

                        // Reset to FREE plan
                        featureManager.setCurrentPlan(FeatureManager.PLAN_FREE);

                        // Navigate to login with clear task
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Logout failed", e);
                        Toast.makeText(this, "Logout failed. Please try again.", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error during logout", e);
            Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user profile when returning to settings
        loadUserProfile();
    }

    // ============================================================
    // DRIVE BACKUP LOGIC (Seamless Integration)
    // ============================================================

    private void requestDriveSignIn(boolean isBackup) {
        Toast.makeText(this, "Starting Seamless Backup...", Toast.LENGTH_SHORT).show(); // Proof of update
        isBackupMode = isBackup;

        // Get the logged-in user email to force that account
        com.mycashbook.app.utils.SessionManager sessionManager = new com.mycashbook.app.utils.SessionManager(this);
        String userEmail = sessionManager.getUserEmail();

        GoogleSignInOptions.Builder builder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE));

        if (userEmail != null && !userEmail.isEmpty()) {
            builder.setAccountName(userEmail);
        }

        GoogleSignInOptions signInOptions = builder.build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        client.signOut().addOnCompleteListener(task -> {
            driveSignInLauncher.launch(client.getSignInIntent());
        });
    }

    private void handleDriveSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            mDriveServiceHelper = DriveServiceHelper.getDriveService(this, account);

            if (isBackupMode) {
                performBackup();
            } else {
                performRestore();
            }
        } catch (ApiException e) {
            Log.e(TAG, "Drive Sign-in FAILED: " + e.getStatusCode(), e);
            Toast.makeText(this, "Connect Error: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
        }
    }

    private void performBackup() {
        if (mDriveServiceHelper == null)
            return;

        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Backing up...");
        progress.setMessage("Uploading to Google Drive");
        progress.setCancelable(false);
        progress.show();

        File dbFile = getDatabasePath(DB_NAME);
        mDriveServiceHelper.uploadFile(dbFile)
                .addOnSuccessListener(fileId -> {
                    progress.dismiss();
                    Toast.makeText(this, "Backup Successful!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progress.dismiss();
                    Toast.makeText(this, "Backup Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void performRestore() {
        if (mDriveServiceHelper == null)
            return;

        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Restoring...");
        progress.setMessage("Downloading from Google Drive");
        progress.setCancelable(false);
        progress.show();

        File dbFile = getDatabasePath(DB_NAME);
        mDriveServiceHelper.downloadFile(dbFile)
                .addOnSuccessListener(aVoid -> {
                    progress.dismiss();
                    Toast.makeText(this, "Restore Successful!", Toast.LENGTH_SHORT).show();
                    restartApp();
                })
                .addOnFailureListener(e -> {
                    progress.dismiss();
                    Toast.makeText(this, "Restore Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void restartApp() {
        Intent intent = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Runtime.getRuntime().exit(0);
        }
    }
}
