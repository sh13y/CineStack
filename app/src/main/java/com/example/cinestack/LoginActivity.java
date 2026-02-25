package com.example.cinestack;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * LoginActivity - Handles user login authentication
 * Validates credentials against database and manages login sessions
 * 
 * @author ICT3214 Group Project
 * @version 1.0
 */
public class LoginActivity extends AppCompatActivity {

    // UI Components
    private TextInputLayout tilUsername, tilPassword;
    private TextInputEditText etUsername, etPassword;
    private CheckBox cbRememberMe;
    private Button btnLogin;
    private TextView tvRegisterLink;

    // Database Helper and Session Manager
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database helper and session manager
        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        setContentView(R.layout.activity_login);



        // Initialize UI components
        initializeViews();

        // Set click listeners
        setClickListeners();

        // Check if username was passed from registration
        checkRegistrationIntent();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        // TextInputLayouts
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);

        // EditTexts
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);

        // CheckBox and Button
        cbRememberMe = findViewById(R.id.cbRememberMe);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
    }

    /**
     * Set click listeners for buttons and links
     */
    private void setClickListeners() {
        // Login button click
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Register link click - navigate to registration screen
        tvRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Check if username was passed from registration (auto-fill)
     */
    private void checkRegistrationIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra("registered_username")) {
            String username = intent.getStringExtra("registered_username");
            etUsername.setText(username);
            etPassword.requestFocus(); // Focus on password field
        }
    }

    /**
     * Main login method - validates credentials and creates session
     */
    private void loginUser() {
        // Clear previous error messages
        clearErrors();

        // Get input values
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        boolean rememberMe = cbRememberMe.isChecked();

        // Validate inputs
        if (!validateInputs(username, password)) {
            return; // Stop if validation fails
        }

        // Verify credentials with database
        int userId = databaseHelper.getUserId(username, password);

        if (userId != -1) {

            // Save user_id in SharedPreferences
            getSharedPreferences("UserSession", MODE_PRIVATE)
                    .edit()
                    .putInt("user_id", userId)
                    .apply();

            // Keep your existing session manager logic


            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

            redirectToMainActivity();

        } else {
            tilPassword.setError("Invalid username/email or password");
            etPassword.requestFocus();
            Toast.makeText(this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validates login input fields
     * @return true if all validations pass, false otherwise
     */
    private boolean validateInputs(String username, String password) {
        
        // Validate Username/Email
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Username or email is required");
            etUsername.requestFocus();
            return false;
        }

        // Validate Password
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        return true; // All validations passed
    }

    /**
     * Clears all error messages from input fields
     */
    private void clearErrors() {
        tilUsername.setError(null);
        tilPassword.setError(null);
    }

    /**
     * Redirect to MainActivity after successful login
     */
    private void redirectToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Close login screen
    }

    /**
     * Handle back button press
     * Exit app instead of going back to previous activity
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity(); // Close all activities and exit app
    }
}
