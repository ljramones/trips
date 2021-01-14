package com.teamgannon.trips.service;

import com.opencsv.CSVReader;
import com.teamgannon.trips.algorithms.StarMath;
import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.dataset.factories.DataSetDescriptorFactory;
import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.file.chview.model.ChViewFile;
import com.teamgannon.trips.file.csvin.RBCsvFile;
import com.teamgannon.trips.file.csvin.RegCSVFile;
import com.teamgannon.trips.file.excel.normal.ExcelFile;
import com.teamgannon.trips.file.excel.rb.RBExcelFile;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.jpa.model.*;
import com.teamgannon.trips.jpa.repository.*;
import com.teamgannon.trips.routing.Route;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.export.model.JsonExportObj;
import com.teamgannon.trips.service.importservices.tasks.ProgressUpdater;
import com.teamgannon.trips.service.model.DatabaseImportStatus;
import com.teamgannon.trips.service.model.ParseResult;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


/**
 * Used to manage larger
 * <p>
 * Created by larrymitchell on 2017-01-20.
 */
@Slf4j
@Service
public class DatabaseManagementService {

    private static final int MAX_REQUEST_SIZE = 9999;

    private final StellarSystemRepository stellarSystemRepository;

    private final StarRepository starRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * storage of data sets in DB
     */
    private final DataSetDescriptorRepository dataSetDescriptorRepository;

    /**
     * storage of astrographic objects in DB
     */
    private final AstrographicObjectRepository astrographicObjectRepository;

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
    private TripsPrefsRepository tripsPrefsRepository;

    /**
     * constructor
     *
     * @param stellarSystemRepository      the repo for star systems
     * @param starRepository               the repo for stars
     * @param dataSetDescriptorRepository  the data descriptor repo
     * @param astrographicObjectRepository the astrographic objects
     * @param graphColorsRepository        the graph colors
     * @param graphEnablesRepository       the graph enables
     * @param starDetailsPersistRepository the star details
     */
    public DatabaseManagementService(StellarSystemRepository stellarSystemRepository,
                                     StarRepository starRepository,
                                     DataSetDescriptorRepository dataSetDescriptorRepository,
                                     AstrographicObjectRepository astrographicObjectRepository,
                                     GraphColorsRepository graphColorsRepository,
                                     GraphEnablesRepository graphEnablesRepository,
                                     StarDetailsPersistRepository starDetailsPersistRepository,
                                     CivilizationDisplayPreferencesRepository civilizationDisplayPreferencesRepository,
                                     TripsPrefsRepository tripsPrefsRepository) {

        this.stellarSystemRepository = stellarSystemRepository;
        this.starRepository = starRepository;
        this.dataSetDescriptorRepository = dataSetDescriptorRepository;
        this.astrographicObjectRepository = astrographicObjectRepository;
        this.graphColorsRepository = graphColorsRepository;
        this.graphEnablesRepository = graphEnablesRepository;
        this.starDetailsPersistRepository = starDetailsPersistRepository;
        this.civilizationDisplayPreferencesRepository = civilizationDisplayPreferencesRepository;
        this.tripsPrefsRepository = tripsPrefsRepository;
    }

    /**
     * drop all the tables
     */
    public void dropDatabase() {
        log.info("Dropping database");
        starRepository.deleteAll();
        stellarSystemRepository.deleteAll();
    }

    /**
     * import a data file into the database
     *
     * @param databasefile the database file
     */
    public void importDatabase(@NotNull File databasefile) {
        log.info("attempting to import database file from:" + databasefile.getAbsolutePath());

        DatabaseImportStatus databaseImportStatus = new DatabaseImportStatus();

        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(databasefile.getAbsolutePath()));
            String[] nextLine;

            // skip first line
            reader.readNext();

            List<Star> starList = new ArrayList<>();
            List<StellarSystem> stellarSystemList = new ArrayList<>();

            int index = 1;
            long startTime = System.currentTimeMillis();
            while ((nextLine = reader.readNext()) != null) {
                ParseResult parseResult = saveStarSystemData(nextLine);
                databaseImportStatus.incTotal();
                if (parseResult.isSuccess()) {
                    databaseImportStatus.incRecordsSuccess();
                    starList.add(parseResult.getStar());
                    System.out.println("star name: " + parseResult.getStar().getCommonName() +
                            ", distance:{}" + parseResult.getStar().getDistance());
                    stellarSystemList.add(parseResult.getStellarSystem());
                } else {
                    databaseImportStatus.incRecordsFailed();
                    databaseImportStatus.addId(parseResult.getIdProcessed());
                }

                if (Math.floorMod(index, 10000) == 0) {
                    bulkSave(starList, stellarSystemList);
                    starList.clear();
                    stellarSystemList.clear();
                    log.info("Processing 10000 catalog records");
                }
                index++;
            }
            if (!starList.isEmpty()) {
                bulkSave(starList, stellarSystemList);
                starList.clear();
                stellarSystemList.clear();
            }
            long endTime = System.currentTimeMillis();
            databaseImportStatus.setProcessingTime((endTime - startTime) / 1000);

        } catch (FileNotFoundException e) {
            log.error("failed to open the file name:" + databasefile.getAbsolutePath());
        } catch (IOException e) {
            log.error("failed to read a record");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //  report the results
        log.info(databaseImportStatus.toString());
    }

    /**
     * parse and save a record line
     *
     * @param nextLine the line to parse
     * @return parse result
     */
    private @NotNull ParseResult saveStarSystemData(String[] nextLine) {

        ParseResult result = new ParseResult();
        long catalogId = -1;
        StellarSystem stellarSystem = new StellarSystem();
        Star star = new Star();
        try {
            catalogId = Long.parseLong(nextLine[0]);
            String hip = nextLine[1];
            String hd = nextLine[2];
            String hr = nextLine[3];
            String gl = nextLine[4];
            String bf = nextLine[5];
            String proper = nextLine[6];
            double ra = Double.parseDouble(nextLine[7]);
            double dec = Double.parseDouble(nextLine[8]);
            double dist = Double.parseDouble(nextLine[9]);
            double pmra = Double.parseDouble(nextLine[10]);
            double pmdec = Double.parseDouble(nextLine[11]);
            double rv = Double.parseDouble(nextLine[12]);
            double mag = Double.parseDouble(nextLine[13]);
            double absmag = Double.parseDouble(nextLine[14]);
            String spect = nextLine[15];
            String ci = nextLine[16];
            double x = Double.parseDouble(nextLine[17]);
            double y = Double.parseDouble(nextLine[18]);
            double z = Double.parseDouble(nextLine[19]);
            double vx = Double.parseDouble(nextLine[20]);
            double vy = Double.parseDouble(nextLine[21]);
            double vz = Double.parseDouble(nextLine[22]);
            double rarad = Double.parseDouble(nextLine[23]);
            double decrad = Double.parseDouble(nextLine[24]);
            double pmrarad = Double.parseDouble(nextLine[25]);
            double prdecrad = Double.parseDouble(nextLine[26]);
            String bayer = nextLine[27];
            String flam = nextLine[28];
            String con = nextLine[29];
            String comp = nextLine[30];
            String comp_primary = nextLine[31];
            String base = nextLine[32];
            String lum = nextLine[33];
            String var = nextLine[34];
            String var_min = nextLine[34];
            String var_max = nextLine[35];

            // create the star definition
            star.setCatalogId(catalogId);
            star.setHipparcosId(hip);
            star.setHarvardRevisedId(hd);
            star.setHenryDraperId(hr);
            star.setGlieseId(gl);
            star.setBayerFlamsteed(bf);
            star.setCommonName(proper);
            star.setRa(ra);
            star.setDec(dec);
            star.setDistance(dist);
            star.setProperMotionRightAscension(pmra);
            star.setProperMotionDeclination(pmdec);
            star.setRadialVelocity(rv);
            star.setMagnitude(mag);
            star.setAbsoluteVisualMagnitude(absmag);
            star.setSpectralType(spect);
            star.setColorIndex(ci);
            star.setX(x);
            star.setY(y);
            star.setZ(z);
            star.setXVelocity(vx);
            star.setYVelocity(vy);
            star.setZVelocity(vz);
            star.setRarad(rarad);
            star.setDecrad(decrad);
            star.setPmrarad(pmrarad);
            star.setPrdecrad(prdecrad);
            star.setBayer(bayer);
            star.setFlam(flam);
            star.setConstellation(con);
            star.setComp(comp);
            star.setComp_primary(comp_primary);
            star.setBase(base);
            star.setLum(lum);
            star.setVar(var);
            star.setVar_min(var_min);
            star.setVar_max(var_max);

            UUID systemId = UUID.randomUUID();

            star.setId(systemId.toString());
            star.setStellarSystemId(systemId.toString());

            stellarSystem.addStar(star.getId());
            stellarSystem.setId(systemId.toString());

            result.setSuccess(true);
            result.setIdProcessed(catalogId);
            result.setStar(star);
            result.setStellarSystem(stellarSystem);

        } catch (Exception e) {
            log.error("failed to parse csv record for incoming data file because of " + e);
            log.error("star definition:" + star.toString());
        }
        return result;
    }

    private void bulkSave(@NotNull List<Star> starList, @NotNull List<StellarSystem> stellarSystemList) {

        log.debug("save 1000 star elements and 1000 stellar system elements");
        starRepository.saveAll(starList);

        stellarSystemRepository.saveAll(stellarSystemList);
    }

    public @NotNull DataSetDescriptor loadCHFile(@NotNull ProgressUpdater progressUpdater, @NotNull Dataset dataset, @NotNull ChViewFile chViewFile) throws Exception {

        // this method call actually saves the dataset in elasticsearch
        return DataSetDescriptorFactory.createDataSetDescriptor(
                progressUpdater,
                dataset,
                dataSetDescriptorRepository,
                astrographicObjectRepository,
                chViewFile
        );
    }

    public List<AstrographicObject> runNativeQuery(String queryToRun) {
        Query query = entityManager.createNativeQuery(queryToRun, AstrographicObject.class);
        List<AstrographicObject> astrographicObjects = query.getResultList();
        for (AstrographicObject astrographicObject : astrographicObjects) {
            log.info(astrographicObject.toString());
        }
        log.info("number of elements=" + astrographicObjects.size());
        return astrographicObjects;
    }

    public @NotNull DataSetDescriptor loadRBStarSet(@NotNull RBExcelFile excelFile) throws Exception {

        // this method call actually saves the dataset in elasticsearch
        return DataSetDescriptorFactory.createDataSetDescriptor(
                dataSetDescriptorRepository,
                astrographicObjectRepository,
                excelFile.getAuthor(),
                excelFile
        );
    }

    public @NotNull DataSetDescriptor loadRBCSVStarSet(@NotNull RBCsvFile rbCsvFile) throws Exception {
        // this method call actually saves the dataset in elasticsearch
        return DataSetDescriptorFactory.createDataSetDescriptor(
                dataSetDescriptorRepository,
                rbCsvFile
        );
    }

    public @NotNull DataSetDescriptor loadCSVFile(@NotNull RegCSVFile regCSVFile) throws Exception {
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
    public List<AstrographicObject> getAstrographicObjectsOnQuery(@NotNull SearchContext searchContext) {
        AstroSearchQuery searchQuery = searchContext.getAstroSearchQuery();
        List<AstrographicObject> astrographicObjects;
        if (searchQuery.isRecenter()) {
            astrographicObjects
                    = astrographicObjectRepository.findByDataSetNameAndXGreaterThanAndXLessThanAndYGreaterThanAndYLessThanAndZGreaterThanAndZLessThanOrderByDisplayName(
                    searchQuery.getDescriptor().getDataSetName(),
                    searchQuery.getXMinus(),
                    searchQuery.getXPlus(),
                    searchQuery.getYMinus(),
                    searchQuery.getYPlus(),
                    searchQuery.getZMinus(),
                    searchQuery.getZPlus()
            );

        } else {
            astrographicObjects = astrographicObjectRepository.findBySearchQuery(searchQuery);
        }
        log.info("New DB Query returns {} stars", astrographicObjects.size());
        astrographicObjects = filterByDistance(astrographicObjects, searchQuery.getCenterCoordinates(), searchQuery.getUpperDistanceLimit());
        log.info("Filtered by distance Query returns {} stars", astrographicObjects.size());
        return astrographicObjects;
    }

    /**
     * filter the list to distance by selected distance
     *
     * @param astrographicObjects    the astrogrpic objects to display
     * @param centerCoordinates      the plot center coordinates
     * @param distanceFromCenterStar the distance frm the centre star to display
     * @return the fitlered list
     */
    private @NotNull List<AstrographicObject> filterByDistance(
            @NotNull List<AstrographicObject> astrographicObjects,
            double[] centerCoordinates,
            double distanceFromCenterStar) {
        List<AstrographicObject> filterList = new ArrayList<>();
        astrographicObjects.forEach(object -> {
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
     * this is used to create
     *
     * @param dataSetDescriptor the descriptor to add
     */
    @Transactional
    public void createDataSet(@NotNull DataSetDescriptor dataSetDescriptor) {
        dataSetDescriptorRepository.save(dataSetDescriptor);
    }

    /**
     * remove the data set and associated stars by name
     *
     * @param dataSetName the dataset to remove
     */
    @Transactional
    public void removeDataSet(String dataSetName) {
        DataSetDescriptor descriptor = dataSetDescriptorRepository.findByDataSetName(dataSetName);
        removeDataSet(descriptor);
    }

    /**
     * remove the dataset by descriptor
     *
     * @param descriptor the descriptor to remove
     */
    @Transactional
    public void removeDataSet(@NotNull DataSetDescriptor descriptor) {
        astrographicObjectRepository.deleteByDataSetName(descriptor.getDataSetName());
        dataSetDescriptorRepository.delete(descriptor);
    }

    /**
     * get the data sets
     *
     * @return the list of all descriptors in the database
     */
    public @NotNull List<DataSetDescriptor> getDataSets() {
        Iterable<DataSetDescriptor> dataSetDescriptors = dataSetDescriptorRepository.findAll();
        List<DataSetDescriptor> descriptors = new ArrayList<>();
        dataSetDescriptors.forEach(descriptors::add);
        return descriptors;
    }

    public @NotNull List<AstrographicObject> getFromDataset(@NotNull DataSetDescriptor dataSetDescriptor) {
        // we can only effectively gather 500 at a time
        return astrographicObjectRepository.findByDataSetName(dataSetDescriptor.getDataSetName());
    }

    public @NotNull List<AstrographicObject> getFromDatasetWithinLimit(@NotNull DataSetDescriptor dataSetDescriptor, double distance) {
        // we can only effectively gather 500 at a time
        return toList(astrographicObjectRepository.findByDataSetNameAndDistanceIsLessThanOrderByDisplayName(dataSetDescriptor.getDataSetName(), distance, PageRequest.of(0, MAX_REQUEST_SIZE)));
    }

    public @NotNull DataSetDescriptor getDatasetFromName(String dataSetName) {
        return dataSetDescriptorRepository.findByDataSetName(dataSetName);
    }
    ///////////////

    /**
     * get the star details
     *
     * @return the list of star details
     */
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

    /**
     * update the star details
     *
     * @param starDetailsPersists the list of star details
     */
    @Transactional
    public void updateStarDetails(@NotNull List<StarDetailsPersist> starDetailsPersists) {
        starDetailsPersistRepository.saveAll(starDetailsPersists);
    }

    //////////////

    /**
     * helper method to return page as list
     *
     * @param pageResult the page result
     * @return the list representation
     */
    private @NotNull List<AstrographicObject> toList(@NotNull Page<AstrographicObject> pageResult) {
        return pageResult.getContent();
    }

    /**
     * remove the star from the db
     *
     * @param astrographicObject the astrographic object
     */
    @Transactional
    public void removeStar(@NotNull AstrographicObject astrographicObject) {
        astrographicObjectRepository.delete(astrographicObject);
    }

    /**
     * add a new star
     *
     * @param astrographicObjectNew the star to add
     */
    @Transactional
    public void addStar(@NotNull AstrographicObject astrographicObjectNew) {
        astrographicObjectRepository.save(astrographicObjectNew);
        Optional<AstrographicObject> testGet = astrographicObjectRepository.findById(astrographicObjectNew.getId());
        if (testGet.isEmpty()) {
            log.error("why didn't this save work");
        }
    }


    /**
     * update the star
     *
     * @param astrographicObject the star to update
     */
    @Transactional
    public void updateStar(@NotNull AstrographicObject astrographicObject) {
        astrographicObjectRepository.save(astrographicObject);
    }

    //////////////////////////

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

    public void updateGraphEnables(@NotNull GraphEnablesPersist graphEnablesPersist) {
        graphEnablesRepository.save(graphEnablesPersist);
    }

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

    public @NotNull ColorPalette getGraphColorsFromDB() {
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
    public void updateStarPreferences(@NotNull StarDisplayPreferences starDisplayPreferences) {
        List<StarDetailsPersist> starDetailsPersistListNew = starDisplayPreferences.getStarDetails();
        starDetailsPersistRepository.saveAll(starDetailsPersistListNew);
    }

    public void addRouteToDataSet(@NotNull DataSetDescriptor dataSetDescriptor, @NotNull RouteDescriptor routeDescriptor) {

        // pull all routes
        List<Route> routeList = dataSetDescriptor.getRoutes();
        // convert to a Route and add to current list
        routeList.add(routeDescriptor.toRoute());
        // overwrite the list of routes
        dataSetDescriptor.setRoutes(routeList);
        dataSetDescriptorRepository.save(dataSetDescriptor);

    }

    public void updateNotesOnStar(@NotNull UUID recordId, String notes) {
        Optional<AstrographicObject> objectOptional = astrographicObjectRepository.findById(recordId);
        if (objectOptional.isPresent()) {
            AstrographicObject object = objectOptional.get();
            object.setNotes(notes);
            astrographicObjectRepository.save(object);

            getStar(recordId);
        } else {
            log.error("Attempt to set notes on a non existent star: {}", recordId);
        }
    }

    public AstrographicObject getStar(@NotNull UUID recordId) {
        Optional<AstrographicObject> objectOptional = astrographicObjectRepository.findById(recordId);
        return objectOptional.orElse(null);
    }

    public void removeStar(@NotNull UUID recordId) {
        astrographicObjectRepository.deleteById(recordId);
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
     * @param astrographicObjectList the list of stars to save
     */
    @Transactional
    public void addStars(@NotNull List<AstrographicObject> astrographicObjectList) {
        astrographicObjectRepository.saveAll(astrographicObjectList);
    }

    /**
     * save a large number of stars at once
     *
     * @param starSet the star set
     */
    @Transactional
    public void starBulkSave(@NotNull Set<AstrographicObject> starSet) {
        astrographicObjectRepository.saveAll(starSet);
    }

    /**
     * find a set of stars that match our search term
     *
     * @param datasetName the dataset to search in
     * @param starName    the star name to search
     * @return the list of matching stars
     */
    public @NotNull List<AstrographicObject> findStarsWithName(String datasetName, String starName) {
        return astrographicObjectRepository.findByDataSetNameAndDisplayNameContainsIgnoreCase(datasetName, starName);
    }


    /**
     * save all the stars for a saved standard excel file
     *
     * @param updater   the updaters
     * @param excelFile the excel file we want to load
     */
    public void loadCSVDataSingelDataset(@NotNull ProgressUpdater updater, @NotNull ExcelFile excelFile) {
        dataSetDescriptorRepository.save(excelFile.getDescriptor());
        updater.updateLoadInfo("saved descriptor in database");
        astrographicObjectRepository.saveAll(excelFile.getAstrographicObjects());
        updater.updateLoadInfo("saved all stars in database");
    }

    public void loadJsonFileSingleDS(ProgressUpdater updater, JsonExportObj jsonExportObj) {
        dataSetDescriptorRepository.save(jsonExportObj.getDescriptor().toDataSetDescriptor());
        updater.updateLoadInfo("saved descriptor in database");
        astrographicObjectRepository.saveAll(jsonExportObj.getAstrographicObjectList());
        updater.updateLoadInfo("saved all stars in database");
    }

    public DataSetDescriptor recheckDescriptor(DataSetDescriptor descriptor, List<AstrographicObject> astrographicObjects) {
        double max = 0;
        long numberStars = 0L;
        for (AstrographicObject star : astrographicObjects) {
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

    public void saveTripsPrefs(TripsPrefs tripsPrefs) {
        tripsPrefsRepository.save(tripsPrefs);
    }
}
