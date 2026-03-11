package com.mycashbook.app.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AuthViewModel extends ViewModel {

    // A simple boolean to track if the user is signed in
    private final MutableLiveData<Boolean> isSignedIn = new MutableLiveData<>(false);

    public LiveData<Boolean> isSignedIn() {
        return com.mycashbook.app.repository.AuthRepository.getInstance().getIsSignedIn();
    }

    public void setSignedIn(boolean value) {
        com.mycashbook.app.repository.AuthRepository.getInstance().setSignedIn(value);
    }
}
