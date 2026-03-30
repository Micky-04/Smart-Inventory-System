package com.smartinventory;

import com.smartinventory.model.User;
import com.smartinventory.ui.LoginView;
import com.smartinventory.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SmartInventoryApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        showLogin(primaryStage);
    }

    private void showLogin(Stage stage) {
        LoginView loginView = new LoginView(user -> showMain(stage, user));
        Scene scene = new Scene(loginView, 800, 500);
        stage.setTitle("Smart Inventory Management System - Login");
        stage.setScene(scene);
        stage.show();
    }

    private void showMain(Stage stage, User user) {
        MainView mainView = new MainView(user);
        Scene scene = new Scene(mainView, 1200, 720);
        stage.setTitle("Smart Inventory Management System");
        stage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

