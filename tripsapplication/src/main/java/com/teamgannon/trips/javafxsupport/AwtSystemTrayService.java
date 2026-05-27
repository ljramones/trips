package com.teamgannon.trips.javafxsupport;

import com.teamgannon.trips.config.application.Localization;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.AWTException;
import java.awt.EventQueue;
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
        // SystemTray.remove(...) internally does:
        //   TrayIcon.removeNotify() → Window.dispose() → EventQueue.invokeAndWait(...)
        // which blocks the calling thread on the AWT EDT. When Spring's
        // destroy chain runs on the JavaFX Application Thread — which on
        // macOS IS the JVM main thread — and the AWT dispose has to hop
        // back to that same main thread, the result is a self-deadlock
        // that even Ctrl-C can't unstick. (See the symptom in commit history:
        // "Disposal was interrupted ... CTrayIcon.dispose ... invokeAndWait".)
        //
        // Fix: schedule the remove on the EDT and don't wait. Spring's
        // destroy chain continues, JVM exits, and the OS reaps the tray
        // icon at process termination either way.
        final TrayIcon toRemove = trayIcon;
        trayIcon = null;
        try {
            EventQueue.invokeLater(() -> {
                try {
                    SystemTray.getSystemTray().remove(toRemove);
                } catch (RuntimeException e) {
                    log.debug("Failed to remove system tray icon", e);
                }
            });
        } catch (RuntimeException e) {
            log.debug("Failed to schedule tray icon removal", e);
        }
    }
}
