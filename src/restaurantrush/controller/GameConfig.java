package restaurantrush.controller;

import restaurantrush.model.menu.Menu;

public class GameConfig {
    private static Menu menu = Menu.menuWithCombos();
    private static int numTables = 2;
    private static int turns = 18;
    private static int paidCustomersGoal = 4;
    private static int revenueGoal = 4500; // $45.00 in cents
    private static double avgSatisfactionGoal = 60.0;

    public static Menu getMenu() {
        return menu;
    }

    public static int getNumTables() {
        return numTables;
    }

    public static int getTurns() {
        return turns;
    }

    public static int getPaidCustomersGoal() {
        return paidCustomersGoal;
    }

    public static int getRevenueGoal() {
        return revenueGoal;
    }

    public static double getAvgSatisfactionGoal() {
        return avgSatisfactionGoal;
    }
}
