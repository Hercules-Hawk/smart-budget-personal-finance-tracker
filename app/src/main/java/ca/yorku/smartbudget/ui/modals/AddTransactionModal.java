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
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class AddTransactionModal {
    private static final String ACCENT_GREEN = "#22c55e";
    private static final String SUBTITLE_GREY = "#6b7280";

    /** When user clicks Add Transaction we call controller.addTransaction(tx) then close. */
    public static void show(Window owner, TransactionsController controller) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Add Transaction");
        stage.setResizable(false);
        if (owner != null && owner instanceof javafx.stage.Stage) {
            stage.setOnShown(ev -> {
                javafx.stage.Stage ownerStage = (javafx.stage.Stage) owner;
                stage.setX(ownerStage.getX() + (ownerStage.getWidth() - stage.getWidth()) / 2);
                stage.setY(ownerStage.getY() + (ownerStage.getHeight() - stage.getHeight()) / 2);
            });
        }

        Button closeBtn = new Button("X");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> stage.close());
        Label title = new Label("Add Transaction");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label subtitle = new Label("Enter the details of your transaction.");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE_GREY + ";");
        HBox titleRow = new HBox();
        titleRow.getChildren().add(title);
        HBox.setHgrow(title, Priority.ALWAYS);
        titleRow.getChildren().add(closeBtn);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        ComboBox<TransactionType> typeCombo = new ComboBox<>();
        typeCombo.getItems().add(TransactionType.EXPENSE);
        typeCombo.getItems().add(TransactionType.INCOME);
        typeCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(TransactionType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : TransactionCategoryOptions.displayType(item));
            }
        });
        typeCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(TransactionType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : TransactionCategoryOptions.displayType(item));
            }
        });
        typeCombo.getSelectionModel().select(TransactionType.EXPENSE);

        TextField amountField = new TextField("0.00");
        amountField.setPromptText("0.00");

        ComboBox<Category> categoryCombo = new ComboBox<>();
        categoryCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        });
        categoryCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        });
        categoryCombo.setPromptText("Select a category");
        populateCategoryOptions(categoryCombo, typeCombo.getValue(), null);
        typeCombo.valueProperty().addListener((obs, oldType, newType) ->
                populateCategoryOptions(categoryCombo, newType, null));

        DatePicker datePicker = new DatePicker(LocalDate.now());

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("e.g., Grocery shopping");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #c62828; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: white; -fx-border-color: #9e9e9e;");
        cancelBtn.setOnAction(e -> stage.close());

        Button addBtn = new Button("Add Transaction");
        addBtn.setStyle("-fx-background-color: " + ACCENT_GREEN + "; -fx-text-fill: white; -fx-font-weight: bold;");
        addBtn.setOnAction(e -> {
            errorLabel.setText("");
            try {
                TransactionType type = typeCombo.getValue();
                if (type == null) { errorLabel.setText("Please select a type."); return; }
                BigDecimal amount = Validator.parseDecimal(amountField.getText());
                if (amount == null) { errorLabel.setText("Enter a valid amount."); return; }
                Category cat = categoryCombo.getValue();
                if (cat == null) { errorLabel.setText("Please select a category."); return; }
                LocalDate date = datePicker.getValue();
                if (date == null) { errorLabel.setText("Please select a date."); return; }
                String note = descriptionField.getText() != null ? descriptionField.getText().trim() : "";
                Transaction tx = new Transaction(type, amount, date, cat, note);
                ca.yorku.smartbudget.util.Validator.validateTransaction(tx);
                controller.addTransaction(tx);
                stage.close();
            } catch (ValidationException ex) {
                errorLabel.setText(ex.getDisplayMessage());
            } catch (Exception ex) {
                errorLabel.setText(ex.getMessage() != null ? ex.getMessage() : "Invalid input.");
            }
        });

        HBox buttons = new HBox(12, cancelBtn, addBtn);
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

        VBox root = new VBox(16, new VBox(4, titleRow, subtitle), form);
        root.setPadding(new Insets(20));
        stage.setScene(new javafx.scene.Scene(root));
        stage.showAndWait();
    }

    private static void populateCategoryOptions(ComboBox<Category> categoryCombo, TransactionType type, Category preferred) {
        categoryCombo.getItems().setAll(TransactionCategoryOptions.categoriesFor(type));
        if (preferred != null && categoryCombo.getItems().contains(preferred)) {
            categoryCombo.getSelectionModel().select(preferred);
        } else {
            categoryCombo.getSelectionModel().clearSelection();
        }
    }
}
