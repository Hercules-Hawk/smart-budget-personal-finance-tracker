package ca.yorku.smartbudget.ui.modals;

import ca.yorku.smartbudget.domain.Category;
import ca.yorku.smartbudget.domain.TransactionType;

import java.util.ArrayList;
import java.util.List;

final class TransactionCategoryOptions {
    private static final List<Category> INCOME_CATEGORIES = List.of(
            Category.SALARY,
            Category.FREELANCE,
            Category.INVESTMENT
    );

    private TransactionCategoryOptions() {
    }

    static List<Category> categoriesFor(TransactionType type) {
        List<Category> out = new ArrayList<>();
        if (type == null) {
            return out;
        }
        if (type == TransactionType.INCOME) {
            out.addAll(INCOME_CATEGORIES);
            return out;
        }
        for (Category category : Category.values()) {
            if (!INCOME_CATEGORIES.contains(category)) {
                out.add(category);
            }
        }
        return out;
    }

    static String displayType(TransactionType type) {
        if (type == TransactionType.EXPENSE) return "Expense";
        if (type == TransactionType.INCOME) return "Income";
        return "";
    }
}
