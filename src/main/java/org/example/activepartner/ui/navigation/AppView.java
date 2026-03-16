package org.example.activepartner.ui.navigation;

public enum AppView {
    AUTOMATION("/org/example/activepartner/views/automation-view.fxml"),
    SETTINGS("/org/example/activepartner/views/settings-view.fxml");

    private final String fxmlPath;

    AppView(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}
