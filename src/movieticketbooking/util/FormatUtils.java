package movieticketbooking.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class FormatUtils {
    private FormatUtils() {}

    public static String formatVnd(double amount) {
        return formatVnd(BigDecimal.valueOf(amount));
    }

    public static String formatVnd(BigDecimal amount) {
        DecimalFormat format = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.US));
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(amount) + " ₫";
    }
}
