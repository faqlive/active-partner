package org.example.activepartner.ui;

import javafx.scene.image.Image;

import java.awt.Taskbar;
import java.awt.Toolkit;
import java.io.InputStream;
import java.net.URL;

public final class IconResources {
    private static final String ICON_PATH = "/org/example/activepartner/assets/app-icon.png";

    private IconResources() {
    }

    public static Image loadFxIcon() {
        InputStream inputStream = IconResources.class.getResourceAsStream(ICON_PATH);
        if (inputStream == null) {
            return null;
        }
        return new Image(inputStream);
    }

    public static java.awt.Image loadAwtIcon() {
        URL url = IconResources.class.getResource(ICON_PATH);
        if (url == null) {
            return null;
        }
        return Toolkit.getDefaultToolkit().getImage(url);
    }

    public static void applyTaskbarIcon() {
        try {
            if (!Taskbar.isTaskbarSupported()) {
                return;
            }
            java.awt.Image image = loadAwtIcon();
            if (image != null) {
                Taskbar.getTaskbar().setIconImage(image);
            }
        } catch (Exception ignored) {
        }
    }
}
