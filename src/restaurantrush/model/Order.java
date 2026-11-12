package restaurantrush.model;

import restaurantrush.model.menu.MenuItem;

/**
 * One customer's food order. Tracks prep progress and status, and holds the
 * bill, which is LOCKED IN when the order is taken. That way, if Happy Hour
 * starts or ends later, this customer's price doesn't change.
 *
 * The bill is calculated by PricingService and passed in; Order never does
 * pricing maths itself (single responsibility).
 */
public class Order {
    private static int nextId = 1;

    private final int id;
    private final int customerId;
    private final MenuItem item;
    private final int billCents;
    private int prepUnitsDone;
    private OrderStatus status;

    public Order(int customerId, MenuItem item, int billCents) {
        if (item == null) throw new IllegalArgumentException("Order needs a menu item");
        if (billCents < 0) throw new IllegalArgumentException("Bill cannot be negative");
        this.id = nextId++;
        this.customerId = customerId;
        this.item = item;
        this.billCents = billCents;
        this.prepUnitsDone = 0;
        this.status = OrderStatus.IN_PREP;
    }

    /**
     * Chef cooks one prep unit. Becomes READY when the last unit is done.
     * @return true if a unit was cooked, false if the order isn't in prep.
     */
    public boolean cookOneUnit() {
        if (status != OrderStatus.IN_PREP) return false;
        prepUnitsDone++;
        if (prepUnitsDone >= item.getPrepUnits()) {
            status = OrderStatus.READY;
        }
        return true;
    }

    /** Waiter serves the dish. Only allowed when READY. */
    public boolean markServed() {
        if (status != OrderStatus.READY) return false;
        status = OrderStatus.SERVED;
        return true;
    }

    /** Cashier charges the bill. Only allowed after it was served. */
    public boolean markPaid() {
        if (status != OrderStatus.SERVED) return false;
        status = OrderStatus.PAID;
        return true;
    }

    /** Customer left. A paid order can't be cancelled. */
    public boolean cancel() {
        if (status == OrderStatus.PAID || status == OrderStatus.CANCELLED) return false;
        status = OrderStatus.CANCELLED;
        return true;
    }

    public int getId() { return id; }
    public int getCustomerId() { return customerId; }
    public MenuItem getItem() { return item; }
    public int getBillCents() { return billCents; }
    public int getPrepUnitsDone() { return prepUnitsDone; }
    public int getPrepUnitsRequired() { return item.getPrepUnits(); }
    public int getRemainingUnits() { return item.getPrepUnits() - prepUnitsDone; }
    public OrderStatus getStatus() { return status; }
    public boolean isInPrep() { return status == OrderStatus.IN_PREP; }
    public boolean isReady() { return status == OrderStatus.READY; }

    /** Only for tests / starting a fresh game in the same run. */
    public static void resetIdCounter() { nextId = 1; }

    @Override
    public String toString() {
        return String.format("Order #%d [customer %d] %s - %s (%d/%d units)",
                id, customerId, item.getName(), status, prepUnitsDone, item.getPrepUnits());
    }
}
