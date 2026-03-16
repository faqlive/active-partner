package org.example.activepartner.ui;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.example.activepartner.ui.navigation.AppView;
import org.example.activepartner.ui.navigation.LoadedView;
import org.example.activepartner.ui.navigation.NavigationService;

public class MainLayoutController {
    @FXML
    private AnchorPane viewContainer;
    @FXML
    private Label titleLabel;
    @FXML
    private Button automationButton;
    @FXML
    private Button settingsButton;

    private final NavigationService navigationService;
    private Stage stage;
    private Scene scene;
    private MainController automationController;
    private SettingsController settingsController;

    public MainLayoutController() {
        this.navigationService = new NavigationService();
    }

    @FXML
    public void initialize() {
        refreshLocalizedTexts();
        navigate(AppView.AUTOMATION);
    }

    public void bindStage(Stage stage) {
        this.stage = stage;
        if (automationController != null) {
            automationController.bindStage(stage);
        }
    }

    public void bindScene(Scene scene) {
        this.scene = scene;
        if (automationController != null) {
            automationController.bindScene(scene);
        }
    }

    public void shutdown() {
        if (automationController != null) {
            automationController.shutdown();
        }
    }

    @FXML
    private void onOpenAutomation() {
        navigate(AppView.AUTOMATION);
    }

    @FXML
    private void onOpenSettings() {
        navigate(AppView.SETTINGS);
    }

    private void navigate(AppView appView) {
        LoadedView loadedView = navigationService.load(appView);
        Pane content = (Pane) loadedView.getRoot();
        viewContainer.getChildren().setAll(content);
        AnchorPane.setTopAnchor(content, 0.0);
        AnchorPane.setRightAnchor(content, 0.0);
        AnchorPane.setBottomAnchor(content, 0.0);
        AnchorPane.setLeftAnchor(content, 0.0);

        if (loadedView.getController() instanceof MainController) {
            automationController = (MainController) loadedView.getController();
            automationController.refreshSettingsFromStore();
            if (stage != null) {
                automationController.bindStage(stage);
            }
            if (scene != null) {
                automationController.bindScene(scene);
            }
        } else if (loadedView.getController() instanceof SettingsController) {
            settingsController = (SettingsController) loadedView.getController();
            settingsController.setLanguageChangedHandler(new Runnable() {
                @Override
                public void run() {
                    onLanguageChanged();
                }
            });
            settingsController.refreshRuntimeSnapshot();
            settingsController.refreshLocalizedTexts();
        }
    }

    private void onLanguageChanged() {
        refreshLocalizedTexts();
        if (automationController != null) {
            automationController.refreshLocalizedTexts();
        }
        if (settingsController != null) {
            settingsController.refreshLocalizedTexts();
        }
        if (stage != null) {
            stage.setTitle(AppServices.i18n().text("app.title"));
        }
    }

    private void refreshLocalizedTexts() {
        titleLabel.setText(AppServices.i18n().text("app.title"));
        automationButton.setText(AppServices.i18n().text("nav.automation"));
        settingsButton.setText(AppServices.i18n().text("nav.settings"));
    }
}
