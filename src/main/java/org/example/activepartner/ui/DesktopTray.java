package org.example.activepartner.ui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javafx.application.Platform;

import org.example.activepartner.i18n.I18nService;

final class DesktopTray implements AutoCloseable {
    private final I18nService i18n;
    private final Runnable show;
    private final Runnable stop;
    private final Runnable exit;
    private TrayIcon icon;

    DesktopTray(I18nService i18n, Runnable show, Runnable stop, Runnable exit) {
        this.i18n = i18n;
        this.show = show;
        this.stop = stop;
        this.exit = exit;
    }

    static boolean isSupported() {
        return SystemTray.isSupported();
    }

    boolean isOpen() {
        return icon != null;
    }

    void open() throws AWTException {
        if (isOpen()) {
            return;
        }

        PopupMenu menu = new PopupMenu();
        menu.add(menuItem("tray.show", show));
        menu.add(menuItem("tray.stop", stop));
        menu.add(menuItem("tray.exit", exit));

        TrayIcon newIcon = new TrayIcon(buildTrayImage(), i18n.text("app.title"), menu);
        newIcon.setImageAutoSize(true);
        newIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 1) {
                    Platform.runLater(show);
                }
            }
        });
        SystemTray.getSystemTray().add(newIcon);
        icon = newIcon;
    }

    void showRunningMessage() {
        icon.displayMessage(i18n.text("app.title"), i18n.text("tray.running"), TrayIcon.MessageType.INFO);
    }

    @Override
    public void close() {
        if (isOpen()) {
            SystemTray.getSystemTray().remove(icon);
            icon = null;
        }
    }

    private MenuItem menuItem(String key, Runnable action) {
        MenuItem item = new MenuItem(i18n.text(key));
        item.addActionListener(event -> Platform.runLater(action));
        return item;
    }

    private static Image buildTrayImage() {
        Image resourceIcon = IconResources.loadAwtIcon();
        if (resourceIcon != null) {
            return resourceIcon;
        }
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(31, 79, 177));
        graphics.fillRoundRect(0, 0, 16, 16, 4, 4);
        graphics.setColor(Color.WHITE);
        graphics.drawString("A", 5, 12);
        graphics.dispose();
        return image;
    }
}
