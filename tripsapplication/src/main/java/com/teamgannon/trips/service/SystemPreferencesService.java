package com.teamgannon.trips.service;


import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.jpa.model.*;
import com.teamgannon.trips.jpa.repository.*;
import com.teamgannon.trips.measure.TrackExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class SystemPreferencesService {


    /**
     * storage of graph colors in DB
     */
    private final GraphColorsRepository graphColorsRepository;

    /**
     * storage of graph enables in DB
     */
    private final GraphEnablesRepository graphEnablesRepository;

    /**
     * stores all teh star details
     */
    private final StarDetailsPersistRepository starDetailsPersistRepository;

    /**
     * civilization bases
     */
    private final CivilizationDisplayPreferencesRepository civilizationDisplayPreferencesRepository;

    /**
     * trips prefs
     */
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
        Optional<TripsPrefs> tripsPrefsOptional = tripsPrefsRepository.findById("main");
        if (tripsPrefsOptional.isPresent()) {
            TripsPrefs tripsPrefs = tripsPrefsOptional.get();
            tripsPrefs.setDatasetName(descriptor.getDataSetName());
            tripsPrefsRepository.save(tripsPrefs);
        }
    }

    /**
     * get the star preferences details
     *
     * @return the list of star details
     */
    @TrackExecutionTime
    @Transactional
    public List<StarDetailsPersist> getStarDetails() {
        Iterable<StarDetailsPersist> starDetailsPersists = starDetailsPersistRepository.findAll();
        List<StarDetailsPersist> starDetailsPersistList = StreamSupport.stream(starDetailsPersists.spliterator(), false).collect(Collectors.toList());
        if (starDetailsPersistList.isEmpty()) {
            StarDisplayPreferences starDisplayPreferences = new StarDisplayPreferences();
            starDisplayPreferences.setDefaults();
            List<StarDetailsPersist> starDetailsPersistListNew = starDisplayPreferences.getStarDetails();
            starDetailsPersistRepository.saveAll(starDetailsPersistListNew);
            return starDetailsPersistListNew;
        } else {
            return starDetailsPersistList;
        }
    }


    @TrackExecutionTime
    @Transactional
    public GraphEnablesPersist getGraphEnablesFromDB() {
        Iterable<GraphEnablesPersist> graphEnables = graphEnablesRepository.findAll();
        GraphEnablesPersist graphEnablesPersist;

        if (graphEnables.iterator().hasNext()) {
            graphEnablesPersist = graphEnables.iterator().next();
        } else {
            graphEnablesPersist = new GraphEnablesPersist();
            graphEnablesPersist.setId(UUID.randomUUID().toString());
            graphEnablesRepository.save(graphEnablesPersist);
        }

        return graphEnablesPersist;
    }


    @TrackExecutionTime
    @Transactional
    public void updateGraphEnables(@NotNull GraphEnablesPersist graphEnablesPersist) {
        graphEnablesRepository.save(graphEnablesPersist);
    }



    @TrackExecutionTime
    @Transactional
    public CivilizationDisplayPreferences getCivilizationDisplayPreferences() {
        Optional<CivilizationDisplayPreferences> optionalCivilizationDisplayPreferences = civilizationDisplayPreferencesRepository.findByStorageTag("Main");
        CivilizationDisplayPreferences civilizationDisplayPreferences;

        if (optionalCivilizationDisplayPreferences.isPresent()) {
            civilizationDisplayPreferences = optionalCivilizationDisplayPreferences.get();
        } else {
            civilizationDisplayPreferences = new CivilizationDisplayPreferences();
            civilizationDisplayPreferences.reset();
            civilizationDisplayPreferences.setId(UUID.randomUUID());
            civilizationDisplayPreferencesRepository.save(civilizationDisplayPreferences);
        }

        return civilizationDisplayPreferences;
    }


    @TrackExecutionTime
    public void updateCivilizationDisplayPreferences(@NotNull CivilizationDisplayPreferences preferences) {
        civilizationDisplayPreferencesRepository.save(preferences);
    }

    @TrackExecutionTime
    @Transactional
    public ColorPalette getGraphColorsFromDB() {
        Iterable<GraphColorsPersist> graphColors = graphColorsRepository.findAll();
        GraphColorsPersist graphColorsPersist;

        if (graphColors.iterator().hasNext()) {
            graphColorsPersist = graphColors.iterator().next();
        } else {
            graphColorsPersist = new GraphColorsPersist();
            graphColorsPersist.init();
            graphColorsRepository.save(graphColorsPersist);
        }

        ColorPalette colorPalette = new ColorPalette();
        colorPalette.assignColors(graphColorsPersist);
        return colorPalette;
    }


    @TrackExecutionTime
    @Transactional
    public void updateColors(@NotNull ColorPalette colorPalette) {
        Optional<GraphColorsPersist> graphColorsPersistOptional = graphColorsRepository.findById(colorPalette.getId());
        if (graphColorsPersistOptional.isPresent()) {
            GraphColorsPersist graphColorsPersist = graphColorsPersistOptional.get();
            graphColorsPersist.setGraphColors(colorPalette);
            graphColorsRepository.save(graphColorsPersist);
        }
    }

    /**
     * update the star display preferences
     *
     * @param starDisplayPreferences the star preferences
     */
    @TrackExecutionTime
    @Transactional
    public void updateStarPreferences(@NotNull StarDisplayPreferences starDisplayPreferences) {
        List<StarDetailsPersist> starDetailsPersistListNew = starDisplayPreferences.getStarDetails();
        starDetailsPersistRepository.saveAll(starDetailsPersistListNew);
    }


    @TrackExecutionTime
    @Transactional
    public TripsPrefs getTripsPrefs() {
        Optional<TripsPrefs> tripsPrefsOptional = tripsPrefsRepository.findById("main");
        if (tripsPrefsOptional.isPresent()) {
            return tripsPrefsOptional.get();
        } else {
            TripsPrefs tripsPrefs = new TripsPrefs();
            tripsPrefs.setId("main");
            tripsPrefs.setShowWelcomeDataReq(false);
            tripsPrefsRepository.save(tripsPrefs);
            return tripsPrefs;
        }
    }


    @TrackExecutionTime
    @Transactional
    public void saveTripsPrefs(TripsPrefs tripsPrefs) {
        tripsPrefsRepository.save(tripsPrefs);
    }



}
