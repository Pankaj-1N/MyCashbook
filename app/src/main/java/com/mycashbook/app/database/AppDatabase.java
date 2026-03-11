package com.mycashbook.app.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.mycashbook.app.database.converters.DateConverter;
import com.mycashbook.app.database.dao.BookDao;
import com.mycashbook.app.database.dao.SubBookDao;
import com.mycashbook.app.database.dao.TransactionDao;
import com.mycashbook.app.model.Book;
import com.mycashbook.app.model.SubBook;
import com.mycashbook.app.model.Transaction;

@Database(entities = {
        Book.class,
        SubBook.class,
        Transaction.class
}, version = 5, // Updated to 5 for book locking support
        exportSchema = false)
@TypeConverters({ DateConverter.class })
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    // ----------- DAO Accessors -----------
    public abstract BookDao bookDao();

    public abstract SubBookDao subBookDao();

    public abstract TransactionDao transactionDao();

    // ----------- Singleton Instance -----------
    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "mycashbook_database.db")
                            // CRITICAL FOR BACKUP: Forces data into one single .db file immediately
                            // This prevents data loss during backup by disabling WAL mode
                            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)

                            // Add migrations
                            .addMigrations(
                                    Migrations.MIGRATION_1_2,
                                    Migrations.MIGRATION_2_3,
                                    Migrations.MIGRATION_3_4,
                                    Migrations.MIGRATION_4_5)

                            // Fallback only if migration fails (not recommended for production)
                            // .fallbackToDestructiveMigration()

                            .addCallback(roomCallback) // Preload or cleanup tasks
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // ----------- Optional Callback -----------
    private static final Callback roomCallback = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Pre-populate logic can be added if needed (e.g., default categories)
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Actions each time DB opens (optional)
        }
    };
}
