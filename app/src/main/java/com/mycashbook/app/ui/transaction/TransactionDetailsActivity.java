package com.mycashbook.app.ui.transaction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mycashbook.app.R;
import com.mycashbook.app.base.BaseActivity;
import com.mycashbook.app.model.Transaction;
import com.mycashbook.app.utils.CurrencyUtils;
import com.mycashbook.app.utils.LogUtils;
import com.mycashbook.app.viewmodel.TransactionViewModel;
import com.mycashbook.app.utils.ExportUtils;
import com.mycashbook.app.utils.FeatureManager;
import com.mycashbook.app.utils.SessionManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Transaction Details Activity - Refactored to extend BaseActivity
 * Displays transactions for a specific sub-book with balance summary
 */
public class TransactionDetailsActivity extends BaseActivity {

    private static final String TAG = "TransactionDetailsActivity";

    // UI Components
    private Toolbar toolbar;
    private RecyclerView recyclerViewTransactions;
    private FloatingActionButton fabAddTransaction;
    private TextView tvEmptyState;
    private TextView tvTotalDebit;
    private TextView tvTotalCredit;
    private TextView tvBalance;
    private TextView textPercentIn;
    private TextView textPercentOut;
    private View viewBarIn;
    private View viewBarOut;
    private View btnShare;
    private View btnFilter;
    private View summaryHeader;
    private View fabRing;

    // ViewModel & Adapter
    private TransactionViewModel transactionViewModel;
    private TransactionListAdapter transactionAdapter;

    // SubBook Data
    private long subBookId;
    private String subBookName;
    private com.mycashbook.app.model.Book currentBook;

    // Transaction to delete
    private Transaction transactionToDelete;

    // Filter state
    private Date filterFromDate;
    private Date filterToDate;
    private String filterPaymentMethod;
    private String filterPaymentApp;
    private List<Transaction> allTransactions = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.methodEntry(TAG, "onCreate");

        setContentView(R.layout.activity_transaction_details);

        // Get SubBook ID from intent
        Intent intent = getIntent();
        subBookId = intent.getLongExtra("subBookId", -1);
        subBookName = intent.getStringExtra("subBookName");

        if (subBookId == -1) {
            LogUtils.e(TAG, "Invalid subBookId");
            showErrorMessage("Error: Invalid sub-book");
            finishActivity();
            return;
        }

        LogUtils.d(TAG, "TransactionDetailsActivity opened for: " + subBookName + " (ID: " + subBookId + ")");

        // Initialize all components
        initViews();
        setupToolbar();
        initViewModel();
        setupRecyclerView();
        setupListeners();
        observeViewModel();

        LogUtils.methodExit(TAG, "onCreate");
    }

    /**
     * Initialize UI views
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewTransactions = findViewById(R.id.recyclerViewTransactions);
        fabAddTransaction = findViewById(R.id.fabAddTransaction);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvTotalDebit = findViewById(R.id.tvTotalDebit);
        tvTotalCredit = findViewById(R.id.tvTotalCredit);
        tvBalance = findViewById(R.id.tvBalance);
        textPercentIn = findViewById(R.id.textPercentIn);
        textPercentOut = findViewById(R.id.textPercentOut);
        viewBarIn = findViewById(R.id.viewBarIn);
        viewBarOut = findViewById(R.id.viewBarOut);
        btnShare = findViewById(R.id.btnShare);
        btnFilter = findViewById(R.id.btnFilter);
        summaryHeader = findViewById(R.id.summaryHeader);
        fabRing = findViewById(R.id.fabRing);

        startFabAnimation();

        LogUtils.d(TAG, "Views initialized");
    }

    /**
     * Start continuous pulse animation for the FAB ring
     */
    private void startFabAnimation() {
        if (fabRing == null)
            return;

        AnimationSet animationSet = new AnimationSet(true);

        ScaleAnimation scaleAnim = new ScaleAnimation(
                1.0f, 1.4f,
                1.0f, 1.4f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnim.setDuration(1500);
        scaleAnim.setRepeatCount(Animation.INFINITE);
        scaleAnim.setRepeatMode(Animation.RESTART);

        AlphaAnimation alphaAnim = new AlphaAnimation(0.3f, 0.0f);
        alphaAnim.setDuration(1500);
        alphaAnim.setRepeatCount(Animation.INFINITE);
        alphaAnim.setRepeatMode(Animation.RESTART);

        animationSet.addAnimation(scaleAnim);
        animationSet.addAnimation(alphaAnim);

        fabRing.startAnimation(animationSet);
    }

    /**
     * Setup toolbar with back button
     */
    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);

                // Explicitly set the back arrow and tint it to ensure visibility in dark mode
                android.graphics.drawable.Drawable upArrow = androidx.core.content.ContextCompat.getDrawable(this,
                        R.drawable.ic_arrow_back);
                if (upArrow != null) {
                    android.util.TypedValue typedValue = new android.util.TypedValue();
                    getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
                    int color = typedValue.data;
                    upArrow.setTint(color);
                    getSupportActionBar().setHomeAsUpIndicator(upArrow);
                }
            }
            toolbar.setTitle(subBookName != null ? subBookName : "Transactions");
        }
    }

    /**
     * Initialize ViewModel with factory pattern
     */
    private void initViewModel() {
        TransactionViewModelFactory factory = new TransactionViewModelFactory(getApplication(), subBookId);
        transactionViewModel = new ViewModelProvider(this, factory).get(TransactionViewModel.class);
        LogUtils.d(TAG, "TransactionViewModel initialized with subBookId: " + subBookId);
    }

    /**
     * Setup RecyclerView with adapter
     */
    private void setupRecyclerView() {
        transactionAdapter = new TransactionListAdapter(this, new TransactionListAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(Transaction transaction) {
                LogUtils.d(TAG, "Transaction clicked: " + transaction.getId());
                openEditTransactionDialog(transaction);
            }

            @Override
            public void onTransactionEditClick(Transaction transaction) {
                LogUtils.d(TAG, "Edit clicked for transaction: " + transaction.getId());
                openEditTransactionDialog(transaction);
            }

            @Override
            public void onTransactionDeleteClick(Transaction transaction) {
                LogUtils.d(TAG, "Delete clicked for transaction: " + transaction.getId());
                showDeleteConfirmationDialog(transaction);
            }
        });

        if (recyclerViewTransactions != null) {
            recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewTransactions.setAdapter(transactionAdapter);
        }
        LogUtils.d(TAG, "RecyclerView setup complete");
    }

    /**
     * Setup button listeners
     */
    private void setupListeners() {
        if (fabAddTransaction != null) {
            fabAddTransaction.setOnClickListener(v -> {
                // Tap feedback animation
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    openAddTransactionDialog();
                }).start();
            });
        }
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> openExportDialog());
        }
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> openFilterDialog());
        }
        LogUtils.d(TAG, "Listeners setup");
    }

    /**
     * Observe ViewModel data
     */
    private void observeViewModel() {
        // Observe parent book for currency
        transactionViewModel.getBookLiveData().observe(this, book -> {
            if (book != null) {
                this.currentBook = book;
                if (transactionAdapter != null) {
                    transactionAdapter.updateBook(book);
                }
                // Refresh balance displays
                updateBalance();
            }
        });

        // Observe all transactions for this sub-book
        transactionViewModel.getTransactions(subBookId).observe(this, transactionList -> {
            if (transactionList != null) {
                allTransactions = transactionList;
                applyFilters();

                // summaryHeader should ALWAYS be visible
                if (summaryHeader != null) {
                    summaryHeader.setVisibility(View.VISIBLE);
                }
            }
        });

        // Observe total debit
        transactionViewModel.getTotalDebit(subBookId).observe(this, debit -> {
            double debitAmount = (debit != null) ? debit : 0.0;
            if (tvTotalDebit != null) {
                tvTotalDebit.setText(CurrencyUtils.formatCurrency(debitAmount, currentBook));
            }
            updateBalance();
        });

        // Observe total credit
        transactionViewModel.getTotalCredit(subBookId).observe(this, credit -> {
            double creditAmount = (credit != null) ? credit : 0.0;
            if (tvTotalCredit != null) {
                tvTotalCredit.setText(CurrencyUtils.formatCurrency(creditAmount, currentBook));
            }
            updateBalance();
        });

        // Observe success messages
        transactionViewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                showSuccessMessage(message);
                transactionViewModel.clearSuccessMessage();
            }
        });

        // Observe error messages
        transactionViewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                showErrorMessage(message);
                transactionViewModel.clearErrorMessage();
            }
        });

        LogUtils.d(TAG, "ViewModel observers set up");
    }

    /**
     * Update balance calculation and display
     */
    private void updateBalance() {
        Double debit = transactionViewModel.getTotalDebit(subBookId).getValue();
        Double credit = transactionViewModel.getTotalCredit(subBookId).getValue();

        double debitAmount = (debit != null) ? debit : 0.0;
        double creditAmount = (credit != null) ? credit : 0.0;
        double balance = creditAmount - debitAmount;
        double totalVolume = creditAmount + debitAmount;

        if (tvBalance != null) {
            tvBalance.setText(CurrencyUtils.formatCurrency(balance, currentBook));
            tvBalance.setTextColor(Color.parseColor("#00D09C")); // Default to green for positive
            if (balance < 0)
                tvBalance.setTextColor(Color.parseColor("#FF5252"));
        }

        // Update Percentage and Bar
        if (totalVolume > 0) {
            int inPercent = (int) ((creditAmount / totalVolume) * 100);
            int outPercent = 100 - inPercent;

            if (textPercentIn != null)
                textPercentIn.setText("IN: " + inPercent + "%");
            if (textPercentOut != null)
                textPercentOut.setText("OUT: " + outPercent + "%");

            if (viewBarIn != null && viewBarOut != null) {
                LinearLayout.LayoutParams lpIn = (LinearLayout.LayoutParams) viewBarIn.getLayoutParams();
                lpIn.weight = inPercent;
                viewBarIn.setLayoutParams(lpIn);

                LinearLayout.LayoutParams lpOut = (LinearLayout.LayoutParams) viewBarOut.getLayoutParams();
                lpOut.weight = outPercent;
                viewBarOut.setLayoutParams(lpOut);
            }
        } else {
            if (textPercentIn != null)
                textPercentIn.setText("IN: 0%");
            if (textPercentOut != null)
                textPercentOut.setText("OUT: 0%");
            if (viewBarIn != null && viewBarOut != null) {
                LinearLayout.LayoutParams lpIn = (LinearLayout.LayoutParams) viewBarIn.getLayoutParams();
                lpIn.weight = 50;
                viewBarIn.setLayoutParams(lpIn);
                LinearLayout.LayoutParams lpOut = (LinearLayout.LayoutParams) viewBarOut.getLayoutParams();
                lpOut.weight = 50;
                viewBarOut.setLayoutParams(lpOut);
            }
        }
    }

    /**
     * Open add transaction dialog
     */
    private void openAddTransactionDialog() {
        AddTransactionDialog dialog = new AddTransactionDialog(this, transactionViewModel, subBookId);
        dialog.show();
        LogUtils.d(TAG, "AddTransactionDialog opened");
    }

    /**
     * Open edit transaction dialog
     */
    private void openEditTransactionDialog(Transaction transaction) {
        EditTransactionDialog dialog = new EditTransactionDialog(this, transactionViewModel, transaction);
        dialog.show();
        LogUtils.d(TAG, "EditTransactionDialog opened for transaction: " + transaction.getId());
    }

    /**
     * Show delete confirmation dialog
     */
    /**
     * Show delete confirmation dialog
     */
    private void showDeleteConfirmationDialog(Transaction transaction) {
        this.transactionToDelete = transaction;
        showConfirmationDialog(
                "Delete Transaction",
                "Are you sure you want to delete this transaction?\nAmount: "
                        + CurrencyUtils.formatCurrency(transaction.getAmount(), currentBook),
                "Delete",
                "Cancel");
    }

    @Override
    protected void onConfirmationYes() {
        try {
            if (transactionToDelete != null && transactionViewModel != null) {
                LogUtils.d(TAG, "Deleting transaction: " + transactionToDelete.getId());
                transactionViewModel.deleteTransaction(transactionToDelete);
                transactionToDelete = null; // Reset
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Error deleting transaction", e);
            showErrorMessage("Error deleting transaction");
        }
    }

    /**
     * Create menu options
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_transaction_details, menu);
        return true;
    }

    /**
     * Handle menu item selection
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_export) {
            LogUtils.d(TAG, "Export clicked");
            openExportDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openExportDialog() {
        List<Transaction> transactions = transactionViewModel.getTransactions(subBookId).getValue();
        if (transactions == null || transactions.isEmpty()) {
            showWarningMessage("No transactions to export");
            return;
        }

        ExportFormatSelectorDialog dialog = new ExportFormatSelectorDialog();
        dialog.setListener(format -> {
            boolean isWhiteLabel = FeatureManager.getInstance(this).hasWhiteLabel();
            File file = null;
            String mimeType = "*/*";

            showLoadingDialog("Generating " + format.toUpperCase() + "...");

            if ("pdf".equals(format)) {
                // Prepare Metadata for PDF
                String dateRange = "All Time";
                if (!transactions.isEmpty()) {
                    SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    Date minDate = transactions.get(0).getDate();
                    Date maxDate = transactions.get(0).getDate();
                    for (Transaction t : transactions) {
                        if (t.getDate() != null) {
                            if (minDate == null || t.getDate().before(minDate))
                                minDate = t.getDate();
                            if (maxDate == null || t.getDate().after(maxDate))
                                maxDate = t.getDate();
                        }
                    }
                    if (minDate != null && maxDate != null) {
                        dateRange = df.format(minDate) + " - " + df.format(maxDate);
                    }
                }

                String currency = (currentBook != null) ? currentBook.getCurrencyCode()
                        : CurrencyUtils.getDefaultCurrencyCode();
                String userName = new SessionManager(this).getUserName();

                file = ExportUtils.exportToPDF(this, transactions, subBookName, dateRange, currency, userName,
                        isWhiteLabel);
                mimeType = "application/pdf";
            } else if ("excel".equals(format)) {
                file = ExportUtils.exportToExcel(this, transactions, subBookName, isWhiteLabel);
                mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            } else if ("csv".equals(format)) {
                file = ExportUtils.exportToCSV(this, transactions, subBookName);
                mimeType = "text/csv";
            }

            hideLoadingDialog();

            if (file != null) {
                ExportUtils.shareFile(this, file, mimeType);
            } else {
                showErrorMessage("Failed to generate " + format);
            }
        });
        dialog.show(getSupportFragmentManager(), "ExportSelector");
    }

    private void openFilterDialog() {
        TransactionFilterBottomSheet filterSheet = TransactionFilterBottomSheet.newInstance(
                filterFromDate, filterToDate, filterPaymentMethod, filterPaymentApp);
        filterSheet.setListener(new TransactionFilterBottomSheet.OnFilterAppliedListener() {
            @Override
            public void onFilterApplied(Date fromDate, Date toDate, String paymentMethod, String paymentApp) {
                filterFromDate = fromDate;
                filterToDate = toDate;
                filterPaymentMethod = paymentMethod;
                filterPaymentApp = paymentApp;
                applyFilters();
            }

            @Override
            public void onFilterCleared() {
                filterFromDate = null;
                filterToDate = null;
                filterPaymentMethod = null;
                filterPaymentApp = null;
                applyFilters();
            }
        });
        filterSheet.show(getSupportFragmentManager(), "FilterBottomSheet");
    }

    private void applyFilters() {
        if (allTransactions == null) {
            allTransactions = new java.util.ArrayList<>();
        }

        List<Transaction> filteredList = new java.util.ArrayList<>();
        Calendar cal = Calendar.getInstance();

        for (Transaction t : allTransactions) {
            boolean matches = true;

            // Date Range Filter (AND logic)
            if (filterFromDate != null || filterToDate != null) {
                Date tDate = t.getDate();
                if (tDate == null) {
                    matches = false;
                } else {
                    // Reset time for comparison if needed, but simple comparison is usually enough
                    if (filterFromDate != null) {
                        cal.setTime(filterFromDate);
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        if (tDate.before(cal.getTime()))
                            matches = false;
                    }
                    if (matches && filterToDate != null) {
                        cal.setTime(filterToDate);
                        cal.set(Calendar.HOUR_OF_DAY, 23);
                        cal.set(Calendar.MINUTE, 59);
                        cal.set(Calendar.SECOND, 59);
                        cal.set(Calendar.MILLISECOND, 999);
                        if (tDate.after(cal.getTime()))
                            matches = false;
                    }
                }
            }

            // Payment Method Filter (AND logic)
            if (matches && (filterPaymentMethod != null || filterPaymentApp != null)) {
                String tMethod = t.getPaymentMethod();
                String tApp = t.getPaymentApp();

                if (filterPaymentApp != null) {
                    // Exact match for sub-method
                    if (!filterPaymentApp.equalsIgnoreCase(tApp))
                        matches = false;
                } else if (filterPaymentMethod != null) {
                    // Match for primary method (Category)
                    if (!filterPaymentMethod.equalsIgnoreCase(tMethod))
                        matches = false;
                }
            }

            if (matches) {
                filteredList.add(t);
            }
        }

        transactionAdapter.submitList(filteredList);

        // Handle empty filtered state
        if (allTransactions.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No Transactions Yet");
            recyclerViewTransactions.setVisibility(View.GONE);
        } else if (filteredList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No transactions match your filters");
            recyclerViewTransactions.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerViewTransactions.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        LogUtils.d(TAG, "Back pressed");
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.methodExit(TAG, "onDestroy");
    }

    /**
     * Factory for creating TransactionViewModel with subBookId
     */
    public static class TransactionViewModelFactory implements ViewModelProvider.Factory {
        private final android.app.Application application;
        private final long subBookId;

        public TransactionViewModelFactory(android.app.Application application, long subBookId) {
            this.application = application;
            this.subBookId = subBookId;
        }

        @NonNull
        @Override
        public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(TransactionViewModel.class)) {
                return (T) new TransactionViewModel(application, subBookId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}