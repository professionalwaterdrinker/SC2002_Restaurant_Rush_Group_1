package restaurantrush.model.customer;

import java.util.ArrayList;
import java.util.List;

public class CustomerRoster {
    private final List<Customer> customers = new ArrayList<>();

    public void add(Customer customer) {
        customers.add(customer);
    }

    public Customer findById(int id) {
        for (Customer c : customers) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    public List<Customer> finishEating(int turn) {
        List<Customer> done = new ArrayList<>();
        for (Customer c : customers) {
            if (c.finishEating(turn)) {
                done.add(c);
            }
        }
        return done;
    }

    public List<Customer> readyToPay(int turn) {
        List<Customer> ready = new ArrayList<>();
        for (Customer c : customers) {
            if (c.canPay(turn)) {
                ready.add(c);
            }
        }
        ready.sort((a, b) -> a.getReadyToPayTurn() - b.getReadyToPayTurn());
        return ready;
    }

    public List<Customer> endOfTurn(int turn) {
        List<Customer> left = new ArrayList<>();
        for (Customer c : customers) {
            if (c.endOfTurn(turn)) {
                left.add(c);
            }
        }
        return left;
    }

    public void recoverWaiting(int amount) {
        for (Customer c : customers) {
            c.recoverSatisfaction(amount);
        }
    }

    public int paidCount() {
        int count = 0;
        for (Customer c : customers) {
            if (c.hasPaid()) {
                count++;
            }
        }
        return count;
    }

    public int servedCount() {
        int count = 0;
        for (Customer c : customers) {
            if (c.hasBeenServed()) {
                count++;
            }
        }
        return count;
    }

    public int unhappyDepartures() {
        int count = 0;
        for (Customer c : customers) {
            if (c.hasLeftUnhappy()) {
                count++;
            }
        }
        return count;
    }

    public double averagePaidSatisfaction() {
        int total = 0;
        int count = 0;
        for (Customer c : customers) {
            if (c.hasPaid()) {
                total += c.getRecordedSatisfaction();
                count++;
            }
        }
        if (count == 0) {
            return 0;
        }
        return (double) total / count;
    }

    public String averagePaidSatisfactionText() {
        if (paidCount() == 0) {
            return "N/A";
        }
        return String.format("%.1f", averagePaidSatisfaction());
    }

    public List<Customer> present() {
        List<Customer> here = new ArrayList<>();
        for (Customer c : customers) {
            if (!c.getStatus().isFinished()) {
                here.add(c);
            }
        }
        return here;
    }

    public List<Customer> all() {
        return customers;
    }
}
