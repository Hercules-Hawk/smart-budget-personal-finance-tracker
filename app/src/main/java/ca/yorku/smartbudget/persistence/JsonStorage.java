package ca.yorku.smartbudget.persistence;

import ca.yorku.smartbudget.domain.Budget;
import ca.yorku.smartbudget.domain.Transaction;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and loads transactions and budgets as JSON files using Jackson.
 */
public class JsonStorage implements Storage {

    private final Path transactionsPath;
    private final Path budgetsPath;
    private final ObjectMapper mapper;

    public JsonStorage(Path transactionsPath, Path budgetsPath) {
        this.transactionsPath = transactionsPath;
        this.budgetsPath = budgetsPath;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public List<Transaction> loadTransactions() {
        if (!Files.exists(transactionsPath)) return new ArrayList<>();
        // Return empty list on parse error so app can still start
        try {
            List<Transaction> list = mapper.readValue(transactionsPath.toFile(),
                    new TypeReference<List<Transaction>>() {});
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void saveTransactions(List<Transaction> transactions) {
        try {
            // Create parent directory if it does not exist
            if (transactionsPath.getParent() != null) {
                Files.createDirectories(transactionsPath.getParent());
            }
            mapper.writeValue(transactionsPath.toFile(), transactions);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save transactions", e);
        }
    }

    @Override
    public List<Budget> loadBudgets() {
        if (!Files.exists(budgetsPath)) return new ArrayList<>();
        // Return empty list on parse error so app can still start
        try {
            List<Budget> list = mapper.readValue(budgetsPath.toFile(),
                    new TypeReference<List<Budget>>() {});
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void saveBudgets(List<Budget> budgets) {
        try {
            if (budgetsPath.getParent() != null) {
                Files.createDirectories(budgetsPath.getParent());
            }
            mapper.writeValue(budgetsPath.toFile(), budgets);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save budgets", e);
        }
    }
}
