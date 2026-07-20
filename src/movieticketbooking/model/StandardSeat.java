package movieticketbooking.model;

import movieticketbooking.exception.ValidationException;

public class StandardSeat extends Seat {
    public StandardSeat(String seatNumber, boolean booked) {
        super(seatNumber, booked);
    }
    
    public StandardSeat(String seatNumber) {
        super(seatNumber);
    }

    @Override
    public double calculatePrice(double basePrice) {
        return basePrice;
    }
}
