package com.mycashbook.app.ui.transaction;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.mycashbook.app.R;
import com.mycashbook.app.model.PaymentOption;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TransactionFilterBottomSheet extends BottomSheetDialogFragment {

    public interface OnFilterAppliedListener {
        void onFilterApplied(Date fromDate, Date toDate, String paymentMethod, String paymentApp);

        void onFilterCleared();
    }

    private OnFilterAppliedListener listener;
    private Date fromDate;
    private Date toDate;
    private String selectedPaymentMethod;
    private String selectedPaymentApp;

    private TextView textFromDate, textToDate, btnSelectPayment;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public static TransactionFilterBottomSheet newInstance(Date from, Date to, String method, String app) {
        TransactionFilterBottomSheet fragment = new TransactionFilterBottomSheet();
        fragment.fromDate = from;
        fragment.toDate = to;
        fragment.selectedPaymentMethod = method;
        fragment.selectedPaymentApp = app;
        return fragment;
    }

    public void setListener(OnFilterAppliedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_transaction_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textFromDate = view.findViewById(R.id.textFromDate);
        textToDate = view.findViewById(R.id.textToDate);
        btnSelectPayment = view.findViewById(R.id.btnSelectPayment);
        MaterialButton btnApply = view.findViewById(R.id.btnApply);
        TextView btnClear = view.findViewById(R.id.btnClear);

        updateUI();

        view.findViewById(R.id.btnFromDate).setOnClickListener(v -> showDatePicker(true));
        view.findViewById(R.id.btnToDate).setOnClickListener(v -> showDatePicker(false));

        btnSelectPayment.setOnClickListener(v -> {
            PaymentMethodSelectorDialog dialog = PaymentMethodSelectorDialog.newInstance();
            dialog.setListener(option -> {
                selectedPaymentMethod = option.getType();
                selectedPaymentApp = option.getName();
                btnSelectPayment.setText(selectedPaymentApp + " (" + selectedPaymentMethod + ")");
            });
            dialog.show(getChildFragmentManager(), "PaymentSelector");
        });

        btnApply.setOnClickListener(v -> {
            if (fromDate != null && toDate != null && fromDate.after(toDate)) {
                Toast.makeText(getContext(), "From Date cannot be later than To Date", Toast.LENGTH_SHORT).show();
                return;
            }
            if (listener != null) {
                listener.onFilterApplied(fromDate, toDate, selectedPaymentMethod, selectedPaymentApp);
            }
            dismiss();
        });

        btnClear.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFilterCleared();
            }
            dismiss();
        });
    }

    private void updateUI() {
        if (fromDate != null)
            textFromDate.setText(dateFormat.format(fromDate));
        if (toDate != null)
            textToDate.setText(dateFormat.format(toDate));
        if (selectedPaymentApp != null) {
            btnSelectPayment.setText(
                    selectedPaymentApp + (selectedPaymentMethod != null ? " (" + selectedPaymentMethod + ")" : ""));
        }
    }

    private void showDatePicker(boolean isFromDate) {
        Calendar cal = Calendar.getInstance();
        if (isFromDate && fromDate != null)
            cal.setTime(fromDate);
        else if (!isFromDate && toDate != null)
            cal.setTime(toDate);

        DatePickerDialog dialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            cal.set(year, month, dayOfMonth);
            if (isFromDate) {
                fromDate = cal.getTime();
                textFromDate.setText(dateFormat.format(fromDate));
            } else {
                toDate = cal.getTime();
                textToDate.setText(dateFormat.format(toDate));
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }
}
