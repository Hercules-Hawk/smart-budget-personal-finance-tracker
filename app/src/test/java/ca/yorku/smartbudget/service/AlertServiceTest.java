package ca.yorku.smartbudget.service;

import ca.yorku.smartbudget.domain.AlertStatus;
import ca.yorku.smartbudget.domain.Budget;
import ca.yorku.smartbudget.domain.Category;
import ca.yorku.smartbudget.domain.OverspendingAlert;
import ca.yorku.smartbudget.domain.Transaction;
import ca.yorku.smartbudget.domain.TransactionType;
import ca.yorku.smartbudget.persistence.InMemoryStorage;
import ca.yorku.smartbudget.persistence.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlertServiceTest {

    private Storage storage;
    private TransactionService transactionService;
    private BudgetService budgetService;
    private AlertService alertService;

    @BeforeEach
    void setUp() {
        storage = new InMemoryStorage();
        transactionService = new TransactionService(storage);
        budgetService = new BudgetService(transactionService, storage);
        alertService = new AlertService(budgetService, transactionService);
    }

    private Transaction addExpense(LocalDate date, Category category, String amount) {
        return transactionService.add(new Transaction(
                TransactionType.EXPENSE,
                new BigDecimal(amount),
                date,
                category,
                "expense"
        ));
    }

    @Test
    void checkBudgetStatus_returnsOk_whenSpentBelowLimit() {
        YearMonth month = YearMonth.of(2025, 1);
        budgetService.upsertBudget(new Budget(Category.FOOD_AND_DINING, month, new BigDecimal("100.00")));
        addExpense(LocalDate.of(2025, 1, 10), Category.FOOD_AND_DINING, "40.00");

        OverspendingAlert alert = alertService.checkBudgetStatus(Category.FOOD_AND_DINING, month);

        assertNotNull(alert);
        assertEquals(AlertStatus.OK, alert.getStatus());
        assertEquals(new BigDecimal("40.00"), alert.getSpent());
        assertEquals(BigDecimal.ZERO, alert.getOverBy());
        assertFalse(alert.isTriggered());
    }

    @Test
    void checkBudgetStatus_returnsReached_whenSpentEqualsLimit_boundaryCase() {
        YearMonth month = YearMonth.of(2025, 1);
        budgetService.upsertBudget(new Budget(Category.SHOPPING, month, new BigDecimal("75.00")));
        addExpense(LocalDate.of(2025, 1, 8), Category.SHOPPING, "75.00");

        OverspendingAlert alert = alertService.checkBudgetStatus(Category.SHOPPING, month);

        assertNotNull(alert);
        assertEquals(AlertStatus.REACHED, alert.getStatus());
        assertEquals(BigDecimal.ZERO, alert.getOverBy());
        assertTrue(alert.isTriggered());
    }

    @Test
    void checkBudgetStatus_returnsExceeded_whenSpentAboveLimit_boundaryCase() {
        YearMonth month = YearMonth.of(2025, 1);
        budgetService.upsertBudget(new Budget(Category.TRANSPORTATION, month, new BigDecimal("100.00")));
        addExpense(LocalDate.of(2025, 1, 18), Category.TRANSPORTATION, "100.01");

        OverspendingAlert alert = alertService.checkBudgetStatus(Category.TRANSPORTATION, month);

        assertNotNull(alert);
        assertEquals(AlertStatus.EXCEEDED, alert.getStatus());
        assertEquals(new BigDecimal("0.01"), alert.getOverBy());
        assertTrue(alert.isTriggered());
    }

    @Test
    void checkOverspendingFor_returnsNull_whenTransactionIsNotEligibleForCheck() {
        Transaction income = new Transaction(
                TransactionType.INCOME,
                new BigDecimal("500.00"),
                LocalDate.of(2025, 1, 2),
                Category.SALARY,
                "salary"
        );
        Transaction missingCategory = new Transaction(
                TransactionType.EXPENSE,
                new BigDecimal("10.00"),
                LocalDate.of(2025, 1, 2),
                null,
                "invalid"
        );
        Transaction missingDate = new Transaction(
                TransactionType.EXPENSE,
                new BigDecimal("10.00"),
                null,
                Category.OTHER,
                "invalid"
        );

        assertNull(alertService.checkOverspendingFor(null));
        assertNull(alertService.checkOverspendingFor(income));
        assertNull(alertService.checkOverspendingFor(missingCategory));
        assertNull(alertService.checkOverspendingFor(missingDate));
    }

    @Test
    void checkOverspendingFor_returnsReached_forExpenseFlow_uc6() {
        YearMonth month = YearMonth.of(2025, 3);
        budgetService.upsertBudget(new Budget(Category.FOOD_AND_DINING, month, new BigDecimal("50.00")));
        Transaction added = addExpense(LocalDate.of(2025, 3, 11), Category.FOOD_AND_DINING, "50.00");

        OverspendingAlert alert = alertService.checkOverspendingFor(added);

        assertNotNull(alert);
        assertEquals(AlertStatus.REACHED, alert.getStatus());
        assertTrue(alert.isTriggered());
    }

    @Test
    void checkBudgetStatus_returnsExceeded_forBudgetFlow_uc6() {
        YearMonth month = YearMonth.of(2025, 4);
        addExpense(LocalDate.of(2025, 4, 3), Category.ENTERTAINMENT, "120.00");
        budgetService.upsertBudget(new Budget(Category.ENTERTAINMENT, month, new BigDecimal("100.00")));

        OverspendingAlert alert = alertService.checkBudgetStatus(Category.ENTERTAINMENT, month);

        assertNotNull(alert);
        assertEquals(AlertStatus.EXCEEDED, alert.getStatus());
        assertEquals(new BigDecimal("20.00"), alert.getOverBy());
        assertTrue(alert.isTriggered());
    }

    @Test
    void checkBudgetStatus_returnsNull_whenBudgetIsMissingOrLimitIsNull() {
        YearMonth month = YearMonth.of(2025, 5);

        assertNull(alertService.checkBudgetStatus(Category.HEALTHCARE, month));

        storage.saveBudgets(List.of(new Budget(Category.HEALTHCARE, month, null)));
        budgetService.loadOnStartup();

        assertNull(alertService.checkBudgetStatus(Category.HEALTHCARE, month));
    }

    @Test
    void checkBudgetStatus_returnsOk_whenLimitIsZero_defensiveCase() {
        YearMonth month = YearMonth.of(2025, 6);
        addExpense(LocalDate.of(2025, 6, 9), Category.OTHER, "20.00");
        storage.saveBudgets(List.of(new Budget(Category.OTHER, month, BigDecimal.ZERO)));
        budgetService.loadOnStartup();

        OverspendingAlert alert = alertService.checkBudgetStatus(Category.OTHER, month);

        assertNotNull(alert);
        assertEquals(AlertStatus.OK, alert.getStatus());
        assertEquals(BigDecimal.ZERO, alert.getOverBy());
        assertFalse(alert.isTriggered());
    }

    @Test
    void checkAllBudgets_returnsOnlyTriggeredAlerts() {
        YearMonth month = YearMonth.of(2025, 7);
        budgetService.upsertBudget(new Budget(Category.FOOD_AND_DINING, month, new BigDecimal("100.00")));
        budgetService.upsertBudget(new Budget(Category.SHOPPING, month, new BigDecimal("50.00")));
        budgetService.upsertBudget(new Budget(Category.ENTERTAINMENT, month, new BigDecimal("20.00")));

        addExpense(LocalDate.of(2025, 7, 2), Category.FOOD_AND_DINING, "60.00");
        addExpense(LocalDate.of(2025, 7, 10), Category.SHOPPING, "50.00");
        addExpense(LocalDate.of(2025, 7, 16), Category.ENTERTAINMENT, "30.00");

        List<OverspendingAlert> alerts = alertService.checkAllBudgets(month);

        assertEquals(2, alerts.size());
        assertTrue(alerts.stream().allMatch(OverspendingAlert::isTriggered));
        assertTrue(alerts.stream().anyMatch(a -> a.getCategory() == Category.SHOPPING && a.getStatus() == AlertStatus.REACHED));
        assertTrue(alerts.stream().anyMatch(a -> a.getCategory() == Category.ENTERTAINMENT && a.getStatus() == AlertStatus.EXCEEDED));
    }
}
