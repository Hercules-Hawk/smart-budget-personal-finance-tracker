# SmartBudget — Updated Backlog with Design Stories

**Deliverable 3.1 — Design-specific user stories**

This backlog integrates design tasks needed to translate the SDD and diagrams into implementable features. Items are prioritized by dependencies and project phase. Design stories are linked to the overall timeline to support a smooth transition into later development.

---

## Priority and timeline key

- **P0** — Must have for Deliverable 3.1 / MVP
- **P1** — Next after core flows
- **P2** — Follow-up enhancements

Phases align with: **Phase 1** (current — GUI shell, navigation, SDD/backlog), **Phase 2** (transactions + persistence), **Phase 3** (budgets + alerts), **Phase 4** (reports + polish).

---

## Design stories

### Architecture and foundation

| ID | Story | Priority | Phase | Dependencies |
|----|--------|----------|--------|----------------|
| DS-1 | As a developer, I have a Software Design Document that references all class, sequence, and activity diagrams so that the architecture is clear and consistent. | P0 | 1 | — |
| DS-2 | As a developer, I have a layer diagram showing UI → Service → Domain → Persistence so that dependency rules are explicit. | P0 | 1 | — |
| DS-3 | As a developer, I have an updated backlog with design stories and priorities so that design tasks are traceable to the timeline. | P0 | 1 | — |
| DS-4 | As a developer, the codebase follows the SDD package structure (ui, service, domain, persistence, util) so that new code fits the blueprint. | P0 | 2 | DS-1 |

### GUI and navigation

| ID | Story | Priority | Phase | Dependencies |
|----|--------|----------|--------|----------------|
| DS-5 | As a user, I see a main window with primary navigation (e.g. Transactions, Budgets, Reports) so that I can reach essential screens. | P0 | 1 | — |
| DS-6 | As a user, I land on a Transactions screen with basic layout (table/list placeholder, add button) so that the Record Transaction flow has a clear target. | P0 | 1 | DS-5 |
| DS-7 | As a user, I can open a Budgets screen with basic layout (list of budgets placeholder, add budget action) so that Set/Edit Budget flows have a target. | P0 | 1 | DS-5 |
| DS-8 | As a user, I can open a Reports screen with basic layout (period selector, summary/chart area) so that View Summary Report has a target. | P0 | 1 | DS-5 |
| DS-9 | As a developer, initial GUI screenshots are documented (e.g. in docs/UI design screenshots/) so that the design approach is visible for deliverables. | P0 | 1 | DS-5, DS-6, DS-7, DS-8 |

### Transactions (UC-1, UC-2, UC-3)

| ID | Story | Priority | Phase | Dependencies |
|----|--------|----------|--------|----------------|
| DS-10 | As a developer, TransactionService implements add/update/delete/getById and uses Storage and Validator as in Diagram A so that UC-1/UC-2 are implementable. | P0 | 2 | DS-4 |
| DS-11 | As a developer, TransactionsController is wired to TransactionService and opens an Add/Edit Transaction form (modal or inline) as in activity diagram UC-1 so that users can record transactions. | P0 | 2 | DS-6, DS-10 |
| DS-12 | As a developer, filtering (TransactionFilter) is implemented in TransactionService and exposed in the Transactions UI as in UC-3 so that users can view and filter transactions. | P1 | 2 | DS-10, DS-11 |

### Persistence and startup

| ID | Story | Priority | Phase | Dependencies |
|----|--------|----------|--------|----------------|
| DS-13 | As a developer, Storage interface and JsonStorage exist and are used by TransactionService (and later BudgetService) as in class diagrams so that data survives restarts. | P0 | 2 | DS-4 |
| DS-14 | As a user, the app loads saved transactions (and later budgets) on startup as in the “Load Saved Data on Startup” activity diagram so that I see my data when I open the app. | P0 | 2 | DS-13, DS-10 |

### Budgets and alerts (UC-4, UC-6)

| ID | Story | Priority | Phase | Dependencies |
|----|--------|----------|--------|----------------|
| DS-15 | As a developer, BudgetService and Budget domain type exist as in Diagram B so that budgets can be set and stored. | P0 | 3 | DS-13 |
| DS-16 | As a developer, AlertService implements checkBudgetStatus/checkOverspendingFor and returns OverspendingAlert as in sequence diagram SD-2 so that overspending can be shown after add transaction or set budget. | P0 | 3 | DS-10, DS-15 |
| DS-17 | As a user, I can set or edit a monthly budget from the Budgets screen and see confirmation or overspending alert as in UC-4/UC-6. | P0 | 3 | DS-7, DS-15, DS-16 |

### Reports (UC-5)

| ID | Story | Priority | Phase | Dependencies |
|----|--------|----------|--------|----------------|
| DS-18 | As a developer, ReportService and ReportResult/SummaryReport/CategoryTotal/PeriodRange exist as in Diagram C so that summary reports can be generated. | P0 | 4 | DS-10 |
| DS-19 | As a user, I can select a period (e.g. This month, All time, Custom range) and see a summary report and optional chart as in UC-5. | P0 | 4 | DS-8, DS-18 |

### Testing and quality

| ID | Story | Priority | Phase | Dependencies |
|----|--------|----------|--------|----------------|
| DS-20 | As a developer, preliminary tests cover Validator, domain types, and basic service behavior so that regressions are caught. | P0 | 1–2 | — |
| DS-21 | As a developer, a JaCoCo coverage report is generated and a short testing summary (coverage %, gaps, next steps) is documented so that Deliverable 3.1 testing is evidenced. | P0 | 1 | DS-20 |

---

## Timeline overview

| Phase | Focus | Design story IDs |
|-------|--------|-------------------|
| 1 | SDD, backlog, GUI shell, navigation, testing summary | DS-1, DS-2, DS-3, DS-5–DS-9, DS-20, DS-21 |
| 2 | Transactions, persistence, startup load | DS-4, DS-10–DS-14 |
| 3 | Budgets and alerts | DS-15–DS-17 |
| 4 | Reports and polish | DS-18, DS-19 |

---

## Notes

- Existing user stories (e.g. in `docs/User stories.docx` / `.pdf`) remain the source for *user-facing* features; this backlog focuses on *design and implementation* tasks that support those features.
- When implementing, complete DS-5–DS-9 and DS-20–DS-21 as part of Deliverable 3.1; then proceed with DS-10 and onward for subsequent deliverables.
