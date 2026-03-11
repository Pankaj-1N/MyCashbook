package com.mycashbook.app.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AuthRepository {

    private static AuthRepository instance;
    private final MutableLiveData<Boolean> isSignedIn = new MutableLiveData<>(false);

    private AuthRepository() {
        // Private constructor for singleton
    }

    public static synchronized AuthRepository getInstance() {
        if (instance == null) {
            instance = new AuthRepository();
        }
        return instance;
    }

    public LiveData<Boolean> getIsSignedIn() {
        return isSignedIn;
    }

    public void setSignedIn(boolean signedIn) {
        isSignedIn.postValue(signedIn);
    }
}
