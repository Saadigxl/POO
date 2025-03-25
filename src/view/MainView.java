package view;

import controller.TaskController;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Task;
import model.User;
import persistence.DatabasePersistence;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import java.time.LocalDate;

public class MainView extends Application {
    private TaskController taskController;
    private ObservableList<Task> taskList;
    private ListView<Task> taskListView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialize the controller with persistence
        taskController = new TaskController(new User("JohnDoe"), new DatabasePersistence());
        taskList = FXCollections.observableArrayList(taskController.getAllTasks());
        taskListView = new ListView<>(taskList);

        // Create UI components
        Label titleLabel = new Label("Titre:");
        TextField titleField = new TextField();
        Label descriptionLabel = new Label("Description:");
        TextField descriptionField = new TextField();
        Label dueDateLabel = new Label("Date limite:");
        DatePicker dueDatePicker = new DatePicker();
        Label priorityLabel = new Label("Priorité:");
        ComboBox<String> priorityComboBox = new ComboBox<>(FXCollections.observableArrayList("Haute", "Moyenne", "Basse"));
        Label categoryLabel = new Label("Catégorie:");
        TextField categoryField = new TextField();
        Label statusLabel = new Label("Statut:");
        ComboBox<String> statusComboBox = new ComboBox<>(FXCollections.observableArrayList("À faire", "En cours", "Terminé"));

        Button addButton = new Button("Ajouter une tâche");
        Button editButton = new Button("Modifier la tâche");
        Button deleteButton = new Button("Supprimer la tâche");

        // Layout setup
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);
        inputGrid.setPadding(new Insets(20));
        inputGrid.addRow(0, titleLabel, titleField);
        inputGrid.addRow(1, descriptionLabel, descriptionField);
        inputGrid.addRow(2, dueDateLabel, dueDatePicker);
        inputGrid.addRow(3, priorityLabel, priorityComboBox);
        inputGrid.addRow(4, categoryLabel, categoryField);
        inputGrid.addRow(5, statusLabel, statusComboBox);
        inputGrid.add(addButton, 1, 6);

        HBox buttonBox = new HBox(10, editButton, deleteButton);
        buttonBox.setPadding(new Insets(10));

        VBox root = new VBox(10, inputGrid, buttonBox, taskListView);
        root.setPadding(new Insets(20));

        // UI Effects
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        applyButtonHoverAnimation(addButton);
        applyButtonHoverAnimation(editButton);
        applyButtonHoverAnimation(deleteButton);

        // Apply styles
        inputGrid.getStyleClass().add("grid-pane");
        root.getStyleClass().add("vbox");
        buttonBox.getStyleClass().add("hbox");

        // Event Handlers
        addButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            String description = descriptionField.getText().trim();
            LocalDate dueDate = dueDatePicker.getValue();
            String priority = priorityComboBox.getValue();
            String category = categoryField.getText().trim();
            String status = statusComboBox.getValue();

            if (title.isEmpty() || dueDate == null || priority == null || category.isEmpty() || status == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs.");
                return;
            }

            Task newTask = new Task(0, title, description, dueDate, priority, category, status);
            taskController.addTask(title, description, dueDate, priority, category, status);
            taskList.setAll(taskController.getAllTasks());
            clearFields(titleField, descriptionField, dueDatePicker, priorityComboBox, categoryField, statusComboBox);
        });

        editButton.setOnAction(e -> {
            Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
            if (selectedTask == null) {
                showAlert("Erreur", "Veuillez sélectionner une tâche à modifier.");
                return;
            }

            // Update the selected task
            selectedTask.setTitle(titleField.getText().trim());
            selectedTask.setDescription(descriptionField.getText().trim());
            selectedTask.setDueDate(dueDatePicker.getValue());
            selectedTask.setPriority(priorityComboBox.getValue());
            selectedTask.setCategory(categoryField.getText().trim());
            selectedTask.setStatus(statusComboBox.getValue());

            taskController.updateTask(selectedTask);
            taskListView.refresh();
        });

        deleteButton.setOnAction(e -> {
            Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
            if (selectedTask == null) {
                showAlert("Erreur", "Veuillez sélectionner une tâche à supprimer.");
                return;
            }

            taskController.removeTask(selectedTask);
            taskList.setAll(taskController.getAllTasks());
        });

        // Populate fields when a task is selected
        taskListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                titleField.setText(newSelection.getTitle());
                descriptionField.setText(newSelection.getDescription());
                dueDatePicker.setValue(newSelection.getDueDate());
                priorityComboBox.setValue(newSelection.getPriority());
                categoryField.setText(newSelection.getCategory());
                statusComboBox.setValue(newSelection.getStatus());
            }
        });

        // Set up the scene
        Scene scene = new Scene(root, 600, 500);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("Gestionnaire de Tâches");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields(TextField titleField, TextField descriptionField, DatePicker dueDatePicker, ComboBox<String> priorityComboBox, TextField categoryField, ComboBox<String> statusComboBox) {
        titleField.clear();
        descriptionField.clear();
        dueDatePicker.setValue(null);
        priorityComboBox.setValue(null);
        categoryField.clear();
        statusComboBox.setValue(null);
    }

    private void applyButtonHoverAnimation(Button button) {
        button.setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), button);
            scaleUp.setToX(1.1);
            scaleUp.setToY(1.1);
            scaleUp.play();
        });

        button.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), button);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();
        });
    }
}
