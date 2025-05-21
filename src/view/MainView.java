package view;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import controller.TaskController;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Task;
import persistence.DatabasePersistence;
import model.Note;
import persistence.NotePersistence;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;

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

    private VBox notificationBox;

    public void setUser(model.User user) {
        this.user = user;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        if (user == null) {

            WelcomeScreen.show(primaryStage, () -> showMainView(primaryStage));
        } else {

            showMainView(primaryStage);
        }
    }

    
    

    private void showMainView(Stage primaryStage) {
        if (user == null) {

            return;
        }
        
        // Set the window title properly
        primaryStage.setTitle("TaskFlow");
        
        taskController = new TaskController(user, new DatabasePersistence());
        taskList = FXCollections.observableArrayList();
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
        primaryStage.setMaximized(true);

        // Ensure counters are updated after UI is built and tasks are loaded
        updateStatusCounts();
        updatePriorityCounts();
        updateNotifications(); 
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

    private Button viewTasksBtn;
    private Button priorityDashboardBtn;
    private Button addTaskBtn;

    private int currentTabIndex = 0; // Tracks the currently selected tab index.
    private Button currentActiveButton; // Tracks the currently active button.


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
//        Button viewTasksBtn = createNavButton("View Tasks", true);
//        Button priorityDashboardBtn = createNavButton("Priority Dashboard", false);
//        Button addTaskBtn = createNavButton("Add New Task", false);

        viewTasksBtn = createNavButton("View Tasks", true);
        priorityDashboardBtn = createNavButton("Priority Dashboard", false);
        addTaskBtn = createNavButton("Add New Task", false);


//        viewTasksBtn.setOnAction(e -> {
//            contentTabPane.getSelectionModel().select(0);
//            setActiveButton(viewTasksBtn, priorityDashboardBtn, addTaskBtn);
//        });
//
//        priorityDashboardBtn.setOnAction(e -> {
//            contentTabPane.getSelectionModel().select(1);
//            setActiveButton(priorityDashboardBtn, viewTasksBtn, addTaskBtn);
//        });
//
//        addTaskBtn.setOnAction(event -> {
//            // Open the Add Task dialog
//            showAddTaskDialog(primaryStage);
//
//            // Remove highlight/focus from the button
//            addTaskBtn.setStyle("-fx-background-color: #f4f6fb;"); // or your default style
//            addTaskBtn.getParent().requestFocus(); // Move focus away from the button
//        });

        viewTasksBtn.setOnAction(e -> {
            contentTabPane.getSelectionModel().select(0); // Switch to the first tab or view
            setActiveButton(viewTasksBtn, priorityDashboardBtn, addTaskBtn); // Update active button
        });

        priorityDashboardBtn.setOnAction(e -> {
            contentTabPane.getSelectionModel().select(1); // Switch to the second tab or view
            setActiveButton(priorityDashboardBtn, viewTasksBtn, addTaskBtn); // Update active button
        });

//        addTaskBtn.setOnAction(e -> {
//            showAddTaskDialog(primaryStage); // Open a dialog box or perform an action
//            setActiveButton(addTaskBtn, viewTasksBtn, priorityDashboardBtn); // Update active button
//        });

        addTaskBtn.setOnAction(e -> {
            // Save the current tab index and active button before opening the dialog
            currentTabIndex = contentTabPane.getSelectionModel().getSelectedIndex();
            currentActiveButton = viewTasksBtn; // Default to viewTasksBtn in case of missing state
            if (priorityDashboardBtn.getUserData() != null &&
                    priorityDashboardBtn.getUserData().equals("active")) {
                currentActiveButton = priorityDashboardBtn;
            } else if (addTaskBtn.getUserData() != null &&
                    addTaskBtn.getUserData().equals("active")) {
                currentActiveButton = addTaskBtn;
            }

            // Open the dialog
            showAddTaskDialog(primaryStage);

            // Set the "Add Task" button as the active button temporarily
            setActiveButton(addTaskBtn, viewTasksBtn, priorityDashboardBtn);
        });


        navItems.getChildren().addAll(viewTasksBtn, priorityDashboardBtn, addTaskBtn);

        // Statistics summary in sidebar
        VBox statusSummary = new VBox(15); // Container for statistics section
        statusSummary.setPadding(new Insets(20, 15, 20, 15)); // Adjusted padding for consistency
        statusSummary.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #F8FAFC);" + // Gradient background
                        "-fx-background-radius: 12px;" + // Rounded corners
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-color: #E2E8F0;" + // Light border for separation
                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.1), 6, 0, 0, 3);" // Subtle shadow for depth
        );

// Title for this section
        Label summaryTitle = new Label("Tasks Overview");
        summaryTitle.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #1E293B;" // Darker blue-gray text for title
        );

// Create the mini status chart (customized style if needed)
        VBox statusChart = createMiniStatusChart();
        statusChart.setStyle("-fx-padding: 10px; -fx-spacing: 10px;"); // Add space inside the chart container

// Add content to the statistics section
        statusSummary.getChildren().addAll(summaryTitle, statusChart);


        // --- Notifications Box ---
        notificationBox = new VBox(10); // Create the notification container
        notificationBox.setPadding(new Insets(15, 15, 15, 15));
        notificationBox.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #FFD700, #FFFACD);" + // Smooth gradient effect
                        "-fx-background-radius: 12px; " +
                        "-fx-border-radius: 12px; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-color: #F59E0B;"
        );

// Add a title for the notifications
        Label notificationTitle = new Label("Due Soon");
        notificationTitle.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #92400E;" // A bolder orange color for the title
        );

// Placeholder notification
        Label notificationMessage = new Label("You have 3 tasks due in the next 24 hours!");
        notificationMessage.setStyle("-fx-font-size: 13px; -fx-text-fill: #4B5563;"); // Gray text for details

// Add an icon by using a Unicode emoji or image
        Label notificationIcon = new Label("\u26A0"); // ⚠ Warning symbol as an icon
        notificationIcon.setStyle("-fx-font-size: 16px; -fx-text-fill: #92400E;"); // Match title color

        HBox notificationItem = new HBox(10, notificationIcon, notificationMessage);
        notificationItem.setAlignment(Pos.CENTER_LEFT); // Align content to the left

// Add entry animation for the notification box
        notificationBox.getChildren().addAll(notificationTitle, notificationItem);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), notificationBox);
        fadeIn.setFromValue(0); // Fade in from invisible
        fadeIn.setToValue(1); // Fully visible
        fadeIn.play();


        // -----------------------------------

        // Log Out button
        Button logoutButton = new Button("Log Out");
        logoutButton.setPrefWidth(190);
        logoutButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 15px; -fx-background-radius: 6px;");
        logoutButton.setOnMouseEntered(e -> logoutButton.setStyle("-fx-background-color: #b91c1c; -fx-text-fill: white; -fx-font-size: 15px; -fx-background-radius: 6px;"));
        logoutButton.setOnMouseExited(e -> logoutButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 15px; -fx-background-radius: 6px;"));

        logoutButton.setOnAction(e -> {
            // Go back to sign-in window
            Stage stage = (Stage) sidebar.getScene().getWindow();
            stage.close();
            showSignInWindow(new Stage());
        });

        // Add a spacer region before the logout button
        Region sidebarSpacer = new Region();
        VBox.setVgrow(sidebarSpacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(
            userProfile,
            navItems,
            statusSummary,
            notificationBox,
            sidebarSpacer,
            logoutButton
        );

        return sidebar;
    }
    
//    private void setActiveButton(Button activeButton, Button... otherButtons) {
//        activeButton.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #4f46e5; -fx-font-weight: bold; -fx-background-radius: 6px;");
//
//        for (Button button : otherButtons) {
//            button.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-background-radius: 6px;");
//        }
//    }

//    private void setActiveButton(Button activeButton, Button... otherButtons) {
//        // Set the active button's style
//        activeButton.setUserData("active"); // Mark as active (for comparison later)
//        activeButton.setStyle(
//                "-fx-background-color: #e0e7ff;" +  // Light blue background for active button
//                        "-fx-text-fill: #4f46e5;" +        // Dark blue text
//                        "-fx-font-weight: bold;" +        // Bold font
//                        "-fx-background-radius: 6px;"    // Rounded corners
//        );
//
//        // Reset the other buttons' styles
//        for (Button button : otherButtons) {
//            button.setUserData("inactive"); // Mark as inactive
//            button.setStyle(
//                    "-fx-background-color: transparent;" + // Transparent background
//                            "-fx-text-fill: #64748b;" +            // Grayish blue text
//                            "-fx-font-weight: normal;" +          // Normal font weight
//                            "-fx-background-radius: 6px;"         // Rounded corners
//            );
//        }
//
//        // Ensure the hover effect doesn’t accidentally override styles
//        setHoverEffects(activeButton, true);
//        for (Button button : otherButtons) {
//            setHoverEffects(button, false);
//        }
//    }
//
//    private void setHoverEffects(Button button, boolean isActive) {
//        button.setOnMouseEntered(e -> {
//            // Only apply hover effects if the button is not the active one
//            if (!"active".equals(button.getUserData())) {
//                button.setStyle(
//                        "-fx-background-color: #f1f5f9;" +  // Slight background
//                                "-fx-text-fill: #1e293b;" +         // Darker gray text
//                                "-fx-font-weight: bold;" +         // Slightly bold
//                                "-fx-background-radius: 6px;"      // Rounded corners
//                );
//            }
//        });
//
//        button.setOnMouseExited(e -> {
//            // Restore the appropriate style
//            if ("active".equals(button.getUserData())) {
//                button.setStyle(
//                        "-fx-background-color: #e0e7ff;" +
//                                "-fx-text-fill: #4f46e5;" +
//                                "-fx-font-weight: bold;" +
//                                "-fx-background-radius: 6px;"
//                );
//            } else {
//                button.setStyle(
//                        "-fx-background-color: transparent;" +
//                                "-fx-text-fill: #64748b;" +
//                                "-fx-font-weight: normal;" +
//                                "-fx-background-radius: 6px;"
//                );
//            }
//        });
//    }


    private void setActiveButton(Button activeButton, Button... otherButtons) {
        // Mark the active button and apply active style
        activeButton.setUserData("active");
        activeButton.setStyle(
                "-fx-background-color: #e0e7ff;" +  // Light blue background for active button
                        "-fx-text-fill: #4f46e5;" +        // Dark blue text
                        "-fx-font-weight: bold;" +        // Bold font
                        "-fx-font-size: 14px;" +          // Ensure proper font size
                        "-fx-background-radius: 6px;"    // Rounded corners
        );

        // Reset styles for the other buttons
        for (Button button : otherButtons) {
            button.setUserData("inactive");
            button.setStyle(
                    "-fx-background-color: transparent;" +   // Transparent background
                            "-fx-text-fill: #64748b;" +             // Gray-blue text
                            "-fx-font-weight: normal;" +            // Normal font weight
                            "-fx-font-size: 14px;" +                // Ensure proper font size
                            "-fx-background-radius: 6px;"          // Rounded corners
            );
        }

        // Update hover effects for consistency
        addHoverEffects(activeButton, true);
        for (Button button : otherButtons) {
            addHoverEffects(button, false);
        }
    }

    private void addHoverEffects(Button button, boolean isActive) {
        button.setOnMouseEntered(e -> {
            // Only apply hover if the button is inactive
            if (!"active".equals(button.getUserData())) {
                button.setStyle(
                        "-fx-background-color: #f1f5f9;" +  // Light hover background
                                "-fx-text-fill: #1e293b;" +         // Dark gray text
                                "-fx-font-weight: bold;" +         // Slightly bold
                                "-fx-font-size: 14px;" +           // Font size
                                "-fx-background-radius: 6px;"      // Rounded corners
                );
            }
        });

        button.setOnMouseExited(e -> {
            // Restore the appropriate style based on the button state
            if ("active".equals(button.getUserData())) {
                button.setStyle(
                        "-fx-background-color: #e0e7ff;" +
                                "-fx-text-fill: #4f46e5;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 14px;" +
                                "-fx-background-radius: 6px;"
                );
            } else {
                button.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: #64748b;" +
                                "-fx-font-weight: normal;" +
                                "-fx-font-size: 14px;" +
                                "-fx-background-radius: 6px;"
                );
            }
        });
    }

    private VBox createMiniStatusChart() {
        VBox chartContainer = new VBox(15); // Container for chart bars
        chartContainer.setPadding(new Insets(15, 10, 15, 10)); // Add padding
        chartContainer.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #F8FAFC);" + // Gradient background
                        "-fx-background-radius: 12px;" + // Rounded edges
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-color: #E2E8F0;" + // Light border
                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.1), 6, 0, 0, 3);" // Shadow effect
        );

        // Add individual status bars
        HBox todoBar = createStatusBar("To Do", "#3b82f6"); // Blue for "To Do"
        HBox inProgressBar = createStatusBar("In Progress", "#f59e0b"); // Yellow for "In Progress"
        HBox doneBar = createStatusBar("Done", "#10b981"); // Green for "Done"

        // Add status bars to the container
        chartContainer.getChildren().addAll(todoBar, inProgressBar, doneBar);
        return chartContainer;
    }

    private HBox createStatusBar(String status, String color) {
        HBox statusBar = new HBox(10); // Container for each status row
        statusBar.setPadding(new Insets(10, 15, 10, 15)); // Add consistent padding
        statusBar.setAlignment(Pos.CENTER_LEFT); // Align items to the left
        statusBar.setStyle(
                "-fx-background-color: " + color + ";" +       // Background color
                        "-fx-background-radius: 12px;" +              // Rounded corners for better visuals
                        "-fx-border-radius: 12px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.2), 6, 0, 0, 3);" // Add shadow for depth
        );

        // Label to display the status name
        Label statusLabel = new Label(status);
        statusLabel.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #FFFFFF;" // White text for contrast with colorful backgrounds
        );

        // Placeholder for visualizing tasks as a bar (optional)
        Region barFiller = new Region();
        HBox.setHgrow(barFiller, Priority.ALWAYS); // Stretch filler across the available space

        // Add the text label and filler to the row
        statusBar.getChildren().addAll(statusLabel, barFiller);
        return statusBar;
    }

//    private Button createNavButton(String text, boolean isActive) {
//        Button button = new Button(text);
//        button.setPrefWidth(190);
//        button.setPadding(new Insets(12, 15, 12, 15));
//        button.setAlignment(Pos.CENTER_LEFT);
//
//        // Helper to check if button is active by its style
//        Runnable setActiveStyle = () -> button.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #4f46e5; -fx-font-weight: bold; -fx-background-radius: 6px;");
//        Runnable setInactiveStyle = () -> button.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-background-radius: 6px;");
//
//        if (isActive) {
//            setActiveStyle.run();
//        } else {
//            setInactiveStyle.run();
//        }
//
//        button.setOnMouseEntered(e -> {
//            // Only apply hover if not active
//            if (!button.getStyle().contains("#e0e7ff")) {
//                button.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #1e293b; -fx-background-radius: 6px;");
//            }
//        });
//
//        button.setOnMouseExited(e -> {
//            // Restore active/inactive style
//            if (button.getStyle().contains("#e0e7ff")) {
//                setActiveStyle.run();
//            } else {
//                setInactiveStyle.run();
//            }
//        });
//
//        return button;
//    }

//    private Button createNavButton(String text, boolean isActive) {
//        Button button = new Button(text);
//        button.setPrefWidth(190);
//        button.setPadding(new Insets(12, 15, 12, 15));
//        button.setAlignment(Pos.CENTER_LEFT);
//        button.setStyle("-fx-font-size: 14px; -fx-font-family: 'Arial'; -fx-background-radius: 6px;");
//
//        // Helper to check if button is active by its style
//        Runnable setActiveStyle = () -> button.setStyle(
//                "-fx-background-color: #e0e7ff;" +  // Light blue background for active button
//                        "-fx-text-fill: #4f46e5;" +       // Darker blue text color
//                        "-fx-font-weight: bold;" +       // Bold font for emphasis
//                        "-fx-font-size: 14px;" +         // Match font size and family
//                        "-fx-background-radius: 6px;"   // Rounded corners
//        );
//
//        Runnable setInactiveStyle = () -> button.setStyle(
//                "-fx-background-color: transparent;" +  // Transparent background for inactive button
//                        "-fx-text-fill: #64748b;" +             // Grayish-blue text color for inactive buttons
//                        "-fx-font-weight: normal;" +           // Normal font weight
//                        "-fx-font-size: 14px;" +               // Matching font size
//                        "-fx-background-radius: 6px;"         // Rounded corners
//        );
//
//        if (isActive) {
//            setActiveStyle.run();
//        } else {
//            setInactiveStyle.run();
//        }
//
//        // Add hover effect
//        button.setOnMouseEntered(e -> {
//            // Apply hover style if the button is not active
//            if (!button.getStyle().contains("#e0e7ff")) {
//                button.setStyle(
//                        "-fx-background-color: #f1f5f9;" +  // Slightly lighter background on hover
//                                "-fx-text-fill: #1e293b;" +        // Dark text color on hover
//                                "-fx-font-weight: bold;" +        // A slightly bold font for hover
//                                "-fx-font-size: 14px;" +          // Match font size
//                                "-fx-background-radius: 6px;"    // Consistent corner radius
//                );
//            }
//        });
//
//        button.setOnMouseExited(e -> {
//            // Restore active/inactive style when hover exits
//            if (button.getStyle().contains("#e0e7ff")) {
//                setActiveStyle.run();
//            } else {
//                setInactiveStyle.run();
//            }
//        });
//
//        return button;
//    }


    private Button createNavButton(String text, boolean isActive) {
        Button button = new Button(text);
        button.setPrefWidth(190);
        button.setPadding(new Insets(12, 15, 12, 15));
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle("-fx-font-size: 14px; -fx-font-family: 'Arial'; -fx-background-radius: 6px;");

        // Define active and inactive styles
        Runnable setActiveStyle = () -> button.setStyle(
                "-fx-background-color: #e0e7ff;" +  // Light blue background for active button
                        "-fx-text-fill: #4f46e5;" +        // Dark blue text color
                        "-fx-font-weight: bold;" +        // Bold font
                        "-fx-font-size: 14px;" +          // Matching font size
                        "-fx-background-radius: 6px;"    // Rounded corners
        );

        Runnable setInactiveStyle = () -> button.setStyle(
                "-fx-background-color: transparent;" + // Transparent background for inactive button
                        "-fx-text-fill: #64748b;" +            // Gray text color
                        "-fx-font-weight: normal;" +          // Normal font weight
                        "-fx-font-size: 14px;" +              // Matching font size
                        "-fx-background-radius: 6px;"         // Rounded corners
        );

        // Apply the initial style based on `isActive`
        if (isActive) {
            setActiveStyle.run();
        } else {
            setInactiveStyle.run();
        }

        // Add hover effect
        button.setOnMouseEntered(e -> {
            if (!button.getStyle().contains("#e0e7ff")) { // Only hover if not active
                button.setStyle(
                        "-fx-background-color: #f1f5f9;" +  // Light hover background
                                "-fx-text-fill: #1e293b;" +         // Dark gray text
                                "-fx-font-weight: bold;" +         // Slightly bold
                                "-fx-font-size: 14px;" +           // Match font size
                                "-fx-background-radius: 6px;"      // Consistent corners
                );
            }
        });

        button.setOnMouseExited(e -> {
            // Restore active/inactive style when hover exits
            if (button.getStyle().contains("#e0e7ff")) {
                setActiveStyle.run();
            } else {
                setInactiveStyle.run();
            }
        });

        // Return the prepared button
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

        contentTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && newTab.getText().equals("Dashboard")) {
                hideDetailsWithAnimation(); // Auto-close details when switching to dashboard
                currentlyDisplayedTask = null;
                // Clear selection in the task list view if it exists
                if (taskListView != null) {
                    taskListView.getSelectionModel().clearSelection();
                }
            }
        });

        // Let the TabPane grow to fill the VBox
        VBox.setVgrow(contentTabPane, Priority.ALWAYS);
        mainContentArea.getChildren().add(contentTabPane);

        // Let the mainContentArea grow in its parent (BorderPane)
        VBox.setVgrow(mainContentArea, Priority.ALWAYS);
    }
    
    

private VBox createTaskViewSection() {
    VBox taskViewSection = new VBox(20);
    taskViewSection.setStyle("-fx-background-color: #f8fafc; -fx-border-radius: 12px; -fx-padding: 20;");

    // Create horizontal box for title and all filter controls
    HBox titleSearchFilterBox = new HBox(15);
    titleSearchFilterBox.setAlignment(Pos.CENTER_LEFT);

    // Task list title
    Label listTitle = new Label("Your Tasks");
    listTitle.setStyle(
            "-fx-font-size: 22px;" + // Slightly larger font for emphasis
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #1E293B;" // Dark blue-gray text for contrast
    );

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    // Create filter controls container
    HBox filterControls = new HBox(12); // More spacing between the controls
    filterControls.setAlignment(Pos.CENTER_RIGHT);

    // Priority filter
    ComboBox<String> priorityFilter = new ComboBox<>();
    priorityFilter.getItems().addAll("All Priorities", "High", "Medium", "Low");
    priorityFilter.setValue("All Priorities");
    priorityFilter.setStyle(
            "-fx-background-color: #FFFFFF;" + // White background for contrast
                    "-fx-border-color: #CBD5E1;" + // Light gray border for subtle design
                    "-fx-border-radius: 8px;" + // More rounded edges
                    "-fx-padding: 6px 12px;" // Better inner padding for text
    );
    priorityFilter.setPrefWidth(140); // Adjusted width for better alignment

    // Status filter
    ComboBox<String> statusFilter = new ComboBox<>();
    statusFilter.getItems().addAll("All Statuses", "To Do", "In Progress", "Done");
    statusFilter.setValue("All Statuses");
    statusFilter.setStyle(
            "-fx-background-color: #FFFFFF;" +
                    "-fx-border-color: #CBD5E1;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-padding: 6px 12px;"
    );
    statusFilter.setPrefWidth(140);

    // Date filter
    DatePicker dateFilter = new DatePicker();
    dateFilter.setPromptText("Filter by date");
    dateFilter.setStyle(
            "-fx-background-color: #FFFFFF;" +
                    "-fx-border-color: #CBD5E1;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-padding: 6px;"
    );

    // Search box
    TextField searchBox = new TextField();
    searchBox.setPromptText("Search tasks...");
    searchBox.setPrefWidth(200); // Increased width for better usability
    searchBox.setStyle(
            "-fx-background-color: #FFFFFF;" +
                    "-fx-border-color: #CBD5E1;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-padding: 8px;" +
                    "-fx-font-size: 14px;" // Match with input font sizes
    );

    // Filter buttons container
    HBox filterButtons = new HBox(8); // Adjusted spacing between buttons
    filterButtons.setAlignment(Pos.CENTER);

    // Apply filter button
    Button applyFilterBtn = new Button("Apply");
    applyFilterBtn.setStyle(
            "-fx-background-color: #4F46E5;" + // Indigo background
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-padding: 8px 16px;" + // Better button size
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(79, 70, 229, 0.3), 6, 0, 1, 2);" // Subtle shadow effect
    );

    // Clear filter button
    Button clearFilterBtn = new Button("Clear");
    clearFilterBtn.setStyle(
            "-fx-background-color: #E5E7EB;" + // Light gray background
                    "-fx-text-fill: #475569;" +
                    "-fx-font-size: 14px;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-padding: 8px 16px;" +
                    "-fx-cursor: hand;"
    );

    // Add hover effects for buttons
    applyFilterBtn.setOnMouseEntered(e -> applyFilterBtn.setStyle(
            "-fx-background-color: #4338CA;" + // Darker indigo for hover
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-padding: 8px 16px;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(67, 56, 202, 0.4), 8, 0, 1, 3);"
    ));
    applyFilterBtn.setOnMouseExited(e -> applyFilterBtn.setStyle(
            "-fx-background-color: #4F46E5;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-padding: 8px 16px;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(79, 70, 229, 0.3), 6, 0, 1, 2);"
    ));

    clearFilterBtn.setOnMouseEntered(e -> clearFilterBtn.setStyle(
            "-fx-background-color: #D1D5DB;" + // Slightly darker gray
                    "-fx-text-fill: #475569;" +
                    "-fx-font-size: 14px;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-padding: 8px 16px;" +
                    "-fx-cursor: hand;"
    ));
    clearFilterBtn.setOnMouseExited(e -> clearFilterBtn.setStyle(
            "-fx-background-color: #E5E7EB;" +
                    "-fx-text-fill: #475569;" +
                    "-fx-font-size: 14px;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-padding: 8px 16px;" +
                    "-fx-cursor: hand;"
    ));

    // Add buttons to the filter buttons container
    filterButtons.getChildren().addAll(clearFilterBtn);

    // Add all filter controls to the container
    filterControls.getChildren().addAll(
            priorityFilter,
            statusFilter,
            dateFilter,
            searchBox,
            filterButtons
    );

    // Add title and filter controls to the main horizontal box
    titleSearchFilterBox.getChildren().addAll(listTitle, spacer, filterControls);

    // Custom ListView for card-style tasks
    taskListView = new ListView<>(taskList);
    taskListView.setId("card-task-list");
    taskListView.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 10;");

    // Custom cell factory for card-style tasks (keep your existing code here)
    taskListView.setCellFactory(lv -> new ListCell<Task>() {
        @Override
        protected void updateItem(Task task, boolean empty) {
            super.updateItem(task, empty);
            if (empty || task == null) {
                setText(null);
                setGraphic(null);
                setStyle("-fx-background-color: transparent;");
            } else {
                // Your existing card creation code
                VBox card = createTaskCard(task);
                setGraphic(card);
                setStyle("-fx-background-color: transparent;");
            }
        }
    });

    // Search functionality
    searchBox.textProperty().addListener((observable, oldValue, newValue) -> {
        applyFilters(newValue, priorityFilter.getValue(), statusFilter.getValue(), dateFilter.getValue());
    });
    priorityFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
        applyFilters(searchBox.getText(), newVal, statusFilter.getValue(), dateFilter.getValue());
    });
    statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
        applyFilters(searchBox.getText(), priorityFilter.getValue(), newVal, dateFilter.getValue());
    });
    dateFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
        applyFilters(searchBox.getText(), priorityFilter.getValue(), statusFilter.getValue(), newVal);
    });

    // Clear filters button action
    clearFilterBtn.setOnAction(e -> {
        searchBox.clear();
        priorityFilter.setValue("All Priorities");
        statusFilter.setValue("All Statuses");
        dateFilter.setValue(null);
        taskListView.setItems(taskList); // Reset to full list
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

    taskViewSection.getChildren().addAll(titleSearchFilterBox, taskListView);

    // Let the ListView grow to fill available space
    VBox.setVgrow(taskListView, Priority.ALWAYS);
    VBox.setVgrow(taskViewSection, Priority.ALWAYS);

    return taskViewSection;
}


// Helper method to create task card (extract from your cell factory)
private VBox createTaskCard(Task task) {
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
    
    // Action buttons for status transitions
    HBox actionButtons = new HBox();
    actionButtons.setAlignment(Pos.CENTER_RIGHT);
    actionButtons.setMinHeight(40); // Ensures consistent height for all cards

    LocalDate now = LocalDate.now();
    boolean isOutdated = task.getDueDate() != null && task.getDueDate().isBefore(now);

    if (isOutdated && !"Done".equals(task.getStatus())) {
        // Show "Outdated" label if due date is in the past and not done
        Label outdatedLabel = new Label("Outdated");
        outdatedLabel.setStyle(
            "-fx-background-color: #fee2e2; " +
            "-fx-text-fill: #b91c1c; " +
            "-fx-font-size: 15px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8px; " +
            "-fx-padding: 10px 24px;"
        );
        actionButtons.getChildren().add(outdatedLabel);
    } else if ("To Do".equals(task.getStatus())) {
        Button startButton = new Button("Start");
        startButton.setStyle(
            "-fx-background-color: #f59e0b; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 8px; " +
            "-fx-font-size: 16px; " +
            "-fx-padding: 10px 24px;"
        );
        startButton.setPrefHeight(40);
        startButton.setPrefWidth(120);
        startButton.setOnAction(e -> {
            task.setStatus("In Progress");
            taskController.updateTask(task);
            int idx = taskList.indexOf(task);
            if (idx >= 0) taskList.set(idx, task);
            updateStatusCounts();
            updatePriorityCounts();
            updateNotifications();
            contentTabPane.getTabs().get(1).setContent(createPriorityDashboard());
            taskListView.refresh();
        });
        actionButtons.getChildren().add(startButton);
    } else if ("In Progress".equals(task.getStatus())) {
        Button completeButton = new Button("Complete");
        completeButton.setStyle(
            "-fx-background-color: #10b981; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 8px; " +
            "-fx-font-size: 16px; " +
            "-fx-padding: 10px 24px;"
        );
        completeButton.setPrefHeight(40);
        completeButton.setPrefWidth(120);
        completeButton.setOnAction(e -> {
            task.setStatus("Done");
            taskController.updateTask(task);
            int idx = taskList.indexOf(task);
            if (idx >= 0) taskList.set(idx, task);
            updateStatusCounts();
            updatePriorityCounts();
            updateNotifications();
            contentTabPane.getTabs().get(1).setContent(createPriorityDashboard());
            taskListView.refresh();
        });
        actionButtons.getChildren().add(completeButton);
    } // For "Done" tasks, actionButtons will be empty but still take up space

    // Always add the actionButtons HBox to keep card heights consistent
    card.getChildren().add(actionButtons);
    
    return card;
}

// Helper method to apply filters
private void applyFilters(String searchText, String priority, String status, LocalDate date) {
    ObservableList<Task> filteredList = FXCollections.observableArrayList();
    
    for (Task task : taskList) {
        // Check if task matches all selected filters
        boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                task.getTitle().toLowerCase().contains(searchText.toLowerCase()) ||
                task.getDescription().toLowerCase().contains(searchText.toLowerCase()) ||
                task.getCategory().toLowerCase().contains(searchText.toLowerCase());
        
        boolean matchesPriority = "All Priorities".equals(priority) || task.getPriority().equals(priority);
        
        boolean matchesStatus = "All Statuses".equals(status) || task.getStatus().equals(status);
        
        boolean matchesDate = date == null || task.getDueDate().equals(date);
        
        // Add task to filtered list if it matches all criteria
        if (matchesSearch && matchesPriority && matchesStatus && matchesDate) {
            filteredList.add(task);
        }
    }
    
    taskListView.setItems(filteredList);
}

    private VBox createPriorityDashboard() {
        // Ensure priorityCounts is up to date for the current user's tasks
        updatePriorityCounts();

        VBox dashboard = new VBox(30);
        dashboard.setPadding(new Insets(20));
        dashboard.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #F9FAFB, #EDF2F7);" + // Light gradient background
                        "-fx-background-radius: 12px;" + // Rounded corners for the dashboard
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-color: #E2E8F0;" + // Light border
                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.1), 6, 0, 0, 3);"
        );

        // Dashboard title
        Label dashboardTitle = new Label("Priority Dashboard");
        dashboardTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Priority distribution chart
        VBox chartSection = new VBox(15);
        chartSection.setPadding(new Insets(20));
        chartSection.setStyle(
                "-fx-background-color: #ffffff;" + // White card-like background
                        "-fx-background-radius: 12px;" +  // Rounded corners
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-color: #E2E8F0;" +    // Light gray border
                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.1), 6, 0, 0, 3);"
        );

        Label chartTitle = new Label("Task Distribution by Priority");
        chartTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Check if there is any data to show
        int total = priorityCounts.getOrDefault("High", 0)
                + priorityCounts.getOrDefault("Medium", 0)
                + priorityCounts.getOrDefault("Low", 0);

        if (total > 0) {
            PieChart priorityChart = createPriorityPieChart();
            chartSection.getChildren().addAll(chartTitle, priorityChart);
        } else {
            Label noDataLabel = new Label("No tasks to display.");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 40 0 40 0;");
            chartSection.getChildren().addAll(chartTitle, noDataLabel);
        }

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

        // Set colors after the chart is rendered
        javafx.application.Platform.runLater(() -> {
            if (pieChartData.size() > 0 && pieChartData.get(0).getNode() != null)
                pieChartData.get(0).getNode().setStyle("-fx-pie-color: #ef4444;");
            if (pieChartData.size() > 1 && pieChartData.get(1).getNode() != null)
                pieChartData.get(1).getNode().setStyle("-fx-pie-color: #f59e0b;");
            if (pieChartData.size() > 2 && pieChartData.get(2).getNode() != null)
                pieChartData.get(2).getNode().setStyle("-fx-pie-color: #10b981;");
        });

        return chart;
    }
    

    private VBox createPrioritySection(String priority, String color) {
        VBox section = new VBox(15);
        section.setPrefWidth(300);
        section.setPadding(new Insets(20));
        section.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #FFFFFF, #F8FAFC);" + // Light card gradient
                        "-fx-background-radius: 12px;" + // Rounded corners
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-color: #E2E8F0;" + // Light border
                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.1), 6, 0, 0, 3);"
        );

        // Section header with count
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Circle priorityDot = new Circle(8, Color.web(color)); // Colored dot for priority type

        Label priorityLabel = new Label(priority);
        priorityLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        String priorityKey = priority.replace(" Priority", "");
        int count = 0;
        for (Task task : taskList) {
            if (task.getPriority().equalsIgnoreCase(priorityKey)) {
                count++;
            }
        }

        Label countLabel = new Label(String.valueOf(count));
        countLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(priorityDot, priorityLabel, spacer, countLabel);

        // Task card list
        VBox tasksList = new VBox(10);
        for (Task task : taskList) {
            if (task.getPriority().equalsIgnoreCase(priorityKey)) {
                HBox taskItem = new HBox(10);
                taskItem.setAlignment(Pos.CENTER_LEFT);
                taskItem.setPadding(new Insets(8));
                taskItem.setStyle(
                        "-fx-background-color: #f9fafb;" + // Subtle light background
                                "-fx-background-radius: 8px;" +   // Rounded card design
                                "-fx-padding: 8;"
                );

                // Status indicator
                Circle statusDot = new Circle(6);
                switch (task.getStatus()) {
                    case "To Do":
                        statusDot.setFill(Color.web("#3b82f6")); // Blue for "To Do"
                        break;
                    case "In Progress":
                        statusDot.setFill(Color.web("#f59e0b")); // Yellow for "In Progress"
                        break;
                    case "Done":
                        statusDot.setFill(Color.web("#10b981")); // Green for "Done"
                        break;
                    default:
                        statusDot.setFill(Color.web("#cbd5e1")); // Default gray color
                }

                Label taskTitle = new Label(task.getTitle());
                taskTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");

                taskItem.getChildren().addAll(statusDot, taskTitle);
                tasksList.getChildren().add(taskItem);
            }
        }

        if (tasksList.getChildren().isEmpty()) {
            Label noTasksLabel = new Label("No " + priorityKey.toLowerCase() + " priority tasks");
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
        
//        cancelButton.setOnAction(e -> dialog.close());
        cancelButton.setOnAction(e -> {
            // Restore the previously selected tab
            contentTabPane.getSelectionModel().select(currentTabIndex);

            // Restore the previously active button
            if (currentActiveButton != null) {
                setActiveButton(currentActiveButton, viewTasksBtn, priorityDashboardBtn, addTaskBtn);
            }

            // Close the dialog
            dialog.close();
        });

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
                updateNotifications();
                // Refresh the priority dashboard
                contentTabPane.getTabs().get(1).setContent(createPriorityDashboard());

                // Switch to the "View Tasks" tab
                contentTabPane.getSelectionModel().select(0);

                // Set the active button to "View Tasks"
                setActiveButton(viewTasksBtn, priorityDashboardBtn, addTaskBtn);



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
                if (detailsSection == null) createDetailsSection();
                if (notesSection == null) createNotesSection();

                updateDetailsSection(task);
                updateNotesSection(task);

                // Remove previous details/notes if present
                mainContentArea.getChildren().removeIf(node -> node == detailsSection || node == notesSection || node instanceof VBox || node instanceof HBox);

                // Close button at the top right
                HBox closeRow = new HBox();
                closeRow.setAlignment(Pos.TOP_RIGHT);
                closeRow.setPadding(new Insets(0, 0, 10, 0));
                Button closeButton = new Button("×");
                closeButton.setStyle(
                    "-fx-background-color: transparent; " +
                    "-fx-font-size: 28px; " +
                    "-fx-text-fill: #64748b; " +
                    "-fx-padding: 2 16 2 16;"
                );
                closeButton.setOnAction(e -> hideDetailsWithAnimation());
                closeRow.getChildren().add(closeButton);

                // Set both sections to 50% width
                detailsSection.setPrefWidth(0.5 * mainContentArea.getWidth());
                notesSection.setPrefWidth(0.5 * mainContentArea.getWidth());
                HBox.setHgrow(detailsSection, Priority.ALWAYS);
                HBox.setHgrow(notesSection, Priority.ALWAYS);

                HBox detailsAndNotes = new HBox(0, detailsSection, notesSection);
                detailsAndNotes.setAlignment(Pos.TOP_LEFT);
                detailsAndNotes.prefWidthProperty().bind(mainContentArea.widthProperty());

                VBox wrapper = new VBox(closeRow, detailsAndNotes);
                wrapper.setAlignment(Pos.TOP_LEFT);

                wrapper.setOpacity(0);
                wrapper.setTranslateY(80);
                mainContentArea.getChildren().add(wrapper);

                TranslateTransition slideIn = new TranslateTransition(Duration.millis(420), wrapper);
                slideIn.setFromY(80);
                slideIn.setToY(0);
                slideIn.setInterpolator(Interpolator.EASE_OUT);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(420), wrapper);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                slideIn.play();
                fadeIn.play();

                selectedTask = task;
            }

            private void hideDetailsWithAnimation() {
                mainContentArea.getChildren().removeIf(node -> node == detailsSection || node == notesSection || node instanceof VBox || node instanceof HBox);
                selectedTask = null;
                currentlyDisplayedTask = null;
                if (taskListView != null) {
                    taskListView.getSelectionModel().clearSelection(); 
                }
            }
            
            private void createDetailsSection() {
                detailsSection = new VBox(20);
                detailsSection.setPrefWidth(350);
                detailsSection.setPadding(new Insets(20));
                detailsSection.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 0 1;");
            }
            
            private void updateDetailsSection(Task task) {
                detailsSection.getChildren().clear();

                // Title only (no close button here)
                HBox titleBox = new HBox();
                titleBox.setAlignment(Pos.CENTER_LEFT);

                Label detailsTitle = new Label("Task Details");
                detailsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

                titleBox.getChildren().addAll(detailsTitle);

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
                            updateNotifications();
                            // Refresh the priority dashboard
                            contentTabPane.getTabs().get(1).setContent(createPriorityDashboard());
                        } else {
                            updateNotifications(); 
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
                    updateNotifications(); // <-- Add this line
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
            
            
            
        private void showSignInWindow(Stage stage) {
        // Replace 'SignInView' with your actual login window class
        try {
        SignInView signInView = new SignInView();
        signInView.start(stage);
        } catch (Exception e) {
        e.printStackTrace();
        }
}

private void updateNotifications() {
    if (notificationBox == null) return;
    // Remove old notifications and any label except the "Due Soon" title
    notificationBox.getChildren().removeIf(node -> node instanceof HBox || (node instanceof Label && !"Due Soon".equals(((Label) node).getText())));

    LocalDate now = LocalDate.now();
    boolean hasDueSoon = false;
    for (Task task : taskList) {
        if (!"Done".equals(task.getStatus()) && task.getDueDate() != null) {
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(now, task.getDueDate());
            if (daysBetween == 0) {
                hasDueSoon = true;
                HBox notif = new HBox(8);
                notif.setAlignment(Pos.CENTER_LEFT);
                notif.setStyle("-fx-background-color: #fef3c7; -fx-background-radius: 6px; -fx-padding: 6px;");
                Circle warnDot = new Circle(6, Color.web("#f59e0b"));
                Label msg = new Label(task.getTitle() + " due today!");
                msg.setStyle("-fx-font-size: 13px; -fx-text-fill: #b45309;");
                notif.getChildren().addAll(warnDot, msg);
                notificationBox.getChildren().add(notif);
            }
        }
    }
    if (!hasDueSoon) {
        Label none = new Label("No tasks due soon.");
        none.setStyle("-fx-font-size:  13px; -fx-text-fill: #64748b;");
        notificationBox.getChildren().add(none);
    }
}

private VBox notesSection;
private ListView<Note> notesListView;
private ObservableList<Note> noteList = FXCollections.observableArrayList();
private NotePersistence notePersistence = new NotePersistence();
private static final DateTimeFormatter NOTE_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

//private void createNotesSection() {
//    notesSection = new VBox(15);
//    notesSection.setPrefWidth(350);
//    notesSection.setPadding(new Insets(20));
//    notesSection.setStyle("-fx-background-color: #fff; -fx-border-color: #e2e8f0; -fx-border-width: 0 1 0 0;");
//
//    Label notesTitle = new Label("Notes");
//    notesTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
//
//    notesListView = new ListView<>(noteList);
//    notesListView.setCellFactory(lv -> new ListCell<Note>() {
//        @Override
//        protected void updateItem(Note note, boolean empty) {
//            super.updateItem(note, empty);
//            if (empty || note == null) {
//                setText(null);
//                setGraphic(null);
//            } else {
//                VBox noteBox = new VBox(2);
//                Label content = new Label(note.getContent());
//                content.setWrapText(true);
//                content.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");
//                String formattedDate = note.getCreatedAt() != null
//                        ? note.getCreatedAt().format(NOTE_DATE_FORMATTER)
//                        : "";
//                Label date = new Label(formattedDate);
//                date.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
//                noteBox.getChildren().addAll(content, date);
//                setGraphic(noteBox);
//            }
//        }
//    });
//
//    // Double click to edit note
//    notesListView.setOnMouseClicked(e -> {
//        if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
//            Note selected = notesListView.getSelectionModel().getSelectedItem();
//            if (selected != null) showEditNoteDialog(selected);
//        }
//    });
//
//    // Add note area
//    TextArea noteInput = new TextArea();
//    noteInput.setPromptText("Add a note...");
//    noteInput.setPrefRowCount(2);
//    noteInput.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
//
//    Button addNoteBtn = new Button("Add Note");
//    addNoteBtn.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-background-radius: 6px;");
//    addNoteBtn.setOnAction(e -> {
//        String content = noteInput.getText().trim();
//        if (!content.isEmpty() && selectedTask != null) {
//            Note note = new Note(0, content, java.time.LocalDateTime.now(), selectedTask.getId(), user.getId());
//            notePersistence.addNote(note);
//            updateNotesSection(selectedTask);
//            noteInput.clear();
//        }
//    });
//
//    notesSection.getChildren().addAll(notesTitle, notesListView, noteInput, addNoteBtn);
//    VBox.setVgrow(notesListView, Priority.ALWAYS);
//}

//    private void createNotesSection() {
//        notesSection = new VBox(15);
//        notesSection.setPrefWidth(350);
//        notesSection.setPadding(new Insets(20));
//        notesSection.setStyle(
//                "-fx-background-color: #f8fafc;" +
//                        "-fx-border-color: #e2e8f0;" +
//                        "-fx-border-width: 0 1 0 0;" +
//                        "-fx-background-radius: 12px;" +
//                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.1), 8, 0, 2, 2);");
//
//        Label notesTitle = new Label("Notes");
//        notesTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
//
//        notesListView = new ListView<>(noteList);
//        notesListView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
//        notesListView.setCellFactory(lv -> new ListCell<Note>() {
//            @Override
//            protected void updateItem(Note note, boolean empty) {
//                super.updateItem(note, empty);
//                if (empty || note == null) {
//                    setText(null);
//                    setGraphic(null);
//                } else {
//                    VBox noteBox = new VBox(5);
//                    noteBox.setPadding(new Insets(10));
//                    noteBox.setStyle(
//                            "-fx-background-color: #ffffff;" +
//                                    "-fx-border-radius: 8px;" +
//                                    "-fx-background-radius: 8px;" +
//                                    "-fx-border-color: #e2e8f0;" +
//                                    "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.05), 5, 0, 1, 1);");
//
//                    noteBox.setOnMouseEntered(e -> noteBox.setStyle(
//                            "-fx-background-color: #f1f5f9;" +
//                                    "-fx-border-radius: 8px;" +
//                                    "-fx-background-radius: 8px;" +
//                                    "-fx-border-color: #cbd5e1;" +
//                                    "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.1), 6, 0, 2, 2);"));
//
//                    noteBox.setOnMouseExited(e -> noteBox.setStyle(
//                            "-fx-background-color: #ffffff;" +
//                                    "-fx-border-radius: 8px;" +
//                                    "-fx-background-radius: 8px;" +
//                                    "-fx-border-color: #e2e8f0;" +
//                                    "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.05), 5, 0, 1, 1);"));
//
//                    Label content = new Label(note.getContent());
//                    content.setWrapText(true);
//                    content.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");
//
//                    String formattedDate = note.getCreatedAt() != null
//                            ? note.getCreatedAt().format(NOTE_DATE_FORMATTER)
//                            : "";
//
//                    Label date = new Label(formattedDate);
//                    date.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
//
//                    noteBox.getChildren().addAll(content, date);
//                    setGraphic(noteBox);
//                }
//            }
//        });
//
//
//
//        // Double click to edit note
//        notesListView.setOnMouseClicked(e -> {
//            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
//                Note selected = notesListView.getSelectionModel().getSelectedItem();
//                if (selected != null) showEditNoteDialog(selected);
//            }
//        });
//
//        // Add note area
//        TextArea noteInput = new TextArea();
//        noteInput.setPromptText("Add a note...");
//        noteInput.setPrefRowCount(2);
//        noteInput.setStyle(
//                "-fx-background-color: #ffffff;" +
//                        "-fx-border-radius: 6px;" +
//                        "-fx-background-radius: 6px;" +
//                        "-fx-border-color: #cbd5e1;" +
//                        "-fx-padding: 10px;" +
//                        "-fx-font-size: 13px;");
//
//        // Add Note Button
//        Button addNoteBtn = new Button("Add Note");
//        addNoteBtn.setStyle(
//                "-fx-background-color: #4f46e5;" +
//                        "-fx-text-fill: white;" +
//                        "-fx-background-radius: 6px;" +
//                        "-fx-font-size: 14px; -fx-padding: 8px 15px;");
//        addNoteBtn.setOnMouseEntered(e -> addNoteBtn.setStyle(
//                "-fx-background-color: #4338ca;" +
//                        "-fx-text-fill: white;" +
//                        "-fx-background-radius: 6px;" +
//                        "-fx-font-size: 14px; -fx-padding: 8px 15px;"));
//
//        addNoteBtn.setOnMouseExited(e -> addNoteBtn.setStyle(
//                "-fx-background-color: #4f46e5;" +
//                        "-fx-text-fill: white;" +
//                        "-fx-background-radius: 6px;" +
//                        "-fx-font-size: 14px; -fx-padding: 8px 15px;"));
//
//        addNoteBtn.setOnAction(e -> {
//            String content = noteInput.getText().trim();
//            if (!content.isEmpty() && selectedTask != null) {
//                Note note = new Note(0, content, java.time.LocalDateTime.now(), selectedTask.getId(), user.getId());
//                notePersistence.addNote(note);
//                updateNotesSection(selectedTask);
//                noteInput.clear();
//            }
//        });
//
//        notesSection.getChildren().addAll(notesTitle, notesListView, noteInput, addNoteBtn);
//        VBox.setVgrow(notesListView, Priority.ALWAYS);
//    }


    private void createNotesSection() {
        notesSection = new VBox(15);
        notesSection.setPrefWidth(350);
        notesSection.setPadding(new Insets(20));
        notesSection.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #f9fafb, #f3f4f6);" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-width: 0 1 0 0;" +
                        "-fx-background-radius: 15px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0.2, 0, 4);");

        Label notesTitle = new Label("Notes");
        notesTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        notesListView = new ListView<>(noteList);
        notesListView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 5px;");
        notesListView.setCellFactory(lv -> new ListCell<Note>() {
            @Override
            protected void updateItem(Note note, boolean empty) {
                super.updateItem(note, empty);
                if (empty || note == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox noteBox = new VBox(5);
                    noteBox.setPadding(new Insets(12));
                    noteBox.setStyle(
                            "-fx-background-color: #ffffff;" +
                                    "-fx-border-radius: 10px;" +
                                    "-fx-background-radius: 10px;" +
                                    "-fx-border-color: #e5e7eb;" +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0.1, 0, 2);" +
                                    "-fx-cursor: hand;");

                    // Smooth hover effect
                    noteBox.hoverProperty().addListener((obs, oldVal, isHovering) -> {
                        if (isHovering) {
                            noteBox.setStyle(
                                    "-fx-background-color: #f9fafb;" +
                                            "-fx-border-radius: 10px;" +
                                            "-fx-background-radius: 10px;" +
                                            "-fx-border-color: #d1d5db;" +
                                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.2, 0, 3);" +
                                            "-fx-cursor: hand;");
                        } else {
                            noteBox.setStyle(
                                    "-fx-background-color: #ffffff;" +
                                            "-fx-border-radius: 10px;" +
                                            "-fx-background-radius: 10px;" +
                                            "-fx-border-color: #e5e7eb;" +
                                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0.1, 0, 2);" +
                                            "-fx-cursor: hand;");
                        }
                    });

                    // Press effect
                    noteBox.setOnMousePressed(e -> noteBox.setStyle(
                            "-fx-background-color: #f3f4f6;" +
                                    "-fx-border-radius: 10px;" +
                                    "-fx-background-radius: 10px;" +
                                    "-fx-border-color: #d1d5db;" +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0.1, 0, 1);"));

                    Label content = new Label(note.getContent());
                    content.setWrapText(true);
                    content.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");

                    String formattedDate = note.getCreatedAt() != null
                            ? note.getCreatedAt().format(NOTE_DATE_FORMATTER)
                            : "";

                    Label date = new Label(formattedDate);
                    date.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

                    noteBox.getChildren().addAll(content, date);
                    setGraphic(noteBox);
                }
            }
        });

        // Double click to edit note
        notesListView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                Note selected = notesListView.getSelectionModel().getSelectedItem();
                if (selected != null) showEditNoteDialog(selected);
            }
        });

        // Add note area
        TextArea noteInput = new TextArea();
        noteInput.setPromptText("Add a note...");
        noteInput.setPrefRowCount(3);
        noteInput.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: #d1d5db;" +
                        "-fx-padding: 12px;" +
                        "-fx-font-size: 14px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 4, 0.1, 0, 1);");

        // Add Note Button with smooth transition
        Button addNoteBtn = new Button("Add Note");
        addNoteBtn.setStyle(
                "-fx-background-color: #4f46e5;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 10px 20px;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(79,70,229,0.3), 6, 0.2, 0, 2);");

        // Button hover effect
        addNoteBtn.setOnMouseEntered(e -> addNoteBtn.setStyle(
                "-fx-background-color: #4338ca;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 10px 20px;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(67,56,202,0.4), 8, 0.3, 0, 3);"));

        addNoteBtn.setOnMouseExited(e -> addNoteBtn.setStyle(
                "-fx-background-color: #4f46e5;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 10px 20px;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(79,70,229,0.3), 6, 0.2, 0, 2);"));

        // Button press effect
        addNoteBtn.setOnMousePressed(e -> addNoteBtn.setStyle(
                "-fx-background-color: #3730a3;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 10px 20px;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(55,48,163,0.3), 4, 0.1, 0, 1);"));

        addNoteBtn.setOnAction(e -> {
            String content = noteInput.getText().trim();
            if (!content.isEmpty() && selectedTask != null) {
                Note note = new Note(0, content, java.time.LocalDateTime.now(), selectedTask.getId(), user.getId());
                notePersistence.addNote(note);
                updateNotesSection(selectedTask);
                noteInput.clear();
            }
        });

        notesSection.getChildren().addAll(notesTitle, notesListView, noteInput, addNoteBtn);
        VBox.setVgrow(notesListView, Priority.ALWAYS);
    }

private void updateNotesSection(Task task) {
    noteList.setAll(notePersistence.getNotesByTask(task.getId()));
}

//private void showEditNoteDialog(Note note) {
//    Stage dialog = new Stage();
//    dialog.initModality(Modality.APPLICATION_MODAL);
//    dialog.setTitle("Edit Note");
//    dialog.setMinWidth(350);
//
//    VBox content = new VBox(15);
//    content.setPadding(new Insets(20));
//
//    TextArea noteArea = new TextArea(note.getContent());
//    noteArea.setPrefRowCount(3);
//
//    HBox buttons = new HBox(10);
//    Button saveBtn = new Button("Save");
//    Button deleteBtn = new Button("Delete");
//    Button cancelBtn = new Button("Cancel");
//
//    saveBtn.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white;");
//    deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
//    cancelBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #64748b;");
//
//    buttons.getChildren().addAll(saveBtn, deleteBtn, cancelBtn);
//    content.getChildren().addAll(noteArea, buttons);
//
//    Scene dialogScene = new Scene(content);
//    dialogScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
//    dialog.setScene(dialogScene);
//
//    // Set button actions
//    cancelBtn.setOnAction(e -> dialog.close());
//    saveBtn.setOnAction(e -> {
//        note.setContent(noteArea.getText());
//        notePersistence.updateNote(note);
//        updateNotesSection(selectedTask);
//        dialog.close();
//    });
//    deleteBtn.setOnAction(e -> {
//        notePersistence.deleteNote(note.getId());
//        updateNotesSection(selectedTask);
//        dialog.close();
//    });
//    cancelBtn.setOnAction(e -> dialog.close());
//
//    dialog.show();
//}
private void showEditNoteDialog(Note note) {
    // Create dialog
    Stage dialog = new Stage();
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.setTitle("Edit Note");
    dialog.setMinWidth(400);

    // Container with spacing and padding
    VBox content = new VBox(20);
    content.setPadding(new Insets(25));
    content.setStyle(
            "-fx-background-color: #ffffff;" +
                    "-fx-background-radius: 12px;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.1), 10, 0, 2, 2);"
    );

    // Input for note content
    TextArea noteArea = new TextArea(note.getContent());
    noteArea.setPromptText("Edit your note...");
    noteArea.setPrefRowCount(3);
    noteArea.setStyle(
            "-fx-background-color: #f8fafc;" +
                    "-fx-border-color: #cbd5e1;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-padding: 12px;" +
                    "-fx-font-size: 13px;"
    );
    noteArea.setOnMouseEntered(e -> noteArea.setStyle(
            "-fx-background-color: #ffffff;" +
                    "-fx-border-color: #94a3b8;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-padding: 12px;" +
                    "-fx-font-size: 13px;"
    ));
    noteArea.setOnMouseExited(e -> noteArea.setStyle(
            "-fx-background-color: #f8fafc;" +
                    "-fx-border-color: #cbd5e1;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-padding: 12px;" +
                    "-fx-font-size: 13px;"
    ));

    // Buttons container
    HBox buttons = new HBox(15);
    buttons.setAlignment(Pos.CENTER_RIGHT);

    // Save Button
    Button saveBtn = new Button("Save");
    saveBtn.setStyle(
            "-fx-background-color: #4f46e5;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-font-size: 14px; -fx-padding: 8px 15px;"
    );
    saveBtn.setOnMouseEntered(e -> saveBtn.setStyle(
            "-fx-background-color: #4338ca;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-font-size: 14px; -fx-padding: 8px 15px;"
    ));
    saveBtn.setOnMouseExited(e -> saveBtn.setStyle(
            "-fx-background-color: #4f46e5;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-font-size: 14px; -fx-padding: 8px 15px;"
    ));

    // Delete Button
    Button deleteBtn = new Button("Delete");
    deleteBtn.setStyle(
            "-fx-background-color: #ef4444;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-font-size: 14px; -fx-padding: 8px 15px;"
    );
    deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
            "-fx-background-color: #dc2626;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-font-size: 14px; -fx-padding: 8px 15px;"
    ));
    deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(
            "-fx-background-color: #ef4444;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-font-size: 14px; -fx-padding: 8px 15px;"
    ));

    // Cancel Button
    Button cancelBtn = new Button("Cancel");
    cancelBtn.setStyle(
            "-fx-background-color: #e2e8f0;" +
                    "-fx-text-fill: #64748b;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-font-size: 14px; -fx-padding: 8px 15px;"
    );
    cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(
            "-fx-background-color: #cbd5e1;" +
                    "-fx-text-fill: #475569;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-font-size: 14px; -fx-padding: 8px 15px;"
    ));
    cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(
            "-fx-background-color: #e2e8f0;" +
                    "-fx-text-fill: #64748b;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-font-size: 14px; -fx-padding: 8px 15px;"
    ));

    // Add buttons to the container
    buttons.getChildren().addAll(saveBtn, deleteBtn, cancelBtn);

    // Add content to the dialog
    content.getChildren().addAll(noteArea, buttons);

    // Setup scene
    Scene dialogScene = new Scene(content);
    dialogScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    dialog.setScene(dialogScene);

    // Button actions
    saveBtn.setOnAction(e -> {
        note.setContent(noteArea.getText());
        notePersistence.updateNote(note);
        updateNotesSection(selectedTask);
        dialog.close();
    });

    deleteBtn.setOnAction(e -> {
        notePersistence.deleteNote(note.getId());
        updateNotesSection(selectedTask);
        dialog.close();
    });

    cancelBtn.setOnAction(e -> dialog.close());

    dialog.show();
}
}