package com.teamgannon.trips.transits;

import com.teamgannon.trips.events.StatusUpdateEvent;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JavaFX Service for managing transit calculation with progress reporting.
 * <p>
 * This service wraps {@link TransitCalculationTask} and provides:
 * <ul>
 *   <li>Progress binding to UI controls</li>
 *   <li>Cancellation support</li>
 *   <li>Completion callbacks</li>
 *   <li>Integration with Spring event publishing</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 * transitCalculationService.startCalculation(
 *     definitions, stars, calculator, publisher,
 *     this::onComplete, progressLabel, progressBar, cancelButton
 * );
 * service.start();
 * </pre>
 */
@Slf4j
@Component
public class TransitCalculationService extends Service<TransitCalculationResult> {

    private TransitDefinitions transitDefinitions;
    private List<StarDisplayRecord> starsInView;
    private ITransitDistanceCalculator calculator;
    private ApplicationEventPublisher eventPublisher;
    private TransitCalculationComplete completionCallback;

    private Label progressText;
    private ProgressBar progressBar;

    /**
     * Callback interface for calculation completion.
     */
    @FunctionalInterface
    public interface TransitCalculationComplete {
        /**
         * Called when calculation completes.
         *
         * @param result the calculation result
         */
        void onComplete(@NotNull TransitCalculationResult result);
    }

    /**
     * Configures and prepares the service for execution.
     *
     * @param transitDefinitions the transit band definitions
     * @param starsInView        the stars to calculate transits between
     * @param calculator         the calculator to use
     * @param eventPublisher     Spring event publisher for status updates
     * @param completionCallback callback to invoke when complete
     * @param progressText       label to bind progress messages to
     * @param progressBar        progress bar to bind progress to
     * @param cancelButton       button to enable/disable based on state (optional)
     */
    public void configureCalculation(@NotNull TransitDefinitions transitDefinitions,
                                      @NotNull List<StarDisplayRecord> starsInView,
                                      @NotNull ITransitDistanceCalculator calculator,
                                      @NotNull ApplicationEventPublisher eventPublisher,
                                      @NotNull TransitCalculationComplete completionCallback,
                                      @NotNull Label progressText,
                                      @NotNull ProgressBar progressBar,
                                      Button cancelButton) {
        this.transitDefinitions = transitDefinitions;
        this.starsInView = starsInView;
        this.calculator = calculator;
        this.eventPublisher = eventPublisher;
        this.completionCallback = completionCallback;
        this.progressText = progressText;
        this.progressBar = progressBar;

        // Bind UI controls
        progressText.textProperty().bind(this.messageProperty());
        progressBar.progressProperty().bind(this.progressProperty());

        if (cancelButton != null) {
            cancelButton.setDisable(false);
        }

        log.debug("Transit calculation service configured for {} stars", starsInView.size());
    }

    /**
     * Cancel the ongoing calculation.
     *
     * @return true if cancellation was successful
     */
    public boolean cancelCalculation() {
        log.info("Cancelling transit calculation");
        return this.cancel();
    }

    @Override
    protected Task<TransitCalculationResult> createTask() {
        return new TransitCalculationTask(transitDefinitions, starsInView, calculator);
    }

    @Override
    protected void succeeded() {
        TransitCalculationResult result = getValue();
        log.info("Transit calculation completed: {} routes found in {} ms",
                result.getTotalRoutes(), result.getCalculationTimeMs());

        eventPublisher.publishEvent(new StatusUpdateEvent(this,
                String.format("Transit calculation complete: %,d routes found", result.getTotalRoutes())));

        unbindProgressControls();
        completionCallback.onComplete(result);
    }

    @Override
    protected void failed() {
        Throwable exception = getException();
        String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
        log.error("Transit calculation failed: {}", errorMessage, exception);

        eventPublisher.publishEvent(new StatusUpdateEvent(this,
                "Transit calculation failed: " + errorMessage));

        unbindProgressControls();

        // Create a failure result
        TransitCalculationResult result = TransitCalculationResult.builder()
                .routesByBand(java.util.Map.of())
                .transitDefinitions(transitDefinitions)
                .totalRoutes(0)
                .calculationTimeMs(0)
                .cancelled(false)
                .errorMessage(errorMessage)
                .build();

        completionCallback.onComplete(result);
    }

    @Override
    protected void cancelled() {
        log.info("Transit calculation was cancelled");

        eventPublisher.publishEvent(new StatusUpdateEvent(this, "Transit calculation cancelled"));

        unbindProgressControls();

        // Create a cancelled result
        TransitCalculationResult result = TransitCalculationResult.builder()
                .routesByBand(java.util.Map.of())
                .transitDefinitions(transitDefinitions)
                .totalRoutes(0)
                .calculationTimeMs(0)
                .cancelled(true)
                .errorMessage(null)
                .build();

        completionCallback.onComplete(result);
    }

    private void unbindProgressControls() {
        if (progressText != null) {
            progressText.textProperty().unbind();
        }
        if (progressBar != null) {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(1.0);
        }
    }
}
