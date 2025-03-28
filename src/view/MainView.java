package view;

import controller.TaskController;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Task;
import model.User;
import persistence.DatabasePersistence;

public class MainView extends Application {
    private TaskController taskController;
    private ObservableList<Task> taskList;
    private ListView<Task> taskListView;
    private TextField titleField, descriptionField, categoryField;
    private DatePicker dueDatePicker;
    private ComboBox<String> priorityComboBox, statusComboBox;
    private Button addButton;
    private Task selectedTask;

    public static void main(String[] args) {
        launch(args);
    }




    private void fadeTransition(VBox layout, Runnable onFinish) {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), layout);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> onFinish.run());
        fadeOut.play();
    }


    private void populateFields() {
        selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            titleField.setText(selectedTask.getTitle());
            descriptionField.setText(selectedTask.getDescription());
            dueDatePicker.setValue(selectedTask.getDueDate());
            priorityComboBox.setValue(selectedTask.getPriority());
            categoryField.setText(selectedTask.getCategory());
            statusComboBox.setValue(selectedTask.getStatus());
        }
    }
    


    private boolean validateFields() {
        if (titleField.getText().trim().isEmpty() ||
            descriptionField.getText().trim().isEmpty() ||
            dueDatePicker.getValue() == null ||
            priorityComboBox.getValue() == null ||
            categoryField.getText().trim().isEmpty() ||
            statusComboBox.getValue() == null) {
            
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champs obligatoires");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez remplir tous les champs.");
            alert.showAndWait();
            return false;
        }
        return true;
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
        welcomeScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    }

    private void showMainView(Stage primaryStage) {
        User user = new User("JohnDoe");
        taskController = new TaskController(user, new DatabasePersistence());
        taskList = FXCollections.observableArrayList(taskController.getAllTasks());
        taskListView = new ListView<>(taskList);
        taskListView.setOnMouseClicked(e -> populateFields());

        VBox root = setupMainUI();
        Scene mainScene = new Scene(root, 600, 500);
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Task Manager");
        mainScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    }

    private VBox setupMainUI() {
        titleField = new TextField();
        descriptionField = new TextField();
        dueDatePicker = new DatePicker();
        priorityComboBox = new ComboBox<>(FXCollections.observableArrayList("Haute", "Moyenne", "Basse"));
        categoryField = new TextField();
        statusComboBox = new ComboBox<>(FXCollections.observableArrayList("À faire", "En cours", "Terminé"));
    
        addButton = new Button("Ajouter");
        addButton.setOnAction(e -> handleAddTask());
    
        Button editButton = new Button("Modifier");
        editButton.setOnAction(e -> handleEditTask());
    
        Button deleteButton = new Button("Supprimer");
        deleteButton.setOnAction(e -> handleDeleteTask());
    
        Button completeButton = new Button("Marquer Terminé");
        completeButton.setOnAction(e -> handleCompleteTask());
    
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);
        inputGrid.setPadding(new Insets(20));
        inputGrid.addRow(0, new Label("Titre:"), titleField);
        inputGrid.addRow(1, new Label("Description:"), descriptionField);
        inputGrid.addRow(2, new Label("Date limite:"), dueDatePicker);
        inputGrid.addRow(3, new Label("Priorité:"), priorityComboBox);
        inputGrid.addRow(4, new Label("Catégorie:"), categoryField);
        inputGrid.addRow(5, new Label("Statut:"), statusComboBox);
        inputGrid.addRow(6, addButton, editButton);
        inputGrid.addRow(7, deleteButton, completeButton);
    
        taskListView = new ListView<>(taskList);
        taskListView.setOnMouseClicked(e -> populateFields());
    
        HBox layout = new HBox(60, inputGrid, taskListView);
        layout.setPadding(new Insets(20));
    
        return new VBox(layout);
    }
    

    private void handleAddTask() {
        if (validateFields()) {
            Task newTask = new Task(0, titleField.getText(), descriptionField.getText(), dueDatePicker.getValue(), 
                                    priorityComboBox.getValue(), categoryField.getText(), statusComboBox.getValue());
            taskController.addTask(newTask.getTitle(), newTask.getDescription(), newTask.getDueDate(),
                                   newTask.getPriority(), newTask.getCategory(), newTask.getStatus());
            taskList.add(newTask);
            clearFields();
        }
    }
    

    private void handleEditTask() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null && validateFields()) {
            selectedTask.setTitle(titleField.getText());
            selectedTask.setDescription(descriptionField.getText());
            selectedTask.setDueDate(dueDatePicker.getValue());
            selectedTask.setPriority(priorityComboBox.getValue());
            selectedTask.setCategory(categoryField.getText());
            selectedTask.setStatus(statusComboBox.getValue());
    
            taskController.updateTask(selectedTask);
            taskListView.refresh();
            clearFields();
        }
    }
    
    private void handleDeleteTask() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            taskController.deleteTask(selectedTask);
            taskList.remove(selectedTask);
            clearFields();
        }
    }
    
    private void handleCompleteTask() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            selectedTask.setStatus("Terminé");
            taskController.updateTask(selectedTask);
            taskListView.refresh();
        }
    }
    

    private boolean confirmDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer cette tâche ?", ButtonType.YES, ButtonType.NO);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void clearFields() {
        titleField.clear(); descriptionField.clear(); dueDatePicker.setValue(null);
        priorityComboBox.setValue(null); categoryField.clear(); statusComboBox.setValue(null);
    }
}
