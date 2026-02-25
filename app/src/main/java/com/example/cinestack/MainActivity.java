package com.example.cinestack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import com.google.android.material.appbar.MaterialToolbar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Button;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private ArrayList<Movie> movieList;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // DB
        databaseHelper = new DatabaseHelper(this);

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerViewMovies);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(movieList, this);
        recyclerView.setAdapter(movieAdapter);

        // Add Movie button
        Button btnAddMovie = findViewById(R.id.btnAddMovie);
        btnAddMovie.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddMovieActivity.class))
        );

        loadMovies();
    }

    private void loadMovies() {
        movieList.clear();

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) return;

        Cursor cursor = databaseHelper.getMoviesByUser(userId);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("movie_id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String genre = cursor.getString(cursor.getColumnIndexOrThrow("genre"));
                int year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));
                String review = cursor.getString(cursor.getColumnIndexOrThrow("review"));

                movieList.add(new Movie(id, title, genre, year, review));
            } while (cursor.moveToNext());
        }

        cursor.close();
        movieAdapter.notifyDataSetChanged();
    }

    private void searchMovies(String keyword) {
        movieList.clear();

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) return;

        Cursor cursor = databaseHelper.searchMovies(userId, keyword);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("movie_id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String genre = cursor.getString(cursor.getColumnIndexOrThrow("genre"));
                int year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));
                String review = cursor.getString(cursor.getColumnIndexOrThrow("review"));

                movieList.add(new Movie(id, title, genre, year, review));
            } while (cursor.moveToNext());
        }

        cursor.close();
        movieAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMovies();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchMovies(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchMovies(newText);
                return true;
            }
        });

        return true;
    }

    // You wanted: login required every time app opens.
    // Clearing here is OK (but note: onDestroy is not always guaranteed).
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}