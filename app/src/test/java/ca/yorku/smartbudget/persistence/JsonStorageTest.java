package ca.yorku.smartbudget.persistence;

import ca.yorku.smartbudget.domain.Budget;
import ca.yorku.smartbudget.domain.Category;
import ca.yorku.smartbudget.domain.Transaction;
import ca.yorku.smartbudget.domain.TransactionType;
import ca.yorku.smartbudget.service.BudgetService;
import ca.yorku.smartbudget.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JsonStorageTest {

    private Path transactionsPath;
    private Path budgetsPath;
    private JsonStorage storage;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDir = Files.createTempDirectory("json-storage-test");
        transactionsPath = tempDir.resolve("transactions.json");
        budgetsPath = tempDir.resolve("budgets.json");
        storage = new JsonStorage(transactionsPath, budgetsPath);
    }

    @Test
    void loadTransactions_returnsEmpty_whenFileDoesNotExist() {
        assertFalse(Files.exists(transactionsPath));
        List<Transaction> loaded = storage.loadTransactions();
        assertNotNull(loaded);
        assertTrue(loaded.isEmpty());
    }

    @Test
    void saveAndLoadTransactions_roundtrips() {
        Transaction tx = new Transaction(
                UUID.randomUUID(),
                TransactionType.EXPENSE,
                new BigDecimal("25.50"),
                LocalDate.of(2025, 1, 15),
                Category.FOOD_AND_DINING,
                "lunch"
        );
        storage.saveTransactions(List.of(tx));
        assertTrue(Files.exists(transactionsPath));

        List<Transaction> loaded = storage.loadTransactions();
        assertEquals(1, loaded.size());
        assertEquals(tx.getId(), loaded.get(0).getId());
        assertEquals(TransactionType.EXPENSE, loaded.get(0).getType());
        assertEquals(new BigDecimal("25.50"), loaded.get(0).getAmount());
        assertEquals(LocalDate.of(2025, 1, 15), loaded.get(0).getDate());
        assertEquals(Category.FOOD_AND_DINING, loaded.get(0).getCategory());
        assertEquals("lunch", loaded.get(0).getNote());
    }

    @Test
    void loadTransactions_returnsEmpty_whenFileEmpty() throws Exception {
        Files.writeString(transactionsPath, "[]");
        List<Transaction> loaded = storage.loadTransactions();
        assertTrue(loaded.isEmpty());
    }

    @Test
    void loadBudgets_returnsEmpty_whenFileDoesNotExist() {
        assertFalse(Files.exists(budgetsPath));
        List<Budget> loaded = storage.loadBudgets();
        assertNotNull(loaded);
        assertTrue(loaded.isEmpty());
    }

    @Test
    void saveAndLoadBudgets_roundtrips() {
        Budget b = new Budget(Category.SHOPPING, YearMonth.of(2025, 2), new BigDecimal("300.00"));
        storage.saveBudgets(List.of(b));
        assertTrue(Files.exists(budgetsPath));

        List<Budget> loaded = storage.loadBudgets();
        assertEquals(1, loaded.size());
        assertEquals(Category.SHOPPING, loaded.get(0).getCategory());
        assertEquals(YearMonth.of(2025, 2), loaded.get(0).getMonth());
        assertEquals(new BigDecimal("300.00"), loaded.get(0).getLimit());
    }

    @Test
    void loadBudgets_returnsEmpty_whenFileEmpty() throws Exception {
        Files.writeString(budgetsPath, "[]");
        List<Budget> loaded = storage.loadBudgets();
        assertTrue(loaded.isEmpty());
    }

    @Test
    void saveTransactions_createsParentDirectory() {
        Path deepPath = transactionsPath.resolveSibling("subdir/transactions.json");
        JsonStorage deepStorage = new JsonStorage(deepPath, budgetsPath);
        deepStorage.saveTransactions(List.of());
        assertTrue(Files.exists(deepPath.getParent()));
        assertTrue(Files.exists(deepPath));
    }

    @Test
    void loadOnStartup_restoresTransactionsAndBudgets() {
        Transaction tx = new Transaction(
                TransactionType.INCOME,
                new BigDecimal("1000.00"),
                LocalDate.of(2025, 1, 1),
                Category.SALARY,
                "paycheck"
        );
        Budget b = new Budget(Category.FOOD_AND_DINING, YearMonth.of(2025, 1), new BigDecimal("400.00"));
        storage.saveTransactions(List.of(tx));
        storage.saveBudgets(List.of(b));

        TransactionService txService = new TransactionService(storage);
        BudgetService budgetService = new BudgetService(txService, storage);
        txService.loadOnStartup();
        budgetService.loadOnStartup();

        assertEquals(1, txService.getAll().size());
        assertEquals("paycheck", txService.getAll().get(0).getNote());
        assertEquals(1, budgetService.getAllBudgets().size());
        assertEquals(new BigDecimal("400.00"), budgetService.getBudget(Category.FOOD_AND_DINING, YearMonth.of(2025, 1)).getLimit());
    }
}
