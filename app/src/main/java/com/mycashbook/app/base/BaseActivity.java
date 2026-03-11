package com.mycashbook.app.base;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.mycashbook.app.utils.LogUtils;

/**
 * Base Activity class for all activities
 * Provides common functionality like toolbar, dialogs, loading, error handling
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    protected ProgressDialog progressDialog;
    protected Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        LogUtils.methodEntry(TAG, "onCreate");
    }

    /**
     * Set up toolbar with back button
     */
    protected void setupToolbar(Toolbar toolbar, String title) {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                LogUtils.d(TAG, "Toolbar setup: " + title);
            }
        }
    }

    /**
     * Set up toolbar without back button
     */
    protected void setupToolbarNoBack(Toolbar toolbar, String title) {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowHomeEnabled(false);
                LogUtils.d(TAG, "Toolbar setup (no back): " + title);
            }
        }
    }

    /**
     * Show toast message (short duration)
     */
    protected void showToast(String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            LogUtils.d(TAG, "Toast: " + message);
        }
    }

    /**
     * Show toast message (long duration)
     */
    protected void showToastLong(String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            LogUtils.d(TAG, "Toast (long): " + message);
        }
    }

    /**
     * Show success message
     */
    protected void showSuccessMessage(String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(context, "✓ " + message, Toast.LENGTH_SHORT).show();
            LogUtils.i(TAG, "Success: " + message);
        }
    }

    /**
     * Show error message
     */
    protected void showErrorMessage(String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(context, "✗ " + message, Toast.LENGTH_SHORT).show();
            LogUtils.w(TAG, "Error: " + message);
        }
    }

    /**
     * Show warning message
     */
    protected void showWarningMessage(String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(context, "⚠ " + message, Toast.LENGTH_SHORT).show();
            LogUtils.w(TAG, "Warning: " + message);
        }
    }

    /**
     * Show loading dialog
     */
    protected void showLoadingDialog(String message) {
        try {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(context);
                progressDialog.setCancelable(false);
            }
            progressDialog.setMessage(message != null ? message : "Loading...");
            progressDialog.show();
            LogUtils.d(TAG, "Loading dialog shown: " + message);
        } catch (Exception e) {
            LogUtils.e(TAG, "Error showing loading dialog", e);
        }
    }

    /**
     * Hide loading dialog
     */
    protected void hideLoadingDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                LogUtils.d(TAG, "Loading dialog hidden");
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Error hiding loading dialog", e);
        }
    }

    /**
     * Show alert dialog with single button
     */
    protected void showAlertDialog(String title, String message, String buttonText) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(buttonText, (dialog, which) -> {
                        dialog.dismiss();
                        onAlertDialogPositiveClick();
                    })
                    .setCancelable(false)
                    .show();
            LogUtils.d(TAG, "Alert dialog shown: " + title);
        } catch (Exception e) {
            LogUtils.e(TAG, "Error showing alert dialog", e);
        }
    }

    /**
     * Show confirmation dialog with Yes/No buttons
     */
    protected void showConfirmationDialog(String title, String message, String yesText, String noText) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(yesText, (dialog, which) -> {
                        dialog.dismiss();
                        onConfirmationYes();
                    })
                    .setNegativeButton(noText, (dialog, which) -> {
                        dialog.dismiss();
                        onConfirmationNo();
                    })
                    .setCancelable(false)
                    .show();
            LogUtils.d(TAG, "Confirmation dialog shown: " + title);
        } catch (Exception e) {
            LogUtils.e(TAG, "Error showing confirmation dialog", e);
        }
    }

    /**
     * Called when alert dialog positive button is clicked
     * Override in subclass if needed
     */
    protected void onAlertDialogPositiveClick() {
        // Override in subclass
    }

    /**
     * Called when confirmation dialog Yes button is clicked
     * Override in subclass if needed
     */
    protected void onConfirmationYes() {
        // Override in subclass
    }

    /**
     * Called when confirmation dialog No button is clicked
     * Override in subclass if needed
     */
    protected void onConfirmationNo() {
        // Override in subclass
    }

    /**
     * Check if string is null or empty
     */
    protected boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Check if string is not empty
     */
    protected boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Safe finish activity
     */
    protected void finishActivity() {
        try {
            hideLoadingDialog();
            finish();
            LogUtils.d(TAG, "Activity finished");
        } catch (Exception e) {
            LogUtils.e(TAG, "Error finishing activity", e);
        }
    }

    /**
     * Safe finish activity with result
     */
    protected void finishActivityWithResult(int resultCode) {
        try {
            hideLoadingDialog();
            setResult(resultCode);
            finish();
            LogUtils.d(TAG, "Activity finished with result: " + resultCode);
        } catch (Exception e) {
            LogUtils.e(TAG, "Error finishing activity with result", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideLoadingDialog();
        LogUtils.methodExit(TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.methodExit(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.methodEntry(TAG, "onResume");
    }
}