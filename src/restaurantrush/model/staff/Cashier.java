package restaurantrush.model.staff;

import java.util.ArrayList;
import java.util.List;

import restaurantrush.model.Restaurant;
import restaurantrush.model.Table;
import restaurantrush.model.customer.Customer;
import restaurantrush.util.MoneyUtils;

/** Automatically charges at most one READY_TO_PAY customer per turn, oldest first. */
public class Cashier extends Staff {

    public Cashier(String name) {
        super(name);
    }

    @Override
    public String getRole() {
        return "Cashier";
    }

    @Override
    public List<String> availableTasks(Restaurant restaurant) {
        List<String> tasks = new ArrayList<>();
        List<Customer> ready = restaurant.getRoster().readyToPay(getCurrentTurn());
        if (!ready.isEmpty()) {
            Customer next = ready.get(0);
            tasks.add("will collect " + MoneyUtils.formatCents(next.getOrder().getBillCents())
                    + " from " + next.label() + " this turn (automatic)");
        }
        return tasks;
    }

    /** @return failure (not an error) when nobody can be charged this turn */
    public ActionResult processPayment(Restaurant restaurant) {
        return perform(() -> {
            List<Customer> ready = restaurant.getRoster().readyToPay(getCurrentTurn());
            if (ready.isEmpty()) {
                return ActionResult.failure("No customer is ready to pay.");
            }
            Customer next = ready.get(0);
            Table table = restaurant.findTableWithCustomer(next);
            int bill = next.pay(getCurrentTurn()); // order -> PAID, satisfaction recorded
            restaurant.addRevenue(bill);
            String freed = "";
            if (table != null) {
                table.clearTable();
                freed = " Table " + table.getTableNumber() + " is free.";
            }
            return ActionResult.success("Cashier collected " + MoneyUtils.formatCents(bill) + " from "
                    + next.label() + " (satisfaction " + next.getRecordedSatisfaction()
                    + " recorded). PAID." + freed);
        });
    }
}
