package com.teamgannon.trips.service;

import com.teamgannon.trips.algorithms.StarMath;
import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.dataset.factories.DataSetDescriptorFactory;
import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.file.chview.model.ChViewFile;
import com.teamgannon.trips.file.csvin.RegCSVFile;
import com.teamgannon.trips.file.excel.normal.ExcelFile;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.jpa.model.*;
import com.teamgannon.trips.jpa.repository.*;
import com.teamgannon.trips.routing.model.Route;
import com.teamgannon.trips.routing.model.SparseStarRecord;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.export.model.JsonExportObj;
import com.teamgannon.trips.service.importservices.tasks.ProgressUpdater;
import com.teamgannon.trips.support.LogExecutionTime;
import com.teamgannon.trips.transits.TransitDefinitions;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * Used to manage all the database interactions
 * <p>
 * Created by larrymitchell on 2017-01-20.
 */
@Slf4j
@Service
public class DatabaseManagementService {

    private static final int MAX_REQUEST_SIZE = 9999;

    /**
     * storage of data sets in DB
     */
    private final DataSetDescriptorRepository dataSetDescriptorRepository;

    /**
     * storage of astrographic objects in DB
     */
    private final StarObjectRepository starObjectRepository;

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

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * trips prefs
     */
    private final TripsPrefsRepository tripsPrefsRepository;

    /**
     * transit settings
     */
    private TransitSettingsRepository transitSettingsRepository;

    /**
     * constructor
     *
     * @param dataSetDescriptorRepository  the data descriptor repo
     * @param starObjectRepository         the astrographic objects
     * @param graphColorsRepository        the graph colors
     * @param graphEnablesRepository       the graph enables
     * @param starDetailsPersistRepository the star details
     */
    public DatabaseManagementService(DataSetDescriptorRepository dataSetDescriptorRepository,
                                     StarObjectRepository starObjectRepository,
                                     GraphColorsRepository graphColorsRepository,
                                     GraphEnablesRepository graphEnablesRepository,
                                     StarDetailsPersistRepository starDetailsPersistRepository,
                                     CivilizationDisplayPreferencesRepository civilizationDisplayPreferencesRepository,
                                     TripsPrefsRepository tripsPrefsRepository,
                                     TransitSettingsRepository transitSettingsRepository) {

        this.dataSetDescriptorRepository = dataSetDescriptorRepository;
        this.starObjectRepository = starObjectRepository;
        this.graphColorsRepository = graphColorsRepository;
        this.graphEnablesRepository = graphEnablesRepository;
        this.starDetailsPersistRepository = starDetailsPersistRepository;
        this.civilizationDisplayPreferencesRepository = civilizationDisplayPreferencesRepository;
        this.tripsPrefsRepository = tripsPrefsRepository;
        this.transitSettingsRepository = transitSettingsRepository;
    }

    /**
     * drop all the tables
     */
    public void dropDatabase() {
        log.info("Dropping database");
        starObjectRepository.deleteAll();
    }

    public @NotNull
    DataSetDescriptor loadCHFile(@NotNull ProgressUpdater progressUpdater, @NotNull Dataset dataset, @NotNull ChViewFile chViewFile) throws Exception {

        // this method call actually saves the dataset in elasticsearch
        return DataSetDescriptorFactory.createDataSetDescriptor(
                progressUpdater,
                dataset,
                dataSetDescriptorRepository,
                starObjectRepository,
                chViewFile
        );
    }

    public List<StarObject> runNativeQuery(String queryToRun) {
        Query query = entityManager.createNativeQuery(queryToRun, StarObject.class);
        List<StarObject> starObjects = query.getResultList();
        for (StarObject starObject : starObjects) {
            log.info(starObject.toString());
        }
        log.info("number of elements=" + starObjects.size());
        return starObjects;
    }

    public @NotNull
    DataSetDescriptor loadCSVFile(@NotNull RegCSVFile regCSVFile) throws Exception {
        return DataSetDescriptorFactory.createDataSetDescriptor(
                dataSetDescriptorRepository,
                regCSVFile
        );
    }

    ///////////////////////////////////////

    /**
     * get a set of astrographic objects based on a query
     *
     * @param searchContext the search context
     * @return the list of objects
     */
    public List<StarObject> getAstrographicObjectsOnQuery(@NotNull SearchContext searchContext) {
        AstroSearchQuery searchQuery = searchContext.getAstroSearchQuery();
        List<StarObject> starObjects;
        if (searchQuery.isRecenter()) {
            starObjects
                    = starObjectRepository.findByDataSetNameAndXGreaterThanAndXLessThanAndYGreaterThanAndYLessThanAndZGreaterThanAndZLessThanOrderByDisplayName(
                    searchQuery.getDescriptor().getDataSetName(),
                    searchQuery.getXMinus(),
                    searchQuery.getXPlus(),
                    searchQuery.getYMinus(),
                    searchQuery.getYPlus(),
                    searchQuery.getZMinus(),
                    searchQuery.getZPlus()
            );

        } else {
            starObjects = starObjectRepository.findBySearchQuery(searchQuery);
        }
        log.info("New DB Query returns {} stars", starObjects.size());
        starObjects = filterByDistance(starObjects, searchQuery.getCenterCoordinates(), searchQuery.getUpperDistanceLimit());
        log.info("Filtered by distance Query returns {} stars", starObjects.size());
        return starObjects;
    }


    @Transactional(readOnly = true)
    public Page<StarObject> getStarPaged(AstroSearchQuery searchQuery, Pageable pageable) {
        return starObjectRepository.findBySearchQueryPaged(searchQuery, pageable);
    }

    /**
     * filter the list to distance by selected distance
     *
     * @param starObjects            the astrogrpic objects to display
     * @param centerCoordinates      the plot center coordinates
     * @param distanceFromCenterStar the distance frm the centre star to display
     * @return the fitlered list
     */
    private @NotNull
    List<StarObject> filterByDistance(
            @NotNull List<StarObject> starObjects,
            double[] centerCoordinates,
            double distanceFromCenterStar) {
        List<StarObject> filterList = new ArrayList<>();
        starObjects.forEach(object -> {
            try {
                double[] starPosition = new double[3];
                starPosition[0] = object.getX();
                starPosition[1] = object.getY();
                starPosition[2] = object.getZ();
                if (StarMath.inSphere(centerCoordinates, starPosition, distanceFromCenterStar)) {
                    filterList.add(object);
                }
            } catch (Exception e) {
                log.error("error in finding distance:", e);
            }
        });
        return filterList;
    }

    //////////////////////////////////////


    /**
     * remove the dataset by descriptor
     *
     * @param descriptor the descriptor to remove
     */
    @Transactional
    public void removeDataSet(@NotNull DataSetDescriptor descriptor) {
        starObjectRepository.deleteByDataSetName(descriptor.getDataSetName());
        dataSetDescriptorRepository.delete(descriptor);
    }

    /**
     * get the data sets
     *
     * @return the list of all descriptors in the database
     */
    public @NotNull
    List<DataSetDescriptor> getDataSets() {
        Iterable<DataSetDescriptor> dataSetDescriptors = dataSetDescriptorRepository.findAll();
        List<DataSetDescriptor> descriptors = new ArrayList<>();
        dataSetDescriptors.forEach(descriptors::add);
        return descriptors;
    }

    public List<StarObject> getFromDataset(DataSetDescriptor dataSetDescriptor) {
        // we can only effectively gather 500 at a time
        return toList(
                starObjectRepository.findByDataSetName(
                        dataSetDescriptor.getDataSetName(),
                        PageRequest.of(0, MAX_REQUEST_SIZE)
                )
        );
    }

    public Page<StarObject> getFromDatasetByPage(DataSetDescriptor dataSetDescriptor, int pageNumber, int requestSize) {
        // we can only effectively gather 500 at a time
        return starObjectRepository.findByDataSetName(
                dataSetDescriptor.getDataSetName(),
                PageRequest.of(pageNumber, requestSize)
        );
    }

    @LogExecutionTime
    @Transactional(readOnly = true)
    public Map<String, SparseStarRecord> getFromDatasetWithinRanges(@NotNull DataSetDescriptor dataSetDescriptor, double distance) {
        final Map<String,SparseStarRecord> starRecordHashMap = new HashMap<>();
        Stream<StarObject> starObjectStream = starObjectRepository.findByDataSetNameAndDistanceIsLessThanEqual(dataSetDescriptor.getDataSetName(), distance);

        starObjectStream.forEach(starObject -> {
            SparseStarRecord sparseStarRecord= starObject.toSparseStarRecord();
            starRecordHashMap.put(sparseStarRecord.getStarName(), sparseStarRecord);
        });

        return starRecordHashMap;
    }

    @LogExecutionTime
    @Transactional(readOnly = true)
    public List<StarObject> getStarsBasedOnId(List<UUID> starIdList) {
        return starObjectRepository.findByIdIn(starIdList);
    }


    public List<StarObject> getFromDatasetWithinLimit(@NotNull DataSetDescriptor dataSetDescriptor, double distance) {
        // we can only effectively gather 500 at a time
        return toList(starObjectRepository.findByDataSetNameAndDistanceIsLessThanOrderByDisplayName(dataSetDescriptor.getDataSetName(), distance, PageRequest.of(0, MAX_REQUEST_SIZE)));
    }

    public DataSetDescriptor getDatasetFromName(String dataSetName) {
        return dataSetDescriptorRepository.findByDataSetName(dataSetName);
    }
    ///////////////

    /**
     * get the star details
     *
     * @return the list of star details
     */
    @Transactional
    public List<StarDetailsPersist> getStarDetails() {
        Iterable<StarDetailsPersist> starDetailsPersists = starDetailsPersistRepository.findAll();
        List<StarDetailsPersist> starDetailsPersistList = StreamSupport.stream(starDetailsPersists.spliterator(), false).collect(Collectors.toList());
        if (starDetailsPersistList.size() == 0) {
            StarDisplayPreferences starDisplayPreferences = new StarDisplayPreferences();
            starDisplayPreferences.setDefaults();
            List<StarDetailsPersist> starDetailsPersistListNew = starDisplayPreferences.getStarDetails();
            starDetailsPersistRepository.saveAll(starDetailsPersistListNew);
            return starDetailsPersistListNew;
        } else {
            return starDetailsPersistList;
        }
    }

    //////////////

    /**
     * helper method to return page as list
     *
     * @param pageResult the page result
     * @return the list representation
     */
    private @NotNull
    List<StarObject> toList(@NotNull Page<StarObject> pageResult) {
        return pageResult.getContent();
    }

    /**
     * remove the star from the db
     *
     * @param starObject the astrographic object
     */
    @Transactional
    public void removeStar(@NotNull StarObject starObject) {
        starObjectRepository.delete(starObject);
    }

    /**
     * add a new star
     *
     * @param starObjectNew the star to add
     */
    @Transactional
    public void addStar(@NotNull StarObject starObjectNew) {
//        starObjectNew.calculateDisplayScore();
        starObjectRepository.save(starObjectNew);
        Optional<StarObject> testGet = starObjectRepository.findById(starObjectNew.getId());
        if (testGet.isEmpty()) {
            log.error("why didn't this save work");
        }
    }


    /**
     * update the star
     *
     * @param starObject the star to update
     */
    @Transactional
    public void updateStar(@NotNull StarObject starObject) {
//        starObject.calculateDisplayScore();
        starObjectRepository.save(starObject);
    }

    //////////////////////////

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

    @Transactional
    public void updateGraphEnables(@NotNull GraphEnablesPersist graphEnablesPersist) {
        graphEnablesRepository.save(graphEnablesPersist);
    }

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

    public void updateCivilizationDisplayPreferences(@NotNull CivilizationDisplayPreferences preferences) {
        civilizationDisplayPreferencesRepository.save(preferences);
    }

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
    @Transactional
    public void updateStarPreferences(@NotNull StarDisplayPreferences starDisplayPreferences) {
        List<StarDetailsPersist> starDetailsPersistListNew = starDisplayPreferences.getStarDetails();
        starDetailsPersistRepository.saveAll(starDetailsPersistListNew);
    }

    @Transactional
    public void addRouteToDataSet(@NotNull DataSetDescriptor dataSetDescriptor, @NotNull RouteDescriptor routeDescriptor) {

        // pull all routes
        List<Route> routeList = dataSetDescriptor.getRoutes();
        // convert to a Route and add to current list
        routeList.add(routeDescriptor.toRoute());
        // overwrite the list of routes
        dataSetDescriptor.setRoutes(routeList);
        dataSetDescriptorRepository.save(dataSetDescriptor);

    }

    @Transactional
    public void updateNotesOnStar(@NotNull UUID recordId, String notes) {
        Optional<StarObject> objectOptional = starObjectRepository.findById(recordId);
        if (objectOptional.isPresent()) {
            StarObject object = objectOptional.get();
            object.setNotes(notes);
            starObjectRepository.save(object);

            getStar(recordId);
        } else {
            log.error("Attempt to set notes on a non existent star: {}", recordId);
        }
    }

    public StarObject getStar(@NotNull UUID recordId) {
        Optional<StarObject> objectOptional = starObjectRepository.findById(recordId);
        return objectOptional.orElse(null);
    }

    @Transactional
    public void removeStar(@NotNull UUID recordId) {
        starObjectRepository.deleteById(recordId);
    }


    /**
     * does a dataset with this name exist?
     *
     * @param name the dataset name that we are looking for
     * @return true if we found one
     */
    public boolean hasDataSet(String name) {
        return dataSetDescriptorRepository.findByDataSetName(name) != null;
    }


    /**
     * store a bulk of stars
     *
     * @param starObjectList the list of stars to save
     */
    @Transactional
    public void addStars(@NotNull List<StarObject> starObjectList) {
        starObjectRepository.saveAll(starObjectList);
    }

    /**
     * save a large number of stars at once
     *
     * @param starSet the star set
     */
    @Transactional
    public void starBulkSave(@NotNull Set<StarObject> starSet) {
        starObjectRepository.saveAll(starSet);
    }

    /**
     * find a set of stars that match our search term
     *
     * @param datasetName the dataset to search in
     * @param starName    the star name to search
     * @return the list of matching stars
     */
    public @NotNull
    List<StarObject> findStarsWithName(String datasetName, String starName) {
        return starObjectRepository.findByDataSetNameAndDisplayNameContainsIgnoreCase(datasetName, starName);
    }

    @Transactional
    public void saveExcelDataSetDescriptor(@NotNull ProgressUpdater updater, @NotNull ExcelFile excelFile) {
        dataSetDescriptorRepository.save(excelFile.getDescriptor());
        updater.updateTaskInfo("saved descriptor in database, complete");
    }

    public void loadJsonFileSingleDS(ProgressUpdater updater, JsonExportObj jsonExportObj) {
        dataSetDescriptorRepository.save(jsonExportObj.getDescriptor().toDataSetDescriptor());
        updater.updateTaskInfo("saved descriptor in database");
        starObjectRepository.saveAll(jsonExportObj.getStarObjectList());
        updater.updateTaskInfo("saved all stars in database");
    }

    public DataSetDescriptor recheckDescriptor(DataSetDescriptor descriptor, List<StarObject> starObjects) {
        double max = 0;
        long numberStars = 0L;
        for (StarObject star : starObjects) {
            if (star.getDistance() > max) {
                max = star.getDistance();
            }
            numberStars++;
        }
        descriptor.setDistanceRange(max);
        descriptor.setNumberStars(numberStars);
        descriptor.clearRoutes();
        descriptor.resetDate();

        return descriptor;
    }

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

    @Transactional
    public TransitSettings getTransitSettings() {
        Optional<TransitSettings> transitSettingsOptional = transitSettingsRepository.findById("main");

        if (transitSettingsOptional.isPresent()) {
            return transitSettingsOptional.get();
        } else {
            TransitSettings transitSettings = new TransitSettings();
            transitSettings.setId("main");
            transitSettingsRepository.save(transitSettings);
            return transitSettings;
        }
    }

    @Transactional
    public void setTransitSettings(TransitSettings transitSettings) {
        transitSettingsRepository.save(transitSettings);
    }

    @Transactional
    public void saveTripsPrefs(TripsPrefs tripsPrefs) {
        tripsPrefsRepository.save(tripsPrefs);
    }

    @Transactional
    public DataSetDescriptor deleteRoute(String descriptorName, RouteDescriptor routeDescriptor) {
        DataSetDescriptor descriptor = dataSetDescriptorRepository.findByDataSetName(descriptorName);
        List<Route> routeList = descriptor.getRoutes();
        List<Route> updatedRoutes = routeList.stream().filter(route -> !routeDescriptor.getId().equals(route.getUuid())).collect(Collectors.toList());
        descriptor.setRoutes(updatedRoutes);
        dataSetDescriptorRepository.save(descriptor);
        return descriptor;
    }

    @Transactional
    public DataSetDescriptor updateRoute(String descriptorName, RouteDescriptor routeDescriptor) {
        DataSetDescriptor descriptor = dataSetDescriptorRepository.findByDataSetName(descriptorName);
        List<Route> routeList = descriptor.getRoutes();
        for (Route route : routeList) {
            if (route.getUuid().equals(routeDescriptor.getId())) {
                route.setRouteColor(routeDescriptor.getColor().toString());
                route.setRouteName(routeDescriptor.getName());
                route.setRouteNotes(routeDescriptor.getRouteNotes());
            }
        }
        descriptor.setRoutes(routeList);
        dataSetDescriptorRepository.save(descriptor);
        return descriptor;
    }


    @Transactional
    public void updateDataSet(DataSetDescriptor descriptor) {
        Optional<TripsPrefs> tripsPrefsOptional = tripsPrefsRepository.findById("main");
        if (tripsPrefsOptional.isPresent()) {
            TripsPrefs tripsPrefs = tripsPrefsOptional.get();
            tripsPrefs.setDatasetName(descriptor.getDataSetName());
            tripsPrefsRepository.save(tripsPrefs);
        }
    }

    @Transactional
    public void clearRoutesFromCurrent(DataSetDescriptor descriptor) {
        DataSetDescriptor descriptorCurrent = dataSetDescriptorRepository.findByDataSetName(descriptor.getDataSetName());
        descriptorCurrent.clearRoutes();
        dataSetDescriptorRepository.save(descriptorCurrent);
    }

    @Transactional
    public void setTransitPreferences(TransitDefinitions transitDefinitions) {
        DataSetDescriptor descriptorCurrent = dataSetDescriptorRepository.findByDataSetName(transitDefinitions.getDataSetName());
        descriptorCurrent.setTransitDefinitions(transitDefinitions);
        dataSetDescriptorRepository.save(descriptorCurrent);
    }

    public boolean doesDatasetExist(String name) {
        return dataSetDescriptorRepository.existsById(name);
    }

    public DataSetDescriptor changeDatasetName(DataSetDescriptor selectedDataset, String newName) {
        if (dataSetDescriptorRepository.existsById(newName)) {
            return null;
        }
        // get dataset based on name
        DataSetDescriptor descriptor = dataSetDescriptorRepository.findByDataSetName(selectedDataset.getDataSetName());

        // remove old dataset
        dataSetDescriptorRepository.delete(descriptor);

        // save as new
        descriptor.setDataSetName(newName);
        dataSetDescriptorRepository.save(descriptor);
        return descriptor;
    }
}
