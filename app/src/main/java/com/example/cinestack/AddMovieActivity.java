package com.example.cinestack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AddMovieActivity extends AppCompatActivity {

    // Input fields
    private EditText etTitle, etGenre, etYear, etReview;

    // Save button
    private Button btnSaveMovie;

    // Database helper
    private DatabaseHelper databaseHelper;

    // Bottom navigation
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);

        // Connect XML views
        etTitle = findViewById(R.id.etTitle);
        etGenre = findViewById(R.id.etGenre);
        etYear = findViewById(R.id.etYear);
        etReview = findViewById(R.id.etReview);
        btnSaveMovie = findViewById(R.id.btnSaveMovie);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Save movie button click
        btnSaveMovie.setOnClickListener(v -> saveMovie());

        // Set current selected bottom tab
        bottomNavigation.setSelectedItemId(R.id.nav_add);

        // Handle bottom navigation clicks
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // Open watchlist page
            if (id == R.id.nav_watchlist) {
                startActivity(new Intent(AddMovieActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            // Stay on add movie page
            else if (id == R.id.nav_add) {
                return true;
            }

            // Open profile page
            else if (id == R.id.nav_profile) {
                startActivity(new Intent(AddMovieActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }

    /**
     * Save movie details into database
     */
    private void saveMovie() {

        // Read input values
        String title = etTitle.getText().toString().trim();
        String genre = etGenre.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();
        String review = etReview.getText().toString().trim();

        // Validate required fields
        if (title.isEmpty() || genre.isEmpty() || yearStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert year to integer
        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid year", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get logged-in user ID from session
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        // If user is not logged in, stop
        if (userId == -1) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert movie into database
        boolean inserted = databaseHelper.insertMovie(title, genre, year, review, userId);

        if (inserted) {
            Toast.makeText(this, "Movie added successfully!", Toast.LENGTH_SHORT).show();

            // Go back to watchlist after saving
            startActivity(new Intent(AddMovieActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Failed to add movie", Toast.LENGTH_SHORT).show();
        }
    }
}