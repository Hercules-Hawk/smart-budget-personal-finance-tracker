package ca.yorku.smartbudget.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TransactionFilterTest {

    @Test
    void isEmpty_returnsTrue_whenAllFieldsNull() {
        TransactionFilter f = new TransactionFilter(null, null, null, null, null);
        assertTrue(f.isEmpty());
    }

    @Test
    void isEmpty_returnsTrue_whenKeywordBlank() {
        TransactionFilter f = new TransactionFilter(null, null, null, null, "   ");
        assertTrue(f.isEmpty());
    }

    @Test
    void isEmpty_returnsFalse_whenCategorySet() {
        TransactionFilter f = new TransactionFilter(Category.FOOD_AND_DINING, null, null, null, null);
        assertFalse(f.isEmpty());
    }

    @Test
    void isEmpty_returnsFalse_whenTypeSet() {
        TransactionFilter f = new TransactionFilter(null, TransactionType.EXPENSE, null, null, null);
        assertFalse(f.isEmpty());
    }

    @Test
    void isEmpty_returnsFalse_whenKeywordNonBlank() {
        TransactionFilter f = new TransactionFilter(null, null, null, null, "coffee");
        assertFalse(f.isEmpty());
    }

    @Test
    void getters_returnConstructorValues() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);
        TransactionFilter f = new TransactionFilter(
                Category.SHOPPING,
                TransactionType.EXPENSE,
                start,
                end,
                "grocery"
        );
        assertEquals(Category.SHOPPING, f.getCategory());
        assertEquals(TransactionType.EXPENSE, f.getType());
        assertEquals(start, f.getStartDate());
        assertEquals(end, f.getEndDate());
        assertEquals("grocery", f.getKeyword());
    }

    @Test
    void keyword_trimmedWhenProvidedWithSpaces() {
        TransactionFilter f = new TransactionFilter(null, null, null, null, "  word  ");
        assertEquals("word", f.getKeyword());
        assertFalse(f.isEmpty());
    }
}
