package org.example.activepartner.ui.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class ProfileBarController {
    @FXML
    private Label profileLabel;
    @FXML
    private ComboBox<String> profileComboBox;
    @FXML
    private Button loadButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;

    public ComboBox<String> getProfileComboBox() {
        return profileComboBox;
    }

    public Label getProfileLabel() {
        return profileLabel;
    }

    public Button getLoadButton() {
        return loadButton;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public Button getDeleteButton() {
        return deleteButton;
    }
}
