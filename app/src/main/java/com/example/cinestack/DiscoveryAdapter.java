package com.example.cinestack;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryAdapter extends RecyclerView.Adapter<DiscoveryAdapter.DiscoveryViewHolder> {

    private final List<TmdbMediaItem> items = new ArrayList<>();
    private final Context context;
    private final DatabaseHelper databaseHelper;
    private final int userId;

    public DiscoveryAdapter(Context context, int userId) {
        this.context = context;
        this.userId = userId;
        this.databaseHelper = new DatabaseHelper(context);
    }

    public void setItems(List<TmdbMediaItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DiscoveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discovery, parent, false);
        return new DiscoveryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscoveryViewHolder holder, int position) {
        TmdbMediaItem item = items.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvMeta.setText(item.getMediaType().toUpperCase() + "  •  " + item.getYear() + "  •  " + item.getGenre());
        holder.tvOverview.setText(item.getOverview());

        if (item.getPosterPath() == null || item.getPosterPath().isEmpty()) {
            holder.ivPoster.setImageResource(R.drawable.ic_profile_placeholder);
        } else {
            Glide.with(context)
                    .load("https://image.tmdb.org/t/p/w500" + item.getPosterPath())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(holder.ivPoster);
        }

        boolean alreadySaved = databaseHelper.isWishlistItemExists(userId, item.getTmdbId(), item.getMediaType());
        holder.btnAddWishlist.setEnabled(!alreadySaved);
        holder.btnAddWishlist.setText(alreadySaved ? "Saved" : "Add to Wishlist");

        holder.btnAddWishlist.setOnClickListener(v -> {
            boolean saved = databaseHelper.upsertWishlistItem(userId, item);
            if (saved) {
                holder.btnAddWishlist.setEnabled(false);
                holder.btnAddWishlist.setText("Saved");
                Toast.makeText(context, "Added to wishlist", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Could not add item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DiscoveryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle;
        TextView tvMeta;
        TextView tvOverview;
        MaterialButton btnAddWishlist;

        DiscoveryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            tvOverview = itemView.findViewById(R.id.tvOverview);
            btnAddWishlist = itemView.findViewById(R.id.btnAddWishlist);
        }
    }
}
