package com.teamgannon.trips.transits;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

/**
 * Dialog that shows progress during transit calculation.
 * <p>
 * This dialog displays:
 * <ul>
 *   <li>A progress bar showing calculation progress</li>
 *   <li>Status text describing current operation</li>
 *   <li>Cancel button to abort calculation</li>
 *   <li>Finish button to close when complete</li>
 * </ul>
 * <p>
 * The calculation runs in the background using {@link TransitCalculationService},
 * allowing the UI to remain responsive.
 */
@Slf4j
public class TransitCalculationDialog extends Dialog<TransitCalculationResult>
        implements TransitCalculationService.TransitCalculationComplete {

    private static final Font STATUS_FONT = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 12);

    private final ProgressBar progressBar = new ProgressBar();
    private final Label statusLabel = new Label("Initializing...");

    private final Button finishButton = new Button("Close");
    private final Button cancelButton = new Button("Cancel");

    private final TransitCalculationService calculationService;
    private TransitCalculationResult calculationResult;

    /**
     * Creates and starts the transit calculation dialog.
     *
     * @param transitDefinitions the transit band definitions
     * @param starsInView        the stars to calculate transits between
     * @param calculator         the calculator to use
     * @param eventPublisher     Spring event publisher for status updates
     * @param calculationService the calculation service (Spring managed)
     */
    public TransitCalculationDialog(@NotNull TransitDefinitions transitDefinitions,
                                     @NotNull List<StarDisplayRecord> starsInView,
                                     @NotNull ITransitDistanceCalculator calculator,
                                     @NotNull ApplicationEventPublisher eventPublisher,
                                     @NotNull TransitCalculationService calculationService) {
        this.calculationService = calculationService;

        setTitle("Calculating Transits");
        setHeaderText(String.format("Finding transits between %,d stars", starsInView.size()));

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setMinWidth(500);

        // Status section
        GridPane statusGrid = new GridPane();
        statusGrid.setHgap(10);
        statusGrid.setVgap(10);

        Label statusTitleLabel = new Label("Status:");
        statusTitleLabel.setFont(STATUS_FONT);
        statusGrid.add(statusTitleLabel, 0, 0);
        statusGrid.add(statusLabel, 1, 0);

        content.getChildren().add(statusGrid);

        // Progress bar
        progressBar.setProgress(0);
        progressBar.setPrefWidth(450);
        progressBar.setMinHeight(25);
        content.getChildren().add(progressBar);

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        finishButton.setDisable(true);
        finishButton.setPrefWidth(100);
        finishButton.setOnAction(this::finishClicked);

        cancelButton.setPrefWidth(100);
        cancelButton.setOnAction(this::cancelClicked);

        buttonBox.getChildren().addAll(finishButton, cancelButton);
        content.getChildren().add(buttonBox);

        getDialogPane().setContent(content);

        // Prevent closing with X button while running
        getDialogPane().getScene().getWindow().setOnCloseRequest(event -> {
            if (calculationService.isRunning()) {
                event.consume(); // Prevent close
            }
        });

        // Start the calculation
        startCalculation(transitDefinitions, starsInView, calculator, eventPublisher);
    }

    private void startCalculation(TransitDefinitions transitDefinitions,
                                   List<StarDisplayRecord> starsInView,
                                   ITransitDistanceCalculator calculator,
                                   ApplicationEventPublisher eventPublisher) {
        // Reset service for reuse
        calculationService.reset();

        // Configure the service
        calculationService.configureCalculation(
                transitDefinitions,
                starsInView,
                calculator,
                eventPublisher,
                this,
                statusLabel,
                progressBar,
                cancelButton
        );

        // Start calculation
        log.info("Starting transit calculation for {} stars", starsInView.size());
        calculationService.start();
    }

    private void finishClicked(ActionEvent event) {
        log.debug("Finish clicked, result: {}", calculationResult != null ? "available" : "null");
        setResult(calculationResult);
    }

    private void cancelClicked(ActionEvent event) {
        log.info("Cancel clicked");
        if (calculationService.isRunning()) {
            calculationService.cancelCalculation();
        } else {
            // If not running, just close
            setResult(TransitCalculationResult.builder()
                    .routesByBand(java.util.Map.of())
                    .transitDefinitions(new TransitDefinitions())
                    .totalRoutes(0)
                    .calculationTimeMs(0)
                    .cancelled(true)
                    .build());
        }
    }

    @Override
    public void onComplete(@NotNull TransitCalculationResult result) {
        this.calculationResult = result;

        // Update UI on completion
        finishButton.setDisable(false);
        cancelButton.setDisable(true);

        if (result.isCancelled()) {
            statusLabel.setText("Calculation cancelled");
        } else if (result.getErrorMessage() != null) {
            statusLabel.setText("Error: " + result.getErrorMessage());
        } else {
            statusLabel.setText(String.format("Complete: %,d transits found in %d ms",
                    result.getTotalRoutes(), result.getCalculationTimeMs()));
        }

        log.info("Transit calculation complete: {} routes, cancelled={}, error={}",
                result.getTotalRoutes(), result.isCancelled(), result.getErrorMessage());
    }
}
