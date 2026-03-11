package com.mycashbook.app.constants;

/**
 * Central repository for all application constants
 * Avoid hardcoding strings, numbers, or values directly in code
 */
public class AppConstants {

    // ============================================================
    // APP INFO
    // ============================================================
    public static final String APP_NAME = "MyCashBook";
    public static final String APP_VERSION = "1.0.0";
    public static final String SUPPORT_EMAIL = "support@mycashbook.app";

    // ============================================================
    // SHARED PREFERENCES KEYS
    // ============================================================
    public static final String PREF_NAME = "MyCashBook_Prefs";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_SESSION_TOKEN = "session_token";
    public static final String KEY_LAST_LOGIN = "last_login";
    public static final String KEY_THEME_MODE = "theme_mode";
    public static final String KEY_CURRENCY = "currency";
    public static final String KEY_AUTO_BACKUP = "auto_backup";
    public static final String KEY_BACKUP_FREQUENCY = "backup_frequency";
    public static final String KEY_LAST_BACKUP = "last_backup";
    public static final String KEY_SESSION_EXPIRY = "session_expiry";

    // ============================================================
    // THEME MODES
    // ============================================================
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_SYSTEM = "system";

    // ============================================================
    // INTENT KEYS
    // ============================================================
    public static final String INTENT_KEY_BOOK_ID = "bookId";
    public static final String INTENT_KEY_BOOK_NAME = "bookName";
    public static final String INTENT_KEY_SUBBOOK_ID = "subBookId";
    public static final String INTENT_KEY_SUBBOOK_NAME = "subBookName";
    public static final String INTENT_KEY_TRANSACTION_ID = "transactionId";
    public static final String INTENT_KEY_TRANSACTION = "transaction";

    // ============================================================
    // TRANSACTION TYPES
    // ============================================================
    public static final String TRANSACTION_TYPE_DEBIT = "DEBIT";
    public static final String TRANSACTION_TYPE_CREDIT = "CREDIT";

    // ============================================================
    // TRANSACTION CATEGORIES (Can be extended later)
    // ============================================================
    public static final String CATEGORY_FOOD = "Food";
    public static final String CATEGORY_TRANSPORT = "Transport";
    public static final String CATEGORY_UTILITIES = "Utilities";
    public static final String CATEGORY_ENTERTAINMENT = "Entertainment";
    public static final String CATEGORY_SALARY = "Salary";
    public static final String CATEGORY_INVESTMENT = "Investment";
    public static final String CATEGORY_OTHER = "Other";

    // ============================================================
    // PLAN TYPES
    // ============================================================
    public static final String PLAN_FREE = "free";
    public static final String PLAN_PREMIUM = "premium";
    public static final String PLAN_BUSINESS = "business";

    // ============================================================
    // PLAN LIMITS
    // ============================================================
    public static final int FREE_PLAN_BOOK_LIMIT = 5;
    public static final int PREMIUM_PLAN_BOOK_LIMIT = 50;
    public static final int BUSINESS_PLAN_BOOK_LIMIT = Integer.MAX_VALUE;

    // ============================================================
    // TIMEOUTS & DELAYS (in milliseconds)
    // ============================================================
    public static final long AUTO_LOGIN_DELAY = 800;
    public static final long ANIMATION_DURATION = 600;
    public static final long PROGRESS_SHOW_DELAY = 300;
    public static final long DIALOG_DISMISS_DELAY = 500;
    public static final long WAVE_ANIMATION_DURATION = 1200;
    public static final long SPLASH_DELAY = 2000;
    public static final long SNACKBAR_DURATION = 3000;
    public static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes

    // ============================================================
    // CURRENCY SETTINGS
    // ============================================================
    public static final String CURRENCY_SYMBOL = "₹";
    public static final String CURRENCY_CODE = "INR";
    public static final int DECIMAL_PLACES = 2;

    // ============================================================
    // DATE & TIME FORMATS
    // ============================================================
    public static final String DATE_FORMAT_DISPLAY = "dd MMM yyyy";
    public static final String DATE_FORMAT_DATABASE = "yyyy-MM-dd";
    public static final String DATE_FORMAT_FULL = "dd MMM yyyy HH:mm";
    public static final String TIME_FORMAT_DISPLAY = "HH:mm";
    public static final String DATETIME_FORMAT_FULL = "dd MMM yyyy hh:mm a";

    // ============================================================
    // DATABASE SETTINGS
    // ============================================================
    public static final String DATABASE_NAME = "mycashbook_database.db";
    public static final int DATABASE_VERSION = 1;

    // ============================================================
    // BACKUP SETTINGS
    // ============================================================
    public static final String BACKUP_FREQUENCY_DAILY = "daily";
    public static final String BACKUP_FREQUENCY_WEEKLY = "weekly";
    public static final String BACKUP_FREQUENCY_MONTHLY = "monthly";
    public static final long BACKUP_FILE_MAX_SIZE = 50 * 1024 * 1024; // 50 MB

    // ============================================================
    // FILE PATHS & NAMING
    // ============================================================
    public static final String BACKUP_FOLDER_NAME = "MyCashBook_Backups";
    public static final String EXPORT_FOLDER_NAME = "MyCashBook_Exports";
    public static final String FEEDBACK_FOLDER_NAME = "MyCashBook_Feedback";
    public static final String BACKUP_FILE_PREFIX = "backup_";
    public static final String EXPORT_FILE_PREFIX = "export_";

    // ============================================================
    // ERROR CODES
    // ============================================================
    public static final int ERROR_CODE_INVALID_INPUT = 400;
    public static final int ERROR_CODE_UNAUTHORIZED = 401;
    public static final int ERROR_CODE_FORBIDDEN = 403;
    public static final int ERROR_CODE_NOT_FOUND = 404;
    public static final int ERROR_CODE_SERVER_ERROR = 500;
    public static final int ERROR_CODE_DATABASE_ERROR = 600;
    public static final int ERROR_CODE_NETWORK_ERROR = 700;
    public static final int ERROR_CODE_UNKNOWN = 999;

    // ============================================================
    // VALIDATION RULES
    // ============================================================
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final int MAX_NOTE_LENGTH = 500;

    // ============================================================
    // PAGINATION & LIMITS
    // ============================================================
    public static final int PAGE_SIZE = 20;
    public static final int MAX_RECENT_ITEMS = 10;

    // ============================================================
    // LOG TAGS (for Logcat debugging)
    // ============================================================
    public static final String TAG_AUTH = "Auth";
    public static final String TAG_HOME = "Home";
    public static final String TAG_BOOK = "Book";
    public static final String TAG_SUBBOOK = "SubBook";
    public static final String TAG_TRANSACTION = "Transaction";
    public static final String TAG_SETTINGS = "Settings";
    public static final String TAG_BACKUP = "Backup";
    public static final String TAG_EXPORT = "Export";
    public static final String TAG_REPORT = "Report";
    public static final String TAG_DATABASE = "Database";
    public static final String TAG_REPOSITORY = "Repository";
    public static final String TAG_VIEWMODEL = "ViewModel";

    // ============================================================
    // FEATURE FLAGS (Can enable/disable features)
    // ============================================================
    public static final boolean FEATURE_BACKUP_ENABLED = true;
    public static final boolean FEATURE_EXPORT_ENABLED = true;
    public static final boolean FEATURE_ANALYTICS_ENABLED = true;
    public static final boolean FEATURE_PREMIUM_ENABLED = true;
    public static final boolean FEATURE_ADS_ENABLED = true;

    // ============================================================
    // DEBUG SETTINGS
    // ============================================================
    public static final boolean DEBUG_MODE = false; // Set to false in production
    public static final boolean VERBOSE_LOGGING = false; // Set to false in production
}