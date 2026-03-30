package ca.yorku.smartbudget.ui.modals;

import ca.yorku.smartbudget.domain.Category;
import ca.yorku.smartbudget.domain.TransactionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionCategoryOptionsTest {

    @Test
    void categoriesFor_income_returnsOnlyIncomeCategories() {
        List<Category> categories = TransactionCategoryOptions.categoriesFor(TransactionType.INCOME);

        assertEquals(List.of(Category.SALARY, Category.FREELANCE, Category.INVESTMENT), categories);
    }

    @Test
    void categoriesFor_expense_excludesIncomeCategories() {
        List<Category> categories = TransactionCategoryOptions.categoriesFor(TransactionType.EXPENSE);

        assertFalse(categories.contains(Category.SALARY));
        assertFalse(categories.contains(Category.FREELANCE));
        assertFalse(categories.contains(Category.INVESTMENT));
        assertTrue(categories.contains(Category.FOOD_AND_DINING));
        assertTrue(categories.contains(Category.BILLS_AND_UTILITIES));
        assertTrue(categories.contains(Category.OTHER));
    }

    @Test
    void categoriesFor_null_returnsEmptyList() {
        assertTrue(TransactionCategoryOptions.categoriesFor(null).isEmpty());
    }

    @Test
    void displayType_returnsUiLabels() {
        assertEquals("Expense", TransactionCategoryOptions.displayType(TransactionType.EXPENSE));
        assertEquals("Income", TransactionCategoryOptions.displayType(TransactionType.INCOME));
        assertEquals("", TransactionCategoryOptions.displayType(null));
    }
}
