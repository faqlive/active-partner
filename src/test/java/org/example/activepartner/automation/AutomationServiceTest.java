package org.example.activepartner.automation;

import org.example.activepartner.domain.ActionPoint;
import org.example.activepartner.domain.ActionType;
import org.example.activepartner.domain.AutomationProfile;
import org.example.activepartner.domain.RunConfig;
import org.example.activepartner.domain.RunState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class AutomationServiceTest {

    @Test
    void executesPointsInFixedOrderWithoutClicksWhenDisabled() throws InterruptedException {
        RecordingMouseController mouseController = new RecordingMouseController();
        AutomationService service = new AutomationService(
                mouseController,
                new StubScreenBoundsService(),
                new Random(123),
                false
        );

        CountDownLatch latch = new CountDownLatch(2);
        service.setListener(new AutomationListener() {
            @Override
            public void onStateChanged(RunState state) {
            }

            @Override
            public void onActionExecuted(ActionPoint point, long count) {
                latch.countDown();
            }

            @Override
            public void onError(String message, Exception exception) {
                Assertions.fail(message);
            }
        });

        service.start(profile(false, false, true));
        boolean completed = latch.await(3, TimeUnit.SECONDS);
        service.stop();

        Assertions.assertTrue(completed);
        Assertions.assertEquals(2, mouseController.moves.size());
        Assertions.assertEquals("100,100", mouseController.moves.get(0));
        Assertions.assertEquals("200,200", mouseController.moves.get(1));
        Assertions.assertEquals(0, mouseController.leftClicks);
        Assertions.assertEquals(0, mouseController.rightClicks);
    }

    @Test
    void pausesAndResumesExecution() throws InterruptedException {
        RecordingMouseController mouseController = new RecordingMouseController();
        AutomationService service = new AutomationService(
                mouseController,
                new StubScreenBoundsService(),
                new Random(123),
                false
        );

        CountDownLatch firstAction = new CountDownLatch(1);
        service.setListener(new AutomationListener() {
            @Override
            public void onStateChanged(RunState state) {
            }

            @Override
            public void onActionExecuted(ActionPoint point, long count) {
                if (count == 1L) {
                    firstAction.countDown();
                }
            }

            @Override
            public void onError(String message, Exception exception) {
                Assertions.fail(message);
            }
        });

        service.start(profile(false, false, true));
        Assertions.assertTrue(firstAction.await(2, TimeUnit.SECONDS));

        service.pause();
        int actionsAfterPause = mouseController.moves.size();
        Thread.sleep(600L);
        Assertions.assertEquals(actionsAfterPause, mouseController.moves.size());

        service.resume();
        Thread.sleep(700L);
        service.stop();

        Assertions.assertTrue(mouseController.moves.size() > actionsAfterPause);
    }

    @Test
    void executesClicksWhenEnabled() throws InterruptedException {
        RecordingMouseController mouseController = new RecordingMouseController();
        AutomationService service = new AutomationService(
                mouseController,
                new StubScreenBoundsService(),
                new Random(123),
                false
        );

        CountDownLatch firstAction = new CountDownLatch(1);
        service.setListener(new AutomationListener() {
            @Override
            public void onStateChanged(RunState state) {
            }

            @Override
            public void onActionExecuted(ActionPoint point, long count) {
                firstAction.countDown();
            }

            @Override
            public void onError(String message, Exception exception) {
                Assertions.fail(message);
            }
        });

        service.start(profile(false, true, true));
        Assertions.assertTrue(firstAction.await(2, TimeUnit.SECONDS));
        service.stop();

        Assertions.assertTrue(mouseController.leftClicks > 0 || mouseController.rightClicks > 0);
    }

    private AutomationProfile profile(boolean randomOrder, boolean clickEnabled, boolean loop) {
        RunConfig config = new RunConfig();
        config.setIntervalMs(500L);
        config.setRandomOrder(randomOrder);
        config.setClickEnabled(clickEnabled);
        config.setLoop(loop);
        config.setStartDelaySec(0);
        config.setJitterPx(0);

        List<ActionPoint> points = new ArrayList<ActionPoint>();
        points.add(new ActionPoint("A", 100, 100, ActionType.LEFT_CLICK, true, 0L));
        points.add(new ActionPoint("B", 200, 200, ActionType.RIGHT_CLICK, true, 0L));
        return new AutomationProfile("Test", points, config);
    }

    private static class RecordingMouseController implements MouseController {
        private final List<String> moves = Collections.synchronizedList(new ArrayList<String>());
        private int leftClicks;
        private int rightClicks;

        @Override
        public void move(int x, int y) {
            moves.add(x + "," + y);
        }

        @Override
        public void leftClick() {
            leftClicks++;
        }

        @Override
        public void rightClick() {
            rightClicks++;
        }

        @Override
        public void doubleClick() {
            leftClicks += 2;
        }
    }

    private static class StubScreenBoundsService extends ScreenBoundsService {
        @Override
        public List<Rectangle> getScreenBounds() {
            return Collections.singletonList(new Rectangle(0, 0, 1920, 1080));
        }
    }
}
