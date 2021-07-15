package com.gorillagang.buspoint.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.gorillagang.buspoint.R;
import com.gorillagang.buspoint.databinding.ActivityRegisterBinding;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private ActivityRegisterBinding binding;
    private EditText username;
    private EditText email;
    private EditText password;
    private EditText passwordConfirm;
    private Button registerButton;
    private Button skipButton;
    private Button signInButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

        username = findViewById(R.id.register_username);
        email = findViewById(R.id.register_email);
        password = findViewById(R.id.register_password);
        passwordConfirm = findViewById(R.id.register_confirm_password);
        registerButton = findViewById(R.id.button_register);
        signInButton = findViewById(R.id.button_activity_login);
        skipButton = findViewById(R.id.button_skip_registration);

        skipButton.setOnClickListener(v -> {
            finish();
        });

        signInButton.setOnClickListener(v -> {
            Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        });

        registerButton.setOnClickListener(v -> {
            signUpNewUser(username.getText().toString(),
                    email.getText().toString(),
                    password.getText().toString());
        });

        TextWatcher afterTextChangeListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                registerDataChanged(
                        username.getText().toString(),
                        email.getText().toString(),
                        password.getText().toString(),
                        passwordConfirm.getText().toString()
                );
            }
        };
        username.addTextChangedListener(afterTextChangeListener);
        email.addTextChangedListener(afterTextChangeListener);
        password.addTextChangedListener(afterTextChangeListener);
        passwordConfirm.addTextChangedListener(afterTextChangeListener);
    }

    private void signUpNewUser(String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        verifyEmailAddress(user, username);
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                R.string.failed_registration,
                                Toast.LENGTH_LONG);
                    }
                })
                .addOnFailureListener(e -> {
                    showFailDialog(e.getMessage());
                    Log.i(TAG, e.getMessage());
                });
    }

    private void verifyEmailAddress(FirebaseUser user, String username) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserProfileChangeRequest profileUpdate =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username).build();
                        user.updateProfile(profileUpdate)
                                .addOnCompleteListener(task1 -> {
                                    Toast.makeText(RegisterActivity.this,
                                            "User: " + username + " registered!",
                                            Toast.LENGTH_LONG).show();
                                });
                        mAuth.signOut();
                        showVerificationDialog();
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Oops! Seems we can't verify your email",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showFailDialog(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog
                .setIcon(R.drawable.ic_baseline_account_circle_24)
                .setTitle("Account Creation Failed")
                .setMessage(message)
                .setPositiveButton(R.string.ok, (dialog1, which) -> {
                    dialog1.dismiss();
                }).create().show();
    }

    private void showVerificationDialog() {
        Log.i(TAG, "Showing verification dialog");
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog
                .setIcon(R.drawable.ic_baseline_account_circle_24)
                .setTitle("Account Created")
                .setMessage("Your Account has been created.\n\n" +
                        "Please check your inbox if you have received a verification email.\n\n" +
                        "And click the link provided so that you can login with this email.")
                .setPositiveButton(R.string.ok, (dialog1, which) -> {
                    dialog1.dismiss();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                }).create().show();
    }

    private void registerDataChanged(String username,
                                     String email,
                                     String password,
                                     String passwordConf) {
        registerButton.setEnabled(false);
        if (!isEmailValid(email)) {
            this.email.setError("Invalid email address");
        } else if (!isPasswordValid(password)) {
            this.password.setError(getString(R.string.invalid_password));
        } else if (!password.contains(passwordConf)) {
            this.passwordConfirm.setError("Passwords do not match");
        } else {
            registerButton.setEnabled(true);
        }
    }

    private boolean isEmailValid(String email) {
        if (email == null) {
            return false;
        }
        if (email.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
        } else {
            return !email.trim().isEmpty();
        }
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
}