package movieticketbooking.service;

import movieticketbooking.exception.ValidationException;
import movieticketbooking.model.Booking;
import movieticketbooking.model.Movie;
import movieticketbooking.model.Screening;
import movieticketbooking.model.Seat;
import movieticketbooking.model.VipSeat;
import movieticketbooking.util.FileManager;
import movieticketbooking.util.FormatUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InvoiceService {
    private static final String EXPORT_DIR = "exports/invoices/";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final ScreeningService screeningService;
    private final MovieService movieService;

    public InvoiceService(ScreeningService screeningService, MovieService movieService) {
        if (screeningService == null || movieService == null) {
            throw new IllegalArgumentException("Dependencies cannot be null.");
        }
        this.screeningService = screeningService;
        this.movieService = movieService;
    }

    public Path exportInvoice(Booking booking) throws ValidationException {
        if (booking == null) {
            throw new ValidationException("Cannot export a null booking.");
        }
        
        Screening screening = screeningService.getScreeningById(booking.getScreeningId());
        if (screening == null) {
            throw new ValidationException("Screening '" + booking.getScreeningId() + "' not found.");
        }
        
        Movie movie = null;
        for (Movie m : movieService.getAllMovies()) {
            if (m.getMovieId().equalsIgnoreCase(screening.getMovieId())) {
                movie = m;
                break;
            }
        }
        if (movie == null) {
            throw new ValidationException("Movie '" + screening.getMovieId() + "' not found.");
        }
        
        if (booking.getBookingId() == null || booking.getBookingId().trim().isEmpty()) {
            throw new ValidationException("Booking ID cannot be null or blank.");
        }
        if (!booking.getBookingId().matches("^[A-Za-z0-9_-]+$")) {
            throw new ValidationException("Booking ID contains invalid characters for a filename.");
        }
        String safeId = booking.getBookingId();
        
        Path dirPath = Paths.get(EXPORT_DIR).toAbsolutePath().normalize();
        try {
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create invoice directory", e);
        }
        
        Path filePath = dirPath.resolve("invoice-" + safeId + ".txt").normalize();
        if (!filePath.startsWith(dirPath)) {
             throw new ValidationException("Path traversal attempt blocked.");
        }
        
        List<String> lines = new ArrayList<>();
        lines.add("========================================");
        lines.add("      MOVIE TICKET BOOKING SYSTEM       ");
        lines.add("========================================");
        lines.add("                INVOICE                 ");
        lines.add("========================================");
        lines.add("Booking ID    : " + booking.getBookingId());
        lines.add("Status        : " + booking.getStatus());
        lines.add("Customer Name : " + booking.getCustomerName());
        lines.add("Phone         : " + booking.getPhone());
        lines.add("");
        lines.add("Movie         : " + movie.getMovieId() + " - " + movie.getTitle());
        lines.add("Screening ID  : " + screening.getScreeningId());
        lines.add("Date          : " + screening.getScreeningDate().format(DATE_FMT));
        lines.add("Time          : " + screening.getStartTime().format(TIME_FMT));
        lines.add("Room          : " + screening.getRoom());
        lines.add("");
        lines.add("Seats (" + booking.getSeats().size() + " tickets):");
        for (Seat seat : booking.getSeats()) {
            Seat typedSeat = Seat.create(seat.getSeatNumber());
            String type = typedSeat instanceof VipSeat ? "VIP" : "Standard";
            lines.add("- " + seat.getSeatNumber() + " (" + type + ")");
        }
        lines.add("");
        lines.add("Total Price   : " + FormatUtils.formatVnd(booking.getTotalPrice()));
        lines.add("========================================");
        lines.add("      Thank you for your booking!       ");
        lines.add("========================================");
        
        FileManager.writeLines(filePath.toString(), lines);
        
        return filePath;
    }
}
