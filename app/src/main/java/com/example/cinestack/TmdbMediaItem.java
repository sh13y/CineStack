package com.example.cinestack;

public class TmdbMediaItem {
    private final int tmdbId;
    private final String mediaType;
    private final String title;
    private final String genre;
    private final String overview;
    private final String posterPath;
    private final String year;
    private final double voteAverage;

    public TmdbMediaItem(int tmdbId, String mediaType, String title, String genre,
                         String overview, String posterPath, String year, double voteAverage) {
        this.tmdbId = tmdbId;
        this.mediaType = mediaType;
        this.title = title;
        this.genre = genre;
        this.overview = overview;
        this.posterPath = posterPath;
        this.year = year;
        this.voteAverage = voteAverage;
    }

    public int getTmdbId() {
        return tmdbId;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public String getOverview() {
        return overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getYear() {
        return year;
    }

    public double getVoteAverage() {
        return voteAverage;
    }
}
