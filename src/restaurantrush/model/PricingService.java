package restaurantrush.model;

import restaurantrush.model.menu.MenuItem;

/**
 * The ONLY place bills are calculated.
 * Order of discounts:
 *   1. Start from the item's price (combos already include their 10% off)
 *   2. Apply the customer's discount (VIP = 10%, Regular/Critic = 0%)
 *   3. Apply Happy Hour (20%) if it is active
 * Each step is rounded to the nearest cent.
 *
 * Example: VIP orders a Burger during Happy Hour
 *   1500 -> 10% off = 1350 -> 20% off = 1080  ($10.80)
 *
 * The customer's discount is passed in as a number so this class doesn't need
 * to know about customer types. Each Customer subclass should provide its own
 * getDiscountPercent(), and the controller passes that in.
 */
public class PricingService {
    private final HappyHour happyHour;

    public PricingService(HappyHour happyHour) {
        if (happyHour == null) throw new IllegalArgumentException("HappyHour cannot be null");
        this.happyHour = happyHour;
    }

    /**
     * @param item                    what the customer ordered
     * @param customerDiscountPercent 0 for Regular, 10 for VIP
     * @return the final bill in cents
     */
    public int calculateBill(MenuItem item, int customerDiscountPercent) {
        if (item == null) throw new IllegalArgumentException("Item cannot be null");
        if (customerDiscountPercent < 0 || customerDiscountPercent > 100) {
            throw new IllegalArgumentException("Discount must be between 0 and 100");
        }

        int bill = item.getPriceCents();
        bill = applyPercentOff(bill, customerDiscountPercent);
        if (happyHour.isActive()) {
            bill = applyPercentOff(bill, happyHour.getDiscountPercent());
        }
        return bill;
    }

    /** Takes percent% off, rounded to the nearest cent. */
    private static int applyPercentOff(int cents, int percent) {
        return Math.round(cents * (100 - percent) / 100.0f);
    }

    public HappyHour getHappyHour() {
        return happyHour;
    }
}
