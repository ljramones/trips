package com.teamgannon.trips.javafxsupport;

import com.teamgannon.trips.config.application.Localization;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.nio.file.Path;

@Slf4j
@Component
public class AwtSystemTrayService {

    private final Localization localization;
    private TrayIcon trayIcon;

    public AwtSystemTrayService(Localization localization) {
        this.localization = localization;
    }

    public void installTrayIcon() {
        if (trayIcon != null) {
            return;
        }
        if (GraphicsEnvironment.isHeadless() || !SystemTray.isSupported()) {
            log.debug("System tray is not supported in this environment");
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();
            Path iconPath = Path.of(localization.getProgramdata()).resolve("tripsicon.png");
            Image image = Toolkit.getDefaultToolkit().getImage(iconPath.toString());
            PopupMenu popup = new PopupMenu();
            MenuItem defaultItem = new MenuItem("TRIPS");
            defaultItem.addActionListener(event -> log.debug("Tray menu selected"));
            popup.add(defaultItem);

            TrayIcon icon = new TrayIcon(image, "TRIPS", popup);
            icon.setImageAutoSize(true);
            icon.addActionListener(event -> log.debug("Tray icon selected"));
            tray.add(icon);
            trayIcon = icon;
        } catch (AWTException | RuntimeException e) {
            log.warn("Failed to add system tray icon", e);
        }
    }

    @PreDestroy
    public void removeTrayIcon() {
        if (trayIcon == null || GraphicsEnvironment.isHeadless() || !SystemTray.isSupported()) {
            return;
        }
        try {
            SystemTray.getSystemTray().remove(trayIcon);
        } catch (RuntimeException e) {
            log.debug("Failed to remove system tray icon", e);
        } finally {
            trayIcon = null;
        }
    }
}
