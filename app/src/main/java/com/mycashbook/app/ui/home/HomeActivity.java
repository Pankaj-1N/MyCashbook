package com.mycashbook.app.ui.home;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mycashbook.app.R;
import com.mycashbook.app.auth.LoginActivity;
import com.mycashbook.app.base.BaseActivity;
import com.mycashbook.app.model.Book;
import com.mycashbook.app.model.BookWithBalance;
import com.mycashbook.app.ui.book.AddBookDialog;
import com.mycashbook.app.ui.book.BookDetailsActivity;
import com.mycashbook.app.ui.backup.BackupActivity;
import com.mycashbook.app.ui.book.EditBookDialog;
import com.mycashbook.app.ui.settings.SettingsActivity;
import com.mycashbook.app.utils.LogUtils;
import com.mycashbook.app.utils.SessionManager;
import com.mycashbook.app.viewmodel.HomeViewModel;

import java.util.List;

/**
 * Home Activity - Refactored to extend BaseActivity
 * Displays list of books and manages navigation
 */
public class HomeActivity extends BaseActivity {

    private static final String TAG = "HomeActivity";

    private HomeViewModel homeViewModel;
    private SessionManager sessionManager;

    private RecyclerView recyclerView;
    private BookListAdapter adapter;
    private View emptyState;
    private Button btnCreateFirst;
    private FloatingActionButton fabAdd;
    private View fabSettings; // Changed to View (it's an ImageView in XML)
    private TextView textBookCount; // Added TextView for book count
    private TextView textTotalBooksCount; // Added for stats card
    private View fabRing;
    private AdView adView;

    private boolean isFreePlan = !com.mycashbook.app.utils.FeatureManager.isTestingMode();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.methodEntry(TAG, "onCreate");

        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);
        initializeAds();
        initViewModels();
        initViews();
        observeData();
        setupListeners();

        LogUtils.methodExit(TAG, "onCreate");
    }

    /**
     * Initialize Google Mobile Ads
     */
    private void initializeAds() {
        try {
            MobileAds.initialize(this, initializationStatus -> {
                LogUtils.d(TAG, "AdMob initialized");
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "Error initializing ads", e);
        }
    }

    /**
     * Initialize ViewModels
     */
    private void initViewModels() {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        LogUtils.d(TAG, "ViewModels initialized");
    }

    /**
     * Initialize UI views
     */
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_books);
        emptyState = findViewById(R.id.empty_state);
        btnCreateFirst = findViewById(R.id.btnCreateFirst);
        fabAdd = findViewById(R.id.fab_add);
        fabSettings = findViewById(R.id.fab_settings); // This is an ImageView
        textBookCount = findViewById(R.id.textBookCount); // Initialize TextView
        textTotalBooksCount = findViewById(R.id.textTotalBooksCount); // Initialize stats count
        fabRing = findViewById(R.id.fabRing);
        adView = findViewById(R.id.adView);

        startFabAnimation();

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            recyclerView.setHasFixedSize(false);

            adapter = new BookListAdapter(this, new BookListAdapter.OnBookClickListener() {
                @Override
                public void onBookClick(Book book) {
                    onBookClicked(book);
                }

                @Override
                public void onBookEdit(Book book) {
                    showEditBookDialog(book);
                }

                @Override
                public void onBookDelete(Book book) {
                    showDeleteConfirmationWithCountdown(book);
                }
            });

            recyclerView.setAdapter(adapter);
            recyclerView.setItemAnimator(null);
        }

        LogUtils.d(TAG, "Views initialized");
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
     * Observe ViewModel data
     */
    private void observeData() {
        homeViewModel.getAllBooks().observe(this, this::updateBookList);

        homeViewModel.getLoading().observe(this, isLoading -> {
            if (isLoading)
                LogUtils.d(TAG, "Loading...");
        });

        homeViewModel.getErrorMessage().observe(this, errorMsg -> {
            if (isNotEmpty(errorMsg)) {
                showErrorMessage(errorMsg);
                homeViewModel.clearErrorMessage();
            }
        });

        homeViewModel.getSuccessMessage().observe(this, successMsg -> {
            if (isNotEmpty(successMsg)) {
                showSuccessMessage(successMsg);
                homeViewModel.clearSuccessMessage();
            }
        });

        // Observe manual restart trigger for instant UI updates (like theme switch)
        homeViewModel.getActivityRestartEvent().observe(this, shouldRestart -> {
            if (shouldRestart != null && shouldRestart) {
                LogUtils.d(TAG, "Restart event received - recreating activity");
                homeViewModel.clearRestartEvent();
                if (!isFinishing()) {
                    recreate();
                }
            }
        });

        homeViewModel.getBookCount().observe(this, count -> {
            LogUtils.d(TAG, "Current book count: " + count);

            // Update book count text
            if (textBookCount != null) {
                String countText = count + (count == 1 ? " Book" : " Books");
                textBookCount.setText(countText);
            }

            // Update stats card count
            if (textTotalBooksCount != null) {
                textTotalBooksCount.setText(String.valueOf(count));
            }

            updateAdVisibility();
            updateFabVisibility(count);
        });

        LogUtils.d(TAG, "Data observers set up");
    }

    /**
     * Update book list when data changes
     */
    private void updateBookList(List<BookWithBalance> books) {
        LogUtils.d(TAG, "updateBookList called with " + (books == null ? "null" : books.size() + " books"));

        if (books == null || books.isEmpty()) {
            if (emptyState != null) {
                emptyState.setVisibility(View.VISIBLE);
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(View.GONE);
            }

            Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            if (emptyState != null) {
                emptyState.startAnimation(fadeIn);
            }

            LogUtils.d(TAG, "No books found - showing empty state");
        } else {
            if (emptyState != null) {
                emptyState.setVisibility(View.GONE);
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(View.VISIBLE);

                adapter.submitList(books, () -> {
                    LogUtils.d(TAG, "Books submitted to adapter, count: " + books.size());
                    recyclerView.requestLayout();
                });
            }

            LogUtils.d(TAG, "Books loaded: " + books.size());
        }
    }

    /**
     * Update FAB visibility based on book count
     */
    private void updateFabVisibility(int bookCount) {
        if (fabAdd != null) {
            fabAdd.setVisibility(bookCount > 0 ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Update ad visibility based on plan
     */
    private void updateAdVisibility() {
        if (adView == null)
            return;

        if (isFreePlan) {
            adView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
            LogUtils.d(TAG, "Ads enabled (FREE plan)");
        } else {
            adView.setVisibility(View.GONE);
            LogUtils.d(TAG, "Ads disabled (Premium plan)");
        }
    }

    /**
     * Setup button click listeners
     */
    private void setupListeners() {
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                animateFabClick();
                openAddBookDialog();
            });
        }

        if (fabSettings != null) {
            fabSettings.setOnClickListener(v -> {
                startActivity(new Intent(this, SettingsActivity.class));
            });
        }

        if (btnCreateFirst != null) {
            btnCreateFirst.setOnClickListener(v -> openAddBookDialog());
        }

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        showExitConfirmation();
                    }
                });
    }

    /**
     * Animate FAB click
     */
    private void animateFabClick() {
        if (fabAdd != null) {
            fabAdd.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        fabAdd.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                    })
                    .start();
        }
    }

    /**
     * Open add book dialog
     */
    private void openAddBookDialog() {
        homeViewModel.canAddBook(isFreePlan, canAdd -> {
            runOnUiThread(() -> {
                if (canAdd) {
                    AddBookDialog dialog = new AddBookDialog(this, homeViewModel, isFreePlan);
                    dialog.show();
                } else {
                    showUpgradeDialog();
                }
            });
        });
    }

    /**
     * Handle book click - navigate to details
     */
    /**
     * Handle book click - check lock status and navigate
     */
    private void onBookClicked(Book book) {
        if (book.isLocked()) {
            com.mycashbook.app.ui.book.UnlockBookDialog dialog = new com.mycashbook.app.ui.book.UnlockBookDialog(
                    this, book, () -> {
                        // Unlock success
                        LogUtils.d(TAG, "Book unlocked: " + book.getName());
                        openBookDetails(book);
                    });
            dialog.show();
        } else {
            openBookDetails(book);
        }
    }

    /**
     * Navigate to book details
     */
    private void openBookDetails(Book book) {
        LogUtils.d(TAG, "Opening book details: " + book.getName() + " (ID: " + book.getId() + ")");
        Intent intent = new Intent(this, BookDetailsActivity.class);
        intent.putExtra("bookId", book.getId());
        startActivity(intent);
    }

    /**
     * Handle book edit
     */
    /**
     * Handle book edit
     */
    private void showEditBookDialog(Book book) {
        LogUtils.d(TAG, "Edit book: " + book.getName());
        // Observe once to get current count
        homeViewModel.getTransactionCountForBook(book.getId()).observe(this,
                new androidx.lifecycle.Observer<Integer>() {
                    @Override
                    public void onChanged(Integer count) {
                        if (count != null) {
                            EditBookDialog dialog = new EditBookDialog(HomeActivity.this, homeViewModel, book);
                            dialog.setTransactionCount(count);
                            dialog.show();
                            // Remove observer to avoid re-opening if count changes
                            homeViewModel.getTransactionCountForBook(book.getId()).removeObserver(this);
                        }
                    }
                });
    }

    /**
     * Show delete confirmation dialog with countdown
     */
    private void showDeleteConfirmationWithCountdown(Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_countdown, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView textTitle = dialogView.findViewById(R.id.textTitle);
        TextView textMessage = dialogView.findViewById(R.id.textMessage);
        TextView textCountdown = dialogView.findViewById(R.id.textCountdown);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);

        if (textTitle != null)
            textTitle.setText("Delete Book?");
        if (textMessage != null)
            textMessage.setText("'" + book.getName() + "' and all its data will be permanently deleted.");

        if (btnDelete != null) {
            btnDelete.setEnabled(false);
            btnDelete.setAlpha(0.5f);
        }

        final int[] countdown = { 5 };
        Handler handler = new Handler();
        Runnable countdownRunnable = new Runnable() {
            @Override
            public void run() {
                if (countdown[0] > 0) {
                    if (textCountdown != null) {
                        textCountdown.setText("You can delete in " + countdown[0] + " seconds");
                    }
                    countdown[0]--;
                    handler.postDelayed(this, 1000);
                } else {
                    if (textCountdown != null) {
                        textCountdown.setText("You can now delete this book");
                        textCountdown.setTextColor(getResources().getColor(R.color.success, null));
                    }
                    if (btnDelete != null) {
                        btnDelete.setEnabled(true);
                        btnDelete.setAlpha(1f);
                    }
                }
            }
        };
        handler.post(countdownRunnable);

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                handler.removeCallbacks(countdownRunnable);
                dialog.dismiss();
            });
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                handler.removeCallbacks(countdownRunnable);
                homeViewModel.deleteBook(book);
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    /**
     * Show upgrade to premium dialog
     */
    private void showUpgradeDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Upgrade to Premium")
                .setMessage(
                        "You've reached the 5-book limit for FREE users.\n\nUpgrade to Premium for unlimited books!")
                .setPositiveButton("Upgrade", (dialog, which) -> showToast("Billing coming in Phase 4!"))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Show exit confirmation dialog
     */
    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App?")
                .setMessage("Are you sure you want to exit MyCashBook?")
                .setPositiveButton("Exit", (dialog, which) -> finishAffinity())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Show logout confirmation dialog
     */
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout?")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    sessionManager.logout();
                    showSuccessMessage("Logged out successfully");
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finishActivity();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Create menu options
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    /**
     * Handle menu item selection
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;

        } else if (id == R.id.menu_backup) {
            startActivity(new Intent(this, BackupActivity.class));
            return true;

        } else if (id == R.id.menu_upgrade) {
            showToast("Billing coming in Phase 4!");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Setup transparent status bar
     */

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null)
            adView.resume();
        homeViewModel.refreshBookCount();
        LogUtils.methodExit(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        if (adView != null)
            adView.pause();
        super.onPause();
        LogUtils.methodExit(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        if (adView != null)
            adView.destroy();
        super.onDestroy();
        LogUtils.methodExit(TAG, "onDestroy");
    }
}