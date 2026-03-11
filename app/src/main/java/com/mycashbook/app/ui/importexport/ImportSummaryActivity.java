package com.mycashbook.app.ui.importexport;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.mycashbook.app.R;
import com.mycashbook.app.model.Transaction;
import com.mycashbook.app.utils.CsvUtils;
import com.mycashbook.app.viewmodel.ImportViewModel;

import java.util.List;

public class ImportSummaryActivity extends AppCompatActivity {

    private long subBookId;
    private ImportMapping mapping;
    private List<Transaction> parsedList;

    private ImportViewModel importViewModel;

    private ListView listPreview;
    private MaterialButton btnImport;
    private TextView textCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_summary);

        initToolbar();
        initViews();
        initData();
        initViewModel();
        initListeners();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_summary);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Preview Import");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        listPreview = findViewById(R.id.listImportPreview);
        btnImport = findViewById(R.id.buttonImportNow);
        textCount = findViewById(R.id.textItemCount);
    }

    private void initData() {
        // Ensure CsvUtils imports the 'model' version of ImportMapping
        if (getIntent().hasExtra("mapping")) {
            mapping = CsvUtils.fromJsonToMapping(getIntent().getStringExtra("mapping"));
        }

        if (getIntent().hasExtra("data")) {
            parsedList = CsvUtils.fromJsonToList(getIntent().getStringExtra("data"));
        }

        subBookId = getIntent().getLongExtra("subBookId", -1);

        if (parsedList != null) {
            textCount.setText(parsedList.size() + " items ready to import");
            // Pass the list to our custom adapter
            SummaryPreviewAdapter adapter = new SummaryPreviewAdapter(this, parsedList);
            listPreview.setAdapter(adapter);
        } else {
            textCount.setText("0 items found");
        }
    }

    private void initViewModel() {
        importViewModel = new ViewModelProvider(this).get(ImportViewModel.class);
    }

    private void initListeners() {
        btnImport.setOnClickListener(v -> {
            if (parsedList != null && !parsedList.isEmpty() && subBookId != -1) {
                importViewModel.importTransactions(subBookId, parsedList);
            } else {
                Toast.makeText(this, "No data to import", Toast.LENGTH_SHORT).show();
            }
        });

        importViewModel.getImportSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Import completed successfully!", Toast.LENGTH_LONG).show();
                // Close this screen and go back to the book
                finish();
            }
        });

        importViewModel.getMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });
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
