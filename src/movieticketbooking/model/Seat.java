package movieticketbooking.model;

import movieticketbooking.exception.ValidationException;
import movieticketbooking.util.ValidationUtils;

public class Seat {
    private String seatNumber;
    private boolean booked;

    public Seat(String seatNumber, boolean booked) {
        this.seatNumber = seatNumber;
        this.booked = booked;
    }

    public Seat(String seatNumber) {
        this(seatNumber, false);
    }

    public double calculatePrice(double basePrice) {
        return basePrice;
    }

    public static Seat create(String seatNumber) {
        return create(seatNumber, false);
    }

    public static Seat create(String seatNumber, boolean booked) {
        if (seatNumber != null && seatNumber.trim().toUpperCase().startsWith("B")) {
            return new VipSeat(seatNumber, booked);
        }
        return new StandardSeat(seatNumber, booked);
    }

    public void validate() throws ValidationException {
        ValidationUtils.validateRequiredText(seatNumber, "Seat number");
        seatNumber = seatNumber.trim().toUpperCase();
        if (!seatNumber.matches("^[A-C][1-5]$")) {
            throw new ValidationException(
                "Invalid seat number '" + seatNumber + "'. Valid seats are A1-A5, B1-B5, and C1-C5."
            );
        }
    }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public boolean isBooked() { return booked; }
    public void setBooked(boolean booked) { this.booked = booked; }

    @Override
    public String toString() {
        return seatNumber + (booked ? "[Booked]" : "[Available]");
    }
}
