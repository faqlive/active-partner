package org.example.activepartner.ui;

import org.example.activepartner.config.AppSettingsService;
import org.example.activepartner.i18n.I18nService;
import org.example.activepartner.logging.AppLogService;
import org.example.activepartner.ui.state.AppState;

public final class AppServices {
    private static final AppSettingsService SETTINGS_SERVICE = new AppSettingsService();
    private static final AppLogService LOG_SERVICE = new AppLogService();
    private static final AppState APP_STATE = new AppState();
    private static final I18nService I18N_SERVICE = new I18nService();

    private AppServices() {
    }

    public static AppSettingsService settingsService() {
        return SETTINGS_SERVICE;
    }

    public static AppLogService logService() {
        return LOG_SERVICE;
    }

    public static AppState appState() {
        return APP_STATE;
    }

    public static I18nService i18n() {
        return I18N_SERVICE;
    }
}
