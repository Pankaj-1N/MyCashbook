package com.mycashbook.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mycashbook.app.model.Book;
import com.mycashbook.app.repository.BookRepository;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BookViewModel extends AndroidViewModel {

    private final BookRepository repository;

    // ---------- UI LiveData ----------
    private final LiveData<List<Book>> allBooks;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> insertSuccess = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>(null);

    public BookViewModel(@NonNull Application application) {
        super(application);
        repository = new BookRepository(application);
        allBooks = repository.getAllBooksLive();
    }

    // --------------------------------------------------
    // LIVE DATA ACCESSORS
    // --------------------------------------------------

    public LiveData<List<Book>> getAllBooks() {
        return allBooks;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getInsertSuccess() {
        return insertSuccess;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    public LiveData<Boolean> getDeleteSuccess() {
        return deleteSuccess;
    }

    // --------------------------------------------------
    // INSERT BOOK
    // --------------------------------------------------

    public void insertBook(String name, String description, boolean isFreePlan) {

        if (name == null || name.trim().isEmpty()) {
            message.postValue("Book name cannot be empty");
            return;
        }

        loading.postValue(true);

        Book book = new Book(
                name.trim(),
                description == null ? "" : description.trim(),
                new Date(),
                new Date()
        );

        repository.insertBook(book, isFreePlan)
                .thenAccept(id -> {
                    loading.postValue(false);

                    if (id == -1L) {
                        message.postValue("Free plan limit reached (Max 5 books)");
                        insertSuccess.postValue(false);
                    } else {
                        insertSuccess.postValue(true);
                    }
                })
                .exceptionally(ex -> {
                    loading.postValue(false);
                    message.postValue("Error: " + ex.getMessage());
                    insertSuccess.postValue(false);
                    return null;
                });
    }

    // --------------------------------------------------
    // UPDATE BOOK
    // --------------------------------------------------

    public void updateBook(Book book) {

        if (book.getName() == null || book.getName().trim().isEmpty()) {
            message.postValue("Book name cannot be empty");
            return;
        }

        loading.postValue(true);

        book.setUpdatedAt(new Date());

        repository.updateBook(book)
                .thenAccept(rows -> {
                    loading.postValue(false);
                    updateSuccess.postValue(rows > 0);
                })
                .exceptionally(ex -> {
                    loading.postValue(false);
                    message.postValue("Error: " + ex.getMessage());
                    updateSuccess.postValue(false);
                    return null;
                });
    }

    // --------------------------------------------------
    // DELETE BOOK
    // --------------------------------------------------

    public void deleteBook(Book book) {

        loading.postValue(true);

        repository.deleteBook(book)
                .thenAccept(rows -> {
                    loading.postValue(false);
                    deleteSuccess.postValue(rows > 0);
                })
                .exceptionally(ex -> {
                    loading.postValue(false);
                    message.postValue("Error: " + ex.getMessage());
                    deleteSuccess.postValue(false);
                    return null;
                });
    }
}
