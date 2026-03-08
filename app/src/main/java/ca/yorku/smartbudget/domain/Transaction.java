package ca.yorku.smartbudget.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    /** Needed for Jackson to load from file. */
    public Transaction() {
    }

    /** Full constructor: the caller provides everything. */
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

    public void setId(UUID id) {
        this.id = id;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @JsonIgnore
    public boolean isIncome() {
        return type == TransactionType.INCOME;
    }

    @JsonIgnore
    public boolean isExpense() {
        return type == TransactionType.EXPENSE;
    }
}
