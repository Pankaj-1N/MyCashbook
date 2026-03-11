package com.mycashbook.app.ui.transaction;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Window;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.mycashbook.app.R;
import com.mycashbook.app.model.Transaction;
import com.mycashbook.app.viewmodel.TransactionViewModel;
import com.mycashbook.app.utils.CurrencyUtils;
import com.mycashbook.app.utils.LogUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditTransactionDialog extends Dialog {

    private static final String TAG = "EditTransactionDialog";

    private final TransactionViewModel transactionViewModel;
    private final Transaction transaction;
    private final LifecycleOwner lifecycleOwner;

    private TextView btnTypeIn;
    private TextView btnTypeOut;
    private EditText editAmount;
    private TextView textDate;
    private TextView textTime;
    private EditText editContact;
    private TextView textPaymentMethod;
    private EditText editPaymentDetails;
    private EditText editDescription;
    private com.google.android.material.button.MaterialButton btnSave;
    private TextView btnCancel;
    private android.widget.ImageButton btnClose;

    private Date selectedDate;
    private boolean isCashIn = true;
    private String selectedPaymentMethodName = "Unspecified";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    // Constructor
    public EditTransactionDialog(@NonNull Context context, TransactionViewModel transactionViewModel,
            Transaction transaction) {
        super(context);
        this.transactionViewModel = transactionViewModel;
        this.transaction = transaction;
        this.lifecycleOwner = com.mycashbook.app.utils.ContextUtils.getLifecycleOwner(context);
        this.selectedDate = transaction.getDate() != null ? transaction.getDate() : new Date();
        this.isCashIn = "CREDIT".equalsIgnoreCase(transaction.getType())
                || "IN".equalsIgnoreCase(transaction.getType());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_edit_transaction);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        initViews();
        populateFields();
        setupListeners();
        observeViewModel();

        Log.d(TAG, "EditTransactionDialog created for ID: " + transaction.getId());
    }

    // ============================================================
    // INITIALIZATION
    // ============================================================

    private void initViews() {
        btnTypeIn = findViewById(R.id.btnTypeIn);
        btnTypeOut = findViewById(R.id.btnTypeOut);
        editAmount = findViewById(R.id.editAmount);
        textDate = findViewById(R.id.textDate);
        textTime = findViewById(R.id.textTime);
        editContact = findViewById(R.id.editContact);
        textPaymentMethod = findViewById(R.id.textPaymentMethod);
        editPaymentDetails = findViewById(R.id.editPaymentDetails);
        editDescription = findViewById(R.id.editDescription);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnClose = findViewById(R.id.btnClose);
    }

    private void populateFields() {
        editAmount.setText(CurrencyUtils.formatAmount(transaction.getAmount()));

        // Populate contact if available
        if (transaction.getContact() != null && !transaction.getContact().isEmpty()) {
            editContact.setText(transaction.getContact());
        }

        // Populate Payment Method/App
        if (transaction.getPaymentMethod() != null && !transaction.getPaymentMethod().isEmpty()) {
            selectedPaymentMethodName = transaction.getPaymentMethod();

            // Logic as per user's preference in Add dialog
            String category = transaction.getPaymentMethod();
            textPaymentMethod.setText(category);
            textPaymentMethod.setTextColor(Color.parseColor("#6B7A8F")); // Default grey hint or color

            if (transaction.getPaymentApp() != null && !transaction.getPaymentApp().isEmpty()) {
                editPaymentDetails.setText(transaction.getPaymentApp());
                editPaymentDetails.setVisibility(android.view.View.VISIBLE);
            }
        }

        // Try to parse existing note for legacy data if fields were empty
        String rawNote = transaction.getNote();
        if (rawNote != null) {
            String[] lines = rawNote.split("\n");
            StringBuilder cleanNote = new StringBuilder();
            for (String line : lines) {
                if (line.startsWith("Contact: ") && (editContact.getText().toString().isEmpty())) {
                    editContact.setText(line.substring(9));
                } else if (line.startsWith("Via: ") && (selectedPaymentMethodName.equals("Unspecified"))) {
                    String content = line.substring(5);
                    if (content.contains(" (")) {
                        int index = content.indexOf(" (");
                        selectedPaymentMethodName = content.substring(0, index);
                        String details = content.substring(index + 2, content.length() - 1);
                        textPaymentMethod.setText(selectedPaymentMethodName);
                        editPaymentDetails.setText(details);
                        editPaymentDetails.setVisibility(android.view.View.VISIBLE);
                    } else {
                        selectedPaymentMethodName = content;
                        textPaymentMethod.setText(selectedPaymentMethodName);
                    }
                } else if (line.startsWith("Note: ")) {
                    cleanNote.append(line.substring(6)).append("\n");
                } else if (!line.trim().isEmpty() && !line.startsWith("Contact: ") && !line.startsWith("Via: ")) {
                    cleanNote.append(line).append("\n");
                }
            }
            editDescription.setText(cleanNote.toString().trim());
        }

        if (textPaymentMethod.getText().toString().isEmpty() || "Unspecified".equals(selectedPaymentMethodName)) {
            textPaymentMethod.setText("Select Payment Method");
            editPaymentDetails.setVisibility(android.view.View.GONE);
        }

        // Date & Time
        textDate.setText(dateFormat.format(selectedDate));
        textTime.setText(timeFormat.format(selectedDate));

        // Type
        updateTypeSelection();
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

    private void setupListeners() {
        if (btnClose != null)
            btnClose.setOnClickListener(v -> dismiss());
        btnCancel.setOnClickListener(v -> dismiss());

        btnTypeIn.setOnClickListener(v -> {
            isCashIn = true;
            updateTypeSelection();
        });
        btnTypeOut.setOnClickListener(v -> {
            isCashIn = false;
            updateTypeSelection();
        });

        // Payment Method Selector
        textPaymentMethod.setOnClickListener(v -> {
            androidx.fragment.app.FragmentActivity activity = scanForActivity(getContext());
            if (activity != null) {
                PaymentMethodSelectorDialog dialog = PaymentMethodSelectorDialog.newInstance();
                dialog.setListener(option -> {
                    selectedPaymentMethodName = option.getName();
                    String category = option.getType();
                    if (category == null || category.isEmpty())
                        category = "Payment Method";

                    textPaymentMethod.setText(category);
                    textPaymentMethod.setTextColor(Color.parseColor("#6B7A8F"));

                    editPaymentDetails.setVisibility(android.view.View.VISIBLE);
                    if ("Other".equalsIgnoreCase(option.getName()) || "Other".equalsIgnoreCase(category)) {
                        editPaymentDetails.setText("");
                        editPaymentDetails.setHint("Enter Details");
                        editPaymentDetails.requestFocus();
                    } else {
                        editPaymentDetails.setText(option.getName());
                    }
                });
                dialog.show(activity.getSupportFragmentManager(), "PaymentSelector");
            }
        });

        textDate.setOnClickListener(v -> openDatePicker());
        textTime.setOnClickListener(v -> openTimePicker());
        btnSave.setOnClickListener(v -> attemptSave());

        // Amount Input Watcher
        editAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSave.setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private androidx.fragment.app.FragmentActivity scanForActivity(Context context) {
        return com.mycashbook.app.utils.ContextUtils.getFragmentActivity(context);
    }

    private void attemptSave() {
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

            transaction.setAmount(amount);
            transaction.setType(isCashIn ? "CREDIT" : "DEBIT");

            transaction.setNote(note.isEmpty() ? "" : note);
            transaction.setContact(contact);
            transaction.setPaymentMethod(selectedPaymentMethodName);
            transaction.setPaymentApp(paymentDetails.isEmpty() ? selectedPaymentMethodName : paymentDetails);

            transaction.setDate(selectedDate);
            transaction.setUpdatedAt(new Date());

            btnSave.setEnabled(false);
            transactionViewModel.updateTransaction(transaction);

        } catch (NumberFormatException e) {
            editAmount.setError("Invalid number");
        }
    }

    // ============================================================
    // PICKERS
    // ============================================================

    private void openDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar newDate = Calendar.getInstance();
                    newDate.setTime(selectedDate); // Keep time
                    newDate.set(Calendar.YEAR, year);
                    newDate.set(Calendar.MONTH, month);
                    newDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    selectedDate = newDate.getTime();
                    textDate.setText(dateFormat.format(selectedDate));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void openTimePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);

        android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    Calendar newDate = Calendar.getInstance();
                    newDate.setTime(selectedDate); // Keep date
                    newDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    newDate.set(Calendar.MINUTE, minute);

                    selectedDate = newDate.getTime();
                    textTime.setText(timeFormat.format(selectedDate));
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true // 24 hour format
        );
        timePickerDialog.show();
    }

    // ============================================================
    // VIEWMODEL OBSERVATION
    // ============================================================

    private void observeViewModel() {
        transactionViewModel.getSuccessMessage().observe(lifecycleOwner, successMsg -> {
            if (successMsg != null && !successMsg.isEmpty()) {
                Toast.makeText(getContext(), "Transaction updated", Toast.LENGTH_SHORT).show();
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

    @Override
    public void dismiss() {
        // Remove observers
        transactionViewModel.getSuccessMessage().removeObservers(lifecycleOwner);
        transactionViewModel.getErrorMessage().removeObservers(lifecycleOwner);
        super.dismiss();
    }
}