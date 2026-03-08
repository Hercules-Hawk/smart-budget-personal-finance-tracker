package ca.yorku.smartbudget.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.YearMonth;

public class Budget {
    private final Category category;
    private final YearMonth month;
    private final BigDecimal limit;

    @JsonCreator
    public Budget(@JsonProperty("category") Category category,
                  @JsonProperty("month") YearMonth month,
                  @JsonProperty("limit") BigDecimal limit) {
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
