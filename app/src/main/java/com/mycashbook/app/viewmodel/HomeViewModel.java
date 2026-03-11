package com.mycashbook.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.mycashbook.app.base.BaseViewModel;
import com.mycashbook.app.model.Book;
import com.mycashbook.app.repository.BookRepository;
import com.mycashbook.app.utils.ErrorHandlingUtils;
import com.mycashbook.app.utils.LogUtils;
import com.mycashbook.app.utils.AdvancedValidationUtils;

import java.util.Date;
import java.util.List;

/**
 * Home ViewModel - Refactored with comprehensive logging
 * Manages book operations with full error handling and validation
 */
public class HomeViewModel extends BaseViewModel {

    private static final String TAG = "HomeViewModel";

    private final BookRepository bookRepository;
    private final LiveData<List<com.mycashbook.app.model.BookWithBalance>> allBooksWithBalance;
    private final MutableLiveData<Integer> bookCount = new MutableLiveData<>(0);
    private final MutableLiveData<Long> refreshTrigger = new MutableLiveData<>(System.currentTimeMillis());
    private final MutableLiveData<Boolean> activityRestartEvent = new MutableLiveData<>(false);

    public HomeViewModel(@NonNull Application application) {
        super(application);
        LogUtils.methodEntry(TAG, "Constructor");

        // Initialize repository OUTSIDE try-catch to fix "might not have been
        // initialized" error
        bookRepository = new BookRepository(application);

        try {
            // Use switchMap to allow manual refreshing of the list
            allBooksWithBalance = Transformations.switchMap(refreshTrigger, timestamp -> {
                LogUtils.d(TAG, "Refreshing book list (trigger: " + timestamp + ")");
                return bookRepository.getAllBooksWithBalanceLive();
            });

            LogUtils.d(TAG, "Repository initialized");

            refreshBookCount();
        } catch (Exception e) {
            LogUtils.e(TAG, "Error initializing ViewModel", e);
            setErrorMessage("Failed to initialize app data");
            throw new RuntimeException("Failed to initialize HomeViewModel", e);
        }

        LogUtils.methodExit(TAG, "Constructor");
    }

    /**
     * Force refresh the book list
     */
    public void refreshBooks() {
        LogUtils.d(TAG, "Forcing book list refresh");
        refreshTrigger.postValue(System.currentTimeMillis());
        // Trigger activity restart for "instant" update feel
        activityRestartEvent.postValue(true);
        refreshBookCount();
    }

    public MutableLiveData<Boolean> getActivityRestartEvent() {
        return activityRestartEvent;
    }

    public void clearRestartEvent() {
        activityRestartEvent.setValue(false);
    }

    // ============================================================
    // GETTERS FOR LIVEDATA
    // ============================================================

    public LiveData<List<com.mycashbook.app.model.BookWithBalance>> getAllBooks() {
        return allBooksWithBalance;
    }

    public LiveData<Integer> getBookCount() {
        return bookCount;
    }

    // ============================================================
    // CREATE BOOK
    // ============================================================

    public void createBook(String name, String description, String currencyCode, String currencySymbol,
            String currencyName, boolean isFreePlan, boolean isLocked, String lockPin) {
        LogUtils.methodEntry(TAG, "createBook");
        LogUtils.d(TAG, "Creating book - Name: " + name + ", Currency: " + currencyCode + ", Locked: " + isLocked);

        // Validate input
        AdvancedValidationUtils.ValidationResult validation = AdvancedValidationUtils.validateBook(name, description);

        if (!validation.isValid) {
            LogUtils.w(TAG, "Book validation failed: " + validation.errorMessage);
            setErrorMessage(validation.errorMessage);
            return;
        }

        startLoading();

        try {
            Book book = new Book(
                    name.trim(),
                    description != null ? description.trim() : "",
                    new Date(),
                    new Date());

            // Set currency fields
            book.setCurrencyCode(currencyCode);
            book.setCurrencySymbol(currencySymbol);
            book.setCurrencyName(currencyName);

            // Set lock fields (Added in v5)
            if (isLocked && lockPin != null) {
                book.setLocked(true);
                book.setLockPin(lockPin);
                // Default useBiometric to true if locked, can be toggled in settings later
                book.setUseBiometric(true);
            }

            LogUtils.d(TAG, "Book object created, inserting to repository");

            bookRepository.insertBook(book, isFreePlan)
                    .thenAccept(bookId -> {
                        stopLoading();

                        if (bookId == -1L) {
                            LogUtils.w(TAG, "Book creation failed: FREE plan limit reached");
                            setErrorMessage("Book limit reached! Upgrade to Premium.");

                        } else if (bookId == -2L) {
                            LogUtils.e(TAG, "Book creation failed: Database error");
                            setErrorMessage("Failed to create book. Please try again.");

                        } else if (bookId > 0) {
                            LogUtils.i(TAG, "Book created successfully - ID: " + bookId + ", Name: " + name);
                            setSuccessMessage("Book created successfully!");
                            refreshBooks();

                        } else {
                            LogUtils.e(TAG, "Book creation failed: Unknown error (ID: " + bookId + ")");
                            setErrorMessage("Unexpected error occurred");
                        }
                    })
                    .exceptionally(ex -> {
                        stopLoading();
                        String friendlyMsg = ErrorHandlingUtils.getErrorMessage(ex);
                        LogUtils.e(TAG, "Exception creating book", ex);
                        setErrorMessage("Failed to create book: " + friendlyMsg);
                        return null;
                    });

        } catch (Exception e) {
            stopLoading();
            String friendlyMsg = ErrorHandlingUtils.getErrorMessage(e);
            LogUtils.e(TAG, "Error in createBook", e);
            setErrorMessage("Error: " + friendlyMsg);
        }

        LogUtils.methodExit(TAG, "createBook");
    }

    // ============================================================
    // UPDATE BOOK
    // ============================================================

    public void updateBook(Book book) {
        LogUtils.methodEntry(TAG, "updateBook");

        if (book == null) {
            LogUtils.w(TAG, "Book is null");
            setErrorMessage("Invalid book data");
            return;
        }

        LogUtils.d(TAG, "Updating book - ID: " + book.getId() + ", Name: " + book.getName());

        // Validate
        AdvancedValidationUtils.ValidationResult validation = AdvancedValidationUtils.validateBookName(book.getName());

        if (!validation.isValid) {
            LogUtils.w(TAG, "Book update validation failed: " + validation.errorMessage);
            setErrorMessage(validation.errorMessage);
            return;
        }

        startLoading();

        try {
            book.setUpdatedAt(new Date());

            bookRepository.updateBook(book)
                    .thenAccept(rowsUpdated -> {
                        stopLoading();

                        if (rowsUpdated > 0) {
                            LogUtils.i(TAG, "Book updated successfully - ID: " + book.getId());
                            setSuccessMessage("Book updated successfully!");
                            refreshBooks();
                        } else if (rowsUpdated == 0) {
                            LogUtils.w(TAG, "Book update failed: Not found (ID: " + book.getId() + ")");
                            setErrorMessage("Book not found");
                        } else {
                            LogUtils.e(TAG, "Book update failed: Database error");
                            setErrorMessage("Failed to update book");
                        }
                    })
                    .exceptionally(ex -> {
                        stopLoading();
                        String friendlyMsg = ErrorHandlingUtils.getErrorMessage(ex);
                        LogUtils.e(TAG, "Exception updating book", ex);
                        setErrorMessage("Error updating book: " + friendlyMsg);
                        return null;
                    });

        } catch (Exception e) {
            stopLoading();
            String friendlyMsg = ErrorHandlingUtils.getErrorMessage(e);
            LogUtils.e(TAG, "Error in updateBook", e);
            setErrorMessage("Error: " + friendlyMsg);
        }

        LogUtils.methodExit(TAG, "updateBook");
    }

    // ============================================================
    // DELETE BOOK
    // ============================================================

    public void deleteBook(Book book) {
        LogUtils.methodEntry(TAG, "deleteBook");

        if (book == null) {
            LogUtils.w(TAG, "Book is null for deletion");
            setErrorMessage("Invalid book data");
            return;
        }

        LogUtils.d(TAG, "Deleting book - ID: " + book.getId() + ", Name: " + book.getName());

        // Validate ID
        AdvancedValidationUtils.ValidationResult validation = AdvancedValidationUtils.validateId(book.getId(), "Book");

        if (!validation.isValid) {
            LogUtils.w(TAG, "Book delete validation failed: " + validation.errorMessage);
            setErrorMessage(validation.errorMessage);
            return;
        }

        startLoading();

        try {
            bookRepository.deleteBook(book)
                    .thenAccept(rowsDeleted -> {
                        stopLoading();

                        if (rowsDeleted > 0) {
                            LogUtils.i(TAG, "Book deleted successfully - ID: " + book.getId());
                            setSuccessMessage("Book deleted successfully!");
                            refreshBooks();
                        } else if (rowsDeleted == 0) {
                            LogUtils.w(TAG, "Book delete failed: Not found (ID: " + book.getId() + ")");
                            setErrorMessage("Book not found");
                        } else {
                            LogUtils.e(TAG, "Book delete failed: Database error");
                            setErrorMessage("Failed to delete book");
                        }
                    })
                    .exceptionally(ex -> {
                        stopLoading();
                        String friendlyMsg = ErrorHandlingUtils.getErrorMessage(ex);
                        LogUtils.e(TAG, "Exception deleting book", ex);
                        setErrorMessage("Error deleting book: " + friendlyMsg);
                        return null;
                    });

        } catch (Exception e) {
            stopLoading();
            String friendlyMsg = ErrorHandlingUtils.getErrorMessage(e);
            LogUtils.e(TAG, "Error in deleteBook", e);
            setErrorMessage("Error: " + friendlyMsg);
        }

        LogUtils.methodExit(TAG, "deleteBook");
    }

    public void deleteBookById(long bookId) {
        LogUtils.methodEntry(TAG, "deleteBookById");
        LogUtils.d(TAG, "Deleting book by ID: " + bookId);

        // Validate ID
        AdvancedValidationUtils.ValidationResult validation = AdvancedValidationUtils.validateId(bookId, "Book");

        if (!validation.isValid) {
            LogUtils.w(TAG, "Book ID validation failed: " + validation.errorMessage);
            setErrorMessage(validation.errorMessage);
            return;
        }

        startLoading();

        try {
            bookRepository.deleteBookById(bookId)
                    .thenAccept(rowsDeleted -> {
                        stopLoading();

                        if (rowsDeleted > 0) {
                            LogUtils.i(TAG, "Book deleted by ID: " + bookId);
                            setSuccessMessage("Book deleted successfully!");
                            refreshBooks();
                        } else if (rowsDeleted == 0) {
                            LogUtils.w(TAG, "Book delete failed: Not found (ID: " + bookId + ")");
                            setErrorMessage("Book not found");
                        } else {
                            LogUtils.e(TAG, "Book delete failed: Database error");
                            setErrorMessage("Failed to delete book");
                        }
                    })
                    .exceptionally(ex -> {
                        stopLoading();
                        String friendlyMsg = ErrorHandlingUtils.getErrorMessage(ex);
                        LogUtils.e(TAG, "Exception deleting book by ID", ex);
                        setErrorMessage("Error deleting book: " + friendlyMsg);
                        return null;
                    });

        } catch (Exception e) {
            stopLoading();
            String friendlyMsg = ErrorHandlingUtils.getErrorMessage(e);
            LogUtils.e(TAG, "Error in deleteBookById", e);
            setErrorMessage("Error: " + friendlyMsg);
        }

        LogUtils.methodExit(TAG, "deleteBookById");
    }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    public void refreshBookCount() {
        LogUtils.methodEntry(TAG, "refreshBookCount");

        try {
            bookRepository.getBookCount()
                    .thenAccept(count -> {
                        int bookCount = count != null ? count : 0;
                        this.bookCount.postValue(bookCount);
                        LogUtils.d(TAG, "Book count refreshed: " + bookCount);
                    })
                    .exceptionally(ex -> {
                        LogUtils.e(TAG, "Failed to refresh book count", ex);
                        return null;
                    });

        } catch (Exception e) {
            LogUtils.e(TAG, "Error in refreshBookCount", e);
        }

        LogUtils.methodExit(TAG, "refreshBookCount");
    }

    public void canAddBook(boolean isFreePlan, CanAddBookCallback callback) {
        LogUtils.methodEntry(TAG, "canAddBook");
        LogUtils.d(TAG, "Checking if can add book - FreePlan: " + isFreePlan);

        try {
            bookRepository.canAddBook(isFreePlan)
                    .thenAccept(canAdd -> {
                        if (callback != null) {
                            callback.onResult(canAdd);
                            LogUtils.d(TAG, "Can add book: " + canAdd);
                        }
                    })
                    .exceptionally(ex -> {
                        LogUtils.e(TAG, "Error checking if can add book", ex);
                        if (callback != null) {
                            callback.onResult(false);
                        }
                        return null;
                    });

        } catch (Exception e) {
            LogUtils.e(TAG, "Exception in canAddBook", e);
            if (callback != null) {
                callback.onResult(false);
            }
        }

        LogUtils.methodExit(TAG, "canAddBook");
    }

    public void isBookNameExists(String name, BookNameExistsCallback callback) {
        LogUtils.methodEntry(TAG, "isBookNameExists");

        if (name == null || name.trim().isEmpty()) {
            LogUtils.w(TAG, "Book name is empty");
            if (callback != null) {
                callback.onResult(false);
            }
            return;
        }

        LogUtils.d(TAG, "Checking if book name exists: " + name);

        try {
            bookRepository.isBookNameExists(name.trim())
                    .thenAccept(exists -> {
                        if (callback != null) {
                            callback.onResult(exists);
                            LogUtils.d(TAG, "Book name exists: " + exists);
                        }
                    })
                    .exceptionally(ex -> {
                        LogUtils.e(TAG, "Error checking book name existence", ex);
                        if (callback != null) {
                            callback.onResult(false);
                        }
                        return null;
                    });

        } catch (Exception e) {
            LogUtils.e(TAG, "Exception in isBookNameExists", e);
            if (callback != null) {
                callback.onResult(false);
            }
        }

        LogUtils.methodExit(TAG, "isBookNameExists");
    }

    public LiveData<Integer> getTransactionCountForBook(long bookId) {
        com.mycashbook.app.repository.TransactionRepository txnRepo = new com.mycashbook.app.repository.TransactionRepository(
                getApplication());
        return txnRepo.getTransactionCountForBook(bookId);
    }

    // ============================================================
    // CALLBACKS
    // ============================================================

    public interface CanAddBookCallback {
        void onResult(boolean canAdd);
    }

    public interface BookNameExistsCallback {
        void onResult(boolean exists);
    }

    // ============================================================
    // CLEANUP
    // ============================================================

    @Override
    protected void onCleared() {
        super.onCleared();
        try {
            if (bookRepository != null) {
                bookRepository.shutdown();
                LogUtils.d(TAG, "Repository shutdown");
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Error shutting down repository", e);
        }
        LogUtils.methodExit(TAG, "onCleared");
    }
}