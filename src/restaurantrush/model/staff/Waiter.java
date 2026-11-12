package restaurantrush.model.staff;

import java.util.ArrayList;
import java.util.List;

import restaurantrush.model.Order;
import restaurantrush.model.Restaurant;
import restaurantrush.model.Table;
import restaurantrush.model.customer.Customer;
import restaurantrush.util.MoneyUtils;

/** Seats a customer, takes an order or serves a READY order: one task per turn. */
public class Waiter extends SeatingStaff {

    public Waiter(String name) {
        super(name);
    }

    @Override
    public String getRole() {
        return "Waiter";
    }

    @Override
    public List<String> availableTasks(Restaurant restaurant) {
        List<String> tasks = new ArrayList<>();
        String seating = seatingTask(restaurant);
        if (seating != null) {
            tasks.add(seating);
        }
        for (Table t : restaurant.getOccupiedTables()) {
            Customer c = t.getCurrentCustomer();
            if (c.canOrder()) {
                tasks.add("take the order at table " + t.getTableNumber() + " (" + c.label() + ")");
            } else if (c.canBeServed()) {
                tasks.add("serve " + c.getOrder().getItem().getName() + " at table "
                        + t.getTableNumber() + " (" + c.label() + ")");
            }
        }
        return tasks;
    }

    /** The customer chooses the food; the price is locked in at this moment. */
    public ActionResult takeOrder(Table table, Restaurant restaurant) {
        return perform(() -> {
            if (table == null) {
                return ActionResult.failure("There is no table with that number.");
            }
            Customer customer = table.getCurrentCustomer();
            if (customer == null) {
                return ActionResult.failure("Table " + table.getTableNumber()
                        + " is empty, so there is no order to take.");
            }
            String why = customer.whyNotOrder();
            if (why != null) {
                return ActionResult.failure(why);
            }
            Order order = customer.placeOrder(restaurant.getMenu(), restaurant.getPricingService());
            return ActionResult.success(customer.label() + " at table " + table.getTableNumber()
                    + " chose " + order.getItem().getName() + " ("
                    + order.getPrepUnitsRequired() + " preparation unit(s)). Bill locked at "
                    + MoneyUtils.formatCents(order.getBillCents()) + ".");
        });
    }

    /** Only an order that is already READY can be served. */
    public ActionResult serveOrder(Table table) {
        return perform(() -> {
            if (table == null) {
                return ActionResult.failure("There is no table with that number.");
            }
            Customer customer = table.getCurrentCustomer();
            if (customer == null) {
                return ActionResult.failure("Table " + table.getTableNumber()
                        + " is empty, so there is nobody to serve.");
            }
            String why = customer.whyNotServe();
            if (why != null) {
                return ActionResult.failure(why);
            }
            customer.serve(getCurrentTurn());
            return ActionResult.success("Waiter served " + customer.getOrder().getItem().getName()
                    + " to " + customer.label() + " at table " + table.getTableNumber()
                    + ". Order is SERVED.");
        });
    }
}
