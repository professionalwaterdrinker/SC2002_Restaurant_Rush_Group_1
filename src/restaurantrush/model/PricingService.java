package restaurantrush.model;

import restaurantrush.model.menu.MenuItem;
import restaurantrush.util.MoneyUtils;

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
 * to know about customer types. Each Customer subclass provides its own
 * getDiscountPercent(), and Customer.placeOrder() passes that in.
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
        int bill = MoneyUtils.percentOff(item.getPriceCents(), customerDiscountPercent);
        if (happyHour.isActive()) {
            bill = MoneyUtils.percentOff(bill, happyHour.getDiscountPercent());
        }
        return bill;
    }

    public HappyHour getHappyHour() {
        return happyHour;
    }
}
