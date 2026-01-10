package com.teamgannon.trips.controller.shared;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.controller.ShowZoomWarning;
import com.teamgannon.trips.controller.SliderControlManager;
import com.teamgannon.trips.controller.UIElement;
import com.teamgannon.trips.events.UIStateChangeEvent;
import com.teamgannon.trips.graphics.PlotManager;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class SharedUIFunctions {

    private final TripsContext tripsContext;
    private final InterstellarSpacePane interstellarSpacePane;
    private final SharedUIState sharedUIState;
    private final ApplicationEventPublisher eventPublisher;
    private SplitPane mainSplitPane;
    private SliderControlManager sliderControlManager;
    private PlotManager plotManager;

    public SharedUIFunctions(TripsContext tripsContext,
                             InterstellarSpacePane interstellarSpacePane,
                             SharedUIState sharedUIState,
                             ApplicationEventPublisher eventPublisher) {
        this.tripsContext = tripsContext;
        this.interstellarSpacePane = interstellarSpacePane;
        this.sharedUIState = sharedUIState;
        this.eventPublisher = eventPublisher;
    }

    public void initialize(PlotManager plotManager, SplitPane mainSplitPane, SliderControlManager sliderControlManager) {
        this.plotManager = plotManager;
        this.mainSplitPane = mainSplitPane;
        this.sliderControlManager = sliderControlManager;
        this.sliderControlManager.initialize(mainSplitPane);

        SplitPane.setResizableWithParent(mainSplitPane.getItems().get(0), false);
        SplitPane.setResizableWithParent(mainSplitPane.getItems().get(1), false);

        mainSplitPane.getDividers().get(0).positionProperty().addListener((obs, oldVal, newVal) -> {
            log.info("Divider position changed from " + oldVal + " to " + newVal);
        });

        mainSplitPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            log.info("SplitPane width changed from " + oldVal + " to " + newVal);
        });

        setSplitPaneParentLayout(mainSplitPane);
    }

    private void setSplitPaneParentLayout(SplitPane splitPane) {
        Parent currentParent = splitPane.getParent();
        if (currentParent instanceof VBox) {
            VBox parentBox = (VBox) currentParent;
            VBox.setVgrow(splitPane, Priority.ALWAYS);
            parentBox.setFillWidth(true);
            return;
        }

        splitPane.parentProperty().addListener((obs, oldParent, newParent) -> {
            if (newParent instanceof VBox) {
                VBox parentBox = (VBox) newParent;
                VBox.setVgrow(splitPane, Priority.ALWAYS);
                parentBox.setFillWidth(true);
            }
        });
    }

    public void plotStars() {
        if (tripsContext.getSearchContext().getDataSetDescriptor() != null) {
            plotManager.showPlot(tripsContext.getSearchContext());
        } else {
            showErrorAlert("Plot stars", "There isn't a dataset selected to plot. Please select one.");
        }
    }

    public void toggleLabels() {
        boolean newState = !sharedUIState.isLabelsOn();
        sharedUIState.setLabelsOn(newState);
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayLabels(sharedUIState.isLabelsOn());
        eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.LABELS, newState));
    }

    public void togglePolities() {
        if (sharedUIState.isStarsOn()) {
            boolean newState = !sharedUIState.isPolities();
            sharedUIState.setPolities(!sharedUIState.isPolities());
            tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayPolities(sharedUIState.isPolities());
            interstellarSpacePane.togglePolities(sharedUIState.isPolities());
            eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.POLITIES, newState));
        }
    }

    public void toggleRoutes() {
        boolean newState = !sharedUIState.isRoutesOn();
        sharedUIState.setRoutesOn(newState);
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayRoutes(newState);
        interstellarSpacePane.toggleRoutes(newState);
        eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.ROUTES, newState));
    }

    public void toggleTransit() {
        boolean newState = !sharedUIState.isTransitsOn();
        sharedUIState.setTransitsOn(newState);
        interstellarSpacePane.toggleTransits(newState);
        eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.TRANSITS, newState));
    }

    public void toggleStars() {
        if (sharedUIState.isStarsOn()) {
            sharedUIState.setPolities(false);
            interstellarSpacePane.togglePolities(false);
            eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.POLITIES, false));
        }
        boolean newState = !sharedUIState.isStarsOn();
        sharedUIState.setStarsOn(newState);
        interstellarSpacePane.toggleStars(newState);
        eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.STARS, newState));
    }

    public void toggleGrid() {
        boolean newState = !sharedUIState.isGridOn();
        sharedUIState.setGridOn(newState);
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayGrid(newState);
        interstellarSpacePane.toggleGrid(newState);
        eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.GRID, newState));

        // Since toggling grid affects scale, we'll call toggleScale here
        toggleScale();
    }

    public void toggleGridExtensions() {
        boolean newState = !sharedUIState.isExtensionsOn();
        sharedUIState.setExtensionsOn(newState);
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayStems(newState);
        interstellarSpacePane.toggleExtensions(newState);
        eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.EXTENSIONS, newState));
    }

    public void toggleScale() {
        boolean newState = !sharedUIState.isScaleOn();
        sharedUIState.setScaleOn(newState);
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayLegend(newState);
        interstellarSpacePane.toggleScale(newState);
        eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.SCALE, newState));
    }

    public void toggleSidePane() {
        boolean newState = !sharedUIState.isSidePaneOn();
        sharedUIState.setSidePaneOn(newState);
        log.info("side pane is set:{}", newState);
        applySidePaneState(newState);
        eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.SIDE_PANE, newState));
    }

    public void applySidePaneState(boolean sidePanelOn) {
        log.info("Applying side pane state: " + sidePanelOn);

        Platform.runLater(() -> {
            if (mainSplitPane == null) {
                log.error("mainSplitPane is null");
                return;
            }

            log.info("SplitPane width inside runLater: " + mainSplitPane.getWidth());
            double newPosition = sidePanelOn ? 0.7 : 1.0;
            log.info("Attempting to set divider position to: " + newPosition);

            sliderControlManager.removeSliderChangeListener();
            mainSplitPane.setDividerPosition(0, newPosition);
            updatePaneWidths(sidePanelOn, newPosition);
            interstellarSpacePane.shiftDisplayLeft(sidePanelOn);
            sliderControlManager.addSliderChangeListener();

            PauseTransition pause = new PauseTransition(Duration.millis(100));
            pause.setOnFinished(event -> checkAndCorrectDividerPosition(sidePanelOn, newPosition));
            pause.play();
        });
    }

    private void updatePaneWidths(boolean sidePanelOn, double newPosition) {
        double leftWidth = mainSplitPane.getWidth() * newPosition;
        double rightWidth = mainSplitPane.getWidth() - leftWidth;

        Node leftPane = mainSplitPane.getItems().get(0);
        Node rightPane = mainSplitPane.getItems().get(1);

        if (leftPane instanceof Region) {
            ((Region) leftPane).setPrefWidth(leftWidth);
        }
        if (rightPane instanceof Region) {
            ((Region) rightPane).setPrefWidth(rightWidth);
        }

        if (rightPane instanceof Region rightRegion) {
            rightRegion.setMinWidth(sidePanelOn ? 100 : 0);
            rightRegion.setMaxWidth(sidePanelOn ? Region.USE_COMPUTED_SIZE : 0);
            log.info("Right pane min width: " + rightRegion.getMinWidth());
            log.info("Right pane max width: " + rightRegion.getMaxWidth());
        }

        mainSplitPane.layout();
        mainSplitPane.requestLayout();  // Request layout update

        log.info("Actual divider position after setting: " + mainSplitPane.getDividerPositions()[0]);
        log.info("SplitPane width after layout: " + mainSplitPane.getWidth());
        log.info("Left pane width: " + leftPane.getBoundsInParent().getWidth());
        log.info("Right pane width: " + rightPane.getBoundsInParent().getWidth());
    }

    private void checkAndCorrectDividerPosition(boolean sidePanelOn, double newPosition) {
        double actualPosition = mainSplitPane.getDividerPositions()[0];
        if (sidePanelOn && actualPosition != newPosition) {
            log.info("Correcting divider position to " + newPosition);
            mainSplitPane.setDividerPosition(0, newPosition);
        } else if (!sidePanelOn && actualPosition != 1.0) {
            log.info("Correcting divider position to 1.0");
            mainSplitPane.setDividerPosition(0, 1.0);
        }

        mainSplitPane.layout();
        mainSplitPane.requestLayout();  // Request layout update

        log.info("Final divider position: " + mainSplitPane.getDividerPositions()[0]);
    }

    public void zoomOut() {
        if (showZoomWarning()) {
            interstellarSpacePane.zoomOut();
        }
    }

    public void zoomIn() {
        if (showZoomWarning()) {
            interstellarSpacePane.zoomIn();
        }
    }

    private boolean showZoomWarning() {
        if (tripsContext.isShowWarningOnZoom()) {
            ShowZoomWarning showZoomWarning = new ShowZoomWarning();
            Optional<Boolean> optionalBoolean = showZoomWarning.showAndWait();
            if (optionalBoolean.isPresent()) {
                Boolean dontShowAgain = optionalBoolean.get();
                if (dontShowAgain) {
                    tripsContext.setShowWarningOnZoom(false);
                }
            }
            return optionalBoolean.orElse(false);
        }
        return true;
    }

    public static void showErrorAlert(String title, String error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(error);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        alert.showAndWait();
        log.error(error);
    }

    public void updateSidePaneOnResize() {
        boolean sidePanelOn = sharedUIState.isSidePaneOn();
        double newPosition = sidePanelOn ? 0.7 : 1.0;
        log.info("Updating side pane on resize, sidePanelOn: " + sidePanelOn);

        Platform.runLater(() -> {
            if (mainSplitPane == null) {
                log.error("mainSplitPane is null");
                return;
            }

            sliderControlManager.removeSliderChangeListener();
            mainSplitPane.setDividerPosition(0, newPosition);
            updatePaneWidths(sidePanelOn, newPosition);
            sliderControlManager.addSliderChangeListener();
        });
    }

}
