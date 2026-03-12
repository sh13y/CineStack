package com.example.cinestack;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * RegisterActivity - Handles user registration
 * Validates input fields and creates new user accounts with secure password hashing
 * 
 * @author ICT3214 Group Project
 * @version 1.0
 */
public class RegisterActivity extends AppCompatActivity {

    // UI Components
    private TextInputLayout tilFullName, tilUsername, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;

    // Database Helper
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize UI components
        initializeViews();

        // Set click listeners
        setClickListeners();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        // TextInputLayouts
        tilFullName = findViewById(R.id.tilFullName);
        tilUsername = findViewById(R.id.tilUsername);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        // EditTexts
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        // Button and TextView
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    /**
     * Set click listeners for buttons and links
     */
    private void setClickListeners() {
        // Register button click
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Login link click - navigate to login screen
        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Login Activity (will be created next)
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Close registration screen
            }
        });
    }

    /**
     * Main registration method - validates inputs and creates user account
     */
    private void registerUser() {
        // Clear previous error messages
        clearErrors();

        // Get input values
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validate all inputs
        if (!validateInputs(fullName, username, email, password, confirmPassword)) {
            return; // Stop if validation fails
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

        // Attempt to register user
        boolean isRegistered = databaseHelper.registerUser(username, email, password, fullName);

        if (isRegistered) {
            // Registration successful
            Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_LONG).show();
            
            // Navigate to Login Activity
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.putExtra("registered_username", username); // Pass username to login screen
            startActivity(intent);
            finish(); // Close registration screen
        } else {
            // Registration failed
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validates all input fields
     * @return true if all validations pass, false otherwise
     */
    private boolean validateInputs(String fullName, String username, String email, 
                                   String password, String confirmPassword) {
        
        // Validate Full Name
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

        // Validate Username
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

        // Validate Email
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

        // Validate Password
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

        // Validate Confirm Password
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

        return true; // All validations passed
    }

    /**
     * Clears all error messages from input fields
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
