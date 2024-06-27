package com.teamgannon.trips.javafxsupport;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.controller.MainPane;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.rgielen.fxweaver.core.FxWeaver;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Primary Stage Initializer
 */
@Component
public class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {

    private final static int CONTROL_PANE_SIZE = 80;

    private final FxWeaver fxWeaver;
    private final MainPane mainPane;

    @Value("${spring.application.ui.title}")
    private String title;

    @Autowired
    public PrimaryStageInitializer(FxWeaver fxWeaver,
                                   MainPane mainPane) {
        this.fxWeaver = fxWeaver;
        this.mainPane = mainPane;
    }

    @Override
    public void onApplicationEvent(@NotNull StageReadyEvent event) {
        Stage primaryStage = event.getStage();
        Parent root = fxWeaver.loadView(MainPane.class);

        Scene scene = new Scene(root, Universe.boxWidth, Universe.boxHeight + CONTROL_PANE_SIZE);
        primaryStage.setScene(scene);
        mainPane.setStage(primaryStage, Universe.boxWidth, Universe.boxHeight, CONTROL_PANE_SIZE);
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setResizable(true);
        primaryStage.setTitle(title);

        // load application icon
        final Image appIcon = new Image("/images/tripsMac.ico");
        primaryStage.getIcons().add(appIcon);

        primaryStage.show();
    }

}
