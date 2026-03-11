package com.mycashbook.app.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mycashbook.app.MainApplication;
import com.mycashbook.app.R;
// import com.mycashbook.app.ui.auth.LoginActivity; <--- REMOVED WRONG IMPORT
import com.mycashbook.app.auth.LoginActivity; // <--- ADDED CORRECT IMPORT
import com.mycashbook.app.ui.home.HomeActivity;
import com.mycashbook.app.utils.ThemeManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Apply saved theme preference BEFORE super.onCreate()
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {

            // Ensure MainApplication has this static LiveData field
            Boolean signedIn = com.mycashbook.app.repository.AuthRepository.getInstance().getIsSignedIn().getValue();
            if (signedIn != null && signedIn) {
                goHome();
            } else {
                goLogin();
            }

        }, 1200); // 1.2 seconds splash
    }

    private void goLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    private void goHome() {
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
        finish();
    }
}
