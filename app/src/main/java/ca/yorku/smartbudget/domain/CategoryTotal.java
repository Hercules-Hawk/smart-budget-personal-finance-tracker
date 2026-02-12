package ca.yorku.smartbudget.domain;

import java.math.BigDecimal;

/**
 * Expense total for one category in a report. Matches Diagram C.
 */
public class CategoryTotal {
    private final Category category;
    private final BigDecimal total;
    private final double percentOfTotal;

    public CategoryTotal(Category category, BigDecimal total, double percentOfTotal) {
        this.category = category;
        this.total = total != null ? total : BigDecimal.ZERO;
        this.percentOfTotal = percentOfTotal;
    }

    public Category getCategory() {
        return category;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public double getPercentOfTotal() {
        return percentOfTotal;
    }
}
