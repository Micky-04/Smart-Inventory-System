package com.smartinventory.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class PlaceholderView extends StackPane {

    public PlaceholderView(String title) {
        setPadding(new Insets(24));
        setStyle("-fx-background-color: linear-gradient(to bottom right, #020617, #111827);");

        VBox card = new VBox(8);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color: #020617; -fx-background-radius: 18; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.6), 18,0,0,6);");
        card.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #e5e7eb; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label subLabel = new Label("This module will be implemented in the upcoming parts with full functionality.");
        subLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");

        card.getChildren().addAll(titleLabel, subLabel);

        setAlignment(card, Pos.CENTER);
        getChildren().add(card);
    }
}

