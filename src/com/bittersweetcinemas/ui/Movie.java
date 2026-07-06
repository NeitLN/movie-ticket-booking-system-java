package com.bittersweetcinemas.ui;

/**
 * MOVIE DOMAIN MODEL
 * -------------------------------------------------------------------------
 * Represents a single movie item inside the booking system.
 * 
 * Demonstrates OOP Encapsulation by shielding all fields as 'private final' (read-only),
 * exposing them strictly through public accessor getter methods.
 */
public class Movie {
    // Encapsulated state fields (OOP: Information Hiding)
    private final String title;       // Movie title
    private final String genre;       // Movie genre
    private final int duration;       // Movie length in minutes
    private final String rating;      // Age classification rating (P, T13, T16, T18)
    private final double score;       // Audience review score (e.g. 8.4)
    private final String posterPath;  // Path to the custom JPG poster image (can be null/empty)

    /**
     * Constructor Overloading 1 (Polymorphism)
     * For movies that do not have a custom poster image (defaults to vector placeholders).
     */
    public Movie(String title, String genre, int duration, String rating, double score) {
        this(title, genre, duration, rating, score, null);
    }

    /**
     * Constructor Overloading 2 (Polymorphism)
     * Fully configures a movie with its title, genre, run-time, rating, score, and poster file path.
     */
    public Movie(String title, String genre, int duration, String rating, double score, String posterPath) {
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.rating = rating;
        this.score = score;
        this.posterPath = posterPath;
    }

    // Accessor Getters (OOP: Access Encapsulation)
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getDuration() { return duration; }
    public String getRating() { return rating; }
    public double getScore() { return score; }
    public String getPosterPath() { return posterPath; }
}
