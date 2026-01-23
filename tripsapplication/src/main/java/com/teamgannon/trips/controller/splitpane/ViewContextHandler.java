package com.teamgannon.trips.controller.splitpane;

import com.teamgannon.trips.events.ContextSelectorEvent;
import com.teamgannon.trips.events.StatusUpdateEvent;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.javafxsupport.FxThread;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.jpa.repository.ExoPlanetRepository;
import com.teamgannon.trips.planetary.PlanetaryContext;
import com.teamgannon.trips.screenobjects.planetary.PlanetaryViewControlPane;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Handles view context switching between interstellar, solar system, and planetary views.
 * <p>
 * This class manages the complex logic for transitioning between different visualization
 * modes and coordinating the left display and right side panel.
 */
@Slf4j
@Component
public class ViewContextHandler {

    private final ApplicationEventPublisher eventPublisher;
    private final RightPanelCoordinator rightPanelCoordinator;
    private final ExoPlanetRepository exoPlanetRepository;

    private LeftDisplayController leftDisplayController;

    public ViewContextHandler(ApplicationEventPublisher eventPublisher,
                              RightPanelCoordinator rightPanelCoordinator,
                              ExoPlanetRepository exoPlanetRepository) {
        this.eventPublisher = eventPublisher;
        this.rightPanelCoordinator = rightPanelCoordinator;
        this.exoPlanetRepository = exoPlanetRepository;
    }

    /**
     * Initialize with the left display controller (called after construction).
     *
     * @param leftDisplayController the left display controller
     */
    public void initialize(LeftDisplayController leftDisplayController) {
        this.leftDisplayController = leftDisplayController;
    }

    @EventListener
    public void onContextSelectorEvent(ContextSelectorEvent event) {
        FxThread.runOnFxThread(() -> {
            try {
                switch (event.getContextSelectionType()) {
                    case INTERSTELLAR -> handleInterstellarContext();
                    case SOLARSYSTEM -> handleSolarSystemContext(event.getStarDisplayRecord());
                    case PLANETARY -> handlePlanetaryContext(event);
                    default -> log.error("Unexpected value: {}", event.getContextSelectionType());
                }
            } catch (Exception e) {
                log.error("Error handling context selector event: {}", event.getContextSelectionType(), e);
                showErrorAlert("View Switch Error", "Failed to switch view: " + e.getMessage());
                eventPublisher.publishEvent(new StatusUpdateEvent(this, "View switch failed"));
            }
        });
    }

    private void handleInterstellarContext() {
        log.info("Showing interstellar Space");
        leftDisplayController.showInterstellar();
        rightPanelCoordinator.switchToInterstellar();
        eventPublisher.publishEvent(new StatusUpdateEvent(this, "Selected Interstellar space"));
    }

    private void handleSolarSystemContext(StarDisplayRecord starDisplayRecord) {
        log.info("Showing a solar system");
        leftDisplayController.showSolarSystem(starDisplayRecord);
        rightPanelCoordinator.switchToSolarSystem(starDisplayRecord);
        eventPublisher.publishEvent(new StatusUpdateEvent(this, "Selected Solarsystem space: " + starDisplayRecord.getStarName()));
    }

    private void handlePlanetaryContext(ContextSelectorEvent event) {
        log.info("Showing planetary surface view");
        PlanetaryContext context = event.getPlanetaryContext();
        if (context == null) {
            StarDisplayRecord star = event.getStarDisplayRecord();
            ExoPlanet planet = findDefaultPlanet(star);
            if (planet == null) {
                log.warn("No planets available for {}", star != null ? star.getStarName() : "unknown star");
                eventPublisher.publishEvent(new StatusUpdateEvent(this,
                        "No planets available for planetary view"));
                return;
            }
            context = PlanetaryContext.builder()
                    .hostStar(star)
                    .planet(planet)
                    .localTime(22.0)
                    .build();
        }

        // Snapshot for lambdas (context is reassigned above, so it's not effectively final)
        final PlanetaryContext finalContext = context;

        leftDisplayController.showPlanetary(finalContext);
        rightPanelCoordinator.switchToPlanetary(finalContext);
        rightPanelCoordinator.updatePlanetaryBrightestStars(
                leftDisplayController.getPlanetarySpacePane().getBrightestStars(),
                leftDisplayController.getPlanetarySpacePane().getVisibleStarCount());

        setupPlanetaryViewControls(finalContext);

        eventPublisher.publishEvent(new StatusUpdateEvent(this,
                "Viewing sky from " + finalContext.getPlanetName()));
    }

    private void setupPlanetaryViewControls(PlanetaryContext context) {
        var viewControlPane = rightPanelCoordinator.getPlanetaryViewControlPane();
        if (viewControlPane == null) {
            return;
        }

        viewControlPane.setOnTimeChanged(time -> {
            leftDisplayController.getPlanetarySpacePane().updateLocalTime(time);
            rightPanelCoordinator.updatePlanetaryBrightestStars(
                    leftDisplayController.getPlanetarySpacePane().getBrightestStars(),
                    leftDisplayController.getPlanetarySpacePane().getVisibleStarCount());
        });

        viewControlPane.setOnMagnitudeChanged(mag -> {
            leftDisplayController.getPlanetarySpacePane().updateMagnitudeLimit(mag);
            rightPanelCoordinator.updatePlanetaryBrightestStars(
                    leftDisplayController.getPlanetarySpacePane().getBrightestStars(),
                    leftDisplayController.getPlanetarySpacePane().getVisibleStarCount());
        });

        viewControlPane.setOnAtmosphereChanged(enabled ->
                leftDisplayController.getPlanetarySpacePane().updateAtmosphere(enabled));

        viewControlPane.setOnOrientationGridChanged(enabled ->
                leftDisplayController.getPlanetarySpacePane().updateOrientationGrid(enabled));

        viewControlPane.setOnShowLabelsChanged(enabled ->
                leftDisplayController.getPlanetarySpacePane().setStarLabelsOn(enabled));

        viewControlPane.setOnLabelMagnitudeChanged(mag -> {
            leftDisplayController.getPlanetarySpacePane().updateLabelMagnitudeLimit(mag);
        });

        // Wire star click-to-identify
        leftDisplayController.getPlanetarySpacePane().setOnStarClicked(star -> {
            rightPanelCoordinator.getPlanetarySidePane().getBrightestStarsPane().showSelectedStar(star);
            rightPanelCoordinator.getPlanetarySidePane().expandStarsList();
        });

        viewControlPane.setOnDirectionChanged(direction ->
                leftDisplayController.getPlanetarySpacePane().setViewingDirection(
                        PlanetaryViewControlPane.directionToAzimuth(direction),
                        context.getViewingAltitude()));

        viewControlPane.setOnPresetSelected(preset -> {
            var spacePane = leftDisplayController.getPlanetarySpacePane();
            double azimuth = context.getViewingAzimuth();
            double altitude = context.getViewingAltitude();
            switch (preset) {
                case ZENITH -> {
                    altitude = 90.0;
                }
                case HIGH_SKY -> {
                    azimuth = 0.0;
                    altitude = 65.0;
                }
                case HORIZON -> {
                    azimuth = 0.0;
                    altitude = 15.0;  // Slight look-up, combined with camera Y offset
                }
                case NADIR -> {
                    altitude = -90.0;
                }
                case FOCUS_BRIGHTEST -> {
                    var brightest = spacePane.getBrightestStars();
                    if (brightest != null && !brightest.isEmpty()) {
                        var target = brightest.get(0);
                        azimuth = target.getAzimuth();
                        altitude = target.getAltitude();
                    }
                }
            }
            context.setViewingAzimuth(azimuth);
            context.setViewingAltitude(altitude);
            spacePane.setViewingDirection(azimuth, altitude);
        });
    }

    private ExoPlanet findDefaultPlanet(StarDisplayRecord star) {
        if (star == null) {
            return null;
        }
        String recordId = star.getRecordId();
        if (recordId != null && !recordId.isBlank()) {
            List<ExoPlanet> planets = exoPlanetRepository.findByHostStarId(recordId);
            if (planets != null && !planets.isEmpty()) {
                return planets.get(0);
            }
        }
        String starName = star.getStarName();
        if (starName != null && !starName.isBlank()) {
            List<ExoPlanet> planets = exoPlanetRepository.findByStarName(starName);
            if (planets != null && !planets.isEmpty()) {
                return planets.get(0);
            }
        }
        return null;
    }
}
