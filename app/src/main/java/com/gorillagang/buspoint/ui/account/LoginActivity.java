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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.gorillagang.buspoint.MainActivity;
import com.gorillagang.buspoint.R;
import com.gorillagang.buspoint.databinding.ActivityLoginBinding;

import java.util.regex.Pattern;

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
            this.emailEditText.setError(getString(R.string.invalid_username));
            loginFormState = new LoginFormState(R.string.invalid_username, null);
        } else if (!isPasswordValid(password)) {
            if (password.trim().length() > 0)
                this.passwordEditText.setError(getString(R.string.invalid_password));
            loginFormState = new LoginFormState(null, R.string.invalid_password);
        } else {
            loginFormState = new LoginFormState(true);
        }

        loginButton.setEnabled(loginFormState.isDataValid());
    }

    public void login(String email, String password) {
        // can be launched in a separate asynchronous job
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        user = mAuth.getCurrentUser();
                        if (user.isEmailVerified()) {
                            //Complete and destroy login activity if the email is validated
                            finish();
                            updateUiWithUser(user);
                        } else {
                            showLoginFailed("Please verify this email address first.\nResend Verification email to this user?", true);
                        }

                    } else {
                        showLoginFailed("Credentials provided are incorrect.", false);
                    }
                });
    }

    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        }

        return false;
    }

    private boolean isPasswordValid(String password) {
        final Pattern PASSWORD_PATTERN =
                Pattern.compile("^" +
                        "(?=.*[@#$%^&+=])" +   // at least 1 special character
                        "(?=\\S+$)" +           // no white spaces
                        ".{6,}" +               // at least 6 characters
                        "$");
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    private void updateUiWithUser(FirebaseUser user) {
        // Initiating a successful login experience
        String welcome = getString(R.string.welcome) + user.getDisplayName() + "!";
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        Intent i = new Intent(this, MainActivity.class);
        setResult(Activity.RESULT_OK);
        startActivity(i);
        finish();
    }

    private void showLoginFailed(String errorString, boolean verify) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_baseline_account_circle_24);
        if (verify) {
            dialog.setTitle("Failed Email Verification")
                    .setMessage(errorString)
                    .setPositiveButton(R.string.verify, (dialog1, which) -> {
                        user.sendEmailVerification();
                    })
                    .setNegativeButton(getString(R.string.cancel), (dialog1, which) -> {
                    });
        } else {
            dialog.setTitle("Account Credentials")
                    .setMessage(errorString)
                    .setPositiveButton(R.string.ok, (dialog1, which) -> {
                    })
                    .setNegativeButton(getString(R.string.cancel), (dialog1, which) -> {
                    });
            findViewById(R.id.container_forgot_password).setVisibility(View.VISIBLE);
            Button forgotPasswordBtn = findViewById(R.id.button_reset_password);
            forgotPasswordBtn.setOnClickListener(v -> {
                mAuth.sendPasswordResetEmail(emailEditText.getText().toString())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this,
                                        "Password Reset successful, an email has been sent to this email address.",
                                        Toast.LENGTH_LONG).show();
                                findViewById(R.id.container_forgot_password).setVisibility(View.GONE);
                            } else {
                                Toast.makeText(LoginActivity.this,
                                        "Password Reset failed, the provided email might be incorrect",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            });
        }
        dialog.create().show();
    }

}