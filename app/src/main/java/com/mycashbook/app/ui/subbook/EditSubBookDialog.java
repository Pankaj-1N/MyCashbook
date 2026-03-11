package com.mycashbook.app.ui.subbook;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.button.MaterialButton;
import com.mycashbook.app.R;
import com.mycashbook.app.model.SubBook;
import com.mycashbook.app.viewmodel.SubBookViewModel;

public class EditSubBookDialog extends Dialog {

    private static final String TAG = "EditSubBookDialog";

    private final SubBookViewModel subBookViewModel;
    private final LifecycleOwner lifecycleOwner;
    private final SubBook subBookToEdit;

    private TextView textTitle;
    private EditText editSubBookName;
    private EditText editSubBookDescription;
    private Button buttonCancel;
    private MaterialButton buttonSave;
    private ImageButton btnClose;

    // Constructor
    public EditSubBookDialog(@NonNull Context context, SubBookViewModel subBookViewModel, SubBook subBookToEdit) {
        super(context);
        this.subBookViewModel = subBookViewModel;
        this.lifecycleOwner = com.mycashbook.app.utils.ContextUtils.getLifecycleOwner(context);
        this.subBookToEdit = subBookToEdit;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_book_figma); // Reusing Figma layout

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        initViews();
        setupInitialData();
        setupListeners();
        observeViewModel();

        Log.d(TAG, "EditSubBookDialog created for ID: " + subBookToEdit.getId());
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
        editSubBookName = findViewById(R.id.editBookName); // Reusing ID
        editSubBookDescription = findViewById(R.id.editBookDescription); // Reusing ID
        buttonCancel = findViewById(R.id.btnCancel);
        buttonSave = findViewById(R.id.btnSave);
        btnClose = findViewById(R.id.btnClose);

        // Customize for Edit Mode
        textTitle.setText("Edit Sub-Book");
        editSubBookName.setHint("Sub-Book Name");
        buttonSave.setText("Update Sub-Book");
    }

    private void setupInitialData() {
        if (subBookToEdit != null) {
            editSubBookName.setText(subBookToEdit.getName());
            editSubBookDescription.setText(subBookToEdit.getDescription());
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

        // Update button
        buttonSave.setOnClickListener(v -> {
            String name = editSubBookName.getText().toString().trim();
            String description = editSubBookDescription.getText().toString().trim();

            if (name.isEmpty()) {
                editSubBookName.setError("Sub-book name is required");
                editSubBookName.requestFocus();
                return;
            }

            // Disable button
            buttonSave.setEnabled(false);

            Log.d(TAG, "Updating sub-book: " + name);

            // Update local object
            subBookToEdit.setName(name);
            subBookToEdit.setDescription(description);
            // UpdatedAt handled in ViewModel

            // Call ViewModel
            subBookViewModel.updateSubBook(subBookToEdit);
        });

        // Text watcher
        editSubBookName.addTextChangedListener(new TextWatcher() {
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

    // ============================================================
    // VIEWMODEL OBSERVATION
    // ============================================================

    private void observeViewModel() {
        // Observe success messages
        subBookViewModel.getSuccessMessage().observe(lifecycleOwner, successMsg -> {
            if (successMsg != null && !successMsg.isEmpty()) {
                if (successMsg.contains("updated")) {
                    Toast.makeText(getContext(), successMsg, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Success: " + successMsg);

                    // Clear message and dismiss dialog
                    subBookViewModel.clearSuccessMessage();
                    dismiss();
                }
            }
        });

        // Observe error messages
        subBookViewModel.getErrorMessage().observe(lifecycleOwner, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error: " + errorMsg);

                // Re-enable save button
                buttonSave.setEnabled(true);

                // Clear error message
                subBookViewModel.clearErrorMessage();
            }
        });

        // Observe loading state
        subBookViewModel.getLoading().observe(lifecycleOwner, isLoading -> {
            if (isLoading != null) {
                buttonSave.setEnabled(!isLoading);
                buttonCancel.setEnabled(!isLoading);
                editSubBookName.setEnabled(!isLoading);
                editSubBookDescription.setEnabled(!isLoading);
                btnClose.setEnabled(!isLoading);

                Log.d(TAG, "Loading: " + isLoading);
            }
        });
    }

    @Override
    public void dismiss() {
        // Clear observers to prevent memory leaks
        subBookViewModel.getSuccessMessage().removeObservers(lifecycleOwner);
        subBookViewModel.getErrorMessage().removeObservers(lifecycleOwner);
        subBookViewModel.getLoading().removeObservers(lifecycleOwner);

        super.dismiss();
        Log.d(TAG, "Dialog dismissed");
    }
}
