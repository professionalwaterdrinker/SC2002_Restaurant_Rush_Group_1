package restaurantrush.ui;

import java.util.List;
import java.util.Scanner;

import restaurantrush.model.Restaurant;
import restaurantrush.model.Table;
import restaurantrush.model.customer.Customer;
import restaurantrush.util.MoneyUtils;

public class ConsoleUI {
    private final Scanner scanner;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
    }

    public void displayTurnHeader(int turn, Restaurant restaurant) {
        System.out.println("\n=================================================================================================");
        System.out.println("                                        TURN " + turn + " / 18");
        System.out.println("=================================================================================================");

        String rev = MoneyUtils.formatCents(restaurant.getTotalRevenueCents());
        String avgSat = restaurant.getRoster().averagePaidSatisfactionText();
        int paidCount = restaurant.getRoster().paidCount();
        int unhappyDeparts = restaurant.getRoster().unhappyDepartures();
        int servedCount = restaurant.getRoster().servedCount();

        System.out.println("Revenue: " + rev + " | Paid Customers: " + paidCount + " | Avg Paid Sat: " + 
                            avgSat + " | Unhappy Departs: " + unhappyDeparts + " | Served Customers: " + servedCount);
        System.out.println("-------------------------------------------------------------------------------------------------");

        System.out.print("Waiting Queue: ");
        List<Customer> waiting = restaurant.getWaitingQueue().asList();
        if (waiting.isEmpty()) {
            System.out.println("[Empty]");
        } else {
            for (int i = 0; i < waiting.size(); i++) {
                Customer c = waiting.get(i);
                System.out.print((i + 1) + ". " + c.label() + " (Sat: " + c.getSatisfaction() + ")  ");
            }
            System.out.println();
        }

        System.out.println("Tables:");
        for (Table t : restaurant.getTables()) {
            if (!t.isOccupied()) {
                System.out.println("  Table " + t.getTableNumber() + ": [EMPTY]");
            } else {
                Customer c = t.getCurrentCustomer();
                System.out.println("  Table " + t.getTableNumber() + ": " + c.label()
                        + " | Status: " + c.getStatus().getDescription()
                        + " | Sat: " + c.getSatisfaction());
                
                try{ 
                    System.out.println("  " + c.getOrder().toString());
                } catch (Exception e) {}
            }

            System.out.println();
        }
        System.out.println("-------------------------------------------------------------------------------------------------");
    }

    public void displayStaffTasks(List<String> tasks) {
        System.out.println("Staff tasks available:");
        if (tasks.isEmpty()) {
            System.out.println("  [none - staff can only wait this turn]");
        }
        for (String task : tasks) {
            System.out.println("  " + task);
        }
        System.out.println("-------------------------------------------------------------------------------------------------");
    }

    public void displayTurnsUsed(int turn, int totalTurns) {
        System.out.println("Turns used: " + turn + " / " + totalTurns);
    }

    public void displayMessage(String msg) {
        System.out.println(msg);
    }

    public void displayError(String errorMsg) {
        System.out.println("[ERROR] " + errorMsg);
    }

    public int getIntInput(String prompt, int min, int max) {
        int choice;
        while (true) {
            System.out.print(prompt + " (" + min + "-" + max + "): ");
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                if (choice >= min && choice <= max) {
                    return choice;
                }
            } else {
                scanner.next();
            }
            System.out.println("Invalid choice. Please enter a number between " + min + " and " + max + ".");
        }
    }
}
