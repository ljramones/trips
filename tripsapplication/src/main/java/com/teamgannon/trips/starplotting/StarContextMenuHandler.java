package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.events.*;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.routing.RouteManager;
import com.teamgannon.trips.routing.RouteFindingService;
import com.teamgannon.trips.routing.dialogs.ContextAutomatedRoutingDialog;
import com.teamgannon.trips.routing.dialogs.ContextManualRoutingDialog;
import com.teamgannon.trips.routing.model.RoutingType;
import com.teamgannon.trips.screenobjects.StarEditDialog;
import com.teamgannon.trips.screenobjects.StarEditStatus;
import com.teamgannon.trips.service.SolarSystemService;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.solarsystem.PlanetDialog;
import com.teamgannon.trips.solarsystem.SolarSystemGenOptions;
import com.teamgannon.trips.solarsystem.SolarSystemGenerationDialog;
import com.teamgannon.trips.solarsystem.SolarSystemReport;
import com.teamgannon.trips.solarsystem.SolarSystemSaveResult;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.stage.Modality;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Handles context menu actions for stars in the interstellar view.
 * <p>
 * This class manages all actions that can be performed from the star context menu:
 * <ul>
 *   <li>Highlighting stars</li>
 *   <li>Displaying/editing star properties</li>
 *   <li>Deleting stars</li>
 *   <li>Recentering view on a star</li>
 *   <li>Starting manual/automated routing</li>
 *   <li>Generating solar systems</li>
 *   <li>Jumping into solar system view</li>
 * </ul>
 */
@Slf4j
@Component
public class StarContextMenuHandler {

    private final TripsContext tripsContext;
    private final StarService starService;
    private final SolarSystemService solarSystemService;
    private final ApplicationEventPublisher eventPublisher;
    private final RouteManager routeManager;
    private final RouteFindingService routeFindingService;

    @Getter
    @Setter
    private StarDisplayPreferences starDisplayPreferences;

    @Getter
    @Setter
    private ContextAutomatedRoutingDialog automatedRoutingDialog;

    @Getter
    @Setter
    private ContextManualRoutingDialog manualRoutingDialog;

    /**
     * Supplier for getting current stars in view (set by StarPlotManager).
     */
    @Setter
    private Supplier<List<StarDisplayRecord>> starsInViewSupplier;

    public StarContextMenuHandler(TripsContext tripsContext,
                                   StarService starService,
                                   SolarSystemService solarSystemService,
                                   ApplicationEventPublisher eventPublisher,
                                   RouteManager routeManager,
                                   RouteFindingService routeFindingService) {
        this.tripsContext = tripsContext;
        this.starService = starService;
        this.solarSystemService = solarSystemService;
        this.eventPublisher = eventPublisher;
        this.routeManager = routeManager;
        this.routeFindingService = routeFindingService;
    }

    /**
     * Creates a context menu for a star node.
     *
     * @param star   the star node
     * @param record the star display record
     * @param starPlotManager reference for routing dialog callbacks
     * @return the created context menu
     */
    public @NotNull ContextMenu createContextMenu(@NotNull Node star,
                                                   @NotNull StarDisplayRecord record,
                                                   @NotNull StarPlotManager starPlotManager) {
        String polity = record.getPolity();
        if (polity.equals("NA")) {
            polity = "Non-aligned";
        }
        String title = record.getStarName() + " (" + polity + ")";

        return new StarContextMenuBuilder(star, record)
                .withTitle(title)
                .withHighlightAction(r ->
                        eventPublisher.publishEvent(new HighlightStarEvent(this, r.getRecordId())))
                .withPropertiesAction(r -> {
                    StarObject starObject = starService.getStar(r.getRecordId());
                    displayProperties(starObject);
                })
                .withRecenterAction(r -> {
                    if (r != null) {
                        eventPublisher.publishEvent(new RecenterStarEvent(this, r));
                    } else {
                        showErrorAlert("Recenter on star", "The star you selected was null!");
                    }
                })
                .withEditAction(r -> {
                    StarDisplayRecord editRecord = editProperties(r);
                    if (editRecord != null) {
                        star.setUserData(editRecord);
                    }
                })
                .withDeleteAction(this::removeNode)
                .withRoutingHeader()
                .withAutomatedRoutingAction(r -> generateAutomatedRoute(r, starPlotManager))
                .withManualRoutingAction(this::generateManualRoute)
                .withDistanceReportAction(r ->
                        eventPublisher.publishEvent(new DistanceReportEvent(this, r)))
                .withEnterSystemAction(this::jumpToSystem)
                .withGenerateSolarSystemAction(this::generateSolarSystem)
                .build();
    }

    /**
     * Display properties for a star in the side panel.
     *
     * @param starObject the star to display
     */
    public void displayProperties(@NotNull StarObject starObject) {
        log.info("Showing properties in side panes for: {}", starObject.getDisplayName());
        eventPublisher.publishEvent(new DisplayStarEvent(this, starObject));
    }

    /**
     * Edit a star's properties via dialog.
     *
     * @param starDisplayRecord the star to edit
     * @return the updated star record, or original if no changes
     */
    public @Nullable StarDisplayRecord editProperties(@NotNull StarDisplayRecord starDisplayRecord) {
        StarObject starObject = starService.getStar(starDisplayRecord.getRecordId());
        StarEditDialog starEditDialog = new StarEditDialog(starObject);
        Optional<StarEditStatus> optionalStarDisplayRecord = starEditDialog.showAndWait();

        if (optionalStarDisplayRecord.isPresent()) {
            StarEditStatus status = optionalStarDisplayRecord.get();
            if (status.isChanged()) {
                StarObject record = status.getRecord();
                StarDisplayRecord record1 = StarDisplayRecord.fromStarObject(record, starDisplayPreferences);
                if (record1 != null) {
                    record1.setCoordinates(starDisplayRecord.getCoordinates());
                    log.info("Changed value: {}", record);
                    starService.updateStar(record);
                } else {
                    log.error("Conversion of {} to star display record returned null", record);
                }
                return record1;
            } else {
                log.warn("No changes made");
                return starDisplayRecord;
            }
        }
        log.info("Editing properties for: {}", starDisplayRecord.getStarName());
        return starDisplayRecord;
    }

    /**
     * Remove a star from the database.
     *
     * @param starDisplayRecord the star to remove
     */
    public void removeNode(@NotNull StarDisplayRecord starDisplayRecord) {
        log.info("Removing star: {}", starDisplayRecord.getStarName());
        starService.removeStar(starDisplayRecord.getRecordId());
    }

    /**
     * Jump to the solar system view for a star.
     *
     * @param starDisplayRecord the star to enter
     */
    public void jumpToSystem(StarDisplayRecord starDisplayRecord) {
        eventPublisher.publishEvent(new ContextSelectorEvent(
                this,
                ContextSelectionType.SOLARSYSTEM,
                starDisplayRecord,
                (java.util.Map<String, String>) null));
    }

    /**
     * Start automated route finding from a star.
     *
     * @param starDescriptor the starting star
     * @param starPlotManager reference for callbacks
     */
    public void generateAutomatedRoute(StarDisplayRecord starDescriptor,
                                        StarPlotManager starPlotManager) {
        log.info("Generate automated route from: {}", starDescriptor.getStarName());

        List<StarDisplayRecord> starsInView = starsInViewSupplier != null
                ? starsInViewSupplier.get()
                : List.of();

        automatedRoutingDialog = new ContextAutomatedRoutingDialog(
                starPlotManager, routeManager, routeFindingService,
                getCurrentDataSet(), starDescriptor, starsInView);

        automatedRoutingDialog.initModality(Modality.NONE);
        automatedRoutingDialog.show();
        routeManager.setRoutingType(RoutingType.AUTOMATIC);
    }

    /**
     * Start manual route creation from a star.
     *
     * @param starDescriptor the starting star
     */
    public void generateManualRoute(StarDisplayRecord starDescriptor) {
        log.info("Generate manual route from: {}", starDescriptor.getStarName());

        manualRoutingDialog = new ContextManualRoutingDialog(
                routeManager,
                getCurrentDataSet(),
                starDescriptor
        );
        manualRoutingDialog.initModality(Modality.NONE);
        manualRoutingDialog.show();
        routeManager.setManualRoutingActive(true);
        routeManager.setRoutingType(RoutingType.MANUAL);
    }

    /**
     * Generate a solar system for a star.
     *
     * @param starDescriptor the star to generate a system for
     */
    public void generateSolarSystem(StarDisplayRecord starDescriptor) {
        StarObject starObject = starService.getStar(starDescriptor.getRecordId());

        // Check for invalid stellar parameters and warn the user
        String parameterIssues = validateStellarParameters(starObject);
        if (parameterIssues != null) {
            Alert warningAlert = new Alert(Alert.AlertType.WARNING);
            warningAlert.setTitle("Missing Stellar Parameters");
            warningAlert.setHeaderText("Some stellar parameters are missing or invalid");
            warningAlert.setContentText(
                    parameterIssues +
                    "\n\nThe generator will use Sun-like default values for missing parameters. " +
                    "This may produce unrealistic results.\n\n" +
                    "Do you want to proceed anyway?"
            );
            warningAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            Optional<ButtonType> warningResult = warningAlert.showAndWait();
            if (warningResult.isEmpty() || warningResult.get() == ButtonType.NO) {
                log.info("User cancelled solar system generation due to missing stellar parameters for '{}'",
                        starObject.getDisplayName());
                return;
            }
            log.warn("User proceeding with solar system generation despite missing parameters for '{}': {}",
                    starObject.getDisplayName(), parameterIssues.replace("\n", "; "));
        }

        SolarSystemGenerationDialog dialog = new SolarSystemGenerationDialog(starObject);
        Optional<SolarSystemGenOptions> solarSystemGenOptional = dialog.showAndWait();

        if (solarSystemGenOptional.isPresent()) {
            SolarSystemGenOptions solarSystemGenOptions = solarSystemGenOptional.get();
            SolarSystemReport report = new SolarSystemReport(starObject, solarSystemGenOptions);
            report.generateReport();

            PlanetDialog planetDialog = new PlanetDialog(report);
            Optional<SolarSystemSaveResult> resultOptional = planetDialog.showAndWait();

            // Handle save request
            if (resultOptional.isPresent()) {
                SolarSystemSaveResult saveResult = resultOptional.get();
                if (saveResult.isSaveRequested()) {
                    int savedCount = solarSystemService.saveGeneratedPlanets(
                            saveResult.getSourceStar(),
                            saveResult.getPlanets()
                    );
                    log.info("Saved {} generated planets to database for star '{}'",
                            savedCount, starObject.getDisplayName());

                    // Show confirmation to user
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Solar System Saved");
                    alert.setHeaderText("Generated planets saved successfully");
                    alert.setContentText(String.format(
                            "Saved %d planets for %s.\n\n" +
                            "You can now use 'Enter System' to view the generated solar system.",
                            savedCount, starObject.getDisplayName()));
                    alert.showAndWait();
                }
            }
        }
    }

    /**
     * Validates stellar parameters required for solar system generation.
     *
     * @param starObject the star to validate
     * @return null if all parameters are valid, otherwise a string describing the issues
     */
    private String validateStellarParameters(StarObject starObject) {
        StringBuilder issues = new StringBuilder();

        if (starObject.getMass() <= 0) {
            issues.append("• Mass is missing or zero\n");
        }

        if (starObject.getRadius() <= 0) {
            issues.append("• Radius is missing or zero\n");
        }

        if (starObject.getTemperature() <= 0) {
            issues.append("• Temperature is missing or zero\n");
        }

        // Luminosity is a String - check if it's empty or not a valid positive number
        String luminosity = starObject.getLuminosity();
        if (luminosity == null || luminosity.trim().isEmpty()) {
            issues.append("• Luminosity is missing\n");
        } else {
            try {
                double lumValue = Double.parseDouble(luminosity.trim());
                if (lumValue <= 0) {
                    issues.append("• Luminosity value is zero or negative\n");
                }
            } catch (NumberFormatException e) {
                // Luminosity might be a class like "V" or "IV" - that's acceptable
                // Only flag it if it's neither a valid number nor a known luminosity class
                String trimmed = luminosity.trim().toUpperCase();
                if (!trimmed.matches("^(I{1,3}|IV|V|VI|VII|0|Ia|Ib|II|III)?[ab]?$")) {
                    // Not a standard luminosity class, might be invalid
                    // But we'll be lenient here - only flag completely empty strings
                }
            }
        }

        if (issues.length() > 0) {
            return "The following stellar parameters are missing or invalid:\n\n" + issues;
        }
        return null;
    }

    private DataSetDescriptor getCurrentDataSet() {
        return tripsContext.getDataSetDescriptor();
    }

    /**
     * Clear the routing flag when routing is complete.
     */
    public void clearRoutingFlag() {
        routeManager.setManualRoutingActive(false);
    }

    /**
     * Check if manual routing is currently active.
     *
     * @return true if manual routing is active
     */
    public boolean isManualRoutingActive() {
        return routeManager.isManualRoutingActive();
    }

    /**
     * Get the current routing type.
     *
     * @return the routing type
     */
    public RoutingType getRoutingType() {
        return routeManager.getRoutingType();
    }
}
