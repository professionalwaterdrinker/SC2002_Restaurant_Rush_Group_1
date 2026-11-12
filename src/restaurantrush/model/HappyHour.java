package restaurantrush.model;

/**
 * Stage 3: Happy Hour gives 20% off orders taken while it is active.
 * It can be used only ONCE per game and lasts 2 turns.
 *
 * How the controller uses it:
 *   - Manager picks "start Happy Hour"  -> happyHour.activate()
 *   - At the end of every turn          -> happyHour.endTurn()
 *   - When pricing an order             -> PricingService checks isActive()
 */
public class HappyHour {
    public static final int DURATION_TURNS = 2;
    public static final int DISCOUNT_PERCENT = 20;
    public static final int SATISFACTION_RECOVERY = 15;

    private boolean used;
    private int turnsRemaining;

    public HappyHour() {
        this.used = false;
        this.turnsRemaining = 0;
    }

    /** True if Happy Hour hasn't been used yet this game. */
    public boolean canActivate() {
        return !used;
    }

    /**
     * Starts Happy Hour for the current turn and the next one.
     * @return false if it was already used this game.
     */
    public boolean activate() {
        if (used) return false;
        used = true;
        turnsRemaining = DURATION_TURNS;
        return true;
    }

    public boolean isActive() {
        return turnsRemaining > 0;
    }

    /** Call once at the end of every turn. Counts down while active. */
    public void endTurn() {
        if (turnsRemaining > 0) turnsRemaining--;
    }

    public boolean isUsed() {
        return used;
    }

    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    public int getDiscountPercent() {
        return DISCOUNT_PERCENT;
    }
}
