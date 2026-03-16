package org.example.activepartner.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class AppSettingsServiceTest {

    @Test
    void savesAndLoadsSettingsWithWindowBounds() throws IOException {
        Path tempDir = Files.createTempDirectory("app-settings-test");
        AppSettingsService service = new AppSettingsService(tempDir.resolve("app-settings.properties"));

        AppSettings settings = new AppSettings();
        settings.setMinimizeToTrayOnStart(false);
        settings.setAutoResumeAfterInteractionSec(45);
        settings.setWindowX(120);
        settings.setWindowY(160);
        settings.setWindowWidth(1100);
        settings.setWindowHeight(760);

        service.save(settings);
        AppSettings loaded = service.load();

        Assertions.assertFalse(loaded.isMinimizeToTrayOnStart());
        Assertions.assertEquals(45, loaded.getAutoResumeAfterInteractionSec());
        Assertions.assertEquals(120.0, loaded.getWindowX());
        Assertions.assertEquals(160.0, loaded.getWindowY());
        Assertions.assertEquals(1100.0, loaded.getWindowWidth());
        Assertions.assertEquals(760.0, loaded.getWindowHeight());
        Assertions.assertTrue(loaded.hasWindowPosition());
    }

    @Test
    void returnsDefaultsWhenFileMissing() throws IOException {
        Path tempDir = Files.createTempDirectory("app-settings-missing");
        AppSettingsService service = new AppSettingsService(tempDir.resolve("missing.properties"));

        AppSettings loaded = service.load();

        Assertions.assertTrue(loaded.isMinimizeToTrayOnStart());
        Assertions.assertEquals(60, loaded.getAutoResumeAfterInteractionSec());
        Assertions.assertFalse(loaded.hasWindowPosition());
    }
}
