package ca.yorku.smartbudget.ui.modals;

import ca.yorku.smartbudget.domain.Budget;
import ca.yorku.smartbudget.domain.Category;
import ca.yorku.smartbudget.ui.BudgetsController;
import ca.yorku.smartbudget.util.ValidationException;
import ca.yorku.smartbudget.util.Validator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.time.YearMonth;

public final class AddBudgetModal {
    private static final String ACCENT_GREEN = "#22c55e";
    private static final String SUBTITLE_GREY = "#6b7280";

    /** On Create Budget we call controller.addBudget(budget). */
    public static void show(Window owner, BudgetsController controller) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Create Budget");

        Button closeBtn = new Button("X");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> stage.close());
        Label title = new Label("Create Budget");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label subtitle = new Label("Set a monthly spending limit for a category.");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE_GREY + ";");

        ComboBox<String> categoryCombo = new ComboBox<>();
        for (Category c : Category.values()) {
            categoryCombo.getItems().add(c.getDisplayName());
        }
        categoryCombo.setPromptText("Select a category");

        TextField limitField = new TextField("0.00");
        limitField.setPromptText("0.00");
        Label limitHelp = new Label("Set how much you want to spend in this category per month.");
        limitHelp.setStyle("-fx-font-size: 11px; -fx-text-fill: " + SUBTITLE_GREY + ";");
        limitHelp.setWrapText(true);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #c62828; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: white; -fx-border-color: #9e9e9e;");
        cancelBtn.setOnAction(e -> stage.close());

        Button createBtn = new Button("Create Budget");
        createBtn.setStyle("-fx-background-color: " + ACCENT_GREEN + "; -fx-text-fill: white; -fx-font-weight: bold;");
        createBtn.setOnAction(e -> {
            errorLabel.setText("");
            try {
                int catIndex = categoryCombo.getSelectionModel().getSelectedIndex();
                if (catIndex < 0) { errorLabel.setText("Please select a category."); return; }
                Category cat = Category.values()[catIndex];
                BigDecimal limit = Validator.parseDecimal(limitField.getText());
                if (limit == null || limit.compareTo(BigDecimal.ZERO) <= 0) {
                    errorLabel.setText("Enter a valid limit greater than 0."); return;
                }
                Budget budget = new Budget(cat, YearMonth.now(), limit);
                ca.yorku.smartbudget.util.Validator.validateBudget(budget);
                controller.addBudget(budget);
                stage.close();
            } catch (ValidationException ex) {
                errorLabel.setText(ex.getDisplayMessage());
            } catch (Exception ex) {
                errorLabel.setText(ex.getMessage() != null ? ex.getMessage() : "Invalid input.");
            }
        });

        HBox buttons = new HBox(12, cancelBtn, createBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox form = new VBox(12,
                new Label("Category"), categoryCombo,
                new Label("Monthly Limit"), limitField, limitHelp,
                errorLabel,
                buttons
        );
        form.setPadding(new Insets(24));
        form.setMinWidth(360);
        for (Node node : form.getChildren()) {
            if (node instanceof Label) {
                Label lbl = (Label) node;
                if (!lbl.getText().isEmpty() && !lbl.getText().startsWith("Set how")) {
                    lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                }
            }
        }

        HBox titleRow = new HBox();
        titleRow.getChildren().add(title);
        HBox.setHgrow(title, Priority.ALWAYS);
        titleRow.getChildren().add(closeBtn);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        VBox root = new VBox(16, new VBox(4, titleRow, subtitle), form);
        root.setPadding(new Insets(20));
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    }
}
