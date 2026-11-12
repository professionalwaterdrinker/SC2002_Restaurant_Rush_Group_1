package restaurantrush.controller;

import java.util.ArrayList;
import java.util.List;

import restaurantrush.model.Restaurant;
import restaurantrush.model.Table;
import restaurantrush.model.WaitingQueue;
import restaurantrush.model.customer.Customer;
import restaurantrush.model.customer.CustomerFactory;
import restaurantrush.model.staff.ActionResult;
import restaurantrush.model.staff.Cashier;
import restaurantrush.model.staff.Chef;
import restaurantrush.model.staff.Staff;
import restaurantrush.model.staff.Waiter;
import restaurantrush.ui.ConsoleUI;

/**
 * Runs the turn sequence (handout Section 3.2). It only sequences the steps and
 * passes the manager's choices on: the rules themselves live in the staff,
 * customer, order and restaurant objects, and all input/output goes through ConsoleUI.
 */
public class GameController {
    private final Restaurant restaurant;
    private final ConsoleUI ui;
    private final CustomerFactory arrivals;
    private final Waiter waiter;
    private final Chef chef;
    private final Cashier cashier;
    private final GameResult result;
    private final List<Staff> staff = new ArrayList<>();
    private final int totalTurns = GameConfig.getTurns();

    public GameController(Restaurant restaurant, ConsoleUI ui, CustomerFactory arrivals,
            Waiter waiter, Chef chef, Cashier cashier, GameResult result) {
        this.restaurant = restaurant;
        this.ui = ui;
        this.arrivals = arrivals;
        this.waiter = waiter;
        this.chef = chef;
        this.cashier = cashier;
        this.result = result;
        // Display order; also the order in which they act each turn.
        staff.add(waiter);
        staff.add(chef);
        staff.add(cashier);
    }

    /** The game always runs every turn; there is no early ending. */
    public void startGame() {
        for (int turn = 1; turn <= totalTurns; turn++) {
            playTurn(turn);
        }
        // Step 6: result check after the last turn.
        ui.displayFinalResult(result.isVictory(), result.summaryLines());
    }

    private void playTurn(int turn) {
        for (Staff s : staff) {
            s.startTurn(turn);
        }

        // Step 1: arrivals
        Customer arrival = arrivals.arrivalFor(turn);
        restaurant.addArrival(arrival);
        ui.displayTurnStart(turn, totalTurns, restaurant, staff, arrival);

        // Steps 2 and 3: manager choices. Each choice is carried out at once, in the
        // order Waiter then Chef, so the Waiter always acts before the Chef.
        waiterChoice();
        chefChoice();

        // Steps 4 and 5, then the end-of-turn results (Section 3.5).
        List<String> events = eatingPaymentAndWaiting(turn);
        ui.displayTurnResults(turn, totalTurns, restaurant, events);
        ui.waitForContinue();
    }

    private void waiterChoice() {
        while (waiter.isAvailable()) {
            int choice = ui.askChoice(
                    "Waiter: 1 = seat a queued customer, 2 = take an order, 3 = serve a READY order, 4 = wait", 1, 4);
            if (choice == 4) {
                return;
            }
            if (choice == 1) {
                report(seatCustomer());
            } else if (choice == 2) {
                report(waiter.takeOrder(askTable("  Take the order at which table?"), restaurant));
            } else {
                report(waiter.serveOrder(askTable("  Serve which table?")));
            }
        }
    }

    private void chefChoice() {
        while (chef.isAvailable()) {
            int choice = ui.askChoice("Chef: 1 = cook one unit of an order, 2 = wait", 1, 2);
            if (choice == 2) {
                return;
            }
            report(chef.prepare(askTable("  Cook the order of which table?")));
        }
    }

    /** The manager picks which queued customer to seat and at which table. */
    private ActionResult seatCustomer() {
        WaitingQueue queue = restaurant.getWaitingQueue();
        Customer customer = null;
        Table table = null;
        if (!queue.isEmpty()) {
            customer = queue.get(ui.askChoice("  Seat which queued customer? (queue position)", 1, queue.size()));
            table = askTable("  Seat " + customer.label() + " at which table?");
        }
        return waiter.seatCustomer(customer, table, queue);
    }

    private Table askTable(String prompt) {
        return restaurant.getTable(ui.askChoice(prompt, 1, restaurant.getTables().size()));
    }

    /** A refused task explains why; the loop then lets the manager choose again. */
    private void report(ActionResult outcome) {
        if (outcome.isSuccess()) {
            ui.displayMessage(outcome.toString());
            ui.displayState(restaurant);
        } else {
            ui.displayMessage(outcome + " Choose again - this did not use up the task.");
        }
    }

    /** Step 4 (eating and payment) and step 5 (waiting and abandonment). */
    private List<String> eatingPaymentAndWaiting(int turn) {
        List<String> events = new ArrayList<>();
        for (Customer c : restaurant.finishEating(turn)) {
            events.add(c.label() + " finished eating and is READY_TO_PAY.");
        }
        ActionResult payment = cashier.processPayment(restaurant);
        if (payment.isSuccess()) {
            events.add(payment.getMessage());
        }
        for (Customer c : restaurant.processWaitingAndDepartures(turn)) {
            events.add(c.label() + " reached satisfaction 0 and left without paying."
                    + (c.getOrder() != null ? " Order #" + c.getOrder().getId() + " is CANCELLED." : ""));
        }
        return events;
    }
}
