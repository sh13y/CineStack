package com.example.cinestack;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddMovieActivity extends AppCompatActivity {

    private EditText etTitle, etGenre, etYear, etReview;
    private Button btnSaveMovie;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);

        etTitle = findViewById(R.id.etTitle);
        etGenre = findViewById(R.id.etGenre);
        etYear = findViewById(R.id.etYear);
        etReview = findViewById(R.id.etReview);
        btnSaveMovie = findViewById(R.id.btnSaveMovie);

        databaseHelper = new DatabaseHelper(this);

        btnSaveMovie.setOnClickListener(v -> saveMovie());
    }

    private void saveMovie() {

        String title = etTitle.getText().toString().trim();
        String genre = etGenre.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();
        String review = etReview.getText().toString().trim();

        if (title.isEmpty() || genre.isEmpty() || yearStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int year = Integer.parseInt(yearStr);

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean inserted = databaseHelper.insertMovie(title, genre, year, review, userId);

        if (inserted) {
            Toast.makeText(this, "Movie added successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to add movie", Toast.LENGTH_SHORT).show();
        }
    }
}