package com.example.cinestack;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * RegisterActivity
 *
 * Handles user registration:
 * - validates input fields
 * - checks duplicate username/email
 * - creates a new account in database
 * - redirects user to login screen after successful registration
 */
public class RegisterActivity extends AppCompatActivity {

    // Input layouts for showing validation errors
    private TextInputLayout tilFullName, tilUsername, tilEmail, tilPassword, tilConfirmPassword;

    // Text input fields
    private TextInputEditText etFullName, etUsername, etEmail, etPassword, etConfirmPassword;

    // Button and login link
    private Button btnRegister;
    private TextView tvLoginLink;

    // Database helper
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Connect XML views
        initializeViews();

        // Set click listeners
        setClickListeners();
    }

    /**
     * Connect all views from XML
     */
    private void initializeViews() {
        // Input layouts
        tilFullName = findViewById(R.id.tilFullName);
        tilUsername = findViewById(R.id.tilUsername);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        // Input fields
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        // Button and text link
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    /**
     * Set button and link click listeners
     */
    private void setClickListeners() {
        // Register button
        btnRegister.setOnClickListener(v -> registerUser());

        // Login link
        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Main registration logic
     */
    private void registerUser() {
        // Clear old errors
        clearErrors();

        // Read user input
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validate all fields
        if (!validateInputs(fullName, username, email, password, confirmPassword)) {
            return;
        }

        // Check if username already exists
        if (databaseHelper.checkUsernameExists(username)) {
            tilUsername.setError("Username already taken");
            etUsername.requestFocus();
            return;
        }

        // Check if email already exists
        if (databaseHelper.checkEmailExists(email)) {
            tilEmail.setError("Email already registered");
            etEmail.requestFocus();
            return;
        }

        // Register user in database
        boolean isRegistered = databaseHelper.registerUser(username, email, password, fullName);

        if (isRegistered) {
            Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_LONG).show();

            // Go to login screen and prefill username
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.putExtra("registered_username", username);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validate all input fields
     */
    private boolean validateInputs(String fullName, String username, String email,
                                   String password, String confirmPassword) {

        // Full name validation
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Full name is required");
            etFullName.requestFocus();
            return false;
        }

        if (fullName.length() < 3) {
            tilFullName.setError("Full name must be at least 3 characters");
            etFullName.requestFocus();
            return false;
        }

        // Username validation
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Username is required");
            etUsername.requestFocus();
            return false;
        }

        if (username.length() < 3) {
            tilUsername.setError("Username must be at least 3 characters");
            etUsername.requestFocus();
            return false;
        }

        if (username.length() > 20) {
            tilUsername.setError("Username must not exceed 20 characters");
            etUsername.requestFocus();
            return false;
        }

        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            tilUsername.setError("Username can only contain letters, numbers, and underscores");
            etUsername.requestFocus();
            return false;
        }

        // Email validation
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        // Confirm password validation
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Clear all previous error messages
     */
    private void clearErrors() {
        tilFullName.setError(null);
        tilUsername.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    /**
     * Handle back button press
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}