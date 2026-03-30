package com.smartinventory.ui;

import com.smartinventory.model.Category;
import com.smartinventory.model.Product;
import com.smartinventory.repository.InMemoryCatalogRepository;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.stream.Collectors;

public class InventoryView extends BorderPane {

    private final InMemoryCatalogRepository catalogRepository = InMemoryCatalogRepository.getInstance();

    private final TableView<Product> stockTable = new TableView<>();
    private final ObservableList<Product> stockItems = FXCollections.observableArrayList();

    private final TextField searchField = new TextField();
    private final ComboBox<Category> categoryFilter = new ComboBox<>();
    private final ComboBox<String> stockStatusFilter = new ComboBox<>();

    private final Spinner<Integer> adjustQuantitySpinner = new Spinner<>(-1000, 1000, 0);
    private final TextArea adjustReasonField = new TextArea();

    private static final int LOW_STOCK_THRESHOLD = 10;

    public InventoryView() {
        buildLayout();
        loadData();
    }

    private void buildLayout() {
        setPadding(new Insets(16));
        setStyle("-fx-background-color: linear-gradient(to bottom, #f5f7fa, #e5e7eb);");

        VBox content = new VBox(12);

        // Top card: inventory overview chips
        HBox overviewCard = new HBox(16);
        overviewCard.setPadding(new Insets(12));
        overviewCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.10), 8,0,0,2);");

        Label title = new Label("Inventory Overview");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label totalSkusChip = new Label();
        totalSkusChip.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #1d4ed8; -fx-padding: 6 12 6 12; -fx-background-radius: 999;");

        Label totalUnitsChip = new Label();
        totalUnitsChip.setStyle("-fx-background-color: #ecfdf3; -fx-text-fill: #15803d; -fx-padding: 6 12 6 12; -fx-background-radius: 999;");

        Label lowStockChip = new Label();
        lowStockChip.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #b91c1c; -fx-padding: 6 12 6 12; -fx-background-radius: 999;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        overviewCard.getChildren().addAll(title, spacer, totalSkusChip, totalUnitsChip, lowStockChip);

        // Filter + table card
        VBox tableCard = new VBox(10);
        tableCard.setPadding(new Insets(12));
        tableCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.10), 8,0,0,2);");

        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("Search by name, SKU, barcode...");
        categoryFilter.setPromptText("All categories");
        stockStatusFilter.setPromptText("All stock status");
        stockStatusFilter.setItems(FXCollections.observableArrayList("All", "Low stock", "Out of stock", "Healthy"));
        stockStatusFilter.getSelectionModel().selectFirst();

        Button applyFiltersBtn = new Button("Apply");
        applyFiltersBtn.setOnAction(e -> applyFilters());

        Button clearFiltersBtn = new Button("Clear");
        clearFiltersBtn.setOnAction(e -> {
            searchField.clear();
            categoryFilter.getSelectionModel().clearSelection();
            stockStatusFilter.getSelectionModel().selectFirst();
            applyFilters();
        });

        HBox.setHgrow(searchField, Priority.ALWAYS);
        filterBar.getChildren().addAll(new Label("Filter:"), searchField, categoryFilter, stockStatusFilter, applyFiltersBtn, clearFiltersBtn);

        configureStockTable();

        tableCard.getChildren().addAll(filterBar, stockTable);
        VBox.setVgrow(stockTable, Priority.ALWAYS);

        // Adjustment card
        VBox adjustCard = new VBox(10);
        adjustCard.setPadding(new Insets(12));
        adjustCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.10), 8,0,0,2);");

        Label adjustTitle = new Label("Stock Adjustment");
        adjustTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        adjustQuantitySpinner.setPrefWidth(120);

        HBox adjustRow = new HBox(10);
        adjustRow.setAlignment(Pos.CENTER_LEFT);
        Label qtyLabel = new Label("Adjust by (+ add / - remove):");
        qtyLabel.setStyle("-fx-font-size: 12px;");

        adjustRow.getChildren().addAll(qtyLabel, adjustQuantitySpinner);

        adjustReasonField.setPromptText("Reason (e.g. damage, correction, audit)");
        adjustReasonField.setPrefRowCount(2);

        Button applyAdjustmentBtn = new Button("Apply Adjustment");
        applyAdjustmentBtn.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-font-weight: bold;");
        applyAdjustmentBtn.setOnAction(e -> applyAdjustment());

        adjustCard.getChildren().addAll(adjustTitle, adjustRow, adjustReasonField, applyAdjustmentBtn);

        content.getChildren().addAll(overviewCard, tableCard, adjustCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        setCenter(content);

        // After layout, populate overview chips once data loads
        updateOverview(totalSkusChip, totalUnitsChip, lowStockChip);
    }

    private void configureStockTable() {
        TableColumn<Product, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        nameCol.setPrefWidth(260);

        TableColumn<Product, String> skuCol = new TableColumn<>("SKU");
        skuCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSku()));

        TableColumn<Product, String> categoriesCol = new TableColumn<>("Categories");
        categoriesCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCategories().stream()
                        .map(Category::getName)
                        .collect(Collectors.joining(", "))
        ));

        TableColumn<Product, Number> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getStockQuantity()));

        TableColumn<Product, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(stockStatusFor(c.getValue())));

        stockTable.getColumns().addAll(nameCol, skuCol, categoriesCol, stockCol, statusCol);
        stockTable.setItems(stockItems);
    }

    private void loadData() {
        List<Product> allProducts = catalogRepository.getAllProducts();
        stockItems.setAll(allProducts);

        List<Category> categories = catalogRepository.getAllCategories();
        categoryFilter.setItems(FXCollections.observableArrayList(categories));
    }

    private void applyFilters() {
        String text = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        Category selectedCategory = categoryFilter.getSelectionModel().getSelectedItem();
        String stockFilter = stockStatusFilter.getSelectionModel().getSelectedItem();

        List<Product> filtered = catalogRepository.getAllProducts().stream()
                .filter(p -> text.isEmpty()
                        || p.getName().toLowerCase().contains(text)
                        || p.getSku().toLowerCase().contains(text)
                        || (p.getBarcode() != null && p.getBarcode().toLowerCase().contains(text)))
                .filter(p -> selectedCategory == null || p.getCategories().contains(selectedCategory))
                .filter(p -> {
                    if (stockFilter == null || "All".equals(stockFilter)) return true;
                    String status = stockStatusFor(p);
                    return status.equalsIgnoreCase(stockFilter);
                })
                .collect(Collectors.toList());

        stockItems.setAll(filtered);
    }

    private String stockStatusFor(Product product) {
        int qty = product.getStockQuantity();
        if (qty <= 0) return "Out of stock";
        if (qty <= LOW_STOCK_THRESHOLD) return "Low stock";
        return "Healthy";
    }

    private void applyAdjustment() {
        Product selected = stockTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection", "Select a product to adjust stock.");
            return;
        }
        int delta = adjustQuantitySpinner.getValue();
        if (delta == 0) {
            showAlert("Validation", "Adjustment value cannot be zero.");
            return;
        }
        String reason = adjustReasonField.getText().trim();
        if (reason.isEmpty()) {
            showAlert("Validation", "Please provide a reason for the adjustment.");
            return;
        }

        int newQty = Math.max(0, selected.getStockQuantity() + delta);
        selected.setStockQuantity(newQty);
        stockTable.refresh();
        applyFilters(); // re-apply filters so status updates

        // Reset controls
        adjustQuantitySpinner.getValueFactory().setValue(0);
        adjustReasonField.clear();
    }

    private void updateOverview(Label totalSkusChip, Label totalUnitsChip, Label lowStockChip) {
        List<Product> all = catalogRepository.getAllProducts();
        int totalSkus = all.size();
        int totalUnits = all.stream().mapToInt(Product::getStockQuantity).sum();
        long lowOrOut = all.stream()
                .filter(p -> p.getStockQuantity() <= LOW_STOCK_THRESHOLD)
                .count();

        totalSkusChip.setText("SKUs: " + totalSkus);
        totalUnitsChip.setText("Units: " + totalUnits);
        lowStockChip.setText("Low/Out: " + lowOrOut);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

