package com.smartinventory.model;

public enum UserRole {
    OWNER("Owner"),
    MANAGER("Manager"),
    STAFF("Staff");

    private final String label;

    UserRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

