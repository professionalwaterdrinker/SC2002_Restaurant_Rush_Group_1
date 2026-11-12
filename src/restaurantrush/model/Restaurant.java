package restaurantrush.model;

import java.util.ArrayList;
import java.util.List;

import restaurantrush.model.customer.Customer;
import restaurantrush.model.customer.CustomerRoster;
import restaurantrush.model.menu.Menu;

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

    public Table getFirstFreeTable() {
        for (Table t : tables) {
            if (!t.isOccupied()) {
                return t;
            }
        }
        return null;
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

    public Table findTableWithCustomer(Customer customer) {
        for (Table t : tables) {
            if (t.getCurrentCustomer() == customer) {
                return t;
            }
        }
        return null;
    }
}