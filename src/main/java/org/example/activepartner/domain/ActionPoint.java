package org.example.activepartner.domain;

public class ActionPoint {
    private String name;
    private int x;
    private int y;
    private ActionType actionType;
    private boolean enabled;
    private long delayAfterMs;

    public ActionPoint() {
        this("Point", 0, 0, ActionType.MOVE_ONLY, true, 0L);
    }

    public ActionPoint(String name, int x, int y, ActionType actionType, boolean enabled, long delayAfterMs) {
        setName(name);
        setX(x);
        setY(y);
        setActionType(actionType);
        this.enabled = enabled;
        setDelayAfterMs(delayAfterMs);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Point name must not be blank");
        }
        this.name = name.trim();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType == null ? ActionType.MOVE_ONLY : actionType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getDelayAfterMs() {
        return delayAfterMs;
    }

    public void setDelayAfterMs(long delayAfterMs) {
        if (delayAfterMs < 0L) {
            throw new IllegalArgumentException("Delay must be >= 0");
        }
        this.delayAfterMs = delayAfterMs;
    }
}
