package org.example.activepartner.automation;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;

public class RobotMouseController implements MouseController {
    private static final int DOUBLE_CLICK_DELAY_MS = 100;
    private static final int CLICK_SETTLE_DELAY_MS = 35;
    private static final int LEFT_BUTTON_MASK = InputEvent.getMaskForButton(1);
    private static final int RIGHT_BUTTON_MASK = InputEvent.getMaskForButton(3);

    private final Robot robot;

    public RobotMouseController() {
        try {
            this.robot = new Robot();
            this.robot.setAutoWaitForIdle(false);
        } catch (AWTException exception) {
            throw new IllegalStateException("Unable to initialize mouse controller", exception);
        }
    }

    @Override
    public void move(int x, int y) {
        robot.mouseMove(x, y);
    }

    @Override
    public void leftClick() {
        click(LEFT_BUTTON_MASK);
    }

    @Override
    public void rightClick() {
        click(RIGHT_BUTTON_MASK);
    }

    @Override
    public void doubleClick() {
        leftClick();
        robot.delay(DOUBLE_CLICK_DELAY_MS);
        leftClick();
    }

    private void click(int mask) {
        robot.delay(CLICK_SETTLE_DELAY_MS);
        robot.mousePress(mask);
        robot.mouseRelease(mask);
    }
}
