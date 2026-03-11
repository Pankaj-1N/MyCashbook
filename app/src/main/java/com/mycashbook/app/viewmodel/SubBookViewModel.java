package com.mycashbook.app.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mycashbook.app.model.SubBook;
import com.mycashbook.app.repository.SubBookRepository;

import java.util.Date;
import java.util.List;

public class SubBookViewModel extends AndroidViewModel {

    private static final String TAG = "SubBookViewModel";

    private final SubBookRepository subBookRepository;
    private final long bookId;

    // LiveData for UI observation
    private final LiveData<List<com.mycashbook.app.model.SubBookWithStats>> allSubBooks;
    private final LiveData<com.mycashbook.app.model.Book> bookLiveData;
    private final LiveData<Double> totalBalance;
    private final LiveData<Double> totalIncome;
    private final LiveData<Double> totalExpense;
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<Integer> subBookCount = new MutableLiveData<>(0);
    private final LiveData<Integer> transactionCount;

    // Constructor
    public SubBookViewModel(@NonNull Application application, long bookId) {
        super(application);
        this.bookId = bookId;
        com.mycashbook.app.repository.BookRepository bookRepo = new com.mycashbook.app.repository.BookRepository(
                application);
        this.bookLiveData = bookRepo.getBookByIdLive(bookId);
        this.subBookRepository = new SubBookRepository(application);
        this.allSubBooks = subBookRepository.getSubBooksWithStatsByBookIdLive(bookId);
        this.totalBalance = subBookRepository.getTotalBalanceForBook(bookId);
        this.totalIncome = subBookRepository.getTotalIncomeForBook(bookId);
        this.totalExpense = subBookRepository.getTotalExpenseForBook(bookId);
        com.mycashbook.app.repository.TransactionRepository txnRepo = new com.mycashbook.app.repository.TransactionRepository(
                application);
        this.transactionCount = txnRepo.getTransactionCountForBook(bookId);
        refreshSubBookCount();
    }

    // ============================================================
    // GETTERS FOR LIVEDATA (UI Observation)
    // ============================================================

    public LiveData<com.mycashbook.app.model.Book> getBookLiveData() {
        return bookLiveData;
    }

    public LiveData<List<com.mycashbook.app.model.SubBookWithStats>> getAllSubBooks() {
        return allSubBooks;
    }

    public LiveData<Double> getTotalBalance() {
        return totalBalance;
    }

    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }

    public LiveData<Double> getTotalExpense() {
        return totalExpense;
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

    public LiveData<Integer> getSubBookCount() {
        return subBookCount;
    }

    public LiveData<Integer> getTransactionCount() {
        return transactionCount;
    }

    public long getBookId() {
        return bookId;
    }

    // ============================================================
    // INSERT SUB-BOOK
    // ============================================================

    /**
     * Create new sub-book
     *
     * @param name        SubBook name (required)
     * @param description SubBook description (optional)
     */
    public void createSubBook(String name, String description) {
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            errorMessage.postValue("Sub-book name cannot be empty");
            return;
        }

        loading.postValue(true);

        // Create sub-book object
        SubBook subBook = new SubBook(
                bookId,
                name.trim(),
                description != null ? description.trim() : "",
                new Date(),
                new Date());

        // Insert sub-book
        subBookRepository.insertSubBook(subBook)
                .thenAccept(subBookId -> {
                    loading.postValue(false);

                    if (subBookId == -2L) {
                        errorMessage.postValue("Failed to create sub-book. Please try again.");
                        Log.e(TAG, "SubBook creation failed: Database error");

                    } else if (subBookId > 0) {
                        successMessage.postValue("Sub-book created successfully!");
                        Log.d(TAG, "SubBook created: " + name + " (ID: " + subBookId + ")");
                        refreshSubBookCount();

                    } else {
                        errorMessage.postValue("Unexpected error occurred");
                        Log.e(TAG, "SubBook creation failed: Unknown error (ID: " + subBookId + ")");
                    }
                })
                .exceptionally(ex -> {
                    loading.postValue(false);
                    errorMessage.postValue("Failed to create sub-book: " + ex.getMessage());
                    Log.e(TAG, "Exception creating sub-book", ex);
                    return null;
                });
    }

    // ============================================================
    // UPDATE SUB-BOOK
    // ============================================================

    /**
     * Update existing sub-book
     *
     * @param subBook SubBook object with updated data (must have valid ID)
     */
    public void updateSubBook(SubBook subBook) {
        if (subBook == null) {
            errorMessage.postValue("Invalid sub-book data");
            return;
        }

        if (subBook.getName() == null || subBook.getName().trim().isEmpty()) {
            errorMessage.postValue("Sub-book name cannot be empty");
            return;
        }

        loading.postValue(true);

        // Update timestamp
        subBook.setUpdatedAt(new Date());

        subBookRepository.updateSubBook(subBook)
                .thenAccept(rowsUpdated -> {
                    loading.postValue(false);

                    if (rowsUpdated > 0) {
                        successMessage.postValue("Sub-book updated successfully!");
                        Log.d(TAG, "SubBook updated: " + subBook.getName());

                    } else if (rowsUpdated == 0) {
                        errorMessage.postValue("Sub-book not found");
                        Log.w(TAG, "SubBook update failed: Not found (ID: " + subBook.getId() + ")");

                    } else {
                        errorMessage.postValue("Failed to update sub-book");
                        Log.e(TAG, "SubBook update failed: Database error");
                    }
                })
                .exceptionally(ex -> {
                    loading.postValue(false);
                    errorMessage.postValue("Failed to update sub-book: " + ex.getMessage());
                    Log.e(TAG, "Exception updating sub-book", ex);
                    return null;
                });
    }

    // ============================================================
    // DELETE SUB-BOOK
    // ============================================================

    /**
     * Delete a sub-book
     *
     * @param subBook SubBook to delete
     */
    public void deleteSubBook(SubBook subBook) {
        if (subBook == null) {
            errorMessage.postValue("Invalid sub-book data");
            return;
        }

        loading.postValue(true);

        subBookRepository.deleteSubBook(subBook)
                .thenAccept(rowsDeleted -> {
                    loading.postValue(false);

                    if (rowsDeleted > 0) {
                        successMessage.postValue("Sub-book deleted successfully!");
                        Log.d(TAG, "SubBook deleted: " + subBook.getName());
                        refreshSubBookCount();

                    } else if (rowsDeleted == 0) {
                        errorMessage.postValue("Sub-book not found");
                        Log.w(TAG, "SubBook deletion failed: Not found (ID: " + subBook.getId() + ")");

                    } else {
                        errorMessage.postValue("Failed to delete sub-book");
                        Log.e(TAG, "SubBook deletion failed: Database error");
                    }
                })
                .exceptionally(ex -> {
                    loading.postValue(false);
                    errorMessage.postValue("Failed to delete sub-book: " + ex.getMessage());
                    Log.e(TAG, "Exception deleting sub-book", ex);
                    return null;
                });
    }

    /**
     * Delete sub-book by ID
     *
     * @param subBookId ID of sub-book to delete
     */
    public void deleteSubBookById(long subBookId) {
        if (subBookId <= 0) {
            errorMessage.postValue("Invalid sub-book ID");
            return;
        }

        loading.postValue(true);

        subBookRepository.deleteSubBookById(subBookId)
                .thenAccept(rowsDeleted -> {
                    loading.postValue(false);

                    if (rowsDeleted > 0) {
                        successMessage.postValue("Sub-book deleted successfully!");
                        Log.d(TAG, "SubBook deleted: ID " + subBookId);
                        refreshSubBookCount();

                    } else if (rowsDeleted == 0) {
                        errorMessage.postValue("Sub-book not found");
                        Log.w(TAG, "SubBook deletion failed: Not found (ID: " + subBookId + ")");

                    } else {
                        errorMessage.postValue("Failed to delete sub-book");
                        Log.e(TAG, "SubBook deletion failed: Database error");
                    }
                })
                .exceptionally(ex -> {
                    loading.postValue(false);
                    errorMessage.postValue("Failed to delete sub-book: " + ex.getMessage());
                    Log.e(TAG, "Exception deleting sub-book by ID", ex);
                    return null;
                });
    }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    /**
     * Refresh sub-book count
     */
    public void refreshSubBookCount() {
        subBookRepository.getSubBookCount(bookId)
                .thenAccept(count -> {
                    subBookCount.postValue(count != null ? count : 0);
                    Log.d(TAG, "SubBook count refreshed: " + count);
                })
                .exceptionally(ex -> {
                    Log.e(TAG, "Failed to refresh sub-book count", ex);
                    return null;
                });
    }

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
        subBookRepository.shutdown();
        Log.d(TAG, "SubBookViewModel cleared");
    }
}