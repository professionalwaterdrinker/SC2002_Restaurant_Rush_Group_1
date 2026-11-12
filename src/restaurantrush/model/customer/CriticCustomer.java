package restaurantrush.model.customer;

import restaurantrush.model.menu.Menu;
import restaurantrush.model.menu.MenuItem;

public class CriticCustomer extends Customer {

    public CriticCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public String getTypeName() {
        return "Critic";
    }

    @Override
    public int getSatisfactionLossPerTurn() {
        return 12;
    }

    @Override
    public MenuItem chooseFood(Menu menu) {
        return menu.getMostExpensive();
    }
}
