# SC2002 Restaurant Rush — Group 1

A turn-based, command-line restaurant management game in Java. The player is the manager: staff time is limited, so the manager decides which customers and orders come first over 18 turns.

**Repository:** https://github.com/lolmasterdeluxe/SC2002_Restaurant_Rush_Group_1

## How to build and run

Requires JDK 11 or later. No libraries and no network access are needed. Run from the project root.

```bash
# compile
javac -d bin -sourcepath src src/restaurantrush/Main.java

# play
java -cp bin restaurantrush.Main
```

Replay a recorded run (inputs are read from a file, so the result is always the same):

```bash
java -cp bin restaurantrush.Main < test/victory.txt     # macOS / Linux / Windows cmd
java -cp bin restaurantrush.Main < test/defeat.txt
```

```powershell
Get-Content test\victory.txt | java -cp bin restaurantrush.Main   # Windows PowerShell
```

Run the automated rule checks (plain Java, no test framework needed):

```bash
javac -d bin -cp bin test/restaurantrush/model/customer/CustomerChecks.java
java -cp bin restaurantrush.model.customer.CustomerChecks
```

## Stage 2: customer behaviour extension

This version is the Stage 1 game plus two new customer types. It is independently playable and uses the same victory and defeat conditions.

| Customer | Satisfaction lost per waiting turn | Bill | Special behaviour |
|:---|:---:|:---|:---|
| Regular | 8 | Full price | Follows the menu rotation |
| VIP | 5 | 10% off | Follows the menu rotation |
| Critic | 12 | Full price | **Always orders the most expensive item on the menu** (Pasta in this stage), ignoring the rotation |

### Base setting

| Setting | Value |
|:---|:---|
| Turns | 18 (the game always runs all 18) |
| Tables | 2 |
| Staff | 1 Waiter (one task per turn), 1 Chef (one preparation unit per turn), 1 Cashier (one payment per turn, automatic) |
| Arrivals | Turn 1 Regular (C1), turn 4 **VIP** (C2), turn 7 **Critic** (C3), turns 10, 13 and 16 Regular (C4–C6) |
| Menu | Salad $9.00 (1 unit), Burger $15.00 (1 unit), Pasta $18.00 (2 units) |
| Victory | At least 4 paid customers AND revenue of at least $45.00 AND average satisfaction of paid customers of at least 60 |

### Rules we chose and documented

- **Food choice is deterministic.** Regular and VIP customers rotate through the menu by arrival number: customer 1 Salad, 2 Burger, 3 Pasta, 4 Salad, 5 Burger, 6 Pasta. The Critic ignores the rotation and takes the most expensive item.
- **The VIP discount is applied when the order is taken** and rounded to the nearest cent; money is stored as integer cents. The choice is shown as soon as the order is taken.
- **Choices are carried out immediately, Waiter before Chef.** The Chef may cook an order the Waiter took this turn, but a dish finished this turn can only be served next turn.
- **Seating priority is the manager's decision.** The manager picks which queued customer to seat; they go to the first free table.
- **An invalid choice explains why** and lets the manager choose again without using up the staff task.

## Stage comparison

### Transition: Stage 1 → Stage 2

Compare: https://github.com/lolmasterdeluxe/SC2002_Restaurant_Rush_Group_1/compare/stage-1...stage-2 (or `git diff stage-1 stage-2`)

**New files**

| File | Purpose |
|:---|:---|
| `model/customer/VIPCustomer.java` | Extends `Customer`. Loses 5 per waiting turn; 10% off the bill. |
| `model/customer/CriticCustomer.java` | Extends `Customer`. Loses 12 per waiting turn; full price; overrides `chooseFood()` to order the most expensive item. |

**Existing files changed**

| File | Change | Reason |
|:---|:---|:---|
| `model/customer/Customer.java` | Added the hook `getDiscountPercent()` (default 0). `placeOrder()` now applies it to the item price, rounded to the nearest cent. | Stage 1 had no discounts, so there was nothing to override. The VIP needs one place to state its discount; the bill is still worked out in one place for every customer type. |
| `model/customer/CustomerFactory.java` | Arrivals 2 and 3 are now a VIP and a Critic. | Setup change required by the brief: the base run must include one VIP and one Critic, with six arrivals in total. |
| `model/menu/Menu.java` | Added `getMostExpensive()`. | The Critic asks the menu a question instead of looping over its items itself. |
| `test/.../CustomerChecks.java` | Added checks for VIP and Critic loss rates, bills and food choice. | Test coverage for the new behaviour. |

**Deleted files:** none.

**Unchanged, and why it matters:** `GameController`, `ConsoleUI`, `Waiter`, `Chef`, `Cashier`, `Restaurant`, `CustomerRoster`, `WaitingQueue`, `Table` and `Order` did not change at all. They only ever talk to the abstract `Customer`, so the new types work through overriding (`getSatisfactionLossPerTurn()`, `getDiscountPercent()`, `chooseFood()`, `getTypeName()`), with no type checks anywhere in the turn controller. The Stage 1 input files replay unchanged; only the customers behave differently.

## Test notes: reproducible runs

Inputs per turn, in order: Waiter choice (`1` seat + queue position, `2` take order + table, `3` serve + table, `4` wait), Chef choice (`1` cook + table, `2` wait), then `0` to continue. Each line of the test files is one turn. The input files are the same as in Stage 1; the results differ because of the VIP and the Critic.

### Victory run — `test/victory.txt`

Expected final result: **5 paid customers, revenue $64.50, average satisfaction 78.0 — Victory.**

| Turn | Waiter | Chef | What happens |
|:---:|:---|:---|:---|
| 1 | Seat C1 (table 1) | Wait | C1 (Regular) arrives |
| 2 | Take order, table 1 | Cook table 1 | C1 chooses Salad; Salad READY |
| 3 | Serve table 1 | Wait | Salad SERVED |
| 4 | Seat C2 (table 2) | Wait | C2 (VIP) arrives; C1 READY_TO_PAY |
| 5 | Take order, table 2 | Cook table 2 | **VIP pricing:** C2 chooses Burger, bill $13.50 instead of $15.00. Cashier collects $9.00 from C1 |
| 6 | Serve table 2 | Wait | Burger SERVED. **VIP patience:** C2 is at 90 after two waiting turns (a Regular would be at 84) |
| 7 | Seat C3 (table 1) | Wait | C3 (Critic) arrives; C2 READY_TO_PAY |
| 8 | Take order, table 1 | Cook table 1 | C3 chooses Pasta (most expensive; 1 of 2 units). Cashier collects $13.50 from C2 |
| 9 | Wait | Cook table 1 | Pasta READY |
| 10 | Serve table 1 | Wait | C4 arrives. **Staff constraint:** the Waiter can serve the Critic or seat C4, not both. **Critic impatience:** C3 is served at 64 (a Regular would be at 76) |
| 11 | Seat C4 (table 2) | Wait | C3 READY_TO_PAY |
| 12 | Take order, table 2 | Cook table 2 | C4 chooses Salad; READY. Cashier collects $18.00 from C3 |
| 13 | Serve table 2 | Wait | C5 arrives |
| 14 | Seat C5 (table 1) | Wait | C4 READY_TO_PAY |
| 15 | Take order, table 1 | Cook table 1 | C5 chooses Burger; READY. Cashier collects $9.00 from C4 |
| 16 | Serve table 1 | Wait | C6 arrives |
| 17 | Seat C6 (table 2) | Wait | C5 READY_TO_PAY |
| 18 | Take order, table 2 | Cook table 2 | Cashier collects $15.00 from C5. C6 cannot finish in time (intended) |

### Defeat run — `test/defeat.txt`

The manager chooses Wait for the Waiter and the Chef on every turn. Nobody is seated. C1 (Regular) leaves at the end of turn 13 and C3 (Critic) at the end of turn 15, after only nine waiting turns; the VIP is still waiting at the end.

Expected final result: **0 paid customers, revenue $0.00, average satisfaction N/A, 2 unhappy departures — Defeat.**

Stage 1 test notes are in the README at the `stage-1` tag.
