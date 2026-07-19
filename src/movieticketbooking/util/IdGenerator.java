package movieticketbooking.util;

import java.util.List;

public final class IdGenerator {
    private IdGenerator() {}

    public static String generateNextId(List<String> activeIds, String prefix, int digitsLength) {
        int maxIndex = 0;
        for (String id : activeIds) {
            if (id != null && id.startsWith(prefix) && id.length() == (prefix.length() + digitsLength)) {
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
        return String.format(formatString, prefix, nextIndex);
    }
}
