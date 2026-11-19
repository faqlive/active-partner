package org.example.activepartner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.example.activepartner.automation.AutomationService;
import org.example.activepartner.config.AppSettings;
import org.example.activepartner.persistence.JsonProfileRepository;
import org.example.activepartner.ui.AppServices;
import org.example.activepartner.ui.IconResources;
import org.example.activepartner.ui.MainController;
import org.example.activepartner.ui.MainLayoutController;
import org.example.activepartner.ui.SettingsController;
import org.example.activepartner.ui.navigation.NavigationService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloApplication extends Application {
    private static final Logger LOGGER = Logger.getLogger(HelloApplication.class.getName());

    private MainLayoutController mainLayoutController;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        Platform.setImplicitExit(false);
        AppSettings settings = AppServices.settingsService().load();
        AppServices.i18n().setLanguageCode(settings.getLanguageCode());

        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("main-layout.fxml"),
                AppServices.i18n().getBundle()
        );
        fxmlLoader.setControllerFactory(this::createController);
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
        primaryStage = stage;
        stage.setOnCloseRequest(event -> {
            event.consume();
            Platform.exit();
        });
        stage.show();
    }

    @Override
    public void stop() {
        if (primaryStage != null) {
            persistWindowBounds(primaryStage);
            primaryStage = null;
        }
        if (mainLayoutController != null) {
            MainLayoutController controller = mainLayoutController;
            mainLayoutController = null;
            controller.shutdown();
        }
    }

    private Object createController(Class<?> type) {
        if (type == MainLayoutController.class) {
            return new MainLayoutController(
                    new NavigationService(AppServices.i18n(), this::createController), AppServices.i18n());
        }
        if (type == MainController.class) {
            return new MainController(new JsonProfileRepository(), new AutomationService(),
                    AppServices.settingsService(), AppServices.logService(), AppServices.i18n(), AppServices.appState());
        }
        if (type == SettingsController.class) {
            return new SettingsController(AppServices.settingsService(), AppServices.i18n(), AppServices.appState());
        }
        try {
            return type.getConstructor().newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to create FXML controller " + type.getName(), exception);
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
            AppServices.settingsService().update(settings -> {
                settings.setWindowX(stage.getX());
                settings.setWindowY(stage.getY());
                settings.setWindowWidth(Math.max(500, stage.getWidth()));
                settings.setWindowHeight(Math.max(400, stage.getHeight()));
            });
        } catch (IllegalStateException | IllegalArgumentException | SecurityException exception) {
            LOGGER.log(Level.WARNING, "Unable to save window preferences", exception);
        }
    }


}
