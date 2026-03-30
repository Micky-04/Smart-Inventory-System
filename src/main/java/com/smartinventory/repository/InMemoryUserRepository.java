package com.smartinventory.repository;

import com.smartinventory.model.User;
import com.smartinventory.model.UserRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryUserRepository {

    private static final InMemoryUserRepository INSTANCE = new InMemoryUserRepository();

    private final List<User> users = new ArrayList<>();

    private InMemoryUserRepository() {
        seedDefaultUsers();
    }

    public static InMemoryUserRepository getInstance() {
        return INSTANCE;
    }

    private void seedDefaultUsers() {
        // Simple hardcoded users for now; can be managed from UI later
        users.add(new User("owner", "owner123", UserRole.OWNER));
        users.add(new User("manager", "manager123", UserRole.MANAGER));
        users.add(new User("staff", "staff123", UserRole.STAFF));
    }

    public Optional<User> authenticate(String username, String password) {
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username)
                        && u.getPassword().equals(password))
                .findFirst();
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }
}

