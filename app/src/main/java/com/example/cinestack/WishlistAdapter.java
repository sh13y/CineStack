package com.example.cinestack;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    public interface WishlistActions {
        void onRemove(WishlistItem item);
        void onReviewUpdated();
    }

    private final Context context;
    private final List<WishlistItem> items = new ArrayList<>();
    private final DatabaseHelper databaseHelper;
    private final WishlistActions actions;

    public WishlistAdapter(Context context, WishlistActions actions) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
        this.actions = actions;
    }

    public void setItems(List<WishlistItem> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wishlist, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        WishlistItem item = items.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvMeta.setText(item.getMediaType().toUpperCase() + "  •  " + item.getYear() + "  •  TMDB " + String.format("%.1f", item.getVoteAverage()));
        holder.tvOverview.setText(item.getOverview());

        String reviewText = (item.getUserReview() == null || item.getUserReview().trim().isEmpty())
                ? "No review yet"
                : item.getUserReview();
        holder.tvReview.setText("Your rating: " + item.getUserRating() + "  •  " + reviewText);

        holder.btnRemove.setOnClickListener(v -> actions.onRemove(item));
        holder.btnReview.setOnClickListener(v -> showReviewDialog(item));
    }

    private void showReviewDialog(WishlistItem item) {
        View dialogView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null, false);

        ViewGroup container = new android.widget.LinearLayout(context);
        ((android.widget.LinearLayout) container).setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(48, 24, 48, 8);

        RatingBar ratingBar = new RatingBar(context, null, android.R.attr.ratingBarStyleSmall);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(0.5f);
        ratingBar.setRating(item.getUserRating());

        CheckBox watchedCheck = new CheckBox(context);
        watchedCheck.setText("I watched this");
        watchedCheck.setChecked(item.isWatched());

        EditText reviewInput = new EditText(context);
        reviewInput.setHint("Write your short review");
        reviewInput.setText(item.getUserReview());
        reviewInput.setMinLines(3);

        container.addView(ratingBar);
        container.addView(watchedCheck);
        container.addView(reviewInput);

        new AlertDialog.Builder(context)
                .setTitle("Rate & Review")
                .setView(container)
                .setPositiveButton("Save", (dialog, which) -> {
                    databaseHelper.updateWishlistReview(
                            item.getWishlistId(),
                            ratingBar.getRating(),
                            reviewInput.getText().toString(),
                            watchedCheck.isChecked()
                    );
                    actions.onReviewUpdated();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class WishlistViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvMeta;
        TextView tvOverview;
        TextView tvReview;
        MaterialButton btnReview;
        MaterialButton btnRemove;

        WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvWishlistTitle);
            tvMeta = itemView.findViewById(R.id.tvWishlistMeta);
            tvOverview = itemView.findViewById(R.id.tvWishlistOverview);
            tvReview = itemView.findViewById(R.id.tvWishlistReview);
            btnReview = itemView.findViewById(R.id.btnReview);
            btnRemove = itemView.findViewById(R.id.btnRemoveWishlist);
        }
    }
}
