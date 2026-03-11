package com.mycashbook.app.utils;

public class Constants {

    // App Configuration
    public static final String APP_NAME = "MyCashBook";
    public static final String APP_TAGLINE = "Your Daily Ledger Partner";

    // Book Limits
    public static final int FREE_BOOK_LIMIT = 5;
    public static final int PREMIUM_BOOK_LIMIT = -1; // Unlimited
    public static final int BUSINESS_BOOK_LIMIT = -1; // Unlimited

    // Transaction Types
    public static final String TRANSACTION_TYPE_IN = "IN";
    public static final String TRANSACTION_TYPE_OUT = "OUT";

    // Date Formats
    public static final String DATE_FORMAT_DISPLAY = "dd MMM yyyy";
    public static final String DATE_FORMAT_FULL = "dd MMM yyyy, hh:mm a";
    public static final String DATE_FORMAT_FILE = "yyyyMMdd_HHmmss";

    // Shared Preferences
    public static final String PREFS_NAME = "MyCashBookPrefs";
    public static final String PREF_USER_PLAN = "user_plan";
    public static final String PREF_GOOGLE_ACCOUNT = "google_account";
    public static final String PREF_PIN_ENABLED = "pin_enabled";
    public static final String PREF_PIN_CODE = "pin_code";
    public static final String PREF_BIOMETRIC_ENABLED = "biometric_enabled";
    public static final String PREF_THEME_MODE = "theme_mode";
    public static final String PREF_AUTO_BACKUP_ENABLED = "auto_backup_enabled";

    // User Plans
    public static final String PLAN_FREE = "FREE";
    public static final String PLAN_PREMIUM = "PREMIUM";
    public static final String PLAN_BUSINESS = "BUSINESS";

    // Subscription SKUs (Replace with your actual SKUs)
    public static final String SKU_PREMIUM_MONTHLY = "premium_monthly";
    public static final String SKU_PREMIUM_YEARLY = "premium_yearly";
    public static final String SKU_PREMIUM_LIFETIME = "premium_lifetime";
    public static final String SKU_BUSINESS_MONTHLY = "business_monthly";
    public static final String SKU_BUSINESS_YEARLY = "business_yearly";
    public static final String SKU_BUSINESS_LIFETIME = "business_lifetime";

    // Google Drive
    public static final String DRIVE_BACKUP_FOLDER = "appDataFolder";
    public static final String BACKUP_FILE_PREFIX = "mycashbook_backup_";
    public static final String BACKUP_FILE_EXTENSION = ".json";
    public static final int MAX_BACKUP_FILES = 10;

    // Export
    public static final String EXPORT_FOLDER_NAME = "MyCashBook";
    public static final String EXPORT_PDF_PREFIX = "MyCashBook_Report_";
    public static final String EXPORT_CSV_PREFIX = "MyCashBook_Export_";
    public static final String EXPORT_EXCEL_PREFIX = "MyCashBook_Export_";

    // Import
    public static final String IMPORT_TYPE_CSV = "CSV";
    public static final String IMPORT_TYPE_EXCEL = "EXCEL";
    public static final String[] CREDIT_KEYWORDS = {"credit", "cr", "deposit", "in", "income", "received"};
    public static final String[] DEBIT_KEYWORDS = {"debit", "dr", "withdraw", "out", "expense", "paid", "payment"};

    // AdMob (Replace with your actual Ad Unit IDs)
    public static final String AD_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"; // Test ID
    public static final String AD_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"; // Test ID
    public static final String AD_APP_OPEN_ID = "ca-app-pub-3940256099942544/3419835294"; // Test ID

    // Ad Display Intervals
    public static final long AD_INTERSTITIAL_INTERVAL_MS = 15 * 60 * 1000; // 15 minutes

    // Request Codes
    public static final int REQUEST_CODE_SIGN_IN = 1001;
    public static final int REQUEST_CODE_PICK_FILE = 1002;
    public static final int REQUEST_CODE_PERMISSION = 1003;

    // Intent Extras
    public static final String EXTRA_BOOK_ID = "book_id";
    public static final String EXTRA_BOOK_NAME = "book_name";
    public static final String EXTRA_SUBBOOK_ID = "subbook_id";
    public static final String EXTRA_SUBBOOK_NAME = "subbook_name";
    public static final String EXTRA_TRANSACTION_ID = "transaction_id";
    public static final String EXTRA_TRANSACTION_TYPE = "transaction_type";

    private Constants() {
        // Private constructor to prevent instantiation
    }
}