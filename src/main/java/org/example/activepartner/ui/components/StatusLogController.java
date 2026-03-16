package org.example.activepartner.ui.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class StatusLogController {
    @FXML
    private Label messageLabel;
    @FXML
    private Label mousePositionLabel;
    @FXML
    private Label autoResumeLabel;
    @FXML
    private Label shortcutsLabel;
    @FXML
    private TextArea logArea;

    public Label getMessageLabel() {
        return messageLabel;
    }

    public Label getMousePositionLabel() {
        return mousePositionLabel;
    }

    public Label getShortcutsLabel() {
        return shortcutsLabel;
    }

    public Label getAutoResumeLabel() {
        return autoResumeLabel;
    }

    public TextArea getLogArea() {
        return logArea;
    }
}
