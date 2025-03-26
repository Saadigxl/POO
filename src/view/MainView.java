package view;

import controller.TaskController;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Task;
import model.User;
import persistence.DatabasePersistence;

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
        showWelcomeScreen(primaryStage);
    }

    private void showWelcomeScreen(Stage primaryStage) {
        VBox welcomeLayout = new VBox(20);
        welcomeLayout.setPadding(new Insets(50));
        welcomeLayout.setAlignment(Pos.CENTER);
        welcomeLayout.setStyle("-fx-background-color: black;");

        Label universityLabel = new Label("Université de Science et Technologie Houari Boumediene");
        universityLabel.setId("university-label");

        Label welcomeLabel = new Label("Projet du module POO2: Task Manager");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Button startButton = new Button("Commencer");
        startButton.setId("commencer-button");
        startButton.setOnAction(e -> fadeTransition(welcomeLayout, () -> showMainView(primaryStage)));

        Label teamLabel = new Label("Boussada, Gueddouche, Boumedienne, Baatout");
        teamLabel.setId("team-names");
        teamLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(2), welcomeLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        welcomeLayout.getChildren().addAll(universityLabel, teamLabel, welcomeLabel, startButton);
        Scene welcomeScene = new Scene(welcomeLayout, 600, 400);
        primaryStage.setScene(welcomeScene);
        primaryStage.setTitle("Bienvenue");
        primaryStage.show();
        String css = getClass().getResource("/styles.css").toExternalForm();
        welcomeScene.getStylesheets().add(css);

    }











    private void showMainView(Stage primaryStage) {
        User user = new User("JohnDoe");
        taskController = new TaskController(user, new DatabasePersistence());
        taskList = FXCollections.observableArrayList(taskController.getAllTasks());
        taskListView = new ListView<>(taskList);

        VBox root = setupMainUI();
        Scene mainScene = new Scene(root, 600, 500);
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Task Manager");
        String css = getClass().getResource("/styles.css").toExternalForm();
        mainScene.getStylesheets().add(css);

    }

    private VBox setupMainUI() {
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

        // Adding Event to 'addButton'
        addButton.setOnAction(e -> {
            String title = titleField.getText();
            String description = descriptionField.getText();
            LocalDate dueDate = dueDatePicker.getValue();
            String priority = priorityComboBox.getValue();
            String category = categoryField.getText();
            String status = statusComboBox.getValue();

            if (title.isEmpty() || description.isEmpty() || dueDate == null || priority == null || category.isEmpty() || status == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs.");
                return;
            }

            // Create Task and Add to Controller
            Task newTask = new Task(0, title, description, dueDate, priority, category, status);
            taskController.addTask(title, description, dueDate, priority, category, status);
            taskList.add(newTask);

            // Clear Input Fields
            titleField.clear();
            descriptionField.clear();
            dueDatePicker.setValue(null);
            priorityComboBox.setValue(null);
            categoryField.clear();
            statusComboBox.setValue(null);
        });

        // Delete Task Event
        deleteButton.setOnAction(e -> {
            Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
            if (selectedTask == null) {
                showAlert("Erreur", "Veuillez sélectionner une tâche à supprimer.");
                return;
            }
            taskController.removeTask(selectedTask);
            taskList.remove(selectedTask);
        });

        // Layout Grid
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

        return root;
        
    }

    private void fadeTransition(VBox layout, Runnable onFinish) {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), layout);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> onFinish.run());
        fadeOut.play();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
