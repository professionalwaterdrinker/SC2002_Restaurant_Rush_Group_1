package restaurantrush.model.menu;

public class ComboMeal extends MenuItem {
    private static final int COMBO_PERCENT = 90;

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
        return Math.round(combined * COMBO_PERCENT / 100.0f);
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
