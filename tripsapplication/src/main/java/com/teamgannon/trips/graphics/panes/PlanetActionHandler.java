package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.dialogs.solarsystem.PlanetEditResult;
import com.teamgannon.trips.dialogs.solarsystem.ProceduralPlanetViewerDialog;
import com.teamgannon.trips.events.ContextSelectionType;
import com.teamgannon.trips.events.ContextSelectorEvent;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.planetary.PlanetaryContext;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetConfig;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetGenerator;
import com.teamgannon.trips.planetarymodelling.procedural.ProceduralPlanetPersistenceHelper;
import com.teamgannon.trips.service.SolarSystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Handles planet-related actions for solar system visualization.
 * <p>
 * Handles:
 * <ul>
 *   <li>Planet property editing</li>
 *   <li>Planet deletion</li>
 *   <li>Landing on planets (switch to planetary view)</li>
 *   <li>Viewing procedural terrain</li>
 *   <li>Adding new planets</li>
 * </ul>
 */
@Slf4j
public class PlanetActionHandler {

    private final SolarSystemService solarSystemService;
    private final ApplicationEventPublisher eventPublisher;
    private final Runnable refreshSystemCallback;

    private SolarSystemDescription currentSystem;

    public PlanetActionHandler(SolarSystemService solarSystemService,
                               ApplicationEventPublisher eventPublisher,
                               Runnable refreshSystemCallback) {
        this.solarSystemService = solarSystemService;
        this.eventPublisher = eventPublisher;
        this.refreshSystemCallback = refreshSystemCallback;
    }

    /**
     * Set the current solar system context.
     */
    public void setCurrentSystem(SolarSystemDescription system) {
        this.currentSystem = system;
    }

    /**
     * Handle planet edit result from properties dialog.
     */
    public void handlePlanetEdit(PlanetEditResult result) {
        if (!result.isChanged()) {
            return;
        }

        log.info("Planet edited: {}, orbital changed: {}", result.getPlanet().getName(), result.isOrbitalChanged());

        // Persist changes to database
        solarSystemService.updateExoPlanet(result.getPlanet());

        // If orbital properties changed, refresh the visualization
        if (result.isOrbitalChanged() && currentSystem != null) {
            refreshSystemCallback.run();
        }
    }

    /**
     * Handle planet deletion.
     */
    public void handlePlanetDelete(ExoPlanet planet) {
        log.info("Deleting planet: {}", planet.getName());

        // Delete from database
        solarSystemService.deleteExoPlanet(planet.getId());

        // Refresh the visualization
        if (currentSystem != null) {
            refreshSystemCallback.run();
        }
    }

    /**
     * Handle adding a new planet to the system.
     */
    public void handleAddPlanet(ExoPlanet newPlanet) {
        if (newPlanet == null) return;

        log.info("Adding new planet: {}", newPlanet.getName());

        // Save to database
        solarSystemService.addExoPlanet(newPlanet);

        // Refresh the visualization
        refreshSystemCallback.run();
    }

    /**
     * Handle "Land on Planet" - switch to planetary view showing sky from planet's surface.
     */
    public void handleLandOnPlanet(ExoPlanet planet) {
        if (planet == null || currentSystem == null) {
            log.warn("Cannot land on planet: planet or system is null");
            return;
        }

        log.info("Landing on planet: {}", planet.getName());

        // Build the planetary context
        PlanetaryContext context = PlanetaryContext.builder()
                .planet(planet)
                .system(currentSystem)
                .hostStar(currentSystem.getStarDisplayRecord())
                .localTime(22.0)  // Default to night
                .viewingAzimuth(0.0)  // Looking north
                .viewingAltitude(15.0)  // Slight look-up, camera Y offset handles horizon placement
                .magnitudeLimit(6.0)  // Default naked-eye limit
                .fieldOfView(90.0)
                .showAtmosphereEffects(true)
                .build();

        // Publish event to switch to planetary view
        eventPublisher.publishEvent(new ContextSelectorEvent(
                this,
                ContextSelectionType.PLANETARY,
                currentSystem.getStarDisplayRecord(),
                context));
    }

    /**
     * Handle "View Terrain" - generate and display procedural terrain for the planet.
     */
    public void handleViewTerrain(ExoPlanet exoPlanet) {
        if (exoPlanet == null) {
            log.warn("Cannot view terrain: planet is null");
            return;
        }

        // Don't allow terrain viewing for gas giants
        if (Boolean.TRUE.equals(exoPlanet.getGasGiant())) {
            log.info("Cannot view terrain for gas giant: {}", exoPlanet.getName());
            return;
        }

        log.info("Generating procedural terrain for: {}", exoPlanet.getName());

        try {
            // Generate deterministic seed from planet ID
            long seed = exoPlanet.getId() != null ? exoPlanet.getId().hashCode() : System.nanoTime();

            // Create PlanetConfig directly from ExoPlanet properties
            PlanetConfig config = ProceduralPlanetPersistenceHelper.buildConfigFromExoPlanet(exoPlanet, seed);

            // Generate procedural terrain
            PlanetGenerator.GeneratedPlanet generated = PlanetGenerator.generate(config);

            log.info("Generated terrain with {} polygons, {} rivers",
                    generated.polygons().size(),
                    generated.rivers() != null ? generated.rivers().size() : 0);

            ProceduralPlanetPersistenceHelper.populateProceduralMetadata(
                    exoPlanet, config, seed, generated, "ACCRETE");
            solarSystemService.updateExoPlanet(exoPlanet);

            // Show the terrain viewer dialog
            ProceduralPlanetViewerDialog dialog = new ProceduralPlanetViewerDialog(
                    exoPlanet.getName(),
                    generated,
                    (planet, planetConfig) -> {
                        ProceduralPlanetPersistenceHelper.populateProceduralMetadata(
                                exoPlanet, planetConfig, planetConfig.seed(), planet, "USER_OVERRIDES");
                        solarSystemService.updateExoPlanet(exoPlanet);
                    });

            // Set planet type for terrain type determination (JOVIAN/ICE_GIANT/etc)
            String planetType = exoPlanet.getPlanetType();
            if (planetType != null && !planetType.isBlank()) {
                dialog.setPlanetType(planetType);
            }

            // Set ice cover for terrain type determination (ICE for icy moons like Europa, Pluto)
            Double iceCover = exoPlanet.getIceCover();
            if (iceCover != null && iceCover > 0) {
                dialog.setIceCover(iceCover);
            }

            // Set surface temperature for terrain type determination (DRY/WET/ICE)
            Double surfaceTemp = exoPlanet.getSurfaceTemperature();
            if (surfaceTemp != null && surfaceTemp > 0) {
                dialog.setSurfaceTemperature(surfaceTemp);
            }

            // Set density for ice detection (ice-rich bodies have lower density)
            Double density = exoPlanet.getDensity();
            if (density != null && density > 0) {
                dialog.setDensity(density);
            }

            // Set semi-major axis for frost line detection (ice beyond ~2.7 AU)
            Double sma = exoPlanet.getSemiMajorAxis();
            if (sma != null && sma > 0) {
                dialog.setSemiMajorAxis(sma);
            }

            dialog.showAndWait();

        } catch (Exception e) {
            log.error("Failed to generate terrain for planet: {}", exoPlanet.getName(), e);
        }
    }

    /**
     * Find an ExoPlanet entity by name.
     */
    public ExoPlanet findExoPlanetByName(String name) {
        return solarSystemService.findExoPlanetByName(name);
    }
}
