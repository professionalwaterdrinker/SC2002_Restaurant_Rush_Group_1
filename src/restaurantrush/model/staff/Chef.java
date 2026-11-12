package restaurantrush.model.staff;

import java.util.ArrayList;
import java.util.List;

import restaurantrush.model.Order;
import restaurantrush.model.Restaurant;
import restaurantrush.model.Table;
import restaurantrush.model.customer.Customer;

/** Advances one order by one preparation unit per turn. */
public class Chef extends Staff {

    public Chef(String name) {
        super(name);
    }

    @Override
    public String getRole() {
        return "Chef";
    }

    @Override
    public List<String> availableTasks(Restaurant restaurant) {
        List<String> tasks = new ArrayList<>();
        for (Table t : restaurant.getOccupiedTables()) {
            Order order = t.getCurrentCustomer().getOrder();
            if (order != null && order.isInPrep()) {
                tasks.add("cook " + order.getItem().getName() + " for table " + t.getTableNumber()
                        + " (" + order.getPrepUnitsDone() + "/" + order.getPrepUnitsRequired()
                        + " units done)");
            }
        }
        return tasks;
    }

    /** Cooks one unit of the order belonging to the customer at this table. */
    public ActionResult prepare(Table table) {
        return perform(() -> {
            if (table == null) {
                return ActionResult.failure("There is no table with that number.");
            }
            Customer customer = table.getCurrentCustomer();
            if (customer == null) {
                return ActionResult.failure("Table " + table.getTableNumber()
                        + " is empty, so there is no order to cook.");
            }
            Order order = customer.getOrder();
            if (order == null) {
                return ActionResult.failure(customer.label()
                        + " has not ordered yet, so there is nothing to cook.");
            }
            if (!order.isInPrep()) {
                return ActionResult.failure("Cannot cook order #" + order.getId()
                        + " (" + order.getItem().getName() + "): it is already " + order.getStatus() + ".");
            }
            order.cookOneUnit();
            if (order.isReady()) {
                return ActionResult.success("Chef finished " + order.getItem().getName() + " (order #"
                        + order.getId() + "). It is READY and can be served from next turn.");
            }
            return ActionResult.success("Chef cooked " + order.getItem().getName() + " (order #"
                    + order.getId() + "): " + order.getRemainingUnits() + " preparation unit(s) left.");
        });
    }
}
