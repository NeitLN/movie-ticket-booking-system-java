package movieticketbooking.util;

import java.util.List;
import java.util.Locale;

public final class IdGenerator {
    private IdGenerator() {}

    public static String generateNextId(List<String> activeIds, String prefix, int digitsLength) {
        int maxIndex = 0;
        for (String id : activeIds) {
            if (isConformingId(id, prefix, digitsLength)) {
                try {
                    String numberPart = id.substring(prefix.length());
                    int index = Integer.parseInt(numberPart);
                    if (index > maxIndex) {
                        maxIndex = index;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        int nextIndex = maxIndex + 1;
        String formatString = "%s%0" + digitsLength + "d";
        String candidate = String.format(Locale.ROOT, formatString, prefix, nextIndex);
        while (containsIgnoreCase(activeIds, candidate)) {
            nextIndex++;
            candidate = String.format(Locale.ROOT, formatString, prefix, nextIndex);
        }
        return candidate;
    }

    private static boolean isConformingId(String id, String prefix, int digitsLength) {
        if (id == null || prefix == null || digitsLength <= 0 ||
                !id.startsWith(prefix) || id.length() < prefix.length() + digitsLength) {
            return false;
        }
        for (int i = prefix.length(); i < id.length(); i++) {
            char character = id.charAt(i);
            if (character < '0' || character > '9') {
                return false;
            }
        }
        return true;
    }

    private static boolean containsIgnoreCase(List<String> ids, String candidate) {
        for (String id : ids) {
            if (id != null && id.equalsIgnoreCase(candidate)) {
                return true;
            }
        }
        return false;
    }
}
