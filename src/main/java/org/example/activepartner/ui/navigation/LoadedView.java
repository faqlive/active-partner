package org.example.activepartner.ui.navigation;

import javafx.scene.Parent;

public class LoadedView {
    private final Parent root;
    private final Object controller;

    public LoadedView(Parent root, Object controller) {
        this.root = root;
        this.controller = controller;
    }

    public Parent getRoot() {
        return root;
    }

    public Object getController() {
        return controller;
    }
}
