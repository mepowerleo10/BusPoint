package com.gorillagang.buspoint.ui.account;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<FirebaseUser> user = new MutableLiveData<>();
    private final FirebaseAuth mAuth;
    private final String TAG = LoginViewModel.class.getSimpleName();

    LoginViewModel(FirebaseAuth mAuth) {
        this.mAuth = mAuth;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<FirebaseUser> getLoginResult() {
        return user;
    }


}