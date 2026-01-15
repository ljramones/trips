package com.teamgannon.trips.screenobjects.planetary;

import com.teamgannon.trips.controller.MainPane;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.ExoPlanetRepository;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import com.teamgannon.trips.service.StarService;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pane that displays a list of star systems with planets.
 * Each entry shows the star name and planet count, with a context menu
 * to enter the solar system view.
 */
@Slf4j
@Component
public class PlanetarySystemsPane extends VBox {

    private final ListView<PlanetarySystemRecord> systemsListView = new ListView<>();
    private final ApplicationEventPublisher eventPublisher;
    private final ExoPlanetRepository exoPlanetRepository;
    private final StarObjectRepository starObjectRepository;
    private final StarService starService;

    public PlanetarySystemsPane(ApplicationEventPublisher eventPublisher,
                                 ExoPlanetRepository exoPlanetRepository,
                                 StarObjectRepository starObjectRepository,
                                 StarService starService) {
        this.eventPublisher = eventPublisher;
        this.exoPlanetRepository = exoPlanetRepository;
        this.starObjectRepository = starObjectRepository;
        this.starService = starService;

        setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        setPrefHeight(400);
        systemsListView.setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        systemsListView.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(systemsListView, Priority.ALWAYS);

        systemsListView.setCellFactory(new PlanetarySystemCellFactory(eventPublisher));
        systemsListView.setPlaceholder(new Label("No planetary systems"));

        getChildren().add(systemsListView);
    }

    /**
     * Clear the list of planetary systems.
     */
    public void clear() {
        Platform.runLater(() -> systemsListView.getItems().clear());
    }

    /**
     * Refresh the list of planetary systems from the database.
     * Uses multiple strategies to find stars with planets:
     * 1. Stars linked via hostStarId (for simulated planets)
     * 2. Stars matched by name (for imported exoplanet catalogs)
     */
    public void refresh() {
        // Run database queries on current thread (should be Spring-managed)
        // Then update UI on FX thread
        try {
            log.info("PLANETARY SYSTEMS: Starting refresh...");

            // Diagnostic: count total exoplanets
            long totalExoplanets = exoPlanetRepository.count();
            log.info("PLANETARY SYSTEMS: Total exoplanets in database: {}", totalExoplanets);

            if (totalExoplanets == 0) {
                log.warn("PLANETARY SYSTEMS: No exoplanets in database - nothing to display");
                Platform.runLater(() -> systemsListView.getItems().clear());
                return;
            }

            Set<String> processedStarIds = new HashSet<>();
            List<PlanetarySystemRecord> records = new ArrayList<>();

            // Strategy 1: Get all stars that have planets linked via hostStarId
            List<StarObject> starsWithPlanets = starObjectRepository.findStarsWithPlanets();
            log.info("PLANETARY SYSTEMS: Found {} stars with planets via hostStarId", starsWithPlanets.size());

            for (StarObject star : starsWithPlanets) {
                long planetCount = exoPlanetRepository.countPlanetsByHostStarId(star.getId());
                if (planetCount > 0) {
                    StarDisplayRecord starRecord = StarDisplayRecord.fromStarObject(star);
                    records.add(new PlanetarySystemRecord(starRecord, planetCount));
                    processedStarIds.add(star.getId());
                    log.debug("PLANETARY SYSTEMS: Added {} with {} planets (via hostStarId)",
                            star.getDisplayName(), planetCount);
                }
            }

            // Strategy 2: Find stars by matching exoplanet starName to star displayName or commonName
            List<String> exoStarNames = exoPlanetRepository.findDistinctStarNames();
            log.info("PLANETARY SYSTEMS: Found {} distinct star names in exoplanet table", exoStarNames.size());

            // Log sample of star names for debugging
            if (!exoStarNames.isEmpty()) {
                int sampleSize = Math.min(10, exoStarNames.size());
                log.info("PLANETARY SYSTEMS: Sample exoplanet star names: {}", exoStarNames.subList(0, sampleSize));
            }

            int matchedByName = 0;
            int unmatchedByName = 0;

            for (String starName : exoStarNames) {
                if (starName == null || starName.isBlank()) continue;

                // Try to find a star with this display name
                StarObject star = starObjectRepository.findFirstByDisplayNameIgnoreCase(starName);

                // Also try common name if display name didn't match
                if (star == null) {
                    List<StarObject> byCommonName = starObjectRepository.findByCommonNameContainsIgnoreCase(starName);
                    if (!byCommonName.isEmpty()) {
                        star = byCommonName.get(0);
                    }
                }

                if (star != null && !processedStarIds.contains(star.getId())) {
                    long planetCount = exoPlanetRepository.countPlanetsByStarName(starName);
                    if (planetCount > 0) {
                        StarDisplayRecord starRecord = StarDisplayRecord.fromStarObject(star);
                        records.add(new PlanetarySystemRecord(starRecord, planetCount));
                        processedStarIds.add(star.getId());
                        matchedByName++;
                        log.debug("PLANETARY SYSTEMS: Added {} with {} planets (via starName match)",
                                star.getDisplayName(), planetCount);
                    }
                } else if (star == null) {
                    unmatchedByName++;
                    if (unmatchedByName <= 5) {
                        log.warn("PLANETARY SYSTEMS: Could not find star for exoplanet starName: '{}'", starName);
                    }
                }
            }

            log.info("PLANETARY SYSTEMS: Name matching results - matched: {}, unmatched: {}", matchedByName, unmatchedByName);

            // Strategy 3: Find stars by RA/Dec proximity to exoplanets (like SolarSystemService does)
            // This handles imported exoplanets where names don't match but coordinates do
            if (unmatchedByName > 0) {
                log.info("PLANETARY SYSTEMS: Trying RA/Dec matching for unmatched exoplanets...");
                int matchedByRaDec = 0;

                for (String starName : exoStarNames) {
                    if (starName == null || starName.isBlank()) continue;

                    // Get an exoplanet with this star name to get its RA/Dec
                    List<com.teamgannon.trips.jpa.model.ExoPlanet> exoPlanets = exoPlanetRepository.findByStarName(starName);
                    if (exoPlanets.isEmpty()) continue;

                    com.teamgannon.trips.jpa.model.ExoPlanet sample = exoPlanets.get(0);
                    if (sample.getRa() == null || sample.getDec() == null) continue;

                    // Find stars near this RA/Dec
                    List<StarObject> nearbyStars = starObjectRepository.findByRaAndDecNear(
                            sample.getRa(), sample.getDec());

                    for (StarObject star : nearbyStars) {
                        if (!processedStarIds.contains(star.getId())) {
                            long planetCount = exoPlanets.size();
                            StarDisplayRecord starRecord = StarDisplayRecord.fromStarObject(star);
                            records.add(new PlanetarySystemRecord(starRecord, planetCount));
                            processedStarIds.add(star.getId());
                            matchedByRaDec++;
                            log.debug("PLANETARY SYSTEMS: Added {} with {} planets (via RA/Dec match)",
                                    star.getDisplayName(), planetCount);
                        }
                    }
                }
                log.info("PLANETARY SYSTEMS: RA/Dec matching found {} additional stars", matchedByRaDec);
            }

            // Sort by star name
            records.sort(Comparator.comparing(PlanetarySystemRecord::getStarName));

            log.info("PLANETARY SYSTEMS: Prepared {} planetary systems total", records.size());

            // Update UI on FX thread
            final List<PlanetarySystemRecord> finalRecords = records;
            Platform.runLater(() -> {
                systemsListView.getItems().clear();
                systemsListView.getItems().addAll(finalRecords);
                log.info("PLANETARY SYSTEMS: UI updated with {} items", finalRecords.size());
            });

        } catch (Exception e) {
            log.error("Error refreshing planetary systems list", e);
        }
    }

    /**
     * Add a single planetary system record to the list.
     *
     * @param starDisplayRecord the star with planets
     * @param planetCount       the number of planets
     */
    public void add(StarDisplayRecord starDisplayRecord, long planetCount) {
        if (starDisplayRecord == null || planetCount <= 0) return;

        Platform.runLater(() -> {
            PlanetarySystemRecord record = new PlanetarySystemRecord(starDisplayRecord, planetCount);

            // Check if already in list
            boolean exists = systemsListView.getItems().stream()
                    .anyMatch(r -> r.getRecordId() != null && r.getRecordId().equals(record.getRecordId()));

            if (!exists) {
                systemsListView.getItems().add(record);
                systemsListView.getItems().sort(Comparator.comparing(PlanetarySystemRecord::getStarName));
            }
        });
    }

    /**
     * Remove a planetary system from the list by star ID.
     *
     * @param starId the star ID to remove
     */
    public void remove(String starId) {
        if (starId == null) return;

        Platform.runLater(() -> {
            systemsListView.getItems().removeIf(r -> starId.equals(r.getRecordId()));
        });
    }

    /**
     * Get the count of planetary systems in the list.
     *
     * @return the count
     */
    public int getCount() {
        return systemsListView.getItems().size();
    }

    // NOTE: PlanetarySystemsPane does NOT listen to ClearListEvent.
    // The planetary systems list is derived from the database, not from the current view,
    // so it should persist regardless of what's displayed in the 3D view.
}
