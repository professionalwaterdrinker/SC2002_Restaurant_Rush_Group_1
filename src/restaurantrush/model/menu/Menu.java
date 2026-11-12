package restaurantrush.model.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The list of everything the restaurant sells. */
public class Menu {
    private final List<MenuItem> items = new ArrayList<>();

    /** Salad, Burger, Pasta. */
    public static Menu standardMenu() {
        Menu menu = new Menu();
        menu.addItem(new Dish("Salad", 900, 1));
        menu.addItem(new Dish("Burger", 1500, 1));
        menu.addItem(new Dish("Pasta", 1800, 2));
        return menu;
    }

    public void addItem(MenuItem item) {
        if (item == null) throw new IllegalArgumentException("Item cannot be null");
        if (findByName(item.getName()) != null) {
            throw new IllegalArgumentException("Duplicate menu item: " + item.getName());
        }
        items.add(item);
    }

    /** Read-only view so nobody outside can change the menu by accident. */
    public List<MenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /** Case-insensitive lookup. Returns null if not found. */
    public MenuItem findByName(String name) {
        for (MenuItem item : items) {
            if (item.getName().equalsIgnoreCase(name)) return item;
        }
        return null;
    }

    /** 1-based lookup, matching the numbers ConsoleUI shows the player. Returns null if out of range. */
    public MenuItem getByNumber(int number) {
        if (number < 1 || number > items.size()) return null;
        return items.get(number - 1);
    }

    public int size() {
        return items.size();
    }
}
