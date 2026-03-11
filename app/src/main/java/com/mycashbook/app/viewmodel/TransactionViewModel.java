package com.mycashbook.app.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mycashbook.app.model.Transaction;
import com.mycashbook.app.repository.TransactionRepository;

import java.util.Date;
import java.util.List;

public class TransactionViewModel extends AndroidViewModel {

    private static final String TAG = "TransactionViewModel";

    private final TransactionRepository transactionRepository;
    private final long subBookId;

    // LiveData for UI observation
    private LiveData<List<Transaction>> transactions;
    private LiveData<Double> totalDebit;
    private LiveData<Double> totalCredit;
    private final LiveData<com.mycashbook.app.model.Book> bookLiveData;
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    // Constructor
    public TransactionViewModel(@NonNull Application application, long subBookId) {
        super(application);
        this.subBookId = subBookId;
        this.transactionRepository = new TransactionRepository(application);
        this.transactions = transactionRepository.getTransactions(subBookId);
        this.totalDebit = transactionRepository.getTotalDebit(subBookId);
        this.totalCredit = transactionRepository.getTotalCredit(subBookId);

        com.mycashbook.app.repository.SubBookRepository subBookRepo = new com.mycashbook.app.repository.SubBookRepository(
                application);
        this.bookLiveData = subBookRepo.getBookBySubBookIdLive(subBookId);

        Log.d(TAG, "TransactionViewModel initialized with subBookId: " + subBookId);
    }

    // ============================================================
    // GETTERS FOR LIVEDATA (UI Observation)
    // ============================================================

    public LiveData<com.mycashbook.app.model.Book> getBookLiveData() {
        return bookLiveData;
    }

    public LiveData<List<Transaction>> getTransactions(long subBookId) {
        return transactions;
    }

    public LiveData<Double> getTotalDebit(long subBookId) {
        return totalDebit;
    }

    public LiveData<Double> getTotalCredit(long subBookId) {
        return totalCredit;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public long getSubBookId() {
        return subBookId;
    }

    // ============================================================
    // INSERT TRANSACTION
    // ============================================================

    /**
     * Insert new transaction
     *
     * @param transaction Transaction object to insert
     */
    public void insertTransaction(Transaction transaction) {
        if (transaction == null) {
            errorMessage.postValue("Invalid transaction data");
            Log.e(TAG, "Cannot insert: transaction is null");
            return;
        }

        if (transaction.getAmount() <= 0) {
            errorMessage.postValue("Amount must be greater than 0");
            Log.e(TAG, "Cannot insert: invalid amount");
            return;
        }

        if (transaction.getType() == null || transaction.getType().isEmpty()) {
            errorMessage.postValue("Transaction type is required");
            Log.e(TAG, "Cannot insert: type is null");
            return;
        }

        loading.postValue(true);

        // Set timestamps if not set
        if (transaction.getCreatedAt() == null) {
            transaction.setCreatedAt(new Date());
        }
        if (transaction.getUpdatedAt() == null) {
            transaction.setUpdatedAt(new Date());
        }

        // Ensure subBookId matches
        transaction.setSubBookId(subBookId);

        transactionRepository.insertTransaction(transaction)
                .thenAccept(transactionId -> {
                    loading.postValue(false);

                    if (transactionId > 0) {
                        successMessage.postValue("Transaction added successfully!");
                        Log.d(TAG,
                                "Transaction inserted: ID " + transactionId + ", Amount: " + transaction.getAmount());
                    } else {
                        errorMessage.postValue("Failed to add transaction");
                        Log.e(TAG, "Transaction insertion failed with ID: " + transactionId);
                    }
                })
                .exceptionally(ex -> {
                    loading.postValue(false);
                    errorMessage.postValue("Error adding transaction: " + ex.getMessage());
                    Log.e(TAG, "Exception inserting transaction", ex);
                    return null;
                });
    }

    /**
     * Insert transaction with parameters (shorthand method)
     *
     * @param type   Transaction type (DEBIT or CREDIT)
     * @param amount Transaction amount
     * @param note   Transaction note
     * @param date   Transaction date
     */
    public void insertTransaction(String type, double amount, String note, Date date) {
        Transaction transaction = new Transaction();
        transaction.setSubBookId(subBookId);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setNote(note);
        transaction.setDate(date);
        transaction.setCreatedAt(new Date());
        transaction.setUpdatedAt(new Date());

        insertTransaction(transaction);
    }

    // ============================================================
    // UPDATE TRANSACTION
    // ============================================================

    /**
     * Update existing transaction
     *
     * @param transaction Transaction object with updated data (must have valid ID)
     */
    public void updateTransaction(Transaction transaction) {
        if (transaction == null) {
            errorMessage.postValue("Invalid transaction data");
            return;
        }

        if (transaction.getId() <= 0) {
            errorMessage.postValue("Transaction not found");
            return;
        }

        if (transaction.getAmount() <= 0) {
            errorMessage.postValue("Amount must be greater than 0");
            return;
        }

        loading.postValue(true);

        // Update timestamp
        transaction.setUpdatedAt(new Date());

        transactionRepository.updateTransaction(transaction)
                .thenAccept(rowsUpdated -> {
                    loading.postValue(false);

                    if (rowsUpdated > 0) {
                        successMessage.postValue("Transaction updated successfully!");
                        Log.d(TAG, "Transaction updated: ID " + transaction.getId());
                    } else if (rowsUpdated == 0) {
                        errorMessage.postValue("Transaction not found");
                        Log.w(TAG, "Transaction update failed: Not found (ID: " + transaction.getId() + ")");
                    } else {
                        errorMessage.postValue("Failed to update transaction");
                        Log.e(TAG, "Transaction update failed with error code: " + rowsUpdated);
                    }
                })
                .exceptionally(ex -> {
                    loading.postValue(false);
                    errorMessage.postValue("Error updating transaction: " + ex.getMessage());
                    Log.e(TAG, "Exception updating transaction", ex);
                    return null;
                });
    }

    // ============================================================
    // DELETE TRANSACTION
    // ============================================================

    /**
     * Delete a transaction
     *
     * @param transaction Transaction to delete
     */
    public void deleteTransaction(Transaction transaction) {
        if (transaction == null) {
            errorMessage.postValue("Invalid transaction data");
            return;
        }

        if (transaction.getId() <= 0) {
            errorMessage.postValue("Transaction not found");
            return;
        }

        loading.postValue(true);

        transactionRepository.deleteTransaction(transaction)
                .thenAccept(rowsDeleted -> {
                    loading.postValue(false);

                    if (rowsDeleted > 0) {
                        successMessage.postValue("Transaction deleted successfully!");
                        Log.d(TAG, "Transaction deleted: ID " + transaction.getId());
                    } else if (rowsDeleted == 0) {
                        errorMessage.postValue("Transaction not found");
                        Log.w(TAG, "Transaction delete failed: Not found (ID: " + transaction.getId() + ")");
                    } else {
                        errorMessage.postValue("Failed to delete transaction");
                        Log.e(TAG, "Transaction delete failed with error code: " + rowsDeleted);
                    }
                })
                .exceptionally(ex -> {
                    loading.postValue(false);
                    errorMessage.postValue("Error deleting transaction: " + ex.getMessage());
                    Log.e(TAG, "Exception deleting transaction", ex);
                    return null;
                });
    }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    /**
     * Clear error message
     */
    public void clearErrorMessage() {
        errorMessage.postValue(null);
    }

    /**
     * Clear success message
     */
    public void clearSuccessMessage() {
        successMessage.postValue(null);
    }

    // ============================================================
    // CLEANUP
    // ============================================================

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "TransactionViewModel cleared");
    }
}