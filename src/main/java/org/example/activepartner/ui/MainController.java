package org.example.activepartner.ui;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.activepartner.automation.AutomationListener;
import org.example.activepartner.automation.AutomationService;
import org.example.activepartner.config.AppSettings;
import org.example.activepartner.config.AppSettingsService;
import org.example.activepartner.domain.ActionPoint;
import org.example.activepartner.domain.ActionType;
import org.example.activepartner.domain.AutomationProfile;
import org.example.activepartner.domain.RunConfig;
import org.example.activepartner.domain.RunState;
import org.example.activepartner.i18n.I18nService;
import org.example.activepartner.logging.AppLogService;
import org.example.activepartner.persistence.JsonProfileRepository;
import org.example.activepartner.persistence.ProfileRepository;
import org.example.activepartner.ui.components.ControlBarController;
import org.example.activepartner.ui.components.PointEditorController;
import org.example.activepartner.ui.components.PointsTableController;
import org.example.activepartner.ui.components.ProfileBarController;
import org.example.activepartner.ui.components.RunSettingsController;
import org.example.activepartner.ui.components.StatusLogController;

import java.awt.Color;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {
    private static final int GLOBAL_ADD_POINT_KEY = NativeKeyEvent.VC_F8;
    private static final int GLOBAL_STOP_KEY = NativeKeyEvent.VC_F9;
    private static final double MIN_WINDOW_WIDTH = 500;
    private static final double MIN_WINDOW_HEIGHT = 400;

    @FXML
    private ProfileBarController profileBarController;
    @FXML
    private PointsTableController pointsTableController;
    @FXML
    private PointEditorController pointEditorController;
    @FXML
    private RunSettingsController runSettingsController;
    @FXML
    private ControlBarController controlBarController;
    @FXML
    private StatusLogController statusLogController;
    @FXML
    private Label pointSectionLabel;
    @FXML
    private Label globalSectionLabel;

    private final ObservableList<ActionPoint> points;
    private final ProfileRepository profileRepository;
    private final AutomationService automationService;
    private final AppSettingsService settingsService;
    private final AppLogService appLogService;
    private final I18nService i18nService;
    private final DateTimeFormatter logTimeFormatter;

    private Timeline mouseTrackerTimeline;
    private NativeKeyListener globalKeyListener;
    private Stage stage;
    private TrayIcon trayIcon;
    private boolean activeRunClickEnabled;
    private boolean minimizeToTrayOnStartEnabled;
    private boolean globalHookAvailable = false;
    private double lastWindowX = -1;
    private double lastWindowY = -1;
    private double lastWindowWidth = -1;
    private double lastWindowHeight = -1;

    public MainController() {
        this.points = FXCollections.observableArrayList();
        this.profileRepository = new JsonProfileRepository();
        this.automationService = new AutomationService();
        this.settingsService = AppServices.settingsService();
        this.appLogService = AppServices.logService();
        this.i18nService = AppServices.i18n();
        this.logTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        this.activeRunClickEnabled = false;
        this.minimizeToTrayOnStartEnabled = true;
    }

    @FXML
    public void initialize() {
        wireActions();
        configureTable();
        configureAutomation();
        applyAppSettings();
        configureDefaults();
        refreshLocalizedTexts();
        configurePointEditing();
        configureMouseTracker();
        loadProfiles();
        loadLastProfile();
        registerGlobalKeyboardHook();
    }

    public void bindScene(Scene scene) {
        if (scene == null) {
            return;
        }

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            // DELETE siempre local
            if (event.getCode() == KeyCode.DELETE && pointsTableController.getPointsTable().getSelectionModel().getSelectedItem() != null) {
                onRemovePoint();
                event.consume();
                return;
            }
            // Si existe hook global activo, dejamos que el handler global se encargue de F8/F9 y notifyManualInteraction
            if (!isGlobalHookActive()) {
                if (event.getCode() == KeyCode.F8 && AppServices.appState().getRunState() == RunState.STOPPED) {
                    addPointFromCurrentMousePosition();
                    event.consume();
                    return;
                }
                if (event.getCode() == KeyCode.F9 && AppServices.appState().getRunState() == RunState.RUNNING) {
                    onStop();
                    event.consume();
                    return;
                }
                // notifyManualInteraction solo cuando NO hay hook global
                automationService.notifyManualInteraction();
            }
        });
    }

    public void bindStage(Stage stage) {
        this.stage = stage;
        captureStageBounds(true);
        stage.xProperty().addListener((obs, oldValue, newValue) -> captureStageBounds(false));
        stage.yProperty().addListener((obs, oldValue, newValue) -> captureStageBounds(false));
        stage.widthProperty().addListener((obs, oldValue, newValue) -> captureStageBounds(false));
        stage.heightProperty().addListener((obs, oldValue, newValue) -> captureStageBounds(false));
    }

    public void refreshSettingsFromStore() {
        applyAppSettings();
    }

    public void refreshLocalizedTexts() {
        pointSectionLabel.setText(t("section.pointConfiguration"));
        globalSectionLabel.setText(t("section.globalRunConfiguration"));

        profileBarController.getProfileLabel().setText(t("profile.label"));
        profileBarController.getLoadButton().setText(t("profile.load"));
        profileBarController.getSaveButton().setText(t("profile.save"));
        profileBarController.getDeleteButton().setText(t("profile.delete"));

        pointsTableController.getNameColumn().setText(t("table.name"));
        pointsTableController.getActionColumn().setText(t("table.action"));
        pointsTableController.getEnabledColumn().setText(t("table.enabled"));
        pointsTableController.getClickColumn().setText(t("table.clickStatus"));
        pointsTableController.getDelayColumn().setText(t("table.delayMs"));

        pointEditorController.getPointNameLabel().setText(t("point.name"));
        pointEditorController.getActionLabel().setText(t("point.action"));
        pointEditorController.getPointEnabledCheck().setText(t("point.enabled"));
        pointEditorController.getDelayLabel().setText(t("point.delayMs"));
        pointEditorController.getAddPointButton().setText(t("point.add"));
        pointEditorController.getUpdatePointButton().setText(t("point.updateSelected"));
        pointEditorController.getRemovePointButton().setText(t("point.removeSelected"));

        runSettingsController.getIntervalLabel().setText(t("run.intervalMs"));
        runSettingsController.getRandomOrderCheck().setText(t("run.randomOrder"));
        runSettingsController.getLoopCheck().setText(t("run.loop"));
        runSettingsController.getClickEnabledCheck().setText(t("run.enableClicks"));
        runSettingsController.getJitterLabel().setText(t("run.jitterPx"));
        runSettingsController.getStartDelayLabel().setText(t("run.startDelaySec"));

        controlBarController.getStartButton().setText(t("control.start"));
        controlBarController.getPauseButton().setText(t("control.pause"));
        controlBarController.getResumeButton().setText(t("control.resume"));
        controlBarController.getStopButton().setText(t("control.stop"));
        controlBarController.getStateTextLabel().setText(t("control.state"));
        controlBarController.getCountTextLabel().setText(t("control.count"));

        statusLogController.getShortcutsLabel().setText(t("status.shortcuts"));
        statusLogController.getMessageLabel().setText(AppServices.appState().getLastMessage());
        applyAppSettings();
        pointsTableController.getPointsTable().refresh();
        refreshTrayLocalization();
    }

    public void shutdown() {
        automationService.stop();
        if (mouseTrackerTimeline != null) {
            mouseTrackerTimeline.stop();
        }
        unregisterGlobalKeyboardHook();
        removeTrayIcon();
    }

    private void wireActions() {
        profileBarController.getLoadButton().setOnAction(event -> onLoadProfile());
        profileBarController.getSaveButton().setOnAction(event -> onSaveProfile());
        profileBarController.getDeleteButton().setOnAction(event -> onDeleteProfile());

        pointEditorController.getAddPointButton().setOnAction(event -> onAddPoint());
        pointEditorController.getUpdatePointButton().setOnAction(event -> onUpdateSelectedPoint());
        pointEditorController.getRemovePointButton().setOnAction(event -> onRemovePoint());

        controlBarController.getStartButton().setOnAction(event -> onStart());
        controlBarController.getPauseButton().setOnAction(event -> onPause());
        controlBarController.getResumeButton().setOnAction(event -> onResume());
        controlBarController.getStopButton().setOnAction(event -> onStop());
    }

    private void onAddPoint() {
        try {
            ActionPoint point = buildPointFromEditor(buildNextPointName());
            points.add(point);
            pointsTableController.getPointsTable().refresh();
            pointEditorController.getPointNameField().setText(buildNextPointName());
            statusLogController.getMessageLabel().setText(t("msg.pointAdded"));
            appendLog(t("log.pointAdded", point.getName(), point.getX(), point.getY()));
        } catch (Exception exception) {
            statusLogController.getMessageLabel().setText(exception.getMessage());
        }
    }

    private void onUpdateSelectedPoint() {
        ActionPoint selected = pointsTableController.getPointsTable().getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLogController.getMessageLabel().setText(t("msg.selectPointToUpdate"));
            return;
        }

        try {
            selected.setName(pointEditorController.getPointNameField().getText());
            selected.setX(parseInteger(pointEditorController.getXField().getText(), t("field.x")));
            selected.setY(parseInteger(pointEditorController.getYField().getText(), t("field.y")));
            selected.setActionType(pointEditorController.getActionTypeChoice().getValue());
            selected.setEnabled(pointEditorController.getPointEnabledCheck().isSelected());
            selected.setDelayAfterMs(parseLong(pointEditorController.getPointDelayField().getText(), t("field.delay")));

            pointsTableController.getPointsTable().refresh();
            statusLogController.getMessageLabel().setText(t("msg.pointUpdated"));
            appendLog(t("log.pointUpdated", selected.getName()));
        } catch (Exception exception) {
            statusLogController.getMessageLabel().setText(exception.getMessage());
        }
    }

    private void onRemovePoint() {
        ActionPoint selected = pointsTableController.getPointsTable().getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLogController.getMessageLabel().setText(t("msg.selectPointToRemove"));
            return;
        }

        String name = selected.getName();
        points.remove(selected);
        pointsTableController.getPointsTable().refresh();
        statusLogController.getMessageLabel().setText(t("msg.pointRemoved"));
        appendLog(t("log.pointRemoved", name));
    }

    private void onStart() {
        try {
            AutomationProfile profile = currentProfileFromForm();
            activeRunClickEnabled = profile.getConfig().isClickEnabled();
            applyAppSettings();
            automationService.start(profile);
            statusLogController.getMessageLabel().setText(t("msg.automationStarted"));
            appendLog(t("log.automationStarted", activeRunClickEnabled ? t("status.on") : t("status.off")));
            if (minimizeToTrayOnStartEnabled) {
                minimizeToTrayOnStart();
            }
        } catch (Exception exception) {
            statusLogController.getMessageLabel().setText(exception.getMessage());
            appendLog(t("log.error", exception.getMessage()));
        }
    }

    private void onPause() {
        automationService.pause();
        appendLog(t("log.automationPaused"));
    }

    private void onResume() {
        automationService.resume();
        appendLog(t("log.automationResumed"));
    }

    private void onStop() {
        automationService.stop();
        statusLogController.getMessageLabel().setText(t("msg.automationStopped"));
        appendLog(t("log.automationStopped"));
        removeTrayIcon();
    }

    private void onSaveProfile() {
        try {
            AutomationProfile profile = currentProfileFromForm();
            profileRepository.save(profile);
            profileRepository.saveLastUsed(profile.getName());
            loadProfiles();
            profileBarController.getProfileComboBox().setValue(profile.getName());
            AppServices.appState().setActiveProfile(profile.getName());
            statusLogController.getMessageLabel().setText(t("msg.profileSaved"));
            appendLog(t("log.profileSaved", profile.getName()));
        } catch (Exception exception) {
            statusLogController.getMessageLabel().setText(exception.getMessage());
        }
    }

    private void onLoadProfile() {
        String selected = profileBarController.getProfileComboBox().getValue();
        if (selected == null || selected.trim().isEmpty()) {
            statusLogController.getMessageLabel().setText(t("msg.selectProfile"));
            return;
        }
        try {
            AutomationProfile profile = profileRepository.load(selected);
            applyProfile(profile);
            profileRepository.saveLastUsed(profile.getName());
            AppServices.appState().setActiveProfile(profile.getName());
            statusLogController.getMessageLabel().setText(t("msg.profileLoaded"));
            appendLog(t("log.profileLoaded", profile.getName()));
        } catch (Exception exception) {
            statusLogController.getMessageLabel().setText(exception.getMessage());
        }
    }

    private void onDeleteProfile() {
        String selected = profileBarController.getProfileComboBox().getValue();
        if (selected == null || selected.trim().isEmpty()) {
            statusLogController.getMessageLabel().setText(t("msg.selectProfile"));
            return;
        }
        try {
            profileRepository.delete(selected);
            loadProfiles();
            statusLogController.getMessageLabel().setText(t("msg.profileDeleted"));
            appendLog(t("log.profileDeleted", selected));
        } catch (Exception exception) {
            statusLogController.getMessageLabel().setText(exception.getMessage());
        }
    }

    private void configureTable() {
        pointsTableController.getNameColumn().setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        pointsTableController.getXColumn().setCellValueFactory(param -> new SimpleIntegerProperty(param.getValue().getX()));
        pointsTableController.getYColumn().setCellValueFactory(param -> new SimpleIntegerProperty(param.getValue().getY()));
        pointsTableController.getActionColumn().setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getActionType().name()));
        pointsTableController.getEnabledColumn().setCellValueFactory(param -> new SimpleBooleanProperty(param.getValue().isEnabled()));
        pointsTableController.getClickColumn().setCellValueFactory(param -> new SimpleStringProperty(resolveClickStatus(param.getValue())));
        pointsTableController.getDelayColumn().setCellValueFactory(param -> new SimpleLongProperty(param.getValue().getDelayAfterMs()));
        pointsTableController.getPointsTable().setItems(points);

        runSettingsController.getClickEnabledCheck().selectedProperty().addListener((obs, oldValue, newValue) ->
                pointsTableController.getPointsTable().refresh());
    }

    private void configurePointEditing() {
        pointsTableController.getPointsTable().getSelectionModel().selectedItemProperty().addListener((obs, oldPoint, newPoint) -> {
            if (newPoint == null) {
                return;
            }
            pointEditorController.getPointNameField().setText(newPoint.getName());
            pointEditorController.getXField().setText(Integer.toString(newPoint.getX()));
            pointEditorController.getYField().setText(Integer.toString(newPoint.getY()));
            pointEditorController.getActionTypeChoice().setValue(newPoint.getActionType());
            pointEditorController.getPointEnabledCheck().setSelected(newPoint.isEnabled());
            pointEditorController.getPointDelayField().setText(Long.toString(newPoint.getDelayAfterMs()));
        });

        pointsTableController.getPointsTable().setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                onUpdateSelectedPoint();
            }
        });
    }

    private void configureAutomation() {
        automationService.setListener(new AutomationListener() {
            @Override
            public void onStateChanged(final RunState state) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        controlBarController.getStateLabel().setText(state.name());
                        AppServices.appState().setRunState(state);
                        appendLog(t("log.stateChanged", state.name()));
                    }
                });
            }

            @Override
            public void onActionExecuted(final ActionPoint point, final long count) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        controlBarController.getCounterLabel().setText(Long.toString(count));
                        String execution = describeExecution(point, activeRunClickEnabled);
                        statusLogController.getMessageLabel().setText(t("msg.executed", point.getName(), execution));
                        appendLog(t("log.executed", count, point.getName(), execution));
                    }
                });
            }

            @Override
            public void onError(final String message, final Exception exception) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        String details = exception == null ? "" : (" - " + exception.getMessage());
                        statusLogController.getMessageLabel().setText(message + details);
                        AppServices.appState().setLastMessage(message + details);
                        appendLog(t("log.error", message + details));
                    }
                });
            }

            @Override
            public void onInfo(final String message) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        statusLogController.getMessageLabel().setText(message);
                        AppServices.appState().setLastMessage(message);
                        appendLog(message);
                    }
                });
            }
        });
    }

    private void configureMouseTracker() {
        mouseTrackerTimeline = new Timeline(new KeyFrame(Duration.millis(120), event -> updateMousePositionLabel()));
        mouseTrackerTimeline.setCycleCount(Timeline.INDEFINITE);
        mouseTrackerTimeline.play();
        updateMousePositionLabel();
    }

    private void updateMousePositionLabel() {
        PointerInfo pointerInfo;
        try {
            pointerInfo = MouseInfo.getPointerInfo();
        } catch (Exception exception) {
            statusLogController.getMousePositionLabel().setText(t("status.mouseUnavailable"));
            return;
        }

        if (pointerInfo == null || pointerInfo.getLocation() == null) {
            statusLogController.getMousePositionLabel().setText(t("status.mouseUnavailable"));
            return;
        }

        java.awt.Point point = pointerInfo.getLocation();
        statusLogController.getMousePositionLabel().setText(t("status.mouse", point.x, point.y));
    }

    private void configureDefaults() {
        pointEditorController.getActionTypeChoice().setItems(FXCollections.observableArrayList(ActionType.values()));
        pointEditorController.getActionTypeChoice().setValue(ActionType.MOVE_ONLY);
        pointEditorController.getPointEnabledCheck().setSelected(true);
        pointEditorController.getPointDelayField().setText("0");
        pointEditorController.getPointNameField().setText(t("point.defaultName", 1));
        pointEditorController.getXField().setText("500");
        pointEditorController.getYField().setText("500");

        runSettingsController.getIntervalField().setText("5000");
        runSettingsController.getRandomOrderCheck().setSelected(false);
        runSettingsController.getLoopCheck().setSelected(true);
        runSettingsController.getClickEnabledCheck().setSelected(false);
        runSettingsController.getJitterField().setText("0");
        runSettingsController.getStartDelayField().setText("3");

        activeRunClickEnabled = false;
        controlBarController.getStateLabel().setText(RunState.STOPPED.name());
        controlBarController.getCounterLabel().setText("0");
        statusLogController.getMessageLabel().setText(t("status.ready"));
        AppServices.appState().setRunState(RunState.STOPPED);
        AppServices.appState().setLastMessage(t("status.ready"));
        statusLogController.getShortcutsLabel().setText(t("status.shortcuts"));
        statusLogController.getAutoResumeLabel().setText(t("status.autoResume", 60));
        statusLogController.getLogArea().clear();
    }

    private void applyAppSettings() {
        AppSettings settings = settingsService.load();
        minimizeToTrayOnStartEnabled = settings.isMinimizeToTrayOnStart();
        automationService.setAutoResumeAfterInteractionMs(settings.getAutoResumeAfterInteractionSec() * 1000L);
        statusLogController.getAutoResumeLabel().setText(t("status.autoResume", settings.getAutoResumeAfterInteractionSec()));
    }

    private void loadProfiles() {
        List<String> profiles = profileRepository.listProfiles();
        profileBarController.getProfileComboBox().setItems(FXCollections.observableArrayList(profiles));
    }

    private void loadLastProfile() {
        String last = profileRepository.loadLastUsed();
        if (last != null) {
            profileBarController.getProfileComboBox().setValue(last);
            try {
                applyProfile(profileRepository.load(last));
            } catch (Exception ignored) {
                statusLogController.getMessageLabel().setText(t("status.ready"));
            }
        }
    }

    private AutomationProfile currentProfileFromForm() {
        String profileName = profileBarController.getProfileComboBox().getEditor().getText();
        if (profileName == null || profileName.trim().isEmpty()) {
            profileName = profileBarController.getProfileComboBox().getValue();
        }
        if (profileName == null || profileName.trim().isEmpty()) {
            profileName = t("profile.defaultName");
        }

        RunConfig config = new RunConfig();
        config.setIntervalMs(parseLong(runSettingsController.getIntervalField().getText(), t("field.interval")));
        config.setRandomOrder(runSettingsController.getRandomOrderCheck().isSelected());
        config.setLoop(runSettingsController.getLoopCheck().isSelected());
        config.setClickEnabled(runSettingsController.getClickEnabledCheck().isSelected());
        config.setJitterPx(parseInteger(runSettingsController.getJitterField().getText(), t("field.jitter")));
        config.setStartDelaySec(parseInteger(runSettingsController.getStartDelayField().getText(), t("field.startDelay")));

        List<ActionPoint> profilePoints = new ArrayList<ActionPoint>(points);
        return new AutomationProfile(profileName, profilePoints, config);
    }

    private void applyProfile(AutomationProfile profile) {
        profileBarController.getProfileComboBox().setValue(profile.getName());
        AppServices.appState().setActiveProfile(profile.getName());
        points.setAll(profile.getPoints());

        RunConfig config = profile.getConfig();
        runSettingsController.getIntervalField().setText(Long.toString(config.getIntervalMs()));
        runSettingsController.getRandomOrderCheck().setSelected(config.isRandomOrder());
        runSettingsController.getLoopCheck().setSelected(config.isLoop());
        runSettingsController.getClickEnabledCheck().setSelected(config.isClickEnabled());
        activeRunClickEnabled = config.isClickEnabled();
        runSettingsController.getJitterField().setText(Integer.toString(config.getJitterPx()));
        runSettingsController.getStartDelayField().setText(Integer.toString(config.getStartDelaySec()));
        pointEditorController.getPointNameField().setText(buildNextPointName());
        pointsTableController.getPointsTable().refresh();
    }

    private boolean isGlobalHookActive() {
        return globalKeyListener != null && GlobalScreen.isNativeHookRegistered() && globalHookAvailable;
    }

    private void registerGlobalKeyboardHook() {
        if (globalKeyListener != null) {
            return;
        }
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);
        try {
            if (!GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.registerNativeHook();
            }
            globalKeyListener = new NativeKeyListener() {
                @Override
                public void nativeKeyTyped(NativeKeyEvent nativeEvent) {}
                @Override
                public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
                    if (nativeEvent.getKeyCode() == GLOBAL_ADD_POINT_KEY && AppServices.appState().getRunState() == RunState.STOPPED) {
                        Platform.runLater(() -> addPointFromCurrentMousePosition());
                        return;
                    }
                    if (nativeEvent.getKeyCode() == GLOBAL_STOP_KEY && AppServices.appState().getRunState() == RunState.RUNNING) {
                        Platform.runLater(() -> {
                            onStop();
                            restoreFromTray();
                        });
                        return;
                    }
                    automationService.notifyManualInteraction();
                }
                @Override
                public void nativeKeyReleased(NativeKeyEvent nativeEvent) {}
            };
            GlobalScreen.addNativeKeyListener(globalKeyListener);
            globalHookAvailable = true;
        } catch (NativeHookException exception) {
            globalHookAvailable = false;
            statusLogController.getMessageLabel().setText(t("msg.globalKeyboardHookNotAvailable", exception.getMessage()));
        }
    }

    private void unregisterGlobalKeyboardHook() {
        if (globalKeyListener != null) {
            GlobalScreen.removeNativeKeyListener(globalKeyListener);
            globalKeyListener = null;
        }
        try {
            if (GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.unregisterNativeHook();
            }
            globalHookAvailable = false;
        } catch (NativeHookException ignored) {
            globalHookAvailable = false;
        }
    }

    private void addPointFromCurrentMousePosition() {
        PointerInfo pointerInfo;
        try {
            pointerInfo = MouseInfo.getPointerInfo();
        } catch (Exception exception) {
            statusLogController.getMessageLabel().setText(t("msg.mousePositionUnavailable"));
            return;
        }

        if (pointerInfo == null || pointerInfo.getLocation() == null) {
            statusLogController.getMessageLabel().setText(t("msg.mousePositionUnavailable"));
            return;
        }

        java.awt.Point pointerPoint = pointerInfo.getLocation();
        pointEditorController.getXField().setText(Integer.toString(pointerPoint.x));
        pointEditorController.getYField().setText(Integer.toString(pointerPoint.y));

        try {
            ActionPoint point = buildPointFromEditor(buildNextPointName());
            points.add(point);
            pointsTableController.getPointsTable().getSelectionModel().select(point);
            pointsTableController.getPointsTable().scrollTo(point);
            pointEditorController.getPointNameField().setText(buildNextPointName());
            statusLogController.getMessageLabel().setText(t("msg.pointAddedFromCursor"));
            appendLog(t("log.pointAddedFromCursor", point.getName(), point.getX(), point.getY()));
        } catch (Exception exception) {
            statusLogController.getMessageLabel().setText(exception.getMessage());
        }
    }

    private ActionPoint buildPointFromEditor(String suggestedName) {
        String name = pointEditorController.getPointNameField().getText();
        if (name == null || name.trim().isEmpty()) {
            name = suggestedName;
        }
        ActionType actionType = pointEditorController.getActionTypeChoice().getValue() == null
                ? ActionType.MOVE_ONLY
                : pointEditorController.getActionTypeChoice().getValue();

        return new ActionPoint(
                name,
                parseInteger(pointEditorController.getXField().getText(), t("field.x")),
                parseInteger(pointEditorController.getYField().getText(), t("field.y")),
                actionType,
                pointEditorController.getPointEnabledCheck().isSelected(),
                safeParseLong(pointEditorController.getPointDelayField().getText(), 0L)
        );
    }

    private void minimizeToTrayOnStart() {
        if (stage == null || !SystemTray.isSupported()) {
            return;
        }

        try {
            ensureTrayIcon();
            captureStageBounds(true);

            trayIcon.displayMessage(t("app.title"), t("tray.running"), TrayIcon.MessageType.INFO);
            stage.setIconified(true);
            stage.hide();
        } catch (Exception exception) {
            appendLog(t("log.trayUnavailable", exception.getMessage()));
        }
    }

    private void refreshTrayLocalization() {
        if (trayIcon == null || !SystemTray.isSupported()) {
            return;
        }
        boolean stageHidden = stage != null && !stage.isShowing();
        try {
            removeTrayIcon();
            ensureTrayIcon();
            if (!stageHidden && stage != null) {
                stage.show();
            }
        } catch (Exception exception) {
            appendLog(t("log.trayUnavailable", exception.getMessage()));
        }
    }

    private void ensureTrayIcon() throws Exception {
        if (trayIcon != null) {
            return;
        }
        Image image = buildTrayImage();
        PopupMenu menu = new PopupMenu();

        MenuItem showItem = new MenuItem(t("tray.show"));
        showItem.addActionListener(event -> Platform.runLater(new Runnable() {
            @Override
            public void run() {
                restoreFromTray();
            }
        }));

        MenuItem stopItem = new MenuItem(t("tray.stop"));
        stopItem.addActionListener(event -> Platform.runLater(new Runnable() {
            @Override
            public void run() {
                onStop();
                restoreFromTray();
            }
        }));

        MenuItem exitItem = new MenuItem(t("tray.exit"));
        exitItem.addActionListener(event -> Platform.runLater(new Runnable() {
            @Override
            public void run() {
                shutdown();
                Platform.exit();
            }
        }));

        menu.add(showItem);
        menu.add(stopItem);
        menu.add(exitItem);

        trayIcon = new TrayIcon(image, t("app.title"), menu);
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 1) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            restoreFromTray();
                        }
                    });
                }
            }
        });
        SystemTray.getSystemTray().add(trayIcon);
    }

    private void restoreFromTray() {
        if (stage == null) {
            return;
        }

        restoreStageBoundsIfNeeded();
        stage.setIconified(false);
        if (!stage.isShowing()) {
            stage.show();
        }
        ensureStageVisibleOnScreen();
        stage.toFront();
        stage.requestFocus();
        boolean wasAlwaysOnTop = stage.isAlwaysOnTop();
        stage.setAlwaysOnTop(true);
        stage.setAlwaysOnTop(wasAlwaysOnTop);
    }

    private void ensureStageVisibleOnScreen() {
        Rectangle2D stageBounds = new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
        for (Screen screen : Screen.getScreens()) {
            if (intersects(stageBounds, screen.getVisualBounds())) {
                return;
            }
        }
        stage.centerOnScreen();
    }

    private boolean intersects(Rectangle2D a, Rectangle2D b) {
        double ax2 = a.getMinX() + a.getWidth();
        double ay2 = a.getMinY() + a.getHeight();
        double bx2 = b.getMinX() + b.getWidth();
        double by2 = b.getMinY() + b.getHeight();
        return ax2 > b.getMinX() && bx2 > a.getMinX() && ay2 > b.getMinY() && by2 > a.getMinY();
    }

    private void captureStageBounds(boolean force) {
        if (stage == null) {
            return;
        }
        if (!force && (!stage.isShowing() || stage.isIconified())) {
            return;
        }
        double width = stage.getWidth();
        double height = stage.getHeight();
        if (width >= MIN_WINDOW_WIDTH && height >= MIN_WINDOW_HEIGHT) {
            lastWindowX = stage.getX();
            lastWindowY = stage.getY();
            lastWindowWidth = width;
            lastWindowHeight = height;
        }
    }

    private void restoreStageBoundsIfNeeded() {
        if (stage == null) {
            return;
        }

        if (lastWindowWidth >= MIN_WINDOW_WIDTH && lastWindowHeight >= MIN_WINDOW_HEIGHT) {
            stage.setWidth(lastWindowWidth);
            stage.setHeight(lastWindowHeight);
            if (lastWindowX >= 0 && lastWindowY >= 0) {
                stage.setX(lastWindowX);
                stage.setY(lastWindowY);
            }
            return;
        }

        if (stage.getWidth() < MIN_WINDOW_WIDTH || stage.getHeight() < MIN_WINDOW_HEIGHT) {
            AppSettings settings = settingsService.load();
            stage.setWidth(Math.max(MIN_WINDOW_WIDTH, settings.getWindowWidth()));
            stage.setHeight(Math.max(MIN_WINDOW_HEIGHT, settings.getWindowHeight()));
            if (settings.hasWindowPosition()) {
                stage.setX(settings.getWindowX());
                stage.setY(settings.getWindowY());
            }
        }
    }

    private void removeTrayIcon() {
        if (trayIcon != null && SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
        }
    }

    private Image buildTrayImage() {
        Image resourceIcon = IconResources.loadAwtIcon();
        if (resourceIcon != null) {
            return resourceIcon;
        }
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(31, 79, 177));
        graphics.fillRoundRect(0, 0, 16, 16, 4, 4);
        graphics.setColor(Color.WHITE);
        graphics.drawString("A", 5, 12);
        graphics.dispose();
        return image;
    }

    private String resolveClickStatus(ActionPoint point) {
        if (point.getActionType() == ActionType.MOVE_ONLY) {
            return "-";
        }
        return runSettingsController.getClickEnabledCheck().isSelected() ? t("status.on") : t("status.off");
    }

    private String describeExecution(ActionPoint point, boolean clicksEnabledInRun) {
        if (!clicksEnabledInRun || point.getActionType() == ActionType.MOVE_ONLY) {
            return t("execution.move");
        }
        if (point.getActionType() == ActionType.LEFT_CLICK) {
            return t("execution.moveLeftClick");
        }
        if (point.getActionType() == ActionType.RIGHT_CLICK) {
            return t("execution.moveRightClick");
        }
        if (point.getActionType() == ActionType.DOUBLE_CLICK) {
            return t("execution.moveDoubleClick");
        }
        return t("execution.move");
    }

    private void appendLog(String message) {
        String line = "[" + LocalTime.now().format(logTimeFormatter) + "] " + message + "\n";
        statusLogController.getLogArea().appendText(line);
        statusLogController.getLogArea().setScrollTop(Double.MAX_VALUE);
        appLogService.append(message);
    }

    private int parseInteger(String value, String label) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception exception) {
            throw new IllegalArgumentException(t("validation.invalidInteger", label));
        }
    }

    private long parseLong(String value, String label) {
        try {
            return Long.parseLong(value.trim());
        } catch (Exception exception) {
            throw new IllegalArgumentException(t("validation.invalidNumber", label));
        }
    }

    private long safeParseLong(String value, long fallbackValue) {
        try {
            return Long.parseLong(value == null ? "" : value.trim());
        } catch (Exception exception) {
            return fallbackValue;
        }
    }

    private String buildNextPointName() {
        return t("point.defaultName", points.size() + 1);
    }

    private String t(String key, Object... args) {
        return i18nService.text(key, args);
    }
}
