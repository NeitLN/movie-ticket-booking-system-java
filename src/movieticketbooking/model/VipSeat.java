package movieticketbooking.model;

import movieticketbooking.exception.ValidationException;

public class VipSeat extends Seat {
    public static final double VIP_MULTIPLIER = 1.5;

    public VipSeat(String seatNumber, boolean booked) {
        super(seatNumber, booked);
    }
    
    public VipSeat(String seatNumber) {
        super(seatNumber);
    }

    @Override
    public double calculatePrice(double basePrice) {
        return basePrice * VIP_MULTIPLIER;
    }
}
