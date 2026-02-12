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

public final class EditBudgetModal {
    private static final String ACCENT_GREEN = "#22c55e";
    private static final String SUBTITLE_GREY = "#6b7280";

    public static void show(Window owner, Budget existing, BudgetsController controller) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Edit Budget");

        Label title = new Label("Edit Budget");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label subtitle = new Label("Update the monthly spending limit for this category.");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE_GREY + ";");

        ComboBox<String> categoryCombo = new ComboBox<>();
        int selIndex = 0;
        for (int i = 0; i < Category.values().length; i++) {
            categoryCombo.getItems().add(Category.values()[i].getDisplayName());
            if (existing.getCategory() == Category.values()[i]) selIndex = i;
        }
        categoryCombo.getSelectionModel().select(selIndex);
        categoryCombo.setDisable(true);

        TextField limitField = new TextField(existing.getLimit() != null ? existing.getLimit().toPlainString() : "0.00");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #c62828; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: white; -fx-border-color: #9e9e9e;");
        cancelBtn.setOnAction(e -> stage.close());

        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: " + ACCENT_GREEN + "; -fx-text-fill: white; -fx-font-weight: bold;");
        saveBtn.setOnAction(e -> {
            errorLabel.setText("");
            try {
                BigDecimal limit = Validator.parseDecimal(limitField.getText());
                if (limit == null || limit.compareTo(BigDecimal.ZERO) <= 0) {
                    errorLabel.setText("Enter a valid limit greater than 0."); return;
                }
                Budget updated = new Budget(existing.getCategory(), existing.getMonth(), limit);
                ca.yorku.smartbudget.util.Validator.validateBudget(updated);
                controller.updateBudget(updated);
                stage.close();
            } catch (ValidationException ex) {
                errorLabel.setText(ex.getDisplayMessage());
            } catch (Exception ex) {
                errorLabel.setText(ex.getMessage() != null ? ex.getMessage() : "Invalid input.");
            }
        });

        HBox buttons = new HBox(12, cancelBtn, saveBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox form = new VBox(12,
                new Label("Category"), categoryCombo,
                new Label("Monthly Limit"), limitField,
                errorLabel,
                buttons
        );
        form.setPadding(new Insets(24));
        form.setMinWidth(360);
        for (Node node : form.getChildren()) {
            if (node instanceof Label) {
                Label lbl = (Label) node;
                if (!lbl.getText().isEmpty()) {
                    lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                }
            }
        }

        VBox root = new VBox(16, new VBox(4, title, subtitle), form);
        root.setPadding(new Insets(20));
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    }
}
