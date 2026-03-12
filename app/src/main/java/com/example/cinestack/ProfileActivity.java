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
 * ProfileActivity - User profile viewing and editing
 * Allows updating username, email, password, profile photo, and theme toggle.
 *
 * @author P.G.P.W. Gunathilake (original), integrated by R P I P P Gotabhaya
 * @version 1.0
 */
public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfile;
    private ImageButton btnEditPhoto;
    private TextView tvMovieCount;

    private TextInputLayout tilUsername, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword;

    private MaterialButton btnSaveProfile, btnLogoutProfile;
    private SwitchMaterial switchTheme;
    private BottomNavigationView bottomNavigation;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    private int userId = -1;
    private String currentImageUri = null;

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

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();

        initializeViews();
        userId = sessionManager.getUserId();

        loadProfileData();
        setupThemeSwitch();
        setupBottomNavigation();

        btnEditPhoto.setOnClickListener(v -> showPhotoOptions());
        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());

        btnLogoutProfile.setOnClickListener(v -> {
            getSharedPreferences("UserSession", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();
            sessionManager.logoutUser();
        });
    }

    private void initializeViews() {
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
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void loadProfileData() {
        if (userId == -1) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Cursor cursor = databaseHelper.getUserProfileById(userId);

        if (cursor != null && cursor.moveToFirst()) {
            String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));

            String profileImage = null;
            int imageIndex = cursor.getColumnIndex("profile_image");
            if (imageIndex != -1) {
                profileImage = cursor.getString(imageIndex);
            }

            etUsername.setText(username);
            etEmail.setText(email);

            currentImageUri = profileImage;
            if (profileImage != null && !profileImage.isEmpty()) {
                ivProfile.setImageURI(Uri.parse(profileImage));
            }

            cursor.close();
        }

        int movieCount = databaseHelper.getMovieCountByUser(userId);
        tvMovieCount.setText(String.valueOf(movieCount));
    }

    private void setupThemeSwitch() {
        boolean isLight =
                AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO;
        switchTheme.setChecked(isLight);

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_profile);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_watchlist) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(ProfileActivity.this, AddMovieActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }

            return false;
        });
    }

    private void showPhotoOptions() {
        String[] options = {"Change Photo", "Remove Photo"};

        new AlertDialog.Builder(this)
                .setTitle("Profile Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        imagePickerLauncher.launch("image/*");
                    } else if (which == 1) {
                        currentImageUri = null;
                        ivProfile.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                })
                .show();
    }

    private void saveProfileChanges() {
        tilUsername.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        // Validate username
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Username is required");
            return;
        }
        if (username.length() < 3) {
            tilUsername.setError("Username must be at least 3 characters");
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

        // Password validation (only if user is changing it)
        if (!TextUtils.isEmpty(password)) {
            if (password.length() < 6) {
                tilPassword.setError("Password must be at least 6 characters");
                return;
            }
            if (!password.equals(confirmPassword)) {
                tilConfirmPassword.setError("Passwords do not match");
                return;
            }
        }

        // Check uniqueness
        if (databaseHelper.isUsernameTaken(username, userId)) {
            tilUsername.setError("Username already taken");
            return;
        }
        if (databaseHelper.isEmailTaken(email, userId)) {
            tilEmail.setError("Email already exists");
            return;
        }

        // Update profile
        boolean updated = databaseHelper.updateUserProfile(
                userId, username, email, password, currentImageUri);

        if (updated) {
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }
}
