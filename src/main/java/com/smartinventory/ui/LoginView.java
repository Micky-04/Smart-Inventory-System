package com.smartinventory.ui;

import com.smartinventory.model.User;
import com.smartinventory.repository.InMemoryUserRepository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class LoginView extends BorderPane {

    private final InMemoryUserRepository userRepository = InMemoryUserRepository.getInstance();

    public LoginView(Consumer<User> onLoginSuccess) {
        setStyle("-fx-background-color: linear-gradient(to bottom, #0f172a, #1f2937);");

        VBox card = new VBox(12);
        card.setPadding(new Insets(24));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.35), 18,0,0,6);");

        Label title = new Label("Smart Inventory Login");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username (owner / manager / staff)");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (e.g. owner123)");

        Label hintLabel = new Label("Demo logins: owner/owner123, manager/manager123, staff/staff123");
        hintLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");

        Button loginButton = new Button("Login");
        loginButton.setDefaultButton(true);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold;");

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            userRepository.authenticate(username, password)
                    .ifPresentOrElse(user -> onLoginSuccess.accept(user),
                            () -> {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Login Failed");
                                alert.setHeaderText(null);
                                alert.setContentText("Invalid username or password.");
                                alert.showAndWait();
                            });
        });

        card.getChildren().addAll(title, usernameField, passwordField, loginButton, hintLabel);

        setCenter(card);
        BorderPane.setAlignment(card, Pos.CENTER);
        setPadding(new Insets(40));
    }
}

