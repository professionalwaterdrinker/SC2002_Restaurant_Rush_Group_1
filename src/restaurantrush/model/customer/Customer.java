package restaurantrush.model.customer;

import java.util.List;

import restaurantrush.model.CustomerStatus;
import restaurantrush.model.Order;
import restaurantrush.model.PricingService;
import restaurantrush.model.menu.Menu;
import restaurantrush.model.menu.MenuItem;

public abstract class Customer {
    private final int id;
    private final int arrivalTurn;
    private int satisfaction = 100;
    private CustomerStatus status = CustomerStatus.WAITING;
    private Order order;
    private int servedTurn = -1;
    private int readyToPayTurn = -1;
    private int recordedSatisfaction = -1;

    public Customer(int id, int arrivalTurn) {
        this.id = id;
        this.arrivalTurn = arrivalTurn;
    }

    public abstract String getTypeName();

    public abstract int getSatisfactionLossPerTurn();

    public int getDiscountPercent() {
        return 0;
    }

    public MenuItem chooseFood(Menu menu) {
        List<MenuItem> items = menu.getItems();
        return items.get((id - 1) % items.size());
    }

    public String whyNotSeat() {
        if (status != CustomerStatus.WAITING) {
            return label() + " cannot be seated: " + status.getDescription();
        }
        return null;
    }

    public String whyNotOrder() {
        if (status != CustomerStatus.SEATED) {
            return label() + " cannot order: " + status.getDescription();
        }
        return null;
    }

    public String whyNotServe() {
        if (status != CustomerStatus.ORDERED) {
            return label() + " cannot be served: " + status.getDescription();
        }
        if (!order.isReady()) {
            return label() + "'s " + order.getItem().getName() + " is not ready yet ("
                    + order.getPrepUnitsDone() + " of " + order.getPrepUnitsRequired() + " units cooked)";
        }
        return null;
    }

    public String whyNotPay(int turn) {
        if (status != CustomerStatus.READY_TO_PAY) {
            return label() + " cannot pay: " + status.getDescription();
        }
        if (readyToPayTurn >= turn) {
            return label() + " only became ready to pay this turn";
        }
        return null;
    }

    public boolean canBeSeated() {
        return whyNotSeat() == null;
    }

    public boolean canOrder() {
        return whyNotOrder() == null;
    }

    public boolean canBeServed() {
        return whyNotServe() == null;
    }

    public boolean canPay(int turn) {
        return whyNotPay(turn) == null;
    }

    public void seat(int turn) {
        check(whyNotSeat());
        status = CustomerStatus.SEATED;
    }

    public Order placeOrder(Menu menu, PricingService pricing, int turn) {
        check(whyNotOrder());
        MenuItem item = chooseFood(menu);
        int bill = pricing.calculateBill(item, getDiscountPercent());
        order = new Order(id, item, bill);
        status = CustomerStatus.ORDERED;
        return order;
    }

    public void serve(int turn) {
        check(whyNotServe());
        order.markServed();
        status = CustomerStatus.EATING;
        servedTurn = turn;
    }

    public boolean finishEating(int turn) {
        if (status == CustomerStatus.EATING && servedTurn < turn) {
            status = CustomerStatus.READY_TO_PAY;
            readyToPayTurn = turn;
            return true;
        }
        return false;
    }

    public int pay(int turn) {
        check(whyNotPay(turn));
        order.markPaid();
        status = CustomerStatus.PAID;
        recordedSatisfaction = satisfaction;
        return order.getBillCents();
    }

    public boolean endOfTurn(int turn) {
        if (!status.isWaitingForService()) {
            return false;
        }
        satisfaction = Math.max(0, satisfaction - getSatisfactionLossPerTurn());
        if (satisfaction > 0) {
            return false;
        }
        status = CustomerStatus.LEFT;
        if (order != null) {
            order.cancel();
        }
        return true;
    }

    public void recoverSatisfaction(int amount) {
        if (status.isWaitingForService()) {
            satisfaction = Math.min(100, satisfaction + amount);
        }
    }

    private void check(String reason) {
        if (reason != null) {
            throw new IllegalStateException(reason);
        }
    }

    public int getId() {
        return id;
    }

    public int getArrivalTurn() {
        return arrivalTurn;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public Order getOrder() {
        return order;
    }

    public int getServedTurn() {
        return servedTurn;
    }

    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    public int getRecordedSatisfaction() {
        return recordedSatisfaction;
    }

    public boolean hasBeenServed() {
        return servedTurn != -1;
    }

    public boolean hasPaid() {
        return status == CustomerStatus.PAID;
    }

    public boolean hasLeftUnhappy() {
        return status == CustomerStatus.LEFT;
    }

    public String label() {
        return "C" + id + " (" + getTypeName() + ")";
    }

    @Override
    public String toString() {
        String s = label() + " - satisfaction " + satisfaction + " - " + status.getDescription();
        if (order != null && !status.isFinished()) {
            s += " - " + order.getItem().getName() + " " + order.getStatus();
        }
        return s;
    }
}
