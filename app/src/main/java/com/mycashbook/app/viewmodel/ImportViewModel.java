package com.mycashbook.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mycashbook.app.model.ImportMapping;
import com.mycashbook.app.model.Transaction;
import com.mycashbook.app.repository.ImportRepository;

import java.util.List;

public class ImportViewModel extends AndroidViewModel {

    private final ImportRepository repository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> importSuccess = new MutableLiveData<>(null);

    public ImportViewModel(@NonNull Application application) {
        super(application);
        repository = new ImportRepository(application);
    }

    // ---------------------- IMPORT TRANSACTIONS -----------------------

    public void importTransactions(long subBookId, List<Transaction> parsedList) {

        if (parsedList == null || parsedList.isEmpty()) {
            message.postValue("No valid transactions found");
            return;
        }

        loading.postValue(true);

        repository.importTransactions(subBookId, parsedList)
                .thenAccept(success -> {
                    loading.postValue(false);
                    importSuccess.postValue(success);
                })
                .exceptionally(ex -> {
                    loading.postValue(false);
                    message.postValue("Import failed: " + ex.getMessage());
                    importSuccess.postValue(false);
                    return null;
                });
    }

    // ---------------------- GETTERS ----------------------

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getImportSuccess() { return importSuccess; }
}
