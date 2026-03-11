package com.mycashbook.app.database;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Database migrations for MyCashBook
 * IMPORTANT: When you update the database schema, increment the version number
 * in AppDatabase.java and add a migration here.
 */
public class Migrations {

    /**
     * Migration from version 1 to 2
     * Adds new fields to transactions table:
     * - paymentMethod (for tracking payment types)
     * - createdBy (for team collaboration)
     * - createdByName (for team collaboration)
     * - synced (for offline sync tracking)
     */
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add new columns to transactions table
            database.execSQL("ALTER TABLE transactions ADD COLUMN paymentMethod TEXT DEFAULT 'CASH'");
            database.execSQL("ALTER TABLE transactions ADD COLUMN createdBy TEXT");
            database.execSQL("ALTER TABLE transactions ADD COLUMN createdByName TEXT");
            database.execSQL("ALTER TABLE transactions ADD COLUMN synced INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE transactions ADD COLUMN contact TEXT");
            database.execSQL("ALTER TABLE transactions ADD COLUMN paymentApp TEXT");
        }
    };

    /**
     * Migration from version 3 to 4
     * Adds currency support to books table
     */
    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE books ADD COLUMN currencyCode TEXT DEFAULT 'INR'");
            database.execSQL("ALTER TABLE books ADD COLUMN currencySymbol TEXT DEFAULT '₹'");
            database.execSQL("ALTER TABLE books ADD COLUMN currencyName TEXT DEFAULT 'Indian Rupee'");
        }
    };

    /**
     * Migration from version 4 to 5
     * Adds book locking/security fields
     */
    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE books ADD COLUMN isLocked INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE books ADD COLUMN lockPin TEXT");
            database.execSQL("ALTER TABLE books ADD COLUMN useBiometric INTEGER NOT NULL DEFAULT 0");
        }
    };
}
