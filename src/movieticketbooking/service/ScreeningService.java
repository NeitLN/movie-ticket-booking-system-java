package movieticketbooking.service;

import movieticketbooking.exception.ValidationException;
import movieticketbooking.model.Movie;
import movieticketbooking.model.Screening;
import movieticketbooking.util.FileManager;
import movieticketbooking.util.IdGenerator;

import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScreeningService {
    private static final String FILE_PATH = "data/screenings.txt";

    /**
     * Business rule: a room needs a cleanup/turnover gap between the end of one
     * screening and the start of the next in the same room.
     */
    private static final int ROOM_CLEANUP_BUFFER_MINUTES = 15;

    private final List<Screening> screenings;
    private final MovieService movieService;
    private boolean lastLoadHadRecords;
    private int lastLoadValidCount;
    private int lastLoadInvalidCount;

    public ScreeningService(MovieService movieService) {
        if (movieService == null) {
            throw new IllegalArgumentException("MovieService dependency cannot be null.");
        }
        this.movieService = movieService;
        this.screenings = new ArrayList<>();
        loadScreenings();
    }

    public void loadScreenings() {
        screenings.clear();
        List<String> lines = FileManager.readLines(FILE_PATH);
        lastLoadHadRecords = !lines.isEmpty();
        lastLoadValidCount = 0;
        lastLoadInvalidCount = 0;

        for (String line : lines) {
            try {
                Screening screening = Screening.fromTxtLine(line);
                screenings.add(screening);
                lastLoadValidCount++;
                if (findMovieById(screening.getMovieId()) == null) {
                    System.err.println(
                        "Warning: screening " + screening.getScreeningId() +
                        " references missing movie " + screening.getMovieId() +
                        "; keeping record in memory."
                    );
                }
            } catch (ValidationException e) {
                lastLoadInvalidCount++;
                System.err.println("Skipping malformed screening record: " + line + " - " + e.getMessage());
            }
        }

        if (lastLoadHadRecords && lastLoadValidCount == 0 && lastLoadInvalidCount > 0) {
            System.err.println(
                "Screening data file contains only invalid records. " +
                "The original file was not replaced."
            );
        }
    }

    public void reload() {
        loadScreenings();
    }

    public void saveScreenings() {
        List<String> lines = new ArrayList<>();
        for (Screening s : screenings) {
            lines.add(s.toTxtLine());
        }
        FileManager.writeLines(FILE_PATH, lines);
    }

    public List<Screening> getAllScreenings() {
        List<Screening> sorted = new ArrayList<>(screenings);
        sorted.sort((a, b) -> a.getStartDateTime().compareTo(b.getStartDateTime()));
        return sorted;
    }

    public Screening getScreeningById(String screeningId) {
        if (screeningId == null || screeningId.trim().isEmpty()) {
            return null;
        }
        for (Screening s : screenings) {
            if (s.getScreeningId().equalsIgnoreCase(screeningId.trim())) {
                return s;
            }
        }
        return null;
    }

    public List<Screening> getScreeningsByMovieId(String movieId) {
        List<Screening> results = new ArrayList<>();
        if (movieId == null || movieId.trim().isEmpty()) {
            return results;
        }
        String target = movieId.trim();
        for (Screening s : screenings) {
            if (s.getMovieId().equalsIgnoreCase(target)) {
                results.add(s);
            }
        }
        results.sort((a, b) -> a.getStartDateTime().compareTo(b.getStartDateTime()));
        return results;
    }

    public List<Screening> getScreeningsByRoom(String room) {
        List<Screening> results = new ArrayList<>();
        if (room == null || room.trim().isEmpty()) {
            return results;
        }
        String target = room.trim();
        for (Screening s : screenings) {
            if (s.getRoom().equalsIgnoreCase(target)) {
                results.add(s);
            }
        }
        results.sort((a, b) -> a.getStartDateTime().compareTo(b.getStartDateTime()));
        return results;
    }

    public List<Screening> getScreeningsByDate(LocalDate date) {
        List<Screening> results = new ArrayList<>();
        if (date == null) {
            return results;
        }
        for (Screening s : screenings) {
            if (date.equals(s.getScreeningDate())) {
                results.add(s);
            }
        }
        results.sort((a, b) -> a.getStartDateTime().compareTo(b.getStartDateTime()));
        return results;
    }

    /**
     * Free-text search across movie ID and room. Null/blank query returns all screenings.
     */
    public List<Screening> searchScreenings(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllScreenings();
        }
        String lowerQuery = query.toLowerCase().trim();
        List<Screening> results = new ArrayList<>();
        for (Screening s : screenings) {
            if (s.getMovieId().toLowerCase().contains(lowerQuery) ||
                s.getRoom().toLowerCase().contains(lowerQuery)) {
                results.add(s);
            }
        }
        results.sort((a, b) -> a.getStartDateTime().compareTo(b.getStartDateTime()));
        return results;
    }

    public String generateNextScreeningId() {
        List<String> activeIds = new ArrayList<>();
        for (Screening s : screenings) {
            activeIds.add(s.getScreeningId());
        }
        return IdGenerator.generateNextId(activeIds, "SCR", 3);
    }

    public void addScreening(Screening screening) throws ValidationException {
        if (screening == null) {
            throw new ValidationException("Screening object cannot be null.");
        }
        screening.validate();

        Movie movie = requireMovieExists(screening.getMovieId());

        for (Screening s : screenings) {
            if (s.getScreeningId().equalsIgnoreCase(screening.getScreeningId())) {
                throw new ValidationException("Duplicate Screening ID detected: " + screening.getScreeningId());
            }
        }

        checkRoomConflict(screening, movie, null);

        screenings.add(screening);
        try {
            saveScreenings();
        } catch (UncheckedIOException e) {
            screenings.remove(screening);
            throw e;
        }
    }

    public void updateScreening(Screening updatedScreening) throws ValidationException {
        if (updatedScreening == null) {
            throw new ValidationException("Screening object cannot be null.");
        }
        updatedScreening.validate();

        Movie movie = requireMovieExists(updatedScreening.getMovieId());

        int index = -1;
        for (int i = 0; i < screenings.size(); i++) {
            if (screenings.get(i).getScreeningId().equalsIgnoreCase(updatedScreening.getScreeningId())) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new ValidationException("Screening with ID " + updatedScreening.getScreeningId() + " not found.");
        }

        checkRoomConflict(updatedScreening, movie, updatedScreening.getScreeningId());

        Screening previousScreening = screenings.set(index, updatedScreening);
        try {
            saveScreenings();
        } catch (UncheckedIOException e) {
            screenings.set(index, previousScreening);
            throw e;
        }
    }

    public void deleteScreening(String screeningId) throws ValidationException {
        int index = -1;
        for (int i = 0; i < screenings.size(); i++) {
            if (screenings.get(i).getScreeningId().equalsIgnoreCase(screeningId)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new ValidationException("Screening with ID " + screeningId + " not found.");
        }
        Screening removedScreening = screenings.remove(index);
        try {
            saveScreenings();
        } catch (UncheckedIOException e) {
            screenings.add(index, removedScreening);
            throw e;
        }
    }

    /**
     * A screening counts as "upcoming" when its start date/time is strictly after
     * the reference moment - a screening later today qualifies, one earlier today does not.
     */
    public int getUpcomingScreeningCount() {
        return getUpcomingScreeningCount(LocalDateTime.now());
    }

    /** Package-private overload so tests can pass a fixed reference instant instead of the wall clock. */
    int getUpcomingScreeningCount(LocalDateTime referenceNow) {
        int count = 0;
        for (Screening s : screenings) {
            if (s.getStartDateTime().isAfter(referenceNow)) {
                count++;
            }
        }
        return count;
    }

    public boolean hasScreeningsForMovie(String movieId) {
        if (movieId == null || movieId.trim().isEmpty()) {
            return false;
        }
        String target = movieId.trim();
        for (Screening s : screenings) {
            if (s.getMovieId().equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    private Movie findMovieById(String movieId) {
        for (Movie m : movieService.getAllMovies()) {
            if (m.getMovieId().equalsIgnoreCase(movieId)) {
                return m;
            }
        }
        return null;
    }

    private Movie requireMovieExists(String movieId) throws ValidationException {
        Movie movie = findMovieById(movieId);
        if (movie == null) {
            throw new ValidationException("Movie with ID " + movieId + " does not exist.");
        }
        return movie;
    }

    /**
     * Rejects overlapping screenings in the same room. Occupied interval is
     * [start, start + movie duration + cleanup buffer). Excludes excludeScreeningId
     * (used during update so a screening never conflicts with its own prior slot).
     * Every relevant existing screening's movie must still exist - room availability
     * cannot be verified (and is therefore never assumed clear) for an orphaned record.
     */
    private void checkRoomConflict(Screening candidate, Movie candidateMovie, String excludeScreeningId) throws ValidationException {
        LocalDateTime newStart = candidate.getStartDateTime();
        LocalDateTime newEnd = newStart.plusMinutes(candidateMovie.getDuration() + ROOM_CLEANUP_BUFFER_MINUTES);

        for (Screening existing : screenings) {
            if (excludeScreeningId != null && existing.getScreeningId().equalsIgnoreCase(excludeScreeningId)) {
                continue;
            }
            if (!existing.getRoom().equalsIgnoreCase(candidate.getRoom())) {
                continue;
            }

            Movie existingMovie = findMovieById(existing.getMovieId());
            if (existingMovie == null) {
                throw new ValidationException(
                    "Cannot verify room availability because screening " + existing.getScreeningId() +
                    " references missing movie " + existing.getMovieId() + "."
                );
            }

            LocalDateTime existingStart = existing.getStartDateTime();
            LocalDateTime existingEnd = existingStart.plusMinutes(existingMovie.getDuration() + ROOM_CLEANUP_BUFFER_MINUTES);

            if (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) {
                throw new ValidationException(
                    "Screening conflicts with existing screening " + existing.getScreeningId() +
                    " in room " + existing.getRoom() + "."
                );
            }
        }
    }
}
