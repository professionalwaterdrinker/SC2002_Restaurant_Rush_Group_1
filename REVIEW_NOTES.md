# Review notes for the group (delete this file before submission)

This branch (`review/fixes`) is a proposal. Nothing on `main` has changed. It sits on top of `main` at `09c1ed3` as three stage commits plus this note.

| Commit | What it is | Intended tag |
|:---|:---|:---|
| `6261bb0` | Stage 1 only: Regular customers, Waiter, Chef, Cashier | `stage-1` |
| `41a6b37` | Stage 2: adds VIP and Critic | `stage-2` |
| `27e9bf1` | Stage 3: adds Host, ComboMeal, Happy Hour, and the full README | `stage-3` |

Each commit was checked from a clean checkout: it compiles with no warnings, its automated checks pass (75 / 76 / 82), and `test/victory.txt` and `test/defeat.txt` replay to the results stated in the README.

No tags have been created. Once the group agrees, and after these commits are on the group repository:

```bash
git tag stage-1 6261bb0
git tag stage-2 41a6b37
git tag stage-3 27e9bf1
git push origin stage-1 stage-2 stage-3
```

The compare and tag links in the README only work after the tags are pushed. If anything in a stage commit is edited, the hashes change, so tag last.

## What changed against `main`, by review item

1. **Stage versions.** `main` only had a Stage 3 game. Stage 1 and Stage 2 were produced by removing the later features, then adding them back in two commits, so the diffs between stages show real additions.
2. **README.** Repository URL, build and run commands, stage table, per-transition new/changed/deleted files with reasons, compare links, placeholders for video timestamps.
3. **Reproducible runs.** `test/victory.txt` and `test/defeat.txt` at every stage, with turn-by-turn notes.
4. **"Bundling codebase for AI assistance" section removed** from the README.
5. **End-of-turn results** now appear after payment and abandonment, and the final screen shows all three targets and whether each was met.
6. **Payments, finished meals and walkouts are announced.**
7. **Seating is a real choice:** the manager picks the queued customer and the table.
8. **"Staff tasks available"** is on the start-of-turn display, and "Turns used" is in the results.

Smaller clean-ups made along the way: the controller no longer prints directly or keeps a static turn counter; the UI no longer hardcodes 18; no empty or catch-all `catch` blocks; the game stops cleanly if piped input ends; `seat()` moved out of `Staff` so the Chef and Cashier do not inherit it; unused methods removed; `Waiter`/`Chef` now take the `Table` the manager chose and explain precisely why a choice failed.

## Decisions for the group

- **How the stages were made.** The README has a short "Development note" saying the stages were separated after the Stage 3 code existed. Keep it truthful; the commit dates show it anyway. Agree the wording and cover it in the report's reflection.
- **Use of AI.** This branch was prepared with help from Claude (an AI assistant). That is GAI assistance and belongs in the declarations and the final commit attribution. The README has a TODO section for the group's statement.
- **Critic in Stage 2.** The Critic is customer 3, and the rotation already gives customer 3 Pasta, which is also the most expensive item. So in the Stage 2 base run the Critic's food preference looks the same as a Regular's; only the faster satisfaction loss is visible. Moving the Critic to arrival 4 (rotation says Salad, Critic takes Pasta) would make the preference visible. Not changed here.
- **Abstract `MenuItem` in Stage 1.** Stage 1 keeps `MenuItem` (abstract) with a single subclass `Dish`, as on `main`. It makes Stage 3 clean (`ComboMeal` is added without touching `Order` or `Chef`) but a marker could ask why Stage 1 needs it. Be ready to explain, or make `MenuItem` concrete in Stage 1.
- **Staff names** in `Main` (Wendy, Carl, Cathy, Hana) are placeholders.
- **Defeat run** is the passive one (everyone waits). A defeat caused by poor prioritisation would be a stronger demonstration for the video.

## Still to do (not part of this branch)

- Fill the README TODOs: member names, video link and timestamps, GAI statement.
- Class diagram and sequence diagram (report and `docs/`). The turn-13 decision in the README ("How a manager choice changes the result") is a ready-made scenario for the sequence diagram.
- PDF report, video, signed declarations.
