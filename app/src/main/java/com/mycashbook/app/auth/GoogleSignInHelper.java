package com.mycashbook.app.auth;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.mycashbook.app.R;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;

public class GoogleSignInHelper {

    private static final String TAG = "GoogleSignInHelper";

    // Web Client ID will be loaded from strings.xml
    private static String WEB_CLIENT_ID;

    public interface Callback {
        void onSuccess(BeginSignInResult result);
        void onFailure(Exception e);
    }

    /**
     * Starts the One Tap Sign-In flow.
     * This shows the native Google Sign-In dialog on your phone.
     */
    public static void startOneTap(Context context, Callback callback) {
        try {
            // Load Web Client ID from strings.xml
            if (WEB_CLIENT_ID == null) {
                WEB_CLIENT_ID = context.getString(R.string.web_client_id);
            }

            SignInClient oneTapClient = Identity.getSignInClient(context);

            BeginSignInRequest signInRequest = BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId(WEB_CLIENT_ID)
                            // Set to false to show all Google accounts on the phone
                            .setFilterByAuthorizedAccounts(false)
                            .build())
                    .setAutoSelectEnabled(false)
                    .build();

            Log.d(TAG, "Starting One Tap with Web Client ID: " + WEB_CLIENT_ID);

            oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener(result -> {
                        Log.d(TAG, "One Tap UI shown successfully");
                        callback.onSuccess(result);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "One Tap failed: " + e.getMessage(), e);
                        callback.onFailure(e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in startOneTap", e);
            callback.onFailure(e);
        }
    }

    /**
     * Extracts the SignInCredential from the Intent.
     * Call this after the user selects their account.
     */
    public static SignInCredential getCredential(Context context, Intent data) throws ApiException {
        try {
            Log.d(TAG, "Extracting credential from intent...");
            SignInCredential credential = Identity.getSignInClient(context).getSignInCredentialFromIntent(data);
            Log.d(TAG, "Credential extracted successfully. Email: " + credential.getId());
            return credential;
        } catch (ApiException e) {
            Log.e(TAG, "Failed to extract credential. Status Code: " + e.getStatusCode(), e);
            throw e;
        }
    }
}