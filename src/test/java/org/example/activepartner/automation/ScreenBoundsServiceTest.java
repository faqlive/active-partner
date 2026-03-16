package org.example.activepartner.automation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;

class ScreenBoundsServiceTest {

    @Test
    void validatesPointInsideConfiguredBounds() {
        ScreenBoundsService service = new StubScreenBoundsService(Arrays.asList(
                new Rectangle(0, 0, 1920, 1080),
                new Rectangle(-1280, 0, 1280, 1024)
        ));

        Assertions.assertTrue(service.isValidPoint(100, 100));
        Assertions.assertTrue(service.isValidPoint(-200, 100));
        Assertions.assertFalse(service.isValidPoint(5000, 5000));
    }

    @Test
    void clampsPointToNearestDisplayBounds() {
        ScreenBoundsService service = new StubScreenBoundsService(Arrays.asList(
                new Rectangle(0, 0, 1920, 1080)
        ));

        Point clamped = service.clamp(2500, 1200);
        Assertions.assertEquals(1919, clamped.x);
        Assertions.assertEquals(1079, clamped.y);
    }

    private static class StubScreenBoundsService extends ScreenBoundsService {
        private final List<Rectangle> rectangles;

        private StubScreenBoundsService(List<Rectangle> rectangles) {
            this.rectangles = rectangles;
        }

        @Override
        public List<Rectangle> getScreenBounds() {
            return rectangles;
        }
    }
}
