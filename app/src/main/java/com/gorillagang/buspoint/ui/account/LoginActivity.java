package com.gorillagang.buspoint.ui.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.gorillagang.buspoint.MainActivity;
import com.gorillagang.buspoint.R;
import com.gorillagang.buspoint.databinding.ActivityLoginBinding;

import timber.log.Timber;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    ProgressBar loadingProgressBar;
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private LoginFormState loginFormState;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button skipButton;
    private Button signUpButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        emailEditText = binding.email;
        passwordEditText = binding.password;
        loginButton = binding.login;
        skipButton = binding.buttonSkip;
        loadingProgressBar = binding.loading;
        signUpButton = binding.buttonSignup;

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginDataChanged(emailEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        emailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login(emailEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
            return false;
        });

        loginButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            login(emailEditText.getText().toString(),
                    passwordEditText.getText().toString());
        });

        skipButton.setOnClickListener(v -> {
            /*Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);*/
            finish();
        });

        signUpButton.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(i);
            finish();
        });
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState = new LoginFormState(R.string.invalid_username, null);
        } else if (!isPasswordValid(password)) {
            loginFormState = new LoginFormState(null, R.string.invalid_password);
        } else {
            loginFormState = new LoginFormState(true);
        }

        if (loginFormState.isDataValid()) {
            loginButton.setEnabled(true);
        }
    }

    public void login(String email, String password) {
        // can be launched in a separate asynchronous job
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loadingProgressBar.setVisibility(View.GONE);

                    //Complete and destroy login activity once successful
                    finish();
                    if (task.isSuccessful()) {
                        user = mAuth.getCurrentUser();
                        updateUiWithUser(user);
                        Timber.tag(TAG).i("login:success %s", user.getEmail());
                    } else {
                        Timber.tag(TAG).i("login:failed");
                        showLoginFailed("Login Failed");
                    }
                });
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    private void updateUiWithUser(FirebaseUser user) {
        String welcome = getString(R.string.welcome) + user.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        Intent i = new Intent(this, MainActivity.class);
        setResult(Activity.RESULT_OK);
        startActivity(i);
        finish();
    }

    private void showLoginFailed(String errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

}