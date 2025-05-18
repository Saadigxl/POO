package view;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class WelcomeScreen extends Application {
    public static void show(Stage primaryStage, Runnable onStart) {
        BorderPane welcomeRoot = new BorderPane();
        welcomeRoot.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8fafc, #e0e7ff);");

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

        Label welcomeLabel = new Label("TaskFlow");
        welcomeLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label subtitle = new Label("Organize your tasks. Boost your productivity.");
        subtitle.setStyle("-fx-font-size: 18px; -fx-text-fill: #64748b; -fx-font-weight: normal;");

        HBox featureBox = new HBox(40);
        featureBox.setAlignment(Pos.CENTER);
        featureBox.setPadding(new Insets(30, 0, 40, 0));

        VBox feature1 = createFeatureBox("✓", "Track Tasks", "Organize and manage all your tasks in one place");
        VBox feature2 = createFeatureBox("⚡", "Set Priorities", "Prioritize tasks and focus on what's important");
        VBox feature3 = createFeatureBox("📊", "Monitor Progress", "Track your productivity and task completion rate");

        featureBox.getChildren().addAll(feature1, feature2, feature3);

        Button startButton = new Button("Get Started");
        startButton.getStyleClass().add("welcome-button");
        startButton.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-size: 16px; " +
                "-fx-padding: 12px 30px; -fx-background-radius: 30px; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(79, 70, 229, 0.3), 10, 0, 0, 4);");

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

        startButton.setOnAction(e -> fadeTransition(contentBox, onStart));

        contentBox.getChildren().addAll(logoContainer, welcomeLabel, subtitle, featureBox, startButton);

        welcomeRoot.setCenter(contentBox);

        Scene welcomeScene = new Scene(welcomeRoot, 900, 700);
        welcomeScene.getStylesheets().add(WelcomeScreen.class.getResource("/styles.css").toExternalForm());
        primaryStage.getIcons().add(new Image("/icon.png"));
        primaryStage.setScene(welcomeScene);
        primaryStage.setTitle("Welcome to TaskFlow");
        primaryStage.show();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), welcomeRoot);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private static VBox createFeatureBox(String icon, String title, String description) {
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

    private static void fadeTransition(VBox contentBox, Runnable onFinished) {
        FadeTransition fade = new FadeTransition(Duration.millis(400), contentBox);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> onFinished.run());
        fade.play();
    }

    @Override
    public void start(Stage primaryStage) {
        show(primaryStage, () -> {
            SignInView signInView = new SignInView();
            try {
                signInView.start(primaryStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}