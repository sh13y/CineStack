package com.example.cinestack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private ArrayList<Movie> movieList;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    private EditText searchBar;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if user is logged in
        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();

        databaseHelper = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerViewMovies);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(movieList, this);
        recyclerView.setAdapter(movieAdapter);

        // Search bar
        searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchMovies(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Bottom navigation
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_watchlist);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_watchlist) {
                return true;
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(MainActivity.this, AddMovieActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });

        loadMovies();
    }

    private int getLoggedInUserId() {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }

    private void loadMovies() {
        movieList.clear();

        int userId = getLoggedInUserId();
        if (userId == -1) return;

        Cursor cursor = databaseHelper.getMoviesByUser(userId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("movie_id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String genre = cursor.getString(cursor.getColumnIndexOrThrow("genre"));
                int year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));
                String review = cursor.getString(cursor.getColumnIndexOrThrow("review"));

                movieList.add(new Movie(id, title, genre, year, review));
            } while (cursor.moveToNext());

            cursor.close();
        }

        movieAdapter.notifyDataSetChanged();
    }

    private void searchMovies(String keyword) {
        movieList.clear();

        int userId = getLoggedInUserId();
        if (userId == -1) return;

        // if empty -> load all
        if (keyword == null || keyword.trim().isEmpty()) {
            loadMovies();
            return;
        }

        Cursor cursor = databaseHelper.searchMovies(userId, keyword);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("movie_id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String genre = cursor.getString(cursor.getColumnIndexOrThrow("genre"));
                int year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));
                String review = cursor.getString(cursor.getColumnIndexOrThrow("review"));

                movieList.add(new Movie(id, title, genre, year, review));
            } while (cursor.moveToNext());

            cursor.close();
        }

        movieAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMovies();
    }


}