package restaurantrush.model.customer;

import java.util.List;

import restaurantrush.model.CustomerStatus;
import restaurantrush.model.HappyHour;
import restaurantrush.model.Order;
import restaurantrush.model.OrderStatus;
import restaurantrush.model.PricingService;
import restaurantrush.model.WaitingQueue;
import restaurantrush.model.menu.Menu;

public class CustomerChecks {

    private static int passed = 0;

    public static void main(String[] args) {
        exampleFromBrief();
        lossPerTypeAndLeaving();
        leavingCancelsTheOrder();
        servedCustomersStopLosing();
        eatingAndPaymentTiming();
        invalidSteps();
        billsAndDiscounts();
        foodChoice();
        happyHourRecovery();
        queueBehaviour();
        baseArrivals();
        rosterTotals();
        System.out.println("All " + passed + " checks passed.");
    }

    static void exampleFromBrief() {
        Menu menu = Menu.standardMenu();
        PricingService pricing = new PricingService(new HappyHour());
        Customer c = new RegularCustomer(3, 1);

        c.seat(1);
        c.finishEating(1);
        c.endOfTurn(1);
        check(c.getSatisfaction() == 92, "turn 1 ends at 92");

        Order order = c.placeOrder(menu, pricing, 2);
        check(order.getItem().getName().equals("Pasta"), "customer 3 orders Pasta");
        check(order.getCustomerId() == 3 && c.getOrder() == order, "order is linked to the customer");
        order.cookOneUnit();
        c.finishEating(2);
        c.endOfTurn(2);
        check(c.getSatisfaction() == 84, "turn 2 ends at 84");

        check(!c.canBeServed(), "not ready yet when the Waiter acts on turn 3");
        order.cookOneUnit();
        check(order.isReady(), "order READY after unit 2");
        c.finishEating(3);
        c.endOfTurn(3);
        check(c.getSatisfaction() == 76, "turn 3 ends at 76");

        c.serve(4);
        check(order.getStatus() == OrderStatus.SERVED, "order SERVED");
        check(!c.finishEating(4), "does not finish eating on the turn served");
        c.endOfTurn(4);
        check(c.getSatisfaction() == 76, "turn 4: no loss once served");

        check(c.finishEating(5) && c.getStatus() == CustomerStatus.READY_TO_PAY, "ready to pay on turn 5");
        check(!c.canPay(5), "cannot pay on the turn they became ready");
        c.endOfTurn(5);
        check(c.getSatisfaction() == 76, "turn 5: no loss while ready to pay");

        check(c.pay(6) == 1800, "pays $18.00");
        check(order.getStatus() == OrderStatus.PAID && c.hasPaid(), "customer and order PAID");
        check(c.getRecordedSatisfaction() == 76, "satisfaction 76 recorded");
        check(!c.getStatus().occupiesTable(), "table is free after paying");
    }

    static void lossPerTypeAndLeaving() {
        check(turnsUntilLeaving(new RegularCustomer(1, 1)) == 13, "Regular leaves after 13 waiting turns");
        check(turnsUntilLeaving(new VIPCustomer(1, 1)) == 20, "VIP leaves after 20 waiting turns");
        check(turnsUntilLeaving(new CriticCustomer(1, 1)) == 9, "Critic leaves after 9 waiting turns");
        Customer c = new RegularCustomer(1, 1);
        turnsUntilLeaving(c);
        check(c.getSatisfaction() == 0, "satisfaction never goes below 0");
        check(c.hasLeftUnhappy() && !c.getStatus().occupiesTable(), "a departed customer frees the table");
        check(!c.endOfTurn(99), "a departed customer is only reported once");
    }

    static void leavingCancelsTheOrder() {
        Customer c = new CriticCustomer(1, 1);
        c.seat(1);
        Order order = c.placeOrder(Menu.standardMenu(), new PricingService(new HappyHour()), 1);
        turnsUntilLeaving(c);
        check(order.getStatus() == OrderStatus.CANCELLED, "order cancelled when the customer leaves");
        check(!order.cookOneUnit(), "the Chef cannot cook a cancelled order");
    }

    static void servedCustomersStopLosing() {
        Customer c = servedCustomer(new CriticCustomer(1, 1), 2);
        for (int t = 2; t < 12; t++) {
            c.endOfTurn(t);
        }
        check(c.getSatisfaction() == 100, "served customers lose nothing");
        Customer seated = new RegularCustomer(2, 1);
        seated.seat(1);
        seated.endOfTurn(1);
        check(seated.getSatisfaction() == 92, "seated but not served still loses satisfaction");
    }

    static void eatingAndPaymentTiming() {
        Customer c = servedCustomer(new RegularCustomer(1, 1), 3);
        check(!c.finishEating(3), "no eating finished on serving turn");
        check(c.finishEating(4), "eating finished the turn after serving");
        check(!c.finishEating(5), "finishing eating is reported once");
        check(!c.canPay(4) && c.canPay(5), "pays the turn after becoming ready");
    }

    static void invalidSteps() {
        Menu menu = Menu.standardMenu();
        PricingService pricing = new PricingService(new HappyHour());
        Customer c = new RegularCustomer(3, 1);
        expectRefused(() -> c.placeOrder(menu, pricing, 1), "cannot order before being seated");
        expectRefused(() -> c.serve(1), "cannot be served before ordering");
        expectRefused(() -> c.pay(1), "cannot pay before ready to pay");
        c.seat(1);
        expectRefused(() -> c.seat(1), "cannot be seated twice");
        check(c.whyNotSeat().contains("seated"), "refusal says what state they are in");
        c.placeOrder(menu, pricing, 1);
        check(c.whyNotServe().contains("Pasta is not ready yet (0 of 2"), "refusal explains the dish isn't ready");
        expectRefused(() -> c.serve(1), "cannot serve a dish that isn't READY");
        check(c.getStatus() == CustomerStatus.ORDERED, "a refused step changes nothing");
    }

    static void billsAndDiscounts() {
        Menu menu = Menu.standardMenu();
        HappyHour happyHour = new HappyHour();
        PricingService pricing = new PricingService(happyHour);
        check(billFor(new RegularCustomer(2, 1), menu, pricing) == 1500, "Regular Burger is $15.00");
        check(billFor(new VIPCustomer(2, 4), menu, pricing) == 1350, "VIP Burger is $13.50");
        check(billFor(new CriticCustomer(3, 7), menu, pricing) == 1800, "Critic pays full price for Pasta");

        happyHour.activate();
        check(billFor(new VIPCustomer(2, 4), menu, pricing) == 1080, "VIP Burger in Happy Hour is $10.80");
        check(billFor(new RegularCustomer(1, 1), menu, pricing) == 720, "Regular Salad in Happy Hour is $7.20");

        HappyHour hh2 = new HappyHour();
        PricingService p2 = new PricingService(hh2);
        Customer c = new RegularCustomer(2, 1);
        c.seat(1);
        Order o = c.placeOrder(menu, p2, 1);
        hh2.activate();
        check(o.getBillCents() == 1500, "Happy Hour starting later doesn't change a placed order");
    }

    static void foodChoice() {
        Menu menu = Menu.standardMenu();
        String[] expected = {"Salad", "Burger", "Pasta", "Salad", "Burger", "Pasta"};
        for (int id = 1; id <= 6; id++) {
            check(new RegularCustomer(id, 1).chooseFood(menu).getName().equals(expected[id - 1]),
                    "customer " + id + " picks " + expected[id - 1]);
        }
        check(new VIPCustomer(2, 4).chooseFood(menu).getName().equals("Burger"), "VIP uses the same rotation");
        check(new CriticCustomer(1, 1).chooseFood(menu).getName().equals("Pasta"), "Critic picks the most expensive");

        Menu combos = Menu.menuWithCombos();
        check(new CriticCustomer(3, 7).chooseFood(combos).getName().equals("Pasta + Salad Combo"),
                "Critic picks the Pasta + Salad Combo in Stage 3");
        check(new RegularCustomer(4, 10).chooseFood(combos).getName().equals("Burger + Salad Combo"),
                "rotation reaches combos in Stage 3");
    }

    static void happyHourRecovery() {
        Customer waiting = new RegularCustomer(1, 1);
        waiting.endOfTurn(1);
        waiting.endOfTurn(2);
        waiting.recoverSatisfaction(HappyHour.SATISFACTION_RECOVERY);
        check(waiting.getSatisfaction() == 99, "waiting customer recovers 15");
        waiting.recoverSatisfaction(HappyHour.SATISFACTION_RECOVERY);
        check(waiting.getSatisfaction() == 100, "recovery is capped at 100");

        Customer eating = new RegularCustomer(2, 1);
        eating.seat(1);
        eating.endOfTurn(1);
        Order o = eating.placeOrder(Menu.standardMenu(), new PricingService(new HappyHour()), 2);
        o.cookOneUnit();
        eating.serve(3);
        eating.recoverSatisfaction(HappyHour.SATISFACTION_RECOVERY);
        check(eating.getSatisfaction() == 92, "customers already served do not recover");
    }

    static void queueBehaviour() {
        WaitingQueue q = new WaitingQueue();
        Customer a = new RegularCustomer(1, 1);
        Customer b = new CriticCustomer(2, 4);
        q.join(a);
        q.join(b);
        check(q.get(1) == a && q.get(2) == b, "queue keeps arrival order, positions start at 1");
        check(q.get(3) == null && q.get(0) == null, "out-of-range position gives null");
        q.join(a);
        check(q.size() == 2, "same customer is not added twice");
        for (int t = 1; t <= 9; t++) {
            a.endOfTurn(t);
            b.endOfTurn(t);
        }
        List<Customer> gone = q.removeDeparted();
        check(gone.size() == 1 && gone.get(0) == b && q.size() == 1, "departed customers leave the queue");
        q.remove(a);
        a.seat(10);
        check(q.isEmpty(), "a seated customer is removed from the queue");
    }

    static void baseArrivals() {
        CustomerFactory f = CustomerFactory.baseSetting();
        check(f.size() == 6, "six arrivals");
        int[] turns = {1, 4, 7, 10, 13, 16};
        for (int i = 0; i < turns.length; i++) {
            Customer c = f.arrivalFor(turns[i]);
            check(c != null && c.getArrivalTurn() == turns[i] && c.getId() == i + 1,
                    "customer " + (i + 1) + " arrives on turn " + turns[i]);
        }
        check(f.arrivalFor(2) == null, "nobody arrives on turn 2");
        check(f.arrivalFor(4) == f.arrivalFor(4), "same turn gives the same customer");
        check(f.arrivalFor(4) instanceof VIPCustomer && f.arrivalFor(7) instanceof CriticCustomer,
                "base run includes a VIP and a Critic");
    }

    static void rosterTotals() {
        CustomerRoster r = new CustomerRoster();
        check(r.averagePaidSatisfactionText().equals("N/A"), "average is N/A before anyone pays");

        Menu menu = Menu.standardMenu();
        PricingService pricing = new PricingService(new HappyHour());
        Customer a = new RegularCustomer(1, 1);
        Customer b = new VIPCustomer(2, 1);
        Customer c = new CriticCustomer(3, 1);
        r.add(a);
        r.add(b);
        r.add(c);
        check(r.findById(2) == b && r.findById(9) == null, "find a customer by id");
        a.seat(1);
        a.placeOrder(menu, pricing, 1).cookOneUnit();
        b.seat(1);
        b.placeOrder(menu, pricing, 1).cookOneUnit();
        r.endOfTurn(1);
        a.serve(2);
        b.serve(2);
        check(r.finishEating(3).size() == 2, "two finish eating on turn 3");
        check(r.readyToPay(3).isEmpty(), "nobody pays on the turn they became ready");
        List<Customer> ready = r.readyToPay(4);
        check(ready.size() == 2 && ready.get(0) == a, "oldest ready customer is first");
        int revenue = a.pay(4) + b.pay(5);
        check(revenue == 900 + 1350, "revenue is $9.00 + $13.50");
        for (int t = 2; t <= 9; t++) {
            r.endOfTurn(t);
        }
        check(r.paidCount() == 2 && r.servedCount() == 2 && r.unhappyDepartures() == 1, "paid, served, departures");
        check(r.averagePaidSatisfactionText().equals("93.5"), "average of 92 and 95 is 93.5");
        check(r.present().isEmpty(), "nobody left in the restaurant");
    }

    static Customer servedCustomer(Customer c, int serveTurn) {
        c.seat(1);
        Order o = c.placeOrder(Menu.standardMenu(), new PricingService(new HappyHour()), 1);
        while (!o.isReady()) {
            o.cookOneUnit();
        }
        c.serve(serveTurn);
        return c;
    }

    static int billFor(Customer c, Menu menu, PricingService pricing) {
        c.seat(1);
        return c.placeOrder(menu, pricing, 1).getBillCents();
    }

    static int turnsUntilLeaving(Customer c) {
        int t = 0;
        while (!c.hasLeftUnhappy()) {
            t++;
            c.endOfTurn(t);
            if (t > 100) {
                throw new AssertionError("never left");
            }
        }
        return t;
    }

    static void expectRefused(Runnable action, String what) {
        try {
            action.run();
        } catch (IllegalStateException e) {
            passed++;
            return;
        }
        throw new AssertionError("FAILED: " + what + " (it was allowed)");
    }

    static void check(boolean ok, String what) {
        if (!ok) {
            throw new AssertionError("FAILED: " + what);
        }
        passed++;
    }
}
