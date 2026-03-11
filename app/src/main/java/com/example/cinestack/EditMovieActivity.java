package com.example.cinestack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class EditMovieActivity extends AppCompatActivity {

    // Input fields
    private EditText etTitle, etGenre, etYear, etReview;

    // Update button
    private MaterialButton btnUpdate;

    // Bottom navigation
    private BottomNavigationView bottomNavigation;

    // Database helper
    private DatabaseHelper databaseHelper;

    // Current movie id
    private int movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_movie);

        // Connect views from XML
        etTitle = findViewById(R.id.etTitle);
        etGenre = findViewById(R.id.etGenre);
        etYear = findViewById(R.id.etYear);
        etReview = findViewById(R.id.etReview);
        btnUpdate = findViewById(R.id.btnUpdate);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Get data sent from previous screen
        // These keys must match MovieAdapter.java
        movieId = getIntent().getIntExtra("id", -1);
        etTitle.setText(getIntent().getStringExtra("title"));
        etGenre.setText(getIntent().getStringExtra("genre"));
        etYear.setText(String.valueOf(getIntent().getIntExtra("year", 0)));
        etReview.setText(getIntent().getStringExtra("review"));

        // Update button click
        btnUpdate.setOnClickListener(v -> updateMovie());

        // Set selected tab in bottom navigation
        // Since edit screen belongs to watchlist flow, keep watchlist selected
        bottomNavigation.setSelectedItemId(R.id.nav_watchlist);

        // Handle bottom navigation clicks
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // Open watchlist page
            if (id == R.id.nav_watchlist) {
                startActivity(new Intent(EditMovieActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            // Open add movie page
            else if (id == R.id.nav_add) {
                startActivity(new Intent(EditMovieActivity.this, AddMovieActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            // Open profile page
            else if (id == R.id.nav_profile) {
                startActivity(new Intent(EditMovieActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }

    /**
     * Validate and update movie in database
     */
    private void updateMovie() {

        // Read values from fields
        String title = etTitle.getText().toString().trim();
        String genre = etGenre.getText().toString().trim();
        String year = etYear.getText().toString().trim();
        String review = etReview.getText().toString().trim();

        // Validate required fields
        if (title.isEmpty() || genre.isEmpty() || year.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update movie in database
        boolean updated = databaseHelper.updateMovie(
                movieId,
                title,
                genre,
                year,
                review
        );

        if (updated) {
            Toast.makeText(this, "Movie Updated", Toast.LENGTH_SHORT).show();

            // Go back to main watchlist page after update
            startActivity(new Intent(EditMovieActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Failed to update movie", Toast.LENGTH_SHORT).show();
        }
    }
}