package com.mycashbook.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mycashbook.app.billing.BillingManager;
import com.mycashbook.app.billing.BillingPlan;

import java.util.ArrayList;
import java.util.List;

public class BillingViewModel extends AndroidViewModel {

    private final BillingManager billingManager;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>(null);

    // We will track if they are "Premium" generally
    private final MutableLiveData<Boolean> isPremium = new MutableLiveData<>(false);

    // --- ADDED: Track "Business" plan specifically so HomeActivity doesn't crash
    // ---
    private final MutableLiveData<Boolean> isBusiness = new MutableLiveData<>(false);

    // List of plans to show in UI
    private final MutableLiveData<List<BillingPlan>> availablePlans = new MutableLiveData<>();

    public BillingViewModel(@NonNull Application application) {
        super(application);
        // Use getInstance instead of constructor
        billingManager = BillingManager.getInstance(application);

        // Load initial dummy data or check status
        loadSubscriptionState();
        loadDummyPlans();
    }

    // ---------------------- LOAD USER PLAN ----------------------

    public void loadSubscriptionState() {
        loading.postValue(true);

        // The new Manager is synchronous for this check in our current implementation
        boolean status = billingManager.getSubscriptionStatus();
        isPremium.postValue(status);

        // --- ADDED: Initialize isBusiness (defaulting to false for now) ---
        isBusiness.postValue(false);

        loading.postValue(false);
    }

    private void loadDummyPlans() {
        List<BillingPlan> plans = new ArrayList<>();
        // Create dummy plans to match your Adapter
        plans.add(new BillingPlan("monthly_premium", "Monthly Premium", "₹199.00", "Remove ads & Unlimited books", ""));
        plans.add(new BillingPlan("yearly_premium", "Yearly Premium", "₹1999.00", "Save 20% on yearly plan", ""));

        availablePlans.postValue(plans);
    }

    public void startPurchaseFlow(BillingPlan plan) {
        billingManager.startPurchaseFlow(plan);
    }

    // ---------------------- GETTERS ----------------------

    public LiveData<Boolean> isPremium() {
        return isPremium;
    }

    // --- ADDED: The missing getter method ---
    public LiveData<Boolean> isBusiness() {
        return isBusiness;
    }

    public LiveData<List<BillingPlan>> getAvailablePlans() {
        return availablePlans;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getMessage() {
        return message;
    }
}
