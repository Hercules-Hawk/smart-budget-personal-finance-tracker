package ca.yorku.smartbudget.domain;

import java.math.BigDecimal;
import java.time.YearMonth;

public class Budget {
    private final Category category;
    private final YearMonth month;
    private final BigDecimal limit;

    public Budget(Category category, YearMonth month, BigDecimal limit) {
        this.category = category;
        this.month = month;
        this.limit = limit;
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
}
