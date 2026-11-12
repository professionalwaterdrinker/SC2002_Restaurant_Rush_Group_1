package restaurantrush.model.staff;

import restaurantrush.model.Order;
import restaurantrush.model.Restaurant;
import restaurantrush.model.Table;
import restaurantrush.model.WaitingQueue;
import restaurantrush.model.customer.Customer;

public class Waiter extends Staff {

    public Waiter(String name) {
        super(name);
    }

    @Override
    public String getRole() {
        return "Waiter";
    }

    public ActionResult seatCustomer(Customer customer, Table table, WaitingQueue queue) {
        return perform(() -> seat(customer, table, queue));
    }

    public ActionResult takeOrder(Customer customer, Restaurant restaurant) {
        return perform(() -> {
            if (customer == null) {
                return ActionResult.failure("No customer selected.");
            }
            String why = customer.whyNotOrder();
            if (why != null) {
                return ActionResult.failure(why);
            }
            Order order = customer.placeOrder(restaurant.getMenu(), getCurrentTurn());
            return ActionResult.success(customer.label() + " ordered "
                    + order.getItem().getName() + ".");
        });
    }

    public ActionResult serveOrder(Customer customer) {
        return perform(() -> {
            if (customer == null) {
                return ActionResult.failure("No customer selected.");
            }
            String why = customer.whyNotServe();
            if (why != null) {
                return ActionResult.failure(why);
            }
            customer.serve(getCurrentTurn());
            return ActionResult.success("Served " + customer.getOrder().getItem().getName()
                    + " to " + customer.label() + ".");
        });
    }
}
