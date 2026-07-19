package movieticketbooking.model;

import movieticketbooking.exception.ValidationException;
import movieticketbooking.util.ValidationUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class Screening {
    private String screeningId;
    private String movieId;
    private LocalDate screeningDate;
    private LocalTime startTime;
    private String room;
    private double basePrice;

    public Screening(String screeningId, String movieId, LocalDate screeningDate, LocalTime startTime, String room, double basePrice) {
        this.screeningId = screeningId;
        this.movieId = movieId;
        this.screeningDate = screeningDate;
        this.startTime = startTime;
        this.room = room;
        this.basePrice = basePrice;
    }

    public void validate() throws ValidationException {
        ValidationUtils.validateId(screeningId, "Screening ID");
        ValidationUtils.validateId(movieId, "Movie ID");
        ValidationUtils.validateScreeningDateTime(screeningDate, startTime);
        ValidationUtils.validateId(room, "Theater Room");
        ValidationUtils.validatePrice(basePrice, "Ticket base price");
    }

    public String toTxtLine() {
        return String.join("|",
            screeningId,
            movieId,
            screeningDate.toString(),
            startTime.toString(),
            room,
            String.valueOf(basePrice)
        );
    }

    public static Screening fromTxtLine(String line) throws ValidationException {
        if (line == null || line.trim().isEmpty()) {
            throw new ValidationException("Cannot parse empty screening line.");
        }
        String[] tokens = line.split("\\|");
        if (tokens.length != 6) {
            throw new ValidationException("Incorrect screening line tokens length: " + tokens.length);
        }
        try {
            String screeningId = tokens[0].trim();
            String movieId = tokens[1].trim();
            LocalDate date = LocalDate.parse(tokens[2].trim());
            LocalTime time = LocalTime.parse(tokens[3].trim());
            String room = tokens[4].trim();
            double price = Double.parseDouble(tokens[5].trim());
            Screening screening = new Screening(screeningId, movieId, date, time, room, price);
            screening.validate();
            return screening;
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Failed parsing screening tokens: " + e.getMessage());
        }
    }

    // Getters and Setters
    public String getScreeningId() { return screeningId; }
    public void setScreeningId(String screeningId) { this.screeningId = screeningId; }

    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }

    public LocalDate getScreeningDate() { return screeningDate; }
    public void setScreeningDate(LocalDate screeningDate) { this.screeningDate = screeningDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    /** Combines screeningDate and startTime for interval math (sorting, conflict detection). */
    public LocalDateTime getStartDateTime() {
        return LocalDateTime.of(screeningDate, startTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Screening)) return false;
        Screening other = (Screening) o;
        return screeningId != null && screeningId.equalsIgnoreCase(other.screeningId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(screeningId == null ? null : screeningId.toLowerCase());
    }

    @Override
    public String toString() {
        return "Screening{" +
                "screeningId='" + screeningId + '\'' +
                ", movieId='" + movieId + '\'' +
                ", Date=" + screeningDate +
                ", Time=" + startTime +
                ", room='" + room + '\'' +
                ", basePrice=" + basePrice +
                '}';
    }
}
