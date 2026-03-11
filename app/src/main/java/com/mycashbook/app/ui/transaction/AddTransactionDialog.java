package com.mycashbook.app.ui.transaction;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.button.MaterialButton;
import com.mycashbook.app.R;
import com.mycashbook.app.model.Transaction;
import com.mycashbook.app.viewmodel.TransactionViewModel;
import com.mycashbook.app.utils.CurrencyUtils;
import com.mycashbook.app.utils.LogUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTransactionDialog extends Dialog {

    private static final String TAG = "AddTransactionDialog";

    private final TransactionViewModel transactionViewModel;
    private final long subBookId;
    private final LifecycleOwner lifecycleOwner;

    private TextView btnTypeIn;
    private TextView btnTypeOut;
    private EditText editAmount;
    private TextView textDate;
    private TextView textTime;
    private EditText editContact;
    private TextView textPaymentMethod; // Changed from Spinner
    private EditText editPaymentDetails;
    private EditText editDescription;
    private com.google.android.material.button.MaterialButton btnSave;
    private TextView btnCancel;
    private android.widget.ImageButton btnClose;

    private Date selectedDate;
    private boolean isCashIn = true;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private String selectedPaymentMethodName = "Unspecified"; // Default

    // Constructor
    public AddTransactionDialog(@NonNull Context context, TransactionViewModel transactionViewModel, long subBookId) {
        super(context);
        this.transactionViewModel = transactionViewModel;
        this.subBookId = subBookId;
        this.lifecycleOwner = com.mycashbook.app.utils.ContextUtils.getLifecycleOwner(context);
        this.selectedDate = new Date();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_transaction_figma);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        initViews();
        populateDefaults();
        setupListeners();
        observeViewModel();
    }

    private void initViews() {
        btnTypeIn = findViewById(R.id.btnTypeIn);
        btnTypeOut = findViewById(R.id.btnTypeOut);
        editAmount = findViewById(R.id.editAmount);
        textDate = findViewById(R.id.textDate);
        textTime = findViewById(R.id.textTime);
        editContact = findViewById(R.id.editContact);
        textPaymentMethod = findViewById(R.id.textPaymentMethod); // Updated ID
        editPaymentDetails = findViewById(R.id.editPaymentDetails);
        editDescription = findViewById(R.id.editDescription);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnClose = findViewById(R.id.btnClose);
    }

    private void populateDefaults() {
        textDate.setText(dateFormat.format(selectedDate));
        textTime.setText(timeFormat.format(selectedDate));
        updateTypeSelection();

        // Default: No selection
        textPaymentMethod.setText("Select Payment Method");
        selectedPaymentMethodName = "Unspecified";
        editPaymentDetails.setText("");
        editPaymentDetails.setVisibility(android.view.View.GONE);
    }

    private void setupListeners() {
        btnTypeIn.setOnClickListener(v -> {
            isCashIn = true;
            updateTypeSelection();
        });
        btnTypeOut.setOnClickListener(v -> {
            isCashIn = false;
            updateTypeSelection();
        });

        btnCancel.setOnClickListener(v -> dismiss());
        if (btnClose != null)
            btnClose.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> saveTransaction());

        // Payment Method Selector
        textPaymentMethod.setOnClickListener(v -> {
            androidx.fragment.app.FragmentActivity activity = scanForActivity(getContext());
            if (activity != null) {
                PaymentMethodSelectorDialog dialog = PaymentMethodSelectorDialog.newInstance();
                dialog.setListener(option -> {
                    selectedPaymentMethodName = option.getName();

                    // Box 1: ALWAYS shows Category Name
                    String category = option.getType();
                    if (category == null || category.isEmpty())
                        category = "Payment Method";

                    textPaymentMethod.setText(category);
                    textPaymentMethod.setTextColor(Color.WHITE);
                    textPaymentMethod.setSingleLine(true);

                    // Box 2: Logic
                    editPaymentDetails.setVisibility(android.view.View.VISIBLE);

                    if ("Other".equalsIgnoreCase(option.getName()) || "Other".equalsIgnoreCase(category)) {
                        // Manual Entry Mode
                        editPaymentDetails.setText("");
                        editPaymentDetails.setHint("Enter Details");
                        editPaymentDetails.requestFocus();
                    } else {
                        // Direct Selection Mode - Pre-fill option name
                        editPaymentDetails.setText(option.getName());
                    }

                });
                dialog.show(activity.getSupportFragmentManager(), "PaymentSelector");
            } else {
                Log.e(TAG, "Could not resolve FragmentActivity from context: " + getContext());
            }
        });

        textDate.setOnClickListener(v -> openDatePicker());
        textTime.setOnClickListener(v -> openTimePicker());
    }

    private androidx.fragment.app.FragmentActivity scanForActivity(Context context) {
        return com.mycashbook.app.utils.ContextUtils.getFragmentActivity(context);
    }

    private void updateTypeSelection() {
        if (isCashIn) {
            btnTypeIn.setBackgroundResource(R.drawable.bg_toggle_active_green);
            btnTypeIn.setTextColor(Color.WHITE);
            btnTypeOut.setBackgroundResource(R.drawable.bg_toggle_unselected);
            btnTypeOut.setTextColor(Color.parseColor("#6B7A8F"));
            btnSave.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#00D09C")));
        } else {
            btnTypeOut.setBackgroundResource(R.drawable.bg_toggle_active_red);
            btnTypeOut.setTextColor(Color.WHITE);
            btnTypeIn.setBackgroundResource(R.drawable.bg_toggle_unselected);
            btnTypeIn.setTextColor(Color.parseColor("#6B7A8F"));
            btnSave.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FF5252")));
        }
    }

    private void openDatePicker() {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    selectedDate = cal.getTime();
                    textDate.setText(dateFormat.format(selectedDate));
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void openTimePicker() {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minute) -> {
                    cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    cal.set(Calendar.MINUTE, minute);
                    selectedDate = cal.getTime();
                    textTime.setText(timeFormat.format(selectedDate));
                },
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    private void saveTransaction() {
        String amountStr = editAmount.getText().toString().trim();
        String note = editDescription.getText().toString().trim();
        String contact = editContact.getText().toString().trim();
        String paymentDetails = editPaymentDetails.getText().toString().trim();

        if (amountStr.isEmpty()) {
            editAmount.setError("Amount is required");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                editAmount.setError("Amount must be > 0");
                return;
            }

            btnSave.setEnabled(false);

            Transaction transaction = new Transaction();
            transaction.setSubBookId(subBookId);
            transaction.setType(isCashIn ? "CREDIT" : "DEBIT");
            transaction.setAmount(amount);

            transaction.setNote(note.isEmpty() ? "" : note);
            transaction.setContact(contact);

            // paymentMethod = Category (UPI), paymentApp = Specific (GPay)
            transaction.setPaymentMethod(selectedPaymentMethodName);
            transaction.setPaymentApp(paymentDetails.isEmpty() ? selectedPaymentMethodName : paymentDetails);

            transaction.setDate(selectedDate);
            transaction.setCreatedAt(new Date());
            transaction.setUpdatedAt(new Date());

            transactionViewModel.insertTransaction(transaction);

        } catch (NumberFormatException e) {
            editAmount.setError("Invalid number");
        }
    }

    // observeViewModel
    private void observeViewModel() {
        transactionViewModel.getSuccessMessage().observe(lifecycleOwner, successMsg -> {
            if (successMsg != null && !successMsg.isEmpty()) {
                Toast.makeText(getContext(), successMsg, Toast.LENGTH_SHORT).show();
                transactionViewModel.clearSuccessMessage();
                dismiss();
            }
        });
        transactionViewModel.getErrorMessage().observe(lifecycleOwner, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                btnSave.setEnabled(true);
                transactionViewModel.clearErrorMessage();
            }
        });
    }
}