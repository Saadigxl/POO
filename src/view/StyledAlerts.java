package view;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class StyledAlerts {
    public static void showInformationAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        styleAlert(alert, title, message);
        alert.showAndWait();
    }

    public static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        styleAlert(alert, title, message);
        alert.showAndWait();
    }

    private static void styleAlert(Alert alert, String title, String message) {
        alert.setTitle(title);
        alert.setHeaderText(null);

        // Custom content area with styled label
        Label contentLabel = new Label(message);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b; -fx-wrap-text: true;");
        alert.getDialogPane().setContent(contentLabel);

        // Style the dialog pane
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1px;");
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);

        // Style buttons
        for (ButtonType buttonType : dialogPane.getButtonTypes()) {
            Button button = (Button) dialogPane.lookupButton(buttonType);
            button.setStyle("-fx-background-color: #4f46e5; " +
                    "-fx-text-fill: white; -fx-font-size: 14px; " +
                    "-fx-padding: 8 16; -fx-background-radius: 4px;");

            button.setOnMouseEntered(e ->
                    button.setStyle("-fx-background-color: #4338ca; " +
                            "-fx-text-fill: white; -fx-font-size: 14px; " +
                            "-fx-padding: 8 16; -fx-background-radius: 4px;")
            );

            button.setOnMouseExited(e ->
                    button.setStyle("-fx-background-color: #4f46e5; " +
                            "-fx-text-fill: white; -fx-font-size: 14px; " +
                            "-fx-padding: 8 16; -fx-background-radius: 4px;")
            );
        }
    }
}