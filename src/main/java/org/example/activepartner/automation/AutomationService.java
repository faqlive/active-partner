package org.example.activepartner.automation;

import org.example.activepartner.domain.ActionPoint;
import org.example.activepartner.domain.ActionType;
import org.example.activepartner.domain.AutomationProfile;
import org.example.activepartner.domain.RunConfig;
import org.example.activepartner.domain.RunState;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class AutomationService {
    private static final long PAUSE_POLL_DELAY_MS = 250L;
    private static final long DEFAULT_AUTO_RESUME_AFTER_INTERACTION_MS = 60_000L;
    private static final int POINTER_DRIFT_TOLERANCE_PX = 3;

    private final MouseController mouseController;
    private final ScreenBoundsService screenBoundsService;
    private final Random random;
    private final boolean manualInteractionMonitoringEnabled;
    private final Object lock;
    private final AtomicReference<RunState> state;
    private final AtomicLong actionCounter;

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledFuture;
    private AutomationListener listener;
    private AutomationProfile activeProfile;
    private List<ActionPoint> enabledPoints;
    private List<ActionPoint> remainingPoints;
    private int currentIndex;
    private boolean manualPaused;
    private boolean autoPausedByInteraction;
    private long autoResumeAtMs;
    private long autoResumeAfterInteractionMs;
    private Point lastAutomatedPointer;
    private Point lastObservedPointer;

    public AutomationService() {
        this(new RobotMouseController(), new ScreenBoundsService(), new Random(), true);
    }

    public AutomationService(MouseController mouseController, ScreenBoundsService screenBoundsService, Random random) {
        this(mouseController, screenBoundsService, random, true);
    }

    public AutomationService(
            MouseController mouseController,
            ScreenBoundsService screenBoundsService,
            Random random,
            boolean manualInteractionMonitoringEnabled
    ) {
        this.mouseController = mouseController;
        this.screenBoundsService = screenBoundsService;
        this.random = random;
        this.manualInteractionMonitoringEnabled = manualInteractionMonitoringEnabled;
        this.lock = new Object();
        this.state = new AtomicReference<RunState>(RunState.STOPPED);
        this.actionCounter = new AtomicLong(0L);
        this.enabledPoints = Collections.emptyList();
        this.remainingPoints = Collections.emptyList();
        this.currentIndex = 0;
        this.manualPaused = false;
        this.autoPausedByInteraction = false;
        this.autoResumeAtMs = 0L;
        this.autoResumeAfterInteractionMs = DEFAULT_AUTO_RESUME_AFTER_INTERACTION_MS;
        this.lastAutomatedPointer = null;
        this.lastObservedPointer = null;
    }

    public void setAutoResumeAfterInteractionMs(long autoResumeAfterInteractionMs) {
        if (autoResumeAfterInteractionMs < 5000L || autoResumeAfterInteractionMs > 600_000L) {
            throw new IllegalArgumentException("Auto-resume delay must be between 5 and 600 seconds");
        }
        this.autoResumeAfterInteractionMs = autoResumeAfterInteractionMs;
    }

    public void setListener(AutomationListener listener) {
        this.listener = listener;
    }

    public RunState getState() {
        return state.get();
    }

    public long getActionCounter() {
        return actionCounter.get();
    }

    public void start(AutomationProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("Profile must not be null");
        }

        synchronized (lock) {
            stopInternal(false);

            this.activeProfile = copyProfile(profile);
            this.enabledPoints = filterEnabledPoints(activeProfile.getPoints());
            if (enabledPoints.isEmpty()) {
                throw new IllegalArgumentException("At least one enabled point is required");
            }
            this.remainingPoints = new ArrayList<ActionPoint>(enabledPoints);
            this.currentIndex = 0;
            this.actionCounter.set(0L);
            this.manualPaused = false;
            this.autoPausedByInteraction = false;
            this.autoResumeAtMs = 0L;
            this.lastAutomatedPointer = null;
            this.lastObservedPointer = null;

            this.scheduler = Executors.newSingleThreadScheduledExecutor();
            updateState(RunState.RUNNING);

            long startDelayMs = activeProfile.getConfig().getStartDelaySec() * 1000L;
            scheduleNext(startDelayMs);
        }
    }

    public void pause() {
        synchronized (lock) {
            manualPaused = true;
            if (state.get() == RunState.RUNNING) {
                updateState(RunState.PAUSED);
            }
        }
    }

    public void resume() {
        synchronized (lock) {
            manualPaused = false;
            if (!autoPausedByInteraction && state.get() == RunState.PAUSED) {
                updateState(RunState.RUNNING);
            }
        }
    }

    public void notifyManualInteraction() {
        synchronized (lock) {
            if (!manualInteractionMonitoringEnabled) {
                return;
            }
            if (state.get() == RunState.STOPPED) {
                return;
            }
            autoPausedByInteraction = true;
            autoResumeAtMs = System.currentTimeMillis() + autoResumeAfterInteractionMs;
            if (state.get() == RunState.RUNNING) {
                updateState(RunState.PAUSED);
                notifyInfo("Manual interaction detected. Auto-resume in " + (autoResumeAfterInteractionMs / 1000L) + "s.");
            }
        }
    }

    public void stop() {
        synchronized (lock) {
            stopInternal(true);
        }
    }

    private void stopInternal(boolean notify) {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        enabledPoints = Collections.emptyList();
        remainingPoints = Collections.emptyList();
        currentIndex = 0;
        manualPaused = false;
        autoPausedByInteraction = false;
        autoResumeAtMs = 0L;
        lastAutomatedPointer = null;
        lastObservedPointer = null;

        RunState previous = state.getAndSet(RunState.STOPPED);
        if (notify && previous != RunState.STOPPED) {
            notifyStateChanged(RunState.STOPPED);
        }
    }

    private void scheduleNext(long delayMs) {
        if (scheduler == null || scheduler.isShutdown()) {
            return;
        }

        scheduledFuture = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                tick();
            }
        }, Math.max(0L, delayMs), TimeUnit.MILLISECONDS);
    }

    private void tick() {
        try {
            RunState currentState = state.get();
            if (currentState == RunState.STOPPED) {
                return;
            }

            if (manualInteractionMonitoringEnabled && detectManualPointerMovement()) {
                notifyManualInteraction();
                scheduleNext(PAUSE_POLL_DELAY_MS);
                return;
            }

            if (autoPausedByInteraction && System.currentTimeMillis() >= autoResumeAtMs && !manualPaused) {
                autoPausedByInteraction = false;
                updateState(RunState.RUNNING);
                notifyInfo("Resumed after inactivity timeout.");
            }

            if (currentState == RunState.PAUSED) {
                scheduleNext(PAUSE_POLL_DELAY_MS);
                return;
            }

            ActionPoint actionPoint;
            synchronized (lock) {
                actionPoint = nextPoint();
            }

            if (actionPoint == null) {
                stop();
                return;
            }

            execute(actionPoint);
            long count = actionCounter.incrementAndGet();
            notifyActionExecuted(actionPoint, count);

            long nextDelay = activeProfile.getConfig().getIntervalMs() + actionPoint.getDelayAfterMs();
            scheduleNext(nextDelay);
        } catch (Exception exception) {
            notifyError("Automation execution failed", exception);
            stop();
        }
    }

    private ActionPoint nextPoint() {
        RunConfig config = activeProfile.getConfig();
        if (enabledPoints.isEmpty()) {
            return null;
        }

        if (config.isRandomOrder()) {
            if (!config.isLoop() && remainingPoints.isEmpty()) {
                return null;
            }

            List<ActionPoint> source = config.isLoop() ? enabledPoints : remainingPoints;
            int randomIndex = random.nextInt(source.size());
            ActionPoint point = source.get(randomIndex);

            if (!config.isLoop()) {
                remainingPoints.remove(randomIndex);
            }

            return point;
        }

        if (currentIndex >= enabledPoints.size()) {
            if (!config.isLoop()) {
                return null;
            }
            currentIndex = 0;
        }

        ActionPoint point = enabledPoints.get(currentIndex);
        currentIndex++;
        return point;
    }

    private void execute(ActionPoint actionPoint) {
        RunConfig config = activeProfile.getConfig();
        int jitter = config.getJitterPx();

        int targetX = actionPoint.getX();
        int targetY = actionPoint.getY();
        if (jitter > 0) {
            targetX += random.nextInt(jitter * 2 + 1) - jitter;
            targetY += random.nextInt(jitter * 2 + 1) - jitter;
        }

        Point clampedPoint = screenBoundsService.clamp(targetX, targetY);
        mouseController.move(clampedPoint.x, clampedPoint.y);
        lastAutomatedPointer = new Point(clampedPoint);
        lastObservedPointer = new Point(clampedPoint);

        if (!config.isClickEnabled()) {
            return;
        }

        ActionType actionType = actionPoint.getActionType();
        if (actionType == ActionType.LEFT_CLICK) {
            mouseController.leftClick();
        } else if (actionType == ActionType.RIGHT_CLICK) {
            mouseController.rightClick();
        } else if (actionType == ActionType.DOUBLE_CLICK) {
            mouseController.doubleClick();
        }
    }

    private AutomationProfile copyProfile(AutomationProfile profile) {
        List<ActionPoint> copiedPoints = new ArrayList<ActionPoint>();
        for (ActionPoint point : profile.getPoints()) {
            copiedPoints.add(new ActionPoint(
                    point.getName(),
                    point.getX(),
                    point.getY(),
                    point.getActionType(),
                    point.isEnabled(),
                    point.getDelayAfterMs()
            ));
        }

        RunConfig sourceConfig = profile.getConfig();
        RunConfig copiedConfig = new RunConfig();
        copiedConfig.setIntervalMs(sourceConfig.getIntervalMs());
        copiedConfig.setRandomOrder(sourceConfig.isRandomOrder());
        copiedConfig.setLoop(sourceConfig.isLoop());
        copiedConfig.setClickEnabled(sourceConfig.isClickEnabled());
        copiedConfig.setJitterPx(sourceConfig.getJitterPx());
        copiedConfig.setStartDelaySec(sourceConfig.getStartDelaySec());

        return new AutomationProfile(profile.getName(), copiedPoints, copiedConfig);
    }

    private List<ActionPoint> filterEnabledPoints(List<ActionPoint> points) {
        List<ActionPoint> result = new ArrayList<ActionPoint>();
        for (ActionPoint point : points) {
            if (point != null && point.isEnabled()) {
                result.add(point);
            }
        }
        return result;
    }

    private void updateState(RunState newState) {
        state.set(newState);
        notifyStateChanged(newState);
    }

    private void notifyStateChanged(RunState runState) {
        if (listener != null) {
            listener.onStateChanged(runState);
        }
    }

    private void notifyActionExecuted(ActionPoint point, long count) {
        if (listener != null) {
            listener.onActionExecuted(point, count);
        }
    }

    private void notifyError(String message, Exception exception) {
        if (listener != null) {
            listener.onError(message, exception);
        }
    }

    private void notifyInfo(String message) {
        if (listener != null) {
            listener.onInfo(message);
        }
    }

    private boolean detectManualPointerMovement() {
        if (state.get() == RunState.STOPPED) {
            return false;
        }

        PointerInfo pointerInfo;
        try {
            pointerInfo = MouseInfo.getPointerInfo();
        } catch (Exception exception) {
            return false;
        }
        if (pointerInfo == null || pointerInfo.getLocation() == null) {
            return false;
        }

        Point current = pointerInfo.getLocation();
        if (lastObservedPointer == null) {
            lastObservedPointer = current;
            return false;
        }

        int deltaX = Math.abs(current.x - lastObservedPointer.x);
        int deltaY = Math.abs(current.y - lastObservedPointer.y);
        lastObservedPointer = current;

        if (lastAutomatedPointer != null) {
            int autoDeltaX = Math.abs(current.x - lastAutomatedPointer.x);
            int autoDeltaY = Math.abs(current.y - lastAutomatedPointer.y);
            if (autoDeltaX <= POINTER_DRIFT_TOLERANCE_PX && autoDeltaY <= POINTER_DRIFT_TOLERANCE_PX) {
                return false;
            }
        }

        return deltaX > POINTER_DRIFT_TOLERANCE_PX || deltaY > POINTER_DRIFT_TOLERANCE_PX;
    }
}
