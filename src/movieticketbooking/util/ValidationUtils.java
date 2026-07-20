package movieticketbooking.util;

import movieticketbooking.exception.ValidationException;
import java.time.LocalDate;
import java.time.LocalTime;

public final class ValidationUtils {
    private ValidationUtils() {}

    public static void validateId(String id, String fieldName) throws ValidationException {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
    }

    public static void validateTitle(String title) throws ValidationException {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Movie title cannot be empty.");
        }
    }

    public static void validateRequiredText(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
    }

    public static void validateDuration(int duration) throws ValidationException {
        if (duration <= 0) {
            throw new ValidationException("Movie duration must be greater than 0 minutes.");
        }
    }

    public static void validatePrice(double price, String fieldName) throws ValidationException {
        if (!Double.isFinite(price) || price <= 0.0) {
            throw new ValidationException(fieldName + " must be greater than 0.");
        }
    }

    public static void validateNonNegativePrice(double price, String fieldName) throws ValidationException {
        if (!Double.isFinite(price) || price < 0.0) {
            throw new ValidationException(fieldName + " must be a finite, non-negative value.");
        }
    }

    public static void validateScreeningDateTime(LocalDate date, LocalTime time) throws ValidationException {
        if (date == null) {
            throw new ValidationException("Screening date cannot be null.");
        }
        if (time == null) {
            throw new ValidationException("Screening start time cannot be null.");
        }
    }

    public static void validatePhone(String phone) throws ValidationException {
        if (phone == null || !phone.matches("^0\\d{9}$")) {
            throw new ValidationException("Invalid phone number format. Must be 10 digits starting with 0.");
        }
    }

    public static void validateSeatsNotEmpty(int numSeats) throws ValidationException {
        if (numSeats == 0) {
            throw new ValidationException("Booking must have at least one seat selected.");
        }
    }

    /**
     * Rejects the pipe-delimited TXT record delimiter and line breaks. Any free-text
     * field that gets joined into a "|"-separated TXT line (e.g. Booking's customerName,
     * phone, status) must pass this check before the record is persisted - otherwise the
     * character shifts every subsequent field on the line, or a literal newline splits one
     * record into two unparseable physical lines, silently corrupting the source file.
     */
    public static void validateTxtSafeField(String value, String fieldName) throws ValidationException {
        if (value != null && (value.indexOf('|') >= 0 || value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0)) {
            throw new ValidationException(fieldName + " cannot contain | or line-break characters.");
        }
    }
}
