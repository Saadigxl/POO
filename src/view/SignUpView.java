package view;

import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import persistence.UserPersistence;

public class SignUpView extends Application {
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8fafc, #e0e7ff);");

        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(60, 50, 60, 50));
        contentBox.setMaxWidth(500);

        // Animated logo
        StackPane logoContainer = new StackPane();
        Circle logoCircle = new Circle(40);
        logoCircle.setFill(Color.web("#4f46e5"));
        logoCircle.setOpacity(0.9);

        Label logoSymbol = new Label("✏️");
        logoSymbol.setStyle("-fx-font-size: 42px; -fx-text-fill: white; -fx-font-weight: bold;");
        logoContainer.getChildren().addAll(logoCircle, logoSymbol);

        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(3), logoCircle);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(1);
        rotateTransition.play();

        // Title
        Label titleLabel = new Label("Create Your Account");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Form container
        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setMaxWidth(350);

        // Username field
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        styleTextField(usernameField);

        // Password field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Create a password");
        styleTextField(passwordField);

        // Confirm Password field
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        styleTextField(confirmPasswordField);

        // Register button
        Button registerBtn = new Button("Create Account");
        stylePrimaryButton(registerBtn);

        // Divider
        HBox divider = new HBox();
        divider.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0; -fx-padding: 15 0;");

        // Sign In link
        Label signInLabel = new Label("Already have an account?");
        Hyperlink signInLink = new Hyperlink("Sign in");
        signInLink.setStyle("-fx-text-fill: #4f46e5; -fx-font-weight: bold;");
        HBox signInBox = new HBox(5, signInLabel, signInLink);
        signInBox.setAlignment(Pos.CENTER);

        formBox.getChildren().addAll(
                usernameField,
                passwordField,
                confirmPasswordField,
                registerBtn,
                divider,
                signInBox
        );

        contentBox.getChildren().addAll(logoContainer, titleLabel, formBox);
        root.setCenter(contentBox);

        // Button actions
        registerBtn.setOnAction(e -> {
            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                StyledAlerts.showErrorAlert("Password Mismatch", "The passwords you entered don't match.");
                return;
            }

            UserPersistence up = new UserPersistence();
            boolean ok = up.registerUser(usernameField.getText(), passwordField.getText());
            if (ok) {
                StyledAlerts.showInformationAlert("Registration Successful",
                        "Your account has been created successfully!\nYou can now sign in.");
                new SignInView().start(primaryStage);
            } else {
                StyledAlerts.showErrorAlert("Registration Failed",
                        "Username already exists. Please choose a different username.");
            }
        });

        signInLink.setOnAction(e -> new SignInView().start(primaryStage));

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Sign Up - TaskFlow");
        primaryStage.show();

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void styleTextField(TextField field) {
        field.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; " +
                "-fx-border-radius: 6px; -fx-padding: 12px 15px; -fx-font-size: 14px;");
        field.setMaxWidth(350);
    }

    private void stylePrimaryButton(Button button) {
        button.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-padding: 12px 30px; " +
                "-fx-background-radius: 30px; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(79, 70, 229, 0.3), 10, 0, 0, 4);");

        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: #4338ca; -fx-text-fill: white; " +
                        "-fx-font-size: 16px; -fx-padding: 12px 30px; " +
                        "-fx-background-radius: 30px; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(79, 70, 229, 0.4), 12, 0, 0, 6);")
        );

        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; " +
                        "-fx-font-size: 16px; -fx-padding: 12px 30px; " +
                        "-fx-background-radius: 30px; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(79, 70, 229, 0.3), 10, 0, 0, 4);")
        );
    }
}