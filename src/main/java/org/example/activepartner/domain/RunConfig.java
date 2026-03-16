package org.example.activepartner.domain;

public class RunConfig {
    public static final long MIN_INTERVAL_MS = 500L;
    public static final int MAX_JITTER_PX = 20;
    public static final int MAX_START_DELAY_SEC = 60;

    private long intervalMs;
    private boolean randomOrder;
    private boolean loop;
    private boolean clickEnabled;
    private int jitterPx;
    private int startDelaySec;

    public RunConfig() {
        this.intervalMs = 5000L;
        this.randomOrder = false;
        this.loop = true;
        this.clickEnabled = false;
        this.jitterPx = 0;
        this.startDelaySec = 3;
    }

    public long getIntervalMs() {
        return intervalMs;
    }

    public void setIntervalMs(long intervalMs) {
        if (intervalMs < MIN_INTERVAL_MS) {
            throw new IllegalArgumentException("Interval must be >= " + MIN_INTERVAL_MS + "ms");
        }
        this.intervalMs = intervalMs;
    }

    public boolean isRandomOrder() {
        return randomOrder;
    }

    public void setRandomOrder(boolean randomOrder) {
        this.randomOrder = randomOrder;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public boolean isClickEnabled() {
        return clickEnabled;
    }

    public void setClickEnabled(boolean clickEnabled) {
        this.clickEnabled = clickEnabled;
    }

    public int getJitterPx() {
        return jitterPx;
    }

    public void setJitterPx(int jitterPx) {
        if (jitterPx < 0 || jitterPx > MAX_JITTER_PX) {
            throw new IllegalArgumentException("Jitter must be between 0 and " + MAX_JITTER_PX);
        }
        this.jitterPx = jitterPx;
    }

    public int getStartDelaySec() {
        return startDelaySec;
    }

    public void setStartDelaySec(int startDelaySec) {
        if (startDelaySec < 0 || startDelaySec > MAX_START_DELAY_SEC) {
            throw new IllegalArgumentException("Start delay must be between 0 and " + MAX_START_DELAY_SEC);
        }
        this.startDelaySec = startDelaySec;
    }
}
