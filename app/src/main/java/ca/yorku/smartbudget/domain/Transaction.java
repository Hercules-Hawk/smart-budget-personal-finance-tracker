package ca.yorku.smartbudget.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class Transaction {
    private UUID id;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDate date;
    private Category category;
    private String note;

    // Needed for JSON deserialization (Jackson)
    public Transaction() {
    }

    // Full constructor: the caller provides everything
    public Transaction(UUID id,
                       TransactionType type,
                       BigDecimal amount,
                       LocalDate date,
                       Category category,
                       String note) {
        this.id = (id == null) ? UUID.randomUUID() : id;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.note = note;
    }

    // Convenience constructor: auto-generate id
    public Transaction(TransactionType type,
                       BigDecimal amount,
                       LocalDate date,
                       Category category,
                       String note) {
        this(UUID.randomUUID(), type, amount, date, category, note);
    }

    public UUID getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public Category getCategory() {
        return category;
    }

    public String getNote() {
        return note;
    }

    public boolean isIncome() {
        return type == TransactionType.INCOME;
    }

    public boolean isExpense() {
        return type == TransactionType.EXPENSE;
    }
}
