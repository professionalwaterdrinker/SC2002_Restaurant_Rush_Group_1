package restaurantrush.model.staff;

import java.util.List;
import java.util.function.Supplier;

import restaurantrush.model.Restaurant;

/**
 * A member of staff. The "at most one task per turn" rule lives here once, in
 * perform(), so no subclass can forget it. A refused task does not use up the turn.
 */
public abstract class Staff {

    private final String name;
    private boolean actedThisTurn;
    private int currentTurn;

    protected Staff(String name) {
        this.name = name;
    }

    /** Called by the controller at the beginning of each turn. */
    public void startTurn(int turn) {
        this.currentTurn = turn;
        this.actedThisTurn = false;
    }

    public String getName() {
        return name;
    }

    public abstract String getRole();

    /**
     * The tasks this member of staff could do right now, for the start-of-turn
     * display. Each role answers for itself, so the UI needs no role checks.
     */
    public abstract List<String> availableTasks(Restaurant restaurant);

    public boolean hasActedThisTurn() {
        return actedThisTurn;
    }

    public boolean isAvailable() {
        return !actedThisTurn;
    }

    protected int getCurrentTurn() {
        return currentTurn;
    }

    /** Runs a task. Only a successful task counts as this turn's task. */
    protected final ActionResult perform(Supplier<ActionResult> action) {
        if (actedThisTurn) {
            return ActionResult.failure(getRole() + " " + name
                    + " has already acted this turn.");
        }
        ActionResult result = action.get();
        if (result.isSuccess()) {
            actedThisTurn = true;
        }
        return result;
    }
}
