package movieticketbooking.model;

import movieticketbooking.exception.ValidationException;
import movieticketbooking.util.ValidationUtils;

public class Movie {
    private String movieId;       // MOV001, MOV002, etc.
    private String title;         // Movie title
    private String genre;         // Genre
    private int duration;         // Duration in minutes (> 0)
    private String ageRating;      // P, T13, T16, T18
    private double score;         // User score (e.g. 8.4)
    private String posterPath;    // JPG poster filename (e.g. "obsession.jpg")

    public Movie(String movieId, String title, String genre, int duration, String ageRating, double score, String posterPath) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.ageRating = ageRating;
        this.score = score;
        this.posterPath = posterPath;
    }

    public Movie(String movieId, String title, String genre, int duration, String ageRating) {
        this(movieId, title, genre, duration, ageRating, 0.0, "");
    }

    public void validate() throws ValidationException {
        ValidationUtils.validateId(movieId, "Movie ID");
        ValidationUtils.validateTitle(title);
        ValidationUtils.validateDuration(duration);
        if (ageRating == null || ageRating.trim().isEmpty()) {
            throw new ValidationException("Age Rating cannot be empty.");
        }
    }

    public String toTxtLine() {
        return String.join("|",
            movieId,
            title,
            genre,
            String.valueOf(duration),
            ageRating,
            String.valueOf(score),
            (posterPath != null ? posterPath : "")
        );
    }

    public static Movie fromTxtLine(String line) throws ValidationException {
        if (line == null || line.trim().isEmpty()) {
            throw new ValidationException("Cannot parse empty line.");
        }
        String[] tokens = line.split("\\|");
        if (tokens.length < 5) {
            throw new ValidationException("Incorrect movie line format");
        }
        try {
            String movieId = tokens[0].trim();
            String title = tokens[1].trim();
            String genre = tokens[2].trim();
            int duration = Integer.parseInt(tokens[3].trim());
            String ageRating = tokens[4].trim();
            
            double score = 0.0;
            if (tokens.length > 5 && !tokens[5].trim().isEmpty()) {
                score = Double.parseDouble(tokens[5].trim());
            }
            
            String posterPath = "";
            if (tokens.length > 6) {
                posterPath = tokens[6].trim();
            }
            
            return new Movie(movieId, title, genre, duration, ageRating, score, posterPath);
        } catch (NumberFormatException e) {
            throw new ValidationException("Failed to parse numerical movie tokens: " + e.getMessage());
        }
    }

    // Getters and Setters
    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getAgeRating() { return ageRating; }
    public void setAgeRating(String ageRating) { this.ageRating = ageRating; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    @Override
    public String toString() {
        return "Movie{" +
                "movieId='" + movieId + '\'' +
                ", title='" + title + '\'' +
                ", genre='" + genre + '\'' +
                ", duration=" + duration +
                ", ageRating='" + ageRating + '\'' +
                ", score=" + score +
                '}';
    }
}
