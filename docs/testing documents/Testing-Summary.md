# SmartBudget — Preliminary Testing and Test Coverage Summary

**Deliverable 3.1 — Preliminary testing and JaCoCo coverage**

This document summarizes the initial testing approach, how to run tests and generate the JaCoCo report, and the current coverage status with noted gaps.

---

## 1. Running tests

From the project root, run tests from the `app/` directory:

```bash
cd app
./mvnw test
```

On Windows:

```bash
cd app
./mvnw.cmd test
```

---

## 2. JaCoCo coverage report

JaCoCo is configured in `app/pom.xml`. After running `./mvnw test`, the report is generated at:

- **Path:** `app/target/site/jacoco/index.html`

Open this file in a browser to see:

- Line and branch coverage by package and class
- Coverage percentage for the project
- Uncovered lines and branches

To open from the command line (macOS):

```bash
open app/target/site/jacoco/index.html
```

---

## 3. Test scope

Current tests cover:

| Area | Tests | Purpose |
|------|--------|---------|
| **Domain** | `TransactionTest`, `TransactionFilterTest` | Constructors, getters, `isIncome`/`isExpense`, `isEmpty` and filter criteria. |
| **Validation** | `ValidatorTest` | `validateTransaction` (required fields, amount &gt; 0, note length), `requireNotBlank`, `requirePositive`, `requireDate`. |
| **Services** | `TransactionServiceTest`, `BudgetServiceTest` | Add/get/delete/update, filter, getByRange, getTotalsInRange, getExpensesForCategoryAndMonth; getAllBudgets, getBudgetsForMonth, getBudget, upsertBudget, deleteBudget, getExpensesForCategoryAndMonth. |
| **Persistence** | `JsonStorageTest` | loadTransactions/loadBudgets return empty when file missing; save/load roundtrips for transactions and budgets; loadOnStartup restores data into services. |
| **Sanity** | `SanityTest` | Ensures test suite runs. |

**GUI:** The JavaFX UI (MainApp, RootView, controllers, modals) is not covered by automated tests. Manual testing is used for the UI. TestFX (or similar) for critical flows can be added later.

---

## 4. Coverage summary and gaps

After running `./mvnw test` and opening `app/target/site/jacoco/index.html`:

- **Expected:** High coverage for `domain`, `util`, and `service` (TransactionService, BudgetService). Lower or no coverage for `ui` (RootView, controllers, modals) and `MainApp`.
- **Gaps:**
  - **UI layer:** `ca.yorku.smartbudget.ui.RootView`, controllers, and modals are not exercised by unit tests. Manual testing is used for the GUI.
  - **ReportService / AlertService:** Can be covered by unit tests in a later cycle if desired.
  - **Persistence:** JsonStorage is covered by `JsonStorageTest` (save/load roundtrips, loadOnStartup integration).

---

## 5. Issues and next steps

- **No known failing tests.** Any new failure should be fixed before submission.
- **Current status:** DS-20 (preliminary tests for Validator, domain types, and basic service behavior) is satisfied. DS-13/DS-14 (persistence) are tested via `JsonStorageTest` (save/load correctness, restart regression). Service tests use `InMemoryStorage`.
- **Next development cycles (optional):**
  1. Add unit tests for `ReportService` and `AlertService` if desired.
  3. Consider TestFX (or similar) for critical user flows (e.g. add transaction, switch tabs) once the UI is stable.
  4. Keep the JaCoCo report under `app/target/site/jacoco/` and refresh it with each `./mvnw test`; document any significant coverage drops in future deliverables.

---

## 6. Reference

- Test classes: `app/src/test/java/ca/yorku/smartbudget/`
- JaCoCo plugin: `app/pom.xml` (jacoco-maven-plugin)
- Design and backlog: `docs/diagrams/`, `docs/Backlog-Design-Stories.md`
