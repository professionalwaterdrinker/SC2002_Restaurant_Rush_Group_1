package restaurantrush.model;

import java.util.ArrayList;
import java.util.List;

import restaurantrush.model.customer.Customer;
import restaurantrush.model.customer.CustomerRoster;
import restaurantrush.model.menu.Menu;

/** Aggregate root of the domain: owns the tables, queue, customer record, menu and revenue. */
public class Restaurant {
    private final List<Table> tables;
    private final WaitingQueue waitingQueue;
    private final CustomerRoster roster;
    private final Menu menu;
    private final HappyHour happyHour;
    private final PricingService pricingService;
    private int totalRevenueCents;

    public Restaurant(int numTables, Menu menu) {
        this.tables = new ArrayList<>();
        for (int i = 1; i <= numTables; i++) {
            tables.add(new Table(i));
        }
        this.waitingQueue = new WaitingQueue();
        this.roster = new CustomerRoster();
        this.menu = menu;
        this.happyHour = new HappyHour();
        this.pricingService = new PricingService(happyHour);
        this.totalRevenueCents = 0;
    }

    public List<Table> getTables() {
        return tables;
    }

    public Table getTable(int tableNumber) {
        for (Table t : tables) {
            if (t.getTableNumber() == tableNumber) {
                return t;
            }
        }
        return null;
    }

    public List<Table> getFreeTables() {
        List<Table> free = new ArrayList<>();
        for (Table t : tables) {
            if (!t.isOccupied()) {
                free.add(t);
            }
        }
        return free;
    }

    public List<Table> getOccupiedTables() {
        List<Table> occupied = new ArrayList<>();
        for (Table t : tables) {
            if (t.isOccupied()) {
                occupied.add(t);
            }
        }
        return occupied;
    }

    public WaitingQueue getWaitingQueue() {
        return waitingQueue;
    }

    public CustomerRoster getRoster() {
        return roster;
    }

    public Menu getMenu() {
        return menu;
    }

    public HappyHour getHappyHour() {
        return happyHour;
    }

    public PricingService getPricingService() {
        return pricingService;
    }

    public void addArrival(Customer customer) {
        if (customer != null) {
            roster.add(customer);
            waitingQueue.join(customer);
        }
    }

    public void addRevenue(int cents) {
        if (cents > 0) {
            this.totalRevenueCents += cents;
        }
    }

    public int getTotalRevenueCents() {
        return totalRevenueCents;
    }

    /** Step 4a: customers served on an earlier turn finish eating and become READY_TO_PAY. */
    public List<Customer> finishEating(int turn) {
        return roster.finishEating(turn);
    }

    /**
     * Step 5: every customer still waiting for a table or for food loses
     * satisfaction. Anyone who reaches 0 leaves: their order is cancelled by the
     * customer, and the queue place or table is released here.
     *
     * @return the customers who left this turn
     */
    public List<Customer> processWaitingAndDepartures(int turn) {
        List<Customer> departed = roster.endOfTurn(turn);
        waitingQueue.removeDeparted();
        for (Customer c : departed) {
            Table table = findTableWithCustomer(c);
            if (table != null) {
                table.clearTable();
            }
        }
        return departed;
    }

    /**
     * Happy Hour, start of a turn: while it is active, customers still waiting
     * for a table or for food recover satisfaction (capped at 100).
     */
    public void applyHappyHourRecovery() {
        if (happyHour.isActive()) {
            roster.recoverWaiting(HappyHour.SATISFACTION_RECOVERY);
        }
    }

    /**
     * Manager switches Happy Hour on at the start of a turn (once per game).
     * @return false if it has already been used
     */
    public boolean activateHappyHour() {
        if (!happyHour.activate()) {
            return false;
        }
        applyHappyHourRecovery();
        return true;
    }

    public Table findTableWithCustomer(Customer customer) {
        for (Table t : tables) {
            if (t.getCurrentCustomer() == customer) {
                return t;
            }
        }
        return null;
    }
}