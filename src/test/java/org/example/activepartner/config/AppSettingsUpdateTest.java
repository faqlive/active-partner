package org.example.activepartner.config;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppSettingsUpdateTest {
    @TempDir
    Path directory;

    @Test
    void updatesPreservePreferencesOwnedByOtherScreens() {
        AppSettingsService service = new AppSettingsService(directory.resolve("settings.properties"));
        service.update(settings -> {
            settings.setWindowX(100);
            settings.setWindowWidth(810);
        });
        service.update(settings -> settings.setLanguageCode("en"));
        assertEquals(100.0, service.load().getWindowX());
        assertEquals(810.0, service.load().getWindowWidth());

        service.update(settings -> settings.setWindowHeight(640));
        assertEquals("en", service.load().getLanguageCode());
        assertEquals(640.0, service.load().getWindowHeight());
    }

    @Test
    void aRejectedUpdateLeavesStoredPreferencesIntact() {
        AppSettingsService service = new AppSettingsService(directory.resolve("settings.properties"));
        service.update(settings -> settings.setLanguageCode("en"));
        assertThrows(IllegalArgumentException.class, () -> service.update(settings -> {
            settings.setLanguageCode("es");
            settings.setWindowWidth(0);
        }));
        assertEquals("en", service.load().getLanguageCode());
    }
}
