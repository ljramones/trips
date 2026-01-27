package com.teamgannon.trips.javafxsupport;

import com.teamgannon.trips.controller.MainPane;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Primary Stage Initializer - Listens for StageReadyEvent and constructs the main application window.
 */
@Slf4j
@Component
public class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {

    private final FxWeaver fxWeaver;
    private final MainPane mainPane;

    @Value("${spring.application.ui.title}")
    private String title;

    @Value("${app.ui.window.width:1080}")
    private double windowWidth;

    @Value("${app.ui.window.height:680}")
    private double windowHeight;

    @Value("${app.ui.window.controlPaneHeight:80}")
    private double controlPaneHeight;

    public PrimaryStageInitializer(FxWeaver fxWeaver,
                                   MainPane mainPane) {
        this.fxWeaver = fxWeaver;
        this.mainPane = mainPane;
    }

    @Override
    public void onApplicationEvent(@NotNull StageReadyEvent event) {
        Stage primaryStage = event.getStage();

        try {
            Parent root = fxWeaver.loadView(MainPane.class);
            if (root == null) {
                throw new IllegalStateException("FxWeaver returned null for MainPane view");
            }

            Scene scene = new Scene(root, windowWidth, windowHeight + controlPaneHeight);
            primaryStage.setScene(scene);
            mainPane.setStage(primaryStage, windowWidth, windowHeight, controlPaneHeight);
            primaryStage.initStyle(StageStyle.DECORATED);
            primaryStage.setResizable(true);
            primaryStage.setTitle(title);

            // Load application icon with fallback
            Image appIcon = loadIconWithFallback("/images/tripsMac.ico");
            if (appIcon != null) {
                primaryStage.getIcons().add(appIcon);
            }

            primaryStage.show();
            log.info("Primary stage initialized successfully: {}x{}", windowWidth, windowHeight + controlPaneHeight);

        } catch (Exception e) {
            log.error("Failed to initialize primary stage", e);
            showErrorDialog(e);
            System.exit(1);
        }
    }

    /**
     * Load application icon with fallback handling if resource is missing.
     */
    private Image loadIconWithFallback(String resourcePath) {
        try {
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                return new Image(is);
            }
            log.warn("Application icon not found at: {}", resourcePath);
        } catch (Exception e) {
            log.warn("Failed to load application icon from: {}", resourcePath, e);
        }
        return null;
    }

    /**
     * Show error dialog when stage initialization fails.
     */
    private void showErrorDialog(Exception e) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Startup Error");
            alert.setHeaderText("Failed to start application");
            alert.setContentText("The application could not be initialized.\n\n" +
                    "Error: " + e.getMessage() + "\n\n" +
                    "Please check the log files for more details.");
            alert.showAndWait();
        } catch (Exception dialogException) {
            // If we can't show a dialog, just log it
            log.error("Failed to show error dialog", dialogException);
        }
    }
}
