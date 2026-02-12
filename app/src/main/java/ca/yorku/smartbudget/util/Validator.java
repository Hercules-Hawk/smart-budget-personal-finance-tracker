package ca.yorku.smartbudget.util;

import ca.yorku.smartbudget.domain.Budget;
import ca.yorku.smartbudget.domain.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class Validator {
    private Validator() {
        // utility class
    }

    // Main entry point used by the service layer
    public static void validateTransaction(Transaction tx) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (tx == null) {
            errors.put("transaction", "Transaction is required.");
            throw new ValidationException(errors);
        }

        // type
        if (tx.getType() == null) {
            errors.put("type", "Transaction type is required.");
        }

        // amount
        BigDecimal amount = tx.getAmount();
        if (amount == null) {
            errors.put("amount", "Amount is required.");
        } else if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.put("amount", "Amount must be greater than 0.");
        }

        // date
        LocalDate date = tx.getDate();
        if (date == null) {
            errors.put("date", "Date is required.");
        }

        // category
        if (tx.getCategory() == null) {
            errors.put("category", "Category is required.");
        }

        // note is optional (no validation needed)
        // If you want: max length guard (safe and simple)
        String note = tx.getNote();
        if (note != null && note.length() > 200) {
            errors.put("note", "Note must be 200 characters or less.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    public static void validateBudget(Budget budget) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (budget == null) {
            errors.put("budget", "Budget is required.");
            throw new ValidationException(errors);
        }
        if (budget.getCategory() == null) {
            errors.put("category", "Category is required.");
        }
        if (budget.getMonth() == null) {
            errors.put("month", "Month is required.");
        }
        if (budget.getLimit() == null) {
            errors.put("limit", "Limit is required.");
        } else if (budget.getLimit().compareTo(BigDecimal.ZERO) <= 0) {
            errors.put("limit", "Limit must be greater than 0.");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    // Helper methods
    public static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }

    public static void requirePositive(BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0.");
        }
    }

    public static void requireDate(LocalDate date, String fieldName) {
        if (date == null) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    /** Parses a decimal string (e.g. from amount/limit fields). Returns null if invalid. */
    public static BigDecimal parseDecimal(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return new BigDecimal(s.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
