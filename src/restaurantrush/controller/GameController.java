package restaurantrush.controller;

import java.util.ArrayList;
import java.util.List;

import restaurantrush.model.Restaurant;
import restaurantrush.model.HappyHour;
import restaurantrush.model.Table;
import restaurantrush.model.customer.CustomerFactory;
import restaurantrush.model.customer.Customer;
import restaurantrush.model.WaitingQueue;
import restaurantrush.model.staff.Cashier;
import restaurantrush.model.staff.Waiter;
import restaurantrush.model.staff.Chef;
import restaurantrush.model.staff.Host;
import restaurantrush.ui.ConsoleUI;

public class GameController {
    private Restaurant restaurant;
    private ConsoleUI console;
    private Waiter waiter;
    private Chef chef;
    private Cashier cashier;
    private Host host;
    private GameResult result;
    
    private static int turn = 1;

    public GameController(Restaurant restaurant, ConsoleUI console, Waiter waiter, Chef chef, Cashier cashier, Host host, GameResult result) {
        this.restaurant = restaurant;
        this.console = console;
        this.waiter = waiter;
        this.chef = chef;
        this.cashier = cashier;
        this.host = host;
        this.result = result;
    }

    //Main game loop
    public void startGame() {
        while (!checkEnd()) {
            waiter.startTurn(turn);
            chef.startTurn(turn);
            cashier.startTurn(turn);
            host.startTurn(turn);
            
            if (restaurant.getHappyHour().isActive() && restaurant.getHappyHour().getTurnsRemaining() < HappyHour.DURATION_TURNS) {
                restaurant.getRoster().recoverWaiting(HappyHour.SATISFACTION_RECOVERY);
            }

            customerArrival();
            System.out.println("Turn Start");
            console.displayTurnHeader(turn, restaurant);
            console.displayStaffTasks(availableTasks());
            managerChoice();
            customerAction();
            System.out.println("Turn End");
            console.displayTurnHeader(turn, restaurant);
            console.displayTurnsUsed(turn, GameConfig.getTurns());
            console.getIntInput("Enter 0 to continue to next turn...", 0, 0);
            restaurant.getHappyHour().endTurn();
            turn++;
        }
    }

    // For each turn, steps 1-6
    //step 1: customer arrival
    public void customerArrival() {
        Customer customer = CustomerFactory.baseSetting().arrivalFor(turn);
        restaurant.addArrival(customer);
    }

    //step 2: manager choice + step 3: kitchen and serving, waiter acts first then chef
    public void managerChoice() {
        if (restaurant.getHappyHour().canActivate()) {
            int happyHourChoice = console.getIntInput("Activate Happy Hour? (1:yes, 2:no): ", 1, 2);
            if (happyHourChoice == 1) {
                restaurant.getHappyHour().activate();
                restaurant.getRoster().recoverWaiting(HappyHour.SATISFACTION_RECOVERY);
                console.displayTurnHeader(turn, restaurant);
            }
        }

        while (!host.hasActedThisTurn()) {
            int hostChoice = console.getIntInput("Host: choose action (1:seat, 2:wait): ", 1, 2);
            WaitingQueue queue = restaurant.getWaitingQueue();
            
            if (hostChoice == 1) {
                System.out.println(host.seatCustomer(chooseQueuedCustomer(queue), restaurant.getFirstFreeTable(), queue).toString());
                console.displayTurnHeader(turn, restaurant);
            }
            else if (hostChoice == 2) {
                break;
            }
        }

        while (!waiter.hasActedThisTurn()) {
            int waiterChoice = console.getIntInput("Waiter: choose action (1:seat, 2:take order, 3:serve, 4:wait): ", 1, 4);
            WaitingQueue queue = restaurant.getWaitingQueue();

            if (waiterChoice == 1) {
                System.out.println(waiter.seatCustomer(chooseQueuedCustomer(queue), restaurant.getFirstFreeTable(), queue).toString());
                console.displayTurnHeader(turn, restaurant);
            }
            else if (waiterChoice == 2) {
                int tableNum = console.getIntInput("Waiter: choose table to take order (1, 2, ...): ", 1, GameConfig.getNumTables());
                Customer customer = restaurant.getTable(tableNum).getCurrentCustomer();

                System.out.println(waiter.takeOrder(customer, restaurant).toString());
                console.displayTurnHeader(turn, restaurant);
            }
            else if (waiterChoice == 3) {
                int tableNum = console.getIntInput("Waiter: choose table to serve order (1, 2, ...): ", 1, GameConfig.getNumTables());
                Customer customer = restaurant.getTable(tableNum).getCurrentCustomer();

                System.out.println(waiter.serveOrder(customer).toString());
                console.displayTurnHeader(turn, restaurant);
            }
            else if (waiterChoice == 4) {
                break;
            }
        }

        while (!chef.hasActedThisTurn()) {
            int chefChoice = console.getIntInput("Chef: choose action (1:cook, 2:wait): ", 1, 2);

            if (chefChoice == 1) {
                
                int tableNum = console.getIntInput("Chef: choose table to cook order (1, 2, ...): ", 1, GameConfig.getNumTables());
                try{
                    Customer customer = restaurant.getTable(tableNum).getCurrentCustomer();

                
                    System.out.println(chef.prepare(customer.getOrder()).toString());
                    console.displayTurnHeader(turn, restaurant);
                }
                catch (Exception e) {
                    System.out.println("FAILED: Cannot cook order #" + tableNum + " : it is EMPTY.");
                }
            }
            else if (chefChoice == 2) {
                break;
            }
        }
    }

    //manager picks which queued customer to seat (seating priority); null if the queue is empty
    private Customer chooseQueuedCustomer(WaitingQueue queue) {
        if (queue.isEmpty()) {
            return null;
        }
        int position = console.getIntInput("Choose customer to seat (queue position 1, 2, ...): ", 1, queue.size());
        return queue.get(position);
    }

    //staff tasks available at the start of the turn, worked out from the domain objects
    private List<String> availableTasks() {
        List<String> tasks = new ArrayList<>();
        boolean canSeat = !restaurant.getWaitingQueue().isEmpty() && restaurant.getFirstFreeTable() != null;
        if (canSeat) {
            tasks.add("Host: seat a queued customer");
            tasks.add("Waiter: seat a queued customer");
        }
        for (Table t : restaurant.getTables()) {
            Customer c = t.getCurrentCustomer();
            if (c == null) {
                continue;
            }
            if (c.canOrder()) {
                tasks.add("Waiter: take order at table " + t.getTableNumber());
            }
            if (c.canBeServed()) {
                tasks.add("Waiter: serve READY order at table " + t.getTableNumber());
            }
            if (c.getOrder() != null && c.getOrder().isInPrep()) {
                tasks.add("Chef: cook order at table " + t.getTableNumber());
            }
        }
        return tasks;
    }

    //step 4: eating and payment + step 5: waiting and abandonment
    public void customerAction() {
        restaurant.getRoster().finishEating(turn); //customers who are served will eat
        cashier.processPayment(restaurant);        //customers who have eaten last turn will pay
        restaurant.getRoster().endOfTurn(turn);    //customers who have exhausted satisfaction will be leaving
        restaurant.getWaitingQueue().removeDeparted(); //remove unhappy customers from queue

        //remove unhappy customers from tables
        for (Table t : restaurant.getTables()) {
            if (t.isOccupied() && t.getCurrentCustomer().hasLeftUnhappy()) {
                t.clearTable();
            }
        }
    }

    //step 6: Game end check + result check
    public Boolean checkEnd() {
        if (turn > GameConfig.getTurns()) {
            System.out.println(result.resultMessage());
            return true;
        }
        else {
            return false;
        }
    }
}
