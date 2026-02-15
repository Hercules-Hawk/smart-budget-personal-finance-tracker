package ca.yorku.smartbudget.domain;

import java.time.LocalDate;

public class TransactionFilter {
    private final Category category;
    private final TransactionType type;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String keyword;

    public TransactionFilter(Category category,
                             TransactionType type,
                             LocalDate startDate,
                             LocalDate endDate,
                             String keyword) {

        this.category = category;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;

        // Normalize keyword to match UI expectations
        if (keyword == null || keyword.trim().isEmpty()) {
            this.keyword = null;
        } else {
            this.keyword = keyword.trim();
        }
    }

    public Category getCategory() {
        return category;
    }

    public TransactionType getType() {
        return type;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getKeyword() {
        return keyword;
    }

    public boolean isEmpty() {
        return category == null &&
               type == null &&
               startDate == null &&
               endDate == null &&
               keyword == null;
    }
}
