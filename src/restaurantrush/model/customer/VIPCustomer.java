package restaurantrush.model.customer;

public class VIPCustomer extends Customer {

    public VIPCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public String getTypeName() {
        return "VIP";
    }

    @Override
    public int getSatisfactionLossPerTurn() {
        return 5;
    }

    @Override
    public int getDiscountPercent() {
        return 10;
    }
}
