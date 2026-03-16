package org.example.activepartner.automation;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class ScreenBoundsService {

    public boolean isValidPoint(int x, int y) {
        for (Rectangle bounds : getScreenBounds()) {
            if (bounds.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    public Point clamp(int x, int y) {
        List<Rectangle> bounds = getScreenBounds();
        if (bounds.isEmpty()) {
            return new Point(x, y);
        }

        for (Rectangle rectangle : bounds) {
            if (rectangle.contains(x, y)) {
                return new Point(x, y);
            }
        }

        Rectangle nearest = bounds.get(0);
        long nearestDistance = distanceSquaredToRectangle(x, y, nearest);
        for (int i = 1; i < bounds.size(); i++) {
            Rectangle candidate = bounds.get(i);
            long candidateDistance = distanceSquaredToRectangle(x, y, candidate);
            if (candidateDistance < nearestDistance) {
                nearest = candidate;
                nearestDistance = candidateDistance;
            }
        }

        int clampedX = Math.max(nearest.x, Math.min(x, nearest.x + nearest.width - 1));
        int clampedY = Math.max(nearest.y, Math.min(y, nearest.y + nearest.height - 1));
        return new Point(clampedX, clampedY);
    }

    public List<Rectangle> getScreenBounds() {
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        List<Rectangle> rectangles = new ArrayList<Rectangle>();
        for (GraphicsDevice device : devices) {
            GraphicsConfiguration configuration = device.getDefaultConfiguration();
            rectangles.add(configuration.getBounds());
        }
        return rectangles;
    }

    private long distanceSquaredToRectangle(int x, int y, Rectangle rectangle) {
        int nearestX = Math.max(rectangle.x, Math.min(x, rectangle.x + rectangle.width - 1));
        int nearestY = Math.max(rectangle.y, Math.min(y, rectangle.y + rectangle.height - 1));
        long deltaX = x - nearestX;
        long deltaY = y - nearestY;
        return deltaX * deltaX + deltaY * deltaY;
    }
}
