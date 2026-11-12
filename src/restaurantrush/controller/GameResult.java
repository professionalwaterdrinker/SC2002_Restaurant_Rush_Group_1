package restaurantrush.controller;

import java.util.ArrayList;
import java.util.List;

import restaurantrush.model.Restaurant;
import restaurantrush.util.MoneyUtils;

/**
 * Step 6: checks the three targets after the last turn.
 * Victory needs at least 4 paid customers AND revenue of at least $45.00 AND
 * an average satisfaction of paid customers of at least 60.
 */
public class GameResult {
    private final Restaurant restaurant;

    public GameResult(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public boolean paidTargetMet() {
        return restaurant.getRoster().paidCount() >= GameConfig.getPaidCustomersGoal();
    }

    public boolean revenueTargetMet() {
        return restaurant.getTotalRevenueCents() >= GameConfig.getRevenueGoal();
    }

    /** With no paid customers there is no average, so the target is not met. */
    public boolean satisfactionTargetMet() {
        return restaurant.getRoster().paidCount() > 0
                && restaurant.getRoster().averagePaidSatisfaction() >= GameConfig.getAvgSatisfactionGoal();
    }

    public boolean isVictory() {
        return paidTargetMet() && revenueTargetMet() && satisfactionTargetMet();
    }

    /** One line per target, with the value reached and whether it was met. */
    public List<String> summaryLines() {
        List<String> lines = new ArrayList<>();
        lines.add(line("Paid customers", String.valueOf(restaurant.getRoster().paidCount()),
                String.valueOf(GameConfig.getPaidCustomersGoal()), paidTargetMet()));
        lines.add(line("Revenue", MoneyUtils.formatCents(restaurant.getTotalRevenueCents()),
                MoneyUtils.formatCents(GameConfig.getRevenueGoal()), revenueTargetMet()));
        lines.add(line("Average satisfaction of paid customers",
                restaurant.getRoster().averagePaidSatisfactionText(),
                String.valueOf(GameConfig.getAvgSatisfactionGoal()), satisfactionTargetMet()));
        lines.add("Served customers: " + restaurant.getRoster().servedCount()
                + " | Unhappy departures: " + restaurant.getRoster().unhappyDepartures());
        return lines;
    }

    private static String line(String label, String value, String target, boolean met) {
        return (met ? "[MET]     " : "[NOT MET] ") + label + ": " + value + " (target: at least " + target + ")";
    }
}
