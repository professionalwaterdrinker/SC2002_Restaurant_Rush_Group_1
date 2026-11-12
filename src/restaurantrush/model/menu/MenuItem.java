package restaurantrush.model.menu;

/**
 * Anything a customer can order. Every item has a name, a price and a
 * number of prep units (how many Chef actions it takes to cook).
 *
 * Prices are stored in CENTS (int) to avoid floating-point rounding errors.
 * Use MoneyUtils to format them for display.
 */
public abstract class MenuItem {
    private final String name;

    protected MenuItem(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Menu item name cannot be empty");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /** Base price in cents, before any discount or Happy Hour. */
    public abstract int getPriceCents();

    /** Number of Chef actions needed to finish this item. */
    public abstract int getPrepUnits();

    @Override
    public String toString() {
        return String.format("%s ($%d.%02d, %d unit%s)", name,
                getPriceCents() / 100, getPriceCents() % 100,
                getPrepUnits(), getPrepUnits() == 1 ? "" : "s");
    }
}
