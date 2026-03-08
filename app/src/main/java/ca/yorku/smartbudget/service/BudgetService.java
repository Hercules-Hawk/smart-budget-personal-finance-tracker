package ca.yorku.smartbudget.service;

import ca.yorku.smartbudget.domain.Budget;
import ca.yorku.smartbudget.domain.Category;
import ca.yorku.smartbudget.persistence.Storage;
import ca.yorku.smartbudget.util.Validator;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class BudgetService {
    private final List<Budget> budgets = new ArrayList<>();
    private final TransactionService transactionService;
    private final Storage storage;

    public BudgetService(TransactionService transactionService, Storage storage) {
        this.transactionService = transactionService;
        this.storage = storage;
    }

    /** Load budgets from storage on startup. */
    public void loadOnStartup() {
        List<Budget> loaded = storage.loadBudgets();
        budgets.clear();
        budgets.addAll(loaded);
    }

    private void persist() {
        storage.saveBudgets(getAllBudgets());
    }

    public List<Budget> getAllBudgets() {
        List<Budget> copy = new ArrayList<>();
        for (Budget b : budgets) {
            copy.add(b);
        }
        return copy;
    }

    public List<Budget> getBudgetsForMonth(YearMonth month) {
        List<Budget> result = new ArrayList<>();
        for (Budget b : budgets) {
            if (b.getMonth().equals(month)) {
                result.add(b);
            }
        }
        return result;
    }

    public Budget getBudget(Category category, YearMonth month) {
        for (Budget b : budgets) {
            if (b.getCategory() == category && b.getMonth().equals(month)) {
                return b;
            }
        }
        return null;
    }

    public void upsertBudget(Budget budget) {
        Validator.validateBudget(budget);
        deleteBudget(budget.getCategory(), budget.getMonth());
        budgets.add(budget);
        persist();
    }

    public void deleteBudget(Category category, YearMonth month) {
        // Backward loop so deleteBudget doesn't affect indices of remaining elements
        for (int i = budgets.size() - 1; i >= 0; i--) {
            Budget b = budgets.get(i);
            if (b.getCategory() == category && b.getMonth().equals(month)) {
                budgets.remove(i);
                persist();
                break;
            }
        }
    }

    /** Diagram B: spending for category/month (delegates to TransactionService). */
    public BigDecimal getExpensesForCategoryAndMonth(Category category, YearMonth month) {
        return transactionService.getExpensesForCategoryAndMonth(category, month);
    }
}
