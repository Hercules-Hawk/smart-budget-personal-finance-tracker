package ca.yorku.smartbudget.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;

/**
 * Alert when budget is reached or exceeded. Matches Diagram B (AlertService).
 */
public class OverspendingAlert {
    private final Category category;
    private final YearMonth month;
    private final BigDecimal limit;
    private final BigDecimal spent;
    private final BigDecimal overBy;
    private final AlertStatus status;

    public OverspendingAlert(Category category, YearMonth month, BigDecimal limit, BigDecimal spent, BigDecimal overBy, AlertStatus status) {
        this.category = category;
        this.month = month;
        this.limit = limit;
        this.spent = spent;
        this.overBy = overBy;
        this.status = status;
    }

    public Category getCategory() {
        return category;
    }

    public YearMonth getMonth() {
        return month;
    }

    public BigDecimal getLimit() {
        return limit;
    }

    public BigDecimal getSpent() {
        return spent;
    }

    public BigDecimal getOverBy() {
        return overBy;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public boolean isTriggered() {
        return status == AlertStatus.REACHED || status == AlertStatus.EXCEEDED;
    }

    /** Single place for alert message text (used by both Transactions and Budgets controllers). */
    public String getDisplayMessage(boolean includeMonth) {
        String part = category.getDisplayName() + (includeMonth ? " for " + month : "") + ": spent $"
                + (spent != null ? spent.setScale(2, RoundingMode.HALF_UP).toPlainString() : "0.00")
                + " (limit $" + (limit != null ? limit.setScale(2, RoundingMode.HALF_UP).toPlainString() : "0.00") + ")";
        if (overBy != null && overBy.compareTo(BigDecimal.ZERO) > 0) {
            part += ". Over by $" + overBy.setScale(2, RoundingMode.HALF_UP).toPlainString();
        }
        return part;
    }
}
