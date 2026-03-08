package ca.yorku.smartbudget.persistence;

import ca.yorku.smartbudget.domain.Budget;
import ca.yorku.smartbudget.domain.Transaction;

import java.util.List;

/**
 * Storage interface for loading and saving transactions and budgets.
 * Implementations persist data (e.g. JSON files) so data survives restarts.
 */
public interface Storage {

    List<Transaction> loadTransactions();

    void saveTransactions(List<Transaction> transactions);

    List<Budget> loadBudgets();

    void saveBudgets(List<Budget> budgets);
}
