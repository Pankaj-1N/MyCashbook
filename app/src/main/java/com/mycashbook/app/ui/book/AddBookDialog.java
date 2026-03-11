package com.mycashbook.app.ui.book;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.button.MaterialButton;
import com.mycashbook.app.R;
import com.mycashbook.app.viewmodel.HomeViewModel;

public class AddBookDialog extends Dialog {

    private static final String TAG = "AddBookDialog";

    private final HomeViewModel homeViewModel;
    private final LifecycleOwner lifecycleOwner;
    private final boolean isFreePlan;

    private EditText editBookName;
    private EditText editBookDescription;
    private Button buttonCancel;
    private MaterialButton buttonCreate; // Changed to MaterialButton
    private ImageButton btnClose; // Added close button

    // Currency selection (Added in v4)
    private String selectedCurrencyCode;
    private String selectedCurrencySymbol;
    private String selectedCurrencyName;
    private android.view.View containerCurrency;
    private android.widget.TextView textSelectedCurrency;

    // Lock fields (Added in v5)
    private com.google.android.material.switchmaterial.SwitchMaterial switchLock;
    private android.view.View containerPin;
    private EditText editBookPin;

    // Constructor
    public AddBookDialog(@NonNull Context context, HomeViewModel homeViewModel, boolean isFreePlan) {
        super(context);
        this.homeViewModel = homeViewModel;
        this.lifecycleOwner = com.mycashbook.app.utils.ContextUtils.getLifecycleOwner(context);
        this.isFreePlan = isFreePlan;

        // Default currency from device locale
        try {
            java.util.Currency currency = java.util.Currency.getInstance(java.util.Locale.getDefault());
            selectedCurrencyCode = currency.getCurrencyCode();
            selectedCurrencySymbol = currency.getSymbol();
            selectedCurrencyName = currency.getDisplayName();
        } catch (Exception e) {
            // Fallback to INR if locale doesn't have a currency
            selectedCurrencyCode = "INR";
            selectedCurrencySymbol = "₹";
            selectedCurrencyName = "Indian Rupee";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_book_figma); // Updated layout

        initViews();
        setupListeners();
        observeViewModel();

        Log.d(TAG, "AddBookDialog created (isFreePlan: " + isFreePlan + ")");
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
        editBookName = findViewById(R.id.editBookName);
        editBookDescription = findViewById(R.id.editBookDescription);
        buttonCancel = findViewById(R.id.btnCancel); // Updated ID
        buttonCreate = findViewById(R.id.btnSave); // Updated ID
        btnClose = findViewById(R.id.btnClose); // Added view

        containerCurrency = findViewById(R.id.containerCurrency);
        textSelectedCurrency = findViewById(R.id.textSelectedCurrency);

        switchLock = findViewById(R.id.switchLock);
        containerPin = findViewById(R.id.containerPin);
        editBookPin = findViewById(R.id.editBookPin);

        // Set initial currency text
        updateCurrencyUI();

        // Initially disable create button
        buttonCreate.setEnabled(false);
    }

    private void updateCurrencyUI() {
        if (textSelectedCurrency != null) {
            textSelectedCurrency.setText(com.mycashbook.app.utils.CurrencyUtils.getCurrencyDisplay(selectedCurrencyCode)
                    + " (" + selectedCurrencyName + ")");
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

        // Currency Selector
        if (containerCurrency != null) {
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

        // Lock Switch
        if (switchLock != null) {
            switchLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (containerPin != null) {
                    containerPin.setVisibility(isChecked ? android.view.View.VISIBLE : android.view.View.GONE);
                    if (isChecked && editBookPin != null) {
                        editBookPin.requestFocus();
                    }
                }
            });
        }

        // Create button
        buttonCreate.setOnClickListener(v -> {
            String name = editBookName.getText().toString().trim();
            String description = editBookDescription.getText().toString().trim();

            if (name.isEmpty()) {
                editBookName.setError("Book name is required");
                editBookName.requestFocus();
                return;
            }

            // Validate Lock & PIN
            boolean isLocked = switchLock != null && switchLock.isChecked();
            String pin = null;

            if (isLocked) {
                pin = editBookPin.getText().toString().trim();
                com.mycashbook.app.utils.BookLockManager lockManager = new com.mycashbook.app.utils.BookLockManager(
                        getContext());
                if (!lockManager.isValidPin(pin)) {
                    editBookPin.setError("Enter a 4-digit PIN");
                    editBookPin.requestFocus();
                    return;
                }
                // Hash PIN for storage
                pin = lockManager.hashPin(pin);
            }

            // Disable button to prevent multiple clicks
            buttonCreate.setEnabled(false);

            Log.d(TAG, "Creating book: " + name + " (Locked: " + isLocked + ")");

            // Create book with Lock details
            homeViewModel.createBook(name, description, selectedCurrencyCode, selectedCurrencySymbol,
                    selectedCurrencyName, isFreePlan, isLocked, pin);
        });

        // Text watcher to enable/disable create button
        editBookName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonCreate.setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // ============================================================
    // VIEWMODEL OBSERVATION
    // ============================================================

    private void observeViewModel() {
        // Observe success messages
        homeViewModel.getSuccessMessage().observe(lifecycleOwner, successMsg -> {
            if (successMsg != null && !successMsg.isEmpty()) {
                Toast.makeText(getContext(), successMsg, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Success: " + successMsg);

                // Clear message and dismiss dialog
                homeViewModel.clearSuccessMessage();
                dismiss();
            }
        });

        // Observe error messages
        homeViewModel.getErrorMessage().observe(lifecycleOwner, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error: " + errorMsg);

                // Re-enable create button on error
                buttonCreate.setEnabled(true);

                // Clear error message
                homeViewModel.clearErrorMessage();
            }
        });

        // Observe loading state
        homeViewModel.getLoading().observe(lifecycleOwner, isLoading -> {
            if (isLoading != null) {
                buttonCreate.setEnabled(!isLoading);
                buttonCancel.setEnabled(!isLoading);
                editBookName.setEnabled(!isLoading);
                editBookDescription.setEnabled(!isLoading);
                btnClose.setEnabled(!isLoading); // Disable close button too

                Log.d(TAG, "Loading: " + isLoading);
            }
        });
    }

    @Override
    public void dismiss() {
        // Clear observers to prevent memory leaks
        homeViewModel.getSuccessMessage().removeObservers(lifecycleOwner);
        homeViewModel.getErrorMessage().removeObservers(lifecycleOwner);
        homeViewModel.getLoading().removeObservers(lifecycleOwner);

        super.dismiss();
        Log.d(TAG, "Dialog dismissed");
    }
}