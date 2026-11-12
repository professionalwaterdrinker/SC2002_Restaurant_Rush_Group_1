package restaurantrush;

import restaurantrush.model.Restaurant;
import restaurantrush.ui.ConsoleUI;
import restaurantrush.controller.GameConfig;
import restaurantrush.controller.GameController;
import restaurantrush.controller.GameResult;
import restaurantrush.model.staff.Cashier;
import restaurantrush.model.staff.Waiter;
import restaurantrush.model.staff.Chef;

public class Main {
    public static void main(String[] args) {
        Waiter waiter = new Waiter("waiter");
        Chef chef = new Chef("chef");
        Cashier cashier = new Cashier("cashier");
        ConsoleUI console = new ConsoleUI();
        Restaurant restaurant = new Restaurant(GameConfig.getNumTables(), GameConfig.getMenu());
        GameResult result = new GameResult(restaurant);

        GameController controller = new GameController(restaurant, console, waiter, chef, cashier, result);

        controller.startGame();
    }
}
