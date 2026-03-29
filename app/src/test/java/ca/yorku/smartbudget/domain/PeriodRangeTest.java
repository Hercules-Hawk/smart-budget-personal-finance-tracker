package ca.yorku.smartbudget.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

class PeriodRangeTest {

    @Test
    void constructorAndGetters_storeProvidedDates() {
        LocalDate start = LocalDate.of(2025, 1, 5);
        LocalDate end = LocalDate.of(2025, 1, 25);

        PeriodRange range = new PeriodRange(start, end);

        assertEquals(start, range.getStart());
        assertEquals(end, range.getEnd());
    }

    @Test
    void isValid_returnsTrue_whenBothDatesAreNull_allTimeCase() {
        PeriodRange range = new PeriodRange(null, null);
        assertTrue(range.isValid());
    }

    @Test
    void isValid_returnsTrue_whenOnlyStartIsSet_openEndedCase() {
        PeriodRange range = new PeriodRange(LocalDate.of(2025, 2, 1), null);
        assertTrue(range.isValid());
    }

    @Test
    void isValid_returnsTrue_whenOnlyEndIsSet_openEndedCase() {
        PeriodRange range = new PeriodRange(null, LocalDate.of(2025, 2, 28));
        assertTrue(range.isValid());
    }

    @Test
    void isValid_returnsTrue_whenStartEqualsEnd_boundaryCase() {
        LocalDate day = LocalDate.of(2025, 3, 14);
        PeriodRange range = new PeriodRange(day, day);
        assertTrue(range.isValid());
    }

    @Test
    void isValid_returnsTrue_whenStartBeforeEnd() {
        PeriodRange range = new PeriodRange(LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 30));
        assertTrue(range.isValid());
    }

    @Test
    void isValid_returnsFalse_whenStartAfterEnd() {
        PeriodRange range = new PeriodRange(LocalDate.of(2025, 4, 30), LocalDate.of(2025, 4, 1));
        assertFalse(range.isValid());
    }

    @Test
    void ofMonth_returnsStartAndEndForRegularMonth() {
        PeriodRange range = PeriodRange.ofMonth(YearMonth.of(2025, 4));
        assertEquals(LocalDate.of(2025, 4, 1), range.getStart());
        assertEquals(LocalDate.of(2025, 4, 30), range.getEnd());
        assertTrue(range.isValid());
    }

    @Test
    void ofMonth_returnsLastDayForLeapYearFebruary() {
        PeriodRange range = PeriodRange.ofMonth(YearMonth.of(2024, 2));
        assertEquals(LocalDate.of(2024, 2, 1), range.getStart());
        assertEquals(LocalDate.of(2024, 2, 29), range.getEnd());
        assertTrue(range.isValid());
    }

    @Test
    void allTime_returnsNullBounds_andIsValid() {
        PeriodRange range = PeriodRange.allTime();
        assertNull(range.getStart());
        assertNull(range.getEnd());
        assertTrue(range.isValid());
    }
}
