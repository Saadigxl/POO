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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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

    @Override
    public void start(Stage primaryStage) {
        showWelcomeScreen(primaryStage);
    }

    private void showWelcomeScreen(Stage primaryStage) {
        StackPane welcomeRoot = new StackPane();
        welcomeRoot.setStyle("-fx-background-color: #f5f7fa;");

        VBox contentBox = new VBox(30);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(50));

        // Modern logo/symbol
        Label logo = new Label("✓");
        logo.setStyle("-fx-font-size: 72px; -fx-text-fill: #4f46e5;");

        Label welcomeLabel = new Label("TaskFlow");
        welcomeLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label subtitle = new Label("Organize your work effortlessly");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b;");

        Button startButton = new Button("Begin");
        startButton.getStyleClass().add("welcome-button");
        startButton.setOnAction(e -> fadeTransition(contentBox, () -> showMainView(primaryStage)));

        contentBox.getChildren().addAll(logo, welcomeLabel, subtitle, startButton);
        welcomeRoot.getChildren().add(contentBox);

        Scene welcomeScene = new Scene(welcomeRoot, 800, 600);
        primaryStage.setScene(welcomeScene);
        primaryStage.setTitle("TaskFlow");
        welcomeScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.show();
    }

    private void showMainView(Stage primaryStage) {
        User user = new User("JohnDoe");
        taskController = new TaskController(user, new DatabasePersistence());
        taskList = FXCollections.observableArrayList(taskController.getAllTasks());

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #ffffff;");

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1px 0;");

        Label title = new Label("TaskFlow");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        HBox.setHgrow(header, Priority.ALWAYS);
        header.getChildren().add(title);

        // Main content
        HBox mainContent = new HBox(40);
        mainContent.setPadding(new Insets(40));

        // Form section
        VBox formSection = new VBox(25);
        formSection.setPadding(new Insets(30));
        formSection.setStyle("-fx-background-color: #f8fafc; -fx-border-radius: 12px;");
        formSection.setPrefWidth(400);

        Label formTitle = new Label("New Task");
        formTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        GridPane formGrid = new GridPane();
        formGrid.setVgap(15);
        formGrid.setHgap(15);

        // Form fields
        titleField = createMinimalTextField();
        descriptionField = createMinimalTextField();
        dueDatePicker = createMinimalDatePicker();
        priorityComboBox = createMinimalComboBox(FXCollections.observableArrayList("High", "Medium", "Low"));
        categoryField = createMinimalTextField();
        statusComboBox = createMinimalComboBox(FXCollections.observableArrayList("To Do", "In Progress", "Done"));

        formGrid.add(createMinimalLabel("Title"), 0, 0);
        formGrid.add(titleField, 1, 0);
        formGrid.add(createMinimalLabel("Description"), 0, 1);
        formGrid.add(descriptionField, 1, 1);
        formGrid.add(createMinimalLabel("Due Date"), 0, 2);
        formGrid.add(dueDatePicker, 1, 2);
        formGrid.add(createMinimalLabel("Priority"), 0, 3);
        formGrid.add(priorityComboBox, 1, 3);
        formGrid.add(createMinimalLabel("Category"), 0, 4);
        formGrid.add(categoryField, 1, 4);
        formGrid.add(createMinimalLabel("Status"), 0, 5);
        formGrid.add(statusComboBox, 1, 5);

        // Action buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        addButton = createMinimalButton("Add Task", "primary");
        addButton.setOnAction(e -> handleAddTask());

        Button editButton = createMinimalButton("Update", "secondary");
        editButton.setOnAction(e -> handleEditTask());

        Button deleteButton = createMinimalButton("Delete", "danger");
        deleteButton.setOnAction(e -> handleDeleteTask());

        buttonBox.getChildren().addAll(addButton, editButton, deleteButton);

        formSection.getChildren().addAll(formTitle, formGrid, buttonBox);

        // Task list section
        VBox listSection = new VBox(20);

        Label listTitle = new Label("Your Tasks");
        listTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        taskListView = new ListView<>(taskList);
        taskListView.setId("minimal-task-list");
        taskListView.setOnMouseClicked(e -> populateFields());

        listSection.getChildren().addAll(listTitle, taskListView);

        mainContent.getChildren().addAll(formSection, listSection);

        root.setTop(header);
        root.setCenter(mainContent);

        Scene mainScene = new Scene(root, 1200, 800);
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("TaskFlow");
        mainScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    }

    private TextField createMinimalTextField() {
        TextField field = new TextField();
        field.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-padding: 10px;");
        return field;
    }

    private DatePicker createMinimalDatePicker() {
        DatePicker picker = new DatePicker();
        picker.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");
        return picker;
    }

    private ComboBox<String> createMinimalComboBox(ObservableList<String> items) {
        ComboBox<String> combo = new ComboBox<>(items);
        combo.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");
        return combo;
    }

    private Label createMinimalLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");
        return label;
    }

    private Button createMinimalButton(String text, String type) {
        Button button = new Button(text);
        button.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 6px;");

        switch (type) {
            case "primary":
                button.setStyle(button.getStyle() + "-fx-background-color: #4f46e5; -fx-text-fill: white;");
                break;
            case "secondary":
                button.setStyle(button.getStyle() + "-fx-background-color: #e2e8f0; -fx-text-fill: #1e293b;");
                break;
            case "danger":
                button.setStyle(button.getStyle() + "-fx-background-color: #fef2f2; -fx-text-fill: #dc2626; -fx-border-color: #dc2626; -fx-border-width: 1px;");
                break;
        }

        button.setOnMouseEntered(e -> {
            if (type.equals("primary")) {
                button.setStyle(button.getStyle() + "-fx-background-color: #4338ca;");
            } else if (type.equals("secondary")) {
                button.setStyle(button.getStyle() + "-fx-background-color: #cbd5e1;");
            } else {
                button.setStyle(button.getStyle() + "-fx-background-color: #fee2e2;");
            }
        });

        button.setOnMouseExited(e -> {
            if (type.equals("primary")) {
                button.setStyle(button.getStyle() + "-fx-background-color: #4f46e5;");
            } else if (type.equals("secondary")) {
                button.setStyle(button.getStyle() + "-fx-background-color: #e2e8f0;");
            } else {
                button.setStyle(button.getStyle() + "-fx-background-color: #fef2f2;");
            }
        });

        return button;
    }

    // ... (rest of your existing methods remain unchanged)
    private void fadeTransition(VBox layout, Runnable onFinish) {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), layout);
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
            alert.setTitle("Required Fields");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in all fields.");
            alert.showAndWait();
            return false;
        }
        return true;
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
            selectedTask.setStatus("Done");
            taskController.updateTask(selectedTask);
            taskListView.refresh();
        }
    }

    private void clearFields() {
        titleField.clear();
        descriptionField.clear();
        dueDatePicker.setValue(null);
        priorityComboBox.setValue(null);
        categoryField.clear();
        statusComboBox.setValue(null);
    }
}