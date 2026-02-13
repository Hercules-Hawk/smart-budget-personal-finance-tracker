package ca.yorku.smartbudget.ui;

import ca.yorku.smartbudget.domain.Category;
import ca.yorku.smartbudget.domain.OverspendingAlert;
import ca.yorku.smartbudget.domain.Transaction;
import ca.yorku.smartbudget.domain.TransactionFilter;
import ca.yorku.smartbudget.domain.TransactionType;
import ca.yorku.smartbudget.service.AlertService;
import ca.yorku.smartbudget.service.TransactionService;
import ca.yorku.smartbudget.ui.modals.AddTransactionModal;
import ca.yorku.smartbudget.ui.modals.DeleteConfirmationModal;
import ca.yorku.smartbudget.ui.modals.EditTransactionModal;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class TransactionsController {
    private static final String ACCENT_GREEN = "#22c55e";
    private static final String EXPENSE_RED = "#dc2626";
    private static final String INCOME_GREEN = "#16a34a";
    private static final String SUBTITLE_GREY = "#6b7280";
    private static final String CARD_STYLE = "-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);";

    private final TransactionService transactionService;
    private final AlertService alertService;
    private BorderPane pane;
    private TableView<Transaction> table;
    private TextField searchField;
    private ComboBox<String> categoryFilter;
    private ComboBox<String> typeFilter;
    private TextField dateFromField;
    private TextField dateToField;

    TransactionsController(TransactionService transactionService, AlertService alertService) {
        this.transactionService = transactionService;
        this.alertService = alertService;
    }

    BorderPane getPane() {
        return pane;
    }

    public void addTransaction(Transaction tx) {
        transactionService.add(tx);
        refresh();
        OverspendingAlert alert = alertService.checkOverspendingFor(tx);
        if (alert != null && alert.isTriggered()) {
            Alert dlg = new Alert(Alert.AlertType.WARNING);
            dlg.setTitle("Budget Alert");
            dlg.setHeaderText("Budget " + alert.getStatus().toString().toLowerCase());
            dlg.setContentText(alert.getDisplayMessage(true));
            dlg.showAndWait();
        }
    }

    public void updateTransaction(Transaction tx) {
        transactionService.update(tx);
        refresh();
    }

    void build(Window owner) {
        Label title = new Label("Transactions");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #212121;");
        Label subtitle = new Label("Manage your income and expenses");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE_GREY + ";");

        Button addBtn = new Button("+ Add Transaction");
        addBtn.setId("add-transaction-btn");
        addBtn.setStyle("-fx-background-color: " + ACCENT_GREEN + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        addBtn.setOnAction(e -> AddTransactionModal.show(owner, this));

        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(4, title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        headerRow.getChildren().addAll(titleBox, addBtn);
        headerRow.setPadding(new Insets(20, 24, 16, 24));

        searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(200);
        searchField.setStyle("-fx-padding: 8 12;");
        categoryFilter = new ComboBox<>();
        categoryFilter.getItems().add("All Categories");
        for (Category c : Category.values()) {
            categoryFilter.getItems().add(c.getDisplayName());
        }
        categoryFilter.getSelectionModel().select(0);
        categoryFilter.setPrefWidth(160);
        typeFilter = new ComboBox<>();
        typeFilter.getItems().add("All Types");
        typeFilter.getItems().add("Expense");
        typeFilter.getItems().add("Income");
        typeFilter.getSelectionModel().select(0);
        typeFilter.setPrefWidth(120);
        dateFromField = new TextField();
        dateFromField.setPromptText("yyyy-mm-dd");
        dateFromField.setPrefWidth(110);
        dateToField = new TextField();
        dateToField.setPromptText("yyyy-mm-dd");
        dateToField.setPrefWidth(110);
        Label toLabel = new Label("to");
        toLabel.setStyle("-fx-text-fill: " + SUBTITLE_GREY + ";");

        // When any filter changes, re-run refresh() to update the table
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refresh());
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> refresh());
        typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> refresh());
        dateFromField.textProperty().addListener((obs, oldVal, newVal) -> refresh());
        dateToField.textProperty().addListener((obs, oldVal, newVal) -> refresh());

        HBox filters = new HBox(12, searchField, categoryFilter, typeFilter, dateFromField, toLabel, dateToField);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setPadding(new Insets(12, 16, 12, 16));
        filters.setStyle(CARD_STYLE);

        Label filterLabel = new Label("Filters");
        filterLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #424242;");
        VBox filterSection = new VBox(8, filterLabel, filters);
        filterSection.setPadding(new Insets(0, 24, 12, 24));

        table = new TableView<>();
        table.setPlaceholder(new Label("No transactions yet. Use \"+ Add Transaction\" to record one."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(CARD_STYLE);

        // Columns use setCellValueFactory to pull data from each Transaction; Type, Amount, and Actions use custom TableCells for styling/buttons
        TableColumn<Transaction, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(cb -> new SimpleStringProperty(cb.getValue().getDate() != null ? cb.getValue().getDate().toString() : ""));
        colDate.setPrefWidth(100);
        colDate.setMinWidth(80);

        TableColumn<Transaction, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(cb -> new SimpleStringProperty(cb.getValue().getNote() != null ? cb.getValue().getNote() : ""));
        colDesc.setCellFactory(tc -> new TableCell<>() {
            private final Label label = new Label();
            { label.setWrapText(true); label.setMaxWidth(Double.MAX_VALUE); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    label.setText(item);
                    setGraphic(label);
                }
            }
        });
        colDesc.setPrefWidth(180);
        colDesc.setMinWidth(100);

        TableColumn<Transaction, String> colCat = new TableColumn<>("Category");
        colCat.setCellValueFactory(cb -> new SimpleStringProperty(cb.getValue().getCategory() != null ? cb.getValue().getCategory().getDisplayName() : ""));
        colCat.setPrefWidth(120);
        colCat.setMinWidth(90);

        TableColumn<Transaction, TransactionType> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(cb -> new javafx.beans.property.SimpleObjectProperty<>(cb.getValue().getType()));
        colType.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(TransactionType type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(type == TransactionType.EXPENSE ? "expense" : "income");
                    setStyle("-fx-background-color: " + (type == TransactionType.EXPENSE ? EXPENSE_RED : INCOME_GREEN) + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 8; -fx-alignment: center;");
                    setAlignment(Pos.CENTER);
                }
            }
        });
        colType.setPrefWidth(90);
        colType.setMinWidth(70);

        TableColumn<Transaction, String> colAmount = new TableColumn<>("Amount");
        colAmount.setCellValueFactory(cb -> {
            Transaction t = cb.getValue();
            String sign = t.isIncome() ? "+" : "-";
            String amt = t.getAmount() != null ? t.getAmount().toPlainString() : "0";
            return new SimpleStringProperty(sign + "$" + amt);
        });
        colAmount.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: " + (item.startsWith("+") ? INCOME_GREEN : EXPENSE_RED) + "; -fx-font-weight: bold;");
                }
            }
        });
        colAmount.setPrefWidth(100);
        colAmount.setMinWidth(80);

        final TransactionsController controllerRef = this;
        TableColumn<Transaction, Transaction> colActions = new TableColumn<>("Actions");
        colActions.setCellValueFactory(cb -> new javafx.beans.property.SimpleObjectProperty<>(cb.getValue()));
        colActions.setCellFactory(tc -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button delBtn = new Button("Delete");
            {
                editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ACCENT_GREEN + "; -fx-cursor: hand;");
                delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + EXPENSE_RED + "; -fx-cursor: hand;");
            }
            @Override
            protected void updateItem(Transaction tx, boolean empty) {
                super.updateItem(tx, empty);
                if (empty || tx == null) {
                    setGraphic(null);
                } else {
                    editBtn.setOnAction(ev -> EditTransactionModal.show(owner, tx, controllerRef));
                    delBtn.setOnAction(ev -> {
                        String description = String.format("%s - %s - $%s",
                            tx.getDate() != null ? tx.getDate().toString() : "",
                            tx.getCategory() != null ? tx.getCategory().getDisplayName() : "",
                            tx.getAmount() != null ? tx.getAmount().toPlainString() : "0.00"
                        );
                        DeleteConfirmationModal.show(owner, "Transaction", description, () -> {
                            transactionService.delete(tx.getId());
                            controllerRef.refresh();
                        });
                    });
                    setGraphic(new HBox(8, editBtn, delBtn));
                }
            }
        });
        colActions.setPrefWidth(120);
        colActions.setMinWidth(100);

        table.getColumns().addAll(colDate, colDesc, colCat, colType, colAmount, colActions);

        VBox tableWrap = new VBox(table);
        tableWrap.setPadding(new Insets(0, 24, 24, 24));
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox main = new VBox(headerRow, filterSection, tableWrap);
        VBox.setVgrow(tableWrap, Priority.ALWAYS);
        main.setStyle("-fx-background-color: #f0f2f5;");
        pane = new BorderPane(main);
        refresh();
    }

    void refresh() {
        if (table == null) return;
        // ComboBox has "All Categories" at index 0, then Category enum order; index 0 means no filter
        Category cat = null;
        if (categoryFilter != null && categoryFilter.getSelectionModel().getSelectedIndex() > 0) {
            int idx = categoryFilter.getSelectionModel().getSelectedIndex();
            cat = Category.values()[idx - 1];
        }
        TransactionType type = null;
        if (typeFilter != null && typeFilter.getSelectionModel().getSelectedIndex() > 0) {
            int idx = typeFilter.getSelectionModel().getSelectedIndex();
            // Filter order: 0=All, 1=Expense, 2=Income; enum is INCOME, EXPENSE so map explicitly
            type = (idx == 1) ? TransactionType.EXPENSE : TransactionType.INCOME;
        }
        LocalDate from = parseDate(dateFromField != null ? dateFromField.getText() : null);
        LocalDate to = parseDate(dateToField != null ? dateToField.getText() : null);
        String keyword = (searchField != null && searchField.getText() != null && !searchField.getText().trim().isEmpty()) ? searchField.getText().trim() : null;
        TransactionFilter filter = new TransactionFilter(cat, type, from, to, keyword);
        List<Transaction> list = transactionService.filter(filter);
        table.getItems().setAll(list);
    }

    private static LocalDate parseDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
