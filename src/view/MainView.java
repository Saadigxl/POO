package view;

import controller.TaskController;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Task;
import model.User;
import persistence.DatabasePersistence;
import java.util.Collections;
import java.util.List;

public class MainView extends Application {
    private TaskController taskController;
    private ObservableList<Task> taskList;
    private ListView<Task> taskListView;
    private TextField titleField, descriptionField, categoryField;
    private DatePicker dueDatePicker;
    private ComboBox<String> priorityComboBox, statusComboBox;
    private Button addButton;
    private VBox detailsSection;
    private VBox listSection;
    private Timeline resizeAnimation;
    private TranslateTransition slideAnimation;
    private Task currentlyDisplayedTask;
    private Task selectedTask;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Set application icon
        try {
            // Try different approaches to load the icon
            Image appIcon = null;
            
            // Approach 1: Try direct file path
            try {
                appIcon = new Image("file:src/main/resources/images/app_icon.png");
            } catch (Exception e) {
                System.err.println("Approach 1 failed: " + e.getMessage());
            }
            
            // Approach 2: Try resource stream
            if (appIcon == null) {
                try {
                    var stream = getClass().getResourceAsStream("/images/app_icon.png");
                    if (stream != null) {
                        appIcon = new Image(stream);
                    }
                } catch (Exception e) {
                    System.err.println("Approach 2 failed: " + e.getMessage());
                }
            }
            
            // Approach 3: Try relative path
            if (appIcon == null) {
                try {
                    appIcon = new Image("src/main/resources/images/app_icon.png");
                } catch (Exception e) {
                    System.err.println("Approach 3 failed: " + e.getMessage());
                }
            }

            if (appIcon != null && !appIcon.isError()) {
                primaryStage.getIcons().add(appIcon);
                System.out.println("Icon loaded successfully!");
            } else {
                System.err.println("Could not load application icon using any approach");
            }
        } catch (Exception e) {
            System.err.println("Error setting application icon: " + e.getMessage());
            e.printStackTrace();
        }
        
        showWelcomeScreen(primaryStage);
    }

    private void showWelcomeScreen(Stage primaryStage) {
        StackPane welcomeRoot = new StackPane();
        welcomeRoot.setStyle("-fx-background-color: #f5f7fa;");

        VBox contentBox = new VBox(30);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(50));

        // Modern logo/symbol and title in one row
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER);

        Label logo = new Label("✓");
        logo.setStyle("-fx-font-size: 36px; -fx-text-fill: #4f46e5;");

        Label welcomeLabel = new Label("TaskFlow");
        welcomeLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        titleBox.getChildren().addAll(logo, welcomeLabel);

        Label subtitle = new Label("Organize your work effortlessly");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b;");

        Button startButton = new Button("Begin");
        startButton.getStyleClass().add("welcome-button");
        startButton.setOnAction(e -> fadeTransition(contentBox, () -> showMainView(primaryStage)));

        contentBox.getChildren().addAll(titleBox, subtitle, startButton);
        welcomeRoot.getChildren().add(contentBox);

        Scene welcomeScene = new Scene(welcomeRoot, 800, 600);
        primaryStage.setScene(welcomeScene);
        primaryStage.setTitle("TaskFlow");
        welcomeScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(welcomeScene);
        primaryStage.show();
    }

    private void showMainView(Stage primaryStage) {
        User user = new User("JohnDoe");
        taskController = new TaskController(user, new DatabasePersistence());
        taskList = FXCollections.observableArrayList();
        // Load tasks in reverse order initially
        List<Task> tasks = taskController.getAllTasks();
        Collections.reverse(tasks);
        taskList.addAll(tasks);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #ffffff;");

        // Header
        HBox header = new HBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1px 0;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("TaskFlow");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        HBox.setHgrow(header, Priority.ALWAYS);
        header.getChildren().add(title);

        // Main content
        HBox mainContent = new HBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.CENTER);

        // Form section - New Task Creation
        VBox formSection = new VBox(20);
        formSection.setPadding(new Insets(20));
        formSection.setStyle("-fx-background-color: #f8fafc; -fx-border-radius: 12px;");
        formSection.setPrefWidth(320);
        formSection.setMinWidth(320);

        Label formTitle = new Label("New Task");
        formTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        GridPane formGrid = new GridPane();
        formGrid.setVgap(15);
        formGrid.setHgap(15);

        // Form fields for new task
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

        // Add button in a centered container
        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER);
        addButton = createMinimalButton("Add Task", "primary");
        addButton.setOnAction(e -> handleAddTask());
        buttonContainer.getChildren().add(addButton);

        formSection.getChildren().addAll(formTitle, formGrid, buttonContainer);

        // Task list section

        VBox listSection = new VBox(20);
        listSection.setPrefWidth(500);

        listSection = new VBox(20);
        listSection.setPrefWidth(500);
        listSection.setMinWidth(500);
        listSection.setStyle("-fx-background-color: #f8fafc; -fx-border-radius: 12px; -fx-padding: 20;");

        // Create horizontal box for title and search
        HBox titleSearchBox = new HBox(15);
        titleSearchBox.setAlignment(Pos.CENTER_LEFT);

        Label listTitle = new Label("Your Tasks");
        listTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Add search box
        TextField searchBox = new TextField();
        searchBox.setPromptText("Search tasks...");
        searchBox.setPrefWidth(200);
        searchBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-padding: 8px;");
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                taskListView.setItems(taskList);
            } else {
                ObservableList<Task> filteredList = FXCollections.observableArrayList();
                for (Task task : taskList) {
                    if (task.getTitle().toLowerCase().contains(newValue.toLowerCase()) ||
                        task.getDescription().toLowerCase().contains(newValue.toLowerCase()) ||
                        task.getCategory().toLowerCase().contains(newValue.toLowerCase())) {
                        filteredList.add(task);
                    }
                }
                taskListView.setItems(filteredList);
            }
        });

        // Add spacer to push search box to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Add title, spacer, and search box to horizontal layout
        titleSearchBox.getChildren().addAll(listTitle, spacer, searchBox);

        taskListView = new ListView<>(taskList);
        taskListView.setId("minimal-task-list");
        taskListView.setCellFactory(lv -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox taskBox = new VBox(5);
                    taskBox.setPadding(new Insets(10));
                    
                    Label titleLabel = new Label(task.getTitle());
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    
                    Label detailsLabel = new Label(
                        String.format("Due: %s • Priority: %s • Status: %s",
                            task.getDueDate(), task.getPriority(), task.getStatus())
                    );
                    detailsLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
                    
                    taskBox.getChildren().addAll(titleLabel, detailsLabel);
                    setGraphic(taskBox);
                }
            }
        });

        // Details section
        detailsSection = new VBox(20);
        detailsSection.setPrefWidth(400);
        detailsSection.setMinWidth(400);
        detailsSection.setStyle("-fx-background-color: #f8fafc; -fx-border-radius: 12px; -fx-padding: 20;");
        detailsSection.setVisible(false);
        detailsSection.setTranslateX(400); // Start outside the view
        detailsSection.setOpacity(0);      // Start transparent

        taskListView.setOnMouseClicked(e -> {
            Task clickedTask = taskListView.getSelectionModel().getSelectedItem();
            if (clickedTask != null) {
                if (clickedTask == currentlyDisplayedTask) {
                    hideDetailsWithAnimation();
                    currentlyDisplayedTask = null;
                    taskListView.getSelectionModel().clearSelection();
                } else {
                    showDetailsWithAnimation(clickedTask);
                    currentlyDisplayedTask = clickedTask;
                }
            }
        });

        listSection.getChildren().addAll(titleSearchBox, taskListView);

        mainContent.getChildren().addAll(formSection, listSection, detailsSection);

        root.setTop(header);
        root.setCenter(mainContent);

        Scene mainScene = new Scene(root, 1200, 800);
        mainScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(mainScene);
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
            taskList.add(0, newTask);
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

    private void showTaskDetails(Task task) {
        detailsSection.getChildren().clear();
        detailsSection.setVisible(true);

        Label detailsTitle = new Label("Task Details");
        detailsTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Task details form
        GridPane detailsGrid = new GridPane();
        detailsGrid.setVgap(15);
        detailsGrid.setHgap(15);

        TextField titleField = createMinimalTextField();
        TextField descriptionField = createMinimalTextField();
        DatePicker dueDatePicker = createMinimalDatePicker();
        ComboBox<String> priorityComboBox = createMinimalComboBox(
            FXCollections.observableArrayList("High", "Medium", "Low"));
        TextField categoryField = createMinimalTextField();
        ComboBox<String> statusComboBox = createMinimalComboBox(
            FXCollections.observableArrayList("To Do", "In Progress", "Done"));

        // Populate fields with task data
        titleField.setText(task.getTitle());
        descriptionField.setText(task.getDescription());
        dueDatePicker.setValue(task.getDueDate());
        priorityComboBox.setValue(task.getPriority());
        categoryField.setText(task.getCategory());
        statusComboBox.setValue(task.getStatus());

        detailsGrid.add(createMinimalLabel("Title"), 0, 0);
        detailsGrid.add(titleField, 1, 0);
        detailsGrid.add(createMinimalLabel("Description"), 0, 1);
        detailsGrid.add(descriptionField, 1, 1);
        detailsGrid.add(createMinimalLabel("Due Date"), 0, 2);
        detailsGrid.add(dueDatePicker, 1, 2);
        detailsGrid.add(createMinimalLabel("Priority"), 0, 3);
        detailsGrid.add(priorityComboBox, 1, 3);
        detailsGrid.add(createMinimalLabel("Category"), 0, 4);
        detailsGrid.add(categoryField, 1, 4);
        detailsGrid.add(createMinimalLabel("Status"), 0, 5);
        detailsGrid.add(statusComboBox, 1, 5);

        // Action buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button updateButton = createMinimalButton("Update", "primary");
        Button deleteButton = createMinimalButton("Delete", "danger");

        updateButton.setOnAction(e -> {
            int taskIndex = taskList.indexOf(task);
            task.setTitle(titleField.getText());
            task.setDescription(descriptionField.getText());
            task.setDueDate(dueDatePicker.getValue());
            task.setPriority(priorityComboBox.getValue());
            task.setCategory(categoryField.getText());
            task.setStatus(statusComboBox.getValue());
            taskController.updateTask(task);
            
            // Update the task in the same position
            if (taskIndex >= 0) {
                taskList.set(taskIndex, task);
            }
            taskListView.refresh();
            hideDetailsWithAnimation();
        });

        deleteButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setTitle("Delete Task");
            alert.setHeaderText("Are you sure you want to delete this task?");
            alert.setContentText("This action cannot be undone.");
            
            ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
            
            alert.showAndWait().ifPresent(response -> {
                if (response == buttonTypeYes) {
                    taskController.deleteTask(task);
                    taskList.remove(task);  // Simply remove the task from the observable list
                    hideDetailsWithAnimation();
                }
            });
        });

        buttonBox.getChildren().addAll(updateButton, deleteButton);

        detailsSection.getChildren().addAll(detailsTitle, detailsGrid, buttonBox);
    }

    private void showDetailsWithAnimation(Task task) {
        if (resizeAnimation != null) {
            resizeAnimation.stop();
        }
        if (slideAnimation != null) {
            slideAnimation.stop();
        }

        detailsSection.setVisible(true);
        showTaskDetails(task);

        // Slide in animation
        slideAnimation = new TranslateTransition(Duration.millis(300), detailsSection);
        slideAnimation.setFromX(400); // Start from outside the view
        slideAnimation.setToX(0);     // Slide to original position

        // Width animation for task list
        double startWidth = listSection.getPrefWidth();
        double endWidth = 500;
        
        resizeAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(listSection.prefWidthProperty(), startWidth),
                new KeyValue(detailsSection.opacityProperty(), 0)
            ),
            new KeyFrame(Duration.millis(300), 
                new KeyValue(listSection.prefWidthProperty(), endWidth),
                new KeyValue(detailsSection.opacityProperty(), 1)
            )
        );
        
        // Play both animations together
        slideAnimation.play();
        resizeAnimation.play();
    }

    private void hideDetailsWithAnimation() {
        if (resizeAnimation != null) {
            resizeAnimation.stop();
        }
        if (slideAnimation != null) {
            slideAnimation.stop();
        }

        // Slide out animation
        slideAnimation = new TranslateTransition(Duration.millis(300), detailsSection);
        slideAnimation.setFromX(0);      // Start from current position
        slideAnimation.setToX(400);      // Slide out of view

        // Width animation for task list
        double startWidth = listSection.getPrefWidth();
        double endWidth = 600;
        
        resizeAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(listSection.prefWidthProperty(), startWidth),
                new KeyValue(detailsSection.opacityProperty(), 1)
            ),
            new KeyFrame(Duration.millis(300), 
                new KeyValue(listSection.prefWidthProperty(), endWidth),
                new KeyValue(detailsSection.opacityProperty(), 0)
            )
        );
        
        slideAnimation.setOnFinished(e -> detailsSection.setVisible(false));
        
        // Play both animations together
        slideAnimation.play();
        resizeAnimation.play();
    }
}