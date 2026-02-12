package ca.yorku.smartbudget.domain;

import java.math.BigDecimal;

/**
 * Summary totals for a report period. Matches Diagram C.
 */
public class SummaryReport {
    private final BigDecimal totalIncome;
    private final BigDecimal totalExpense;
    private final BigDecimal balance;

    public SummaryReport(BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal balance) {
        this.totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        this.totalExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public boolean isEmpty() {
        return totalIncome.compareTo(BigDecimal.ZERO) == 0 && totalExpense.compareTo(BigDecimal.ZERO) == 0;
    }
}
