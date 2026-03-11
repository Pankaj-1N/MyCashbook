package com.mycashbook.app.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.AccelerateDecelerateInterpolator; // <-- missing import added
import com.google.android.material.button.MaterialButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.mycashbook.app.R;
import com.mycashbook.app.base.BaseActivity;
import com.mycashbook.app.ui.home.HomeActivity;
import com.mycashbook.app.utils.LogUtils;
import com.mycashbook.app.utils.SessionManager;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    private AuthViewModel authViewModel;
    private SessionManager sessionManager;
    private ActivityResultLauncher<IntentSenderRequest> oneTapLauncher;

    private MaterialButton btnLogin;
    private ConstraintLayout rootLoginContent;
    private TextView textSignIn;
    private ProgressBar progressBar;
    private View logoCard, topWave, bottomWave;
    private boolean isLoggingIn = false;
    private static final String KEY_MANUAL_LOGOUT = "manual_logout";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.methodEntry(TAG, "onCreate");

        setupTransparentStatusBar();
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        if (checkAutoLogin()) return;

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        initViews();
        initOneTapLauncher();
        setupListeners();
        animateEntry();

        LogUtils.methodExit(TAG, "onCreate");
    }

    private boolean checkAutoLogin() {
        if (sessionManager.isLoggedIn() && !sessionManager.isSessionExpired()) {
            String email = sessionManager.getUserEmail();
            LogUtils.d(TAG, "Auto-login: " + email);
            showSuccessMessage("Welcome back!");
            new Handler(Looper.getMainLooper()).postDelayed(this::navigateToHome, 800);
            return true;
        }
        return false;
    }

    private void initViews() {
        rootLoginContent = findViewById(R.id.rootLoginContent);
        btnLogin = findViewById(R.id.buttonGoogleLogin);
        textSignIn = findViewById(R.id.textSignIn);
        progressBar = findViewById(R.id.progressBar);
        logoCard = findViewById(R.id.logoCard);
        topWave = findViewById(R.id.topWave);
        bottomWave = findViewById(R.id.bottomWave);

        if (progressBar != null) progressBar.setVisibility(View.GONE);
        LogUtils.d(TAG, "Views initialized");
    }

    /**
     * ANIMATION ENTRY FIXED
     */
    private void animateEntry() {

        if (logoCard != null) {
            logoCard.animate().translationYBy(-50f).setDuration(600).withEndAction(() ->
                    logoCard.animate().translationYBy(50f).setDuration(600).start()
            ).start();
        }

        if (btnLogin != null) {
            btnLogin.setAlpha(0f);
            btnLogin.animate().alpha(1f).setDuration(500).setStartDelay(300).start();
        }

        LogUtils.d(TAG, "Entry animation started");

        View waveBackground = findViewById(R.id.waveBackground);

        if (waveBackground != null) {
            waveBackground.animate()
                    .translationY(-50f)
                    .setDuration(3000)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() ->
                            waveBackground.animate()
                                    .translationY(0f)
                                    .setDuration(3000)
                                    .withEndAction(() -> animateWave(waveBackground))
                                    .start()
                    ).start();
        }
    }

    /**
     * FIXED – moved out of animateEntry()
     */
    private void animateWave(View wave) {
        wave.animate()
                .translationY(-50f)
                .setDuration(3000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() ->
                        wave.animate()
                                .translationY(0f)
                                .setDuration(3000)
                                .withEndAction(() -> animateWave(wave))
                                .start()
                ).start();
    }

    private void setupListeners() {
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                if (!isLoggingIn) {
                    btnLogin.animate().scaleX(0.95f).scaleY(0.95f)
                            .setDuration(100).withEndAction(() ->
                                    btnLogin.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                            ).start();

                    startOneTap();
                } else {
                    showWarningMessage("Sign-in already in progress...");
                }
            });
        }
        LogUtils.d(TAG, "Listeners setup");
    }

    private void initOneTapLauncher() {
        oneTapLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    isLoggingIn = false;
                    hideProgress();

                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        try {
                            SignInCredential credential = GoogleSignInHelper.getCredential(this, result.getData());
                            String email = credential.getId();
                            String name = credential.getDisplayName();

                            if (email != null && !email.isEmpty()) {
                                sessionManager.createLoginSession(email, name);
                                authViewModel.setSignedIn(true);

                                LogUtils.d(TAG, "User signed in: " + email);
                                showSuccessMessage("Welcome, " + (name != null ? name : email));

                                performWaveTransition();
                            } else {
                                showErrorMessage("Email not found in credentials");
                                LogUtils.w(TAG, "Email missing");
                            }
                        } catch (ApiException e) {
                            handleLoginError("Login failed", e);
                        } catch (Exception e) {
                            handleLoginError("Unexpected error", e);
                        }
                    } else {
                        showWarningMessage("Sign-in cancelled");
                    }
                }
        );
    }

    private void startOneTap() {
        isLoggingIn = true;
        showProgress();

        GoogleSignInHelper.startOneTap(this, new GoogleSignInHelper.Callback() {
            @Override
            public void onSuccess(BeginSignInResult result) {
                try {
                    IntentSenderRequest request = new IntentSenderRequest.Builder(
                            result.getPendingIntent().getIntentSender()
                    ).build();

                    oneTapLauncher.launch(request);

                } catch (Exception e) {
                    handleLoginError("Failed to open sign-in dialog", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                isLoggingIn = false;
                hideProgress();
                handleLoginError("One Tap Error", e);
            }
        });
    }

    private void handleLoginError(String message, Exception e) {
        isLoggingIn = false;
        hideProgress();
        showErrorMessage(message);
        LogUtils.e(TAG, message, e);
    }

    private void showProgress() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (textSignIn != null) textSignIn.setText("Signing in...");
        if (btnLogin != null) {
            btnLogin.setEnabled(false);
            btnLogin.setAlpha(0.7f);
        }
    }

    private void hideProgress() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (textSignIn != null) textSignIn.setText("Sign in with Google");
        if (btnLogin != null) {
            btnLogin.setEnabled(true);
            btnLogin.setAlpha(1f);
        }
    }

    private void performWaveTransition() {

        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        if (topWave == null || bottomWave == null) {
            navigateToHome();
            return;
        }

        ObjectAnimator topScale = ObjectAnimator.ofFloat(topWave, "scaleY", 1f, 10f);
        ObjectAnimator topTranslate = ObjectAnimator.ofFloat(topWave, "translationY", 0f, screenHeight / 2f);

        ObjectAnimator bottomScale = ObjectAnimator.ofFloat(bottomWave, "scaleY", 1f, 10f);
        ObjectAnimator bottomTranslate = ObjectAnimator.ofFloat(bottomWave, "translationY", 0f, -screenHeight / 2f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(topScale, topTranslate, bottomScale, bottomTranslate);
        set.setDuration(1200);
        set.setInterpolator(new DecelerateInterpolator());

        if (rootLoginContent != null) {
            rootLoginContent.animate().alpha(0f).setDuration(600).setStartDelay(400).start();
        }

        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                navigateToHome();
            }
        });

        set.start();
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finishActivity();
    }

    private void setupTransparentStatusBar() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (topWave != null) topWave.animate().cancel();
        if (bottomWave != null) bottomWave.animate().cancel();
    }
}
