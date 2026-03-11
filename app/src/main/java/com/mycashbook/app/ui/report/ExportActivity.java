package com.mycashbook.app.ui.report;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.mycashbook.app.R;
import com.mycashbook.app.model.Transaction;
import com.mycashbook.app.utils.CsvUtils;
import com.mycashbook.app.utils.ExcelUtils;
import com.mycashbook.app.viewmodel.BillingViewModel;
import com.mycashbook.app.viewmodel.TransactionViewModel;

import java.util.List;

public class ExportActivity extends AppCompatActivity {

    private long subBookId;

    private BillingViewModel billingViewModel;
    private TransactionViewModel transactionViewModel;

    private boolean isPremium = false;
    private boolean isBusiness = false;

    private List<Transaction> transactionList;

    private LinearLayout btnExportCsv, btnExportExcel, btnPreview;

    private ActivityResultLauncher<Intent> fileCreatorLauncher;

    private boolean exportAsExcel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        subBookId = getIntent().getLongExtra("subBookId", -1);
        if (subBookId == -1) {
            finish();
            return;
        }

        initToolbar();
        initViews();
        initViewModels();
        initFileCreatorLauncher();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_export);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Export");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initViews() {
        btnExportCsv = findViewById(R.id.buttonExportCSV);
        btnExportExcel = findViewById(R.id.buttonExportExcel);
        btnPreview = findViewById(R.id.buttonPreviewReport);

        btnExportCsv.setOnClickListener(v -> export(false));
        btnExportExcel.setOnClickListener(v -> export(true));
        btnPreview.setOnClickListener(v -> preview());
    }

    private void initViewModels() {
        billingViewModel = new ViewModelProvider(this).get(BillingViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        billingViewModel.loadSubscriptionState();

        billingViewModel.isPremium().observe(this, val -> isPremium = val);
        billingViewModel.isBusiness().observe(this, val -> isBusiness = val);

        transactionViewModel.getTransactions(subBookId).observe(this, list -> {
            transactionList = list;
        });
    }

    private void export(boolean excel) {

        if (!isPremium && !isBusiness) {
            Toast.makeText(this, "Upgrade to export", Toast.LENGTH_LONG).show();
            return;
        }

        if (excel && !isBusiness) {
            Toast.makeText(this, "Excel export is for Business plan only", Toast.LENGTH_LONG).show();
            return;
        }

        if (transactionList == null || transactionList.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        exportAsExcel = excel;

        // Open file creator
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (excel) {
            intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            intent.putExtra(Intent.EXTRA_TITLE, "transactions.xlsx");
        } else {
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_TITLE, "transactions.csv");
        }

        fileCreatorLauncher.launch(intent);
    }

    private void preview() {
        Intent i = new Intent(this, ReportPreviewActivity.class);
        i.putExtra("subBookId", subBookId);
        startActivity(i);
    }

    private void initFileCreatorLauncher() {

        fileCreatorLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() == Activity.RESULT_OK &&
                            result.getData() != null &&
                            result.getData().getData() != null) {

                        Uri uri = result.getData().getData();

                        try {
                            if (exportAsExcel) {
                                ExcelUtils.writeTransactionsToExcel(
                                        getContentResolver().openOutputStream(uri),
                                        transactionList
                                );
                                Toast.makeText(this, "Excel exported successfully", Toast.LENGTH_LONG).show();
                            } else {
                                CsvUtils.writeTransactionsToCsv(
                                        getContentResolver().openOutputStream(uri),
                                        transactionList
                                );
                                Toast.makeText(this, "CSV exported successfully", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Export failed", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
