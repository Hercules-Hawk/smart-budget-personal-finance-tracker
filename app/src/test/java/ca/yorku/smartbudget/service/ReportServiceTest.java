package ca.yorku.smartbudget.service;

import ca.yorku.smartbudget.domain.Category;
import ca.yorku.smartbudget.domain.CategoryTotal;
import ca.yorku.smartbudget.domain.PeriodRange;
import ca.yorku.smartbudget.domain.ReportResult;
import ca.yorku.smartbudget.domain.SummaryReport;
import ca.yorku.smartbudget.domain.Transaction;
import ca.yorku.smartbudget.domain.TransactionType;
import ca.yorku.smartbudget.persistence.InMemoryStorage;
import ca.yorku.smartbudget.persistence.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportServiceTest {

    private TransactionService transactionService;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        Storage storage = new InMemoryStorage();
        transactionService = new TransactionService(storage);
        reportService = new ReportService(transactionService);
    }

    private void addIncome(LocalDate date, Category category, String amount) {
        transactionService.add(new Transaction(
                TransactionType.INCOME,
                new BigDecimal(amount),
                date,
                category,
                "income"
        ));
    }

    private void addExpense(LocalDate date, Category category, String amount) {
        transactionService.add(new Transaction(
                TransactionType.EXPENSE,
                new BigDecimal(amount),
                date,
                category,
                "expense"
        ));
    }

    private void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

    private CategoryTotal findCategoryTotal(List<CategoryTotal> list, Category category) {
        return list.stream().filter(ct -> ct.getCategory() == category).findFirst().orElse(null);
    }

    @Test
    void generateReport_returnsInvalidResult_whenRangeIsNull() {
        ReportResult result = reportService.generateReport(null);

        assertNotNull(result);
        assertNull(result.getRange());
        assertFalse(result.isHasData());
        assertEquals("Invalid date range.", result.getMessage());
        assertTrue(result.getBreakdown().isEmpty());
        assertAmount("0", result.getSummary().getTotalIncome());
        assertAmount("0", result.getSummary().getTotalExpense());
        assertAmount("0", result.getSummary().getBalance());
    }

    @Test
    void generateReport_returnsInvalidResult_whenRangeStartAfterEnd() {
        PeriodRange invalid = new PeriodRange(LocalDate.of(2025, 1, 31), LocalDate.of(2025, 1, 1));

        ReportResult result = reportService.generateReport(invalid);

        assertNotNull(result);
        assertSame(invalid, result.getRange());
        assertFalse(result.isHasData());
        assertEquals("Invalid date range.", result.getMessage());
        assertTrue(result.getBreakdown().isEmpty());
        assertAmount("0", result.getSummary().getTotalIncome());
        assertAmount("0", result.getSummary().getTotalExpense());
        assertAmount("0", result.getSummary().getBalance());
    }

    @Test
    void generateReport_returnsNoRecords_whenValidRangeHasNoTransactions() {
        PeriodRange range = new PeriodRange(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 28));

        ReportResult result = reportService.generateReport(range);

        assertNotNull(result);
        assertTrue(result.getSummary().isEmpty());
        assertFalse(result.isHasData());
        assertEquals("No records found", result.getMessage());
        assertTrue(result.getBreakdown().isEmpty());
    }

    @Test
    void generateReport_calculatesSummaryAndBreakdown_forUc5Range() {
        addIncome(LocalDate.of(2025, 1, 2), Category.SALARY, "1000.00");
        addExpense(LocalDate.of(2025, 1, 5), Category.FOOD_AND_DINING, "150.00");
        addExpense(LocalDate.of(2025, 1, 15), Category.FOOD_AND_DINING, "50.00");
        addExpense(LocalDate.of(2025, 1, 21), Category.SHOPPING, "100.00");

        // Out of selected range (must not affect January report)
        addIncome(LocalDate.of(2025, 2, 1), Category.SALARY, "500.00");
        addExpense(LocalDate.of(2025, 2, 2), Category.SHOPPING, "75.00");

        PeriodRange january = new PeriodRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        ReportResult result = reportService.generateReport(january);

        assertTrue(result.isHasData());
        assertNull(result.getMessage());

        SummaryReport summary = result.getSummary();
        assertAmount("1000.00", summary.getTotalIncome());
        assertAmount("300.00", summary.getTotalExpense());
        assertAmount("700.00", summary.getBalance());

        List<CategoryTotal> breakdown = result.getBreakdown();
        assertEquals(2, breakdown.size());

        CategoryTotal food = findCategoryTotal(breakdown, Category.FOOD_AND_DINING);
        assertNotNull(food);
        assertAmount("200.00", food.getTotal());
        assertEquals(66.67, food.getPercentOfTotal(), 0.0001);

        CategoryTotal shopping = findCategoryTotal(breakdown, Category.SHOPPING);
        assertNotNull(shopping);
        assertAmount("100.00", shopping.getTotal());
        assertEquals(33.33, shopping.getPercentOfTotal(), 0.0001);
    }

    @Test
    void generateReport_includesTransactionsOnStartAndEndDates_boundaryCase() {
        PeriodRange range = new PeriodRange(LocalDate.of(2025, 6, 10), LocalDate.of(2025, 6, 20));

        addExpense(LocalDate.of(2025, 6, 10), Category.FOOD_AND_DINING, "10.00"); // start boundary
        addExpense(LocalDate.of(2025, 6, 20), Category.FOOD_AND_DINING, "20.00"); // end boundary
        addExpense(LocalDate.of(2025, 6, 9), Category.FOOD_AND_DINING, "99.00");  // out of range
        addExpense(LocalDate.of(2025, 6, 21), Category.FOOD_AND_DINING, "99.00"); // out of range

        ReportResult result = reportService.generateReport(range);

        assertTrue(result.isHasData());
        assertAmount("30.00", result.getSummary().getTotalExpense());
        assertAmount("-30.00", result.getSummary().getBalance());

        List<CategoryTotal> breakdown = result.getBreakdown();
        assertEquals(1, breakdown.size());
        assertAmount("30.00", breakdown.get(0).getTotal());
    }

    @Test
    void generateReport_treatsIncomeOnlyRangeAsData_notNoRecordsState() {
        PeriodRange range = new PeriodRange(LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 30));
        addIncome(LocalDate.of(2025, 9, 15), Category.SALARY, "200.00");

        ReportResult result = reportService.generateReport(range);

        assertTrue(result.isHasData());
        assertNull(result.getMessage());
        assertAmount("200.00", result.getSummary().getTotalIncome());
        assertAmount("0", result.getSummary().getTotalExpense());
        assertTrue(result.getBreakdown().isEmpty());
    }

    @Test
    void getSummary_returnsZeroSummary_whenRangeIsNull() {
        SummaryReport summary = reportService.getSummary(null);

        assertTrue(summary.isEmpty());
        assertAmount("0", summary.getTotalIncome());
        assertAmount("0", summary.getTotalExpense());
        assertAmount("0", summary.getBalance());
    }

    @Test
    void getSummary_returnsTotals_forSelectedRangeOnly() {
        addIncome(LocalDate.of(2025, 3, 10), Category.SALARY, "500.00");
        addIncome(LocalDate.of(2025, 4, 1), Category.SALARY, "100.00");
        addExpense(LocalDate.of(2025, 3, 12), Category.BILLS_AND_UTILITIES, "120.00");
        addExpense(LocalDate.of(2025, 4, 2), Category.BILLS_AND_UTILITIES, "20.00");

        PeriodRange march = new PeriodRange(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31));
        SummaryReport summary = reportService.getSummary(march);

        assertAmount("500.00", summary.getTotalIncome());
        assertAmount("120.00", summary.getTotalExpense());
        assertAmount("380.00", summary.getBalance());
    }

    @Test
    void getSpendingByCategory_returnsEmpty_whenRangeIsNull() {
        assertTrue(reportService.getSpendingByCategory(null).isEmpty());
    }

    @Test
    void getSpendingByCategory_ignoresIncome_andReturnsOnlyExpenseCategories() {
        addIncome(LocalDate.of(2025, 5, 1), Category.SALARY, "1500.00");
        addExpense(LocalDate.of(2025, 5, 2), Category.ENTERTAINMENT, "30.00");
        addExpense(LocalDate.of(2025, 5, 3), Category.HEALTHCARE, "70.00");

        PeriodRange may = new PeriodRange(LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 31));
        List<CategoryTotal> breakdown = reportService.getSpendingByCategory(may);

        assertEquals(2, breakdown.size());
        assertNotNull(findCategoryTotal(breakdown, Category.ENTERTAINMENT));
        assertNotNull(findCategoryTotal(breakdown, Category.HEALTHCARE));
        assertNull(findCategoryTotal(breakdown, Category.SALARY));
    }

    @Test
    void generateReport_allTimeIncludesAllTransactions() {
        addIncome(LocalDate.of(2025, 1, 1), Category.SALARY, "100.00");
        addExpense(LocalDate.of(2025, 1, 2), Category.OTHER, "40.00");
        addIncome(LocalDate.of(2025, 8, 1), Category.FREELANCE, "50.00");
        addExpense(LocalDate.of(2025, 8, 2), Category.OTHER, "10.00");

        ReportResult result = reportService.generateReport(PeriodRange.allTime());

        assertTrue(result.isHasData());
        assertAmount("150.00", result.getSummary().getTotalIncome());
        assertAmount("50.00", result.getSummary().getTotalExpense());
        assertAmount("100.00", result.getSummary().getBalance());
    }
}
