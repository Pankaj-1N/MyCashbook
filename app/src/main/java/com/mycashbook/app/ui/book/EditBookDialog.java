package com.mycashbook.app.ui.book;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mycashbook.app.R;
import com.mycashbook.app.model.Book;
import com.mycashbook.app.utils.BookLockManager;
import com.mycashbook.app.viewmodel.HomeViewModel;

public class EditBookDialog extends Dialog {

    private static final String TAG = "EditBookDialog";

    private final HomeViewModel homeViewModel;
    private final LifecycleOwner lifecycleOwner;
    private final Book bookToEdit;

    private TextView textTitle;
    private EditText editBookName;
    private EditText editBookDescription;
    private Button buttonCancel;
    private MaterialButton buttonSave;
    private ImageButton btnClose;

    // Currency selection
    private String selectedCurrencyCode;
    private String selectedCurrencySymbol;
    private String selectedCurrencyName;
    private android.view.View containerCurrency;
    private android.widget.TextView textSelectedCurrency;
    private int transactionCount = 0;

    // Lock fields
    private SwitchMaterial switchLock;
    private View containerPin;
    private EditText editBookPin;
    private boolean wasLockedInitially = false;

    // Constructor
    public EditBookDialog(@NonNull Context context, HomeViewModel homeViewModel, Book bookToEdit) {
        super(context);
        this.homeViewModel = homeViewModel;
        this.lifecycleOwner = com.mycashbook.app.utils.ContextUtils.getLifecycleOwner(context);
        this.bookToEdit = bookToEdit;

        // Set initial currency
        this.selectedCurrencyCode = bookToEdit.getCurrencyCode();
        this.selectedCurrencySymbol = bookToEdit.getCurrencySymbol();
        this.selectedCurrencyName = bookToEdit.getCurrencyName();
    }

    public void setTransactionCount(int count) {
        this.transactionCount = count;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_book_figma);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        initViews();
        setupListeners();
        setupInitialData();
        observeViewModel();

        Log.d(TAG, "EditBookDialog created for book ID: " + bookToEdit.getId());
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

    // ============================================================
    // INITIALIZATION
    // ============================================================

    private void initViews() {
        textTitle = findViewById(R.id.textTitle);
        editBookName = findViewById(R.id.editBookName);
        editBookDescription = findViewById(R.id.editBookDescription);
        buttonCancel = findViewById(R.id.btnCancel);
        buttonSave = findViewById(R.id.btnSave);
        btnClose = findViewById(R.id.btnClose);

        // Customize for Edit Mode
        textTitle.setText("Edit Book");
        buttonSave.setText("Update Book");

        containerCurrency = findViewById(R.id.containerCurrency);
        textSelectedCurrency = findViewById(R.id.textSelectedCurrency);

        // Lock fields
        switchLock = findViewById(R.id.switchLock);
        containerPin = findViewById(R.id.containerPin);
        editBookPin = findViewById(R.id.editBookPin);

        updateCurrencyUI();
    }

    private void updateCurrencyUI() {
        if (textSelectedCurrency != null) {
            textSelectedCurrency.setText(com.mycashbook.app.utils.CurrencyUtils.getCurrencyDisplay(selectedCurrencyCode)
                    + " (" + selectedCurrencyName + ")");
        }

        // LOCKING LOGIC for currency
        if (transactionCount > 0) {
            if (containerCurrency != null) {
                containerCurrency.setClickable(false);
                containerCurrency.setAlpha(0.6f);
            }
        }
    }

    private void setupInitialData() {
        if (bookToEdit != null) {
            editBookName.setText(bookToEdit.getName());
            editBookDescription.setText(bookToEdit.getDescription());

            // Set lock switch state from book
            wasLockedInitially = bookToEdit.isLocked();
            if (switchLock != null) {
                switchLock.setChecked(wasLockedInitially);
            }

            // Don't show PIN container initially when book is already locked
            // PIN container only shows when:
            // 1. User tries to turn OFF the lock (to verify current PIN)
            // 2. User enables lock on a previously unlocked book (to set new PIN)
            if (containerPin != null) {
                containerPin.setVisibility(View.GONE);
            }
        }
    }

    private void setupListeners() {
        // Cancel button
        buttonCancel.setOnClickListener(v -> {
            Log.d(TAG, "Cancel clicked");
            dismiss();
        });

        // Close button
        btnClose.setOnClickListener(v -> {
            Log.d(TAG, "Close clicked");
            dismiss();
        });

        // Lock Switch Listener
        if (switchLock != null) {
            switchLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isChecked && wasLockedInitially) {
                    // User is trying to turn OFF the lock - require password
                    showUnlockConfirmationDialog();
                } else if (isChecked && !wasLockedInitially) {
                    // Turning ON a NEW lock (book wasn't locked before) - show PIN container
                    if (containerPin != null) {
                        containerPin.setVisibility(View.VISIBLE);
                        if (editBookPin != null) {
                            editBookPin.setHint("Set 4-digit PIN");
                            editBookPin.setText("");
                            editBookPin.requestFocus();
                        }
                    }
                } else if (!isChecked) {
                    // Turning OFF and wasn't locked initially, just hide PIN container
                    if (containerPin != null) {
                        containerPin.setVisibility(View.GONE);
                    }
                }
                // If isChecked && wasLockedInitially: do nothing (keep PIN container hidden)
            });
        }

        // Update button
        buttonSave.setOnClickListener(v -> {
            String name = editBookName.getText().toString().trim();
            String description = editBookDescription.getText().toString().trim();

            if (name.isEmpty()) {
                editBookName.setError("Book name is required");
                editBookName.requestFocus();
                return;
            }

            // Validate lock and PIN
            boolean isLocked = switchLock != null && switchLock.isChecked();
            String newPinHash = null;

            if (isLocked) {
                String pinText = editBookPin.getText().toString().trim();
                BookLockManager lockManager = new BookLockManager(getContext());

                // If book was already locked and PIN field is empty, keep existing PIN
                if (wasLockedInitially && pinText.isEmpty()) {
                    newPinHash = bookToEdit.getLockPin(); // Keep existing
                } else if (!pinText.isEmpty()) {
                    if (!lockManager.isValidPin(pinText)) {
                        editBookPin.setError("Enter a 4-digit PIN");
                        editBookPin.requestFocus();
                        return;
                    }
                    newPinHash = lockManager.hashPin(pinText);
                } else {
                    // New lock but no PIN entered
                    editBookPin.setError("PIN is required to lock");
                    editBookPin.requestFocus();
                    return;
                }
            }

            // Disable button
            buttonSave.setEnabled(false);

            Log.d(TAG, "Updating book: " + name);

            // Update local object
            bookToEdit.setName(name);
            bookToEdit.setDescription(description);
            bookToEdit.setCurrencyCode(selectedCurrencyCode);
            bookToEdit.setCurrencySymbol(selectedCurrencySymbol);
            bookToEdit.setCurrencyName(selectedCurrencyName);
            bookToEdit.setLocked(isLocked);
            if (isLocked) {
                bookToEdit.setLockPin(newPinHash);
            } else {
                bookToEdit.setLockPin(null);
            }

            // Call ViewModel
            homeViewModel.updateBook(bookToEdit);
        });

        // Currency Selector
        if (containerCurrency != null && transactionCount == 0) {
            containerCurrency.setOnClickListener(v -> {
                androidx.fragment.app.FragmentActivity activity = com.mycashbook.app.utils.ContextUtils
                        .getFragmentActivity(getContext());
                if (activity != null) {
                    CurrencySelectorBottomSheet bottomSheet = new CurrencySelectorBottomSheet();
                    bottomSheet.setListener((code, symbol, name) -> {
                        selectedCurrencyCode = code;
                        selectedCurrencySymbol = symbol;
                        selectedCurrencyName = name;
                        updateCurrencyUI();
                    });
                    bottomSheet.show(activity.getSupportFragmentManager(), "CurrencySelector");
                }
            });
        }

        // Text watcher
        editBookName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonSave.setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Show dialog to verify PIN before unlocking
     */
    /**
     * Show dialog to verify PIN before unlocking
     */
    private void showUnlockConfirmationDialog() {
        // Re-check the switch to ON while we verify
        switchLock.setChecked(true);

        // Use the themed UnlockBookDialog for verification
        UnlockBookDialog dialog = new UnlockBookDialog(getContext(), bookToEdit, () -> {
            // PIN correct - allow unlock
            wasLockedInitially = false; // Now treat as unlocked
            switchLock.setChecked(false);
            if (containerPin != null) {
                containerPin.setVisibility(View.GONE);
            }
            Toast.makeText(getContext(), "Lock removed", Toast.LENGTH_SHORT).show();
        });

        // Set custom title for verification context
        dialog.setCustomTitle("Verify PIN to Unlock");

        // Disable auto-biometric prompt to avoid "opening book" feel
        dialog.setAutoShowBiometric(false);

        dialog.show();

        // Handle dialog cancellation to keep switch ON is implicit
        // because we setChecked(true) at the start and only uncheck on success.
    }

    // ============================================================
    // VIEWMODEL OBSERVATION
    // ============================================================

    private void observeViewModel() {
        // Observe success messages
        homeViewModel.getSuccessMessage().observe(lifecycleOwner, successMsg -> {
            if (successMsg != null && !successMsg.isEmpty()) {
                // Check if this success message is relevant to us
                if (successMsg.contains("updated")) {
                    Toast.makeText(getContext(), successMsg, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Success: " + successMsg);

                    // Clear message and dismiss dialog
                    homeViewModel.clearSuccessMessage();
                    dismiss();
                }
            }
        });

        // Observe error messages
        homeViewModel.getErrorMessage().observe(lifecycleOwner, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error: " + errorMsg);

                // Re-enable save button
                buttonSave.setEnabled(true);

                // Clear error message
                homeViewModel.clearErrorMessage();
            }
        });

        // Observe loading state
        homeViewModel.getLoading().observe(lifecycleOwner, isLoading -> {
            if (isLoading != null) {
                buttonSave.setEnabled(!isLoading);
                buttonCancel.setEnabled(!isLoading);
                editBookName.setEnabled(!isLoading);
                editBookDescription.setEnabled(!isLoading);
                btnClose.setEnabled(!isLoading);

                Log.d(TAG, "Loading: " + isLoading);
            }
        });
    }

    @Override
    public void dismiss() {
        homeViewModel.getSuccessMessage().removeObservers(lifecycleOwner);
        homeViewModel.getErrorMessage().removeObservers(lifecycleOwner);
        homeViewModel.getLoading().removeObservers(lifecycleOwner);

        super.dismiss();
        Log.d(TAG, "Dialog dismissed");
    }
}
