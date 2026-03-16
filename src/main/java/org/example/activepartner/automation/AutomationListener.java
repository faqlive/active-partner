package org.example.activepartner.automation;

import org.example.activepartner.domain.ActionPoint;
import org.example.activepartner.domain.RunState;

public interface AutomationListener {
    void onStateChanged(RunState state);

    void onActionExecuted(ActionPoint point, long count);

    void onError(String message, Exception exception);

    default void onInfo(String message) {
    }
}
