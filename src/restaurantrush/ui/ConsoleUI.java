package restaurantrush.ui;

import java.util.List;
import java.util.Scanner;

import restaurantrush.model.HappyHour;
import restaurantrush.model.Restaurant;
import restaurantrush.model.Table;
import restaurantrush.model.customer.Customer;
import restaurantrush.model.staff.Staff;
import restaurantrush.util.MoneyUtils;

/**
 * Boundary class: shows the state and collects the manager's decisions.
 * It contains no game rules; it only reads from the domain objects.
 */
public class ConsoleUI {
    private static final String LINE = "=".repeat(78);
    private static final String THIN = "-".repeat(78);

    private final Scanner scanner;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
    }

    /** Start-of-turn display (handout Section 3.1). */
    public void displayTurnStart(int turn, int totalTurns, Restaurant restaurant,
            List<Staff> staff, Customer arrival) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("  TURN " + turn + " of " + totalTurns);
        System.out.println(LINE);
        System.out.println("Revenue: " + MoneyUtils.formatCents(restaurant.getTotalRevenueCents())
                + " | Paid customers: " + restaurant.getRoster().paidCount()
                + " | Average satisfaction (paid): " + restaurant.getRoster().averagePaidSatisfactionText());
        System.out.println(happyHourLine(restaurant.getHappyHour()));
        if (arrival != null) {
            System.out.println("Arrival: " + arrival.label() + " joins the waiting queue with satisfaction "
                    + arrival.getSatisfaction() + ".");
        }
        displayState(restaurant);
        System.out.println("Staff tasks available:");
        for (Staff s : staff) {
            List<String> tasks = s.availableTasks(restaurant);
            System.out.println("  " + s.getRole() + ": " + (tasks.isEmpty() ? "nothing to do" : String.join("; ", tasks)));
        }
        System.out.println(THIN);
    }

    /** The waiting queue and the tables, with each customer's satisfaction and order status. */
    public void displayState(Restaurant restaurant) {
        System.out.println(THIN);
        List<Customer> waiting = restaurant.getWaitingQueue().asList();
        if (waiting.isEmpty()) {
            System.out.println("Waiting queue: [empty]");
        } else {
            System.out.println("Waiting queue:");
            for (int i = 0; i < waiting.size(); i++) {
                Customer c = waiting.get(i);
                System.out.println("  " + (i + 1) + ". " + c.label() + " | satisfaction " + c.getSatisfaction());
            }
        }
        System.out.println("Tables:");
        for (Table t : restaurant.getTables()) {
            if (!t.isOccupied()) {
                System.out.println("  Table " + t.getTableNumber() + ": [empty]");
                continue;
            }
            Customer c = t.getCurrentCustomer();
            System.out.println("  Table " + t.getTableNumber() + ": " + c.label()
                    + " | satisfaction " + c.getSatisfaction()
                    + " | " + c.getStatus().getDescription()
                    + " | " + (c.getOrder() == null ? "no order yet" : c.getOrder().toString()));
        }
        System.out.println(THIN);
    }

    /** End-of-turn results (handout Section 3.5), shown after payment and abandonment. */
    public void displayTurnResults(int turn, int totalTurns, Restaurant restaurant, List<String> events) {
        System.out.println();
        System.out.println("--- End of turn " + turn + " ---");
        for (String event : events) {
            System.out.println("  * " + event);
        }
        System.out.println("Served: " + restaurant.getRoster().servedCount()
                + " | Unhappy departures: " + restaurant.getRoster().unhappyDepartures()
                + " | Revenue: " + MoneyUtils.formatCents(restaurant.getTotalRevenueCents())
                + " | Average satisfaction (paid): " + restaurant.getRoster().averagePaidSatisfactionText()
                + " | Turns used: " + turn + " of " + totalTurns);
    }

    public void displayFinalResult(boolean victory, List<String> summaryLines) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("  FINAL RESULT");
        System.out.println(LINE);
        for (String line : summaryLines) {
            System.out.println(line);
        }
        System.out.println(THIN);
        System.out.println(victory
                ? "VICTORY! All three targets were met."
                : "DEFEAT. At least one target was not met.");
    }

    public void displayMessage(String msg) {
        System.out.println(msg);
    }

    public void waitForContinue() {
        askChoice("Enter 0 to continue", 0, 0);
    }

    /** Keeps asking until the manager enters a whole number between min and max. */
    public int askChoice(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt + (min == max ? "" : " [" + min + "-" + max + "]") + ": ");
            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                if (choice >= min && choice <= max) {
                    return choice;
                }
                System.out.println("Invalid choice: " + choice + " is not "
                        + (min == max ? min : "between " + min + " and " + max) + ". Try again.");
            } else {
                // scanner.next() throws NoSuchElementException when input has ended; Main handles it.
                String bad = scanner.next();
                System.out.println("Invalid choice: \"" + bad + "\" is not a whole number. Try again.");
            }
        }
    }

    private static String happyHourLine(HappyHour happyHour) {
        if (happyHour.isActive()) {
            return "Happy Hour: ACTIVE, " + happyHour.getTurnsRemaining()
                    + " turn(s) remaining including this one (orders taken now get "
                    + happyHour.getDiscountPercent() + "% off)";
        }
        return happyHour.canActivate() ? "Happy Hour: available (not used yet)" : "Happy Hour: already used";
    }
}
