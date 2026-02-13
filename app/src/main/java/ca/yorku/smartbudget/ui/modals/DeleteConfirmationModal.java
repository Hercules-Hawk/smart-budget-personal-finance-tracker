package ca.yorku.smartbudget.ui.modals;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * A reusable confirmation modal for delete actions.
 * Shows a warning message and requires user confirmation before proceeding.
 */
public final class DeleteConfirmationModal {
    private static final String ACCENT_RED = "#ef4444";
    private static final String SUBTITLE_GREY = "#6b7280";

    /**
     * Shows a delete confirmation modal.
     *
     * @param owner The parent window
     * @param itemType The type of item being deleted (e.g., "Transaction", "Budget")
     * @param itemDescription A brief description of the item
     * @param onConfirm Callback to execute if user confirms deletion
     */
    public static void show(Window owner, String itemType, String itemDescription, Runnable onConfirm) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Confirm Delete");

        Label title = new Label("Delete " + itemType + "?");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + ACCENT_RED + ";");

        Label message = new Label("Are you sure you want to delete this " + itemType.toLowerCase() + "?");
        message.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
        message.setWrapText(true);

        Label itemLabel = new Label(itemDescription);
        itemLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE_GREY + "; -fx-font-style: italic;");
        itemLabel.setWrapText(true);

        Label warning = new Label("This action cannot be undone.");
        warning.setStyle("-fx-font-size: 12px; -fx-text-fill: " + ACCENT_RED + "; -fx-font-weight: bold;");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: white; -fx-border-color: #9e9e9e; -fx-padding: 8 16;");
        cancelBtn.setOnAction(e -> stage.close());

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: " + ACCENT_RED + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
        deleteBtn.setOnAction(e -> {
            onConfirm.run();
            stage.close();
        });

        HBox buttons = new HBox(12, cancelBtn, deleteBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(16, message, itemLabel, warning, buttons);
        content.setPadding(new Insets(20));

        VBox root = new VBox(12, title, content);
        root.setPadding(new Insets(20));
        root.setMinWidth(400);
        root.setStyle("-fx-background-color: white;");

        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    }
}

