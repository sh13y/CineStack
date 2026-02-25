package com.example.cinestack;

public class Movie {

    private int id;
    private String title;
    private String genre;
    private int year;
    private String review;

    public Movie(int id, String title, String genre, int year, String review) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.year = year;
        this.review = review;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getYear() { return year; }
    public String getReview() { return review; }
}
