package restaurantrush.model.staff;

import restaurantrush.model.Table;
import restaurantrush.model.WaitingQueue;
import restaurantrush.model.customer.Customer;

public class Host extends Staff {

    public Host(String name) {
        super(name);
    }

    @Override
    public String getRole() {
        return "Host";
    }

    public ActionResult seatCustomer(Customer customer, Table table, WaitingQueue queue) {
        return perform(() -> seat(customer, table, queue));
    }
}
