package ca.yorku.smartbudget.service;

import ca.yorku.smartbudget.domain.AlertStatus;
import ca.yorku.smartbudget.domain.Budget;
import ca.yorku.smartbudget.domain.Category;
import ca.yorku.smartbudget.domain.OverspendingAlert;
import ca.yorku.smartbudget.domain.Transaction;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks budget status and overspending. Matches Diagram A/B (AlertService).
 */
public class AlertService {
    private final BudgetService budgetService;
    private final TransactionService transactionService;

    public AlertService(BudgetService budgetService, TransactionService transactionService) {
        this.budgetService = budgetService;
        this.transactionService = transactionService;
    }

    /**
     * After adding or editing an expense, check if that category/month is over budget.
     * Returns null if no alert (income, or no budget, or under limit).
     */
    public OverspendingAlert checkOverspendingFor(Transaction tx) {
        if (tx == null || !tx.isExpense() || tx.getCategory() == null || tx.getDate() == null) {
            return null;
        }
        YearMonth month = YearMonth.from(tx.getDate());
        return checkBudgetStatus(tx.getCategory(), month);
    }

    /**
     * Check budget status for one category in one month. Returns null if no budget set.
     */
    public OverspendingAlert checkBudgetStatus(Category category, YearMonth month) {
        Budget budget = budgetService.getBudget(category, month);
        if (budget == null || budget.getLimit() == null) {
            return null;
        }
        BigDecimal spent = transactionService.getExpensesForCategoryAndMonth(category, month);
        BigDecimal limit = budget.getLimit();
        if (limit.compareTo(BigDecimal.ZERO) <= 0) {
            return new OverspendingAlert(category, month, limit, spent, BigDecimal.ZERO, AlertStatus.OK);
        }
        int cmp = spent.compareTo(limit);
        if (cmp < 0) {
            return new OverspendingAlert(category, month, limit, spent, BigDecimal.ZERO, AlertStatus.OK);
        }
        if (cmp == 0) {
            return new OverspendingAlert(category, month, limit, spent, BigDecimal.ZERO, AlertStatus.REACHED);
        }
        BigDecimal overBy = spent.subtract(limit);
        return new OverspendingAlert(category, month, limit, spent, overBy, AlertStatus.EXCEEDED);
    }

    /**
     * Check all budgets for a month and return any that are reached or exceeded.
     */
    public List<OverspendingAlert> checkAllBudgets(YearMonth month) {
        List<OverspendingAlert> result = new ArrayList<>();
        List<Budget> budgets = budgetService.getBudgetsForMonth(month);
        for (Budget b : budgets) {
            OverspendingAlert alert = checkBudgetStatus(b.getCategory(), month);
            if (alert != null && alert.isTriggered()) {
                result.add(alert);
            }
        }
        return result;
    }
}
