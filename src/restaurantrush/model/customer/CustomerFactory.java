package restaurantrush.model.customer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerFactory {
    private final Map<Integer, Customer> arrivals = new HashMap<>();
    private final List<Customer> customers = new ArrayList<>();

    public void add(Customer customer) {
        arrivals.put(customer.getArrivalTurn(), customer);
        customers.add(customer);
    }

    public static CustomerFactory baseSetting() {
        CustomerFactory factory = new CustomerFactory();
        factory.add(new RegularCustomer(1, 1));
        factory.add(new VIPCustomer(2, 4));
        factory.add(new CriticCustomer(3, 7));
        factory.add(new RegularCustomer(4, 10));
        factory.add(new RegularCustomer(5, 13));
        factory.add(new RegularCustomer(6, 16));
        return factory;
    }

    public Customer arrivalFor(int turn) {
        return arrivals.get(turn);
    }

    public List<Customer> allCustomers() {
        return customers;
    }

    public int size() {
        return customers.size();
    }
}
