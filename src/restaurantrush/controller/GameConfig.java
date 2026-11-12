package restaurantrush.controller;

import restaurantrush.model.menu.Menu;

/** The required base setting (handout Section 2.1). */
public final class GameConfig {
    private static final int NUM_TABLES = 2;
    private static final int TURNS = 18;
    private static final int PAID_CUSTOMERS_GOAL = 4;
    private static final int REVENUE_GOAL_CENTS = 4500; // $45.00
    private static final int AVG_SATISFACTION_GOAL = 60;

    private GameConfig() {
    }

    /** Salad, Burger and Pasta. */
    public static Menu getMenu() {
        return Menu.standardMenu();
    }

    public static int getNumTables() {
        return NUM_TABLES;
    }

    public static int getTurns() {
        return TURNS;
    }

    public static int getPaidCustomersGoal() {
        return PAID_CUSTOMERS_GOAL;
    }

    public static int getRevenueGoal() {
        return REVENUE_GOAL_CENTS;
    }

    public static int getAvgSatisfactionGoal() {
        return AVG_SATISFACTION_GOAL;
    }
}
