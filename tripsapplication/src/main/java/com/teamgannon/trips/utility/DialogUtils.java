package com.teamgannon.trips.utility;

import javafx.scene.control.Dialog;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.net.URL;


public final class DialogUtils {

    private DialogUtils() {
    }

    public static void bindCloseHandler(Dialog<?> dialog, javafx.event.EventHandler<WindowEvent> handler) {
        dialog.getDialogPane().sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                return;
            }
            Window window = newScene.getWindow();
            if (window != null) {
                window.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, handler);
            } else {
                newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, handler);
                    }
                });
            }
        });
    }

    public static void applyTheme(Dialog<?> dialog) {
        URL themeCss = DialogUtils.class.getResource("/com/teamgannon/trips/theme.css");
        if (themeCss == null) {
            return;
        }
        String stylesheet = themeCss.toExternalForm();
        if (!dialog.getDialogPane().getStylesheets().contains(stylesheet)) {
            dialog.getDialogPane().getStylesheets().add(stylesheet);
        }
    }
}
