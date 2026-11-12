package restaurantrush.model.staff;

import java.util.List;

import restaurantrush.model.Restaurant;
import restaurantrush.model.Table;
import restaurantrush.model.WaitingQueue;
import restaurantrush.model.customer.Customer;

/**
 * Staff who can seat customers (Stage 3: the Waiter and the Host). The seating
 * rules are written once here, so both roles are guaranteed to follow the same ones.
 */
public abstract class SeatingStaff extends Staff {

    protected SeatingStaff(String name) {
        super(name);
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
    protected String seatingTask(Restaurant restaurant) {
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
}
