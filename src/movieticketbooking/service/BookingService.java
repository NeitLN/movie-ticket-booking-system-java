package movieticketbooking.service;

import movieticketbooking.exception.ValidationException;
import movieticketbooking.model.Booking;
import movieticketbooking.model.Screening;
import movieticketbooking.model.Seat;
import movieticketbooking.util.FileManager;
import movieticketbooking.util.IdGenerator;
import movieticketbooking.util.ValidationUtils;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * BOOKING SERVICE — PHASE 7 (Student 3)
 * -----------------------------------------------
 * Handles all booking business logic: creating bookings, cancelling them,
 * releasing seats on cancel, querying booked seats per screening, and
 * persisting everything to data/bookings.txt.
 *
 * BookingService is the single source of truth for bookings.txt.
 * ReportService reads the same file independently (read-only).
 */
public class BookingService {

    private static final String FILE_PATH = "data/bookings.txt";

    // Standard cinema layout: 3 rows × 5 columns = 15 seats per screening
    public static final String[] SEAT_NUMBERS = {
        "A1", "A2", "A3", "A4", "A5",
        "B1", "B2", "B3", "B4", "B5",
        "C1", "C2", "C3", "C4", "C5"
    };

    private final List<Booking> bookings;
    private final ScreeningService screeningService;

    public BookingService(ScreeningService screeningService) {
        if (screeningService == null) {
            throw new IllegalArgumentException("ScreeningService dependency cannot be null.");
        }
        this.screeningService = screeningService;
        this.bookings = new ArrayList<>();
        loadBookings();
    }

    // -------------------------------------------------------------------------
    // Load / Save
    // -------------------------------------------------------------------------

    public void loadBookings() {
        bookings.clear();
        List<String> lines = FileManager.readLines(FILE_PATH);
        for (String line : lines) {
            try {
                bookings.add(Booking.fromTxtLine(line));
            } catch (ValidationException e) {
                System.err.println("Skipping malformed booking record: " + line + " — " + e.getMessage());
            }
        }
    }

    public void reload() {
        loadBookings();
    }

    private void saveBookings() {
        List<String> lines = new ArrayList<>();
        for (Booking b : bookings) {
            lines.add(b.toTxtLine());
        }
        FileManager.writeLines(FILE_PATH, lines);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /** Returns a defensive copy of all bookings. */
    public List<Booking> getAllBookings() {
        return new ArrayList<>(bookings);
    }

    /** Finds a booking by ID (case-insensitive). Returns null if not found. */
    public Booking getBookingById(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) return null;
        String target = bookingId.trim();
        for (Booking b : bookings) {
            if (b.getBookingId().equalsIgnoreCase(target)) return b;
        }
        return null;
    }

    /** Returns all bookings for a given screening ID. */
    public List<Booking> getBookingsByScreeningId(String screeningId) {
        List<Booking> result = new ArrayList<>();
        if (screeningId == null || screeningId.trim().isEmpty()) return result;
        String target = screeningId.trim();
        for (Booking b : bookings) {
            if (b.getScreeningId().equalsIgnoreCase(target)) result.add(b);
        }
        return result;
    }

    /** Returns all bookings matching a customer name substring (case-insensitive). */
    public List<Booking> searchByCustomerName(String query) {
        List<Booking> result = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) return new ArrayList<>(bookings);
        String lq = query.trim().toLowerCase();
        for (Booking b : bookings) {
            if (b.getCustomerName().toLowerCase().contains(lq)) result.add(b);
        }
        return result;
    }

    /** Returns all bookings matching a phone number substring. */
    public List<Booking> searchByPhone(String query) {
        List<Booking> result = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) return new ArrayList<>(bookings);
        String lq = query.trim();
        for (Booking b : bookings) {
            if (b.getPhone().contains(lq)) result.add(b);
        }
        return result;
    }

    /**
     * Returns the set of seat numbers that are currently booked (CONFIRMED)
     * for a given screening. Cancelled bookings release their seats.
     */
    public List<String> getBookedSeatNumbers(String screeningId) {
        List<String> booked = new ArrayList<>();
        if (screeningId == null || screeningId.trim().isEmpty()) return booked;
        String target = screeningId.trim();
        for (Booking b : bookings) {
            if (b.getScreeningId().equalsIgnoreCase(target) &&
                    "CONFIRMED".equalsIgnoreCase(b.getStatus())) {
                for (Seat s : b.getSeats()) {
                    booked.add(s.getSeatNumber());
                }
            }
        }
        return booked;
    }

    /** Returns true if the screening still has at least one available seat. */
    public boolean hasAvailableSeats(String screeningId) {
        List<String> booked = getBookedSeatNumbers(screeningId);
        return booked.size() < SEAT_NUMBERS.length;
    }

    /**
     * Returns true when any booking-history record references the screening.
     * Cancelled bookings still remain in history, so deleting their screening
     * would create an orphan record and break Phase 9 data consistency.
     */
    public boolean hasBookingsForScreening(String screeningId) {
        if (screeningId == null || screeningId.trim().isEmpty()) return false;
        String target = screeningId.trim();
        for (Booking b : bookings) {
            if (b.getScreeningId().equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // ID generation
    // -------------------------------------------------------------------------

    public String generateNextBookingId() {
        List<String> ids = new ArrayList<>();
        for (Booking b : bookings) ids.add(b.getBookingId());
        return IdGenerator.generateNextId(ids, "BKG", 3);
    }

    // -------------------------------------------------------------------------
    // Mutators
    // -------------------------------------------------------------------------

    /**
     * Creates a new confirmed booking.
     *
     * Validations (Phase 7 requirements):
     *  - Screening must exist.
     *  - customerName and phone must not be blank; phone must match pattern.
     *  - At least one seat must be selected.
     *  - None of the selected seats may already be booked for this screening.
     *  - Booking ID must be unique.
     */
    public Booking createBooking(String screeningId,
                                 String customerName,
                                 String phone,
                                 List<String> selectedSeatNumbers) throws ValidationException {

        // 1. Screening must exist
        if (screeningId == null || screeningId.trim().isEmpty()) {
            throw new ValidationException("A screening must be selected.");
        }
        Screening screening = screeningService.getScreeningById(screeningId.trim());
        if (screening == null) {
            throw new ValidationException("Screening '" + screeningId + "' does not exist.");
        }

        // 2. Customer info
        ValidationUtils.validateRequiredText(customerName, "Customer name");
        ValidationUtils.validatePhone(phone);

        // 3. At least one seat
        if (selectedSeatNumbers == null || selectedSeatNumbers.isEmpty()) {
            throw new ValidationException("At least one seat must be selected.");
        }

        // 4. Normalize and validate the selected seats against the fixed A1-C5 layout.
        //    This also rejects blank entries and duplicates before any data is changed.
        List<String> deduped = new ArrayList<>();
        for (String sn : selectedSeatNumbers) {
            String normalized = sn == null ? "" : sn.trim().toUpperCase();
            Seat candidate = new Seat(normalized, false);
            candidate.validate();
            String seatNumber = candidate.getSeatNumber();
            if (deduped.contains(seatNumber)) {
                throw new ValidationException("Duplicate seat selected: " + seatNumber);
            }
            deduped.add(seatNumber);
        }

        // 5. No seat already booked for this screening
        List<String> alreadyBooked = getBookedSeatNumbers(screeningId.trim());
        for (String sn : deduped) {
            if (alreadyBooked.contains(sn)) {
                throw new ValidationException("Seat " + sn + " is already booked for this screening.");
            }
        }

        // 6. Build seat objects and calculate total price
        List<Seat> seats = new ArrayList<>();
        for (String sn : deduped) {
            seats.add(new Seat(sn, true));
        }
        double totalPrice = deduped.size() * screening.getBasePrice();

        // 7. Generate unique booking ID
        String bookingId = generateNextBookingId();

        // 8. Construct, validate and persist
        Booking booking = new Booking(
            bookingId,
            screeningId.trim(),
            customerName.trim(),
            phone.trim(),
            seats,
            totalPrice,
            "CONFIRMED"
        );
        booking.validate();

        bookings.add(booking);
        try {
            saveBookings();
        } catch (UncheckedIOException e) {
            bookings.remove(booking);
            throw e;
        }
        return booking;
    }

    /**
     * Cancels a booking and releases its seats back into the pool.
     * Already-cancelled bookings cannot be cancelled again.
     */
    public void cancelBooking(String bookingId) throws ValidationException {
        Booking booking = getBookingById(bookingId);
        if (booking == null) {
            throw new ValidationException("Booking '" + bookingId + "' not found.");
        }
        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            throw new ValidationException("Booking '" + bookingId + "' is already cancelled.");
        }
        String previousStatus = booking.getStatus();
        booking.setStatus("CANCELLED");
        try {
            saveBookings();
        } catch (UncheckedIOException e) {
            booking.setStatus(previousStatus);
            throw e;
        }
    }

    /**
     * Updates mutable customer-contact fields (name and/or phone).
     * Cancelled bookings can still have their contact info corrected.
     */
    public void updateCustomerInfo(String bookingId,
                                   String newCustomerName,
                                   String newPhone) throws ValidationException {
        Booking booking = getBookingById(bookingId);
        if (booking == null) {
            throw new ValidationException("Booking '" + bookingId + "' not found.");
        }
        ValidationUtils.validateRequiredText(newCustomerName, "Customer name");
        ValidationUtils.validatePhone(newPhone);

        String oldName = booking.getCustomerName();
        String oldPhone = booking.getPhone();
        booking.setCustomerName(newCustomerName.trim());
        booking.setPhone(newPhone.trim());
        try {
            saveBookings();
        } catch (UncheckedIOException e) {
            booking.setCustomerName(oldName);
            booking.setPhone(oldPhone);
            throw e;
        }
    }
}
