package com.mycashbook.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mycashbook.app.model.SubBook;

import java.util.List;

@Dao
public interface SubBookDao {

    // Insert SubBook
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSubBook(SubBook subBook);

    // Insert list of SubBooks (for restore)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SubBook> subBooks);

    // Update SubBook
    @Update
    int updateSubBook(SubBook subBook);

    // Delete SubBook
    @Delete
    int deleteSubBook(SubBook subBook);

    // Get all sub-books for a specific book (LiveData)
    @Query("SELECT * FROM sub_books WHERE bookId = :bookId ORDER BY createdAt DESC")
    LiveData<List<SubBook>> getSubBooksByBookIdLive(long bookId);

    // Get all sub-books for a specific book (List)
    @Query("SELECT * FROM sub_books WHERE bookId = :bookId ORDER BY createdAt DESC")
    List<SubBook> getSubBooksByBookId(long bookId);

    // Get sub-book by ID
    @Query("SELECT * FROM sub_books WHERE id = :subBookId LIMIT 1")
    SubBook getSubBookById(long subBookId);

    // Count sub-books in a specific book
    @Query("SELECT COUNT(*) FROM sub_books WHERE bookId = :bookId")
    int getSubBookCount(long bookId);

    // Count all sub-books
    @Query("SELECT COUNT(*) FROM sub_books")
    int getSubBookCount();

    // Delete all sub-books for a specific book
    @Query("DELETE FROM sub_books WHERE bookId = :bookId")
    void deleteSubBooksByBookId(long bookId);

    // Delete all sub-books
    @Query("DELETE FROM sub_books")
    void deleteAllSubBooks();

    // Get all sub-books for a specific book with STATS (LiveData)
    @Query("SELECT S.*, " +
            "(SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE subBookId = S.id AND type = 'CREDIT') as totalIncome, "
            +
            "(SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE subBookId = S.id AND type = 'DEBIT') as totalExpense "
            +
            "FROM sub_books S WHERE bookId = :bookId ORDER BY createdAt DESC")
    LiveData<List<com.mycashbook.app.model.SubBookWithStats>> getSubBooksWithStatsByBookIdLive(long bookId);

    // Get total balance for a book (calculated from transactions: CREDIT - DEBIT)
    @Query("SELECT " +
            "(SELECT COALESCE(SUM(T.amount), 0) FROM transactions T JOIN sub_books S ON T.subBookId = S.id WHERE S.bookId = :bookId AND T.type = 'CREDIT') - "
            +
            "(SELECT COALESCE(SUM(T.amount), 0) FROM transactions T JOIN sub_books S ON T.subBookId = S.id WHERE S.bookId = :bookId AND T.type = 'DEBIT')")
    LiveData<Double> getTotalBalanceForBook(long bookId);

    // Get total Income for a book (sum of 'CREDIT' transactions across all
    // sub-books)
    @Query("SELECT SUM(amount) FROM transactions T JOIN sub_books S ON T.subBookId = S.id WHERE S.bookId = :bookId AND T.type = 'CREDIT'")
    LiveData<Double> getTotalIncomeForBook(long bookId);

    // Get total Expense for a book (sum of 'DEBIT' transactions across all
    // sub-books)
    @Query("SELECT SUM(amount) FROM transactions T JOIN sub_books S ON T.subBookId = S.id WHERE S.bookId = :bookId AND T.type = 'DEBIT'")
    LiveData<Double> getTotalExpenseForBook(long bookId);

    // Get the parent Book for a specific sub-book (Reactive)
    @Query("SELECT B.* FROM books B JOIN sub_books S ON B.id = S.bookId WHERE S.id = :subBookId")
    LiveData<com.mycashbook.app.model.Book> getBookBySubBookIdLive(long subBookId);

    // Synchronous get all sub-books (for backup)
    @Query("SELECT * FROM sub_books")
    List<SubBook> getAllSubBooksSync();
}
