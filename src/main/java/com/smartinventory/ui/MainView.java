package com.smartinventory.ui;

import com.smartinventory.model.User;
import com.smartinventory.model.UserRole;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MainView extends BorderPane {

    private final User currentUser;

    private final VBox sidebar = new VBox(10);
    private final Label headerTitle = new Label("Dashboard");

    private final DashboardView dashboardView = new DashboardView();
    private final ProductsView productsView = new ProductsView();
    private final InventoryView inventoryView = new InventoryView();
    private final PlaceholderView purchasingView = new PlaceholderView("Purchasing & Suppliers");
    private final SalesView salesView = new SalesView();
    private final PlaceholderView reportsView = new PlaceholderView("Reports & Analytics");
    private final PlaceholderView settingsView = new PlaceholderView("Settings & Configuration");

    public MainView(User currentUser) {
        this.currentUser = currentUser;
        buildLayout();
        showDashboard();
    }

    private void buildLayout() {
        // Sidebar
        sidebar.setPadding(new Insets(16));
        sidebar.setStyle("-fx-background-color: linear-gradient(to bottom, #111827, #1f2937);");

        Label appTitle = new Label("Smart Inventory");
        appTitle.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        appTitle.setPadding(new Insets(0, 0, 4, 0));

        Label userLabel = new Label(currentUser.getUsername() + " (" + currentUser.getRole().getLabel() + ")");
        userLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");
        userLabel.setPadding(new Insets(0, 0, 12, 0));

        Button dashboardBtn = createNavButton("Dashboard", this::showDashboard, true);
        Button productsBtn = createNavButton("Products", this::showProducts, canAccessProducts());
        Button inventoryBtn = createNavButton("Inventory", this::showInventory, canAccessInventory());
        Button purchasingBtn = createNavButton("Purchasing", this::showPurchasing, canAccessPurchasing());
        Button salesBtn = createNavButton("Sales / POS", this::showSales, canAccessSales());
        Button reportsBtn = createNavButton("Reports", this::showReports, canAccessReports());
        Button settingsBtn = createNavButton("Settings", this::showSettings, canAccessSettings());

        sidebar.getChildren().addAll(
                appTitle,
                userLabel,
                dashboardBtn,
                productsBtn,
                inventoryBtn,
                purchasingBtn,
                salesBtn,
                reportsBtn,
                settingsBtn
        );

        VBox.setVgrow(dashboardBtn, Priority.NEVER);

        // Header
        BorderPane header = new BorderPane();
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.12), 8,0,0,2);");
        headerTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        header.setLeft(headerTitle);

        setLeft(sidebar);
        setTop(header);
        setPadding(new Insets(0));
        setStyle("-fx-background-color: #e5e7eb;");
    }

    private Button createNavButton(String text, Runnable action, boolean enabled) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        if (enabled) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e5e7eb; -fx-font-size: 13px;");
            btn.setOnAction(e -> action.run());
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #374151; -fx-text-fill: white; -fx-font-size: 13px;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e5e7eb; -fx-font-size: 13px;"));
        } else {
            btn.setDisable(true);
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7280; -fx-font-size: 13px;");
        }
        return btn;
    }

    private boolean canAccessProducts() {
        return currentUser.getRole() == UserRole.OWNER || currentUser.getRole() == UserRole.MANAGER;
    }

    private boolean canAccessInventory() {
        return currentUser.getRole() == UserRole.OWNER || currentUser.getRole() == UserRole.MANAGER;
    }

    private boolean canAccessPurchasing() {
        return currentUser.getRole() == UserRole.OWNER || currentUser.getRole() == UserRole.MANAGER;
    }

    private boolean canAccessSales() {
        return true; // All roles can access sales/POS
    }

    private boolean canAccessReports() {
        return currentUser.getRole() == UserRole.OWNER || currentUser.getRole() == UserRole.MANAGER;
    }

    private boolean canAccessSettings() {
        return currentUser.getRole() == UserRole.OWNER;
    }

    private void showDashboard() {
        headerTitle.setText("Dashboard");
        setCenter(dashboardView);
    }

    private void showProducts() {
        headerTitle.setText("Products & Categories");
        setCenter(productsView);
    }

    private void showInventory() {
        headerTitle.setText("Inventory Management");
        setCenter(inventoryView);
    }

    private void showPurchasing() {
        headerTitle.setText("Purchasing & Suppliers");
        setCenter(purchasingView);
    }

    private void showSales() {
        headerTitle.setText("Sales / POS");
        setCenter(salesView);
    }

    private void showReports() {
        headerTitle.setText("Reports & Analytics");
        setCenter(reportsView);
    }

    private void showSettings() {
        headerTitle.setText("Settings & Configuration");
        setCenter(settingsView);
    }
}
