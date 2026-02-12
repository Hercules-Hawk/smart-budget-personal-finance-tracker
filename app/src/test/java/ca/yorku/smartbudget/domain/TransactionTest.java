package ca.yorku.smartbudget.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void constructor_autoGeneratesId_whenNullIdPassed() {
        Transaction tx = new Transaction(
                null,
                TransactionType.EXPENSE,
                new BigDecimal("50.00"),
                LocalDate.now(),
                Category.FOOD_AND_DINING,
                "Test"
        );
        assertNotNull(tx.getId());
    }

    @Test
    void constructor_usesProvidedId_whenNonNull() {
        UUID id = UUID.randomUUID();
        Transaction tx = new Transaction(
                id,
                TransactionType.INCOME,
                new BigDecimal("100.00"),
                LocalDate.now(),
                Category.SALARY,
                "Pay"
        );
        assertEquals(id, tx.getId());
    }

    @Test
    void convenienceConstructor_createsValidTransaction() {
        Transaction tx = new Transaction(
                TransactionType.EXPENSE,
                new BigDecimal("25.50"),
                LocalDate.of(2025, 2, 1),
                Category.TRANSPORTATION,
                "Bus fare"
        );
        assertNotNull(tx.getId());
        assertEquals(TransactionType.EXPENSE, tx.getType());
        assertEquals(0, new BigDecimal("25.50").compareTo(tx.getAmount()));
        assertEquals(LocalDate.of(2025, 2, 1), tx.getDate());
        assertEquals(Category.TRANSPORTATION, tx.getCategory());
        assertEquals("Bus fare", tx.getNote());
    }

    @Test
    void isIncome_returnsTrue_forIncomeType() {
        Transaction tx = new Transaction(
                TransactionType.INCOME,
                new BigDecimal("200.00"),
                LocalDate.now(),
                Category.SALARY,
                null
        );
        assertTrue(tx.isIncome());
        assertFalse(tx.isExpense());
    }

    @Test
    void isExpense_returnsTrue_forExpenseType() {
        Transaction tx = new Transaction(
                TransactionType.EXPENSE,
                new BigDecimal("15.00"),
                LocalDate.now(),
                Category.ENTERTAINMENT,
                "Movie"
        );
        assertTrue(tx.isExpense());
        assertFalse(tx.isIncome());
    }
}
