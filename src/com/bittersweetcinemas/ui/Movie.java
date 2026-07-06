package com.bittersweetcinemas.ui;

public class Movie {
    private final String title;
    private final String genre;
    private final int duration;
    private final String rating;
    private final double score;
    private final String posterPath;

    public Movie(String title, String genre, int duration, String rating, double score) {
        this(title, genre, duration, rating, score, null);
    }

    public Movie(String title, String genre, int duration, String rating, double score, String posterPath) {
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.rating = rating;
        this.score = score;
        this.posterPath = posterPath;
    }

    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getDuration() { return duration; }
    public String getRating() { return rating; }
    public double getScore() { return score; }
    public String getPosterPath() { return posterPath; }
}
