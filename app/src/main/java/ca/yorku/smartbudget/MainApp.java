package ca.yorku.smartbudget;

import ca.yorku.smartbudget.service.AlertService;
import ca.yorku.smartbudget.service.BudgetService;
import ca.yorku.smartbudget.service.ReportService;
import ca.yorku.smartbudget.service.TransactionService;
import ca.yorku.smartbudget.ui.RootView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * SmartBudget main entry point. Loads the initial GUI with sidebar
 * navigation (Transactions, Budgets, Reports) and wired transaction/budget functionality.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        TransactionService transactionService = new TransactionService();
        BudgetService budgetService = new BudgetService(transactionService);
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
