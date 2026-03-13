package com.example.cinestack.tmdb.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GenreResponse {

    @SerializedName("genres")
    private List<GenreItem> genres;

    public List<GenreItem> getGenres() {
        return genres;
    }

    public static class GenreItem {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
