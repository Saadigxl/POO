package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import persistence.UserPersistence;
import model.User;
public class SignInView extends Application {
    @Override
    public void start(Stage primaryStage) {
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button signInBtn = new Button("Sign In");
        Button signUpBtn = new Button("Sign Up");

        signInBtn.setOnAction(e -> {
            UserPersistence up = new UserPersistence();
            String username = usernameField.getText();
            boolean ok = up.authenticateUser(username, passwordField.getText());
            if (ok) {
                User user = up.getUserByUsername(username);
                MainView mainView = new MainView();
                mainView.setUser(user);
                mainView.start(primaryStage);
            } else {
                new Alert(Alert.AlertType.ERROR, "Invalid credentials").showAndWait();
            }
        });

        signUpBtn.setOnAction(e -> {
            new SignUpView().start(primaryStage);
        });

        VBox vbox = new VBox(10, new Label("Username:"), usernameField, new Label("Password:"), passwordField, signInBtn, signUpBtn);
        primaryStage.setScene(new Scene(vbox, 300, 200));
        primaryStage.setTitle("Sign In");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}