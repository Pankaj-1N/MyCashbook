package com.mycashbook.app.ui.importexport;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mycashbook.app.R;
import com.mycashbook.app.model.Transaction;
import com.mycashbook.app.utils.CsvUtils;
import com.mycashbook.app.utils.ExcelUtils;
import com.google.android.material.button.MaterialButton;

import java.io.InputStream;
import java.util.List;

public class ImportActivity extends AppCompatActivity {

    private MaterialButton buttonSelectFile, buttonNext;
    private TextView textFileName;

    private Uri selectedFileUri = null;
    private List<Transaction> parsedData;

    private long subBookId;

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null && result.getData().getData() != null) {
                        selectedFileUri = result.getData().getData();
                        handleFileSelected();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        subBookId = getIntent().getLongExtra("subBookId", -1);
        if (subBookId == -1) {
            Toast.makeText(this, "Invalid SubBook", Toast.LENGTH_SHORT).show();
            finish();
        }

        initViews();
        initToolbar();
        initListeners();
    }

    private void initViews() {
        buttonSelectFile = findViewById(R.id.buttonSelectFile);
        buttonNext = findViewById(R.id.buttonNextImport);
        textFileName = findViewById(R.id.textSelectedFile);

        buttonNext.setEnabled(false);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_import);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Import Transactions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initListeners() {

        buttonSelectFile.setOnClickListener(v -> openFilePicker());

        buttonNext.setOnClickListener(v -> {
            Intent i = new Intent(this, ColumnMappingActivity.class);
            i.putExtra("subBookId", subBookId);
            i.putExtra("data", CsvUtils.toJson(parsedData));
            startActivity(i);
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "text/csv",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        });
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(intent);
    }

    private void handleFileSelected() {
        if (selectedFileUri == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            return;
        }

        textFileName.setText(getFileName(selectedFileUri));

        try {
            InputStream is = getContentResolver().openInputStream(selectedFileUri);

            String name = getFileName(selectedFileUri).toLowerCase();

            if (name.endsWith(".csv")) {
                parsedData = CsvUtils.readTransactionsFromCsv(is);
            } else if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
                parsedData = ExcelUtils.readTransactionsFromExcel(is);
            } else {
                Toast.makeText(this, "Unsupported file format", Toast.LENGTH_SHORT).show();
                return;
            }

            if (parsedData == null || parsedData.isEmpty()) {
                Toast.makeText(this, "No valid data found.", Toast.LENGTH_SHORT).show();
                return;
            }

            buttonNext.setEnabled(true);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading file", Toast.LENGTH_LONG).show();
        }
    }

    private String getFileName(Uri uri) {
        String name = "";
        Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null);
        if (cursor != null) {
            try {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                cursor.moveToFirst();
                name = cursor.getString(nameIndex);
            } finally {
                cursor.close();
            }
        }
        return name;
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
