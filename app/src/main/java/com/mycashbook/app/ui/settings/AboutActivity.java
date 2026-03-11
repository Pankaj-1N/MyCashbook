package com.mycashbook.app.ui.settings;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.mycashbook.app.R;
import com.mycashbook.app.base.BaseActivity;
import com.mycashbook.app.utils.LogUtils;

public class AboutActivity extends BaseActivity {

    private static final String TAG = "AboutActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        LogUtils.methodEntry(TAG, "onCreate");

        initViews();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        // Version Name
        TextView textVersion = findViewById(R.id.textVersion);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            textVersion.setText("Version " + pInfo.versionName);
        } catch (Exception e) {
            textVersion.setText("Version 1.0.0");
        }

        // Links
        findViewById(R.id.btnWebsite).setOnClickListener(v -> openUrl("https://google.com"));
        findViewById(R.id.btnPrivacy).setOnClickListener(v -> openUrl("https://policies.google.com/privacy"));
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open browser", Toast.LENGTH_SHORT).show();
            LogUtils.e(TAG, "Error opening URL: " + url, e);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
