package com.example.cinestack;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.database.Cursor;



import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

/**
 * LoginActivity
 *
 * Handles:
 * - user login
 * - input validation
 * - session creation
 * - remember me logic
 */
public class LoginActivity extends AppCompatActivity {

    // UI components
    private TextInputLayout tilUsername, tilPassword;
    private EditText etUsername, etPassword;
    private CheckBox cbRememberMe;
    private Button btnLogin;
    private TextView tvRegisterLink, tvForgotPassword;

    // Helper classes
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize helper classes
        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // If user is already logged in and remember me is enabled,
        // go directly to main screen
        if (sessionManager.isLoggedIn() && sessionManager.isRememberMe()) {
            redirectToMainActivity();
            return;
        } else if (sessionManager.isLoggedIn() && !sessionManager.isRememberMe()) {
            // Clear session if remember me was not selected
            sessionManager.clearSession();
        }

        // Connect XML views
        initializeViews();

        // Set button and text click listeners
        setClickListeners();

        // Auto-fill username after successful registration
        checkRegistrationIntent();
    }

    /**
     * Connect all views from XML
     */
    private void initializeViews() {
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);

        cbRememberMe = findViewById(R.id.cbRememberMe);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    /**
     * Set click listeners
     */
    private void setClickListeners() {
        // Login button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Register link
        tvRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Forgot password link click
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Auto-fill username if user came from RegisterActivity
     */
    private void checkRegistrationIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra("registered_username")) {
            String username = intent.getStringExtra("registered_username");
            etUsername.setText(username);
            etPassword.requestFocus();
        }
    }

    /**
     * Handle login logic
     */
    private void loginUser() {
        // Remove previous errors
        clearErrors();

        // Read input values
        String loginInput = etUsername.getText().toString().trim(); // username or email
        String password = etPassword.getText().toString();
        boolean rememberMe = cbRememberMe.isChecked();

        // Validate fields
        if (!validateInputs(loginInput, password)) {
            return;
        }

        // Check credentials in database
        int userId = databaseHelper.getUserId(loginInput, password);

        if (userId != -1) {

            // Get full profile details using user ID
            Cursor cursor = databaseHelper.getUserProfileById(userId);

            String realUsername = "";
            String fullName = "";
            String email = "";

            if (cursor != null && cursor.moveToFirst()) {
                realUsername = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                cursor.close();
            }

            // Create session with real values from database
            sessionManager.createLoginSession(realUsername, fullName, email, userId);

            // Save remember me choice
            sessionManager.setRememberMe(rememberMe);

            // Also keep user id in UserSession because your movie screens use it
            getSharedPreferences("UserSession", MODE_PRIVATE)
                    .edit()
                    .putInt("user_id", userId)
                    .apply();

            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

            redirectToMainActivity();

        } else {
            tilPassword.setError("Invalid username/email or password");
            etPassword.requestFocus();
            Toast.makeText(this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validate input fields
     */
    private boolean validateInputs(String usernameOrEmail, String password) {

        // Check username/email
        if (TextUtils.isEmpty(usernameOrEmail)) {
            tilUsername.setError("Username or email is required");
            etUsername.requestFocus();
            return false;
        }

        // Check password
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Clear previous validation errors
     */
    private void clearErrors() {
        tilUsername.setError(null);
        tilPassword.setError(null);
    }

    /**
     * Go to MainActivity after successful login
     */
    private void redirectToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Back button closes the app
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}