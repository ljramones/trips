package com.teamgannon.trips.javafxsupport;

import com.teamgannon.trips.controller.MainPane;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Primary Stage Intiializer
 */
@Component
public class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {

    private final FxWeaver fxWeaver;
    private MainPane mainPane;

    @Value("${spring.application.ui.title}")
    private String title;

    @Autowired
    public PrimaryStageInitializer(FxWeaver fxWeaver,
                                   MainPane mainPane) {
        this.fxWeaver = fxWeaver;
        this.mainPane = mainPane;
    }



    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.stage;
        Parent root = fxWeaver.loadView(MainPane.class);
        Scene scene = new Scene(root, 1101, 816);
        stage.setScene(scene);
        stage.initStyle(StageStyle.DECORATED);
        stage.setResizable(true);
        stage.setTitle(title);
        mainPane.setStage(stage);
        stage.show();
    }

}
