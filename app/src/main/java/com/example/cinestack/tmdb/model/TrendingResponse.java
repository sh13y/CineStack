package com.example.cinestack.tmdb.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TrendingResponse {

    @SerializedName("results")
    private List<TrendingItem> results;

    public List<TrendingItem> getResults() {
        return results;
    }

    public static class TrendingItem {
        @SerializedName("id")
        private int id;

        @SerializedName("media_type")
        private String mediaType;

        @SerializedName("title")
        private String title;

        @SerializedName("name")
        private String name;

        @SerializedName("overview")
        private String overview;

        @SerializedName("poster_path")
        private String posterPath;

        @SerializedName("release_date")
        private String releaseDate;

        @SerializedName("first_air_date")
        private String firstAirDate;

        @SerializedName("vote_average")
        private double voteAverage;

        @SerializedName("genre_ids")
        private List<Integer> genreIds;

        public int getId() {
            return id;
        }

        public String getMediaType() {
            return mediaType;
        }

        public String getTitle() {
            return title;
        }

        public String getName() {
            return name;
        }

        public String getOverview() {
            return overview;
        }

        public String getPosterPath() {
            return posterPath;
        }

        public String getReleaseDate() {
            return releaseDate;
        }

        public String getFirstAirDate() {
            return firstAirDate;
        }

        public double getVoteAverage() {
            return voteAverage;
        }

        public List<Integer> getGenreIds() {
            return genreIds;
        }
    }
}
