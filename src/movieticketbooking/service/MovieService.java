package movieticketbooking.service;

import movieticketbooking.exception.ValidationException;
import movieticketbooking.model.Movie;
import movieticketbooking.util.FileManager;
import movieticketbooking.util.IdGenerator;

import java.io.UncheckedIOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MovieService {
    private static final String FILE_PATH = "data/movies.txt";
    private final List<Movie> movies;
    private boolean lastLoadHadRecords;
    private int lastLoadValidCount;
    private int lastLoadInvalidCount;

    public enum MovieSortOption {
        ID_ASC("ID (Ascending)"),
        TITLE_ASC("Title (A-Z)"),
        TITLE_DESC("Title (Z-A)"),
        DURATION_ASC("Duration (Shortest First)"),
        DURATION_DESC("Duration (Longest First)"),
        SCORE_DESC("Score (Highest First)"),
        SCORE_ASC("Score (Lowest First)");

        private final String displayName;

        MovieSortOption(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

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
        List<String> lines = FileManager.readLines(FILE_PATH);
        List<Movie> loadedMovies = new ArrayList<>();
        int validCount = 0;
        int invalidCount = 0;

        for (String line : lines) {
            try {
                Movie movie = Movie.fromTxtLine(line);
                movie.validate();
                loadedMovies.add(movie);
                validCount++;
            } catch (ValidationException e) {
                invalidCount++;
                System.err.println("Skipping malformed movie record: " + line + " - " + e.getMessage());
            }
        }

        movies.clear();
        movies.addAll(loadedMovies);
        lastLoadHadRecords = !lines.isEmpty();
        lastLoadValidCount = validCount;
        lastLoadInvalidCount = invalidCount;

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
        return searchMovies(query, null, null, null);
    }

    /**
     * Searches, filters and sorts the in-memory movie snapshot without changing it.
     */
    public List<Movie> searchMovies(String query, String genre, String ageRating,
                                    MovieSortOption sortOption) {
        String normalizedQuery = normalizeSearchText(query);
        String normalizedGenre = normalizeFilter(genre, "All Genres");
        String normalizedRating = normalizeFilter(ageRating, "All Ratings");
        String[] queryTokens = normalizedQuery.isEmpty()
            ? new String[0]
            : normalizedQuery.split(" ");

        List<Movie> results = new ArrayList<>();
        for (Movie m : movies) {
            if (!normalizedGenre.isEmpty() &&
                    !normalizeSearchText(m.getGenre()).equals(normalizedGenre)) {
                continue;
            }
            if (!normalizedRating.isEmpty() &&
                    !normalizeSearchText(m.getAgeRating()).equals(normalizedRating)) {
                continue;
            }

            String searchableText = String.join(" ",
                normalizeSearchText(m.getMovieId()),
                normalizeSearchText(m.getTitle()),
                normalizeSearchText(m.getGenre()),
                normalizeSearchText(m.getAgeRating())
            );
            boolean matchesAllTokens = true;
            for (String token : queryTokens) {
                if (!searchableText.contains(token)) {
                    matchesAllTokens = false;
                    break;
                }
            }
            if (matchesAllTokens) {
                results.add(m);
            }
        }
        results.sort(comparatorFor(sortOption));
        return results;
    }

    public List<String> getGenreOptions() {
        return getDistinctSortedOptions(true);
    }

    public List<String> getAgeRatingOptions() {
        return getDistinctSortedOptions(false);
    }

    public List<Movie> getMoviesSortedByName(boolean ascending) {
        return searchMovies(null, null, null,
            ascending ? MovieSortOption.TITLE_ASC : MovieSortOption.TITLE_DESC);
    }

    public List<Movie> getMoviesSortedByDuration(boolean ascending) {
        return searchMovies(null, null, null,
            ascending ? MovieSortOption.DURATION_ASC : MovieSortOption.DURATION_DESC);
    }

    public List<Movie> getMoviesSortedByGenre(boolean ascending) {
        List<Movie> sorted = new ArrayList<>(movies);
        sorted.sort((m1, m2) -> {
            int comp = normalizeSearchText(m1.getGenre()).compareTo(normalizeSearchText(m2.getGenre()));
            if (comp != 0) {
                return ascending ? comp : -comp;
            }
            return compareMovieIds(m1, m2);
        });
        return sorted;
    }

    private List<String> getDistinctSortedOptions(boolean genres) {
        Map<String, String> distinct = new LinkedHashMap<>();
        for (Movie movie : movies) {
            String value = genres ? movie.getGenre() : movie.getAgeRating();
            String normalized = normalizeSearchText(value);
            if (!normalized.isEmpty()) {
                distinct.putIfAbsent(normalized, value.trim());
            }
        }
        List<String> options = new ArrayList<>(distinct.values());
        options.sort((left, right) -> {
            int comparison = normalizeSearchText(left).compareTo(normalizeSearchText(right));
            return comparison != 0 ? comparison : left.compareToIgnoreCase(right);
        });
        return options;
    }

    private static String normalizeFilter(String filter, String allLabel) {
        String normalized = normalizeSearchText(filter);
        return normalized.equals(normalizeSearchText(allLabel)) ? "" : normalized;
    }

    private static String normalizeSearchText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .replace('đ', 'd')
            .replace('Đ', 'D')
            .toLowerCase(Locale.ROOT)
            .trim();
        return normalized.replaceAll("\\s+", " ");
    }

    private static Comparator<Movie> comparatorFor(MovieSortOption requestedOption) {
        MovieSortOption option = requestedOption == null ? MovieSortOption.ID_ASC : requestedOption;
        Comparator<Movie> idComparator = MovieService::compareMovieIds;
        switch (option) {
            case TITLE_ASC:
                return Comparator.comparing(
                    (Movie movie) -> normalizeSearchText(movie.getTitle())
                ).thenComparing(idComparator);
            case TITLE_DESC:
                return (left, right) -> {
                    int comparison = normalizeSearchText(right.getTitle())
                        .compareTo(normalizeSearchText(left.getTitle()));
                    return comparison != 0 ? comparison : compareMovieIds(left, right);
                };
            case DURATION_ASC:
                return Comparator.comparingInt(Movie::getDuration).thenComparing(idComparator);
            case DURATION_DESC:
                return (left, right) -> {
                    int comparison = Integer.compare(right.getDuration(), left.getDuration());
                    return comparison != 0 ? comparison : compareMovieIds(left, right);
                };
            case SCORE_DESC:
                return (left, right) -> {
                    int comparison = Double.compare(right.getScore(), left.getScore());
                    return comparison != 0 ? comparison : compareMovieIds(left, right);
                };
            case SCORE_ASC:
                return Comparator.comparingDouble(Movie::getScore).thenComparing(idComparator);
            case ID_ASC:
            default:
                return idComparator;
        }
    }

    private static int compareMovieIds(Movie left, Movie right) {
        Integer leftNumber = validMovieNumber(left.getMovieId());
        Integer rightNumber = validMovieNumber(right.getMovieId());
        if (leftNumber != null && rightNumber != null) {
            int numericComparison = Integer.compare(leftNumber, rightNumber);
            if (numericComparison != 0) {
                return numericComparison;
            }
        } else if (leftNumber != null) {
            return -1;
        } else if (rightNumber != null) {
            return 1;
        }
        return normalizeSearchText(left.getMovieId()).compareTo(normalizeSearchText(right.getMovieId()));
    }

    private static Integer validMovieNumber(String id) {
        if (id == null || !id.startsWith("MOV") || id.length() < 6) {
            return null;
        }
        String suffix = id.substring(3);
        for (int i = 0; i < suffix.length(); i++) {
            char character = suffix.charAt(i);
            if (character < '0' || character > '9') {
                return null;
            }
        }
        try {
            return Integer.valueOf(suffix);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
