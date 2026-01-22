package com.teamgannon.trips.routing.automation;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.RouteFindingService;
import com.teamgannon.trips.routing.dialogs.DisplayAutoRoutesDialog;
import com.teamgannon.trips.routing.dialogs.RouteFinderDialogInView;
import com.teamgannon.trips.routing.model.RouteFindingOptions;
import com.teamgannon.trips.routing.model.RouteFindingResult;
import com.teamgannon.trips.routing.model.RoutingMetric;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Route finder for stars currently in view.
 * <p>
 * This component handles the UI workflow for finding routes:
 * <ol>
 *   <li>Shows dialog to collect route parameters</li>
 *   <li>Executes route finding asynchronously with progress feedback</li>
 *   <li>Displays results and plots selected routes</li>
 * </ol>
 * <p>
 * Route finding is performed asynchronously to prevent UI freezing, especially
 * when the number of stars approaches the graph threshold (1500).
 */
@Slf4j
@Component
public class RouteFinderInView {

    private final InterstellarSpacePane interstellarSpacePane;
    private final RouteFindingService routeFindingService;

    public RouteFinderInView(InterstellarSpacePane interstellarSpacePane,
                             RouteFindingService routeFindingService) {
        this.interstellarSpacePane = interstellarSpacePane;
        this.routeFindingService = routeFindingService;
    }

    /**
     * Start the route finding workflow.
     *
     * @param currentDataSet the current dataset
     */
    public void startRouteLocation(DataSetDescriptor currentDataSet) {
        RouteFinderDialogInView routeFinderDialogInView = new RouteFinderDialogInView(
                interstellarSpacePane.getCurrentStarsInView());
        Stage theStage = (Stage) routeFinderDialogInView.getDialogPane().getScene().getWindow();
        theStage.setAlwaysOnTop(true);
        theStage.toFront();

        processRouteRequest(currentDataSet, theStage, routeFinderDialogInView);
    }

    /**
     * Process the route request from the dialog.
     */
    public void processRouteRequest(DataSetDescriptor currentDataSet,
                                    Stage theStage,
                                    @NotNull RouteFinderDialogInView routeFinderDialogInView) {

        Optional<RouteFindingOptions> optionalOptions = routeFinderDialogInView.showAndWait();
        if (optionalOptions.isEmpty()) {
            return;
        }

        RouteFindingOptions options = optionalOptions.get();
        if (!options.isSelected()) {
            return;
        }

        findAndDisplayRoutes(currentDataSet, theStage, routeFinderDialogInView, options);
    }

    /**
     * Find routes asynchronously and display results.
     */
    private void findAndDisplayRoutes(DataSetDescriptor currentDataSet,
                                       Stage theStage,
                                       RouteFinderDialogInView routeFinderDialogInView,
                                       RouteFindingOptions options) {
        log.info("Finding route between {} and {}", options.getOriginStarName(), options.getDestinationStarName());

        List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();

        // Create async service
        InViewRouteFinderService service = new InViewRouteFinderService(routeFindingService);
        service.configure(options, starsInView, currentDataSet);

        // Create progress dialog
        Stage progressStage = createProgressDialog(theStage, service, options);

        // Set up success handler
        service.setOnSucceeded(event -> {
            progressStage.close();
            RouteFindingResult result = service.getValue();
            handleRouteResult(currentDataSet, theStage, routeFinderDialogInView, result);
        });

        // Set up failure handler
        service.setOnFailed(event -> {
            progressStage.close();
            Throwable exception = service.getException();
            log.error("Route finding failed: {}", exception.getMessage(), exception);
            showErrorAlert("Route Finder", "Route finding failed: " + exception.getMessage());
            processRouteRequest(currentDataSet, theStage, routeFinderDialogInView);
        });

        // Set up cancellation handler
        service.setOnCancelled(event -> {
            progressStage.close();
            log.info("Route finding was cancelled");
        });

        // Start service and show progress
        service.start();
        progressStage.show();
    }

    /**
     * Creates a progress dialog for route finding.
     */
    private Stage createProgressDialog(Stage owner, InViewRouteFinderService service, RouteFindingOptions options) {
        Stage progressStage = new Stage();
        progressStage.initStyle(StageStyle.UTILITY);
        progressStage.initModality(Modality.APPLICATION_MODAL);
        progressStage.initOwner(owner);
        progressStage.setTitle("Finding Routes");
        progressStage.setResizable(false);

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label(String.format("Finding routes: %s → %s",
                options.getOriginStarName(), options.getDestinationStarName()));
        titleLabel.setStyle("-fx-font-weight: bold;");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(50, 50);

        Label statusLabel = new Label("Initializing...");
        statusLabel.textProperty().bind(service.messageProperty());

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            service.cancel();
            progressStage.close();
        });

        content.getChildren().addAll(titleLabel, progressIndicator, statusLabel, cancelButton);

        Scene scene = new Scene(content, 300, 180);
        progressStage.setScene(scene);
        progressStage.setAlwaysOnTop(true);

        return progressStage;
    }

    /**
     * Handles the route finding result.
     */
    private void handleRouteResult(DataSetDescriptor currentDataSet,
                                    Stage theStage,
                                    RouteFinderDialogInView routeFinderDialogInView,
                                    RouteFindingResult result) {
        if (!result.isSuccess()) {
            showErrorAlert("Route Finder", result.getErrorMessage());
            processRouteRequest(currentDataSet, theStage, routeFinderDialogInView);
            return;
        }

        if (!result.hasRoutes()) {
            showErrorAlert("Route Finder", "No routes found with the given parameters.");
            processRouteRequest(currentDataSet, theStage, routeFinderDialogInView);
            return;
        }

        // Display results for user selection
        displayFoundRoutes(currentDataSet, result);
    }

    /**
     * Display the found routes for user selection.
     */
    private void displayFoundRoutes(DataSetDescriptor currentDataSet, @NotNull RouteFindingResult result) {
        DisplayAutoRoutesDialog displayAutoRoutesDialog = new DisplayAutoRoutesDialog(result.getRoutes());
        Stage dialogStage = (Stage) displayAutoRoutesDialog.getDialogPane().getScene().getWindow();
        dialogStage.setAlwaysOnTop(true);
        dialogStage.toFront();

        Optional<List<RoutingMetric>> optionalRoutingMetrics = displayAutoRoutesDialog.showAndWait();
        if (optionalRoutingMetrics.isPresent()) {
            List<RoutingMetric> selectedRoutingMetrics = optionalRoutingMetrics.get();
            if (!selectedRoutingMetrics.isEmpty()) {
                log.info("Plotting {} selected routes", selectedRoutingMetrics.size());
                interstellarSpacePane.plotRouteDescriptors(currentDataSet, selectedRoutingMetrics);
            }
        }
    }
}
