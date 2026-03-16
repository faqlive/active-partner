package org.example.activepartner.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class AppSettingsService {
    private static final String ROOT_DIR = ".active-partner";
    private static final String SETTINGS_FILE = "app-settings.properties";

    private final Path settingsPath;

    public AppSettingsService() {
        this(Paths.get(System.getProperty("user.home"), ROOT_DIR, SETTINGS_FILE));
    }

    AppSettingsService(Path settingsPath) {
        this.settingsPath = settingsPath;
        ensureDirectory();
    }

    public AppSettings load() {
        AppSettings defaults = new AppSettings();
        if (!Files.exists(settingsPath)) {
            return defaults;
        }

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(settingsPath)) {
            properties.load(inputStream);
            defaults.setMinimizeToTrayOnStart(Boolean.parseBoolean(
                    properties.getProperty("minimizeToTrayOnStart", Boolean.toString(defaults.isMinimizeToTrayOnStart()))
            ));
            defaults.setLanguageCode(properties.getProperty("languageCode", defaults.getLanguageCode()));
            defaults.setAutoResumeAfterInteractionSec(Integer.parseInt(
                    properties.getProperty("autoResumeAfterInteractionSec", Integer.toString(defaults.getAutoResumeAfterInteractionSec()))
            ));
            defaults.setWindowX(parseDoubleOrDefault(properties.getProperty("windowX"), defaults.getWindowX()));
            defaults.setWindowY(parseDoubleOrDefault(properties.getProperty("windowY"), defaults.getWindowY()));
            defaults.setWindowWidth(parseDoubleOrDefault(properties.getProperty("windowWidth"), defaults.getWindowWidth()));
            defaults.setWindowHeight(parseDoubleOrDefault(properties.getProperty("windowHeight"), defaults.getWindowHeight()));
            return defaults;
        } catch (Exception exception) {
            return defaults;
        }
    }

    public void save(AppSettings settings) {
        ensureDirectory();
        Properties properties = new Properties();
        properties.setProperty("minimizeToTrayOnStart", Boolean.toString(settings.isMinimizeToTrayOnStart()));
        properties.setProperty("languageCode", settings.getLanguageCode());
        properties.setProperty("autoResumeAfterInteractionSec", Integer.toString(settings.getAutoResumeAfterInteractionSec()));
        properties.setProperty("windowX", Double.toString(settings.getWindowX()));
        properties.setProperty("windowY", Double.toString(settings.getWindowY()));
        properties.setProperty("windowWidth", Double.toString(settings.getWindowWidth()));
        properties.setProperty("windowHeight", Double.toString(settings.getWindowHeight()));

        try (OutputStream outputStream = Files.newOutputStream(settingsPath)) {
            properties.store(outputStream, "Active Partner Settings");
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save settings", exception);
        }
    }

    private void ensureDirectory() {
        try {
            Files.createDirectories(settingsPath.getParent());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create settings directory", exception);
        }
    }

    private double parseDoubleOrDefault(String value, double defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }
}
