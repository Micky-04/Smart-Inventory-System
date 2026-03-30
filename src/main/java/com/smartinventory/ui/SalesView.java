package com.smartinventory.ui;

import com.smartinventory.model.Product;
import com.smartinventory.repository.InMemoryCatalogRepository;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class SalesView extends BorderPane {

    private final InMemoryCatalogRepository catalogRepository = InMemoryCatalogRepository.getInstance();

    private final TextField scanField = new TextField();
    private final TableView<Product> cartTable = new TableView<>();
    private final ObservableList<Product> cartItems = FXCollections.observableArrayList();

    private final Label totalItemsLabel = new Label("0 items");
    private final Label totalAmountLabel = new Label("₹0.00");

    public SalesView() {
        buildLayout();
    }

    private void buildLayout() {
        setPadding(new Insets(16));
        setStyle("-fx-background-color: linear-gradient(to bottom right, #0f172a, #1e293b);");

        HBox root = new HBox(14);

        // Left: scanning and cart table
        VBox leftCard = new VBox(10);
        leftCard.setPadding(new Insets(12));
        leftCard.setStyle("-fx-background-color: #020617; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.5), 16,0,0,4);");

        Label title = new Label("Sales / POS");
        title.setStyle("-fx-text-fill: #e5e7eb; -fx-font-size: 16px; -fx-font-weight: bold;");

        HBox scanRow = new HBox(8);
        scanRow.setAlignment(Pos.CENTER_LEFT);
        scanField.setPromptText("Scan barcode or type SKU / name...");

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> addProductToCart());

        HBox.setHgrow(scanField, Priority.ALWAYS);
        scanRow.getChildren().addAll(scanField, addBtn);

        configureCartTable();

        leftCard.getChildren().addAll(title, scanRow, cartTable);
        VBox.setVgrow(cartTable, Priority.ALWAYS);

        // Right: summary and payment
        VBox rightCard = new VBox(12);
        rightCard.setPadding(new Insets(16));
        rightCard.setStyle("-fx-background-color: #111827; -fx-background-radius: 18; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.7), 18,0,0,6);");

        Label summaryTitle = new Label("Bill Summary");
        summaryTitle.setStyle("-fx-text-fill: #e5e7eb; -fx-font-size: 15px; -fx-font-weight: bold;");

        Label totalItemsCaption = new Label("Items");
        totalItemsCaption.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");
        totalItemsLabel.setStyle("-fx-text-fill: #f9fafb; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label totalAmountCaption = new Label("Grand Total");
        totalAmountCaption.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");
        totalAmountLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 24px; -fx-font-weight: bold;");

        VBox totalBox = new VBox(4, totalItemsCaption, totalItemsLabel, totalAmountCaption, totalAmountLabel);

        Separator sep = new Separator();

        Label paymentTitle = new Label("Payment Mode");
        paymentTitle.setStyle("-fx-text-fill: #e5e7eb; -fx-font-size: 13px; -fx-font-weight: bold;");

        ToggleGroup paymentGroup = new ToggleGroup();
        RadioButton cashRb = new RadioButton("Cash");
        RadioButton cardRb = new RadioButton("Card");
        RadioButton upiRb = new RadioButton("UPI");
        cashRb.setToggleGroup(paymentGroup);
        cardRb.setToggleGroup(paymentGroup);
        upiRb.setToggleGroup(paymentGroup);
        cashRb.setSelected(true);
        cashRb.setStyle("-fx-text-fill: #e5e7eb;");
        cardRb.setStyle("-fx-text-fill: #e5e7eb;");
        upiRb.setStyle("-fx-text-fill: #e5e7eb;");

        HBox paymentRow = new HBox(10, cashRb, cardRb, upiRb);

        Button completeSaleBtn = new Button("Complete Sale");
        completeSaleBtn.setMaxWidth(Double.MAX_VALUE);
        completeSaleBtn.setStyle("-fx-background-color: linear-gradient(to right, #22c55e, #16a34a); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        completeSaleBtn.setOnAction(e -> completeSale());

        rightCard.getChildren().addAll(summaryTitle, totalBox, sep, paymentTitle, paymentRow, completeSaleBtn);
        VBox.setVgrow(completeSaleBtn, Priority.NEVER);

        root.getChildren().addAll(leftCard, rightCard);
        HBox.setHgrow(leftCard, Priority.ALWAYS);

        setCenter(root);
    }

    private void configureCartTable() {
        TableColumn<Product, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        nameCol.setPrefWidth(220);

        TableColumn<Product, String> skuCol = new TableColumn<>("SKU");
        skuCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSku()));

        TableColumn<Product, Number> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getSellingPrice()));

        TableColumn<Product, Number> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(c -> new SimpleIntegerProperty(1));

        cartTable.getColumns().addAll(nameCol, skuCol, priceCol, qtyCol);
        cartTable.setItems(cartItems);
    }

    private void addProductToCart() {
        String text = scanField.getText();
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        String q = text.trim().toLowerCase();
        Product match = catalogRepository.getAllProducts().stream()
                .filter(p -> p.getSku().toLowerCase().contains(q)
                        || p.getName().toLowerCase().contains(q)
                        || (p.getBarcode() != null && p.getBarcode().toLowerCase().contains(q)))
                .findFirst()
                .orElse(null);
        if (match != null) {
            cartItems.add(match);
            updateTotals();
            scanField.clear();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Not Found");
            alert.setHeaderText(null);
            alert.setContentText("No product matches the entered value.");
            alert.showAndWait();
        }
    }

    private void updateTotals() {
        int count = cartItems.size();
        double total = cartItems.stream().mapToDouble(Product::getSellingPrice).sum();
        totalItemsLabel.setText(count + " items");
        totalAmountLabel.setText("₹" + String.format("%.2f", total));
    }

    private void completeSale() {
        if (cartItems.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Empty Cart");
            alert.setHeaderText(null);
            alert.setContentText("Add at least one product before completing the sale.");
            alert.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sale Completed");
        alert.setHeaderText(null);
        alert.setContentText("Sale completed successfully. (Stock and history can be implemented in later parts.)");
        alert.showAndWait();
        cartItems.clear();
        updateTotals();
    }
}

