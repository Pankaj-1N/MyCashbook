package com.mycashbook.app.billing;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.mycashbook.app.R;
import com.mycashbook.app.utils.FeatureManager;

import java.util.ArrayList;
import java.util.List;

/**
 * SubscriptionActivity - Displays subscription plans with Monthly/Lifetime
 * toggle
 * GLITCH PREVENTION:
 * - Null safety for all views
 * - Proper adapter initialization
 * - Feature gating integration
 * - Error handling for billing operations
 */
public class SubscriptionActivity extends AppCompatActivity {

    private static final String TAG = "SubscriptionActivity";

    // Views
    private ImageButton btnBack;
    private MaterialButton btnMonthly;
    private MaterialButton btnLifetime;
    private RecyclerView recyclerPlans;

    // Managers
    private FeatureManager featureManager;
    private BillingManager billingManager;

    // Adapter
    private SubscriptionPlanAdapter adapter;
    private boolean isMonthlySelected = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        // Initialize managers
        featureManager = FeatureManager.getInstance(this);
        billingManager = BillingManager.getInstance(this);

        initViews();
        setupToggleButtons();
        setupRecyclerView();
        loadPlans();
    }

    /**
     * Initialize all views
     * GLITCH PREVENTION: Null checks for all views
     */
    private void initViews() {
        try {
            btnBack = findViewById(R.id.btnBack);
            btnMonthly = findViewById(R.id.btnMonthly);
            btnLifetime = findViewById(R.id.btnLifetime);
            recyclerPlans = findViewById(R.id.recyclerPlans);

            // Back button
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            Toast.makeText(this, "Error loading subscription plans", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Setup Monthly/Lifetime toggle buttons
     * GLITCH PREVENTION: Proper state management
     */
    private void setupToggleButtons() {
        if (btnMonthly != null) {
            btnMonthly.setOnClickListener(v -> {
                isMonthlySelected = true;
                updateToggleState();
                loadPlans();
            });
        }

        if (btnLifetime != null) {
            btnLifetime.setOnClickListener(v -> {
                isMonthlySelected = false;
                updateToggleState();
                loadPlans();
            });
        }

        // Set initial state
        updateToggleState();
    }

    /**
     * Update toggle button visual state
     * GLITCH PREVENTION: Null checks before updating
     */
    private void updateToggleState() {
        if (btnMonthly != null && btnLifetime != null) {
            if (isMonthlySelected) {
                btnMonthly.setBackgroundTintList(getColorStateList(R.color.cyan));
                btnMonthly.setTextColor(getColor(android.R.color.white));
                btnLifetime.setBackgroundTintList(getColorStateList(R.color.card_background));
                btnLifetime.setTextColor(getColor(R.color.text_secondary));
            } else {
                btnLifetime.setBackgroundTintList(getColorStateList(R.color.cyan));
                btnLifetime.setTextColor(getColor(android.R.color.white));
                btnMonthly.setBackgroundTintList(getColorStateList(R.color.card_background));
                btnMonthly.setTextColor(getColor(R.color.text_secondary));
            }
        }
    }

    /**
     * Setup RecyclerView with adapter
     * GLITCH PREVENTION: Proper layout manager and adapter initialization
     */
    private void setupRecyclerView() {
        if (recyclerPlans != null) {
            recyclerPlans.setLayoutManager(new LinearLayoutManager(this));
            adapter = new SubscriptionPlanAdapter(this, new ArrayList<>(), plan -> {
                // Handle plan selection
                handlePlanSelection(plan);
            });
            recyclerPlans.setAdapter(adapter);
        }
    }

    /**
     * Load subscription plans based on selected period
     * GLITCH PREVENTION: Creates safe plan data
     */
    private void loadPlans() {
        try {
            List<SubscriptionPlan> plans = new ArrayList<>();
            String currentPlan = featureManager.getCurrentPlan();

            if (isMonthlySelected) {
                // Monthly Plans
                plans.add(createPlan("FREE", "Free", 0, "month",
                        new String[] {
                                "Up to 3 main books",
                                "Basic tracking",
                                "Manual backup",
                                "CSV export",
                                "Ad-supported"
                        },
                        currentPlan.equals(FeatureManager.PLAN_FREE),
                        false,
                        "#6B7A8F"));

                plans.add(createPlan("BASIC", "Basic", 29, "month",
                        new String[] {
                                "Unlimited books",
                                "Full tracking",
                                "Google Drive backup",
                                "CSV & Excel export",
                                "No ads",
                                "Email support"
                        },
                        currentPlan.equals(FeatureManager.PLAN_BASIC),
                        true, // MOST POPULAR
                        "#00B4D8"));

                plans.add(createPlan("PREMIUM", "Premium", 59, "month",
                        new String[] {
                                "All Basic features",
                                "Advanced analytics",
                                "PDF export",
                                "Multi-currency",
                                "Payment reminders",
                                "Priority support",
                                "Custom categories"
                        },
                        currentPlan.equals(FeatureManager.PLAN_PREMIUM),
                        false,
                        "#9D4EDD"));

                plans.add(createPlan("BUSINESS", "Business", 149, "month",
                        new String[] {
                                "All Premium features",
                                "Multiple users",
                                "Team collaboration",
                                "Advanced security",
                                "Custom export templates",
                                "API access",
                                "Dedicated account manager",
                                "White-label options"
                        },
                        currentPlan.equals(FeatureManager.PLAN_BUSINESS),
                        false,
                        "#FF6B35"));
            } else {
                // Lifetime Plans (one-time payment)
                plans.add(createPlan("BASIC_LIFETIME", "Basic Lifetime", 499, "one-time",
                        new String[] {
                                "All Basic features",
                                "Lifetime access",
                                "One-time payment",
                                "No recurring fees"
                        },
                        false,
                        false,
                        "#00B4D8"));

                plans.add(createPlan("PREMIUM_LIFETIME", "Premium Lifetime", 999, "one-time",
                        new String[] {
                                "All Premium features",
                                "Lifetime access",
                                "One-time payment",
                                "Best value"
                        },
                        false,
                        true, // BEST VALUE
                        "#9D4EDD"));

                plans.add(createPlan("BUSINESS_LIFETIME", "Business Lifetime", 2499, "one-time",
                        new String[] {
                                "All Business features",
                                "Lifetime access",
                                "One-time payment",
                                "Maximum savings"
                        },
                        false,
                        false,
                        "#FF6B35"));
            }

            if (adapter != null) {
                adapter.updatePlans(plans);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading plans", e);
            Toast.makeText(this, "Error loading plans", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper to create subscription plan
     * GLITCH PREVENTION: Safe object creation
     */
    private SubscriptionPlan createPlan(String id, String name, int price, String period,
            String[] features, boolean isCurrent, boolean isPopular,
            String color) {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.id = id;
        plan.name = name;
        plan.price = price;
        plan.period = period;
        plan.features = features;
        plan.isCurrent = isCurrent;
        plan.isPopular = isPopular;
        plan.color = color;
        return plan;
    }

    /**
     * Handle plan selection and purchase
     * GLITCH PREVENTION: Validates plan before purchase
     */
    private void handlePlanSelection(SubscriptionPlan plan) {
        try {
            if (plan == null) {
                Log.e(TAG, "Plan is null");
                return;
            }

            if (plan.isCurrent) {
                Toast.makeText(this, "This is your current plan", Toast.LENGTH_SHORT).show();
                return;
            }

            if (plan.id.equals("FREE")) {
                // Downgrade to free
                featureManager.setCurrentPlan(FeatureManager.PLAN_FREE);
                Toast.makeText(this, "Switched to Free plan", Toast.LENGTH_SHORT).show();
                loadPlans(); // Refresh to show current plan
                return;
            }

            // TODO: Implement actual billing flow
            Toast.makeText(this, "Purchase flow for " + plan.name + " coming soon", Toast.LENGTH_LONG).show();

            // For testing, you can uncomment this to simulate purchase:
            // simulatePurchase(plan);
        } catch (Exception e) {
            Log.e(TAG, "Error handling plan selection", e);
            Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Simulate purchase for testing (remove in production)
     * GLITCH PREVENTION: Safe plan mapping
     */
    private void simulatePurchase(SubscriptionPlan plan) {
        String featurePlan = FeatureManager.PLAN_FREE;

        if (plan.id.contains("BASIC")) {
            featurePlan = FeatureManager.PLAN_BASIC;
        } else if (plan.id.contains("PREMIUM")) {
            featurePlan = FeatureManager.PLAN_PREMIUM;
        } else if (plan.id.contains("BUSINESS")) {
            featurePlan = FeatureManager.PLAN_BUSINESS;
        }

        featureManager.setCurrentPlan(featurePlan);
        Toast.makeText(this, "Upgraded to " + plan.name + "!", Toast.LENGTH_LONG).show();
        loadPlans(); // Refresh to show current plan
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up adapter
        if (adapter != null) {
            adapter = null;
        }
    }
}
