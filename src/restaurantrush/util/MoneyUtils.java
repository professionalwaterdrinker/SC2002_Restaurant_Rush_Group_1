package restaurantrush.util;

/** Money is stored as integer cents everywhere; this class formats and discounts it. */
public final class MoneyUtils {

    private MoneyUtils() {
    }

    /** Formats integer cents as dollars with two decimals (e.g. 1800 -> "$18.00"). */
    public static String formatCents(int cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }

    /** Takes percent% off an amount in cents, rounded to the nearest cent. */
    public static int percentOff(int cents, int percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Discount must be between 0 and 100");
        }
        return (cents * (100 - percent) + 50) / 100;
    }
}
