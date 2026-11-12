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

## Stage 1: basic restaurant operation

This version contains only the Stage 1 game: Regular customers, the three menu items, Waiter, Chef, Cashier, tables, orders, the waiting queue, the full six-step turn sequence, and both endings.

### Base setting

| Setting | Value |
|:---|:---|
| Turns | 18 (the game always runs all 18) |
| Tables | 2 |
| Staff | 1 Waiter (one task per turn), 1 Chef (one preparation unit per turn), 1 Cashier (one payment per turn, automatic) |
| Arrivals | One Regular customer at the start of turns 1, 4, 7, 10, 13 and 16 |
| Menu | Salad $9.00 (1 unit), Burger $15.00 (1 unit), Pasta $18.00 (2 units) |
| Victory | At least 4 paid customers AND revenue of at least $45.00 AND average satisfaction of paid customers of at least 60 |

### Rules we chose and documented

- **Food choice is deterministic.** Customers choose their own food by arrival number, rotating through the menu: customer 1 Salad, 2 Burger, 3 Pasta, 4 Salad, 5 Burger, 6 Pasta (`Customer.chooseFood`). The choice and the bill are shown as soon as the Waiter takes the order.
- **Choices are carried out immediately, Waiter before Chef.** The Chef may therefore cook an order the Waiter took this turn, but a dish the Chef finishes this turn can only be served next turn.
- **Seating is the manager's decision.** The manager picks which queued customer to seat and at which free table.
- **An invalid choice explains why** and lets the manager choose again without using up the staff task.

### Test notes: reproducible runs

Inputs per turn, in order: Waiter choice (`1` seat + queue position + table, `2` take order + table, `3` serve + table, `4` wait), Chef choice (`1` cook + table, `2` wait), then `0` to continue. Each line of the test files is one turn.

#### Victory run — `test/victory.txt`

Expected final result: **5 paid customers, revenue $66.00, average satisfaction 79.2 — VICTORY.**

| Turn | Waiter | Chef | What happens |
|:---:|:---|:---|:---|
| 1 | Seat C1 at table 1 | Wait | C1 arrives |
| 2 | Take order, table 1 | Cook table 1 | C1 chooses Salad; Salad READY |
| 3 | Serve table 1 | Wait | Salad SERVED |
| 4 | Seat C2 at table 2 | Wait | C2 arrives; C1 READY_TO_PAY |
| 5 | Take order, table 2 | Cook table 2 | C2 chooses Burger; READY. Cashier collects $9.00 from C1 (satisfaction 84) |
| 6 | Serve table 2 | Wait | Burger SERVED |
| 7 | Seat C3 at table 1 | Wait | C3 arrives; C2 READY_TO_PAY |
| 8 | Take order, table 1 | Cook table 1 | C3 chooses Pasta (1 of 2 units). Cashier collects $15.00 from C2 |
| 9 | Wait | Cook table 1 | Pasta READY |
| 10 | Serve table 1 | Wait | C4 arrives. **Staff constraint:** the Waiter can serve C3 or seat C4, not both |
| 11 | Seat C4 at table 2 | Wait | C3 READY_TO_PAY |
| 12 | Take order, table 2 | Cook table 2 | C4 chooses Salad; READY. Cashier collects $18.00 from C3 |
| 13 | Serve table 2 | Wait | C5 arrives |
| 14 | Seat C5 at table 1 | Wait | C4 READY_TO_PAY |
| 15 | Take order, table 1 | Cook table 1 | C5 chooses Burger; READY. Cashier collects $9.00 from C4 |
| 16 | Serve table 1 | Wait | C6 arrives |
| 17 | Seat C6 at table 2 | Wait | C5 READY_TO_PAY |
| 18 | Take order, table 2 | Cook table 2 | Cashier collects $15.00 from C5. C6 cannot finish in time (intended) |

#### Defeat run — `test/defeat.txt`

The manager chooses Wait for the Waiter and the Chef on every turn. Nobody is seated, C1 and C2 reach satisfaction 0 and leave (end of turns 13 and 16).

Expected final result: **0 paid customers, revenue $0.00, average satisfaction N/A, 2 unhappy departures — DEFEAT.**
