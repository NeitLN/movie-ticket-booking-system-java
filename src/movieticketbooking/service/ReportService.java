package movieticketbooking.service;

import movieticketbooking.exception.ValidationException;
import movieticketbooking.model.Booking;
import movieticketbooking.model.Movie;
import movieticketbooking.model.Screening;
import movieticketbooking.util.FileManager;

import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Read-only reporting service over data/bookings.txt. Never creates, updates,
 * deletes, or rewrites booking records - it only aggregates what MovieService
 * and ScreeningService (and the booking file) already say.
 *
 * Revenue rule: only bookings whose status is "CONFIRMED" (case-insensitive)
 * count toward Gross Revenue / tickets sold / confirmed-booking totals.
 * "CANCELLED" bookings are excluded. Any other status is treated as unknown -
 * excluded from revenue and warned about, never silently counted.
 *
 * "Gross Revenue" uses each booking's own stored totalPrice - historical
 * booking totals are never recalculated from the current screening price,
 * since prices may have changed since the booking was made.
 */
public class ReportService {
    private static final String FILE_PATH = "data/bookings.txt";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private final MovieService movieService;
    private final ScreeningService screeningService;
    private final List<Booking> bookings = new ArrayList<>();

    private int lastLoadValidCount;
    private int lastLoadInvalidCount;
    private boolean lastLoadFailed;
    private String lastLoadErrorMessage;

    public ReportService(MovieService movieService, ScreeningService screeningService) {
        if (movieService == null || screeningService == null) {
            throw new IllegalArgumentException("MovieService and ScreeningService dependencies cannot be null.");
        }
        this.movieService = movieService;
        this.screeningService = screeningService;
        reloadBookings();
    }

    /**
     * Re-reads bookings.txt. On read failure, the previously loaded booking list
     * is left untouched (never wiped to a false empty/zero state) and
     * isLastLoadFailed()/getLastLoadErrorMessage() report the failure to the caller.
     * Never calls FileManager.writeLines - loading never saves or normalizes the file.
     */
    public void reloadBookings() {
        List<String> lines;
        try {
            lines = FileManager.readLines(FILE_PATH);
        } catch (UncheckedIOException e) {
            lastLoadFailed = true;
            lastLoadErrorMessage = "Unable to read bookings data: " + e.getMessage();
            return;
        }

        List<Booking> loaded = new ArrayList<>();
        int valid = 0;
        int invalid = 0;
        for (String line : lines) {
            try {
                Booking booking = Booking.fromTxtLine(line);
                loaded.add(booking);
                valid++;
                if (!isRevenueEligible(booking.getStatus()) && !isCancelled(booking.getStatus())) {
                    System.err.println(
                        "Warning: booking " + booking.getBookingId() +
                        " has unrecognized status '" + booking.getStatus() + "'; excluded from revenue."
                    );
                }
            } catch (ValidationException e) {
                invalid++;
                System.err.println("Skipping malformed booking record: " + line + " - " + e.getMessage());
            }
        }
        if (!lines.isEmpty() && valid == 0 && invalid > 0) {
            System.err.println(
                "Booking data file contains only invalid records. " +
                "The original file was not replaced."
            );
        }

        bookings.clear();
        bookings.addAll(loaded);
        lastLoadValidCount = valid;
        lastLoadInvalidCount = invalid;
        lastLoadFailed = false;
        lastLoadErrorMessage = null;
    }

    public boolean isLastLoadFailed() {
        return lastLoadFailed;
    }

    public String getLastLoadErrorMessage() {
        return lastLoadErrorMessage;
    }

    public int getLastLoadInvalidCount() {
        return lastLoadInvalidCount;
    }

    public int getLastLoadValidCount() {
        return lastLoadValidCount;
    }

    public List<Booking> getAllValidBookings() {
        return new ArrayList<>(bookings);
    }

    private static boolean isRevenueEligible(String status) {
        return status != null && STATUS_CONFIRMED.equalsIgnoreCase(status.trim());
    }

    private static boolean isCancelled(String status) {
        return status != null && STATUS_CANCELLED.equalsIgnoreCase(status.trim());
    }

    public int getConfirmedBookingCount() {
        int count = 0;
        for (Booking b : bookings) {
            if (isRevenueEligible(b.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public int getTicketsSold() {
        int total = 0;
        for (Booking b : bookings) {
            if (isRevenueEligible(b.getStatus())) {
                total += b.getSeats().size();
            }
        }
        return total;
    }

    public BigDecimal getGrossRevenue() {
        BigDecimal total = BigDecimal.ZERO;
        for (Booking b : bookings) {
            if (isRevenueEligible(b.getStatus())) {
                total = total.add(BigDecimal.valueOf(b.getTotalPrice()));
            }
        }
        return total;
    }

    public BigDecimal getAverageBookingValue() {
        int count = getConfirmedBookingCount();
        if (count == 0) {
            return BigDecimal.ZERO;
        }
        return getGrossRevenue().divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    /**
     * Counts every loaded booking (any status) whose screening is missing, or
     * whose screening's movie is missing - a general data-quality indicator,
     * independent of revenue eligibility.
     */
    public int getUnresolvedBookingCount() {
        int count = 0;
        for (Booking b : bookings) {
            Screening screening = resolveScreening(b);
            if (screening == null || resolveMovie(screening) == null) {
                count++;
            }
        }
        return count;
    }

    private Screening resolveScreening(Booking booking) {
        return screeningService.getScreeningById(booking.getScreeningId());
    }

    private Movie resolveMovie(Screening screening) {
        for (Movie m : movieService.getAllMovies()) {
            if (m.getMovieId().equalsIgnoreCase(screening.getMovieId())) {
                return m;
            }
        }
        return null;
    }

    /**
     * Resolves a display label for a booking's movie without ever inventing data:
     * a real title when resolvable, otherwise a clearly labeled placeholder.
     */
    private String resolveMovieLabel(Booking booking) {
        Screening screening = resolveScreening(booking);
        if (screening == null) {
            return "Missing screening: " + booking.getScreeningId();
        }
        Movie movie = resolveMovie(screening);
        if (movie == null) {
            return "Missing movie: " + screening.getMovieId();
        }
        return movie.getTitle();
    }

    public Map<String, BigDecimal> getRevenueByMovie() {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Booking b : bookings) {
            if (!isRevenueEligible(b.getStatus())) {
                continue;
            }
            String label = resolveMovieLabel(b);
            result.merge(label, BigDecimal.valueOf(b.getTotalPrice()), BigDecimal::add);
        }
        return result;
    }

    public Map<String, Integer> getBookingCountByMovie() {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Booking b : bookings) {
            if (!isRevenueEligible(b.getStatus())) {
                continue;
            }
            String label = resolveMovieLabel(b);
            result.merge(label, 1, Integer::sum);
        }
        return result;
    }

    /**
     * Revenue grouped by screening date. Bookings whose screening is missing have
     * no reliable date and are excluded here (never assigned a fake date) - see
     * getUnresolvedBookingCount() for visibility into how many records that affects.
     */
    public Map<LocalDate, BigDecimal> getRevenueByScreeningDate() {
        Map<LocalDate, BigDecimal> result = new TreeMap<>();
        for (Booking b : bookings) {
            if (!isRevenueEligible(b.getStatus())) {
                continue;
            }
            Screening screening = resolveScreening(b);
            if (screening == null) {
                continue;
            }
            result.merge(screening.getScreeningDate(), BigDecimal.valueOf(b.getTotalPrice()), BigDecimal::add);
        }
        return result;
    }

    /** Unfiltered, per-screening revenue breakdown - equivalent to getReportData(ReportFilter.all()).getRows(). */
    public List<ScreeningRevenueRow> getRevenueByScreening() {
        return getReportData(ReportFilter.all()).getRows();
    }

    /**
     * Builds the filtered report used by RevenueReportPanel. Date filtering is based
     * on screening date (Booking has no guaranteed sale timestamp). A booking whose
     * screening is missing cannot be placed in a date range, so when a date filter is
     * active such bookings are excluded and counted in ReportData.unresolvedExcludedCount
     * rather than silently assigned to "now".
     */
    public ReportData getReportData(ReportFilter filter) {
        if (filter == null) {
            filter = ReportFilter.all();
        }
        boolean dateFilterActive = filter.getStartDate() != null || filter.getEndDate() != null;

        Map<String, ScreeningAccumulator> byScreening = new LinkedHashMap<>();
        int unresolvedExcluded = 0;

        for (Booking b : bookings) {
            if (!isRevenueEligible(b.getStatus())) {
                continue;
            }

            Screening screening = resolveScreening(b);

            if (filter.getMovieId() != null) {
                String movieId = screening != null ? screening.getMovieId() : null;
                if (movieId == null || !movieId.equalsIgnoreCase(filter.getMovieId())) {
                    continue;
                }
            }

            LocalDate screeningDate = screening != null ? screening.getScreeningDate() : null;

            if (dateFilterActive) {
                if (screeningDate == null) {
                    unresolvedExcluded++;
                    continue;
                }
                if (filter.getStartDate() != null && screeningDate.isBefore(filter.getStartDate())) {
                    continue;
                }
                if (filter.getEndDate() != null && screeningDate.isAfter(filter.getEndDate())) {
                    continue;
                }
            }

            String key = b.getScreeningId();
            ScreeningAccumulator acc = byScreening.computeIfAbsent(key,
                k -> new ScreeningAccumulator(b.getScreeningId(), resolveMovieLabel(b), screeningDate));
            acc.confirmedBookings++;
            acc.ticketsSold += b.getSeats().size();
            acc.grossRevenue = acc.grossRevenue.add(BigDecimal.valueOf(b.getTotalPrice()));
        }

        List<ScreeningRevenueRow> rows = new ArrayList<>();
        for (ScreeningAccumulator acc : byScreening.values()) {
            rows.add(acc.toRow());
        }
        rows.sort(ScreeningRevenueRow.chronologicalComparator());

        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalConfirmed = 0;
        int totalTickets = 0;
        for (ScreeningRevenueRow r : rows) {
            totalRevenue = totalRevenue.add(r.getGrossRevenue());
            totalConfirmed += r.getConfirmedBookings();
            totalTickets += r.getTicketsSold();
        }
        BigDecimal avg = totalConfirmed == 0
            ? BigDecimal.ZERO
            : totalRevenue.divide(BigDecimal.valueOf(totalConfirmed), 2, RoundingMode.HALF_UP);

        return new ReportData(rows, totalRevenue, totalConfirmed, totalTickets, avg, unresolvedExcluded);
    }

    private static final class ScreeningAccumulator {
        final String screeningId;
        final String movieLabel;
        final LocalDate screeningDate;
        int confirmedBookings;
        int ticketsSold;
        BigDecimal grossRevenue = BigDecimal.ZERO;

        ScreeningAccumulator(String screeningId, String movieLabel, LocalDate screeningDate) {
            this.screeningId = screeningId;
            this.movieLabel = movieLabel;
            this.screeningDate = screeningDate;
        }

        ScreeningRevenueRow toRow() {
            return new ScreeningRevenueRow(screeningId, movieLabel, screeningDate, confirmedBookings, ticketsSold, grossRevenue);
        }
    }

    /** One row of the revenue-by-screening breakdown. screeningDate is null only when the screening itself is missing. */
    public static final class ScreeningRevenueRow {
        private final String screeningId;
        private final String movieLabel;
        private final LocalDate screeningDate;
        private final int confirmedBookings;
        private final int ticketsSold;
        private final BigDecimal grossRevenue;

        ScreeningRevenueRow(String screeningId, String movieLabel, LocalDate screeningDate,
                             int confirmedBookings, int ticketsSold, BigDecimal grossRevenue) {
            this.screeningId = screeningId;
            this.movieLabel = movieLabel;
            this.screeningDate = screeningDate;
            this.confirmedBookings = confirmedBookings;
            this.ticketsSold = ticketsSold;
            this.grossRevenue = grossRevenue;
        }

        public String getScreeningId() { return screeningId; }
        public String getMovieLabel() { return movieLabel; }
        public LocalDate getScreeningDate() { return screeningDate; }
        public int getConfirmedBookings() { return confirmedBookings; }
        public int getTicketsSold() { return ticketsSold; }
        public BigDecimal getGrossRevenue() { return grossRevenue; }

        static Comparator<ScreeningRevenueRow> chronologicalComparator() {
            return Comparator
                .comparing((ScreeningRevenueRow r) -> r.screeningDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(r -> r.screeningId);
        }
    }

    /** Immutable report filter: optional movie, optional inclusive start/end screening-date bounds. */
    public static final class ReportFilter {
        private final String movieId;
        private final LocalDate startDate;
        private final LocalDate endDate;

        private ReportFilter(String movieId, LocalDate startDate, LocalDate endDate) {
            this.movieId = movieId;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public static ReportFilter all() {
            return new ReportFilter(null, null, null);
        }

        public static ReportFilter of(String movieId, LocalDate startDate, LocalDate endDate) throws ValidationException {
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                throw new ValidationException("Start date must not be after end date.");
            }
            return new ReportFilter(movieId, startDate, endDate);
        }

        public String getMovieId() { return movieId; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
    }

    /** Aggregated result of getReportData(filter): filtered rows plus their totals. */
    public static final class ReportData {
        private final List<ScreeningRevenueRow> rows;
        private final BigDecimal grossRevenue;
        private final int confirmedBookingCount;
        private final int ticketsSold;
        private final BigDecimal averageBookingValue;
        private final int unresolvedExcludedCount;

        ReportData(List<ScreeningRevenueRow> rows, BigDecimal grossRevenue, int confirmedBookingCount,
                   int ticketsSold, BigDecimal averageBookingValue, int unresolvedExcludedCount) {
            this.rows = rows;
            this.grossRevenue = grossRevenue;
            this.confirmedBookingCount = confirmedBookingCount;
            this.ticketsSold = ticketsSold;
            this.averageBookingValue = averageBookingValue;
            this.unresolvedExcludedCount = unresolvedExcludedCount;
        }

        public List<ScreeningRevenueRow> getRows() { return new ArrayList<>(rows); }
        public BigDecimal getGrossRevenue() { return grossRevenue; }
        public int getConfirmedBookingCount() { return confirmedBookingCount; }
        public int getTicketsSold() { return ticketsSold; }
        public BigDecimal getAverageBookingValue() { return averageBookingValue; }

        /** Bookings excluded from this filtered result because their screening is missing (only relevant when a date filter is active). */
        public int getUnresolvedExcludedCount() { return unresolvedExcludedCount; }
    }
}
