package org.example.activepartner.ui.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.example.activepartner.ui.AppServices;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class NavigationService {
    private final Map<AppView, LoadedView> cache;

    public NavigationService() {
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
                    AppServices.i18n().getBundle()
            );
            Parent root = loader.load();
            LoadedView loadedView = new LoadedView(root, loader.getController());
            cache.put(view, loadedView);
            return loadedView;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load view: " + view.name(), exception);
        }
    }
}
