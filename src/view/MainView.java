package view;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controller.TaskController;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Task;
import model.User;
import persistence.DatabasePersistence;

public class MainView extends Application {
    private TaskController taskController;
    private ObservableList<Task> taskList;
    private ListView<Task> taskListView;
    private VBox detailsSection;
    private VBox mainContentArea;
    private Timeline resizeAnimation;
    public Timeline getResizeAnimation() {
        return resizeAnimation;
    }

    public void setResizeAnimation(Timeline resizeAnimation) {
        this.resizeAnimation = resizeAnimation;
    }

    private TranslateTransition slideAnimation;
    private Task currentlyDisplayedTask;
    private Task selectedTask;
    public Task getSelectedTask() {
        return selectedTask;
    }

    public void setSelectedTask(Task selectedTask) {
        this.selectedTask = selectedTask;
    }

    private Label todoCountLabel, inProgressCountLabel, doneCountLabel;
    private Map<String, Integer> statusCounts = new HashMap<>();
    private Map<String, Integer> priorityCounts = new HashMap<>();
    private TabPane contentTabPane;
    private model.User user;

    public void setUser(model.User user) {
        this.user = user;
    }

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
        BorderPane welcomeRoot = new BorderPane();
        welcomeRoot.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8fafc, #e0e7ff);");
    
        // Create main content container with vertical spacing
        VBox contentBox = new VBox(30);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(60, 50, 60, 50));
        contentBox.setMaxWidth(600);
    
        // Branding section with animated logo
        StackPane logoContainer = new StackPane();
        logoContainer.setPadding(new Insets(0, 0, 20, 0));
        
        Circle logoCircle = new Circle(50);
        logoCircle.setFill(Color.web("#4f46e5"));
        logoCircle.setOpacity(0.9);
        
        Label logoSymbol = new Label("✓");
        logoSymbol.setStyle("-fx-font-size: 52px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        logoContainer.getChildren().addAll(logoCircle, logoSymbol);
        
        // Rotate animation for the logo
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(3), logoCircle);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(1);
        rotateTransition.setInterpolator(Interpolator.EASE_BOTH);
        rotateTransition.play();
    
        // Title and tagline with modern typography
        Label welcomeLabel = new Label("TaskFlow");
        welcomeLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Label subtitle = new Label("Organize your tasks. Boost your productivity.");
        subtitle.setStyle("-fx-font-size: 18px; -fx-text-fill: #64748b; -fx-font-weight: normal;");
    
        // Feature highlights section
        HBox featureBox = new HBox(40);
        featureBox.setAlignment(Pos.CENTER);
        featureBox.setPadding(new Insets(30, 0, 40, 0));
        
        // Feature 1: Track Tasks
        VBox feature1 = createFeatureBox("✓", "Track Tasks", "Organize and manage all your tasks in one place");
        
        // Feature 2: Set Priorities
        VBox feature2 = createFeatureBox("⚡", "Set Priorities", "Prioritize tasks and focus on what's important");
        
        // Feature 3: Monitor Progress
        VBox feature3 = createFeatureBox("📊", "Monitor Progress", "Track your productivity and task completion rate");
        
        featureBox.getChildren().addAll(feature1, feature2, feature3);
    
        // Start button with hover effects
        Button startButton = new Button("Get Started");
        startButton.getStyleClass().add("welcome-button");
        startButton.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-size: 16px; " +
                            "-fx-padding: 12px 30px; -fx-background-radius: 30px; -fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(79, 70, 229, 0.3), 10, 0, 0, 4);");
        
        // Hover effects
        startButton.setOnMouseEntered(e -> 
            startButton.setStyle("-fx-background-color: #4338ca; -fx-text-fill: white; -fx-font-size: 16px; " +
                               "-fx-padding: 12px 30px; -fx-background-radius: 30px; -fx-cursor: hand; " +
                               "-fx-effect: dropshadow(gaussian, rgba(79, 70, 229, 0.4), 12, 0, 0, 6);")
        );
        
        startButton.setOnMouseExited(e -> 
            startButton.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-size: 16px; " +
                               "-fx-padding: 12px 30px; -fx-background-radius: 30px; -fx-cursor: hand; " +
                               "-fx-effect: dropshadow(gaussian, rgba(79, 70, 229, 0.3), 10, 0, 0, 4);")
        );
        
        startButton.setOnAction(e -> fadeTransition(contentBox, () -> showMainView(primaryStage)));
    
        // Footer with version info
        Label versionLabel = new Label("v1.0 • Modern Task Management");
        versionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-padding: 20 0 0 0;");
        
        // Add all elements to content box
        contentBox.getChildren().addAll(logoContainer, welcomeLabel, subtitle, featureBox, startButton, versionLabel);
        
        // Center the content in welcome screen
        welcomeRoot.setCenter(contentBox);
    
        Scene welcomeScene = new Scene(welcomeRoot, 900, 700);
        welcomeScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(welcomeScene);
        primaryStage.setTitle("Welcome to TaskFlow");
        primaryStage.show();
        
        // Add a gentle fade-in animation for the whole scene
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), welcomeRoot);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
    private VBox createFeatureBox(String icon, String title, String description) {
        VBox featureBox = new VBox(10);
        featureBox.setAlignment(Pos.TOP_CENTER);
        featureBox.setPrefWidth(180);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #4f46e5;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-wrap-text: true; -fx-text-alignment: center;");
        descLabel.setWrapText(true);
        
        featureBox.getChildren().addAll(iconLabel, titleLabel, descLabel);
        return featureBox;
    }

    private void showMainView(Stage primaryStage) {
        if (user == null) {
            // Should not happen, but fallback
            return;
        }
        taskController = new TaskController(user, new DatabasePersistence());
        taskList = FXCollections.observableArrayList();
        // Load only tasks for this user
        List<Task> tasks = taskController.getAllTasks(user.getId());
        Collections.reverse(tasks);
        taskList.addAll(tasks);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #ffffff;");

        // Header with counter indicators
        HBox header = createHeader();

        // Sidebar
        VBox sidebar = createSidebar(primaryStage);

        // Main content area with tabs
        createMainContentArea();

        root.setTop(header);
        root.setLeft(sidebar);
        root.setCenter(mainContentArea);

        Scene mainScene = new Scene(root, 1200, 800);
        mainScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(mainScene);

        // Ensure counters are updated after UI is built and tasks are loaded
        updateStatusCounts();
        updatePriorityCounts();
    }
    
    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1px 0;");
        header.setAlignment(Pos.CENTER_LEFT);

        // Title
        Label title = new Label("TaskFlow");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Spacer to push counters to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Task counter indicators
        HBox counters = new HBox(20);
        counters.setAlignment(Pos.CENTER);
        
        // To Do counter
        VBox todoCounter = createCounterBox("To Do", "#3b82f6");
        todoCountLabel = (Label) todoCounter.getChildren().get(0);
        
        // In Progress counter
        VBox inProgressCounter = createCounterBox("In Progress", "#f59e0b");
        inProgressCountLabel = (Label) inProgressCounter.getChildren().get(0);
        
        // Done counter
        VBox doneCounter = createCounterBox("Done", "#10b981");
        doneCountLabel = (Label) doneCounter.getChildren().get(0);
        
        counters.getChildren().addAll(todoCounter, inProgressCounter, doneCounter);
        
        header.getChildren().addAll(title, spacer, counters);
        return header;
    }
    
    private VBox createCounterBox(String status, String color) {
        VBox counterBox = new VBox(5);
        counterBox.setAlignment(Pos.CENTER);
        counterBox.setPadding(new Insets(5, 15, 5, 15));
        counterBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10px;");
        
        Label countLabel = new Label("0");
        countLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        Label statusLabel = new Label(status);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        
        counterBox.getChildren().addAll(countLabel, statusLabel);
        return counterBox;
    }
    
    private VBox createSidebar(Stage primaryStage) {
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(220);
        sidebar.setMinWidth(220);
        sidebar.setPadding(new Insets(20, 15, 20, 15));
        sidebar.setStyle("-fx-background-color: #f1f5f9;");
        
        // User profile section
        HBox userProfile = new HBox(10);
        userProfile.setAlignment(Pos.CENTER_LEFT);
        userProfile.setPadding(new Insets(0, 0, 20, 0));
        
        Circle userAvatar = new Circle(20, Color.web("#4f46e5"));
        Label userName = new Label(" " + (user != null ? user.getUsername() : ""));
        userName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        userProfile.getChildren().addAll(userAvatar, userName);
        
        // Navigation items
        VBox navItems = new VBox(5);
        navItems.setPadding(new Insets(10, 0, 10, 0));
        
        // Navigation buttons
        Button viewTasksBtn = createNavButton("View Tasks", true);
        Button priorityDashboardBtn = createNavButton("Priority Dashboard", false);
        Button addTaskBtn = createNavButton("Add New Task", false);
        
        viewTasksBtn.setOnAction(e -> {
            contentTabPane.getSelectionModel().select(0);
            setActiveButton(viewTasksBtn, priorityDashboardBtn, addTaskBtn);
        });
        
        priorityDashboardBtn.setOnAction(e -> {
            contentTabPane.getSelectionModel().select(1);
            setActiveButton(priorityDashboardBtn, viewTasksBtn, addTaskBtn);
        });
        
        addTaskBtn.setOnAction(e -> {
            showAddTaskDialog(primaryStage);
        });
        
        navItems.getChildren().addAll(viewTasksBtn, priorityDashboardBtn, addTaskBtn);
        
        // Statistics summary in sidebar
        VBox statusSummary = new VBox(15);
        statusSummary.setPadding(new Insets(20, 10, 20, 10));
        statusSummary.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px;");
        
        Label summaryTitle = new Label("Tasks Overview");
        summaryTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        VBox statusChart = createMiniStatusChart();
        
        statusSummary.getChildren().addAll(summaryTitle, statusChart);
        
        sidebar.getChildren().addAll(userProfile, navItems, statusSummary);
        return sidebar;
    }
    
    private void setActiveButton(Button activeButton, Button... otherButtons) {
        activeButton.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #4f46e5; -fx-font-weight: bold; -fx-background-radius: 6px;");
        
        for (Button button : otherButtons) {
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-background-radius: 6px;");
        }
    }
    
    private VBox createMiniStatusChart() {
        // This could be replaced with a small chart in a production app
        VBox chartContainer = new VBox(10);
        
        // For now, just display status distribution with colored indicators
        HBox todoBar = createStatusBar("To Do", "#3b82f6");
        HBox inProgressBar = createStatusBar("In Progress", "#f59e0b");
        HBox doneBar = createStatusBar("Done", "#10b981");
        
        chartContainer.getChildren().addAll(todoBar, inProgressBar, doneBar);
        return chartContainer;
    }
    
    private HBox createStatusBar(String status, String color) {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        
        Circle statusDot = new Circle(6, Color.web(color));
        Label statusLabel = new Label(status);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        
        bar.getChildren().addAll(statusDot, statusLabel);
        return bar;
    }
    
    private Button createNavButton(String text, boolean isActive) {
        Button button = new Button(text);
        button.setPrefWidth(190);
        button.setPadding(new Insets(12, 15, 12, 15));
        button.setAlignment(Pos.CENTER_LEFT);
        
        if (isActive) {
            button.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #4f46e5; -fx-font-weight: bold; -fx-background-radius: 6px;");
        } else {
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-background-radius: 6px;");
        }
        
        // Hover effects
        button.setOnMouseEntered(e -> {
            if (!isActive) {
                button.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #1e293b; -fx-background-radius: 6px;");
            }
        });
        
        button.setOnMouseExited(e -> {
            if (!isActive) {
                button.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-background-radius: 6px;");
            }
        });
        
        return button;
    }
    
    private void createMainContentArea() {
        mainContentArea = new VBox(0);
        contentTabPane = new TabPane();
        contentTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        contentTabPane.getStyleClass().add("invisible-tab-header");
        
        // Task list tab
        Tab taskListTab = new Tab("Tasks");
        taskListTab.setContent(createTaskViewSection());
        
        // Priority dashboard tab
        Tab priorityDashboardTab = new Tab("Dashboard");
        priorityDashboardTab.setContent(createPriorityDashboard());
        
        contentTabPane.getTabs().addAll(taskListTab, priorityDashboardTab);
        mainContentArea.getChildren().add(contentTabPane);
    }
    
    
// Replace the createTaskViewSection() method with this card-based implementation
private VBox createTaskViewSection() {
    VBox taskViewSection = new VBox(20);
    taskViewSection.setPrefWidth(700);
    taskViewSection.setStyle("-fx-background-color: #f8fafc; -fx-border-radius: 12px; -fx-padding: 20;");
    
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

    // Custom ListView for card-style tasks
    taskListView = new ListView<>(taskList);
    taskListView.setId("card-task-list");
    taskListView.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 10;");
    
    
    // Custom cell factory for card-style tasks
    taskListView.setCellFactory(lv -> new ListCell<Task>() {
        @Override
        protected void updateItem(Task task, boolean empty) {
            super.updateItem(task, empty);
            if (empty || task == null) {
                setText(null);
                setGraphic(null);
                setStyle("-fx-background-color: transparent;");
            } else {
                // Create a card container
                VBox card = new VBox(12);
                card.setPadding(new Insets(16));
                card.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 5, 0, 0, 2); " +
                              "-fx-background-radius: 8px; -fx-border-radius: 8px;");
                
                // Header section with title and priority
                HBox header = new HBox();
                header.setAlignment(Pos.CENTER_LEFT);
                header.setSpacing(10);
                
                // Status indicator
                Circle statusIndicator = new Circle(8);
                switch (task.getStatus()) {
                    case "To Do":
                        statusIndicator.setFill(Color.web("#3b82f6"));
                        break;
                    case "In Progress":
                        statusIndicator.setFill(Color.web("#f59e0b"));
                        break;
                    case "Done":
                        statusIndicator.setFill(Color.web("#10b981"));
                        break;
                    default:
                        statusIndicator.setFill(Color.web("#cbd5e1"));
                }
                
                // Task title
                Label titleLabel = new Label(task.getTitle());
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1e293b;");
                titleLabel.setWrapText(true);
                HBox.setHgrow(titleLabel, Priority.ALWAYS);
                
                // Create the priority badge
                HBox priorityBadge = new HBox(5);
                priorityBadge.setPadding(new Insets(3, 8, 3, 8));
                priorityBadge.setAlignment(Pos.CENTER);
                
                String priorityColor;
                switch (task.getPriority()) {
                    case "High":
                        priorityColor = "#fee2e2";
                        priorityBadge.setStyle("-fx-background-color: " + priorityColor + "; -fx-background-radius: 12px;");
                        break;
                    case "Medium":
                        priorityColor = "#fef3c7";
                        priorityBadge.setStyle("-fx-background-color: " + priorityColor + "; -fx-background-radius: 12px;");
                        break;
                    case "Low":
                        priorityColor = "#d1fae5";
                        priorityBadge.setStyle("-fx-background-color: " + priorityColor + "; -fx-background-radius: 12px;");
                        break;
                    default:
                        priorityColor = "#f1f5f9";
                        priorityBadge.setStyle("-fx-background-color: " + priorityColor + "; -fx-background-radius: 12px;");
                }
                
                Label priorityLabel = new Label(task.getPriority());
                switch (task.getPriority()) {
                    case "High":
                        priorityLabel.setStyle("-fx-text-fill: #b91c1c; -fx-font-size: 12px; -fx-font-weight: bold;");
                        break;
                    case "Medium":
                        priorityLabel.setStyle("-fx-text-fill: #b45309; -fx-font-size: 12px; -fx-font-weight: bold;");
                        break;
                    case "Low":
                        priorityLabel.setStyle("-fx-text-fill: #047857; -fx-font-size: 12px; -fx-font-weight: bold;");
                        break;
                    default:
                        priorityLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px; -fx-font-weight: bold;");
                }
                
                priorityBadge.getChildren().add(priorityLabel);
                
                header.getChildren().addAll(statusIndicator, titleLabel, priorityBadge);
                
                // Description (truncated if too long)
                String displayDescription = task.getDescription();
                if (displayDescription.length() > 100) {
                    displayDescription = displayDescription.substring(0, 97) + "...";
                }
                
                Label descriptionLabel = new Label(displayDescription);
                descriptionLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
                descriptionLabel.setWrapText(true);
                
                // Footer with metadata
                HBox footer = new HBox(15);
                footer.setAlignment(Pos.CENTER_LEFT);
                footer.setPadding(new Insets(5, 0, 0, 0));
                
                // Due date label
                Label dueLabel = new Label("Due: " + task.getDueDate());
                dueLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
                
                // Category label
                Label categoryLabel = new Label(task.getCategory());
                categoryLabel.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #4f46e5; -fx-font-size: 12px; " +
                                      "-fx-background-radius: 12px; -fx-padding: 3 8 3 8;");
                
                // Status label
                Label statusLabel = new Label(task.getStatus());
                switch (task.getStatus()) {
                    case "To Do":
                        statusLabel.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8; -fx-font-size: 12px; " +
                                           "-fx-background-radius: 12px; -fx-padding: 3 8 3 8;");
                        break;
                    case "In Progress":
                        statusLabel.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #b45309; -fx-font-size: 12px; " +
                                           "-fx-background-radius: 12px; -fx-padding: 3 8 3 8;");
                        break;
                    case "Done":
                        statusLabel.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #047857; -fx-font-size: 12px; " +
                                           "-fx-background-radius: 12px; -fx-padding: 3 8 3 8;");
                        break;
                    default:
                        statusLabel.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-size: 12px; " +
                                           "-fx-background-radius: 12px; -fx-padding: 3 8 3 8;");
                }
                
                footer.getChildren().addAll(dueLabel, categoryLabel, statusLabel);
                
                card.getChildren().addAll(header, descriptionLabel, footer);
                
                // Add hover effects
                card.setOnMouseEntered(e -> {
                    card.setStyle("-fx-background-color: #f8fafc; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 8, 0, 0, 3); " +
                                 "-fx-background-radius: 8px; -fx-border-radius: 8px; -fx-cursor: hand;");
                });
                
                card.setOnMouseExited(e -> {
                    card.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 5, 0, 0, 2); " +
                                 "-fx-background-radius: 8px; -fx-border-radius: 8px;");
                });
                
                setGraphic(card);
                setPadding(new Insets(0, 0, 10, 0)); // Add spacing between cards
                setStyle("-fx-background-color: transparent;");
            }
        }
    });
    
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

    // Add some CSS for better appearance
    taskListView.getStyleClass().add("task-card-list");

    taskViewSection.getChildren().addAll(titleSearchBox, taskListView);
    return taskViewSection;
}
    
    private VBox createPriorityDashboard() {
        VBox dashboard = new VBox(30);
        dashboard.setPadding(new Insets(20));
        dashboard.setStyle("-fx-background-color: #f8fafc;");
        
        // Dashboard title
        Label dashboardTitle = new Label("Priority Dashboard");
        dashboardTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        // Priority distribution chart
        VBox chartSection = new VBox(15);
        chartSection.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12px; -fx-padding: 20;");
        
        Label chartTitle = new Label("Task Distribution by Priority");
        chartTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        // Create pie chart
        PieChart priorityChart = createPriorityPieChart();
        
        chartSection.getChildren().addAll(chartTitle, priorityChart);
        
        // Priority task sections
        HBox prioritySections = new HBox(20);
        prioritySections.setAlignment(Pos.CENTER);
        
        // High priority tasks
        VBox highPrioritySection = createPrioritySection("High Priority", "#ef4444");
        
        // Medium priority tasks
        VBox mediumPrioritySection = createPrioritySection("Medium Priority", "#f59e0b");
        
        // Low priority tasks
        VBox lowPrioritySection = createPrioritySection("Low Priority", "#10b981");
        
        prioritySections.getChildren().addAll(highPrioritySection, mediumPrioritySection, lowPrioritySection);
        
        dashboard.getChildren().addAll(dashboardTitle, chartSection, prioritySections);
        return dashboard;
    }
    
    private PieChart createPriorityPieChart() {
        // Create data for the chart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("High", priorityCounts.getOrDefault("High", 0)),
            new PieChart.Data("Medium", priorityCounts.getOrDefault("Medium", 0)),
            new PieChart.Data("Low", priorityCounts.getOrDefault("Low", 0))
        );
        
        PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Tasks by Priority");
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setStartAngle(90);
        
        // Apply colors
        pieChartData.get(0).getNode().setStyle("-fx-pie-color: #ef4444;");
        pieChartData.get(1).getNode().setStyle("-fx-pie-color: #f59e0b;");
        pieChartData.get(2).getNode().setStyle("-fx-pie-color: #10b981;");
        
        return chart;
    }
    
    private VBox createPrioritySection(String priority, String color) {
        VBox section = new VBox(15);
        section.setPrefWidth(300);
        section.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12px; -fx-padding: 20;");
        
        // Section header with count
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Circle priorityDot = new Circle(8, Color.web(color));
        
        Label priorityLabel = new Label(priority);
        priorityLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Label countLabel = new Label(String.valueOf(priorityCounts.getOrDefault(priority.split(" ")[0], 0)));
        countLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        header.getChildren().addAll(priorityDot, priorityLabel, spacer, countLabel);
        
        // Task list for this priority
        VBox tasksList = new VBox(10);
        
        // Get tasks with this priority
        String priorityKey = priority.split(" ")[0]; // Extract "High", "Medium", or "Low"
        for (Task task : taskList) {
            if (task.getPriority().equals(priorityKey)) {
                HBox taskItem = new HBox(10);
                taskItem.setAlignment(Pos.CENTER_LEFT);
                taskItem.setPadding(new Insets(8));
                taskItem.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 6px;");
                
                // Status indicator
                Circle statusDot = new Circle(6);
                switch (task.getStatus()) {
                    case "To Do":
                        statusDot.setFill(Color.web("#3b82f6"));
                        break;
                    case "In Progress":
                        statusDot.setFill(Color.web("#f59e0b"));
                        break;
                    case "Done":
                        statusDot.setFill(Color.web("#10b981"));
                        break;
                    default:
                        statusDot.setFill(Color.web("#cbd5e1"));
                }
                
                Label taskTitle = new Label(task.getTitle());
                taskTitle.setStyle("-fx-font-size: 14px;");
                
                taskItem.getChildren().addAll(statusDot, taskTitle);
                tasksList.getChildren().add(taskItem);
            }
        }
        
        // If no tasks, show message
        if (tasksList.getChildren().isEmpty()) {
            Label noTasksLabel = new Label("No " + priority.toLowerCase() + " tasks");
            noTasksLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
            tasksList.getChildren().add(noTasksLabel);
        }
        
        section.getChildren().addAll(header, tasksList);
        return section;
    }
    
    private void showAddTaskDialog(Stage parentStage) {
        // Create a dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle("Add New Task");
        dialog.setMinWidth(400);
        dialog.setMinHeight(450);
        
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setStyle("-fx-background-color: #ffffff;");
        
        Label dialogTitle = new Label("Create New Task");
        dialogTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        // Form fields
        GridPane formGrid = new GridPane();
        formGrid.setVgap(15);
        formGrid.setHgap(15);
        
        TextField titleField = createMinimalTextField();
        TextField descriptionField = createMinimalTextField();
        DatePicker dueDatePicker = createMinimalDatePicker();
        ComboBox<String> priorityComboBox = createMinimalComboBox(
            FXCollections.observableArrayList("High", "Medium", "Low"));
        TextField categoryField = createMinimalTextField();
        ComboBox<String> statusComboBox = createMinimalComboBox(
            FXCollections.observableArrayList("To Do", "In Progress", "Done"));
        
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

        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelButton = createMinimalButton("Cancel", "secondary");
        Button saveButton = createMinimalButton("Save Task", "primary");
        
        cancelButton.setOnAction(e -> dialog.close());
        saveButton.setOnAction(e -> {
            if (validateFields(titleField, descriptionField, dueDatePicker, priorityComboBox, categoryField, statusComboBox)) {
                Task newTask = new Task(
                    0,
                    titleField.getText(),
                    descriptionField.getText(),
                    dueDatePicker.getValue(),
                    priorityComboBox.getValue(),
                    categoryField.getText(),
                    statusComboBox.getValue(),
                    user.getId() // <-- Set the userId here!
                );
                taskController.addTask(newTask);
                taskList.add(0, newTask);
                updateStatusCounts();
                updatePriorityCounts();
                
                // Refresh the priority dashboard
                contentTabPane.getTabs().get(1).setContent(createPriorityDashboard());
                
                dialog.close();
            }
        });
        
        buttonBox.getChildren().addAll(cancelButton, saveButton);
        
        dialogContent.getChildren().addAll(dialogTitle, formGrid, buttonBox);
                
        Scene dialogScene = new Scene(dialogContent);
        dialogScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.show();
            }
            
            private TextField createMinimalTextField() {
                TextField field = new TextField();
                field.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-padding: 8px;");
                return field;
            }
            
            private DatePicker createMinimalDatePicker() {
                DatePicker datePicker = new DatePicker();
                datePicker.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-padding: 8px;");
                return datePicker;
            }
            
            private ComboBox<String> createMinimalComboBox(ObservableList<String> items) {
                ComboBox<String> comboBox = new ComboBox<>(items);
                comboBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-padding: 8px;");
                return comboBox;
            }
            
            private Label createMinimalLabel(String text) {
                Label label = new Label(text);
                label.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
                return label;
            }
            
            private Button createMinimalButton(String text, String type) {
                Button button = new Button(text);
                if ("primary".equals(type)) {
                    button.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-border-radius: 6px; -fx-padding: 8px 16px;");
                } else {
                    button.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #64748b; -fx-border-radius: 6px; -fx-padding: 8px 16px;");
                }
                
                // Hover effects
                button.setOnMouseEntered(e -> {
                    if ("primary".equals(type)) {
                        button.setStyle("-fx-background-color: #4338ca; -fx-text-fill: white; -fx-border-radius: 6px; -fx-padding: 8px 16px;");
                    } else {
                        button.setStyle("-fx-background-color: #cbd5e1; -fx-text-fill: #1e293b; -fx-border-radius: 6px; -fx-padding: 8px 16px;");
                    }
                });
                
                button.setOnMouseExited(e -> {
                    if ("primary".equals(type)) {
                        button.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-border-radius: 6px; -fx-padding: 8px 16px;");
                    } else {
                        button.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #64748b; -fx-border-radius: 6px; -fx-padding: 8px 16px;");
                    }
                });
                
                return button;
            }
            
            private boolean validateFields(TextField titleField, TextField descriptionField, DatePicker dueDatePicker,
                                         ComboBox<String> priorityComboBox, TextField categoryField, ComboBox<String> statusComboBox) {
                boolean isValid = true;
                
                // Title validation
                if (titleField.getText().trim().isEmpty()) {
                    titleField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ef4444; -fx-border-radius: 6px; -fx-padding: 8px;");
                    isValid = false;
                } else {
                    titleField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-padding: 8px;");
                }
                
                // Due date validation
                if (dueDatePicker.getValue() == null) {
                    dueDatePicker.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ef4444; -fx-border-radius: 6px; -fx-padding: 8px;");
                    isValid = false;
                } else {
                    dueDatePicker.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-padding: 8px;");
                }
                
                // Priority validation
                if (priorityComboBox.getValue() == null) {
                    priorityComboBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ef4444; -fx-border-radius: 6px; -fx-padding: 8px;");
                    isValid = false;
                } else {
                    priorityComboBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-padding: 8px;");
                }
                
                // Status validation
                if (statusComboBox.getValue() == null) {
                    statusComboBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ef4444; -fx-border-radius: 6px; -fx-padding: 8px;");
                    isValid = false;
                } else {
                    statusComboBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-padding: 8px;");
                }
                
                return isValid;
            }
            
            private void showDetailsWithAnimation(Task task) {
                // If details section doesn't exist, create it
                if (detailsSection == null) {
                    createDetailsSection();
                }

                // Set the task data in the details section
                updateDetailsSection(task);

                // Add the details section to mainContentArea if it's not already there
                if (!mainContentArea.getChildren().contains(detailsSection)) {
                    detailsSection.setOpacity(0);
                    detailsSection.setTranslateX(60); // Start slightly off-screen
                    mainContentArea.getChildren().add(detailsSection);

                    // Slide-in and fade-in animation
                    TranslateTransition slideIn = new TranslateTransition(Duration.millis(350), detailsSection);
                    slideIn.setFromX(60);
                    slideIn.setToX(0);
                    slideIn.setInterpolator(Interpolator.EASE_OUT);

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(350), detailsSection);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);

                    slideIn.play();
                    fadeIn.play();
                }
                selectedTask = task;
            }

            private void hideDetailsWithAnimation() {
                if (detailsSection != null && mainContentArea.getChildren().contains(detailsSection)) {
                    // Slide-out and fade-out animation
                    TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), detailsSection);
                    slideOut.setFromX(0);
                    slideOut.setToX(60);
                    slideOut.setInterpolator(Interpolator.EASE_IN);

                    FadeTransition fadeOut = new FadeTransition(Duration.millis(250), detailsSection);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);

                    slideOut.play();
                    fadeOut.play();

                    fadeOut.setOnFinished(e -> mainContentArea.getChildren().remove(detailsSection));
                }
                selectedTask = null;
            }
            
            private void createDetailsSection() {
                detailsSection = new VBox(20);
                detailsSection.setPrefWidth(350);
                detailsSection.setPadding(new Insets(20));
                detailsSection.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 0 1;");
            }
            
            private void updateDetailsSection(Task task) {
                detailsSection.getChildren().clear();
                
                // Title with close button
                HBox titleBox = new HBox();
                titleBox.setAlignment(Pos.CENTER_LEFT);
                
                Label detailsTitle = new Label("Task Details");
                detailsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                Button closeButton = new Button("×");
                closeButton.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-text-fill: #64748b;");
                closeButton.setOnAction(e -> hideDetailsWithAnimation());
                
                titleBox.getChildren().addAll(detailsTitle, spacer, closeButton);
                
                // Task information
                VBox infoBox = new VBox(15);
                infoBox.setPadding(new Insets(10, 0, 10, 0));
                
                Label taskTitle = new Label(task.getTitle());
                taskTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
                
                Label description = new Label(task.getDescription());
                description.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-wrap-text: true;");
                
                // Status with colored indicator
                HBox statusBox = new HBox(10);
                statusBox.setAlignment(Pos.CENTER_LEFT);
                
                Circle statusIndicator = new Circle(8);
                switch (task.getStatus()) {
                    case "To Do":
                        statusIndicator.setFill(Color.web("#3b82f6"));
                        break;
                    case "In Progress":
                        statusIndicator.setFill(Color.web("#f59e0b"));
                        break;
                    case "Done":
                        statusIndicator.setFill(Color.web("#10b981"));
                        break;
                    default:
                        statusIndicator.setFill(Color.web("#cbd5e1"));
                }
                
                Label statusLabel = new Label("Status: " + task.getStatus());
                statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");
                
                statusBox.getChildren().addAll(statusIndicator, statusLabel);
                
                // Priority with colored indicator
                HBox priorityBox = new HBox(10);
                priorityBox.setAlignment(Pos.CENTER_LEFT);
                
                Circle priorityIndicator = new Circle(8);
                switch (task.getPriority()) {
                    case "High":
                        priorityIndicator.setFill(Color.web("#ef4444"));
                        break;
                    case "Medium":
                        priorityIndicator.setFill(Color.web("#f59e0b"));
                        break;
                    case "Low":
                        priorityIndicator.setFill(Color.web("#10b981"));
                        break;
                    default:
                        priorityIndicator.setFill(Color.web("#cbd5e1"));
                }
                
                Label priorityLabel = new Label("Priority: " + task.getPriority());
                priorityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");
                
                priorityBox.getChildren().addAll(priorityIndicator, priorityLabel);
                
                // Other details
                Label dueDate = new Label("Due Date: " + task.getDueDate());
                dueDate.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");
                
                Label category = new Label("Category: " + task.getCategory());
                category.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");
                
                infoBox.getChildren().addAll(taskTitle, description, new Separator(), statusBox, priorityBox, dueDate, category);
                
                // Action buttons
                HBox actionBox = new HBox(15);
                actionBox.setPadding(new Insets(10, 0, 0, 0));
                
                Button editButton = createMinimalButton("Edit Task", "primary");
                Button deleteButton = createMinimalButton("Delete", "secondary");
                
                editButton.setOnAction(e -> showEditTaskDialog(task));
                deleteButton.setOnAction(e -> showDeleteConfirmation(task));
                
                actionBox.getChildren().addAll(editButton, deleteButton);
                
                detailsSection.getChildren().addAll(titleBox, infoBox, actionBox);
            }
            
            private void showEditTaskDialog(Task task) {
                // Create a dialog
                Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.setTitle("Edit Task");
                dialog.setMinWidth(400);
                dialog.setMinHeight(450);
                
                VBox dialogContent = new VBox(20);
                dialogContent.setPadding(new Insets(30));
                dialogContent.setStyle("-fx-background-color: #ffffff;");
                
                Label dialogTitle = new Label("Edit Task");
                dialogTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
                
                // Form fields
                GridPane formGrid = new GridPane();
                formGrid.setVgap(15);
                formGrid.setHgap(15);
                
                TextField titleField = createMinimalTextField();
                titleField.setText(task.getTitle());
                
                TextField descriptionField = createMinimalTextField();
                descriptionField.setText(task.getDescription());
                
                DatePicker dueDatePicker = createMinimalDatePicker();
                dueDatePicker.setValue(task.getDueDate());
                
                ComboBox<String> priorityComboBox = createMinimalComboBox(
                    FXCollections.observableArrayList("High", "Medium", "Low"));
                priorityComboBox.setValue(task.getPriority());
                
                TextField categoryField = createMinimalTextField();
                categoryField.setText(task.getCategory());
                
                ComboBox<String> statusComboBox = createMinimalComboBox(
                    FXCollections.observableArrayList("To Do", "In Progress", "Done"));
                statusComboBox.setValue(task.getStatus());
                
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
                
                // Buttons
                HBox buttonBox = new HBox(15);
                buttonBox.setAlignment(Pos.CENTER_RIGHT);
                
                Button cancelButton = createMinimalButton("Cancel", "secondary");
                Button saveButton = createMinimalButton("Save Changes", "primary");
                
                cancelButton.setOnAction(e -> dialog.close());
                saveButton.setOnAction(e -> {
                    if (validateFields(titleField, descriptionField, dueDatePicker, priorityComboBox, categoryField, statusComboBox)) {
                        // Get the old status and priority for updating counts
                        String oldStatus = task.getStatus();
                        String oldPriority = task.getPriority();
                        
                        // Update task
                        task.setTitle(titleField.getText());
                        task.setDescription(descriptionField.getText());
                        task.setDueDate(dueDatePicker.getValue());
                        task.setPriority(priorityComboBox.getValue());
                        task.setCategory(categoryField.getText());
                        task.setStatus(statusComboBox.getValue());
                        
                        // Update the task in the controller
                        taskController.updateTask(task);
                        
                        // Refresh the list view
                        int index = taskList.indexOf(task);
                        if (index >= 0) {
                            taskList.set(index, task);
                        }
                        
                        // Update the details panel if it's visible
                        if (currentlyDisplayedTask == task) {
                            updateDetailsSection(task);
                        }
                        
                        // Update status and priority counts if they've changed
                        if (!oldStatus.equals(task.getStatus()) || !oldPriority.equals(task.getPriority())) {
                            updateStatusCounts();
                            updatePriorityCounts();
                            
                            // Refresh the priority dashboard
                            contentTabPane.getTabs().get(1).setContent(createPriorityDashboard());
                        }
                        
                        dialog.close();
                    }
                });
                
                buttonBox.getChildren().addAll(cancelButton, saveButton);
                
                dialogContent.getChildren().addAll(dialogTitle, formGrid, buttonBox);
                
                Scene dialogScene = new Scene(dialogContent);
                dialogScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
                dialog.setScene(dialogScene);
                dialog.show();
            }
            
            private void showDeleteConfirmation(Task task) {
                // Create a dialog
                Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.setTitle("Delete Task");
                dialog.setMinWidth(400);
                dialog.setMinHeight(200);
                
                VBox dialogContent = new VBox(20);
                dialogContent.setPadding(new Insets(30));
                dialogContent.setAlignment(Pos.CENTER);
                dialogContent.setStyle("-fx-background-color: #ffffff;");
                
                Label confirmLabel = new Label("Are you sure you want to delete this task?");
                confirmLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #1e293b;");
                
                Label taskTitleLabel = new Label("\"" + task.getTitle() + "\"");
                taskTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
                
                HBox buttonBox = new HBox(15);
                buttonBox.setAlignment(Pos.CENTER);
                
                Button cancelButton = createMinimalButton("Cancel", "secondary");
                Button deleteButton = createMinimalButton("Delete", "primary");
                deleteButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-border-radius: 6px; -fx-padding: 8px 16px;");
                
                cancelButton.setOnAction(e -> dialog.close());
                deleteButton.setOnAction(e -> {
                    // Delete task
                    taskController.deleteTask(task);
                    
                    // Remove from the list
                    taskList.remove(task);
                    
                    // Hide details panel if showing this task
                    if (currentlyDisplayedTask == task) {
                        hideDetailsWithAnimation();
                        currentlyDisplayedTask = null;
                    }
                    
                    // Update counts
                    updateStatusCounts();
                    updatePriorityCounts();
                    
                    // Refresh the priority dashboard
                    contentTabPane.getTabs().get(1).setContent(createPriorityDashboard());
                    
                    dialog.close();
                });
                
                buttonBox.getChildren().addAll(cancelButton, deleteButton);
                
                dialogContent.getChildren().addAll(confirmLabel, taskTitleLabel, buttonBox);
                
                Scene dialogScene = new Scene(dialogContent);
                dialogScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
                dialog.setScene(dialogScene);
                dialog.show();
            }
            
            private void updateStatusCounts() {
                // Reset counts
                statusCounts.clear();
                statusCounts.put("To Do", 0);
                statusCounts.put("In Progress", 0);
                statusCounts.put("Done", 0);
                
                // Count tasks by status
                for (Task task : taskList) {
                    String status = task.getStatus();
                    statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
                }
                
                // Update the counter labels
                if (todoCountLabel != null) {
                    todoCountLabel.setText(String.valueOf(statusCounts.getOrDefault("To Do", 0)));
                }
                
                if (inProgressCountLabel != null) {
                    inProgressCountLabel.setText(String.valueOf(statusCounts.getOrDefault("In Progress", 0)));
                }
                
                if (doneCountLabel != null) {
                    doneCountLabel.setText(String.valueOf(statusCounts.getOrDefault("Done", 0)));
                }
            }
            
            private void updatePriorityCounts() {
                // Reset counts
                priorityCounts.clear();
                priorityCounts.put("High", 0);
                priorityCounts.put("Medium", 0);
                priorityCounts.put("Low", 0);
                
                // Count tasks by priority
                for (Task task : taskList) {
                    String priority = task.getPriority();
                    priorityCounts.put(priority, priorityCounts.getOrDefault(priority, 0) + 1);
                }
            }
            
            private void fadeTransition(VBox contentBox, Runnable onFinished) {
                FadeTransition fade = new FadeTransition(Duration.millis(400), contentBox);
                fade.setFromValue(1.0);
                fade.setToValue(0.0);
                fade.setOnFinished(e -> onFinished.run());
                fade.play();
            }
        }