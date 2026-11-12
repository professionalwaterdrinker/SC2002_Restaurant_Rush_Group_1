package restaurantrush;

import java.util.NoSuchElementException;

import restaurantrush.controller.GameConfig;
import restaurantrush.controller.GameController;
import restaurantrush.controller.GameResult;
import restaurantrush.model.Restaurant;
import restaurantrush.model.customer.CustomerFactory;
import restaurantrush.model.staff.Cashier;
import restaurantrush.model.staff.Chef;
import restaurantrush.model.staff.Waiter;
import restaurantrush.ui.ConsoleUI;

/** Entry point: builds the base-setting game and starts it. */
public class Main {
    public static void main(String[] args) {
        Restaurant restaurant = new Restaurant(GameConfig.getNumTables(), GameConfig.getMenu());
        ConsoleUI console = new ConsoleUI();
        GameResult result = new GameResult(restaurant);

        GameController controller = new GameController(restaurant, console,
                CustomerFactory.baseSetting(), new Waiter("Wendy"), new Chef("Carl"),
                new Cashier("Cathy"), result);

        try {
            controller.startGame();
        } catch (NoSuchElementException e) {
            // Input closed (e.g. a piped test file ran out) before turn 18.
            console.displayMessage("\nInput ended before the game finished. Game stopped.");
        }
    }
}
