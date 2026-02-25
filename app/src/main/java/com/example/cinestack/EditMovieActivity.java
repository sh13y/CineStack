package com.example.cinestack;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditMovieActivity extends AppCompatActivity {

    EditText etTitle, etGenre, etYear, etReview;
    Button btnUpdate;

    DatabaseHelper databaseHelper;
    int movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_movie);

        etTitle = findViewById(R.id.etTitle);
        etGenre = findViewById(R.id.etGenre);
        etYear = findViewById(R.id.etYear);
        etReview = findViewById(R.id.etReview);
        btnUpdate = findViewById(R.id.btnSave);

        databaseHelper = new DatabaseHelper(this);

        // Get data from intent
        movieId = getIntent().getIntExtra("id", -1);
        etTitle.setText(getIntent().getStringExtra("title"));
        etGenre.setText(getIntent().getStringExtra("genre"));
        etYear.setText(getIntent().getStringExtra("year"));
        etReview.setText(getIntent().getStringExtra("review"));

        btnUpdate.setOnClickListener(v -> {

            boolean updated = databaseHelper.updateMovie(
                    movieId,
                    etTitle.getText().toString(),
                    etGenre.getText().toString(),
                    etYear.getText().toString(),
                    etReview.getText().toString()
            );

            if (updated) {
                Toast.makeText(this, "Movie Updated", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}