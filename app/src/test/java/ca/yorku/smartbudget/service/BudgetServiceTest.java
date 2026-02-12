package ca.yorku.smartbudget.service;

import ca.yorku.smartbudget.domain.Budget;
import ca.yorku.smartbudget.domain.Category;
import ca.yorku.smartbudget.domain.Transaction;
import ca.yorku.smartbudget.domain.TransactionType;
import ca.yorku.smartbudget.util.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BudgetServiceTest {

    private TransactionService transactionService;
    private BudgetService budgetService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService();
        budgetService = new BudgetService(transactionService);
    }

    @Test
    void getAllBudgets_returnsEmpty_whenNone() {
        assertTrue(budgetService.getAllBudgets().isEmpty());
    }

    @Test
    void upsertBudget_addsBudget() {
        Budget b = new Budget(Category.FOOD_AND_DINING, YearMonth.of(2025, 1), new BigDecimal("500.00"));
        budgetService.upsertBudget(b);
        assertEquals(1, budgetService.getAllBudgets().size());
        assertEquals(b.getCategory(), budgetService.getAllBudgets().get(0).getCategory());
        assertEquals(new BigDecimal("500.00"), budgetService.getAllBudgets().get(0).getLimit());
    }

    @Test
    void upsertBudget_invalid_throwsValidationException() {
        Budget b = new Budget(null, YearMonth.of(2025, 1), new BigDecimal("100.00"));
        assertThrows(ValidationException.class, () -> budgetService.upsertBudget(b));
    }

    @Test
    void upsertBudget_replacesExistingForSameCategoryAndMonth() {
        YearMonth jan = YearMonth.of(2025, 1);
        budgetService.upsertBudget(new Budget(Category.FOOD_AND_DINING, jan, new BigDecimal("300.00")));
        budgetService.upsertBudget(new Budget(Category.FOOD_AND_DINING, jan, new BigDecimal("400.00")));
        List<Budget> all = budgetService.getAllBudgets();
        assertEquals(1, all.size());
        assertEquals(new BigDecimal("400.00"), all.get(0).getLimit());
    }

    @Test
    void getBudgetsForMonth_returnsOnlyForMonth() {
        YearMonth jan = YearMonth.of(2025, 1);
        YearMonth feb = YearMonth.of(2025, 2);
        budgetService.upsertBudget(new Budget(Category.FOOD_AND_DINING, jan, new BigDecimal("300.00")));
        budgetService.upsertBudget(new Budget(Category.SHOPPING, jan, new BigDecimal("200.00")));
        budgetService.upsertBudget(new Budget(Category.FOOD_AND_DINING, feb, new BigDecimal("350.00")));
        List<Budget> janBudgets = budgetService.getBudgetsForMonth(jan);
        assertEquals(2, janBudgets.size());
        List<Budget> febBudgets = budgetService.getBudgetsForMonth(feb);
        assertEquals(1, febBudgets.size());
    }

    @Test
    void getBudget_returnsBudget_whenExists() {
        YearMonth jan = YearMonth.of(2025, 1);
        budgetService.upsertBudget(new Budget(Category.FOOD_AND_DINING, jan, new BigDecimal("500.00")));
        Budget found = budgetService.getBudget(Category.FOOD_AND_DINING, jan);
        assertNotNull(found);
        assertEquals(new BigDecimal("500.00"), found.getLimit());
    }

    @Test
    void getBudget_returnsNull_whenNotExists() {
        assertNull(budgetService.getBudget(Category.FOOD_AND_DINING, YearMonth.of(2025, 1)));
    }

    @Test
    void deleteBudget_removesBudget() {
        YearMonth jan = YearMonth.of(2025, 1);
        budgetService.upsertBudget(new Budget(Category.FOOD_AND_DINING, jan, new BigDecimal("500.00")));
        budgetService.deleteBudget(Category.FOOD_AND_DINING, jan);
        assertTrue(budgetService.getAllBudgets().isEmpty());
        assertNull(budgetService.getBudget(Category.FOOD_AND_DINING, jan));
    }

    @Test
    void getExpensesForCategoryAndMonth_delegatesToTransactionService() {
        YearMonth jan = YearMonth.of(2025, 1);
        transactionService.add(new Transaction(
                TransactionType.EXPENSE,
                new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 15),
                Category.FOOD_AND_DINING,
                "groceries"
        ));
        BigDecimal spent = budgetService.getExpensesForCategoryAndMonth(Category.FOOD_AND_DINING, jan);
        assertEquals(new BigDecimal("100.00"), spent);
    }
}
