package ca.yorku.smartbudget.ui.modals;

import ca.yorku.smartbudget.domain.Category;
import ca.yorku.smartbudget.domain.Transaction;
import ca.yorku.smartbudget.domain.TransactionType;
import ca.yorku.smartbudget.ui.TransactionsController;
import ca.yorku.smartbudget.util.ValidationException;
import ca.yorku.smartbudget.util.Validator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class EditTransactionModal {
    private static final String ACCENT_GREEN = "#22c55e";
    private static final String SUBTITLE_GREY = "#6b7280";

    /** Shows modal pre-filled with existing transaction; on Save calls controller.updateTransaction(updated). */
    public static void show(Window owner, Transaction existing, TransactionsController controller) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Edit Transaction");

        Label title = new Label("Edit Transaction");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label subtitle = new Label("Update the details of your transaction.");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE_GREY + ";");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().add("Expense");
        typeCombo.getItems().add("Income");
        typeCombo.getSelectionModel().select(existing.getType() == TransactionType.EXPENSE ? 0 : 1);

        TextField amountField = new TextField(existing.getAmount() != null ? existing.getAmount().toPlainString() : "0.00");

        ComboBox<String> categoryCombo = new ComboBox<>();
        int catIndex = 0;
        for (int i = 0; i < Category.values().length; i++) {
            categoryCombo.getItems().add(Category.values()[i].getDisplayName());
            if (existing.getCategory() == Category.values()[i]) catIndex = i;
        }
        categoryCombo.getSelectionModel().select(catIndex);

        DatePicker datePicker = new DatePicker(existing.getDate() != null ? existing.getDate() : LocalDate.now());

        TextField descriptionField = new TextField(existing.getNote() != null ? existing.getNote() : "");

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
                int typeIndex = typeCombo.getSelectionModel().getSelectedIndex();
                if (typeIndex < 0) { errorLabel.setText("Please select a type."); return; }
                TransactionType type = (typeIndex == 0) ? TransactionType.EXPENSE : TransactionType.INCOME;
                BigDecimal amount = Validator.parseDecimal(amountField.getText());
                if (amount == null) { errorLabel.setText("Enter a valid amount."); return; }
                int cIndex = categoryCombo.getSelectionModel().getSelectedIndex();
                if (cIndex < 0) { errorLabel.setText("Please select a category."); return; }
                Category cat = Category.values()[cIndex];
                LocalDate date = datePicker.getValue();
                if (date == null) { errorLabel.setText("Please select a date."); return; }
                String note = descriptionField.getText() != null ? descriptionField.getText().trim() : "";
                Transaction updated = new Transaction(existing.getId(), type, amount, date, cat, note);
                ca.yorku.smartbudget.util.Validator.validateTransaction(updated);
                controller.updateTransaction(updated);
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
                new Label("Type"), typeCombo,
                new Label("Amount"), amountField,
                new Label("Category"), categoryCombo,
                new Label("Date"), datePicker,
                new Label("Description"), descriptionField,
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
