package ca.yorku.smartbudget;

import ca.yorku.smartbudget.persistence.JsonStorage;
import ca.yorku.smartbudget.persistence.Storage;
import ca.yorku.smartbudget.service.AlertService;
import ca.yorku.smartbudget.service.BudgetService;
import ca.yorku.smartbudget.service.ReportService;
import ca.yorku.smartbudget.service.TransactionService;
import ca.yorku.smartbudget.ui.RootView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * SmartBudget main entry point. Loads saved data on startup, then shows the GUI
 * with sidebar navigation (Transactions, Budgets, Reports).
 */
public class MainApp extends Application {

    private static Path getDataDir() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, ".smartbudget");
    }

    @Override
    public void start(Stage stage) {
        Path dataDir = getDataDir();
        Storage storage = new JsonStorage(
                dataDir.resolve("transactions.json"),
                dataDir.resolve("budgets.json")
        );
        TransactionService transactionService = new TransactionService(storage);
        BudgetService budgetService = new BudgetService(transactionService, storage);
        // Load saved data from ~/.smartbudget/ before showing the UI
        transactionService.loadOnStartup();
        budgetService.loadOnStartup();
        AlertService alertService = new AlertService(budgetService, transactionService);
        ReportService reportService = new ReportService(transactionService);
        BorderPane root = RootView.createRoot(stage, transactionService, budgetService, alertService, reportService);
        Scene scene = new Scene(root, 960, 640);
        scene.getStylesheets().add(getClass().getResource("/ca/yorku/smartbudget/app.css").toExternalForm());
        stage.setTitle("SmartBudget - Personal Finance Tracker");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(500);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
