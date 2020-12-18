package com.teamgannon.trips.progress;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.FileProcessResult;
import com.teamgannon.trips.file.csvin.RBCsvFile;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.animation.FadeTransition;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

@Slf4j
public class ProgressLoader {

    private static final int SPLASH_WIDTH = 676;
    private static final int SPLASH_HEIGHT = 227;

    private SplashScreen splashScreen;
    private final File file;
    private final Dataset dataset;
    private final DatabaseManagementService databaseManagementService;

    private @NotNull AtomicBoolean done = new AtomicBoolean(false);

    private @NotNull FileProcessResult processResult = new FileProcessResult();

    private @Nullable RBCsvFile rbCsvFile;

    public ProgressLoader(File file,
                          Dataset dataset,
                          DatabaseManagementService databaseManagementService) {
        this.file = file;
        this.dataset = dataset;
        this.databaseManagementService = databaseManagementService;
    }

    public void start(@NotNull Stage primaryStage) {
        splashScreen = new SplashScreen();
        final LoadGaiaDBTask loadGaiaDBTask = new LoadGaiaDBTask(null,null, null);

        showSplash(
                primaryStage,
                loadGaiaDBTask,
                () -> showMainStage(loadGaiaDBTask)
        );
        new Thread(loadGaiaDBTask).start();
    }

    private void showMainStage(@NotNull LoadGaiaDBTask loadGaiaDBTask) {
        try {
            DataSetDescriptor dataSetDescriptor = databaseManagementService.loadRBCSVStarSet(rbCsvFile);
            String data = String.format("%s records loaded from dataset %s, Use plot to see data.",
                    dataSetDescriptor.getNumberStars(),
                    dataSetDescriptor.getDataSetName());
            showInfoMessage("Load CSV Format", data);
            processResult.setSuccess(true);
            processResult.setDataSetDescriptor(dataSetDescriptor);
        } catch (Exception e) {
            showErrorAlert("Duplicate Dataset", "This dataset was already loaded in the system ");
            processResult.setSuccess(false);
        }
        rbCsvFile = loadGaiaDBTask.getFile();
        EndStatusPane pane = new EndStatusPane(" records loaded from GAIA file into DB");
        pane.show(loadGaiaDBTask.getFinalCount());
        done.set(true);
    }

    private void showSplash(
            final @NotNull Stage initStage,
            @NotNull Task<?> task,
            ProgressLoader.@NotNull InitCompletionHandler initCompletionHandler
    ) {

        splashScreen.set(task);

        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                splashScreen.finish();
                initStage.toFront();
                FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashScreen);
                fadeSplash.setFromValue(1.0);
                fadeSplash.setToValue(0.0);
                fadeSplash.setOnFinished(actionEvent -> initStage.hide());
                fadeSplash.play();

                initCompletionHandler.complete();
            } else if (newState == Worker.State.FAILED) {
                log.error("failed to load data");
            }
        });

        Scene splashScene = new Scene(splashScreen, Color.TRANSPARENT);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2.0);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2.0);
//        initStage.initStyle(StageStyle.TRANSPARENT);
        initStage.setAlwaysOnTop(true);
        initStage.show();
    }

    public @Nullable RBCsvFile getfile() {
        return rbCsvFile;
    }

    public interface InitCompletionHandler {
        void complete();
    }

    public @NotNull FileProcessResult getProcessResult() {
        return processResult;
    }

    public boolean isDone() {
        return done.get();
    }

}
