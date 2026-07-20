package movieticketbooking.tests;

import movieticketbooking.exception.ValidationException;
import movieticketbooking.model.Booking;
import movieticketbooking.model.Screening;
import movieticketbooking.service.BookingService;
import movieticketbooking.service.MovieService;
import movieticketbooking.service.ReportService;
import movieticketbooking.service.ScreeningService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * Dependency-free regression test for the mandatory booking-system business rules.
 * Run from the project root. Existing screenings.txt and bookings.txt are backed
 * up and restored in a finally block, even when an assertion fails.
 */
public final class BookingSystemRegressionTest {
    private static final Path SCREENINGS = Paths.get("data/screenings.txt");
    private static final Path BOOKINGS = Paths.get("data/bookings.txt");
    private static int passed;

    private BookingSystemRegressionTest() {}

    public static void main(String[] args) throws Exception {
        byte[] originalScreenings = readIfExists(SCREENINGS);
        byte[] originalBookings = readIfExists(BOOKINGS);
        boolean screeningsExisted = Files.exists(SCREENINGS);
        boolean bookingsExisted = Files.exists(BOOKINGS);

        try {
            Files.createDirectories(Paths.get("data"));
            Files.writeString(SCREENINGS,
                "SCR900|MOV001|2030-01-01|10:00|Test Room 1|100000.0\n" +
                "SCR901|MOV001|2030-01-01|14:00|Test Room 2|100000.0\n",
                StandardCharsets.UTF_8);
            Files.deleteIfExists(BOOKINGS);

            MovieService movieService = new MovieService();
            ScreeningService screeningService = new ScreeningService(movieService);
            BookingService bookingService = new BookingService(screeningService);
            screeningService.setBookingService(bookingService);

            check(Files.exists(BOOKINGS), "Missing bookings.txt is recreated automatically");

            expectValidation(
                () -> bookingService.createBooking("SCR900", "Test User", "0901234567", List.of("Z99")),
                "Seat outside A1-C5 is rejected"
            );

            Booking multiSeat = bookingService.createBooking(
                "SCR900", "Test User", "0901234567", Arrays.asList("a1", "A2"));
            check(multiSeat.getSeats().size() == 2, "Multiple seats can be booked");
            check(Math.abs(multiSeat.getTotalPrice() - 200000.0) < 0.001,
                "Total price equals selected seats multiplied by base price");

            expectValidation(
                () -> bookingService.createBooking("SCR900", "Test User", "0901234567", List.of("A3", "A3")),
                "Duplicate seats in one request are rejected"
            );
            expectValidation(
                () -> bookingService.createBooking("SCR900", "Second User", "0912345678", List.of("A1")),
                "An already sold seat cannot be booked again"
            );
            expectValidation(
                () -> screeningService.deleteScreening("SCR900"),
                "A screening with a confirmed booking cannot be deleted"
            );

            bookingService.cancelBooking(multiSeat.getBookingId());
            check(bookingService.getBookedSeatNumbers("SCR900").isEmpty(),
                "Cancelling a booking releases its seats");
            expectValidation(
                () -> screeningService.deleteScreening("SCR900"),
                "A cancelled booking remains protected as booking-history data"
            );

            screeningService.deleteScreening("SCR901");
            check(screeningService.getScreeningById("SCR901") == null,
                "A screening without bookings can be deleted");

            screeningService.addScreening(new Screening(
                "SCR902", "MOV001", LocalDate.of(2030, 1, 2),
                LocalTime.of(10, 0), "Test Room 3", 100000.0));
            bookingService.createBooking(
                "SCR902", "Revenue User", "0923456789", List.of("B1"));

            BookingService reloadedBookings = new BookingService(screeningService);
            check(reloadedBookings.getAllBookings().size() == 2,
                "Booking data persists after services are recreated");

            Files.writeString(BOOKINGS,
                "BKG999|SCR902|Invalid Seat User|0934567890|Z99|100000.0|CONFIRMED\n",
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND);
            BookingService cleanedReload = new BookingService(screeningService);
            check(cleanedReload.getAllBookings().size() == 2,
                "Malformed booking records with invalid seats are skipped safely");

            ReportService reportService = new ReportService(movieService, screeningService);
            check(reportService.getConfirmedBookingCount() == 1,
                "Revenue report excludes cancelled bookings");
            check(reportService.getTicketsSold() == 1,
                "Revenue report counts tickets from confirmed bookings only");
            check(reportService.getGrossRevenue().compareTo(new BigDecimal("100000.0")) == 0,
                "Revenue report calculates the confirmed gross revenue correctly");

            System.out.println("RESULT: " + passed + "/" + passed + " checks passed.");
        } finally {
            restore(SCREENINGS, originalScreenings, screeningsExisted);
            restore(BOOKINGS, originalBookings, bookingsExisted);
        }
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError("FAILED: " + description);
        }
        passed++;
        System.out.println("PASS " + passed + ": " + description);
    }

    private static void expectValidation(CheckedAction action, String description) throws Exception {
        try {
            action.run();
            throw new AssertionError("FAILED: " + description + " (no ValidationException was thrown)");
        } catch (ValidationException expected) {
            check(true, description);
        }
    }

    private static byte[] readIfExists(Path path) throws Exception {
        return Files.exists(path) ? Files.readAllBytes(path) : new byte[0];
    }

    private static void restore(Path path, byte[] original, boolean existed) throws Exception {
        if (existed) {
            Files.write(path, original);
        } else {
            Files.deleteIfExists(path);
        }
    }

    @FunctionalInterface
    private interface CheckedAction {
        void run() throws Exception;
    }
}
