package org.example.activepartner.ui.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ControlBarController {
    @FXML
    private Button startButton;
    @FXML
    private Button pauseButton;
    @FXML
    private Button resumeButton;
    @FXML
    private Button stopButton;
    @FXML
    private Label stateTextLabel;
    @FXML
    private Label stateLabel;
    @FXML
    private Label countTextLabel;
    @FXML
    private Label counterLabel;

    public Button getStartButton() {
        return startButton;
    }

    public Button getPauseButton() {
        return pauseButton;
    }

    public Button getResumeButton() {
        return resumeButton;
    }

    public Button getStopButton() {
        return stopButton;
    }

    public Label getStateLabel() {
        return stateLabel;
    }

    public Label getStateTextLabel() {
        return stateTextLabel;
    }

    public Label getCounterLabel() {
        return counterLabel;
    }

    public Label getCountTextLabel() {
        return countTextLabel;
    }
}
