package com.mycashbook.app.ui.importexport;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mycashbook.app.R;
import com.mycashbook.app.model.ImportMapping;
import com.mycashbook.app.model.Transaction;
import com.mycashbook.app.utils.CsvUtils;

import java.util.List;

public class ColumnMappingActivity extends AppCompatActivity {

    private Spinner spAmount, spType, spDate, spNote;

    private List<Transaction> parsedList;
    private long subBookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_column_mapping);

        initToolbar();
        initViews();
        initData();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_mapping);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Map Columns");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initViews() {
        spAmount = findViewById(R.id.spinnerAmount);
        spType = findViewById(R.id.spinnerType);
        spDate = findViewById(R.id.spinnerDate);
        spNote = findViewById(R.id.spinnerNote);

        findViewById(R.id.buttonContinueMapping).setOnClickListener(v -> {
            completeMapping();
        });
    }

    private void initData() {
        String json = getIntent().getStringExtra("data");
        parsedList = CsvUtils.fromJsonToList(json);
        subBookId = getIntent().getLongExtra("subBookId", -1);

        if (parsedList == null || parsedList.isEmpty()) {
            Toast.makeText(this, "Invalid data", Toast.LENGTH_SHORT).show();
            finish();
        }

        // A typical CSV/EXCEL has columns:
        // Amount, Type, Date, Note
        String[] cols = new String[]{"Amount", "Type", "Date", "Note"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                cols
        );

        spAmount.setAdapter(adapter);
        spType.setAdapter(adapter);
        spDate.setAdapter(adapter);
        spNote.setAdapter(adapter);

        // Auto-select most common
        spAmount.setSelection(0);
        spType.setSelection(1);
        spDate.setSelection(2);
        spNote.setSelection(3);
    }

    private void completeMapping() {
        // FIX: Use getSelectedItemPosition() to pass the index (int), not the name (String)
        ImportMapping mapping = new ImportMapping(
                spAmount.getSelectedItemPosition(),
                spType.getSelectedItemPosition(),
                spDate.getSelectedItemPosition(),
                spNote.getSelectedItemPosition()
        );

        Intent i = new Intent(this, ImportSummaryActivity.class);
        i.putExtra("mapping", CsvUtils.toJson(mapping));
        i.putExtra("data", CsvUtils.toJson(parsedList));
        i.putExtra("subBookId", subBookId);
        startActivity(i);
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
