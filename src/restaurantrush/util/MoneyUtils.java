package restaurantrush.util;

/** Money is stored as integer cents everywhere; this class formats it. */
public final class MoneyUtils {

    private MoneyUtils() {
    }

    /** Formats integer cents as dollars with two decimals (e.g. 1800 -> "$18.00"). */
    public static String formatCents(int cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }
}
