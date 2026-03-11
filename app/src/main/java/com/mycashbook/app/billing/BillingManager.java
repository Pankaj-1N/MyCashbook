package com.mycashbook.app.billing;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.mycashbook.app.utils.FeatureManager;

import java.util.ArrayList;
import java.util.List;

/**
 * BillingManager - Handles Google Play Billing with lifecycle awareness
 * GLITCH PREVENTION:
 * - Lifecycle observer to prevent memory leaks
 * - Null safety checks throughout
 * - Proper connection state management
 * - Automatic reconnection on disconnect
 */
public class BillingManager implements PurchasesUpdatedListener, LifecycleObserver {

    private static final String TAG = "BillingManager";
    private static BillingManager instance;
    private final BillingClient billingClient;
    private final Context context;
    private boolean isConnected = false;
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 3;

    // Listener interface to send updates back to ViewModel/Activity
    public interface BillingUpdatesListener {
        void onBillingClientSetupFinished();

        void onPurchasesUpdated(List<Purchase> purchases);

        void onProductDetailsLoaded(List<ProductDetails> productDetailsList);

        void onPurchaseError(String message);
    }

    private BillingUpdatesListener updatesListener;

    private BillingManager(Context context) {
        this.context = context.getApplicationContext();
        billingClient = BillingClient.newBuilder(this.context)
                .setListener(this)
                .enablePendingPurchases()
                .build();
    }

    public static synchronized BillingManager getInstance(Context context) {
        if (instance == null) {
            instance = new BillingManager(context);
        }
        return instance;
    }

    public void setUpdatesListener(BillingUpdatesListener listener) {
        this.updatesListener = listener;
    }

    /**
     * Get current subscription status from FeatureManager
     * GLITCH PREVENTION: Null-safe, uses encrypted storage
     */
    public boolean getSubscriptionStatus() {
        try {
            String plan = FeatureManager.getInstance(context).getCurrentPlan();
            return !FeatureManager.PLAN_FREE.equals(plan);
        } catch (Exception e) {
            Log.e(TAG, "Error getting subscription status", e);
            return false;
        }
    }

    /**
     * Check if billing client is ready
     * GLITCH PREVENTION: Prevents crashes from calling methods on disconnected
     * client
     */
    public boolean isReady() {
        return billingClient != null && isConnected;
    }

    // FIX 3: Added stub for starting purchase (Called by ViewModel)
    public void startPurchaseFlow(Object plan) {
        // This will eventually take a BillingPlan and call launchPurchaseFlow
        Log.d(TAG, "Start purchase flow requested");
    }

    /**
     * Start billing connection with automatic retry
     * GLITCH PREVENTION: Handles disconnections gracefully
     */
    public void startConnection() {
        if (billingClient == null) {
            Log.e(TAG, "BillingClient is null, cannot start connection");
            return;
        }

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    isConnected = true;
                    reconnectAttempts = 0;
                    Log.d(TAG, "Billing Setup Finished");

                    // Query existing purchases on connection
                    queryExistingPurchases();

                    if (updatesListener != null) {
                        updatesListener.onBillingClientSetupFinished();
                    }
                } else {
                    isConnected = false;
                    Log.e(TAG, "Billing Setup Failed: " + billingResult.getDebugMessage());
                    if (updatesListener != null) {
                        updatesListener.onPurchaseError("Billing setup failed: " + billingResult.getDebugMessage());
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                isConnected = false;
                Log.w(TAG, "Billing Service Disconnected");

                // Automatic reconnection with exponential backoff
                if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                    reconnectAttempts++;
                    long delay = (long) Math.pow(2, reconnectAttempts) * 1000; // 2s, 4s, 8s
                    Log.d(TAG, "Attempting reconnection #" + reconnectAttempts + " in " + delay + "ms");

                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                            () -> startConnection(),
                            delay);
                } else {
                    Log.e(TAG, "Max reconnection attempts reached");
                    if (updatesListener != null) {
                        updatesListener.onPurchaseError("Unable to connect to billing service");
                    }
                }
            }
        });
    }

    // 2. Query Available Products (Subscriptions)
    public void queryProducts(List<String> productIds) {
        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        for (String id : productIds) {
            productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(id)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build());
        }

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, list) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                if (updatesListener != null) {
                    updatesListener.onProductDetailsLoaded(list);
                }
            } else {
                Log.e(TAG, "Error querying products: " + billingResult.getDebugMessage());
            }
        });
    }

    // 3. Launch Purchase Flow
    public void launchPurchaseFlow(Activity activity, ProductDetails productDetails, String offerToken) {
        List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();

        productDetailsParamsList.add(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build());

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        billingClient.launchBillingFlow(activity, billingFlowParams);
    }

    // 4. Handle Purchase Updates
    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
            if (updatesListener != null) {
                updatesListener.onPurchasesUpdated(purchases);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled purchase");
        } else {
            Log.e(TAG, "Purchase error: " + billingResult.getDebugMessage());
            if (updatesListener != null) {
                updatesListener.onPurchaseError(billingResult.getDebugMessage());
            }
        }
    }

    /**
     * Handle purchase and update FeatureManager
     * GLITCH PREVENTION: Validates purchase state, handles acknowledgment errors
     */
    private void handlePurchase(Purchase purchase) {
        if (purchase == null) {
            Log.e(TAG, "Purchase is null");
            return;
        }

        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // Update FeatureManager with new plan
            updatePlanFromPurchase(purchase);

            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

                billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Purchase Acknowledged");
                    } else {
                        Log.e(TAG, "Failed to acknowledge purchase: " + billingResult.getDebugMessage());
                    }
                });
            }
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            Log.d(TAG, "Purchase is pending");
        }
    }

    /**
     * Update FeatureManager based on purchased product
     * GLITCH PREVENTION: Validates product IDs, handles errors gracefully
     */
    private void updatePlanFromPurchase(Purchase purchase) {
        try {
            List<String> products = purchase.getProducts();
            if (products == null || products.isEmpty()) {
                Log.e(TAG, "No products in purchase");
                return;
            }

            String productId = products.get(0);
            String plan = FeatureManager.PLAN_FREE;

            // Map product IDs to plans
            if (productId.contains("basic")) {
                plan = FeatureManager.PLAN_BASIC;
            } else if (productId.contains("premium")) {
                plan = FeatureManager.PLAN_PREMIUM;
            } else if (productId.contains("business")) {
                plan = FeatureManager.PLAN_BUSINESS;
            }

            FeatureManager.getInstance(context).setCurrentPlan(plan);
            Log.d(TAG, "Updated plan to: " + plan);
        } catch (Exception e) {
            Log.e(TAG, "Error updating plan from purchase", e);
        }
    }

    /**
     * Check existing purchases and update FeatureManager
     * GLITCH PREVENTION: Validates connection state, handles empty lists
     */
    public void queryExistingPurchases() {
        if (!isReady()) {
            Log.w(TAG, "BillingClient not ready, cannot query purchases");
            return;
        }

        billingClient.queryPurchasesAsync(
                com.android.billingclient.api.QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                (billingResult, list) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        if (list != null && !list.isEmpty()) {
                            // Update FeatureManager with active subscriptions
                            for (Purchase purchase : list) {
                                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                    updatePlanFromPurchase(purchase);
                                }
                            }

                            if (updatesListener != null) {
                                updatesListener.onPurchasesUpdated(list);
                            }
                        } else {
                            // No active subscriptions, set to FREE
                            FeatureManager.getInstance(context).setCurrentPlan(FeatureManager.PLAN_FREE);
                            Log.d(TAG, "No active subscriptions, set to FREE plan");
                        }
                    } else {
                        Log.e(TAG, "Error querying purchases: " + billingResult.getDebugMessage());
                    }
                });
    }

    /**
     * Lifecycle cleanup to prevent memory leaks
     * GLITCH PREVENTION: Properly disconnect billing client
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        if (billingClient != null && billingClient.isReady()) {
            billingClient.endConnection();
            isConnected = false;
        }
        updatesListener = null;
        Log.d(TAG, "BillingManager destroyed");
    }
}
