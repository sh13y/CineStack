package com.example.cinestack;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;
import android.widget.Button;
import android.widget.Button;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private ArrayList<Movie> movieList;
    private DatabaseHelper databaseHelper;

    public MovieAdapter(ArrayList<Movie> movieList, Context context) {
        this.movieList = movieList;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);

        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {

        Movie movie = movieList.get(position);

        holder.tvTitle.setText(movie.getTitle());
        holder.tvGenre.setText("Genre: " + movie.getGenre());
        holder.tvYear.setText("Year: " + movie.getYear());
        holder.tvReview.setText("Review: " + movie.getReview());

        holder.btnDelete.setOnClickListener(v -> {

            boolean deleted = databaseHelper.deleteMovie(movie.getId());

            if (deleted) {
                movieList.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
            }
        });

        holder.btnEdit.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(), EditMovieActivity.class);

            intent.putExtra("id", movie.getId());
            intent.putExtra("title", movie.getTitle());
            intent.putExtra("genre", movie.getGenre());
            intent.putExtra("year", movie.getYear());
            intent.putExtra("review", movie.getReview());

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvGenre, tvYear, tvReview;
        Button btnDelete, btnEdit;


        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvGenre = itemView.findViewById(R.id.tvGenre);
            tvYear = itemView.findViewById(R.id.tvYear);
            tvReview = itemView.findViewById(R.id.tvReview);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}
