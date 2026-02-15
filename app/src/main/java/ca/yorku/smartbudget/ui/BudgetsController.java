package ca.yorku.smartbudget.ui;

import ca.yorku.smartbudget.domain.Budget;
import ca.yorku.smartbudget.domain.Category;
import ca.yorku.smartbudget.domain.OverspendingAlert;
import ca.yorku.smartbudget.service.AlertService;
import ca.yorku.smartbudget.service.BudgetService;
import ca.yorku.smartbudget.service.TransactionService;
import ca.yorku.smartbudget.ui.modals.AddBudgetModal;
import ca.yorku.smartbudget.ui.modals.DeleteConfirmationModal;
import ca.yorku.smartbudget.ui.modals.EditBudgetModal;
import javafx.scene.control.Alert;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;

public final class BudgetsController {
    private static final String BRAND_GREEN = "#16a34a";
    private static final String ACCENT_GREEN = "#22c55e";
    private static final String EXPENSE_RED = "#dc2626";
    private static final String INCOME_GREEN = "#16a34a";
    private static final String SUBTITLE_GREY = "#6b7280";

    private final BudgetService budgetService;
    private final TransactionService transactionService;
    private final AlertService alertService;
    private BorderPane pane;
    private FlowPane cardsContainer;
    private Window owner;

    BudgetsController(BudgetService budgetService, TransactionService transactionService, AlertService alertService) {
        this.budgetService = budgetService;
        this.transactionService = transactionService;
        this.alertService = alertService;
    }

    BorderPane getPane() {
        return pane;
    }

    public void addBudget(Budget budget) {
        budgetService.upsertBudget(budget);
        refresh();
        showAlertIfNeeded(budget.getCategory());
    }

    public void updateBudget(Budget budget) {
        budgetService.upsertBudget(budget);
        refresh();
        showAlertIfNeeded(budget.getCategory());
    }

    void showAlertIfNeeded(Category category) {
        OverspendingAlert alert = alertService.checkBudgetStatus(category, java.time.YearMonth.now());
        if (alert != null && alert.isTriggered()) {
            Alert dlg = new Alert(Alert.AlertType.WARNING);
            dlg.setTitle("Budget Alert");
            dlg.setHeaderText("Budget " + alert.getStatus().toString().toLowerCase());
            dlg.setContentText(alert.getDisplayMessage(false));
            dlg.showAndWait();
        }
    }

    void build(Window owner) {
        this.owner = owner;
        Label title = new Label("Budgets");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #212121;");
        Label subtitle = new Label("Set monthly spending limits for each category");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE_GREY + ";");

        Button addBtn = new Button("+ Add Budget");
        addBtn.setId("add-budget-btn");
        addBtn.setStyle("-fx-background-color: " + ACCENT_GREEN + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        addBtn.setOnAction(e -> AddBudgetModal.show(owner, this));

        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(4, title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        headerRow.getChildren().addAll(titleBox, addBtn);
        headerRow.setPadding(new Insets(20, 24, 16, 24));

        cardsContainer = new FlowPane();
        cardsContainer.setHgap(16);
        cardsContainer.setVgap(16);
        cardsContainer.setPadding(new Insets(0, 24, 24, 24));

        VBox main = new VBox(headerRow, cardsContainer);
        VBox.setVgrow(cardsContainer, Priority.ALWAYS);
        main.setStyle("-fx-background-color: #f0f2f5;");
        pane = new BorderPane(main);
        refresh();
    }

    void refresh() {
        if (cardsContainer == null) return;
        cardsContainer.getChildren().clear();
        YearMonth month = YearMonth.now();
        for (Budget budget : budgetService.getBudgetsForMonth(month)) {
            BigDecimal spent = budgetService.getExpensesForCategoryAndMonth(budget.getCategory(), month);
            BigDecimal limit = budget.getLimit();
            // Decide if over budget, by how much, and how much is left; pct is for the progress bar (cap at 1.0 for display)
            boolean overBudget = limit != null && limit.compareTo(BigDecimal.ZERO) > 0 && spent.compareTo(limit) > 0;
            BigDecimal overBy = (limit != null && spent.compareTo(limit) > 0) ? spent.subtract(limit) : BigDecimal.ZERO;
            BigDecimal left = (limit != null && spent.compareTo(limit) <= 0) ? limit.subtract(spent) : BigDecimal.ZERO;
            double pct = (limit != null && limit.compareTo(BigDecimal.ZERO) > 0)
                    ? spent.divide(limit, 4, RoundingMode.HALF_UP).doubleValue()
                    : 0;

            VBox card = buildBudgetCard(
                    budget,
                    spent,
                    limit,
                    pct,
                    overBudget,
                    overBy,
                    left,
                    owner
            );
            cardsContainer.getChildren().add(card);
        }
    }

    private VBox buildBudgetCard(Budget budget, BigDecimal spent, BigDecimal limit, double pctUsed,
                                  boolean overBudget, BigDecimal overBy, BigDecimal left, Window owner) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");
        card.setMinWidth(280);
        card.setMaxWidth(320);

        Label name = new Label(budget.getCategory().getDisplayName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #212121;");
        name.setWrapText(true);

        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + BRAND_GREEN + "; -fx-cursor: hand;");
        editBtn.setOnAction(e -> EditBudgetModal.show(owner, budget, this));

        Button delBtn = new Button("Delete");
        delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + EXPENSE_RED + "; -fx-cursor: hand;");
        delBtn.setOnAction(e -> {
            String description = String.format("%s - %s - Limit: $%s",
                budget.getCategory().getDisplayName(),
                budget.getMonth() != null ? budget.getMonth().toString() : "",
                budget.getLimit() != null ? budget.getLimit().toPlainString() : "0.00"
            );
            DeleteConfirmationModal.show(owner, "Budget", description, () -> {
                budgetService.deleteBudget(budget.getCategory(), budget.getMonth());
                refresh();
            });
        });

        HBox cardHeader = new HBox();
        cardHeader.getChildren().add(name);
        HBox.setHgrow(name, Priority.ALWAYS);
        cardHeader.getChildren().addAll(editBtn, delBtn);
        cardHeader.setAlignment(Pos.CENTER_LEFT);

        String spentStr = "$" + (spent != null ? spent.setScale(2, RoundingMode.HALF_UP).toPlainString() : "0.00");
        String limitStr = "$" + (limit != null ? limit.setScale(2, RoundingMode.HALF_UP).toPlainString() : "0.00");

        Label spentLabel = new Label("Spent");
        spentLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + SUBTITLE_GREY + ";");
        Label spentValue = new Label(spentStr);
        spentValue.setStyle("-fx-font-weight: bold; " + (overBudget ? "-fx-text-fill: " + EXPENSE_RED + ";" : ""));

        Label limitLabel = new Label("Limit");
        limitLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + SUBTITLE_GREY + ";");
        Label limitValue = new Label(limitStr);

        ProgressBar bar = new ProgressBar(Math.min(pctUsed, 1.0));
        bar.setPrefWidth(240);
        bar.setPrefHeight(8);
        if (overBudget) bar.setStyle("-fx-accent: " + EXPENSE_RED + ";");
        else bar.setStyle("-fx-accent: " + ACCENT_GREEN + ";");

        int pctInt = (int) Math.round(pctUsed * 100);
        String pctText = pctInt + "% used";
        String leftText = overBudget
                ? "$" + overBy.setScale(2, RoundingMode.HALF_UP).toPlainString() + " over"
                : "$" + left.setScale(2, RoundingMode.HALF_UP).toPlainString() + " left";
        Label pctLabel = new Label(pctText);
        pctLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE_GREY + ";");
        Label leftLabel = new Label(leftText);
        leftLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (overBudget ? EXPENSE_RED : INCOME_GREEN) + ";");

        card.getChildren().add(cardHeader);
        card.getChildren().add(spentLabel);
        card.getChildren().add(spentValue);
        card.getChildren().add(limitLabel);
        card.getChildren().add(limitValue);
        card.getChildren().add(bar);
        card.getChildren().add(pctLabel);
        card.getChildren().add(leftLabel);

        if (overBudget) {
            HBox alert = new HBox(8, new Label("\u26A0 You've exceeded your budget by $" + overBy.setScale(2, RoundingMode.HALF_UP).toPlainString()));
            alert.setPadding(new Insets(8));
            alert.setStyle("-fx-background-color: #ffebee; -fx-background-radius: 4;");
            ((Label) alert.getChildren().get(0)).setStyle("-fx-text-fill: " + EXPENSE_RED + "; -fx-font-size: 12px;");
            card.getChildren().add(alert);
        }

        return card;
    }
}
