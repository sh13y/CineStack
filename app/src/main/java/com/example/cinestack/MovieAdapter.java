package com.example.cinestack;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    // List of movies to display
    private ArrayList<Movie> movieList;

    // Database helper for delete action
    private DatabaseHelper databaseHelper;

    // Context used for starting activities
    private Context context;

    public MovieAdapter(ArrayList<Movie> movieList, Context context) {
        this.movieList = movieList;
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Inflate item_movie.xml for each movie row
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);

        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {

        // Get current movie
        Movie movie = movieList.get(position);

        // Set movie data into views
        holder.tvTitle.setText(movie.getTitle());
        holder.tvGenre.setText("Genre: " + movie.getGenre());
        holder.tvYear.setText("Year: " + movie.getYear());
        holder.tvReview.setText("Review: " + movie.getReview());

        // Delete button click
        holder.btnDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();

            // Make sure position is valid
            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }

            boolean deleted = databaseHelper.deleteMovie(movie.getId());

            if (deleted) {
                movieList.remove(adapterPosition);
                notifyItemRemoved(adapterPosition);
            }
        });

        // Edit button click
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditMovieActivity.class);

            // Send movie data to EditMovieActivity
            intent.putExtra("id", movie.getId());
            intent.putExtra("title", movie.getTitle());
            intent.putExtra("genre", movie.getGenre());
            intent.putExtra("year", movie.getYear());
            intent.putExtra("review", movie.getReview());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    // ViewHolder class for movie item
    public static class MovieViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvGenre, tvYear, tvReview;
        Button btnDelete, btnEdit;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);

            // Connect item_movie.xml views
            tvTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvGenre = itemView.findViewById(R.id.tvGenre);
            tvYear = itemView.findViewById(R.id.tvYear);
            tvReview = itemView.findViewById(R.id.tvReview);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}