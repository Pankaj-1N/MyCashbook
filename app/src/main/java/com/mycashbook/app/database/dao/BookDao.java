package com.mycashbook.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mycashbook.app.model.Book;

import java.util.List;

@Dao
public interface BookDao {

    // Insert Book
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertBook(Book book);

    // --- ADDED: Insert List of Books (Required for Restore) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Book> books);

    // Update Book
    @Update
    int updateBook(Book book);

    // Delete Book
    @Delete
    int deleteBook(Book book);

    // Get all books (LiveData)
    @Query("SELECT * FROM books ORDER BY createdAt DESC")
    LiveData<List<Book>> getAllBooksLive();

    // Get all books (List)
    @Query("SELECT * FROM books ORDER BY createdAt DESC")
    List<Book> getAllBooks();

    // --- ADDED: Synchronous get all (Required for Backup) ---
    @Query("SELECT * FROM books")
    List<Book> getAllBooksSync();

    // Get book by ID (Reactive)
    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    LiveData<Book> getBookByIdLive(long bookId);

    // Get book by ID (Snapshot)
    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    Book getBookById(long bookId);

    // Count books → used for FREE plan limit (5 max)
    @Query("SELECT COUNT(*) FROM books")
    int getBookCount();

    // Delete all books
    @Query("DELETE FROM books")
    void deleteAllBooks();

    // --- ADDED: Delete All alias (Required for Restore) ---
    @Query("DELETE FROM books")
    void deleteAll();

    // --- ADDED: Get Books with Balance ---
    @Query("SELECT b.*, SUM(sb.balance) as totalBalance " +
            "FROM books b " +
            "LEFT JOIN sub_books sb ON b.id = sb.bookId " +
            "GROUP BY b.id " +
            "ORDER BY b.createdAt DESC")
    LiveData<List<com.mycashbook.app.model.BookWithBalance>> getAllBooksWithBalanceLive();
}
