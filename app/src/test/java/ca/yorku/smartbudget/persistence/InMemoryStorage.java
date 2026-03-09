package ca.yorku.smartbudget.persistence;

import ca.yorku.smartbudget.domain.Budget;
import ca.yorku.smartbudget.domain.Transaction;

import java.util.ArrayList;
import java.util.List;

/** In-memory Storage for unit tests. Does not persist to disk. */
public class InMemoryStorage implements Storage {

    private List<Transaction> transactions = new ArrayList<>();
    private List<Budget> budgets = new ArrayList<>();

    @Override
    public List<Transaction> loadTransactions() {
        return new ArrayList<>(transactions);
    }

    @Override
    public void saveTransactions(List<Transaction> txs) {
        this.transactions = new ArrayList<>(txs);
    }

    @Override
    public List<Budget> loadBudgets() {
        return new ArrayList<>(budgets);
    }

    @Override
    public void saveBudgets(List<Budget> b) {
        this.budgets = new ArrayList<>(b);
    }
}
