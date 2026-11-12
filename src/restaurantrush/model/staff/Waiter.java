package restaurantrush.model.staff;

import java.util.ArrayList;
import java.util.List;

import restaurantrush.model.Order;
import restaurantrush.model.Restaurant;
import restaurantrush.model.Table;
import restaurantrush.model.WaitingQueue;
import restaurantrush.model.customer.Customer;
import restaurantrush.util.MoneyUtils;

/** Seats a customer, takes an order or serves a READY order: one task per turn. */
public class Waiter extends Staff {

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

    /** Seats one queued customer at a free table. */
    public ActionResult seatCustomer(Customer customer, Table table, WaitingQueue queue) {
        return perform(() -> {
            if (queue.isEmpty()) {
                return ActionResult.failure("The waiting queue is empty, so there is nobody to seat.");
            }
            if (customer == null) {
                return ActionResult.failure("There is no customer at that queue position.");
            }
            if (table == null) {
                return ActionResult.failure("There is no table with that number.");
            }
            if (!queue.contains(customer)) {
                return ActionResult.failure(customer.label() + " is not in the waiting queue.");
            }
            String why = customer.whyNotSeat();
            if (why != null) {
                return ActionResult.failure(why);
            }
            if (table.isOccupied()) {
                return ActionResult.failure("Table " + table.getTableNumber() + " is occupied by "
                        + table.getCurrentCustomer().label() + ".");
            }
            queue.remove(customer);
            table.seatCustomer(customer);
            customer.seat(getCurrentTurn());
            return ActionResult.success(getRole() + " seated " + customer.label() + " at table "
                    + table.getTableNumber() + ".");
        });
    }

    /** Describes the seating task if it is possible right now, otherwise null. */
    private String seatingTask(Restaurant restaurant) {
        int waiting = restaurant.getWaitingQueue().size();
        List<Table> free = restaurant.getFreeTables();
        if (waiting == 0 || free.isEmpty()) {
            return null;
        }
        StringBuilder tables = new StringBuilder();
        for (Table t : free) {
            tables.append(tables.length() == 0 ? "" : ", ").append(t.getTableNumber());
        }
        return "seat a queued customer (" + waiting + " waiting; free table(s): " + tables + ")";
    }

    /** The customer chooses the food. */
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
            Order order = customer.placeOrder(restaurant.getMenu());
            return ActionResult.success(customer.label() + " at table " + table.getTableNumber()
                    + " chose " + order.getItem().getName() + " ("
                    + order.getPrepUnitsRequired() + " preparation unit(s)). Bill: "
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
