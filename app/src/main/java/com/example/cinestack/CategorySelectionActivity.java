package com.example.cinestack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategorySelectionActivity extends AppCompatActivity {

    private static final List<String> DEFAULT_CATEGORIES = Arrays.asList(
            "Action", "Adventure", "Animation", "Comedy", "Crime", "Documentary",
            "Drama", "Family", "Fantasy", "History", "Horror", "Music",
            "Mystery", "Romance", "Sci-Fi", "Thriller", "War", "Western"
    );

    private ChipGroup chipGroupCategories;
    private TextView tvSelectedCount;
    private DatabaseHelper databaseHelper;

    private int userId;
    private boolean fromLogin;
    private String registeredUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_selection);

        databaseHelper = new DatabaseHelper(this);

        userId = getIntent().getIntExtra("user_id", -1);
        fromLogin = getIntent().getBooleanExtra("from_login", false);
        registeredUsername = getIntent().getStringExtra("registered_username");

        if (userId == -1) {
            Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        MaterialButton btnContinue = findViewById(R.id.btnContinueCategories);

        setupCategoryChips();
        btnContinue.setOnClickListener(v -> saveCategoriesAndContinue());
    }

    private void setupCategoryChips() {
        for (String category : DEFAULT_CATEGORIES) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.accent_surface);
            chip.setTextColor(getColor(R.color.text_primary));
            chip.setCheckedIconVisible(false);
            chip.setChipStrokeWidth(1f);
            chip.setChipStrokeColorResource(R.color.accent);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> updateCount());
            chipGroupCategories.addView(chip);
        }
    }

    private void updateCount() {
        int selectedCount = 0;
        for (int i = 0; i < chipGroupCategories.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupCategories.getChildAt(i);
            if (chip.isChecked()) {
                selectedCount++;
            }
        }
        tvSelectedCount.setText("Selected: " + selectedCount);
    }

    private void saveCategoriesAndContinue() {
        ArrayList<String> selected = new ArrayList<>();
        for (int i = 0; i < chipGroupCategories.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupCategories.getChildAt(i);
            if (chip.isChecked()) {
                selected.add(chip.getText().toString());
            }
        }

        if (selected.size() < 3) {
            Toast.makeText(this, "Please select at least 3 categories.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean saved = databaseHelper.saveUserCategories(userId, selected);
        if (!saved) {
            Toast.makeText(this, "Could not save categories.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fromLogin) {
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        } else {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            if (registeredUsername != null) {
                loginIntent.putExtra("registered_username", registeredUsername);
            }
            startActivity(loginIntent);
        }
        finish();
    }
}
