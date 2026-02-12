package ca.yorku.smartbudget.ui;

import ca.yorku.smartbudget.service.AlertService;
import ca.yorku.smartbudget.service.BudgetService;
import ca.yorku.smartbudget.service.ReportService;
import ca.yorku.smartbudget.service.TransactionService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Builds the main application layout to match the UI design screenshots:
 * left sidebar (SmartBudget branding + Transactions / Budgets / Reports),
 * main content area with screen-specific layout and controls.
 */
public final class RootView {

    private static final String BRAND_GREEN = "#16a34a";
    private static final String SUBTITLE_GREY = "#6b7280";
    private static final String SIDEBAR_BG = "#fafafa";
    private static final String ACTIVE_NAV_BG = "#dcfce7";

    private RootView() {
    }

    /**
     * Returns the root layout: sidebar + main content. Content area is swapped when nav is clicked.
     */
    public static BorderPane createRoot(Stage stage, TransactionService transactionService, BudgetService budgetService,
                                        AlertService alertService, ReportService reportService) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f2f5;");

        StackPane contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #f0f2f5;");

        TransactionsController transactionsController = new TransactionsController(transactionService, alertService);
        transactionsController.build(stage);
        BudgetsController budgetsController = new BudgetsController(budgetService, transactionService, alertService);
        budgetsController.build(stage);
        ReportsController reportsController = new ReportsController(reportService);
        reportsController.build();

        contentArea.getChildren().add(transactionsController.getPane());

        VBox sidebar = buildSidebar(contentArea, transactionsController, budgetsController, reportsController);
        root.setLeft(sidebar);
        root.setCenter(contentArea);
        return root;
    }

    private static VBox buildSidebar(StackPane contentArea, TransactionsController transactionsController, BudgetsController budgetsController, ReportsController reportsController) {
        Label brand = new Label("SmartBudget");
        brand.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + BRAND_GREEN + ";");
        Label tagline = new Label("Personal Finance Tracker");
        tagline.setStyle("-fx-font-size: 11px; -fx-text-fill: " + SUBTITLE_GREY + ";");
        tagline.setWrapText(true);

        VBox header = new VBox(2, brand, tagline);
        header.setPadding(new Insets(20, 20, 24, 20));

        Button transactionsBtn = navButton("Transactions", true);
        Button budgetsBtn = navButton("Budgets", false);
        Button reportsBtn = navButton("Reports", false);

        transactionsBtn.setOnAction(e -> {
            setActiveNav(transactionsBtn, budgetsBtn, reportsBtn);
            contentArea.getChildren().setAll(transactionsController.getPane());
            transactionsController.refresh();
        });
        budgetsBtn.setOnAction(e -> {
            setActiveNav(budgetsBtn, transactionsBtn, reportsBtn);
            contentArea.getChildren().setAll(budgetsController.getPane());
            budgetsController.refresh();
        });
        reportsBtn.setOnAction(e -> {
            setActiveNav(reportsBtn, transactionsBtn, budgetsBtn);
            contentArea.getChildren().setAll(reportsController.getPane());
            reportsController.refresh();
        });

        VBox nav = new VBox(4, transactionsBtn, budgetsBtn, reportsBtn);
        nav.setPadding(new Insets(0, 12, 12, 12));

        VBox sidebarBox = new VBox(header, nav);
        sidebarBox.setStyle("-fx-background-color: " + SIDEBAR_BG + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 10, 0, 0, 0);");
        sidebarBox.setMinWidth(220);
        sidebarBox.setMaxWidth(220);
        return sidebarBox;
    }

    private static Button navButton(String text, boolean active) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setPadding(new Insets(12, 16, 12, 16));
        b.setId("nav-" + text.toLowerCase());
        b.setStyle(active
                ? "-fx-background-color: " + ACTIVE_NAV_BG + "; -fx-text-fill: " + BRAND_GREEN + "; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6;"
                : "-fx-background-color: transparent; -fx-text-fill: " + SUBTITLE_GREY + "; -fx-cursor: hand; -fx-background-radius: 6;");
        return b;
    }

    private static void setActiveNav(Button active, Button... others) {
        active.setStyle("-fx-background-color: " + ACTIVE_NAV_BG + "; -fx-text-fill: " + BRAND_GREEN + "; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6;");
        for (Button o : others) {
            o.setStyle("-fx-background-color: transparent; -fx-text-fill: " + SUBTITLE_GREY + "; -fx-cursor: hand; -fx-background-radius: 6;");
        }
    }

}
