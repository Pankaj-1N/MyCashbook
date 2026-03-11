package com.mycashbook.app.constants;

/**
 * Centralized string constants for the application
 * All UI strings, error messages, and labels should be defined here
 * This makes it easy to manage text and support multiple languages in future
 */
public class StringConstants {

    // ============================================================
    // APP INFO
    // ============================================================
    public static final String APP_NAME = "MyCashBook";
    public static final String APP_TAGLINE = "Your Daily Ledger Partner";

    // ============================================================
    // GOOGLE CREDENTIALS
    // ============================================================
    public static final String GOOGLE_SERVER_CLIENT_ID = "289644403111-i8hdp4oprrld32415s3406b5o6kk6u6v.apps.googleusercontent.com";
    public static final String GOOGLE_WEB_CLIENT_ID = "289644403111-18i4kpmnl2p1dsk0cs8imcjnp4uikq3k.apps.googleusercontent.com";

    // ============================================================
    // ERROR MESSAGES - User Facing
    // ============================================================
    public static final String ERROR_INVALID_EMAIL = "Invalid email address";
    public static final String ERROR_INVALID_AMOUNT = "Amount must be greater than 0";
    public static final String ERROR_EMPTY_NAME = "Name cannot be empty";
    public static final String ERROR_EMPTY_DESCRIPTION = "Description cannot be empty";
    public static final String ERROR_INVALID_DATE = "Please select a valid date";
    public static final String ERROR_DATABASE = "Database error. Please try again.";
    public static final String ERROR_NETWORK = "Network error. Please check your connection.";
    public static final String ERROR_UNKNOWN = "An unexpected error occurred. Please try again.";
    public static final String ERROR_SESSION_EXPIRED = "Your session has expired. Please log in again.";
    public static final String ERROR_UNAUTHORIZED = "You are not authorized to perform this action.";
    public static final String ERROR_EXPORT_FAILED = "Export failed";
    public static final String ERROR_BACKUP_FAILED = "Backup failed";
    public static final String ERROR_RESTORE_FAILED = "Restore failed";
    public static final String ERROR_IMPORT_FAILED = "Import failed";

    // ============================================================
    // SUCCESS MESSAGES
    // ============================================================
    public static final String SUCCESS_SAVED = "Saved successfully!";
    public static final String SUCCESS_DELETED = "Deleted successfully!";
    public static final String SUCCESS_UPDATED = "Updated successfully!";
    public static final String SUCCESS_LOGIN = "Login successful!";
    public static final String SUCCESS_EXPORT = "Export successful!";
    public static final String SUCCESS_BACKUP = "Backup successful!";
    public static final String SUCCESS_RESTORE = "Restore successful!";
    public static final String SUCCESS_IMPORT = "Import successful!";

    // ============================================================
    // CONFIRMATION MESSAGES
    // ============================================================
    public static final String CONFIRM_DELETE = "Are you sure you want to delete?";
    public static final String CONFIRM_DELETE_BOOK = "Delete this book?";
    public static final String CONFIRM_DELETE_SUBBOOK = "Delete this sub-book?";
    public static final String CONFIRM_DELETE_TRANSACTION = "Delete this transaction?";
    public static final String CONFIRM_LOGOUT = "Are you sure you want to log out?";

    // ============================================================
    // INFO MESSAGES
    // ============================================================
    public static final String INFO_LOADING = "Loading…";
    public static final String INFO_WELCOME_BACK = "Welcome back!";
    public static final String INFO_SIGNING_IN = "Signing in...";
    public static final String INFO_EMPTY_LIST = "No data found";

    // ============================================================
    // SPLASH SCREEN
    // ============================================================
    public static final String SPLASH_LOADING = "Loading your ledger…";

    // ============================================================
    // HOME SCREEN
    // ============================================================
    public static final String HOME_TITLE = "My Books";
    public static final String HOME_ADD_BOOK = "Add Book";
    public static final String HOME_NO_BOOKS = "No books yet. Create your first book!";
    public static final String HOME_MENU_SETTINGS = "Settings";
    public static final String HOME_MENU_BACKUP = "Backup & Restore";
    public static final String HOME_MENU_UPGRADE = "Upgrade Plan";

    // ============================================================
    // BOOK MANAGEMENT
    // ============================================================
    public static final String BOOK_NAME = "Book Name";
    public static final String BOOK_NAME_HINT = "e.g., Personal, Business";
    public static final String BOOK_CREATE = "Create Book";
    public static final String BOOK_LIMIT_REACHED = "Book limit reached. Upgrade to create more books.";

    // ============================================================
    // SUBBOOK MANAGEMENT
    // ============================================================
    public static final String SUBBOOK_NAME = "SubBook Name";
    public static final String SUBBOOK_NAME_HINT = "e.g., Cash, Bank Account";
    public static final String SUBBOOK_CREATE = "Create SubBook";
    public static final String SUBBOOK_NO_ITEMS = "No subbooks yet. Create one to start tracking.";

    // ============================================================
    // TRANSACTION MANAGEMENT
    // ============================================================
    public static final String TRANSACTION_ADD = "Add Transaction";
    public static final String TRANSACTION_EDIT = "Edit Transaction";
    public static final String TRANSACTION_TYPE_IN = "IN";
    public static final String TRANSACTION_TYPE_OUT = "OUT";
    public static final String TRANSACTION_TYPE_DEBIT = "Debit";
    public static final String TRANSACTION_TYPE_CREDIT = "Credit";
    public static final String TRANSACTION_AMOUNT = "Amount";
    public static final String TRANSACTION_NOTES = "Notes";
    public static final String TRANSACTION_DATE = "Date";
    public static final String TRANSACTION_SAVE = "Save";
    public static final String TRANSACTION_NO_ITEMS = "No transactions yet.";

    // ============================================================
    // FILTER & SEARCH
    // ============================================================
    public static final String FILTER_TITLE = "Filter Transactions";
    public static final String FILTER_DATE_SINGLE = "Single Date";
    public static final String FILTER_DATE_RANGE = "Date Range";
    public static final String FILTER_NOTES = "Search Notes";
    public static final String FILTER_APPLY = "Apply Filter";
    public static final String FILTER_CLEAR = "Clear Filter";

    // ============================================================
    // SUMMARY & REPORTS
    // ============================================================
    public static final String SUMMARY_TOTAL_IN = "Total IN";
    public static final String SUMMARY_TOTAL_OUT = "Total OUT";
    public static final String SUMMARY_BALANCE = "Balance";
    public static final String SUMMARY_TITLE = "Summary";

    // ============================================================
    // EXPORT FUNCTIONALITY
    // ============================================================
    public static final String EXPORT_TITLE = "Export Report";
    public static final String EXPORT_PDF = "Export as PDF";
    public static final String EXPORT_CSV = "Export as CSV";
    public static final String EXPORT_EXCEL = "Export as Excel";
    public static final String EXPORT_SUCCESS = "Export successful!";
    public static final String EXPORT_FAILED = "Export failed";
    public static final String EXPORT_EXCEL_BUSINESS_ONLY = "Excel export is available in Business Plan only.";
    public static final String EXPORT_BILL_SLIP = "Export Bill Slip";

    // ============================================================
    // BACKUP & RESTORE
    // ============================================================
    public static final String BACKUP_TITLE = "Backup & Restore";
    public static final String BACKUP_SIGN_IN = "Sign in with Google";
    public static final String BACKUP_CREATE = "Create Backup";
    public static final String BACKUP_RESTORE = "Restore from Backup";
    public static final String BACKUP_AUTO = "Auto Backup";
    public static final String BACKUP_SUCCESS = "Backup successful!";
    public static final String BACKUP_FAILED = "Backup failed";
    public static final String RESTORE_SUCCESS = "Restore successful!";
    public static final String RESTORE_FAILED = "Restore failed";
    public static final String BACKUP_HISTORY = "Backup History";

    // ============================================================
    // IMPORT FUNCTIONALITY
    // ============================================================
    public static final String IMPORT_TITLE = "Import Data";
    public static final String IMPORT_SELECT_FILE = "Select File";
    public static final String IMPORT_CSV = "Import CSV";
    public static final String IMPORT_EXCEL = "Import Excel";
    public static final String IMPORT_BUSINESS_ONLY = "Import feature is available in Business Plan only.";
    public static final String IMPORT_MAPPING_TITLE = "Map Columns";
    public static final String IMPORT_SUCCESS = "Import successful!";

    // ============================================================
    // BILLING & PLANS
    // ============================================================
    public static final String BILLING_TITLE = "Upgrade Your Plan";
    public static final String BILLING_FREE = "Free";
    public static final String BILLING_PREMIUM = "Premium";
    public static final String BILLING_BUSINESS = "Business";
    public static final String BILLING_SUBSCRIBE = "Subscribe";
    public static final String BILLING_RESTORE = "Restore Purchase";
    public static final String BILLING_CURRENT_PLAN = "Current Plan";
    public static final String BILLING_UPGRADE = "Upgrade";

    // ============================================================
    // SETTINGS
    // ============================================================
    public static final String SETTINGS_TITLE = "Settings";
    public static final String SETTINGS_THEME = "Theme";
    public static final String SETTINGS_SECURITY = "Security";
    public static final String SETTINGS_PIN_LOCK = "PIN Lock";
    public static final String SETTINGS_BIOMETRIC = "Biometric Lock";
    public static final String SETTINGS_ABOUT = "About";
    public static final String SETTINGS_FEEDBACK = "Send Feedback";
    public static final String SETTINGS_PRIVACY = "Privacy Policy";
    public static final String SETTINGS_TERMS = "Terms of Service";
    public static final String SETTINGS_VERSION = "Version";

    // ============================================================
    // GENERAL UI ACTIONS
    // ============================================================
    public static final String ACTION_OK = "OK";
    public static final String ACTION_CANCEL = "Cancel";
    public static final String ACTION_DELETE = "Delete";
    public static final String ACTION_EDIT = "Edit";
    public static final String ACTION_SAVE = "Save";
    public static final String ACTION_UPDATE = "Update";
    public static final String ACTION_SEARCH = "Search";
    public static final String ACTION_LOADING = "Loading…";
    public static final String ACTION_ERROR = "Error";
    public static final String ACTION_YES = "Yes";
    public static final String ACTION_NO = "No";
    public static final String ACTION_ADD = "Add";
    public static final String ACTION_CLOSE = "Close";
    public static final String ACTION_BACK = "Back";
    public static final String ACTION_RETRY = "Retry";
    public static final String ACTION_SUBMIT = "Submit";

    // ============================================================
    // EMPTY STATES
    // ============================================================
    public static final String EMPTY_NO_BOOKS = "No books yet. Create your first book!";
    public static final String EMPTY_NO_SUBBOOKS = "No sub-books yet. Create one to start tracking.";
    public static final String EMPTY_NO_TRANSACTIONS = "No transactions yet.";
    public static final String EMPTY_NO_DATA = "No data found";

    // ============================================================
    // FEEDBACK & SUPPORT
    // ============================================================
    public static final String FEEDBACK_TITLE = "Send Feedback";
    public static final String FEEDBACK_SUBJECT_PLACEHOLDER = "Subject";
    public static final String FEEDBACK_MESSAGE_PLACEHOLDER = "Tell us what you think...";
    public static final String FEEDBACK_ATTACH_SCREENSHOT = "Attach Screenshot";
    public static final String FEEDBACK_SEND = "Send Feedback";
    public static final String FEEDBACK_SUCCESS = "Thank you for your feedback!";
    public static final String FEEDBACK_ERROR = "Failed to send feedback";

    // ============================================================
    // LABELS & HEADINGS
    // ============================================================
    public static final String LABEL_DATE = "Date";
    public static final String LABEL_AMOUNT = "Amount";
    public static final String LABEL_DESCRIPTION = "Description";
    public static final String LABEL_NOTE = "Note";
    public static final String LABEL_CATEGORY = "Category";
    public static final String LABEL_STATUS = "Status";
    public static final String LABEL_TYPE = "Type";
    public static final String LABEL_TOTAL = "Total";
    public static final String LABEL_BALANCE = "Balance";

    // ============================================================
    // TIME LABELS
    // ============================================================
    public static final String TIME_TODAY = "Today";
    public static final String TIME_YESTERDAY = "Yesterday";
    public static final String TIME_THIS_WEEK = "This Week";
    public static final String TIME_THIS_MONTH = "This Month";
    public static final String TIME_THIS_YEAR = "This Year";

    // ============================================================
    // VALIDATION & MESSAGES
    // ============================================================
    public static final String VALIDATION_REQUIRED = "This field is required";
    public static final String VALIDATION_INVALID_FORMAT = "Invalid format";
    public static final String VALIDATION_MUST_BE_POSITIVE = "Must be a positive number";

}