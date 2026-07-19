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

    public void validate() throws ValidationException {
        ValidationUtils.validateRequiredText(seatNumber, "Seat number");
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
