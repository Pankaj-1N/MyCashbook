package com.mycashbook.app.utils;

import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.Executor;

/**
 * Manages book-level locking with PIN and biometric authentication
 */
public class BookLockManager {

    private static final String TAG = "BookLockManager";

    private final Context context;

    public BookLockManager(Context context) {
        this.context = context;
    }

    // ============ PIN Hashing ============

    /**
     * Hash a PIN for secure storage
     * Uses SHA-256 for one-way hashing
     */
    public String hashPin(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pin.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception e) {
            LogUtils.e(TAG, "Error hashing PIN", e);
            return null;
        }
    }

    /**
     * Verify if entered PIN matches stored hash
     */
    public boolean verifyPin(String enteredPin, String storedHash) {
        if (enteredPin == null || storedHash == null) {
            return false;
        }
        String enteredHash = hashPin(enteredPin);
        return storedHash.equals(enteredHash);
    }

    /**
     * Validate PIN format (must be 4 digits)
     */
    public boolean isValidPin(String pin) {
        if (pin == null || pin.length() != 4) {
            return false;
        }
        return pin.matches("\\d{4}");
    }

    // ============ Biometric Support ============

    /**
     * Check if device supports biometric authentication
     */
    public boolean isBiometricAvailable() {
        BiometricManager biometricManager = BiometricManager.from(context);
        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG |
                        BiometricManager.Authenticators.BIOMETRIC_WEAK);
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS;
    }

    /**
     * Get biometric availability status message
     */
    public String getBiometricStatusMessage() {
        BiometricManager biometricManager = BiometricManager.from(context);
        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG |
                        BiometricManager.Authenticators.BIOMETRIC_WEAK);

        switch (canAuthenticate) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return "Biometric available";
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return "No biometric hardware";
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                return "Biometric hardware unavailable";
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return "No biometrics enrolled. Please setup in device settings.";
            default:
                return "Biometric not available";
        }
    }

    /**
     * Show biometric prompt for book unlock
     */
    public void showBiometricPrompt(FragmentActivity activity, String bookName,
            BiometricCallback callback) {
        Executor executor = ContextCompat.getMainExecutor(activity);

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock " + bookName)
                .setSubtitle("Use your fingerprint to unlock this book")
                .setNegativeButtonText("Use PIN instead")
                .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG |
                                BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        callback.onSuccess();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            callback.onUsePinInstead();
                        } else {
                            callback.onError(errString.toString());
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        callback.onError("Authentication failed");
                    }
                });

        biometricPrompt.authenticate(promptInfo);
    }

    /**
     * Callback interface for biometric authentication
     */
    public interface BiometricCallback {
        void onSuccess();

        void onUsePinInstead();

        void onError(String error);
    }
}
