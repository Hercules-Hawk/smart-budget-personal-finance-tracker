package ca.yorku.smartbudget.service;

import ca.yorku.smartbudget.domain.Category;
import ca.yorku.smartbudget.domain.PeriodRange;
import ca.yorku.smartbudget.domain.Transaction;
import ca.yorku.smartbudget.domain.TransactionFilter;
import ca.yorku.smartbudget.persistence.Storage;
import ca.yorku.smartbudget.util.Validator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TransactionService {
    private final List<Transaction> transactions = new ArrayList<>();
    private final Storage storage;

    public TransactionService(Storage storage) {
        this.storage = storage;
    }

    /** Load transactions from storage on startup. */
    public void loadOnStartup() {
        List<Transaction> loaded = storage.loadTransactions();
        transactions.clear();
        transactions.addAll(loaded);
    }

    private void persist() {
        storage.saveTransactions(getAll());
    }

    public List<Transaction> getAll() {
        List<Transaction> copy = new ArrayList<>();
        for (Transaction t : transactions) {
            copy.add(t);
        }
        return copy;
    }

    public Transaction add(Transaction tx) {
        Validator.validateTransaction(tx);
        Transaction toAdd = new Transaction(
                tx.getType(),
                tx.getAmount(),
                tx.getDate(),
                tx.getCategory(),
                tx.getNote() != null ? tx.getNote() : ""
        );
        transactions.add(toAdd);
        persist();
        return toAdd;
    }

    public void update(Transaction tx) {
        Validator.validateTransaction(tx);
        if (tx.getId() == null) return;
        delete(tx.getId());
        transactions.add(tx);
        persist();
    }

    public void delete(UUID id) {
        // Loop backwards so removing an element doesn't change indices of items we haven't checked
        for (int i = transactions.size() - 1; i >= 0; i--) {
            if (transactions.get(i).getId().equals(id)) {
                transactions.remove(i);
                persist();
                break;
            }
        }
    }

    public Transaction getById(UUID id) {
        for (Transaction t : transactions) {
            if (t.getId().equals(id)) {
                return t;
            }
        }
        return null;
    }

    public List<Transaction> filter(TransactionFilter criteria) {
        if (criteria == null || criteria.isEmpty()) {
            return getAll();
        }
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions) {
            // Skip if any criterion doesn't match (null criterion means "no filter" for that field)
            if (criteria.getCategory() != null && t.getCategory() != criteria.getCategory()) continue;
            if (criteria.getType() != null && t.getType() != criteria.getType()) continue;
            if (criteria.getStartDate() != null && t.getDate().isBefore(criteria.getStartDate())) continue;
            if (criteria.getEndDate() != null && t.getDate().isAfter(criteria.getEndDate())) continue;
            if (criteria.getKeyword() != null) {
                if (t.getNote() == null) continue;
                if (!t.getNote().toLowerCase().contains(criteria.getKeyword().toLowerCase())) continue;
            }
            result.add(t);
        }
        return result;
    }

    /** Transactions in the given period (Diagram C: getByRange). */
    public List<Transaction> getByRange(PeriodRange range) {
        if (range == null) return new ArrayList<>();
        return getTransactionsInRange(range.getStart(), range.getEnd());
    }

    /** Sum of expenses for the given category in the given month (Diagram B: getExpensesForCategoryAndMonth). */
    public BigDecimal getExpensesForCategoryAndMonth(Category category, YearMonth month) {
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if (!t.isExpense()) continue;
            if (t.getCategory() != category) continue;
            if (t.getDate() == null) continue;
            YearMonth txMonth = YearMonth.from(t.getDate());
            if (!txMonth.equals(month)) continue;
            total = total.add(t.getAmount());
        }
        return total;
    }

    /** Transactions within the given date range (inclusive). */
    public List<Transaction> getTransactionsInRange(LocalDate from, LocalDate to) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getDate() == null) continue;
            if (from != null && t.getDate().isBefore(from)) continue;
            if (to != null && t.getDate().isAfter(to)) continue;
            result.add(t);
        }
        return result;
    }

    /** Total income and total expenses in the given date range. Returns { "income" -> total, "expense" -> total }. */
    public Map<String, BigDecimal> getTotalsInRange(LocalDate from, LocalDate to) {
        List<Transaction> list = getTransactionsInRange(from, to);
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        for (Transaction t : list) {
            if (t.isIncome()) income = income.add(t.getAmount());
            else expense = expense.add(t.getAmount());
        }
        Map<String, BigDecimal> out = new LinkedHashMap<>();
        out.put("income", income);
        out.put("expense", expense);
        return out;
    }

    /** Expense total per category in the given date range. Categories with zero are omitted. */
    public Map<Category, BigDecimal> getExpenseTotalsByCategoryInRange(LocalDate from, LocalDate to) {
        List<Transaction> list = getTransactionsInRange(from, to);
        Map<Category, BigDecimal> out = new LinkedHashMap<>();
        for (Transaction t : list) {
            if (!t.isExpense() || t.getCategory() == null) continue;
            BigDecimal amt = t.getAmount();
            if (out.containsKey(t.getCategory())) {
                out.put(t.getCategory(), out.get(t.getCategory()).add(amt));
            } else {
                out.put(t.getCategory(), amt);
            }
        }
        return out;
    }
}
