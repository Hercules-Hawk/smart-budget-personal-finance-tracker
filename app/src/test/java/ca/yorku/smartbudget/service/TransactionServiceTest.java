package ca.yorku.smartbudget.service;

import ca.yorku.smartbudget.domain.Category;
import ca.yorku.smartbudget.domain.PeriodRange;
import ca.yorku.smartbudget.domain.Transaction;
import ca.yorku.smartbudget.domain.TransactionFilter;
import ca.yorku.smartbudget.domain.TransactionType;
import ca.yorku.smartbudget.util.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceTest {

    private TransactionService service;

    @BeforeEach
    void setUp() {
        service = new TransactionService();
    }

    private Transaction expense(LocalDate date, Category category, String amount) {
        return new Transaction(TransactionType.EXPENSE, new BigDecimal(amount), date, category, "note");
    }

    private Transaction income(LocalDate date, Category category, String amount) {
        return new Transaction(TransactionType.INCOME, new BigDecimal(amount), date, category, "note");
    }

    @Test
    void getAll_returnsEmpty_whenNoTransactions() {
        assertTrue(service.getAll().isEmpty());
    }

    @Test
    void add_validTransaction_returnsAddedAndAppearsInGetAll() {
        Transaction tx = expense(LocalDate.now(), Category.FOOD_AND_DINING, "25.00");
        Transaction added = service.add(tx);
        assertNotNull(added.getId());
        assertEquals(1, service.getAll().size());
        assertEquals(added.getId(), service.getAll().get(0).getId());
    }

    @Test
    void add_invalidTransaction_throwsValidationException() {
        Transaction tx = new Transaction(TransactionType.EXPENSE, new BigDecimal("-1"), LocalDate.now(), Category.FOOD_AND_DINING, "");
        assertThrows(ValidationException.class, () -> service.add(tx));
        assertTrue(service.getAll().isEmpty());
    }

    @Test
    void getById_returnsTransaction_whenExists() {
        Transaction tx = service.add(expense(LocalDate.now(), Category.SHOPPING, "50.00"));
        Transaction found = service.getById(tx.getId());
        assertNotNull(found);
        assertEquals(tx.getId(), found.getId());
    }

    @Test
    void getById_returnsNull_whenNotExists() {
        assertNull(service.getById(UUID.randomUUID()));
    }

    @Test
    void delete_removesTransaction() {
        Transaction tx = service.add(expense(LocalDate.now(), Category.FOOD_AND_DINING, "10.00"));
        service.delete(tx.getId());
        assertTrue(service.getAll().isEmpty());
        assertNull(service.getById(tx.getId()));
    }

    @Test
    void update_replacesTransaction() {
        Transaction tx = service.add(expense(LocalDate.now(), Category.FOOD_AND_DINING, "10.00"));
        UUID id = tx.getId();
        Transaction updated = new Transaction(id, TransactionType.EXPENSE, new BigDecimal("20.00"), LocalDate.now(), Category.SHOPPING, "updated");
        service.update(updated);
        assertEquals(1, service.getAll().size());
        assertEquals(new BigDecimal("20.00"), service.getById(id).getAmount());
        assertEquals(Category.SHOPPING, service.getById(id).getCategory());
    }

    @Test
    void filter_emptyCriteria_returnsAll() {
        service.add(expense(LocalDate.now(), Category.FOOD_AND_DINING, "10.00"));
        List<Transaction> result = service.filter(new TransactionFilter(null, null, null, null, null));
        assertEquals(1, result.size());
    }

    @Test
    void filter_byCategory_returnsMatching() {
        service.add(expense(LocalDate.now(), Category.FOOD_AND_DINING, "10.00"));
        service.add(expense(LocalDate.now(), Category.SHOPPING, "20.00"));
        List<Transaction> result = service.filter(new TransactionFilter(Category.FOOD_AND_DINING, null, null, null, null));
        assertEquals(1, result.size());
        assertEquals(Category.FOOD_AND_DINING, result.get(0).getCategory());
    }

    @Test
    void getByRange_returnsTransactionsInRange() {
        LocalDate jan1 = LocalDate.of(2025, 1, 1);
        LocalDate jan15 = LocalDate.of(2025, 1, 15);
        LocalDate feb1 = LocalDate.of(2025, 2, 1);
        service.add(expense(jan1, Category.FOOD_AND_DINING, "10.00"));
        service.add(expense(jan15, Category.SHOPPING, "20.00"));
        service.add(expense(feb1, Category.FOOD_AND_DINING, "30.00"));
        PeriodRange jan = new PeriodRange(jan1, LocalDate.of(2025, 1, 31));
        List<Transaction> inJan = service.getByRange(jan);
        assertEquals(2, inJan.size());
    }

    @Test
    void getTotalsInRange_sumsIncomeAndExpense() {
        LocalDate d = LocalDate.of(2025, 1, 10);
        service.add(income(d, Category.SALARY, "100.00"));
        service.add(expense(d, Category.FOOD_AND_DINING, "30.00"));
        service.add(expense(d, Category.SHOPPING, "20.00"));
        Map<String, BigDecimal> totals = service.getTotalsInRange(d, d);
        assertEquals(new BigDecimal("100.00"), totals.get("income"));
        assertEquals(new BigDecimal("50.00"), totals.get("expense"));
    }

    @Test
    void getExpensesForCategoryAndMonth_returnsSumForCategoryAndMonth() {
        YearMonth jan = YearMonth.of(2025, 1);
        service.add(expense(LocalDate.of(2025, 1, 5), Category.FOOD_AND_DINING, "15.00"));
        service.add(expense(LocalDate.of(2025, 1, 20), Category.FOOD_AND_DINING, "25.00"));
        service.add(expense(LocalDate.of(2025, 2, 1), Category.FOOD_AND_DINING, "10.00"));
        BigDecimal spent = service.getExpensesForCategoryAndMonth(Category.FOOD_AND_DINING, jan);
        assertEquals(new BigDecimal("40.00"), spent);
    }
}
