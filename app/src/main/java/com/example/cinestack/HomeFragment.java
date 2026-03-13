package com.example.cinestack;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinestack.tmdb.TmdbApiService;
import com.example.cinestack.tmdb.TmdbClient;
import com.example.cinestack.tmdb.model.GenreResponse;
import com.example.cinestack.tmdb.model.TrendingResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private final Map<Integer, String> genreMapMovie = new HashMap<>();
    private final Map<Integer, String> genreMapTv = new HashMap<>();

    private ProgressBar progressBar;
    private TextView tvState;
    private DiscoveryAdapter adapter;
    private DatabaseHelper databaseHelper;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseHelper = new DatabaseHelper(requireContext());
        userId = new SessionManager(requireContext()).getUserId();

        progressBar = view.findViewById(R.id.progressHome);
        tvState = view.findViewById(R.id.tvHomeState);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerHome);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new DiscoveryAdapter(requireContext(), userId);
        recyclerView.setAdapter(adapter);

        loadTrending();
    }

    private void loadTrending() {
        String apiKey = BuildConfig.TMDB_API_KEY;
        if (TextUtils.isEmpty(apiKey)) {
            showState("TMDB API key missing. Add TMDB_API_KEY in local.properties.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvState.setVisibility(View.GONE);

        TmdbApiService service = TmdbClient.getService();
        service.getMovieGenres(apiKey, "en-US").enqueue(new Callback<GenreResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenreResponse> call, @NonNull Response<GenreResponse> response) {
                if (response.body() != null && response.body().getGenres() != null) {
                    for (GenreResponse.GenreItem item : response.body().getGenres()) {
                        genreMapMovie.put(item.getId(), item.getName());
                    }
                }
                loadTvGenresThenTrending(apiKey);
            }

            @Override
            public void onFailure(@NonNull Call<GenreResponse> call, @NonNull Throwable t) {
                loadTvGenresThenTrending(apiKey);
            }
        });
    }

    private void loadTvGenresThenTrending(String apiKey) {
        TmdbApiService service = TmdbClient.getService();
        service.getTvGenres(apiKey, "en-US").enqueue(new Callback<GenreResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenreResponse> call, @NonNull Response<GenreResponse> response) {
                if (response.body() != null && response.body().getGenres() != null) {
                    for (GenreResponse.GenreItem item : response.body().getGenres()) {
                        genreMapTv.put(item.getId(), item.getName());
                    }
                }
                loadTrendingList(apiKey);
            }

            @Override
            public void onFailure(@NonNull Call<GenreResponse> call, @NonNull Throwable t) {
                loadTrendingList(apiKey);
            }
        });
    }

    private void loadTrendingList(String apiKey) {
        TmdbClient.getService().getTrendingAllWeek(apiKey, "en-US").enqueue(new Callback<TrendingResponse>() {
            @Override
            public void onResponse(@NonNull Call<TrendingResponse> call, @NonNull Response<TrendingResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) {
                    return;
                }

                if (!response.isSuccessful() || response.body() == null || response.body().getResults() == null) {
                    showState("Could not load trending content.");
                    return;
                }

                List<String> preferred = databaseHelper.getUserCategories(userId);
                Set<String> preferredSet = new HashSet<>();
                for (String value : preferred) {
                    preferredSet.add(value.toLowerCase(Locale.ROOT));
                }

                ArrayList<TmdbMediaItem> filtered = new ArrayList<>();
                ArrayList<TmdbMediaItem> fallback = new ArrayList<>();

                for (TrendingResponse.TrendingItem item : response.body().getResults()) {
                    String mediaType = item.getMediaType();
                    if (!"movie".equals(mediaType) && !"tv".equals(mediaType)) {
                        continue;
                    }

                    String title = "movie".equals(mediaType) ? item.getTitle() : item.getName();
                    String date = "movie".equals(mediaType) ? item.getReleaseDate() : item.getFirstAirDate();
                    String year = (date != null && date.length() >= 4) ? date.substring(0, 4) : "N/A";

                    String primaryGenre = "General";
                    if (item.getGenreIds() != null && !item.getGenreIds().isEmpty()) {
                        Integer first = item.getGenreIds().get(0);
                        String fromMap = "movie".equals(mediaType) ? genreMapMovie.get(first) : genreMapTv.get(first);
                        if (fromMap != null) {
                            primaryGenre = fromMap;
                        }
                    }

                    TmdbMediaItem mapped = new TmdbMediaItem(
                            item.getId(),
                            mediaType,
                            title == null ? "Untitled" : title,
                            primaryGenre,
                            item.getOverview() == null ? "No synopsis available." : item.getOverview(),
                            item.getPosterPath(),
                            year,
                            item.getVoteAverage()
                    );

                    fallback.add(mapped);
                    if (preferredSet.contains(primaryGenre.toLowerCase(Locale.ROOT))) {
                        filtered.add(mapped);
                    }
                }

                List<TmdbMediaItem> display = filtered.isEmpty() ? fallback : filtered;
                if (display.isEmpty()) {
                    showState("No trending items available right now.");
                    return;
                }

                if (display.size() > 20) {
                    display = display.subList(0, 20);
                }

                adapter.setItems(display);
            }

            @Override
            public void onFailure(@NonNull Call<TrendingResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                showState("Network error. Please check your internet connection.");
            }
        });
    }

    private void showState(String text) {
        progressBar.setVisibility(View.GONE);
        tvState.setText(text);
        tvState.setVisibility(View.VISIBLE);
    }
}
