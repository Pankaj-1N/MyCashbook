package com.mycashbook.app.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.mycashbook.app.R;
import com.mycashbook.app.base.BaseActivity;
import com.mycashbook.app.utils.DeviceDiagnostics;
import com.mycashbook.app.utils.LogUtils;

public class FeedbackActivity extends BaseActivity {

    private static final String TAG = "FeedbackActivity";
    private static final String SUPPORT_EMAIL = "support@mycashbook.app";

    private EditText editFeedback;
    private MaterialCheckBox checkDiagnostics;
    private MaterialButton btnSend;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        LogUtils.methodEntry(TAG, "onCreate");

        initViews();
        setupListeners();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        editFeedback = findViewById(R.id.editFeedback);
        checkDiagnostics = findViewById(R.id.checkDiagnostics);
        btnSend = findViewById(R.id.btnSend);

        // Setup diagnostics preview text
        TextView textDiagnosticsDetails = findViewById(R.id.textDiagnosticsDetails);
        if (textDiagnosticsDetails != null) {
            textDiagnosticsDetails.setOnClickListener(v -> {
                checkDiagnostics.setChecked(!checkDiagnostics.isChecked());
            });
        }
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendFeedback());
    }

    private void sendFeedback() {
        String feedback = editFeedback.getText().toString().trim();
        if (feedback.isEmpty()) {
            editFeedback.setError("Please describe your feedback");
            editFeedback.requestFocus();
            return;
        }

        StringBuilder body = new StringBuilder();
        body.append(feedback);

        if (checkDiagnostics.isChecked()) {
            body.append(DeviceDiagnostics.getDiagnostics(this));
        }

        launchEmailIntent(body.toString());
    }

    private void launchEmailIntent(String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { SUPPORT_EMAIL });
        intent.putExtra(Intent.EXTRA_SUBJECT, "MyCashBook Feedback");
        intent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            startActivity(Intent.createChooser(intent, "Send Feedback via..."));
            // We don't finish() here so user can come back if they cancel
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
