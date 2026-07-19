package movieticketbooking.model;

import movieticketbooking.exception.ValidationException;
import movieticketbooking.util.ValidationUtils;
import java.util.ArrayList;
import java.util.List;

public class Booking {
    private static final String DEFAULT_STATUS = "CONFIRMED";

    private String bookingId;
    private String screeningId;
    private String customerName;
    private String phone;
    private List<Seat> seats;
    private double totalPrice;
    private String status;

    public Booking(String bookingId, String screeningId, String customerName, String phone, List<Seat> seats, double totalPrice, String status) {
        this.bookingId = bookingId;
        this.screeningId = screeningId;
        this.customerName = customerName;
        this.phone = phone;
        this.seats = seats == null ? new ArrayList<>() : new ArrayList<>(seats);
        this.totalPrice = totalPrice;
        this.status = normalizeStatus(status);
    }

    public void validate() throws ValidationException {
        ValidationUtils.validateId(bookingId, "Booking ID");
        ValidationUtils.validateId(screeningId, "Screening ID");
        ValidationUtils.validateId(customerName, "Customer Name");
        ValidationUtils.validatePhone(phone);
        if (seats == null || seats.isEmpty()) {
            throw new ValidationException("Booking must have at least one seat selected.");
        }
        for (Seat seat : seats) {
            if (seat == null) {
                throw new ValidationException("Booking seats cannot contain null elements.");
            }
            seat.validate();
        }
        ValidationUtils.validateNonNegativePrice(totalPrice, "Total Price");
        status = normalizeStatus(status);
    }

    public String toTxtLine() {
        List<String> seatNums = new ArrayList<>();
        for (Seat s : seats) {
            seatNums.add(s.getSeatNumber());
        }
        String seatsSerialized = String.join(",", seatNums);
        return String.join("|",
            bookingId,
            screeningId,
            customerName,
            phone,
            seatsSerialized,
            String.valueOf(totalPrice),
            status
        );
    }

    public static Booking fromTxtLine(String line) throws ValidationException {
        if (line == null || line.trim().isEmpty()) {
            throw new ValidationException("Cannot parse empty booking line.");
        }
        String[] tokens = line.split("\\|", -1);
        if (tokens.length < 6 || tokens.length > 7) {
            throw new ValidationException("Incorrect booking line tokens length: " + tokens.length);
        }
        try {
            String bookingId = tokens[0].trim();
            String screeningId = tokens[1].trim();
            String name = tokens[2].trim();
            String phone = tokens[3].trim();
            
            List<Seat> seatList = new ArrayList<>();
            String seatsPart = tokens[4].trim();
            if (!seatsPart.isEmpty()) {
                String[] seatNums = seatsPart.split(",");
                for (String sn : seatNums) {
                    seatList.add(new Seat(sn.trim(), true));
                }
            }
            double totalPrice = Double.parseDouble(tokens[5].trim());
            String status = tokens.length == 7 ? tokens[6].trim() : DEFAULT_STATUS;
            Booking booking = new Booking(bookingId, screeningId, name, phone, seatList, totalPrice, status);
            booking.validate();
            return booking;
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Failed parsing booking tokens: " + e.getMessage());
        }
    }

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getScreeningId() { return screeningId; }
    public void setScreeningId(String screeningId) { this.screeningId = screeningId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public List<Seat> getSeats() { return new ArrayList<>(seats); }
    public void setSeats(List<Seat> seats) {
        this.seats = seats == null ? new ArrayList<>() : new ArrayList<>(seats);
    }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = normalizeStatus(status); }

    private static String normalizeStatus(String status) {
        return status == null || status.trim().isEmpty() ? DEFAULT_STATUS : status.trim();
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId='" + bookingId + '\'' +
                ", screeningId='" + screeningId + '\'' +
                ", customer='" + customerName + '\'' +
                ", seats=" + seats +
                ", totalPrice=" + totalPrice +
                ", status='" + status + '\'' +
                '}';
    }
}
