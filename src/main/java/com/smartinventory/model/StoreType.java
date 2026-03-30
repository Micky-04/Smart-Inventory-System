package com.smartinventory.model;

public enum StoreType {
    KIRANA("Kirana / Convenience Store"),
    MINI_MART("Mini-Mart"),
    SUPERMARKET("Supermarket");

    private final String label;

    StoreType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

