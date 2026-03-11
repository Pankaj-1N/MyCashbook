package com.mycashbook.app.ui.book;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mycashbook.app.R;
import com.mycashbook.app.base.BaseActivity;
import com.mycashbook.app.model.SubBook;
import com.mycashbook.app.ui.subbook.AddSubBookDialog;
import com.mycashbook.app.ui.subbook.EditSubBookDialog;
import com.mycashbook.app.ui.subbook.SubBookListAdapter;
import com.mycashbook.app.ui.transaction.TransactionDetailsActivity;
import com.mycashbook.app.utils.LogUtils;
import com.mycashbook.app.viewmodel.SubBookViewModel;

/**
 * Book Details Activity - Refactored to extend BaseActivity
 * Displays sub-books for a specific book with error handling
 */
public class BookDetailsActivity extends BaseActivity {

    private static final String TAG = "BookDetailsActivity";
    private static final long INVALID_BOOK_ID = -1L;

    // UI Components
    private Toolbar toolbar;
    private RecyclerView recyclerViewSubBooks;
    private FloatingActionButton fabAddSubBook;
    private android.widget.TextView tvEmptyState;

    // Header UI Components
    private android.widget.TextView textTotalBalance;
    private android.widget.TextView textCashIn;
    private android.widget.TextView textCashOut;
    private android.widget.TextView textStatusBadge;
    private android.widget.TextView textPercentIn;
    private android.widget.TextView textPercentOut;
    private View viewBarIn;
    private View viewBarOut;
    private View fabRing;
    private android.widget.TextView tvSubBookCount;

    // ViewModel & Adapter
    private SubBookViewModel subBookViewModel;
    private SubBookListAdapter subBookAdapter;

    // Book Data
    private long bookId;
    private String bookName;
    private com.mycashbook.app.model.Book currentBook;
    private java.util.List<com.mycashbook.app.model.SubBookWithStats> pendingSubBooks; // Pending list until book loads

    // Stats for calculation
    private double currentIncome = 0.0;
    private double currentExpense = 0.0;
    private int transactionCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.methodEntry(TAG, "onCreate");

        setContentView(R.layout.activity_book_details);

        // Extract extras with validation
        if (!extractAndValidateExtras()) {
            return;
        }

        LogUtils.d(TAG, "Opened for: " + bookName + " (ID: " + bookId + ")");

        initViews();
        setupToolbar();
        initViewModel();
        setupRecyclerView();
        setupListeners();
        observeViewModel();
        handleBackPress();

        LogUtils.methodExit(TAG, "onCreate");
    }

    /**
     * Extract and validate intent extras
     */
    private boolean extractAndValidateExtras() {
        try {
            Intent intent = getIntent();
            if (intent == null) {
                LogUtils.e(TAG, "Intent is null");
                showErrorMessage("Error: Invalid request");
                finishActivity();
                return false;
            }

            bookId = intent.getLongExtra("bookId", INVALID_BOOK_ID);
            bookName = intent.getStringExtra("bookName");

            if (bookId == INVALID_BOOK_ID) {
                LogUtils.e(TAG, "Invalid bookId: " + bookId);
                showErrorMessage("Error: Invalid book");
                finishActivity();
                return false;
            }

            if (isEmpty(bookName)) {
                bookName = "Book Details";
                LogUtils.w(TAG, "Book name is empty, using default");
            }

            return true;
        } catch (Exception e) {
            LogUtils.e(TAG, "Error extracting extras", e);
            showErrorMessage("Error loading book details");
            finishActivity();
            return false;
        }
    }

    /**
     * Initialize UI views with null checks
     */
    private void initViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            recyclerViewSubBooks = findViewById(R.id.recyclerViewSubBooks);
            fabAddSubBook = findViewById(R.id.fabAddSubBook);
            tvEmptyState = findViewById(R.id.tvEmptyState);

            // Header Views
            textTotalBalance = findViewById(R.id.textTotalBalance);
            textCashIn = findViewById(R.id.textCashIn);
            textCashOut = findViewById(R.id.textCashOut);
            textStatusBadge = findViewById(R.id.textStatusBadge);
            textPercentIn = findViewById(R.id.textPercentIn);
            textPercentOut = findViewById(R.id.textPercentOut);
            viewBarIn = findViewById(R.id.viewBarIn);
            viewBarOut = findViewById(R.id.viewBarOut);
            fabRing = findViewById(R.id.fabRing);
            tvSubBookCount = findViewById(R.id.tvSubBookCount);

            startFabAnimation();

            // Validate critical views
            if (recyclerViewSubBooks == null) {
                LogUtils.w(TAG, "RecyclerView is null");
            }
            if (fabAddSubBook == null) {
                LogUtils.w(TAG, "FAB is null");
            }

            LogUtils.d(TAG, "Views initialized");
        } catch (Exception e) {
            LogUtils.e(TAG, "Error initializing views", e);
            showErrorMessage("Error loading interface");
        }
    }

    /**
     * Start continuous pulse animation for the FAB ring
     */
    private void startFabAnimation() {
        if (fabRing == null)
            return;

        android.view.animation.AnimationSet animationSet = new android.view.animation.AnimationSet(true);

        android.view.animation.ScaleAnimation scaleAnim = new android.view.animation.ScaleAnimation(
                1.0f, 1.4f,
                1.0f, 1.4f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnim.setDuration(1500);
        scaleAnim.setRepeatCount(android.view.animation.Animation.INFINITE);
        scaleAnim.setRepeatMode(android.view.animation.Animation.RESTART);

        android.view.animation.AlphaAnimation alphaAnim = new android.view.animation.AlphaAnimation(0.3f, 0.0f);
        alphaAnim.setDuration(1500);
        alphaAnim.setRepeatCount(android.view.animation.Animation.INFINITE);
        alphaAnim.setRepeatMode(android.view.animation.Animation.RESTART);

        animationSet.addAnimation(scaleAnim);
        animationSet.addAnimation(alphaAnim);

        fabRing.startAnimation(animationSet);
    }

    /**
     * Setup toolbar with validation
     */
    private void setupToolbar() {
        try {
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(bookName);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    LogUtils.d(TAG, "Toolbar setup: " + bookName);
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Error setting up toolbar", e);
        }
    }

    /**
     * Initialize ViewModel with error handling
     */
    private void initViewModel() {
        try {
            SubBookViewModelFactory factory = new SubBookViewModelFactory(getApplication(), bookId);
            subBookViewModel = new ViewModelProvider(this, factory).get(SubBookViewModel.class);
            LogUtils.d(TAG, "SubBookViewModel initialized with bookId: " + bookId);
        } catch (Exception e) {
            LogUtils.e(TAG, "Error initializing ViewModel", e);
            showErrorMessage("Error loading data");
            finishActivity();
        }
    }

    /**
     * Setup RecyclerView with error handling
     */
    private void setupRecyclerView() {
        try {
            if (recyclerViewSubBooks == null) {
                LogUtils.w(TAG, "RecyclerView is null, skipping setup");
                return;
            }

            subBookAdapter = new SubBookListAdapter(this, new SubBookListAdapter.OnSubBookClickListener() {
                @Override
                public void onSubBookClick(SubBook subBook) {
                    if (subBook != null) {
                        openTransactionDetails(subBook);
                    } else {
                        LogUtils.w(TAG, "SubBook is null on click");
                    }
                }

                @Override
                public void onSubBookEditClick(SubBook subBook) {
                    if (subBook != null) {
                        LogUtils.d(TAG, "Edit: " + subBook.getName());
                        showEditSubBookDialog(subBook);
                    }
                }

                @Override
                public void onSubBookDeleteClick(SubBook subBook) {
                    if (subBook != null) {
                        showDeleteConfirmationDialog(subBook);
                    }
                }
            });

            recyclerViewSubBooks.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewSubBooks.setAdapter(subBookAdapter);
            LogUtils.d(TAG, "RecyclerView initialized");
        } catch (Exception e) {
            LogUtils.e(TAG, "Error setting up RecyclerView", e);
            showErrorMessage("Error loading sub-books list");
        }
    }

    /**
     * Setup button listeners
     */
    private void setupListeners() {
        try {
            if (fabAddSubBook != null) {
                fabAddSubBook.setOnClickListener(v -> {
                    LogUtils.d(TAG, "FAB Add SubBook clicked");
                    openAddSubBookDialog();
                });
            }
            LogUtils.d(TAG, "Listeners setup");
        } catch (Exception e) {
            LogUtils.e(TAG, "Error setting up listeners", e);
        }
    }

    /**
     * Observe ViewModel data with error handling
     */
    private void observeViewModel() {
        try {
            if (subBookViewModel == null) {
                LogUtils.e(TAG, "ViewModel is null, cannot observe");
                return;
            }

            // Observe and update current book
            subBookViewModel.getBookLiveData().observe(this, book -> {
                if (book != null) {
                    this.currentBook = book;
                    setupToolbar(); // Update toolbar title if book name changed in DB

                    // If we have pending sub-books, submit them now that book is loaded
                    if (pendingSubBooks != null && subBookAdapter != null) {
                        subBookAdapter.updateBook(currentBook);
                        subBookAdapter.submitList(pendingSubBooks);
                        pendingSubBooks = null; // Clear pending
                    }

                    // Refresh current displays with correct symbol
                    if (subBookViewModel.getTotalBalance().getValue() != null) {
                        textTotalBalance.setText(com.mycashbook.app.utils.CurrencyUtils
                                .formatCurrency(subBookViewModel.getTotalBalance().getValue(), book));
                    }
                    if (subBookViewModel.getTotalIncome().getValue() != null) {
                        textCashIn.setText(com.mycashbook.app.utils.CurrencyUtils
                                .formatCurrency(subBookViewModel.getTotalIncome().getValue(), book));
                    }
                    if (subBookViewModel.getTotalExpense().getValue() != null) {
                        textCashOut.setText(com.mycashbook.app.utils.CurrencyUtils
                                .formatCurrency(subBookViewModel.getTotalExpense().getValue(), book));
                    }
                }
            });

            // Observe all sub-books (now SubBookWithStats)
            subBookViewModel.getAllSubBooks().observe(this, subBooks -> {
                try {
                    if (subBooks != null && !subBooks.isEmpty()) {
                        LogUtils.d(TAG, "SubBooks loaded: " + subBooks.size());
                        if (subBookAdapter != null) {
                            // Only submit list if currentBook is already loaded
                            if (currentBook != null) {
                                subBookAdapter.updateBook(currentBook);
                                subBookAdapter.submitList(subBooks);
                            } else {
                                // Store for later when book loads
                                pendingSubBooks = subBooks;
                                LogUtils.d(TAG, "Book not loaded yet, storing pending sub-books");
                            }
                        }
                        updateViewVisibility(true);
                        // Update sub-book count display
                        updateSubBookCount(subBooks.size());
                    } else {
                        LogUtils.d(TAG, "No sub-books found");
                        updateViewVisibility(false);
                        updateSubBookCount(0);
                    }
                } catch (Exception e) {
                    LogUtils.e(TAG, "Error updating sub-books", e);
                }
            });

            // Observe Stats - only update if book is loaded for correct currency
            subBookViewModel.getTotalBalance().observe(this, balance -> {
                if (textTotalBalance != null && currentBook != null) {
                    textTotalBalance.setText(
                            com.mycashbook.app.utils.CurrencyUtils.formatCurrency(balance != null ? balance : 0.0,
                                    currentBook));
                }
            });

            subBookViewModel.getTotalIncome().observe(this, income -> {
                this.currentIncome = income != null ? income : 0.0;
                if (textCashIn != null && currentBook != null) {
                    textCashIn
                            .setText(com.mycashbook.app.utils.CurrencyUtils.formatCurrency(currentIncome, currentBook));
                }
                updateBarsAndBadge();
            });

            subBookViewModel.getTotalExpense().observe(this, expense -> {
                this.currentExpense = expense != null ? expense : 0.0;
                if (textCashOut != null && currentBook != null) {
                    textCashOut.setText(
                            com.mycashbook.app.utils.CurrencyUtils.formatCurrency(currentExpense, currentBook));
                }
                updateBarsAndBadge();
            });

            // Observe success messages
            subBookViewModel.getSuccessMessage().observe(this, msg -> {
                if (isNotEmpty(msg)) {
                    showSuccessMessage(msg);
                    subBookViewModel.clearSuccessMessage();
                }
            });

            // Observe error messages
            subBookViewModel.getErrorMessage().observe(this, msg -> {
                if (isNotEmpty(msg)) {
                    showErrorMessage(msg);
                    subBookViewModel.clearErrorMessage();
                }
            });

            // Observe transaction count for locking currency
            subBookViewModel.getTransactionCount().observe(this, count -> {
                if (count != null) {
                    this.transactionCount = count;
                    LogUtils.d(TAG, "Transaction count updated: " + count);
                }
            });

            LogUtils.d(TAG, "ViewModel observers set up");
        } catch (Exception e) {
            LogUtils.e(TAG, "Error observing ViewModel", e);
        }
    }

    /**
     * Update the Green/Red bars, percentages, and status badge
     */
    private void updateBarsAndBadge() {
        try {
            double total = currentIncome + currentExpense;

            // Badge Logic
            if (textStatusBadge != null) {
                if (currentIncome >= currentExpense) {
                    textStatusBadge.setText("Surplus");
                    textStatusBadge.setTextColor(getColor(R.color.green_500));
                    textStatusBadge.setBackgroundResource(R.drawable.bg_badge_null); // Or green tint
                } else {
                    textStatusBadge.setText("Deficit");
                    textStatusBadge.setTextColor(getColor(R.color.red_500));
                }
            }

            // Percentage Bars Logic
            if (total > 0) {
                float inPercent = (float) ((currentIncome / total) * 100);
                float outPercent = (float) ((currentExpense / total) * 100);

                // Update text
                if (textPercentIn != null)
                    textPercentIn.setText(String.format("IN: %.0f%%", inPercent));
                if (textPercentOut != null)
                    textPercentOut.setText(String.format("OUT: %.0f%%", outPercent));

                // Update Bar Weights
                if (viewBarIn != null) {
                    android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) viewBarIn
                            .getLayoutParams();
                    params.weight = inPercent;
                    viewBarIn.setLayoutParams(params);
                }
                if (viewBarOut != null) {
                    android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) viewBarOut
                            .getLayoutParams();
                    params.weight = outPercent;
                    viewBarOut.setLayoutParams(params);
                }
            } else {
                // Default 50/50 empty state
                if (textPercentIn != null)
                    textPercentIn.setText("IN: 0%");
                if (textPercentOut != null)
                    textPercentOut.setText("OUT: 0%");

                if (viewBarIn != null) {
                    android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) viewBarIn
                            .getLayoutParams();
                    params.weight = 50;
                    viewBarIn.setLayoutParams(params);
                }
                if (viewBarOut != null) {
                    android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) viewBarOut
                            .getLayoutParams();
                    params.weight = 50;
                    viewBarOut.setLayoutParams(params);
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Error updating bars", e);
        }
    }

    /**
     * Update visibility of RecyclerView and empty state
     */
    private void updateViewVisibility(boolean hasItems) {
        try {
            if (recyclerViewSubBooks != null) {
                recyclerViewSubBooks.setVisibility(hasItems ? View.VISIBLE : View.GONE);
            }
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(hasItems ? View.GONE : View.VISIBLE);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Error updating view visibility", e);
        }
    }

    /**
     * Update the sub-book count display in the header
     */
    private void updateSubBookCount(int count) {
        try {
            if (tvSubBookCount != null) {
                String text = count == 1 ? "1 account" : count + " accounts";
                tvSubBookCount.setText(text);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Error updating sub-book count", e);
        }
    }

    /**
     * Open add sub-book dialog
     */
    private void openAddSubBookDialog() {
        try {
            if (subBookViewModel == null) {
                showErrorMessage("Error: Data not loaded");
                return;
            }
            AddSubBookDialog dialog = new AddSubBookDialog(this, subBookViewModel);
            dialog.show();
            LogUtils.d(TAG, "AddSubBookDialog opened");
        } catch (Exception e) {
            LogUtils.e(TAG, "Error opening add dialog", e);
            showErrorMessage("Error opening dialog");
        }
    }

    /**
     * Open edit sub-book dialog
     */
    private void showEditSubBookDialog(SubBook subBook) {
        try {
            if (subBookViewModel == null) {
                showErrorMessage("Error: Data not loaded");
                return;
            }
            EditSubBookDialog dialog = new EditSubBookDialog(this, subBookViewModel, subBook);
            dialog.show();
            LogUtils.d(TAG, "EditSubBookDialog opened");
        } catch (Exception e) {
            LogUtils.e(TAG, "Error opening edit dialog", e);
            showErrorMessage("Error opening dialog");
        }
    }

    private long lastClickTime = 0;

    /**
     * Open transaction details for a sub-book
     */
    private void openTransactionDetails(SubBook subBook) {
        try {
            // Debounce click (prevent double opening)
            if (System.currentTimeMillis() - lastClickTime < 1000) {
                return;
            }
            lastClickTime = System.currentTimeMillis();

            if (subBook == null || subBook.getId() <= 0) {
                LogUtils.e(TAG, "Invalid subBook");
                showErrorMessage("Error: Invalid sub-book");
                return;
            }

            Intent intent = new Intent(BookDetailsActivity.this, TransactionDetailsActivity.class);
            intent.putExtra("subBookId", subBook.getId());
            intent.putExtra("subBookName", subBook.getName());
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // Prevent multiple instances
            startActivity(intent);
            LogUtils.d(TAG, "Opening TransactionDetailsActivity: " + subBook.getName());
        } catch (Exception e) {
            LogUtils.e(TAG, "Error opening transaction details", e);
            showErrorMessage("Error opening transactions");
        }
    }

    /**
     * Show delete confirmation dialog
     */
    private void showDeleteConfirmationDialog(SubBook subBook) {
        try {
            if (subBook == null) {
                LogUtils.e(TAG, "SubBook is null for delete");
                return;
            }

            showConfirmationDialog(
                    "Delete Sub-Book",
                    "Delete \"" + subBook.getName() + "\"?",
                    "Delete",
                    "Cancel");

            // Store the subBook for deletion if confirmed
            this.lastSelectedSubBook = subBook;
        } catch (Exception e) {
            LogUtils.e(TAG, "Error showing delete dialog", e);
        }
    }

    private SubBook lastSelectedSubBook;

    @Override
    protected void onConfirmationYes() {
        try {
            if (lastSelectedSubBook != null && subBookViewModel != null) {
                LogUtils.d(TAG, "Deleting sub-book: " + lastSelectedSubBook.getName());
                subBookViewModel.deleteSubBook(lastSelectedSubBook);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Error deleting sub-book", e);
            showErrorMessage("Error deleting sub-book");
        }
    }

    /**
     * Create options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_book_details, menu);
            return true;
        } catch (Exception e) {
            LogUtils.e(TAG, "Error creating menu", e);
            return false;
        }
    }

    /**
     * Handle menu item selection
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            int id = item.getItemId();

            if (id == android.R.id.home) {
                onBackPressed();
                return true;
            } else if (id == R.id.menu_edit) {
                if (currentBook != null) {
                    EditBookDialog dialog = new EditBookDialog(this,
                            new ViewModelProvider(this).get(com.mycashbook.app.viewmodel.HomeViewModel.class),
                            currentBook);
                    dialog.setTransactionCount(transactionCount);
                    dialog.show();
                }
                return true;
            } else if (id == R.id.menu_delete) {
                showToast("Delete Book clicked");
                return true;
            } else if (id == R.id.action_settings) {
                showToast("Settings clicked");
                return true;
            }

            return super.onOptionsItemSelected(item);
        } catch (Exception e) {
            LogUtils.e(TAG, "Error handling menu selection", e);
            return false;
        }
    }

    /**
     * Handle back press
     */
    private void handleBackPress() {
        try {
            getOnBackPressedDispatcher().addCallback(this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            LogUtils.d(TAG, "Back pressed");
                            finishActivity();
                        }
                    });
        } catch (Exception e) {
            LogUtils.e(TAG, "Error handling back press", e);
        }
    }

    /**
     * ViewModel Factory with error handling
     */
    public static class SubBookViewModelFactory implements ViewModelProvider.Factory {
        private final android.app.Application application;
        private final long bookId;

        public SubBookViewModelFactory(android.app.Application application, long bookId) {
            this.application = application;
            this.bookId = bookId;
        }

        @NonNull
        @Override
        public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
            try {
                if (modelClass.isAssignableFrom(SubBookViewModel.class)) {
                    return (T) new SubBookViewModel(application, bookId);
                }
                throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
            } catch (Exception e) {
                LogUtils.e("SubBookViewModelFactory", "Error creating ViewModel", e);
                throw e;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.methodExit(TAG, "onDestroy");
    }
}