package org.example.activepartner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.example.activepartner.config.AppSettings;
import org.example.activepartner.ui.AppServices;
import org.example.activepartner.ui.IconResources;
import org.example.activepartner.ui.MainLayoutController;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloApplication extends Application {
    private static final Logger LOGGER = Logger.getLogger(HelloApplication.class.getName());

    private MainLayoutController mainLayoutController;

    @Override
    public void start(Stage stage) throws IOException {
        Platform.setImplicitExit(false);
        AppSettings settings = AppServices.settingsService().load();
        AppServices.i18n().setLanguageCode(settings.getLanguageCode());

        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("main-layout.fxml"),
                AppServices.i18n().getBundle()
        );
        Scene scene = new Scene(fxmlLoader.load(), 920, 700);
        this.mainLayoutController = fxmlLoader.getController();
        this.mainLayoutController.bindScene(scene);
        this.mainLayoutController.bindStage(stage);
        stage.setTitle(AppServices.i18n().text("app.title"));
        Image appIcon = IconResources.loadFxIcon();
        if (appIcon != null) {
            stage.getIcons().add(appIcon);
        }
        IconResources.applyTaskbarIcon();
        stage.setScene(scene);
        applyWindowBounds(stage);
        stage.setOnCloseRequest(event -> {
            persistWindowBounds(stage);
            if (mainLayoutController != null) {
                mainLayoutController.shutdown();
            }
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }

    @Override
    public void stop() {
        // Stage close handler already persists bounds.
        if (mainLayoutController != null) {
            mainLayoutController.shutdown();
        }
    }

    private void applyWindowBounds(Stage stage) {
        AppSettings settings = AppServices.settingsService().load();
        stage.setWidth(settings.getWindowWidth());
        stage.setHeight(settings.getWindowHeight());
        if (settings.hasWindowPosition()) {
            stage.setX(settings.getWindowX());
            stage.setY(settings.getWindowY());
        }
    }

    private void persistWindowBounds(Stage stage) {
        try {
            AppSettings settings = AppServices.settingsService().load();
            settings.setWindowX(stage.getX());
            settings.setWindowY(stage.getY());
            settings.setWindowWidth(Math.max(500, stage.getWidth()));
            settings.setWindowHeight(Math.max(400, stage.getHeight()));
            AppServices.settingsService().save(settings);
        } catch (IllegalStateException | IllegalArgumentException | SecurityException exception) {
            LOGGER.log(Level.WARNING, "Unable to save window preferences", exception);
        }
    }


}
