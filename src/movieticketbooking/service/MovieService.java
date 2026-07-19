package movieticketbooking.service;

import movieticketbooking.exception.ValidationException;
import movieticketbooking.model.Movie;
import movieticketbooking.util.FileManager;
import movieticketbooking.util.IdGenerator;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

public class MovieService {
    private static final String FILE_PATH = "data/movies.txt";
    private final List<Movie> movies;
    private boolean lastLoadHadRecords;
    private int lastLoadValidCount;
    private int lastLoadInvalidCount;

    public MovieService() {
        this.movies = new ArrayList<>();
        loadMoviesFromFile();
        
        if (!lastLoadHadRecords) {
            bootstrapDefaultMovies();
        }
    }

    /**
     * Re-bootstrapping default movies with EMPTY poster paths as requested by user.
     */
    private void bootstrapDefaultMovies() {
        movies.add(new Movie("MOV001", "Obsession", "Kinh dị / Hồi hộp", 108, "T18", 7.8, ""));
        movies.add(new Movie("MOV002", "Thỏ ơi!!", "Tâm lý", 127, "T16", 8.1, ""));
        movies.add(new Movie("MOV003", "Doraemon: Nobita và Lâu Đài Dưới Đáy Biển", "Hoạt hình/Phiêu lưu", 101, "P", 7.5, ""));
        movies.add(new Movie("MOV004", "Backrooms: Thực Thể Quỷ Quyệt", "Kinh dị/Hồi hộp", 110, "T16", 8.4, ""));
        movies.add(new Movie("MOV005", "Tên Cậu Là Gì.", "Hoạt hình", 107, "T13", 7.9, ""));
        movies.add(new Movie("MOV006", "Minions & Quái vật", "Hoạt hình", 90, "P", 7.2, ""));
        saveMoviesToFile();
    }

    public void loadMoviesFromFile() {
        movies.clear();
        List<String> lines = FileManager.readLines(FILE_PATH);
        lastLoadHadRecords = !lines.isEmpty();
        lastLoadValidCount = 0;
        lastLoadInvalidCount = 0;

        for (String line : lines) {
            try {
                Movie movie = Movie.fromTxtLine(line);
                movie.validate();
                movies.add(movie);
                lastLoadValidCount++;
            } catch (ValidationException e) {
                lastLoadInvalidCount++;
                System.err.println("Skipping malformed movie record: " + line + " - " + e.getMessage());
            }
        }

        if (lastLoadHadRecords && lastLoadValidCount == 0 && lastLoadInvalidCount > 0) {
            System.err.println(
                "Movie data file contains only invalid records. " +
                "The original file was not replaced."
            );
        }
    }

    public void saveMoviesToFile() {
        List<String> lines = new ArrayList<>();
        for (Movie m : movies) {
            lines.add(m.toTxtLine());
        }
        FileManager.writeLines(FILE_PATH, lines);
    }

    public List<Movie> getAllMovies() {
        return new ArrayList<>(movies);
    }

    public String generateNextMovieId() {
        List<String> activeIds = new ArrayList<>();
        for (Movie m : movies) {
            activeIds.add(m.getMovieId());
        }
        return IdGenerator.generateNextId(activeIds, "MOV", 3);
    }

    public void addMovie(Movie movie) throws ValidationException {
        if (movie == null) {
            throw new ValidationException("Movie object cannot be null.");
        }
        movie.validate();
        
        for (Movie m : movies) {
            if (m.getMovieId().equalsIgnoreCase(movie.getMovieId())) {
                throw new ValidationException("Duplicate Movie ID detected: " + movie.getMovieId());
            }
        }
        movies.add(movie);
        try {
            saveMoviesToFile();
        } catch (UncheckedIOException e) {
            movies.remove(movie);
            throw e;
        }
    }

    public void updateMovie(Movie updatedMovie) throws ValidationException {
        if (updatedMovie == null) {
            throw new ValidationException("Movie object cannot be null.");
        }
        updatedMovie.validate();
        
        int index = -1;
        for (int i = 0; i < movies.size(); i++) {
            if (movies.get(i).getMovieId().equalsIgnoreCase(updatedMovie.getMovieId())) {
                index = i;
                break;
            }
        }
        
        if (index == -1) {
            throw new ValidationException("Movie with ID " + updatedMovie.getMovieId() + " not found.");
        }
        Movie previousMovie = movies.set(index, updatedMovie);
        try {
            saveMoviesToFile();
        } catch (UncheckedIOException e) {
            movies.set(index, previousMovie);
            throw e;
        }
    }

    public void deleteMovie(String movieId) throws ValidationException {
        int index = -1;
        for (int i = 0; i < movies.size(); i++) {
            if (movies.get(i).getMovieId().equalsIgnoreCase(movieId)) {
                index = i;
                break;
            }
        }
        
        if (index == -1) {
            throw new ValidationException("Movie with ID " + movieId + " not found.");
        }
        Movie removedMovie = movies.remove(index);
        try {
            saveMoviesToFile();
        } catch (UncheckedIOException e) {
            movies.add(index, removedMovie);
            throw e;
        }
    }

    public List<Movie> searchMovies(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllMovies();
        }
        String lowerQuery = query.toLowerCase().trim();
        List<Movie> results = new ArrayList<>();
        for (Movie m : movies) {
            if (m.getTitle().toLowerCase().contains(lowerQuery) ||
                m.getGenre().toLowerCase().contains(lowerQuery)) {
                results.add(m);
            }
        }
        return results;
    }

    public List<Movie> getMoviesSortedByName(boolean ascending) {
        List<Movie> sorted = new ArrayList<>(movies);
        sorted.sort((m1, m2) -> {
            int comp = m1.getTitle().compareToIgnoreCase(m2.getTitle());
            return ascending ? comp : -comp;
        });
        return sorted;
    }

    public List<Movie> getMoviesSortedByDuration(boolean ascending) {
        List<Movie> sorted = new ArrayList<>(movies);
        sorted.sort((m1, m2) -> {
            int comp = Integer.compare(m1.getDuration(), m2.getDuration());
            return ascending ? comp : -comp;
        });
        return sorted;
    }

    public List<Movie> getMoviesSortedByGenre(boolean ascending) {
        List<Movie> sorted = new ArrayList<>(movies);
        sorted.sort((m1, m2) -> {
            int comp = m1.getGenre().compareToIgnoreCase(m2.getGenre());
            return ascending ? comp : -comp;
        });
        return sorted;
    }
}
