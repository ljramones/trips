package com.teamgannon.trips.service;

import com.teamgannon.trips.config.application.PreferencesConstants;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.events.CivilizationDisplayPreferencesChangeEvent;
import com.teamgannon.trips.events.ColorPaletteChangeEvent;
import com.teamgannon.trips.events.GraphEnablesPersistEvent;
import com.teamgannon.trips.events.StarDisplayPreferencesChangeEvent;
import com.teamgannon.trips.jpa.model.*;
import com.teamgannon.trips.jpa.repository.*;
import com.teamgannon.trips.measure.TrackExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service for managing system preferences persistence.
 * Handles loading, saving, and event-driven updates of all preference types.
 */
@Slf4j
@Service
public class SystemPreferencesService {

    private final GraphColorsRepository graphColorsRepository;
    private final GraphEnablesRepository graphEnablesRepository;
    private final StarDetailsPersistRepository starDetailsPersistRepository;
    private final CivilizationDisplayPreferencesRepository civilizationDisplayPreferencesRepository;
    private final TripsPrefsRepository tripsPrefsRepository;

    public SystemPreferencesService(GraphColorsRepository graphColorsRepository,
                                    GraphEnablesRepository graphEnablesRepository,
                                    StarDetailsPersistRepository starDetailsPersistRepository,
                                    CivilizationDisplayPreferencesRepository civilizationDisplayPreferencesRepository,
                                    TripsPrefsRepository tripsPrefsRepository) {
        this.graphColorsRepository = graphColorsRepository;
        this.graphEnablesRepository = graphEnablesRepository;
        this.starDetailsPersistRepository = starDetailsPersistRepository;
        this.civilizationDisplayPreferencesRepository = civilizationDisplayPreferencesRepository;
        this.tripsPrefsRepository = tripsPrefsRepository;
    }

    @TrackExecutionTime
    @Transactional
    public void updateDataSet(DataSetDescriptor descriptor) {
        log.info("Updating data set to: {}", descriptor.getDataSetName());
        TripsPrefs tripsPrefs = tripsPrefsRepository.findById(PreferencesConstants.MAIN_PREFS_ID)
                .orElseGet(this::createDefaultTripsPrefs);
        tripsPrefs.setDatasetName(descriptor.getDataSetName());
        tripsPrefsRepository.save(tripsPrefs);
    }

    @TrackExecutionTime
    @Transactional
    public List<StarDetailsPersist> getStarDetails() {
        log.info("Get star details");
        List<StarDetailsPersist> starDetailsPersistList = StreamSupport.stream(
                        starDetailsPersistRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());

        if (starDetailsPersistList.isEmpty()) {
            StarDisplayPreferences starDisplayPreferences = new StarDisplayPreferences();
            starDisplayPreferences.setDefaults();
            starDetailsPersistList = starDisplayPreferences.getStarDetails();
            starDetailsPersistRepository.saveAll(starDetailsPersistList);
            log.info("Created default star details for {} stellar types", starDetailsPersistList.size());
        }

        return starDetailsPersistList;
    }

    @TrackExecutionTime
    @Transactional
    public GraphEnablesPersist getGraphEnablesFromDB() {
        log.info("getGraphEnablesFromDB");
        return graphEnablesRepository.findAll().iterator().hasNext() ?
                graphEnablesRepository.findAll().iterator().next() :
                createDefaultGraphEnablesPersist();
    }

    private GraphEnablesPersist createDefaultGraphEnablesPersist() {
        GraphEnablesPersist graphEnablesPersist = new GraphEnablesPersist();
        graphEnablesPersist.setId(UUID.randomUUID().toString());
        graphEnablesRepository.save(graphEnablesPersist);
        log.info("Created default graph enables");
        return graphEnablesPersist;
    }

    @TrackExecutionTime
    @Transactional
    public void updateGraphEnables(@NotNull GraphEnablesPersist graphEnablesPersist) {
        log.info("Updating graph enables");
        graphEnablesRepository.save(graphEnablesPersist);
    }

    @TrackExecutionTime
    @Transactional
    public CivilizationDisplayPreferences getCivilizationDisplayPreferences() {
        log.info("getCivilizationDisplayPreferences");
        return civilizationDisplayPreferencesRepository.findByStorageTag(PreferencesConstants.CIVILIZATION_STORAGE_TAG)
                .orElseGet(() -> {
                    CivilizationDisplayPreferences preferences = new CivilizationDisplayPreferences();
                    preferences.reset();
                    preferences.setId(UUID.randomUUID().toString());
                    preferences.setStorageTag(PreferencesConstants.CIVILIZATION_STORAGE_TAG);
                    civilizationDisplayPreferencesRepository.save(preferences);
                    log.info("Created default civilization display preferences");
                    return preferences;
                });
    }

    @TrackExecutionTime
    @Transactional
    public void updateCivilizationDisplayPreferences(@NotNull CivilizationDisplayPreferences preferences) {
        log.info("Updating CivilizationDisplayPreferences with ID: {} and storageTag: {}",
                preferences.getId(), preferences.getStorageTag());

        CivilizationDisplayPreferences existingPreferences = civilizationDisplayPreferencesRepository
                .findByStorageTag(preferences.getStorageTag())
                .orElse(preferences);

        existingPreferences.setAratKurPolityColor(preferences.getAratKurPolityColor());
        existingPreferences.setDornaniPolityColor(preferences.getDornaniPolityColor());
        existingPreferences.setHkhRkhPolityColor(preferences.getHkhRkhPolityColor());
        existingPreferences.setHumanPolityColor(preferences.getHumanPolityColor());
        existingPreferences.setKtorPolityColor(preferences.getKtorPolityColor());
        existingPreferences.setOther1PolityColor(preferences.getOther1PolityColor());
        existingPreferences.setOther2PolityColor(preferences.getOther2PolityColor());
        existingPreferences.setOther3PolityColor(preferences.getOther3PolityColor());
        existingPreferences.setOther4PolityColor(preferences.getOther4PolityColor());
        existingPreferences.setSlaasriithiPolityColor(preferences.getSlaasriithiPolityColor());
        existingPreferences.setStorageTag(preferences.getStorageTag());

        civilizationDisplayPreferencesRepository.save(existingPreferences);
        log.info("Saved CivilizationDisplayPreferences successfully");
    }

    @TrackExecutionTime
    @Transactional
    public ColorPalette getGraphColorsFromDB() {
        log.info("getGraphColorsFromDB");
        GraphColorsPersist graphColorsPersist = graphColorsRepository.findAll().iterator().hasNext() ?
                graphColorsRepository.findAll().iterator().next() :
                createDefaultGraphColorsPersist();

        ColorPalette colorPalette = new ColorPalette();
        colorPalette.assignColors(graphColorsPersist);
        return colorPalette;
    }

    private GraphColorsPersist createDefaultGraphColorsPersist() {
        GraphColorsPersist graphColorsPersist = new GraphColorsPersist();
        graphColorsPersist.init();
        graphColorsRepository.save(graphColorsPersist);
        log.info("Created default graph colors");
        return graphColorsPersist;
    }

    @TrackExecutionTime
    @Transactional
    public void updateColors(@NotNull ColorPalette colorPalette) {
        log.info("Updating colors for palette ID: {}", colorPalette.getId());
        GraphColorsPersist graphColorsPersist = graphColorsRepository.findById(colorPalette.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "ColorPalette not found with ID: " + colorPalette.getId()));
        graphColorsPersist.setGraphColors(colorPalette);
        graphColorsRepository.save(graphColorsPersist);
    }

    @TrackExecutionTime
    @Transactional
    public void updateStarPreferences(@NotNull StarDisplayPreferences starDisplayPreferences) {
        log.info("Updating StarPreferences");
        List<StarDetailsPersist> starDetailsPersistListNew = starDisplayPreferences.getStarDetails();
        starDetailsPersistRepository.saveAll(starDetailsPersistListNew);
        log.info("Saved {} star detail records", starDetailsPersistListNew.size());
    }

    @TrackExecutionTime
    @Transactional
    public TripsPrefs getTripsPrefs() {
        log.info("getTripsPrefs");
        return tripsPrefsRepository.findById(PreferencesConstants.MAIN_PREFS_ID)
                .orElseGet(() -> {
                    TripsPrefs tripsPrefs = createDefaultTripsPrefs();
                    tripsPrefsRepository.save(tripsPrefs);
                    return tripsPrefs;
                });
    }

    private TripsPrefs createDefaultTripsPrefs() {
        TripsPrefs tripsPrefs = new TripsPrefs();
        tripsPrefs.setId(PreferencesConstants.MAIN_PREFS_ID);
        tripsPrefs.setSkipStartupDialog(false);
        log.info("Created default trips preferences");
        return tripsPrefs;
    }

    @TrackExecutionTime
    @Transactional
    public void saveTripsPrefs(TripsPrefs tripsPrefs) {
        tripsPrefsRepository.save(tripsPrefs);
        log.info("Saved trips preferences");
    }

    // ==================== Event Listeners ====================

    @EventListener
    @Async("taskExecutor")
    @Transactional
    public void onColorPaletteChangeEvent(ColorPaletteChangeEvent event) {
        try {
            updateColors(event.getColorPalette());
            log.info("Color palette persisted successfully");
        } catch (Exception e) {
            log.error("Failed to persist color palette change", e);
        }
    }

    @EventListener
    @Async("taskExecutor")
    @Transactional
    public void onGraphEnablesPersistEvent(GraphEnablesPersistEvent event) {
        try {
            updateGraphEnables(event.getGraphEnablesPersist());
            log.info("Graph enables persisted successfully");
        } catch (Exception e) {
            log.error("Failed to persist graph enables change", e);
        }
    }

    @EventListener
    @Async("taskExecutor")
    @Transactional
    public void onStarDisplayPreferencesChangeEvent(StarDisplayPreferencesChangeEvent event) {
        try {
            updateStarPreferences(event.getStarDisplayPreferences());
            log.info("Star display preferences persisted successfully");
        } catch (Exception e) {
            log.error("Failed to persist star display preferences change", e);
        }
    }

    @EventListener
    @Async("taskExecutor")
    @Transactional
    public void onCivilizationDisplayPreferencesChangeEvent(CivilizationDisplayPreferencesChangeEvent event) {
        try {
            CivilizationDisplayPreferences preferences = event.getCivilizationDisplayPreferences();
            log.info("Handling CivilizationDisplayPreferencesChangeEvent for ID: {}", preferences.getId());
            updateCivilizationDisplayPreferences(preferences);
            log.info("Civilization display preferences persisted successfully");
        } catch (Exception e) {
            log.error("Failed to persist civilization display preferences change", e);
        }
    }
}
