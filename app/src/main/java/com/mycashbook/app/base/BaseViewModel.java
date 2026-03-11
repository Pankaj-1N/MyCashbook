package com.mycashbook.app.base;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.mycashbook.app.utils.LogUtils;

/**
 * Base ViewModel class for all ViewModels
 * Provides common functionality like loading state, error handling, messages
 */
public abstract class BaseViewModel extends AndroidViewModel {

    private static final String TAG = "BaseViewModel";

    // Loading state
    protected MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    // Error messages
    protected MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Success messages
    protected MutableLiveData<String> successMessage = new MutableLiveData<>();

    // Warning messages
    protected MutableLiveData<String> warningMessage = new MutableLiveData<>();

    public BaseViewModel(@NonNull Application application) {
        super(application);
        LogUtils.methodEntry(TAG, "BaseViewModel constructor");
    }

    /**
     * Get loading state as LiveData
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Alias for getIsLoading() - for compatibility
     */
    public LiveData<Boolean> getLoading() {
        return isLoading;
    }

    /**
     * Set loading state
     */
    protected void setIsLoading(boolean loading) {
        isLoading.postValue(loading);
        LogUtils.d(TAG, "Loading: " + loading);
    }

    /**
     * Get error message as LiveData
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Set error message
     */
    protected void setErrorMessage(String message) {
        if (message != null && !message.isEmpty()) {
            errorMessage.postValue(message);
            LogUtils.e(TAG, "Error: " + message);
        }
    }

    /**
     * Clear error message - CHANGED TO PUBLIC
     */
    public void clearErrorMessage() {
        errorMessage.postValue(null);
    }

    /**
     * Get success message as LiveData
     */
    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    /**
     * Set success message
     */
    protected void setSuccessMessage(String message) {
        if (message != null && !message.isEmpty()) {
            successMessage.postValue(message);
            LogUtils.i(TAG, "Success: " + message);
        }
    }

    /**
     * Clear success message - CHANGED TO PUBLIC
     */
    public void clearSuccessMessage() {
        successMessage.postValue(null);
    }

    /**
     * Get warning message as LiveData
     */
    public LiveData<String> getWarningMessage() {
        return warningMessage;
    }

    /**
     * Set warning message
     */
    protected void setWarningMessage(String message) {
        if (message != null && !message.isEmpty()) {
            warningMessage.postValue(message);
            LogUtils.w(TAG, "Warning: " + message);
        }
    }

    /**
     * Clear warning message
     */
    protected void clearWarningMessage() {
        warningMessage.postValue(null);
    }

    /**
     * Start loading operation
     */
    protected void startLoading() {
        setIsLoading(true);
        clearErrorMessage();
        LogUtils.d(TAG, "Loading started");
    }

    /**
     * Stop loading operation
     */
    protected void stopLoading() {
        setIsLoading(false);
        LogUtils.d(TAG, "Loading stopped");
    }

    /**
     * Handle error in loading
     */
    protected void handleLoadingError(String errorMsg) {
        stopLoading();
        setErrorMessage(errorMsg);
        LogUtils.e(TAG, "Loading error: " + errorMsg);
    }

    /**
     * Handle success in loading
     */
    protected void handleLoadingSuccess(String successMsg) {
        stopLoading();
        setSuccessMessage(successMsg);
        LogUtils.i(TAG, "Loading success: " + successMsg);
    }

    /**
     * Handle exception
     */
    protected void handleException(String operationName, Exception e) {
        stopLoading();
        String errorMsg = operationName + " failed: " + (e.getMessage() != null ? e.getMessage() : "Unknown error");
        setErrorMessage(errorMsg);
        LogUtils.e(TAG, errorMsg, e);
    }

    /**
     * Check if string is null or empty
     */
    protected boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Check if string is not empty
     */
    protected boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Log method entry
     */
    protected void logMethodEntry(String methodName) {
        LogUtils.methodEntry(TAG, methodName);
    }

    /**
     * Log method exit
     */
    protected void logMethodExit(String methodName) {
        LogUtils.methodExit(TAG, methodName);
    }

    /**
     * Log debug message
     */
    protected void logDebug(String message) {
        LogUtils.d(TAG, message);
    }

    /**
     * Log info message
     */
    protected void logInfo(String message) {
        LogUtils.i(TAG, message);
    }

    /**
     * Log warning message
     */
    protected void logWarning(String message) {
        LogUtils.w(TAG, message);
    }

    /**
     * Log error message
     */
    protected void logError(String message, Exception e) {
        LogUtils.e(TAG, message, e);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        LogUtils.methodExit(TAG, "onCleared");
    }
}