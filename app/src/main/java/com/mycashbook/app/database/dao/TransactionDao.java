package com.mycashbook.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mycashbook.app.model.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {

    // Insert Single Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTransaction(Transaction transaction);

    // --- ADDED: Insert List of Transactions (Required for Restore) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Transaction> transactions);

    // Update Transaction
    @Update
    int updateTransaction(Transaction transaction);

    // Delete Transaction
    @Delete
    int deleteTransaction(Transaction transaction);

    // All transactions for a SubBook
    @Query("SELECT * FROM transactions WHERE subBookId = :subBookId ORDER BY date DESC, id DESC")
    LiveData<List<Transaction>> getTransactionsForSubBook(long subBookId);

    // List version (non-LiveData)
    @Query("SELECT * FROM transactions WHERE subBookId = :subBookId ORDER BY date DESC, id DESC")
    List<Transaction> getTransactionsListForSubBook(long subBookId);

    // Get single transaction
    @Query("SELECT * FROM transactions WHERE id = :txnId LIMIT 1")
    Transaction getTransactionById(long txnId);

    // --- ADDED: Get ALL Transactions (Required for Backup) ---
    @Query("SELECT * FROM transactions")
    List<Transaction> getAllTransactionsSync();

    // --- ADDED: Alias for BackupRepository (same as above) ---
    @Query("SELECT * FROM transactions")
    List<Transaction> getAllTransactionsForBackup();

    // Delete all transactions of a subbook
    @Query("DELETE FROM transactions WHERE subBookId = :subBookId")
    void deleteTransactionsBySubBook(long subBookId);

    // Delete all (Existing method)
    @Query("DELETE FROM transactions")
    void deleteAllTransactions();

    // --- ADDED: Delete All (Required for Restore - Alias) ---
    @Query("DELETE FROM transactions")
    void deleteAll();

    // Summary: Total debit/credit - CHANGED TO RETURN LiveData<Double>
    @Query("SELECT SUM(amount) FROM transactions WHERE subBookId = :subBookId AND type = 'DEBIT'")
    LiveData<Double> getTotalDebit(long subBookId);

    @Query("SELECT SUM(amount) FROM transactions WHERE subBookId = :subBookId AND type = 'CREDIT'")
    LiveData<Double> getTotalCredit(long subBookId);

    // --- ADDED: Synchronous versions for Repository updates ---
    @Query("SELECT SUM(amount) FROM transactions WHERE subBookId = :subBookId AND type = 'DEBIT'")
    Double getTotalDebitSync(long subBookId);

    @Query("SELECT COUNT(*) FROM transactions WHERE subBookId = :subBookId AND type = 'CREDIT'")
    Double getTotalCreditSync(long subBookId);

    @Query("SELECT COUNT(*) FROM transactions t JOIN sub_books s ON t.subBookId = s.id WHERE s.bookId = :bookId")
    LiveData<Integer> getTransactionCountForBook(long bookId);

    @Query("SELECT COUNT(*) FROM transactions t JOIN sub_books s ON t.subBookId = s.id WHERE s.bookId = :bookId")
    Integer getTransactionCountForBookSync(long bookId);
}