package org.example.activepartner.ui.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Callback;
import org.example.activepartner.i18n.I18nService;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class NavigationService {
    private final Map<AppView, LoadedView> cache;

    private final I18nService i18n;
    private final Callback<Class<?>, Object> controllerFactory;

    public NavigationService(I18nService i18n, Callback<Class<?>, Object> controllerFactory) {
        this.i18n = i18n;
        this.controllerFactory = controllerFactory;
        this.cache = new EnumMap<AppView, LoadedView>(AppView.class);
    }

    public LoadedView load(AppView view) {
        LoadedView cached = cache.get(view);
        if (cached != null) {
            return cached;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    NavigationService.class.getResource(view.getFxmlPath()),
                    i18n.getBundle()
            );
            loader.setControllerFactory(controllerFactory);
            Parent root = loader.load();
            LoadedView loadedView = new LoadedView(root, loader.getController());
            cache.put(view, loadedView);
            return loadedView;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load view: " + view.name(), exception);
        }
    }
}
