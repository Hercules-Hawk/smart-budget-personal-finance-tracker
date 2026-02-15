package ca.yorku.smartbudget.service;

import ca.yorku.smartbudget.domain.Category;
import ca.yorku.smartbudget.domain.CategoryTotal;
import ca.yorku.smartbudget.domain.PeriodRange;
import ca.yorku.smartbudget.domain.ReportResult;
import ca.yorku.smartbudget.domain.SummaryReport;
import ca.yorku.smartbudget.domain.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates summary reports and spending-by-category. Matches Diagram C.
 * Uses a single pass over transactions in range (getByRange) to build summary and breakdown.
 */
public class ReportService {
    private final TransactionService transactionService;

    public ReportService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public ReportResult generateReport(PeriodRange range) {
        if (range == null || !range.isValid()) {
            return new ReportResult(range, new SummaryReport(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                    new ArrayList<>(), false, "Invalid date range.");
        }
        List<Transaction> inRange = transactionService.getByRange(range);
        SummaryReport summary = buildSummary(inRange);
        List<CategoryTotal> breakdown = buildBreakdown(inRange);
        boolean hasData = inRange != null && !inRange.isEmpty();
        String message = hasData ? null : "No records found";
        return new ReportResult(range, summary, breakdown, hasData, message);
    }

    public SummaryReport getSummary(PeriodRange range) {
        if (range == null) return new SummaryReport(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        return buildSummary(transactionService.getByRange(range));
    }

    public List<CategoryTotal> getSpendingByCategory(PeriodRange range) {
        if (range == null) return new ArrayList<>();
        return buildBreakdown(transactionService.getByRange(range));
    }

    private SummaryReport buildSummary(List<Transaction> transactions) {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if (t.isIncome()) income = income.add(t.getAmount());
            else expense = expense.add(t.getAmount());
        }
        return new SummaryReport(income, expense, income.subtract(expense));
    }

    private List<CategoryTotal> buildBreakdown(List<Transaction> transactions) {
        Map<Category, BigDecimal> byCategory = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            if (!t.isExpense() || t.getCategory() == null) continue;
            BigDecimal amt = t.getAmount();
            if (byCategory.containsKey(t.getCategory())) {
                byCategory.put(t.getCategory(), byCategory.get(t.getCategory()).add(amt));
            } else {
                byCategory.put(t.getCategory(), amt);
            }
        }
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (BigDecimal v : byCategory.values()) {
            totalExpense = totalExpense.add(v);
        }
        List<CategoryTotal> result = new ArrayList<>();
        for (Map.Entry<Category, BigDecimal> e : byCategory.entrySet()) {
            BigDecimal tot = e.getValue();
            if (tot.compareTo(BigDecimal.ZERO) <= 0) continue;
            double pct = totalExpense.compareTo(BigDecimal.ZERO) == 0 ? 0.0
                    : tot.divide(totalExpense, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
            result.add(new CategoryTotal(e.getKey(), tot, pct));
        }
        return result;
    }
}
