package com.smartinventory.ui;

import com.smartinventory.model.Category;
import com.smartinventory.model.Product;
import com.smartinventory.repository.InMemoryCatalogRepository;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.stream.Collectors;

public class ProductsView extends BorderPane {

    private final InMemoryCatalogRepository catalogRepository = InMemoryCatalogRepository.getInstance();

    private final ListView<Category> categoryListView = new ListView<>();
    private final ObservableList<Category> categoryItems = FXCollections.observableArrayList();

    private final TableView<Product> productsTable = new TableView<>();
    private final ObservableList<Product> productItems = FXCollections.observableArrayList();

    private final TextField nameField = new TextField();
    private final TextField skuField = new TextField();
    private final TextField barcodeField = new TextField();
    private final TextField brandField = new TextField();
    private final TextField purchasePriceField = new TextField();
    private final TextField sellingPriceField = new TextField();
    private final TextField stockField = new TextField();
    private final ListView<Category> productCategoriesList = new ListView<>();

    private Product selectedProduct;

    public ProductsView() {
        buildLayout();
        loadData();
    }

    private void buildLayout() {
        setPadding(new Insets(18));
        setStyle("-fx-background-color: linear-gradient(to bottom right, #020617, #111827);");

        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.26);

        // Left: Categories panel
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(14));
        leftPanel.setStyle("-fx-background-color: #020617; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.6), 18,0,0,6);");

        Label categoriesTitle = new Label("Categories");
        categoriesTitle.setStyle("-fx-text-fill: #e5e7eb; -fx-font-size: 15px; -fx-font-weight: bold;");

        categoryListView.setItems(categoryItems);
        categoryListView.setPrefHeight(200);

        TextField categoryNameField = new TextField();
        categoryNameField.setPromptText("Category name");
        TextField categoryDescField = new TextField();
        categoryDescField.setPromptText("Description");

        HBox categoryButtons = new HBox(8);
        Button addCategoryBtn = new Button("Add");
        Button updateCategoryBtn = new Button("Update");
        Button deleteCategoryBtn = new Button("Delete");
        categoryButtons.getChildren().addAll(addCategoryBtn, updateCategoryBtn, deleteCategoryBtn);

        addCategoryBtn.setOnAction(e -> {
            String name = categoryNameField.getText().trim();
            if (name.isEmpty()) {
                showAlert("Validation", "Category name is required.");
                return;
            }
            Category newCategory = catalogRepository.addCategory(name, categoryDescField.getText().trim());
            categoryItems.add(newCategory);
            categoryNameField.clear();
            categoryDescField.clear();
            reloadProductCategoryChoices();
        });

        updateCategoryBtn.setOnAction(e -> {
            Category selected = categoryListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Selection", "Select a category to update.");
                return;
            }
            String name = categoryNameField.getText().trim();
            if (name.isEmpty()) {
                showAlert("Validation", "Category name is required.");
                return;
            }
            catalogRepository.updateCategory(selected, name, categoryDescField.getText().trim());
            categoryListView.refresh();
            reloadProductCategoryChoices();
        });

        deleteCategoryBtn.setOnAction(e -> {
            Category selected = categoryListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Selection", "Select a category to delete.");
                return;
            }
            catalogRepository.deleteCategory(selected);
            categoryItems.remove(selected);
            reloadProductCategoryChoices();
            reloadProducts();
        });

        categoryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                categoryNameField.setText(newVal.getName());
                categoryDescField.setText(newVal.getDescription());
            }
        });

        leftPanel.getChildren().addAll(categoriesTitle, categoryListView, new Separator(), categoryNameField, categoryDescField, categoryButtons);

        // Right: Products panel
        VBox rightPanel = new VBox(12);
        rightPanel.setPadding(new Insets(14));
        rightPanel.setStyle("-fx-background-color: #020617; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.6), 18,0,0,6);");

        Label productsTitle = new Label("Products");
        productsTitle.setStyle("-fx-text-fill: #e5e7eb; -fx-font-size: 15px; -fx-font-weight: bold;");

        configureProductsTable();

        HBox formRow1 = new HBox(10, labeledField("Name", nameField), labeledField("SKU", skuField), labeledField("Barcode", barcodeField));
        HBox formRow2 = new HBox(10, labeledField("Brand", brandField), labeledField("Purchase Price", purchasePriceField), labeledField("Selling Price", sellingPriceField));
        HBox formRow3 = new HBox(10, labeledField("Stock", stockField));

        productCategoriesList.setPrefHeight(80);
        productCategoriesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        VBox categoriesBox = new VBox(4, new Label("Product Categories"), productCategoriesList);

        HBox productButtons = new HBox(8);
        Button newProductBtn = new Button("New");
        Button saveProductBtn = new Button("Save");
        Button deleteProductBtn = new Button("Delete");
        productButtons.getChildren().addAll(newProductBtn, saveProductBtn, deleteProductBtn);

        newProductBtn.setOnAction(e -> clearProductForm());

        saveProductBtn.setOnAction(e -> saveOrUpdateProduct());

        deleteProductBtn.setOnAction(e -> deleteSelectedProduct());

        productsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadProductIntoForm(newVal);
            }
        });

        rightPanel.getChildren().addAll(productsTitle, productsTable, new Separator(), formRow1, formRow2, formRow3, categoriesBox, productButtons);
        VBox.setVgrow(productsTable, Priority.ALWAYS);

        splitPane.getItems().addAll(leftPanel, rightPanel);
        splitPane.setOrientation(Orientation.HORIZONTAL);

        setCenter(splitPane);
    }

    private VBox labeledField(String labelText, TextField field) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");
        field.setPromptText(labelText);
        VBox box = new VBox(2, label, field);
        VBox.setVgrow(field, Priority.NEVER);
        return box;
    }

    private void configureProductsTable() {
        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<Product, String> skuCol = new TableColumn<>("SKU");
        skuCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSku()));

        TableColumn<Product, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBrand() == null ? "" : c.getValue().getBrand()));

        TableColumn<Product, Number> priceCol = new TableColumn<>("Selling Price");
        priceCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getSellingPrice()));

        TableColumn<Product, Number> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getStockQuantity()));

        TableColumn<Product, String> categoriesCol = new TableColumn<>("Categories");
        categoriesCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCategories().stream()
                        .map(Category::getName)
                        .collect(Collectors.joining(", "))
        ));

        productsTable.getColumns().addAll(nameCol, skuCol, brandCol, priceCol, stockCol, categoriesCol);
        productsTable.setItems(productItems);
    }

    private void loadData() {
        categoryItems.setAll(catalogRepository.getAllCategories());
        productItems.setAll(catalogRepository.getAllProducts());
        reloadProductCategoryChoices();
    }

    private void reloadProductCategoryChoices() {
        productCategoriesList.setItems(FXCollections.observableArrayList(categoryItems));
    }

    private void reloadProducts() {
        productItems.setAll(catalogRepository.getAllProducts());
        productsTable.refresh();
    }

    private void clearProductForm() {
        selectedProduct = null;
        nameField.clear();
        skuField.clear();
        barcodeField.clear();
        brandField.clear();
        purchasePriceField.clear();
        sellingPriceField.clear();
        stockField.clear();
        productCategoriesList.getSelectionModel().clearSelection();
    }

    private void loadProductIntoForm(Product product) {
        selectedProduct = product;
        nameField.setText(product.getName());
        skuField.setText(product.getSku());
        barcodeField.setText(product.getBarcode());
        brandField.setText(product.getBrand());
        purchasePriceField.setText(String.valueOf(product.getPurchasePrice()));
        sellingPriceField.setText(String.valueOf(product.getSellingPrice()));
        stockField.setText(String.valueOf(product.getStockQuantity()));

        productCategoriesList.getSelectionModel().clearSelection();
        for (Category c : product.getCategories()) {
            int index = productCategoriesList.getItems().indexOf(c);
            if (index >= 0) {
                productCategoriesList.getSelectionModel().select(index);
            }
        }
    }

    private void saveOrUpdateProduct() {
        String name = nameField.getText().trim();
        String sku = skuField.getText().trim();

        if (name.isEmpty() || sku.isEmpty()) {
            showAlert("Validation", "Name and SKU are required.");
            return;
        }

        double purchasePrice = parseDoubleOrZero(purchasePriceField.getText());
        double sellingPrice = parseDoubleOrZero(sellingPriceField.getText());
        int stock = parseIntOrZero(stockField.getText());

        if (selectedProduct == null) {
            Product product = new Product(name, sku);
            applyFormToProduct(product, purchasePrice, sellingPrice, stock);
            catalogRepository.addProduct(product);
            productItems.add(product);
            productsTable.getSelectionModel().select(product);
        } else {
            applyFormToProduct(selectedProduct, purchasePrice, sellingPrice, stock);
            productsTable.refresh();
        }
        clearProductForm();
    }

    private void applyFormToProduct(Product product, double purchasePrice, double sellingPrice, int stock) {
        product.setName(nameField.getText().trim());
        product.setSku(skuField.getText().trim());
        product.setBarcode(barcodeField.getText().trim());
        product.setBrand(brandField.getText().trim());
        product.setPurchasePrice(purchasePrice);
        product.setSellingPrice(sellingPrice);
        product.setStockQuantity(stock);

        product.getCategories().clear();
        product.getCategories().addAll(productCategoriesList.getSelectionModel().getSelectedItems());
    }

    private void deleteSelectedProduct() {
        Product product = productsTable.getSelectionModel().getSelectedItem();
        if (product == null) {
            showAlert("Selection", "Select a product to delete.");
            return;
        }
        catalogRepository.deleteProduct(product);
        productItems.remove(product);
        clearProductForm();
    }

    private double parseDoubleOrZero(String text) {
        try {
            return text == null || text.trim().isEmpty() ? 0 : Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int parseIntOrZero(String text) {
        try {
            return text == null || text.trim().isEmpty() ? 0 : Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

