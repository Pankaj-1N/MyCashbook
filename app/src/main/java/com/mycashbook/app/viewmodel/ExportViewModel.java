package com.mycashbook.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mycashbook.app.model.Transaction;
import com.mycashbook.app.repository.TransactionRepository;
import com.mycashbook.app.utils.CsvUtils;
import com.mycashbook.app.utils.ExcelUtils;

import java.io.File;
import java.util.List;

public class ExportViewModel extends AndroidViewModel {

    private final TransactionRepository repository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>(null);
    private final MutableLiveData<File> exportFile = new MutableLiveData<>(null);

    public ExportViewModel(@NonNull Application application) {
        super(application);
        repository = new TransactionRepository(application);
    }

    // ---------------------- EXPORT CSV ----------------------

    public void exportToCsv(File file, List<Transaction> list) {

        if (file == null) {
            message.postValue("Invalid export file");
            return;
        }

        loading.postValue(true);

        try {
            CsvUtils.exportTransactions(file, list);
            loading.postValue(false);
            exportFile.postValue(file);

        } catch (Exception ex) {
            loading.postValue(false);
            message.postValue("CSV Export failed: " + ex.getMessage());
        }
    }

    // ---------------------- EXPORT EXCEL ----------------------

    public void exportToExcel(File file, List<Transaction> list) {

        if (file == null) {
            message.postValue("Invalid export file");
            return;
        }

        loading.postValue(true);

        try {
            ExcelUtils.exportTransactions(file, list);
            loading.postValue(false);
            exportFile.postValue(file);

        } catch (Exception ex) {
            loading.postValue(false);
            message.postValue("Excel Export failed: " + ex.getMessage());
        }
    }

    // ---------------------- GETTERS ----------------------

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<File> getExportFile() { return exportFile; }
}
