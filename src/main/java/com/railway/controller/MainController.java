package com.railway.controller;

import com.railway.booking.backend.model.User;

public class MainController {
    private User currentUser;

    public MainController(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }
}
