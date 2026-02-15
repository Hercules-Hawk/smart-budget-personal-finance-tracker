package ca.yorku.smartbudget.util;

import ca.yorku.smartbudget.domain.Category;
import ca.yorku.smartbudget.domain.Transaction;
import ca.yorku.smartbudget.domain.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ValidatorTest {
    private Transaction validTx() {
        return new Transaction(
                TransactionType.EXPENSE,
                new BigDecimal("100.00"),
                LocalDate.now(),
                Category.FOOD_AND_DINING,
                "Lunch with friends"
        );
    }

    @Test
    public void validateTransaction_validTransaction_doesNotThrow() {
        Transaction tx = validTx();
        assertDoesNotThrow(() -> Validator.validateTransaction(tx));
    }

    @Test
    public void validateTransaction_nullTransaction_throwsValidateException() {
        ValidationException ex = assertThrows(ValidationException.class, () -> Validator.validateTransaction(null));
        assertTrue(ex.getFieldErrors().containsKey("transaction"));
    }

    @Test
    public void validateTransaction_missingType_collectsTypeError() {
        Transaction tx = validTx();
        tx = new Transaction(tx.getId(), null, tx.getAmount(), tx.getDate(), tx.getCategory(), tx.getNote());
        Transaction finalTx = tx;
        ValidationException ex = assertThrows(ValidationException.class, () -> Validator.validateTransaction(finalTx));
        assertEquals("Transaction type is required.", ex.getFieldErrors().get("type"));
    }

    @Test
    public void validateTransaction_missingCategory_collectsCategoryError() {
        Transaction tx = validTx();
        tx = new Transaction(tx.getId(), tx.getType(), tx.getAmount(), tx.getDate(), null, tx.getNote());
        Transaction finalTx = tx;
        ValidationException ex = assertThrows(ValidationException.class, () -> Validator.validateTransaction(finalTx));
        assertEquals("Category is required.", ex.getFieldErrors().get("category"));
    }

    @Test
    public void validateTransaction_missingDate_collectsDateError() {
        Transaction tx = validTx();
        tx = new Transaction(tx.getId(), tx.getType(), tx.getAmount(), null, tx.getCategory(), tx.getNote());
        Transaction finalTx = tx;
        ValidationException ex = assertThrows(ValidationException.class, () -> Validator.validateTransaction(finalTx));
        assertEquals("Date is required.", ex.getFieldErrors().get("date"));
    }

    @Test
    public void validateTransaction_missingAmount_collectsAmountRequiredError() {
        Transaction tx = validTx();
        tx = new Transaction(tx.getId(), tx.getType(), null, tx.getDate(), tx.getCategory(), tx.getNote());
        Transaction finalTx = tx;
        ValidationException ex = assertThrows(ValidationException.class, () -> Validator.validateTransaction(finalTx));
        assertEquals("Amount is required.", ex.getFieldErrors().get("amount"));
    }

    @Test
    void validateTransaction_zeroAmount_collectsAmountPositiveError() {
        Transaction tx = validTx();
        tx = new Transaction(tx.getId(), tx.getType(), BigDecimal.ZERO, tx.getDate(), tx.getCategory(), tx.getNote());

        Transaction finalTx = tx;
        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateTransaction(finalTx));

        assertEquals("Amount must be greater than 0.", ex.getFieldErrors().get("amount"));
    }

    @Test
    void validateTransaction_negativeAmount_collectsAmountPositiveError() {
        Transaction tx = validTx();
        tx = new Transaction(tx.getId(), tx.getType(), new BigDecimal("-1.00"), tx.getDate(), tx.getCategory(), tx.getNote());

        Transaction finalTx = tx;
        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateTransaction(finalTx));

        assertEquals("Amount must be greater than 0.", ex.getFieldErrors().get("amount"));
    }

    @Test
    void validateTransaction_noteNull_isAllowed() {
        Transaction tx = validTx();
        tx = new Transaction(tx.getId(), tx.getType(), tx.getAmount(), tx.getDate(), tx.getCategory(), null);

        Transaction finalTx = tx;
        assertDoesNotThrow(() -> Validator.validateTransaction(finalTx));
    }

    @Test
    void validateTransaction_noteBlank_isAllowed() {
        Transaction tx = validTx();
        tx = new Transaction(tx.getId(), tx.getType(), tx.getAmount(), tx.getDate(), tx.getCategory(), "   ");

        Transaction finalTx = tx;
        assertDoesNotThrow(() -> Validator.validateTransaction(finalTx));
    }


    @Test
    void validateTransaction_multipleMissingFields_collectsAllErrors() {
        Transaction tx = new Transaction(
                UUID.randomUUID(),
                null,               // type missing
                null,               // amount missing
                null,               // date missing
                null,               // category missing
                null
        );

        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateTransaction(tx));

        Map<String, String> errors = ex.getFieldErrors();
        assertTrue(errors.containsKey("type"));
        assertTrue(errors.containsKey("amount"));
        assertTrue(errors.containsKey("date"));
        assertTrue(errors.containsKey("category"));
        assertEquals(4, errors.size()); // expects exactly these 4 keys for this input
    }

    // Helper methods tests
    // -------------------------

    @Test
    void requireNotBlank_null_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Validator.requireNotBlank(null, "note"));
        assertTrue(ex.getMessage().toLowerCase().contains("must not be blank"));
    }

    @Test
    void requireNotBlank_blank_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Validator.requireNotBlank("   ", "note"));
        assertTrue(ex.getMessage().toLowerCase().contains("must not be blank"));
    }

    @Test
    void requireNotBlank_ok_doesNotThrow() {
        assertDoesNotThrow(() -> Validator.requireNotBlank("hello", "note"));
    }

    @Test
    void requirePositive_null_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Validator.requirePositive(null, "amount"));
        assertTrue(ex.getMessage().toLowerCase().contains("required"));
    }

    @Test
    void requirePositive_zero_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Validator.requirePositive(BigDecimal.ZERO, "amount"));
        assertTrue(ex.getMessage().toLowerCase().contains("greater than 0"));
    }

    @Test
    void requirePositive_positive_doesNotThrow() {
        assertDoesNotThrow(() -> Validator.requirePositive(new BigDecimal("0.01"), "amount"));
    }

    @Test
    void requireDate_null_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Validator.requireDate(null, "date"));
        assertTrue(ex.getMessage().toLowerCase().contains("required"));
    }

    @Test
    void requireDate_ok_doesNotThrow() {
        assertDoesNotThrow(() -> Validator.requireDate(LocalDate.now(), "date"));
    }

    @Test
    void transaction_isExpense_and_isIncome_forExpense() {
        Transaction tx = validTx(); // EXPENSE
        assertTrue(tx.isExpense());
        assertFalse(tx.isIncome());
    }

    @Test
    void transaction_isExpense_and_isIncome_forIncome() {
        Transaction tx = new Transaction(
                TransactionType.INCOME,
                new BigDecimal("50.00"),
                LocalDate.now(),
                Category.SALARY,
                "Pay"
        );
        assertTrue(tx.isIncome());
        assertFalse(tx.isExpense());
    }
}
