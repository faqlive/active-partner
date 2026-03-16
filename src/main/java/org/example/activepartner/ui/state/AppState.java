package org.example.activepartner.ui.state;

import org.example.activepartner.domain.RunState;

public class AppState {
    private RunState runState;
    private String activeProfile;
    private String lastMessage;

    public AppState() {
        this.runState = RunState.STOPPED;
        this.activeProfile = "Default";
        this.lastMessage = "Ready.";
    }

    public RunState getRunState() {
        return runState;
    }

    public void setRunState(RunState runState) {
        this.runState = runState;
    }

    public String getActiveProfile() {
        return activeProfile;
    }

    public void setActiveProfile(String activeProfile) {
        this.activeProfile = activeProfile;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
