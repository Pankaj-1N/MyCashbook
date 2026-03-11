package com.mycashbook.app.ui.report;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.mycashbook.app.R;
import com.mycashbook.app.model.Transaction;
import com.mycashbook.app.viewmodel.TransactionViewModel;

import java.util.List;

public class ReportPreviewActivity extends AppCompatActivity {

    private long subBookId;

    private TransactionViewModel transactionViewModel;
    private List<Transaction> transactions;

    private ListView listPreview;
    private TextView textCount, textDebit, textCredit, textBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_preview);

        subBookId = getIntent().getLongExtra("subBookId", -1);
        if (subBookId == -1) {
            finish();
            return;
        }

        initToolbar();
        initViews();
        initViewModel();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_preview);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Report Preview");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        listPreview = findViewById(R.id.listReportPreview);
        textCount = findViewById(R.id.textPreviewCount);
        textDebit = findViewById(R.id.textPreviewDebit);
        textCredit = findViewById(R.id.textPreviewCredit);
        textBalance = findViewById(R.id.textPreviewBalance);
    }

    private void initViewModel() {
        // Create factory with subBookId
        TransactionViewModelFactory factory = new TransactionViewModelFactory(getApplication(), subBookId);
        transactionViewModel = new ViewModelProvider(this, factory).get(TransactionViewModel.class);

        // Observe transactions list
        transactionViewModel.getTransactions(subBookId).observe(this, list -> {
            if (list != null) {
                this.transactions = list;
                listPreview.setAdapter(new SummaryPreviewAdapter(this, list));
                textCount.setText("Items: " + list.size());
            }
        });

        // Observe total debit
        transactionViewModel.getTotalDebit(subBookId).observe(this, debit -> {
            if (debit != null) {
                textDebit.setText(String.format("Debit: ₹ %.2f", debit));
                updateBalance();
            }
        });

        // Observe total credit
        transactionViewModel.getTotalCredit(subBookId).observe(this, credit -> {
            if (credit != null) {
                textCredit.setText(String.format("Credit: ₹ %.2f", credit));
                updateBalance();
            }
        });
    }

    /**
     * Calculate and update balance (Credit - Debit)
     */
    private void updateBalance() {
        Double debit = transactionViewModel.getTotalDebit(subBookId).getValue();
        Double credit = transactionViewModel.getTotalCredit(subBookId).getValue();

        double debitAmount = (debit != null) ? debit : 0.0;
        double creditAmount = (credit != null) ? credit : 0.0;
        double balance = creditAmount - debitAmount;

        textBalance.setText(String.format("Balance: ₹ %.2f", balance));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ============================================================
    // VIEWMODEL FACTORY
    // ============================================================

    /**
     * Factory for creating TransactionViewModel with subBookId parameter
     */
    public static class TransactionViewModelFactory implements ViewModelProvider.Factory {
        private final android.app.Application application;
        private final long subBookId;

        public TransactionViewModelFactory(android.app.Application application, long subBookId) {
            this.application = application;
            this.subBookId = subBookId;
        }

        @NonNull
        @Override
        public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(TransactionViewModel.class)) {
                return (T) new TransactionViewModel(application, subBookId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}