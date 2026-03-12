package com.example.cinestack;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * ProfileActivity
 *
 * This screen allows the user to:
 * - view profile details
 * - change/remove profile photo
 * - update username
 * - update email
 * - update password
 * - confirm password before saving
 * - view number of added movies
 * - temporarily switch app to light theme while this page is open
 * - navigate using bottom navigation
 */
public class ProfileActivity extends AppCompatActivity {

    // Profile image and movie count
    private ImageView ivProfile;
    private ImageButton btnEditPhoto;
    private TextView tvMovieCount;

    // Input layouts for validation errors
    private TextInputLayout tilUsername, tilEmail, tilPassword, tilConfirmPassword;

    // Input fields
    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword;

    // Buttons and theme switch
    private MaterialButton btnSaveProfile, btnLogoutProfile;
    private SwitchMaterial switchTheme;

    // Bottom navigation
    private BottomNavigationView bottomNavigation;

    // Helper classes
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    // Logged-in user id
    private int userId = -1;

    // Store selected image URI as string to save in database
    private String currentImageUri = null;

    /**
     * Modern image picker launcher
     * Opens gallery and returns selected image
     */
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    currentImageUri = uri.toString();
                    ivProfile.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize database helper and session manager
        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // If user is not logged in, redirect to login screen
        sessionManager.checkLogin();

        // Connect profile views from XML
        ivProfile = findViewById(R.id.ivProfile);
        btnEditPhoto = findViewById(R.id.btnEditPhoto);
        tvMovieCount = findViewById(R.id.tvMovieCount);

        tilUsername = findViewById(R.id.tilUsername);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnLogoutProfile = findViewById(R.id.btnLogoutProfile);
        switchTheme = findViewById(R.id.switchTheme);

        // Connect bottom navigation
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Get logged-in user id from session
        userId = sessionManager.getUserId();

        // Load current profile data from database
        loadProfileData();

        // Setup theme switch behavior
        setupThemeSwitch();

        // Setup bottom navigation
        setupBottomNavigation();

        // Pencil icon click -> show photo options
        btnEditPhoto.setOnClickListener(v -> showPhotoOptions());

        // Save button click -> validate and save changes
        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());

        // Logout button click
        btnLogoutProfile.setOnClickListener(v -> {
            getSharedPreferences("UserSession", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();

            sessionManager.logoutUser();
        });
    }

    /**
     * Load user profile data from database
     */
    private void loadProfileData() {
        if (userId == -1) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Cursor cursor = databaseHelper.getUserProfileById(userId);

        if (cursor != null && cursor.moveToFirst()) {
            // Get saved username and email
            String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));

            // Get saved profile image if available
            String profileImage = null;
            int imageIndex = cursor.getColumnIndex("profile_image");
            if (imageIndex != -1) {
                profileImage = cursor.getString(imageIndex);
            }

            // Set text fields
            etUsername.setText(username);
            etEmail.setText(email);

            // Set image if one exists
            currentImageUri = profileImage;
            if (profileImage != null && !profileImage.isEmpty()) {
                ivProfile.setImageURI(Uri.parse(profileImage));
            }

            cursor.close();
        }

        // Load and show movie count
        int movieCount = databaseHelper.getMovieCountByUser(userId);
        tvMovieCount.setText(String.valueOf(movieCount));
    }

    /**
     * Theme switch logic
     *
     * OFF = dark mode
     * ON = light mode
     *
     * When this activity closes, app returns to dark mode
     */
    private void setupThemeSwitch() {
        boolean isLight =
                AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO;

        switchTheme.setChecked(isLight);

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Light theme for whole app
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                // Dark theme for whole app
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });
    }

    /**
     * Bottom navigation setup
     */
    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_profile);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // Open watchlist page
            if (id == R.id.nav_watchlist) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            // Open add movie page
            else if (id == R.id.nav_add) {
                startActivity(new Intent(ProfileActivity.this, AddMovieActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            // Stay on profile page
            else if (id == R.id.nav_profile) {
                return true;
            }

            return false;
        });
    }

    /**
     * Show options to change or remove profile photo
     */
    private void showPhotoOptions() {
        String[] options = {"Change Photo", "Remove Photo"};

        new AlertDialog.Builder(this)
                .setTitle("Profile Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Open gallery
                        imagePickerLauncher.launch("image/*");
                    } else if (which == 1) {
                        // Remove saved image and restore placeholder
                        currentImageUri = null;
                        ivProfile.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                })
                .show();
    }

    /**
     * Validate and save profile changes
     */
    private void saveProfileChanges() {
        // Clear old error messages
        tilUsername.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        // Read input values
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        // Validate username
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Username is required");
            return;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email");
            return;
        }

        // Validate password only if user entered a new password
        if (!TextUtils.isEmpty(password) || !TextUtils.isEmpty(confirmPassword)) {
            if (password.length() < 6) {
                tilPassword.setError("Password must be at least 6 characters");
                return;
            }

            if (!password.equals(confirmPassword)) {
                tilConfirmPassword.setError("Passwords do not match");
                return;
            }
        }

        // Check if username already belongs to another account
        if (databaseHelper.isUsernameTaken(username, userId)) {
            tilUsername.setError("Username already exists");
            return;
        }

        // Check if email already belongs to another account
        if (databaseHelper.isEmailTaken(email, userId)) {
            tilEmail.setError("Email already exists");
            return;
        }

        // Update profile in database
        boolean updated = databaseHelper.updateUserProfile(
                userId,
                username,
                email,
                password,        // DatabaseHelper will hash it
                currentImageUri
        );

        if (updated) {
            // Update session values too
            sessionManager.createLoginSession(username, username, email, userId);

            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

            // Clear password fields after successful update
            etPassword.setText("");
            etConfirmPassword.setText("");
        } else {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * When this activity closes, app goes back to dark theme

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }*/
}
