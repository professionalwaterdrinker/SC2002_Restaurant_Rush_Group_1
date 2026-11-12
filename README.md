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

To build an earlier stage, check out its tag first (`git checkout stage-1`, `stage-2` or `stage-3`) and use the same commands.

## Base setting

| Setting | Value |
|:---|:---|
| Turns | 18 (the game always runs all 18; there is no early ending) |
| Tables | 2 |
| Staff | 1 Waiter (one task per turn), 1 Chef (one preparation unit per turn), 1 Cashier (one payment per turn, automatic), and from Stage 3 1 Host (seats one customer per turn) |
| Arrivals | Turn 1 Regular (C1), turn 4 VIP (C2), turn 7 Critic (C3), turns 10, 13 and 16 Regular (C4–C6). Stage 1 has six Regulars. |
| Menu | Salad $9.00 (1 unit), Burger $15.00 (1 unit), Pasta $18.00 (2 units); from Stage 3 also Burger + Salad Combo $21.60 (2 units) and Pasta + Salad Combo $24.30 (3 units) |
| Victory | At the end of turn 18: at least 4 paid customers AND revenue of at least $45.00 AND average satisfaction of paid customers of at least 60. Otherwise defeat. |

## Rules we chose and documented

| Rule | Our choice | Where in the code |
|:---|:---|:---|
| How customers choose food | Deterministic, by arrival number, rotating through the menu. Stage 1 and 2: C1 Salad, C2 Burger, C3 Pasta, C4 Salad, C5 Burger, C6 Pasta. The choice is shown as soon as the order is taken. | `Customer.chooseFood()` |
| Critic's special behaviour | Ignores the rotation and always orders the most expensive item on the menu (Pasta in Stage 2, Pasta + Salad Combo in Stage 3). | `CriticCustomer.chooseFood()` |
| How a customer chooses a combo | Combos are menu items 4 and 5, so the same rotation reaches them. Stage 3: C1 Salad, C2 Burger, C3 Critic takes the Pasta + Salad Combo, C4 Burger + Salad Combo, C5 Pasta + Salad Combo, C6 Salad. | `Menu.menuWithCombos()`, `Customer.chooseFood()` |
| Order of discounts | Combo price first (90% of the two items), then the customer's own discount (VIP 10%), then Happy Hour (20%). Each step is rounded to the nearest cent. Money is stored as integer cents. | `ComboMeal.getPriceCents()`, `PricingService.calculateBill()` |
| When the price is fixed | When the Waiter takes the order. Happy Hour starting or ending later does not change it. | `Customer.placeOrder()`, `Order` |
| Order of actions in a turn | Choices are carried out immediately in the order Host, Waiter, Chef. The Waiter can take the order of a customer the Host seated this turn, and the Chef can cook an order taken this turn. A dish finished this turn can only be served next turn. | `GameController.managerChoice()` |
| Seating priority | The manager picks which queued customer to seat; they go to the first free table. | `GameController.chooseQueuedCustomer()`, `Staff.seat()` |
| Happy Hour | Offered at the start of each turn until it is used. It lasts that turn and the next. At the start of each active turn, customers waiting for a table or food recover 15 (maximum 100). | `HappyHour`, `GameController.managerChoice()` |
| Invalid choices | The game explains why the choice failed and asks again. A refused task does not use up the staff member's turn. | `Staff.perform()`, `ActionResult` |

## Stage overview

| Stage | Tag | Adds | New files | Existing files changed | Deleted |
|:---:|:---:|:---|:---|:---|:---:|
| 1 Basic operation | [`stage-1`](https://github.com/lolmasterdeluxe/SC2002_Restaurant_Rush_Group_1/tree/stage-1) | Regular customers, 3-item menu, Waiter, Chef, Cashier, tables, queue, full turn sequence, victory and defeat | 24 source files (baseline) | — | — |
| 2 Customer behaviour | [`stage-2`](https://github.com/lolmasterdeluxe/SC2002_Restaurant_Rush_Group_1/tree/stage-2) | VIP and Critic customers | `VIPCustomer`, `CriticCustomer` | `Customer`, `CustomerFactory`, `Menu` | None |
| 3 New role and menu policy | [`stage-3`](https://github.com/lolmasterdeluxe/SC2002_Restaurant_Rush_Group_1/tree/stage-3) | Host, ComboMeal, Happy Hour | `Host`, `ComboMeal`, `HappyHour`, `PricingService` | `Waiter`, `Customer`, `CustomerRoster`, `Restaurant`, `Menu`, `GameConfig`, `GameController`, `ConsoleUI`, `Main` | None |

> **Development note (TODO: group to confirm the wording).** We first built the game as one codebase that already contained the Stage 3 features. The three stage versions were then separated into the commits tagged above, so that each stage compiles and runs by itself and contains only its own features. Our report's reflection section describes this.

### Stage 1 baseline

| Package | Classes | Responsibility |
|:---|:---|:---|
| `restaurantrush` | `Main` | Entry point; builds the objects and starts the game |
| `controller` | `GameController` | Runs the six turn steps in order and passes the manager's choices to the staff |
| | `GameConfig`, `GameResult` | The base setting; the three-target result check |
| `ui` | `ConsoleUI` | Boundary: displays the state and reads the manager's choices. Contains no rules. |
| `model` | `Restaurant` | Owns the tables, waiting queue, customer record, menu and revenue |
| | `Table`, `WaitingQueue`, `Order`, `OrderStatus`, `CustomerStatus` | Domain objects and their states |
| `model.customer` | `Customer` (abstract), `RegularCustomer` | Customer lifecycle, satisfaction, food choice |
| | `CustomerFactory`, `CustomerRoster` | The arrival schedule; the record of every customer and the statistics |
| `model.menu` | `MenuItem` (abstract), `Dish`, `Menu` | What can be ordered, its price and preparation units |
| `model.staff` | `Staff` (abstract, holds the shared `seat()` rule), `Waiter`, `Chef`, `Cashier`, `ActionResult` | One task per turn; seat, take order, serve, cook, collect payment |
| `util` | `MoneyUtils` | Integer-cent formatting |

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

### Transition: Stage 2 → Stage 3

Compare: https://github.com/lolmasterdeluxe/SC2002_Restaurant_Rush_Group_1/compare/stage-2...stage-3 (or `git diff stage-2 stage-3`)

**New files**

| File | Purpose |
|:---|:---|
| `model/staff/Host.java` | Seats at most one queued customer per turn, in addition to the Waiter's task. |
| `model/menu/ComboMeal.java` | A `MenuItem` made of two other menu items. Preparation units are the sum; price is 90% of the combined price. |
| `model/HappyHour.java` | The once-per-game, two-turn policy: whether it has been used, turns remaining, the 20% discount and the 15-point recovery. |
| `model/PricingService.java` | The one place a bill is calculated: customer discount first, then Happy Hour. |

**Existing files changed**

| File | Change | Reason |
|:---|:---|:---|
| `model/staff/Waiter.java` | `takeOrder()` passes the restaurant's `PricingService` to the customer. | The bill now depends on Happy Hour, which the customer does not know about. |
| `model/customer/Customer.java` | `placeOrder()` now asks `PricingService` for the bill instead of applying the discount itself. Added `recoverSatisfaction()`. | In Stage 2 the only discount belonged to the customer. Happy Hour is a restaurant-wide policy the customer should not know about, so pricing moved to a class that knows both. The customer still supplies its own discount through `getDiscountPercent()`. |
| `model/customer/CustomerRoster.java` | Added `recoverWaiting()`. | Happy Hour recovery applies to every waiting customer. |
| `model/Restaurant.java` | Owns the `HappyHour` and `PricingService`. | One shared Happy Hour state for the whole restaurant, used by pricing and by the controller. |
| `model/menu/Menu.java` | Added `menuWithCombos()` and `getCombos()`. | Registers the two combos. |
| `model/Order.java`, `model/menu/MenuItem.java` | Comments only. | Explain that the bill is locked when the order is taken and that the base price is before discounts. |
| `controller/GameConfig.java` | Uses `menuWithCombos()`. | Setup change. |
| `controller/GameController.java` | Offers Happy Hour at the start of the turn, applies the second-turn recovery, asks for the Host's choice before the Waiter's, counts Happy Hour down at turn end, and lists the Host's task in "Staff tasks available". | New manager decisions need new prompts. The six-step sequence itself is unchanged. |
| `ui/ConsoleUI.java` | Shows the Happy Hour status and remaining duration. | Required display. |
| `Main.java` | Creates the Host. | Setup change. |
| `test/.../CustomerChecks.java`, `test/victory.txt`, `test/defeat.txt` | Checks for Happy Hour pricing, price locking, recovery and combos; runs updated for the Host and Happy Hour prompts. | Test coverage for the new behaviour. |

**Deleted files:** none.

**Unchanged, and why it matters:** `Chef`, `Cashier`, `Staff`, `Table`, `WaitingQueue`, `Dish`, `GameResult`, `CustomerFactory`, `MoneyUtils` and the three concrete customer classes did not change, and `Order` changed only in its comments. `Order` and `Chef` only use `MenuItem.getPriceCents()` and `getPrepUnits()`, so a `ComboMeal` is cooked, served and paid for exactly like a single dish. The Host reuses the seating rule already in `Staff`, so it needed no seating code of its own.

**Earlier behaviour still works.** Moving the bill calculation from `Customer` into `PricingService` is the one change to a stable rule. `PricingService` uses the same rounding as Stage 2. The Stage 2 pricing checks (Regular Burger $15.00, VIP Burger $13.50, Critic Pasta $18.00) are still in `CustomerChecks` and still pass in Stage 3, and with Happy Hour off the Stage 2 bills are identical.

## Test notes: reproducible runs

All runs use the base setting. There is no randomness, so replaying an input file always gives the same result. Each line of a test file is one turn.

| Stage | `test/victory.txt` | `test/defeat.txt` | Automated checks |
|:---:|:---|:---|:---:|
| 1 | 5 paid, $66.00, average satisfaction 79.2 | 0 paid, $0.00, N/A, 2 departures | 75 pass |
| 2 | 5 paid, $64.50, average satisfaction 78.0 | 0 paid, $0.00, N/A, 2 departures | 76 pass |
| 3 | 5 paid, $88.38, average satisfaction 83.6 | 0 paid, $0.00, N/A, 2 departures | 82 pass |

The turn-by-turn notes for Stages 1 and 2 are in the README at the `stage-1` and `stage-2` tags. The Stage 3 notes follow.

Inputs per turn, in order:

1. Happy Hour: `1` activate, `2` not now (asked only until it has been used)
2. Host: `1` seat + queue position, or `2` wait
3. Waiter: `1` seat + queue position, `2` take order + table, `3` serve + table, or `4` wait
4. Chef: `1` cook + table, or `2` wait
5. `0` to continue

### Stage 3 victory run — `test/victory.txt`

Expected final result: **5 paid customers, revenue $88.38, average satisfaction 83.6 — Victory.**

| Turn | Happy Hour | Host | Waiter | Chef | What happens |
|:---:|:---:|:---|:---|:---|:---|
| 1 | No | Seat C1 (table 1) | Take order, table 1 | Cook table 1 | C1 chooses Salad, $9.00; READY the same turn |
| 2 | No | Wait | Serve table 1 | Wait | Salad SERVED |
| 3 | No | Wait | Wait | Wait | C1 READY_TO_PAY |
| 4 | No | Seat C2 (table 2) | Take order, table 2 | Cook table 2 | **VIP pricing:** C2 chooses Burger, $13.50. Cashier collects $9.00 from C1 (satisfaction 92) |
| 5 | No | Wait | Serve table 2 | Wait | Burger SERVED |
| 6 | No | Wait | Wait | Wait | C2 READY_TO_PAY |
| 7 | No | Seat C3 (table 1) | Take order, table 1 | Cook table 1 | **Critic + combo:** C3 chooses Pasta + Salad Combo, $24.30, 3 units (1 of 3). Cashier collects $13.50 from C2 (95) |
| 8 | No | Wait | Wait | Cook table 1 | 2 of 3 |
| 9 | No | Wait | Wait | Cook table 1 | Combo READY |
| 10 | **Activate** | Seat C4 (table 2) | Serve table 1 | Wait | **Happy Hour recovery:** the Critic goes from 64 to 79 before being served. **Staff constraint:** the Waiter can serve the Critic or take C4's order, not both |
| 11 | (active) | Wait | Take order, table 2 | Cook table 2 | **Happy Hour price:** C4 chooses Burger + Salad Combo, $21.60 × 0.8 = $17.28. C3 READY_TO_PAY |
| 12 | — | Wait | Wait | Cook table 2 | Combo READY. Cashier collects $24.30 from C3 (79) |
| 13 | — | Seat C5 (table 1) | Take order, table 1 | Cook table 1 | **Key decision:** C4's food is READY, but the Waiter takes C5's order first. C5 chooses Pasta + Salad Combo at full price, $24.30 (Happy Hour is over) |
| 14 | — | Wait | Serve table 2 | Cook table 1 | C4 SERVED |
| 15 | — | Wait | Wait | Cook table 1 | C5's combo READY. C4 READY_TO_PAY |
| 16 | — | Wait | Serve table 1 | Wait | C5 SERVED. Cashier collects $17.28 from C4 (76) |
| 17 | — | Seat C6 (table 2) | Take order, table 2 | Cook table 2 | C6 chooses Salad. C5 READY_TO_PAY |
| 18 | — | Wait | Serve table 2 | Wait | Cashier collects $24.30 from C5 (76). C6 is eating and cannot pay within 18 turns (intended) |

### How a manager choice changes the result

On turn 13 the Waiter has two useful tasks and can do only one: serve C4's READY combo, or take C5's order.

| Turn 13 choice | Consequence | Final result |
|:---|:---|:---|
| Take C5's order first (the run above) | C5's 3-unit combo is READY on turn 15, served on 16, paid on 18 | 5 paid, $88.38, average 83.6 |
| Serve C4 first, take C5's order on turn 14 | C5's combo is READY on turn 16, served on 17, and C5 is still waiting to pay when the game ends | 4 paid, $64.08, average 87.5 |

Serving the customer whose food is ready looks like the obvious move, but it costs a whole paying customer and $24.30.

### Stage 3 defeat run — `test/defeat.txt`

The manager never activates Happy Hour and chooses Wait for the Host, Waiter and Chef on every turn. Nobody is seated. C1 (Regular) leaves at the end of turn 13 and C3 (Critic) at the end of turn 15.

Expected final result: **0 paid customers, revenue $0.00, average satisfaction N/A, 2 unhappy departures — Defeat.**

## Design evidence

The class diagram, sequence diagram and design discussion are in the PDF report. Copies of the diagrams are in this repository:

- TODO: add `docs/class-diagram.png` (final design, matching the `stage-3` code)
- TODO: add `docs/sequence-diagram.png`

## Video

TODO: add the video link (it must stay available to graders).

| # | Segment | Timestamp | Presented by |
|:---:|:---|:---:|:---|
| 1 | The game launching from the submitted project | TODO | TODO |
| 2 | Meaningful manager choices, including a decision where staff are constrained | TODO | TODO |
| 3 | A dish going READY → SERVED → payment | TODO | TODO |
| 4 | A VIP or Critic behaviour | TODO | TODO |
| 5 | A Stage 3 feature | TODO | TODO |
| 6 | One victory and one defeat | TODO | TODO |
| 7 | The stage tag comparisons and the diagrams | TODO | TODO |

## Team and task division

| Member | Area | Main files |
|:---|:---|:---|
| TODO: name | Game engine and control (`controller`) | `Main`, `GameController`, `GameConfig`, `GameResult` |
| TODO: name | Presentation and core containers (`ui`, `model`) | `ConsoleUI`, `MoneyUtils`, `Restaurant`, `Table`, `WaitingQueue` |
| TODO: name | Staff (`model.staff`) | `Staff`, `Waiter`, `Chef`, `Cashier`, `Host`, `ActionResult` |
| TODO: name | Customers (`model.customer`) | `Customer`, `RegularCustomer`, `VIPCustomer`, `CriticCustomer`, `CustomerFactory`, `CustomerRoster`, `CustomerStatus` |
| TODO: name | Menu, orders and pricing (`model.menu`, `model`) | `MenuItem`, `Dish`, `ComboMeal`, `Menu`, `Order`, `OrderStatus`, `HappyHour`, `PricingService` |

## Game mechanics, dynamics and aesthetics (MDA)

**Mechanics.** Eighteen turns, two tables, and staff who each do one thing per turn. Customers lose satisfaction every turn they wait for a table or for food (Regular 8, VIP 5, Critic 12) and leave without paying at 0.

**Dynamics.**

- *Staff bottleneck.* The Waiter must constantly choose between bringing in a new customer (seat, take order) and finishing an existing one (serve).
- *Kitchen pipelining.* The Chef can start an order on the turn the Waiter takes it, so ordering early matters more than serving early.
- *Happy Hour timing (Stage 3).* One activation, two turns: it can rescue an impatient Critic's satisfaction, at the cost of 20% off every order taken while it runs.

**Aesthetics.** Limited staff actions against falling satisfaction make each turn a small puzzle; chaining Host, Waiter and Chef on the same customer in one turn is the reward for planning ahead.

## Use of generative AI

TODO: the group states here, consistently with each member's Declaration on Use of GAI Assistance, which tools were used and for what.
