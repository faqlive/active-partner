package org.example.activepartner.ui.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.activepartner.domain.ActionType;

public class PointEditorController {
    @FXML
    private Label pointNameLabel;
    @FXML
    private Label xLabel;
    @FXML
    private Label yLabel;
    @FXML
    private Label actionLabel;
    @FXML
    private Label delayLabel;
    @FXML
    private TextField pointNameField;
    @FXML
    private TextField xField;
    @FXML
    private TextField yField;
    @FXML
    private ChoiceBox<ActionType> actionTypeChoice;
    @FXML
    private CheckBox pointEnabledCheck;
    @FXML
    private TextField pointDelayField;
    @FXML
    private Button addPointButton;
    @FXML
    private Button updatePointButton;
    @FXML
    private Button removePointButton;

    public TextField getPointNameField() {
        return pointNameField;
    }

    public Label getPointNameLabel() {
        return pointNameLabel;
    }

    public Label getXLabel() {
        return xLabel;
    }

    public Label getYLabel() {
        return yLabel;
    }

    public Label getActionLabel() {
        return actionLabel;
    }

    public Label getDelayLabel() {
        return delayLabel;
    }

    public TextField getXField() {
        return xField;
    }

    public TextField getYField() {
        return yField;
    }

    public ChoiceBox<ActionType> getActionTypeChoice() {
        return actionTypeChoice;
    }

    public CheckBox getPointEnabledCheck() {
        return pointEnabledCheck;
    }

    public TextField getPointDelayField() {
        return pointDelayField;
    }

    public Button getAddPointButton() {
        return addPointButton;
    }

    public Button getUpdatePointButton() {
        return updatePointButton;
    }

    public Button getRemovePointButton() {
        return removePointButton;
    }
}
