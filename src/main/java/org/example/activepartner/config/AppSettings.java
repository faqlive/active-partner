package org.example.activepartner.config;

public class AppSettings {
    private boolean minimizeToTrayOnStart;
    private int autoResumeAfterInteractionSec;
    private String languageCode;
    private double windowX;
    private double windowY;
    private double windowWidth;
    private double windowHeight;

    public AppSettings() {
        this.minimizeToTrayOnStart = true;
        this.autoResumeAfterInteractionSec = 60;
        this.languageCode = "es";
        this.windowX = -1;
        this.windowY = -1;
        this.windowWidth = 920;
        this.windowHeight = 700;
    }

    public boolean isMinimizeToTrayOnStart() {
        return minimizeToTrayOnStart;
    }

    public void setMinimizeToTrayOnStart(boolean minimizeToTrayOnStart) {
        this.minimizeToTrayOnStart = minimizeToTrayOnStart;
    }

    public int getAutoResumeAfterInteractionSec() {
        return autoResumeAfterInteractionSec;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            this.languageCode = "es";
            return;
        }
        String normalized = languageCode.trim().toLowerCase();
        if (!"es".equals(normalized) && !"en".equals(normalized)) {
            throw new IllegalArgumentException("Language must be 'es' or 'en'");
        }
        this.languageCode = normalized;
    }

    public void setAutoResumeAfterInteractionSec(int autoResumeAfterInteractionSec) {
        if (autoResumeAfterInteractionSec < 5 || autoResumeAfterInteractionSec > 600) {
            throw new IllegalArgumentException("Auto-resume delay must be between 5 and 600 seconds");
        }
        this.autoResumeAfterInteractionSec = autoResumeAfterInteractionSec;
    }

    public double getWindowX() {
        return windowX;
    }

    public void setWindowX(double windowX) {
        this.windowX = windowX;
    }

    public double getWindowY() {
        return windowY;
    }

    public void setWindowY(double windowY) {
        this.windowY = windowY;
    }

    public double getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(double windowWidth) {
        if (windowWidth < 500) {
            throw new IllegalArgumentException("Window width must be >= 500");
        }
        this.windowWidth = windowWidth;
    }

    public double getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(double windowHeight) {
        if (windowHeight < 400) {
            throw new IllegalArgumentException("Window height must be >= 400");
        }
        this.windowHeight = windowHeight;
    }

    public boolean hasWindowPosition() {
        return windowX >= 0 && windowY >= 0;
    }
}
