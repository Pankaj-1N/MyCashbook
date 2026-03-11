package com.mycashbook.app.ui.book;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.button.MaterialButton;
import com.mycashbook.app.R;
import com.mycashbook.app.model.Book;
import com.mycashbook.app.utils.BookLockManager;
import com.mycashbook.app.utils.ContextUtils;

public class UnlockBookDialog extends Dialog {

    private final Book book;
    private final OnUnlockListener listener;
    private final BookLockManager lockManager;
    private final FragmentActivity activity;
    private String customTitle;
    private boolean autoShowBiometric = true;

    private EditText editPin;
    private MaterialButton btnUnlock;
    private Button btnCancel;
    private TextView btnBiometric;

    public interface OnUnlockListener {
        void onUnlockSuccess();
    }

    public UnlockBookDialog(@NonNull Context context, @NonNull Book book, @NonNull OnUnlockListener listener) {
        super(context);
        this.book = book;
        this.listener = listener;
        this.lockManager = new BookLockManager(context);
        this.activity = ContextUtils.getFragmentActivity(context);
    }

    public void setCustomTitle(String title) {
        this.customTitle = title;
    }

    public void setAutoShowBiometric(boolean autoShow) {
        this.autoShowBiometric = autoShow;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_unlock_book);

        initViews();
        setupListeners();

        // Auto-show biometric if enabled and available AND allowed by flag
        if (autoShowBiometric && book.isUseBiometric() && lockManager.isBiometricAvailable() && activity != null) {
            btnBiometric.setVisibility(android.view.View.VISIBLE);
            showBiometricPrompt();
        } else if (!autoShowBiometric && book.isUseBiometric() && lockManager.isBiometricAvailable()) {
            // Even if auto-show is off, show the button so user can manually trigger it
            if (btnBiometric != null) {
                btnBiometric.setVisibility(android.view.View.VISIBLE);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getWindow() != null) {
            getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void initViews() {
        TextView textTitle = findViewById(R.id.textTitle);
        if (customTitle != null && !customTitle.isEmpty()) {
            textTitle.setText(customTitle);
        } else {
            textTitle.setText("Unlock " + book.getName());
        }

        editPin = findViewById(R.id.editPin);
        btnUnlock = findViewById(R.id.btnUnlock);
        btnCancel = findViewById(R.id.btnCancel);
        btnBiometric = findViewById(R.id.btnBiometric);

        btnUnlock.setEnabled(false);
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dismiss());

        btnUnlock.setOnClickListener(v -> verifyPin());

        editPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnUnlock.setEnabled(s.toString().length() == 4);
                if (s.toString().length() == 4) {
                    verifyPin();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnBiometric.setOnClickListener(v -> showBiometricPrompt());
    }

    private void verifyPin() {
        String enteredPin = editPin.getText().toString();

        if (lockManager.verifyPin(enteredPin, book.getLockPin())) {
            listener.onUnlockSuccess();
            dismiss();
        } else {
            editPin.setError("Incorrect PIN");
            editPin.setText("");
        }
    }

    private void showBiometricPrompt() {
        if (activity == null)
            return;

        lockManager.showBiometricPrompt(activity, book.getName(), new BookLockManager.BiometricCallback() {
            @Override
            public void onSuccess() {
                listener.onUnlockSuccess();
                dismiss();
            }

            @Override
            public void onUsePinInstead() {
                if (editPin != null)
                    editPin.requestFocus();
            }

            @Override
            public void onError(String error) {
                // Determine if we should show a toast, or just log
                // Don't show toast for simple cancellation
                if (!error.contains("Cancel") && !error.contains("use PIN")) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
