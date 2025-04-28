package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import persistence.UserPersistence;

public class SignUpView extends Application {
    @Override
    public void start(Stage primaryStage) {
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button registerBtn = new Button("Register");
        Button backBtn = new Button("Back to Sign In");

        registerBtn.setOnAction(e -> {
            UserPersistence up = new UserPersistence();
            boolean ok = up.registerUser(usernameField.getText(), passwordField.getText());
            if (ok) {
                new Alert(Alert.AlertType.INFORMATION, "Registration successful!").showAndWait();
                new SignInView().start(primaryStage);
            } else {
                new Alert(Alert.AlertType.ERROR, "Username already exists!").showAndWait();
            }
        });

        backBtn.setOnAction(e -> {
            new SignInView().start(primaryStage);
        });

        VBox vbox = new VBox(10, new Label("Username:"), usernameField, new Label("Password:"), passwordField, registerBtn, backBtn);
        primaryStage.setScene(new Scene(vbox, 300, 200));
        primaryStage.setTitle("Sign Up");
        primaryStage.show();
    }
}