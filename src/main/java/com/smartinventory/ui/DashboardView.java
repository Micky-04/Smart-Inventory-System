package com.smartinventory.ui;

import com.smartinventory.model.Category;
import com.smartinventory.model.Product;
import com.smartinventory.model.StoreType;
import com.smartinventory.repository.InMemoryCatalogRepository;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardView extends BorderPane {

    private final InMemoryCatalogRepository catalogRepository = InMemoryCatalogRepository.getInstance();

    private final ComboBox<StoreType> storeTypeComboBox = new ComboBox<>();
    private final Label totalProductsLabel = new Label();
    private final Label totalStockLabel = new Label();

    private final TextField searchField = new TextField();
    private final ComboBox<Category> categoryFilterComboBox = new ComboBox<>();
    private final TableView<Product> productsTable = new TableView<>();

    public DashboardView() {
        buildLayout();
        refreshDashboard();
        refreshProductTable();
    }

    private void buildLayout() {
        setPadding(new Insets(18));
        setStyle("-fx-background-color: linear-gradient(to bottom right, #0f172a, #1e293b);");

        VBox root = new VBox(14);

        // Hero card with gradient and store type
        HBox heroCard = new HBox(24);
        heroCard.setPadding(new Insets(16));
        heroCard.setStyle("-fx-background-color: linear-gradient(to right, #2563eb, #7c3aed); -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.4), 18,0,0,6);");

        VBox heroText = new VBox(6);
        Label heroTitle = new Label("Smart Inventory Snapshot");
        heroTitle.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label heroSubtitle = new Label("Monitor stock, categories and products at a glance.");
        heroSubtitle.setStyle("-fx-text-fill: rgba(249,250,251,0.85); -fx-font-size: 12px;");

        HBox storeTypeBox = new HBox(8);
        storeTypeBox.setAlignment(Pos.CENTER_LEFT);
        Label storeTypeLabel = new Label("Store Type");
        storeTypeLabel.setStyle("-fx-text-fill: #bfdbfe; -fx-font-size: 12px; -fx-font-weight: bold;");
        storeTypeComboBox.setItems(FXCollections.observableArrayList(StoreType.values()));
        storeTypeComboBox.getSelectionModel().select(StoreType.MINI_MART);
        storeTypeComboBox.setOnAction(e -> refreshDashboard());
        storeTypeComboBox.setStyle("-fx-background-radius: 999; -fx-padding: 2 8 2 8;");
        storeTypeBox.getChildren().addAll(storeTypeLabel, storeTypeComboBox);

        heroText.getChildren().addAll(heroTitle, heroSubtitle, storeTypeBox);

        VBox kpiColumn = new VBox(6);
        kpiColumn.setAlignment(Pos.CENTER_RIGHT);

        totalProductsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        totalStockLabel.setStyle("-fx-text-fill: #e5e7eb; -fx-font-size: 13px;");

        kpiColumn.getChildren().addAll(totalProductsLabel, totalStockLabel);
        HBox.setHgrow(kpiColumn, Priority.ALWAYS);

        heroCard.getChildren().addAll(heroText, new Region(), kpiColumn);
        HBox.setHgrow(heroCard.getChildren().get(1), Priority.ALWAYS);

        // Middle row: category pie chart + quick filters
        HBox middleRow = new HBox(14);

        VBox chartCard = new VBox(8);
        chartCard.setPadding(new Insets(12));
        chartCard.setStyle("-fx-background-color: #0b1120; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.5), 14,0,0,4);");

        Label chartTitle = new Label("Stock by Category");
        chartTitle.setStyle("-fx-text-fill: #e5e7eb; -fx-font-size: 13px; -fx-font-weight: bold;");

        PieChart pieChart = new PieChart();
        pieChart.setLegendVisible(false);
        pieChart.setLabelsVisible(false);
        pieChart.setPrefHeight(180);
        pieChart.setClockwise(true);

        chartCard.getChildren().addAll(chartTitle, pieChart);

        VBox quickSearchCard = new VBox(8);
        quickSearchCard.setPadding(new Insets(12));
        quickSearchCard.setStyle("-fx-background-color: #020617; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.5), 14,0,0,4);");

        Label quickTitle = new Label("Quick Product Lookup");
        quickTitle.setStyle("-fx-text-fill: #e5e7eb; -fx-font-size: 13px; -fx-font-weight: bold;");

        HBox searchBar = new HBox(8);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("Search by name, SKU, barcode");
        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> refreshProductTable());
        searchButton.setDefaultButton(true);

        categoryFilterComboBox.setPromptText("Filter by category");
        List<Category> categories = catalogRepository.getAllCategories();
        categoryFilterComboBox.setItems(FXCollections.observableArrayList(categories));

        Button clearFiltersButton = new Button("Clear");
        clearFiltersButton.setOnAction(e -> {
            searchField.clear();
            categoryFilterComboBox.getSelectionModel().clearSelection();
            refreshProductTable();
        });

        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchBar.getChildren().addAll(searchField, categoryFilterComboBox, searchButton, clearFiltersButton);

        quickSearchCard.getChildren().addAll(quickTitle, searchBar);

        middleRow.getChildren().addAll(chartCard, quickSearchCard);
        HBox.setHgrow(chartCard, Priority.ALWAYS);
        HBox.setHgrow(quickSearchCard, Priority.ALWAYS);

        // Bottom: table card
        VBox tableCard = new VBox(6);
        tableCard.setPadding(new Insets(10));
        tableCard.setStyle("-fx-background-color: #020617; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.45), 14,0,0,4);");

        Label tableTitle = new Label("Recent & Matching Products");
        tableTitle.setStyle("-fx-text-fill: #e5e7eb; -fx-font-size: 13px; -fx-font-weight: bold;");

        configureTable();

        tableCard.getChildren().addAll(tableTitle, productsTable);
        VBox.setVgrow(productsTable, Priority.ALWAYS);

        root.getChildren().addAll(heroCard, middleRow, tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        setCenter(root);

        // Fill chart after layout
        refreshCategoryChart(pieChart);
    }

    private void configureTable() {
        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> javafx.beans.property.SimpleStringProperty
                .stringExpression(javafx.beans.binding.Bindings.createStringBinding(c.getValue()::getName)));
        nameCol.setPrefWidth(280);

        TableColumn<Product, String> skuCol = new TableColumn<>("SKU");
        skuCol.setCellValueFactory(c -> javafx.beans.property.SimpleStringProperty
                .stringExpression(javafx.beans.binding.Bindings.createStringBinding(c.getValue()::getSku)));

        TableColumn<Product, String> barcodeCol = new TableColumn<>("Barcode");
        barcodeCol.setCellValueFactory(c -> javafx.beans.property.SimpleStringProperty
                .stringExpression(javafx.beans.binding.Bindings.createStringBinding(c.getValue()::getBarcode)));

        TableColumn<Product, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(c -> javafx.beans.property.SimpleStringProperty
                .stringExpression(javafx.beans.binding.Bindings.createStringBinding(() ->
                        c.getValue().getBrand() == null ? "" : c.getValue().getBrand())));

        TableColumn<Product, Number> priceCol = new TableColumn<>("Selling Price");
        priceCol.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getSellingPrice()));

        TableColumn<Product, Number> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getStockQuantity()));

        TableColumn<Product, String> categoriesCol = new TableColumn<>("Categories");
        categoriesCol.setCellValueFactory(c -> javafx.beans.property.SimpleStringProperty
                .stringExpression(javafx.beans.binding.Bindings.createStringBinding(() ->
                        String.join(", ", c.getValue().getCategories()
                                .stream()
                                .map(Category::getName)
                                .toList()))));

        productsTable.getColumns().addAll(nameCol, skuCol, barcodeCol, brandCol, priceCol, stockCol, categoriesCol);
    }

    private void refreshDashboard() {
        List<Product> allProducts = catalogRepository.getAllProducts();
        int totalProducts = allProducts.size();
        int totalStock = allProducts.stream().mapToInt(Product::getStockQuantity).sum();

        totalProductsLabel.setText("Total Products: " + totalProducts);
        totalStockLabel.setText("Total Stock Units: " + totalStock);
    }

    private void refreshProductTable() {
        String query = searchField.getText();
        Category category = categoryFilterComboBox.getSelectionModel().getSelectedItem();
        List<Product> results = catalogRepository.searchProducts(query, category);
        productsTable.setItems(FXCollections.observableArrayList(results));
    }

    private void refreshCategoryChart(PieChart pieChart) {
        List<Product> all = catalogRepository.getAllProducts();
        Map<String, Integer> byCategory = all.stream()
                .flatMap(p -> p.getCategories().stream().map(c -> Map.entry(c.getName(), p.getStockQuantity())))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Integer::sum
                ));

        pieChart.getData().clear();
        byCategory.forEach((name, qty) -> {
            if (qty > 0) {
                pieChart.getData().add(new PieChart.Data(name, qty));
            }
        });
    }
}

