package com.smartinventory.repository;

import com.smartinventory.model.Category;
import com.smartinventory.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class InMemoryCatalogRepository {

    private static final InMemoryCatalogRepository INSTANCE = new InMemoryCatalogRepository();

    private final List<Category> categories = new ArrayList<>();
    private final List<Product> products = new ArrayList<>();

    private InMemoryCatalogRepository() {
        seedDefaultData();
    }

    public static InMemoryCatalogRepository getInstance() {
        return INSTANCE;
    }

    private void seedDefaultData() {
        Category dairy = new Category("Dairy", "Milk, curd, paneer, etc.");
        Category electronics = new Category("Electronics", "Small appliances and gadgets");
        Category hardware = new Category("Hardware", "Tools and hardware items");
        Category grooming = new Category("Grooming", "Personal care and grooming");

        categories.add(dairy);
        categories.add(electronics);
        categories.add(hardware);
        categories.add(grooming);

        Product milk = new Product("Toned Milk 1L", "DAIRY-001");
        milk.setBarcode("890000000001");
        milk.setBrand("Local Dairy");
        milk.setPurchasePrice(40);
        milk.setSellingPrice(45);
        milk.setStockQuantity(50);
        milk.addCategory(dairy);

        Product shampoo = new Product("Anti Dandruff Shampoo 200ml", "GROOM-010");
        shampoo.setBarcode("890000000010");
        shampoo.setBrand("CleanScalp");
        shampoo.setPurchasePrice(90);
        shampoo.setSellingPrice(120);
        shampoo.setStockQuantity(30);
        shampoo.addCategory(grooming);

        Product drill = new Product("Electric Drill 500W", "HARD-100");
        drill.setBarcode("890000001000");
        drill.setBrand("ToolPro");
        drill.setPurchasePrice(1800);
        drill.setSellingPrice(2200);
        drill.setStockQuantity(8);
        drill.addCategory(hardware);
        drill.addCategory(electronics);

        products.add(milk);
        products.add(shampoo);
        products.add(drill);
    }

    public List<Category> getAllCategories() {
        return new ArrayList<>(categories);
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    public List<Product> searchProducts(String query, Category categoryFilter) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);

        return products.stream()
                .filter(p -> {
                    boolean matchesText = normalized.isEmpty()
                            || p.getName().toLowerCase(Locale.ROOT).contains(normalized)
                            || p.getSku().toLowerCase(Locale.ROOT).contains(normalized)
                            || (p.getBarcode() != null
                            && p.getBarcode().toLowerCase(Locale.ROOT).contains(normalized));

                    boolean matchesCategory = categoryFilter == null
                            || p.getCategories().contains(categoryFilter);

                    return matchesText && matchesCategory;
                })
                .collect(Collectors.toList());
    }

    public Category addCategory(String name, String description) {
        Category category = new Category(name, description);
        categories.add(category);
        return category;
    }

    public void updateCategory(Category category, String name, String description) {
        category.setName(name);
        category.setDescription(description);
    }

    public void deleteCategory(Category category) {
        products.forEach(p -> p.getCategories().remove(category));
        categories.remove(category);
    }

    public Product addProduct(Product product) {
        products.add(product);
        return product;
    }

    public void deleteProduct(Product product) {
        products.remove(product);
    }

}

