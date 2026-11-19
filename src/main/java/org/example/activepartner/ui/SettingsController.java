package org.example.activepartner.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.activepartner.config.AppSettings;
import org.example.activepartner.config.AppSettingsService;
import org.example.activepartner.i18n.AppLanguage;
import org.example.activepartner.i18n.I18nService;

import java.util.ArrayList;
import java.util.List;

public class SettingsController {
    @FXML
    private Label titleLabel;
    @FXML
    private CheckBox minimizeOnStartCheck;
    @FXML
    private Label autoResumeLabel;
    @FXML
    private TextField autoResumeSecField;
    @FXML
    private Label languageLabel;
    @FXML
    private ComboBox<AppLanguage> languageComboBox;
    @FXML
    private Button saveButton;
    @FXML
    private Label runtimeStateTextLabel;
    @FXML
    private Label runtimeStateLabel;
    @FXML
    private Label activeProfileTextLabel;
    @FXML
    private Label activeProfileLabel;
    @FXML
    private Label statusLabel;

    private final AppSettingsService settingsService;
    private final I18nService i18nService;
    private Runnable languageChangedHandler;

    public SettingsController() {
        this.settingsService = AppServices.settingsService();
        this.i18nService = AppServices.i18n();
    }

    @FXML
    public void initialize() {
        configureLanguageCombo();
        loadSettings();
        refreshLocalizedTexts();
        refreshRuntimeSnapshot();
        statusLabel.setText(t("status.ready"));
    }

    @FXML
    private void onSaveSettings() {
        try {
            boolean minimizeOnStart = minimizeOnStartCheck.isSelected();
            int autoResumeSeconds = Integer.parseInt(autoResumeSecField.getText().trim());
            AppLanguage selectedLanguage = languageComboBox.getValue() == null
                    ? AppLanguage.SPANISH : languageComboBox.getValue();
            settingsService.update(settings -> {
                settings.setMinimizeToTrayOnStart(minimizeOnStart);
                settings.setAutoResumeAfterInteractionSec(autoResumeSeconds);
                settings.setLanguageCode(selectedLanguage.getCode());
            });
            boolean languageChanged = !selectedLanguage.getCode().equalsIgnoreCase(i18nService.getLanguageCode());
            i18nService.setLanguageCode(selectedLanguage.getCode());
            if (languageChanged && languageChangedHandler != null) {
                languageChangedHandler.run();
            }
            statusLabel.setText(t("settings.saved"));
        } catch (Exception exception) {
            statusLabel.setText(t("settings.invalidValues", exception.getMessage()));
        }
    }

    private void loadSettings() {
        AppSettings settings = settingsService.load();
        minimizeOnStartCheck.setSelected(settings.isMinimizeToTrayOnStart());
        autoResumeSecField.setText(Integer.toString(settings.getAutoResumeAfterInteractionSec()));
        languageComboBox.setValue(AppLanguage.fromCode(settings.getLanguageCode()));
    }

    public void refreshRuntimeSnapshot() {
        runtimeStateLabel.setText(AppServices.appState().getRunState().name());
        activeProfileLabel.setText(AppServices.appState().getActiveProfile());
    }

    public void refreshLocalizedTexts() {
        titleLabel.setText(t("settings.title"));
        minimizeOnStartCheck.setText(t("settings.minimizeOnStart"));
        autoResumeLabel.setText(t("settings.autoResumeAfterInteractionSec"));
        languageLabel.setText(t("settings.language"));
        saveButton.setText(t("settings.save"));
        runtimeStateTextLabel.setText(t("settings.currentRuntimeState"));
        activeProfileTextLabel.setText(t("settings.activeProfile"));
    }

    public void setLanguageChangedHandler(Runnable languageChangedHandler) {
        this.languageChangedHandler = languageChangedHandler;
    }

    private void configureLanguageCombo() {
        List<AppLanguage> languages = new ArrayList<AppLanguage>();
        languages.add(AppLanguage.SPANISH);
        languages.add(AppLanguage.ENGLISH);
        languageComboBox.getItems().setAll(languages);
        languageComboBox.setConverter(new javafx.util.StringConverter<AppLanguage>() {
            @Override
            public String toString(AppLanguage language) {
                return language == null ? "" : language.getLabel();
            }

            @Override
            public AppLanguage fromString(String string) {
                return AppLanguage.fromCode(string);
            }
        });
    }

    private String t(String key, Object... args) {
        return i18nService.text(key, args);
    }
}
