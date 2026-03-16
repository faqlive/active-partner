package org.example.activepartner.ui.components;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class RunSettingsController {
    @FXML
    private Label intervalLabel;
    @FXML
    private Label jitterLabel;
    @FXML
    private Label startDelayLabel;
    @FXML
    private TextField intervalField;
    @FXML
    private CheckBox randomOrderCheck;
    @FXML
    private CheckBox loopCheck;
    @FXML
    private CheckBox clickEnabledCheck;
    @FXML
    private TextField jitterField;
    @FXML
    private TextField startDelayField;

    public TextField getIntervalField() {
        return intervalField;
    }

    public Label getIntervalLabel() {
        return intervalLabel;
    }

    public Label getJitterLabel() {
        return jitterLabel;
    }

    public Label getStartDelayLabel() {
        return startDelayLabel;
    }

    public CheckBox getRandomOrderCheck() {
        return randomOrderCheck;
    }

    public CheckBox getLoopCheck() {
        return loopCheck;
    }

    public CheckBox getClickEnabledCheck() {
        return clickEnabledCheck;
    }

    public TextField getJitterField() {
        return jitterField;
    }

    public TextField getStartDelayField() {
        return startDelayField;
    }
}
