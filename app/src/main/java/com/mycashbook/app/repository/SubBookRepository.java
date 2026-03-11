package com.mycashbook.app.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.mycashbook.app.database.AppDatabase;
import com.mycashbook.app.database.dao.SubBookDao;
import com.mycashbook.app.model.SubBook;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubBookRepository {

    private static final String TAG = "SubBookRepository";
    private final SubBookDao subBookDao;
    private final ExecutorService executorService;

    // Constructor
    public SubBookRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.subBookDao = db.subBookDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // ============================================================
    // GET OPERATIONS
    // ============================================================

    /**
     * Get all sub-books for a book as LiveData (auto-updates UI)
     */
    public LiveData<List<SubBook>> getSubBooksByBookIdLive(long bookId) {
        return subBookDao.getSubBooksByBookIdLive(bookId);
    }

    /**
     * Get all sub-books for a book with STATS as LiveData
     */
    public LiveData<List<com.mycashbook.app.model.SubBookWithStats>> getSubBooksWithStatsByBookIdLive(long bookId) {
        return subBookDao.getSubBooksWithStatsByBookIdLive(bookId);
    }

    /**
     * Get total balance for a book (LiveData)
     */
    public LiveData<Double> getTotalBalanceForBook(long bookId) {
        return subBookDao.getTotalBalanceForBook(bookId);
    }

    /**
     * Get total Income for a book (LiveData)
     */
    public LiveData<Double> getTotalIncomeForBook(long bookId) {
        return subBookDao.getTotalIncomeForBook(bookId);
    }

    /**
     * Get total Expense for a book (LiveData)
     */
    public LiveData<Double> getTotalExpenseForBook(long bookId) {
        return subBookDao.getTotalExpenseForBook(bookId);
    }

    /**
     * Get the parent Book for a specific sub-book (LiveData)
     */
    public LiveData<com.mycashbook.app.model.Book> getBookBySubBookIdLive(long subBookId) {
        return subBookDao.getBookBySubBookIdLive(subBookId);
    }

    public CompletableFuture<List<SubBook>> getSubBooksByBookId(long bookId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return subBookDao.getSubBooksByBookId(bookId);
            } catch (Exception e) {
                Log.e(TAG, "Error getting sub-books for book " + bookId, e);
                return null;
            }
        }, executorService);
    }

    /**
     * Get sub-book by ID (async)
     */
    public CompletableFuture<SubBook> getSubBookById(long subBookId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                SubBook subBook = subBookDao.getSubBookById(subBookId);
                if (subBook == null) {
                    Log.w(TAG, "SubBook not found with ID: " + subBookId);
                }
                return subBook;
            } catch (Exception e) {
                Log.e(TAG, "Error getting sub-book by ID: " + subBookId, e);
                return null;
            }
        }, executorService);
    }

    /**
     * Get sub-book count for a book (async)
     */
    public CompletableFuture<Integer> getSubBookCount(long bookId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return subBookDao.getSubBookCount(bookId);
            } catch (Exception e) {
                Log.e(TAG, "Error getting sub-book count for book " + bookId, e);
                return 0;
            }
        }, executorService);
    }

    // ============================================================
    // INSERT OPERATIONS
    // ============================================================

    /**
     * Insert new sub-book
     *
     * @param subBook SubBook to insert
     * @return SubBook ID if success, -2 if error
     */
    public CompletableFuture<Long> insertSubBook(SubBook subBook) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validate sub-book
                if (subBook.getName() == null || subBook.getName().trim().isEmpty()) {
                    Log.e(TAG, "Cannot insert sub-book: name is empty");
                    return -2L;
                }

                if (subBook.getBookId() <= 0) {
                    Log.e(TAG, "Cannot insert sub-book: invalid bookId");
                    return -2L;
                }

                // Set timestamps if not set
                if (subBook.getCreatedAt() == null) {
                    subBook.setCreatedAt(new Date());
                }
                if (subBook.getUpdatedAt() == null) {
                    subBook.setUpdatedAt(new Date());
                }

                // Insert sub-book
                long subBookId = subBookDao.insertSubBook(subBook);
                Log.d(TAG, "SubBook inserted successfully with ID: " + subBookId);
                return subBookId;

            } catch (Exception e) {
                Log.e(TAG, "Error inserting sub-book", e);
                return -2L;
            }
        }, executorService);
    }

    /**
     * Insert multiple sub-books (for restore)
     */
    public CompletableFuture<Boolean> insertAllSubBooks(List<SubBook> subBooks) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (subBooks == null || subBooks.isEmpty()) {
                    Log.w(TAG, "Cannot insert: sub-book list is empty");
                    return false;
                }

                subBookDao.insertAll(subBooks);
                Log.d(TAG, "Inserted " + subBooks.size() + " sub-books successfully");
                return true;

            } catch (Exception e) {
                Log.e(TAG, "Error inserting multiple sub-books", e);
                return false;
            }
        }, executorService);
    }

    // ============================================================
    // UPDATE OPERATIONS
    // ============================================================

    /**
     * Update existing sub-book
     *
     * @param subBook SubBook to update (must have valid ID)
     * @return number of rows updated (1 if success, 0 if not found, -1 if error)
     */
    public CompletableFuture<Integer> updateSubBook(SubBook subBook) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validate sub-book
                if (subBook == null || subBook.getId() <= 0) {
                    Log.e(TAG, "Cannot update: invalid sub-book or ID");
                    return -1;
                }

                if (subBook.getName() == null || subBook.getName().trim().isEmpty()) {
                    Log.e(TAG, "Cannot update: sub-book name is empty");
                    return -1;
                }

                // Update timestamp
                subBook.setUpdatedAt(new Date());

                // Update sub-book
                int rowsUpdated = subBookDao.updateSubBook(subBook);
                if (rowsUpdated > 0) {
                    Log.d(TAG, "SubBook updated successfully: " + subBook.getName());
                } else {
                    Log.w(TAG, "SubBook not found for update: ID " + subBook.getId());
                }
                return rowsUpdated;

            } catch (Exception e) {
                Log.e(TAG, "Error updating sub-book", e);
                return -1;
            }
        }, executorService);
    }

    // ============================================================
    // DELETE OPERATIONS
    // ============================================================

    /**
     * Delete a sub-book
     *
     * @param subBook SubBook to delete
     * @return number of rows deleted (1 if success, 0 if not found, -1 if error)
     */
    public CompletableFuture<Integer> deleteSubBook(SubBook subBook) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (subBook == null || subBook.getId() <= 0) {
                    Log.e(TAG, "Cannot delete: invalid sub-book or ID");
                    return -1;
                }

                int rowsDeleted = subBookDao.deleteSubBook(subBook);
                if (rowsDeleted > 0) {
                    Log.d(TAG, "SubBook deleted successfully: " + subBook.getName());
                } else {
                    Log.w(TAG, "SubBook not found for deletion: ID " + subBook.getId());
                }
                return rowsDeleted;

            } catch (Exception e) {
                Log.e(TAG, "Error deleting sub-book", e);
                return -1;
            }
        }, executorService);
    }

    /**
     * Delete sub-book by ID
     */
    public CompletableFuture<Integer> deleteSubBookById(long subBookId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                SubBook subBook = subBookDao.getSubBookById(subBookId);
                if (subBook == null) {
                    Log.w(TAG, "SubBook not found for deletion: ID " + subBookId);
                    return 0;
                }
                return subBookDao.deleteSubBook(subBook);

            } catch (Exception e) {
                Log.e(TAG, "Error deleting sub-book by ID", e);
                return -1;
            }
        }, executorService);
    }

    /**
     * Delete all sub-books for a specific book
     */
    public CompletableFuture<Boolean> deleteSubBooksByBookId(long bookId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                subBookDao.deleteSubBooksByBookId(bookId);
                Log.d(TAG, "All sub-books deleted for book: " + bookId);
                return true;

            } catch (Exception e) {
                Log.e(TAG, "Error deleting sub-books by book ID", e);
                return false;
            }
        }, executorService);
    }

    /**
     * Delete all sub-books (USE WITH CAUTION!)
     */
    public CompletableFuture<Boolean> deleteAllSubBooks() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                subBookDao.deleteAllSubBooks();
                Log.d(TAG, "All sub-books deleted successfully");
                return true;

            } catch (Exception e) {
                Log.e(TAG, "Error deleting all sub-books", e);
                return false;
            }
        }, executorService);
    }

    // ============================================================
    // CLEANUP
    // ============================================================

    /**
     * Shutdown executor service
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            Log.d(TAG, "ExecutorService shutdown");
        }
    }
}