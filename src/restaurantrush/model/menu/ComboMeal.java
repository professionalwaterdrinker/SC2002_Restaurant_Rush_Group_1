package restaurantrush.model.menu;

import restaurantrush.util.MoneyUtils;

/**
 * Stage 3: two existing menu items sold together. Preparation units are the sum
 * of the two parts; the price is 90% of their combined price, to the nearest cent.
 */
public class ComboMeal extends MenuItem {
    private static final int COMBO_DISCOUNT_PERCENT = 10;

    private final MenuItem first;
    private final MenuItem second;

    public ComboMeal(String name, MenuItem first, MenuItem second) {
        super(name);
        if (first == null || second == null) {
            throw new IllegalArgumentException("A combo needs two items");
        }
        this.first = first;
        this.second = second;
    }

    @Override
    public int getPriceCents() {
        int combined = first.getPriceCents() + second.getPriceCents();
        return MoneyUtils.percentOff(combined, COMBO_DISCOUNT_PERCENT);
    }

    @Override
    public int getPrepUnits() {
        return first.getPrepUnits() + second.getPrepUnits();
    }

    public MenuItem getFirst() {
        return first;
    }

    public MenuItem getSecond() {
        return second;
    }
}
