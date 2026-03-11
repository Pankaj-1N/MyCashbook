package com.mycashbook.app.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.mycashbook.app.database.AppDatabase;
import com.mycashbook.app.database.dao.BookDao;
import com.mycashbook.app.model.Book;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookRepository {

    private static final String TAG = "BookRepository";
    private final BookDao bookDao;
    private final ExecutorService executorService;

    // Constructor
    public BookRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.bookDao = db.bookDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // ============================================================
    // GET OPERATIONS
    // ============================================================

    /**
     * Get all books as LiveData (auto-updates UI)
     * Use this in Activities/Fragments with observe()
     */
    public LiveData<List<Book>> getAllBooksLive() {
        return bookDao.getAllBooksLive();
    }

    /**
     * Get all books with balance as LiveData
     */
    public LiveData<List<com.mycashbook.app.model.BookWithBalance>> getAllBooksWithBalanceLive() {
        return bookDao.getAllBooksWithBalanceLive();
    }

    /**
     * Get all books as List (async)
     * Use this when you need the list once (e.g., for export)
     */
    public CompletableFuture<List<Book>> getAllBooks() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return bookDao.getAllBooks();
            } catch (Exception e) {
                Log.e(TAG, "Error getting all books", e);
                return null;
            }
        }, executorService);
    }

    /**
     * Get all books synchronously (for backup)
     * WARNING: Must be called from background thread
     */
    public List<Book> getAllBooksSync() {
        try {
            return bookDao.getAllBooksSync();
        } catch (Exception e) {
            Log.e(TAG, "Error getting all books sync", e);
            return null;
        }
    }

    /**
     * Get book by ID as LiveData
     */
    public LiveData<Book> getBookByIdLive(long bookId) {
        return bookDao.getBookByIdLive(bookId);
    }

    /**
     * Get book by ID (async)
     * Returns null if not found
     */
    public CompletableFuture<Book> getBookById(long bookId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Book book = bookDao.getBookById(bookId);
                if (book == null) {
                    Log.w(TAG, "Book not found with ID: " + bookId);
                }
                return book;
            } catch (Exception e) {
                Log.e(TAG, "Error getting book by ID: " + bookId, e);
                return null;
            }
        }, executorService);
    }

    /**
     * Get book count (async)
     * Used for checking FREE plan limit (5 books max)
     */
    public CompletableFuture<Integer> getBookCount() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return bookDao.getBookCount();
            } catch (Exception e) {
                Log.e(TAG, "Error getting book count", e);
                return 0;
            }
        }, executorService);
    }

    // ============================================================
    // INSERT OPERATIONS
    // ============================================================

    /**
     * Insert new book with FREE plan limit check
     *
     * @param book       Book to insert
     * @param isFreePlan true if user is on FREE plan
     * @return Book ID if success, -1 if limit reached, -2 if error
     */
    public CompletableFuture<Long> insertBook(Book book, boolean isFreePlan) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validate book name
                if (book.getName() == null || book.getName().trim().isEmpty()) {
                    Log.e(TAG, "Cannot insert book: name is empty");
                    return -2L;
                }

                // Check FREE plan limit (5 books max)
                if (isFreePlan) {
                    int count = bookDao.getBookCount();
                    if (count >= 5) {
                        Log.w(TAG, "Book limit reached for FREE plan: " + count + "/5");
                        return -1L; // Limit reached
                    }
                }

                // Set timestamps if not set
                if (book.getCreatedAt() == null) {
                    book.setCreatedAt(new Date());
                }
                if (book.getUpdatedAt() == null) {
                    book.setUpdatedAt(new Date());
                }

                // Insert book
                long bookId = bookDao.insertBook(book);
                Log.d(TAG, "Book inserted successfully with ID: " + bookId);
                return bookId;

            } catch (Exception e) {
                Log.e(TAG, "Error inserting book", e);
                return -2L; // Error
            }
        }, executorService);
    }

    /**
     * Insert book without plan check (for PREMIUM/BUSINESS users or restore)
     *
     * @param book Book to insert
     * @return Book ID if success, -2 if error
     */
    public CompletableFuture<Long> insertBookNoPlanCheck(Book book) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validate book name
                if (book.getName() == null || book.getName().trim().isEmpty()) {
                    Log.e(TAG, "Cannot insert book: name is empty");
                    return -2L;
                }

                // Set timestamps if not set
                if (book.getCreatedAt() == null) {
                    book.setCreatedAt(new Date());
                }
                if (book.getUpdatedAt() == null) {
                    book.setUpdatedAt(new Date());
                }

                // Insert book
                long bookId = bookDao.insertBook(book);
                Log.d(TAG, "Book inserted (no plan check) with ID: " + bookId);
                return bookId;

            } catch (Exception e) {
                Log.e(TAG, "Error inserting book (no plan check)", e);
                return -2L;
            }
        }, executorService);
    }

    /**
     * Insert multiple books (for restore from backup)
     *
     * @param books List of books to insert
     * @return true if success, false if error
     */
    public CompletableFuture<Boolean> insertAllBooks(List<Book> books) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (books == null || books.isEmpty()) {
                    Log.w(TAG, "Cannot insert: book list is empty");
                    return false;
                }

                bookDao.insertAll(books);
                Log.d(TAG, "Inserted " + books.size() + " books successfully");
                return true;

            } catch (Exception e) {
                Log.e(TAG, "Error inserting multiple books", e);
                return false;
            }
        }, executorService);
    }

    // ============================================================
    // UPDATE OPERATIONS
    // ============================================================

    /**
     * Update existing book
     *
     * @param book Book to update (must have valid ID)
     * @return number of rows updated (1 if success, 0 if not found, -1 if error)
     */
    public CompletableFuture<Integer> updateBook(Book book) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validate book
                if (book == null || book.getId() <= 0) {
                    Log.e(TAG, "Cannot update: invalid book or ID");
                    return -1;
                }

                if (book.getName() == null || book.getName().trim().isEmpty()) {
                    Log.e(TAG, "Cannot update: book name is empty");
                    return -1;
                }

                // Update timestamp
                book.setUpdatedAt(new Date());

                // Update book
                int rowsUpdated = bookDao.updateBook(book);
                if (rowsUpdated > 0) {
                    Log.d(TAG, "Book updated successfully: " + book.getName());
                } else {
                    Log.w(TAG, "Book not found for update: ID " + book.getId());
                }
                return rowsUpdated;

            } catch (Exception e) {
                Log.e(TAG, "Error updating book", e);
                return -1;
            }
        }, executorService);
    }

    // ============================================================
    // DELETE OPERATIONS
    // ============================================================

    /**
     * Delete a book (will cascade delete SubBooks & Transactions if DB configured)
     *
     * @param book Book to delete
     * @return number of rows deleted (1 if success, 0 if not found, -1 if error)
     */
    public CompletableFuture<Integer> deleteBook(Book book) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (book == null || book.getId() <= 0) {
                    Log.e(TAG, "Cannot delete: invalid book or ID");
                    return -1;
                }

                int rowsDeleted = bookDao.deleteBook(book);
                if (rowsDeleted > 0) {
                    Log.d(TAG, "Book deleted successfully: " + book.getName());
                } else {
                    Log.w(TAG, "Book not found for deletion: ID " + book.getId());
                }
                return rowsDeleted;

            } catch (Exception e) {
                Log.e(TAG, "Error deleting book", e);
                return -1;
            }
        }, executorService);
    }

    /**
     * Delete book by ID
     *
     * @param bookId ID of book to delete
     * @return number of rows deleted (1 if success, 0 if not found, -1 if error)
     */
    public CompletableFuture<Integer> deleteBookById(long bookId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Book book = bookDao.getBookById(bookId);
                if (book == null) {
                    Log.w(TAG, "Book not found for deletion: ID " + bookId);
                    return 0;
                }
                return bookDao.deleteBook(book);

            } catch (Exception e) {
                Log.e(TAG, "Error deleting book by ID", e);
                return -1;
            }
        }, executorService);
    }

    /**
     * Delete all books (USE WITH CAUTION!)
     * Typically used before restore from backup
     *
     * @return true if success, false if error
     */
    public CompletableFuture<Boolean> deleteAllBooks() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                bookDao.deleteAllBooks();
                Log.d(TAG, "All books deleted successfully");
                return true;

            } catch (Exception e) {
                Log.e(TAG, "Error deleting all books", e);
                return false;
            }
        }, executorService);
    }

    // ============================================================
    // VALIDATION & UTILITY
    // ============================================================

    /**
     * Check if book name already exists (case-insensitive)
     * Useful for preventing duplicate book names
     *
     * @param name Book name to check
     * @return true if exists, false if not
     */
    public CompletableFuture<Boolean> isBookNameExists(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (name == null || name.trim().isEmpty()) {
                    return false;
                }

                List<Book> allBooks = bookDao.getAllBooks();
                String nameLower = name.trim().toLowerCase();

                for (Book book : allBooks) {
                    if (book.getName().trim().toLowerCase().equals(nameLower)) {
                        return true;
                    }
                }
                return false;

            } catch (Exception e) {
                Log.e(TAG, "Error checking book name existence", e);
                return false;
            }
        }, executorService);
    }

    /**
     * Check if user can add more books (FREE plan check)
     *
     * @param isFreePlan true if user is on FREE plan
     * @return true if can add, false if limit reached
     */
    public CompletableFuture<Boolean> canAddBook(boolean isFreePlan) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!isFreePlan) {
                    return true; // PREMIUM/BUSINESS = unlimited
                }

                int count = bookDao.getBookCount();
                return count < 5; // FREE = max 5 books

            } catch (Exception e) {
                Log.e(TAG, "Error checking if can add book", e);
                return false;
            }
        }, executorService);
    }

    // ============================================================
    // CLEANUP
    // ============================================================

    /**
     * Shutdown executor service
     * Call this when repository is no longer needed (e.g., app shutdown)
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            Log.d(TAG, "ExecutorService shutdown");
        }
    }
}