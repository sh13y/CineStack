package com.example.cinestack;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WishlistFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private WishlistAdapter adapter;
    private TextView tvState;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wishlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseHelper = new DatabaseHelper(requireContext());
        userId = new SessionManager(requireContext()).getUserId();
        tvState = view.findViewById(R.id.tvWishlistState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerWishlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new WishlistAdapter(requireContext(), new WishlistAdapter.WishlistActions() {
            @Override
            public void onRemove(WishlistItem item) {
                databaseHelper.removeWishlistItem(item.getWishlistId());
                loadWishlist();
            }

            @Override
            public void onReviewUpdated() {
                loadWishlist();
            }
        });
        recyclerView.setAdapter(adapter);

        loadWishlist();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWishlist();
    }

    private void loadWishlist() {
        ArrayList<WishlistItem> items = new ArrayList<>();
        Cursor cursor = databaseHelper.getWishlistByUser(userId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int wishlistId = cursor.getInt(cursor.getColumnIndexOrThrow("wishlist_id"));
                int tmdbId = cursor.getInt(cursor.getColumnIndexOrThrow("tmdb_id"));
                String mediaType = cursor.getString(cursor.getColumnIndexOrThrow("media_type"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String genre = cursor.getString(cursor.getColumnIndexOrThrow("genre"));
                String overview = cursor.getString(cursor.getColumnIndexOrThrow("overview"));
                String poster = cursor.getString(cursor.getColumnIndexOrThrow("poster_path"));
                String year = cursor.getString(cursor.getColumnIndexOrThrow("release_year"));
                double vote = cursor.getDouble(cursor.getColumnIndexOrThrow("vote_average"));
                float rating = cursor.getFloat(cursor.getColumnIndexOrThrow("user_rating"));
                String review = cursor.getString(cursor.getColumnIndexOrThrow("user_review"));
                boolean watched = cursor.getInt(cursor.getColumnIndexOrThrow("is_watched")) == 1;

                items.add(new WishlistItem(wishlistId, tmdbId, mediaType, title, genre, overview, poster, year, vote, rating, review, watched));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.setItems(items);

        if (items.isEmpty()) {
            tvState.setVisibility(View.VISIBLE);
            tvState.setText("Wishlist is empty. Add titles from Home.");
        } else {
            tvState.setVisibility(View.GONE);
        }
    }
}
