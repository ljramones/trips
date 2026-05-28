package com.teamgannon.trips.service;

import com.teamgannon.trips.algorithms.StarMath;
import com.teamgannon.trips.config.application.model.DataSetContext;
import com.teamgannon.trips.dialogs.db.DBReference;
import com.teamgannon.trips.dialogs.search.model.StarDistances;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.DataSetDescriptorRepository;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import com.teamgannon.trips.measure.TrackExecutionTime;
import com.teamgannon.trips.routing.model.SparseStarRecord;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.search.SearchContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.CancellationException;
import java.util.stream.Stream;

@Slf4j
@Service
public class StarService {

    @PersistenceContext
    private EntityManager entityManager;

    private static final int MAX_REQUEST_SIZE = 9999;
    private static final int MAX_PLOT_STARS = 2000;

    private final StarObjectRepository starObjectRepository;

    private final DataSetDescriptorRepository dataSetDescriptorRepository;


    public StarService(StarObjectRepository starObjectRepository,
                       DataSetDescriptorRepository dataSetDescriptorRepository) {
        this.starObjectRepository = starObjectRepository;
        this.dataSetDescriptorRepository = dataSetDescriptorRepository;
    }


    /**
     * get a list of all the data sets
     * @param queryToRun the query to run
     * @return the list of data sets
     */
    @TrackExecutionTime
    public List<StarObject> runNativeQuery(String queryToRun) {
        return runNativeQuery(queryToRun, 0);
    }

    /**
     * Run an advanced native query with an optional row cap.
     *
     * @param queryToRun the query to run
     * @param maxResults maximum rows to return; values <= 0 mean unbounded
     * @return the list of stars returned by the query
     */
    @TrackExecutionTime
    public List<StarObject> runNativeQuery(String queryToRun, int maxResults) {
        Query query = entityManager.createNativeQuery(queryToRun, StarObject.class);
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        List<StarObject> starObjects = query.getResultList();
        int sampleSize = Math.min(50, starObjects.size());
        for (int i = 0; i < sampleSize; i++) {
            StarObject starObject = starObjects.get(i);
            log.info("Advanced query sample {}: name={}, distance={}, x={}, y={}, z={}",
                    i + 1,
                    starObject.getDisplayName(),
                    starObject.getDistance(),
                    starObject.getX(),
                    starObject.getY(),
                    starObject.getZ());
        }
        log.info("number of elements={}", starObjects.size());
        return starObjects;
    }


    /**
     * get a set of astrographic objects based on a query
     *
     * @param searchContext the search context
     * @return the list of objects
     */
    @TrackExecutionTime
    @Transactional(readOnly = true)
    public List<StarObject> getAstrographicObjectsOnQuery(@NotNull SearchContext searchContext) {
        AstroSearchQuery searchQuery = searchContext.getAstroSearchQuery();
        try (Stream<StarObject> starObjectStream = streamForPlot(searchQuery)) {
            PlotStarSelection selection = selectPlotStars(
                    starObjectStream,
                    searchQuery.getCenterCoordinates(),
                    searchQuery.getUpperDistanceLimit()
            );
            if (selection.trimmed()) {
                log.warn("Plot limit reached: trimmed {} stars down to {}", selection.matchedCount(), MAX_PLOT_STARS);
            }
            log.info("Filtered by distance Query returns {} stars", selection.stars().size());
            return selection.stars();
        }
    }

    @TrackExecutionTime
    @Transactional(readOnly = true)
    public List<StarDistances> getAstrographicObjectsOnQuery(@NotNull AstroSearchQuery searchQuery) {
        try (Stream<StarObject> starObjectStream = streamForPlot(searchQuery)) {
            PlotStarDistanceSelection selection = selectPlotStarDistances(
                    starObjectStream,
                    searchQuery.getCenterCoordinates(),
                    searchQuery.getUpperDistanceLimit()
            );
            if (selection.trimmed()) {
                log.warn("Plot limit reached: trimmed {} stars down to {}", selection.matchedCount(), MAX_PLOT_STARS);
            }
            log.info("Filtered by distance Query returns {} stars", selection.starDistances().size());
            return selection.starDistances();
        }
    }

    private @NotNull Stream<StarObject> streamForPlot(@NotNull AstroSearchQuery searchQuery) {
        if (searchQuery.isRecenter()) {
            return starObjectRepository.streamInBoundingBox(
                    searchQuery.getDataSetContext().getDescriptor().getDataSetName(),
                    searchQuery.getXMinus(),
                    searchQuery.getXPlus(),
                    searchQuery.getYMinus(),
                    searchQuery.getYPlus(),
                    searchQuery.getZMinus(),
                    searchQuery.getZPlus()
            );
        }
        return starObjectRepository.findBySearchQueryStream(searchQuery);
    }

    @TrackExecutionTime
    @Transactional(readOnly = true)
    public Page<StarObject> getStarPaged(AstroSearchQuery searchQuery, Pageable pageable) {
        return starObjectRepository.findBySearchQueryPaged(searchQuery, pageable);
    }

    /**
     * count the number of stars matching a search query
     *
     * @param searchQuery the search query
     * @return the count of matching stars
     */
    @TrackExecutionTime
    @Transactional(readOnly = true)
    public long countBySearchQuery(AstroSearchQuery searchQuery) {
        return starObjectRepository.countBySearchQuery(searchQuery);
    }


    private void checkInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            throw new CancellationException("Task cancelled.");
        }
    }

    private @NotNull PlotStarSelection selectPlotStars(@NotNull Stream<StarObject> starObjectStream,
                                                       double[] centerCoordinates,
                                                       double distanceFromCenterStar) {
        PlotStarDistanceSelection selection = selectPlotStarDistances(
                starObjectStream,
                centerCoordinates,
                distanceFromCenterStar
        );
        List<StarObject> stars = selection.starDistances().stream()
                .map(StarDistances::getStarObject)
                .collect(Collectors.toCollection(() -> new ArrayList<>(selection.starDistances().size())));
        return new PlotStarSelection(stars, selection.matchedCount(), selection.trimmed());
    }

    private @NotNull PlotStarDistanceSelection selectPlotStarDistances(@NotNull Stream<StarObject> starObjectStream,
                                                                       double[] centerCoordinates,
                                                                       double distanceFromCenterStar) {
        List<StarDistances> firstMatches = new ArrayList<>(MAX_PLOT_STARS);
        PriorityQueue<StarDistances> nearestStars = null;
        long matchedCount = 0;

        Iterator<StarObject> iterator = starObjectStream.iterator();
        while (iterator.hasNext()) {
            checkInterrupted();
            StarObject object = iterator.next();
            double[] starPosition = new double[]{object.getX(), object.getY(), object.getZ()};
            double distance = StarMath.getDistance(centerCoordinates, starPosition);
            if (distance >= distanceFromCenterStar) {
                continue;
            }

            matchedCount++;
            StarDistances candidate = new StarDistances(object, distance);
            if (nearestStars == null) {
                if (firstMatches.size() < MAX_PLOT_STARS) {
                    firstMatches.add(candidate);
                    continue;
                }

                nearestStars = new PriorityQueue<>(MAX_PLOT_STARS,
                        Comparator.comparingDouble(StarDistances::getDistance).reversed());
                nearestStars.addAll(firstMatches);
                firstMatches.clear();
            }

            if (nearestStars.size() < MAX_PLOT_STARS) {
                nearestStars.add(candidate);
            } else if (candidate.getDistance() < nearestStars.peek().getDistance()) {
                nearestStars.poll();
                nearestStars.add(candidate);
            }
        }

        if (nearestStars == null) {
            return new PlotStarDistanceSelection(firstMatches, matchedCount, false);
        }

        List<StarDistances> limited = new ArrayList<>(nearestStars);
        limited.sort(Comparator.comparingDouble(StarDistances::getDistance));
        return new PlotStarDistanceSelection(limited, matchedCount, true);
    }

    private record PlotStarSelection(List<StarObject> stars, long matchedCount, boolean trimmed) {
    }

    private record PlotStarDistanceSelection(List<StarDistances> starDistances, long matchedCount, boolean trimmed) {
    }


    @TrackExecutionTime
    public List<StarObject> getFromDataset(DataSetDescriptor dataSetDescriptor) {
        // we can only effectively gather 500 at a time
        return toList(
                starObjectRepository.findByDataSetName(
                        dataSetDescriptor.getDataSetName(),
                        PageRequest.of(0, MAX_REQUEST_SIZE)
                )
        );
    }

    @TrackExecutionTime
    public Page<StarObject> getFromDatasetByPage(DataSetDescriptor dataSetDescriptor, int pageNumber, int requestSize) {
        // we can only effectively gather 500 at a time
        return starObjectRepository.findByDataSetName(
                dataSetDescriptor.getDataSetName(),
                PageRequest.of(pageNumber, requestSize)
        );
    }

    @TrackExecutionTime
    @Transactional(readOnly = true)
    public Map<String, SparseStarRecord> getFromDatasetWithinRanges(@NotNull DataSetDescriptor dataSetDescriptor, double distance) {
        final Map<String, SparseStarRecord> starRecordHashMap = new HashMap<>();
        try (Stream<StarObject> starObjectStream = starObjectRepository.streamByDistanceWithin(dataSetDescriptor.getDataSetName(), distance)) {
            starObjectStream.forEach(starObject -> {
                SparseStarRecord sparseStarRecord = starObject.toSparseStarRecord();
                starRecordHashMap.put(sparseStarRecord.getStarName(), sparseStarRecord);
            });
        }

        return starRecordHashMap;
    }

    /**
     * get a count of the number of stars based on a limit
     *
     * @param datasetName the dataset name
     * @param distance    the limit
     * @return the count
     */
    public long getCountOfDatasetWithinLimit(String datasetName, double distance) {
        return starObjectRepository.countByDistanceWithin(datasetName, distance);
    }


    /**
     * return the count of the dataset for export
     *
     * @param datasetName the dataset name
     * @return the count
     */
    public long getCountOfDataset(String datasetName) {
        return starObjectRepository.countByDataSetName(datasetName);
    }

    @TrackExecutionTime
    @Transactional(readOnly = true)
    public List<StarObject> getStarsBasedOnId(List<String> starIdList) {
        return starObjectRepository.findByIdIn(starIdList);
    }

    @TrackExecutionTime
    public List<StarObject> getFromDatasetWithinLimit(@NotNull DataSetDescriptor dataSetDescriptor, double distance) {
        // we can only effectively gather 500 at a time
        return toList(starObjectRepository.findByDistanceLessThan(dataSetDescriptor.getDataSetName(), distance, PageRequest.of(0, MAX_REQUEST_SIZE)));
    }

    /**
     * helper method to return page as list
     *
     * @param pageResult the page result
     * @return the list representation
     */
    @TrackExecutionTime
    private @NotNull
    List<StarObject> toList(@NotNull Page<StarObject> pageResult) {
        return pageResult.getContent();
    }

    /**
     * remove the star from the db
     *
     * @param starObject the astrographic object
     */
    @TrackExecutionTime
    @Transactional
    public void removeStar(@NotNull StarObject starObject) {
        starObjectRepository.delete(starObject);
    }

    /**
     * add a new star
     *
     * @param starObjectNew the star to add
     */
    @TrackExecutionTime
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
    @TrackExecutionTime
    @Transactional
    public void updateStar(@NotNull StarObject starObject) {
        log.info(">>>>updating star={}, name={}, common ={}", starObject.getId(), starObject.getDisplayName(), starObject.getCommonName());
        StarObject object = starObjectRepository.save(starObject);
        if (starObject.getId().equals(object.getId())) {
            log.info("same");
        } else {
            log.error("not same, org={}, new={}", starObject.getId(), object.getId());
        }
    }

    @TrackExecutionTime
    @Transactional
    public void updateNotesOnStar(@NotNull String recordId, String notes) {
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

    @TrackExecutionTime
    public StarObject getStar(@NotNull String recordId) {
        Optional<StarObject> objectOptional = starObjectRepository.findById(recordId);
        return objectOptional.orElse(null);
    }

    /**
     * Get a StarDisplayRecord for a star by its ID.
     *
     * @param recordId the star's record ID
     * @return the StarDisplayRecord, or null if not found
     */
    @TrackExecutionTime
    public StarDisplayRecord getStarDisplayRecord(@NotNull String recordId) {
        StarObject starObject = getStar(recordId);
        if (starObject == null) {
            return null;
        }
        return StarDisplayRecord.fromStarObject(starObject);
    }

    /**
     * Get a StarDisplayRecord for a star by its display name.
     *
     * @param starName the star's display name
     * @return the StarDisplayRecord, or null if not found
     */
    @TrackExecutionTime
    public StarDisplayRecord getStarDisplayRecordByName(@NotNull String starName) {
        StarObject starObject = starObjectRepository.findFirstByDisplayNameIgnoreCase(starName);
        if (starObject == null) {
            return null;
        }
        return StarDisplayRecord.fromStarObject(starObject);
    }

    @TrackExecutionTime
    @Transactional
    public void removeStar(@NotNull String recordId) {
        starObjectRepository.deleteById(recordId);
    }

    /**
     * store a bulk of stars
     *
     * @param starObjectList the list of stars to save
     */
    @TrackExecutionTime
    @Transactional
    public void addStars(@NotNull List<StarObject> starObjectList) {
        starObjectRepository.saveAll(starObjectList);
    }

    /**
     * save a large number of stars at once
     *
     * @param starSet the star set
     */
    /**
     * Bulk save stars to the database.
     * <p>
     * Uses Hibernate batch inserts for better performance, and crucially flushes
     * + clears the persistence context after each call so the first-level cache
     * doesn't accumulate across batches in a long-running import (Phase 1.3).
     * On a 2M-star import this is the difference between ~200 MB peak heap and
     * ~1 GB+ peak heap.
     * <p>
     * When called inside an outer transaction (the typical import path, where
     * {@code BulkLoadService.loadCsvDataset} owns the rollback boundary), the
     * default REQUIRED propagation joins that transaction and the flush+clear
     * stays correctly within its scope — entities written here will still be
     * rolled back if the outer transaction fails.
     *
     * @param stars the collection of stars to save
     */
    @TrackExecutionTime
    @Transactional
    public void starBulkSave(@NotNull java.util.Collection<StarObject> stars) {
        starObjectRepository.saveAll(stars);
        entityManager.flush();
        entityManager.clear();
    }

    @TrackExecutionTime
    @Transactional
    public void updateStars(@NotNull List<StarObject> starObjects) {
        starObjectRepository.saveAll(starObjects);
    }

    @TrackExecutionTime
    public Page<StarObject> findMissingDistanceWithIds(@NotNull String dataSetName, @NotNull Pageable pageable) {
        return starObjectRepository.findMissingDistanceWithIds(dataSetName, pageable);
    }

    @TrackExecutionTime
    public long countMissingDistance(@NotNull String dataSetName) {
        return starObjectRepository.countMissingDistance(dataSetName);
    }

    @TrackExecutionTime
    public Page<StarObject> findMissingDistanceWithPhotometry(@NotNull String dataSetName, @NotNull Pageable pageable) {
        return starObjectRepository.findMissingDistanceWithPhotometry(dataSetName, pageable);
    }

    @TrackExecutionTime
    public long countMissingDistanceWithPhotometry(@NotNull String dataSetName) {
        return starObjectRepository.countMissingDistanceWithPhotometry(dataSetName);
    }

    @TrackExecutionTime
    public Page<StarObject> findMissingMassWithGaiaId(@NotNull String dataSetName, @NotNull Pageable pageable) {
        return starObjectRepository.findMissingMassWithGaiaId(dataSetName, pageable);
    }

    @TrackExecutionTime
    public long countMissingMassWithGaiaId(@NotNull String dataSetName) {
        return starObjectRepository.countMissingMassWithGaiaId(dataSetName);
    }

    @TrackExecutionTime
    public long countMissingMass(@NotNull String dataSetName) {
        return starObjectRepository.countMissingMass(dataSetName);
    }

    @TrackExecutionTime
    public Page<StarObject> findMissingMassWithPhotometry(@NotNull String dataSetName, @NotNull Pageable pageable) {
        return starObjectRepository.findMissingMassWithPhotometry(dataSetName, pageable);
    }

    @TrackExecutionTime
    public long countMissingMassWithPhotometry(@NotNull String dataSetName) {
        return starObjectRepository.countMissingMassWithPhotometry(dataSetName);
    }

    @TrackExecutionTime
    public List<String> findMissingMassWithPhotometryIds(@NotNull String dataSetName) {
        return starObjectRepository.findMissingMassWithPhotometryIds(dataSetName);
    }

    @TrackExecutionTime
    public List<String> findMissingTemperatureWithBprpIds(@NotNull String dataSetName) {
        return starObjectRepository.findMissingTemperatureWithBprpIds(dataSetName);
    }

    @TrackExecutionTime
    public List<String> findMissingSpectralWithBprpIds(@NotNull String dataSetName) {
        return starObjectRepository.findMissingSpectralWithBprpIds(dataSetName);
    }

    @TrackExecutionTime
    public List<String> findMissingSpectralWithTempIds(@NotNull String dataSetName) {
        return starObjectRepository.findMissingSpectralWithTempIds(dataSetName);
    }

    @TrackExecutionTime
    public List<String> findMissingTempWithSpectralIds(@NotNull String dataSetName) {
        return starObjectRepository.findMissingTempWithSpectralIds(dataSetName);
    }

    @TrackExecutionTime
    public List<StarObject> findStarsByIds(@NotNull List<String> ids) {
        List<StarObject> result = new ArrayList<>();
        starObjectRepository.findAllById(ids).forEach(result::add);
        return result;
    }

    /**
     * find a set of stars that match our search term
     *
     * @param datasetName the dataset to search in
     * @param starName    the star name to search
     * @return the list of matching stars
     */
    @TrackExecutionTime
    @Transactional
    public @NotNull
    List<StarObject> findStarsWithName(String datasetName, String starName) {
        return starObjectRepository.findByDisplayNameContaining(datasetName, starName);
    }

    public List<StarObject> findStarWithName(String datasetName, String starName) {
        return starObjectRepository.findByDisplayNameContaining(datasetName, starName);
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

    public List<StarObject> findStarsWithCatalogId(String datasetName, String catalogId) {
        return starObjectRepository.findByCatalogId(datasetName, catalogId);
    }

    public StarObject findStarWithBayerId(String datasetName, String bayerId) {
        return starObjectRepository.findByBayerId(datasetName, bayerId);
    }

    public StarObject findStarWithFlamsteedId(String datasetName, String flamsteedId) {
        return starObjectRepository.findByFlamsteedId(datasetName, flamsteedId);
    }

    public StarObject findStarWithGJId(String datasetName, String gjId) {
        return starObjectRepository.findByGlieseId(datasetName, gjId);
    }

    public StarObject findStarWithHDId(String datasetName, String hdId) {
        return starObjectRepository.findByHdId(datasetName, hdId);
    }

    public StarObject findStarWithHipId(String datasetName, String hipId) {
        return starObjectRepository.findByHipId(datasetName, hipId);
    }

    public StarObject findWithCsiId(String datasetName, String csiId) {
        return starObjectRepository.findByCsiId(datasetName, csiId);
    }

    public StarObject findWithTychoId(String datasetName, String tychoId) {
        return starObjectRepository.findByTychoId(datasetName, tychoId);
    }

    public StarObject findWithTwoMassId(String datasetName, String twoMassId) {
        return starObjectRepository.findByTwoMassId(datasetName, twoMassId);
    }

    public StarObject findWithGaiaDR2Id(String datasetName, String gaiaDr2Id) {
        return starObjectRepository.findByGaiaDR2Id(datasetName, gaiaDr2Id);
    }

    public StarObject findWithGaiaDR3Id(String datasetName, String gaiaDr3Id) {
        return starObjectRepository.findByGaiaDR3Id(datasetName, gaiaDr3Id);
    }

    public StarObject findWithGaiaEDR3Id(String datasetName, String gaiaEdr3Id) {
        return starObjectRepository.findByGaiaEDR3Id(datasetName, gaiaEdr3Id);
    }

    /**
     * find stars by the common name if it exists
     *
     * @param datasetName the dataset name
     * @param commonName  the common name
     * @return the stars that match
     */
    public List<StarObject> findStarsByCommonName(String datasetName, String commonName) {
        return starObjectRepository.findByCommonNameContaining(datasetName, commonName);
    }

    /**
     * find all the stars in a constellation
     *
     * @param constellation the constellation
     * @return the list of stars
     */
    public List<StarObject> findStarsByConstellation(String datasetName, String constellation) {
        return starObjectRepository.findByConstellation(datasetName, constellation);
    }

    /**
     * add an alias list to the star
     *
     * @param starObjectId the id
     * @param aliasList    the list of alias names
     * @return true means we could find the star we want vs false is that we can't find the star
     */
    public boolean addAliasToStar(String starObjectId, Set<String> aliasList) {
        Optional<StarObject> starObjectRetOpt = starObjectRepository.findById(starObjectId);
        if (starObjectRetOpt.isPresent()) {
            StarObject starObjectRet = starObjectRetOpt.get();
            starObjectRet.getAliasList().addAll(aliasList);
            starObjectRepository.save(starObjectRet);
            return true;
        } else {
            log.error("Star: id={} does not exist", starObjectId);
            return false;
        }
    }

    /**
     * add an alias list to the star
     *
     * @param starObject the star to add to
     * @param aliasList  the alias list
     * @return true means we could find the star we want vs false is that we can't find the star
     */
    public boolean addAliasToStar(StarObject starObject, Set<String> aliasList) {
        return addAliasToStar(starObject.getId(), aliasList);
    }

    public Optional<StarObject> findId(String id) {
        return starObjectRepository.findById(id);
    }

    @Transactional
    public List<DBReference> compareStars(String sourceSelection, String targetSelection) {
        List<DBReference> starsNotFound;
        try (Stream<StarObject> starObjectStream = starObjectRepository.findByDataSetName(sourceSelection)) {
            starsNotFound = starObjectStream.filter(starObject ->
                            starObjectRepository.findByDisplayNameContaining(targetSelection, starObject.getDisplayName()).isEmpty())
                    .map(
                            starObject ->
                                    DBReference.builder().id(starObject.getId()).displayName(starObject.getDisplayName()).build())
                    .collect(Collectors.toList());
        }

        return starsNotFound;
    }

    public List<StarDistances> findStarsWithinDistance(String dataSetName, StarObject starObject, Double distanceToSearch) {
        AstroSearchQuery searchQuery = new AstroSearchQuery();
        searchQuery.setRecenter(true);
        searchQuery.setCenterStar(starObject.getDisplayName());
        DataSetDescriptor descriptor = dataSetDescriptorRepository.findByDataSetName(dataSetName);
        searchQuery.setDescriptor(descriptor);
        DataSetContext dataSetContext = new DataSetContext(descriptor);

        searchQuery.setDataSetContext(dataSetContext);
        double[] center = starObject.getCoordinates();
        searchQuery.setCenterCoordinates(center);

        searchQuery.setUpperDistanceLimit(distanceToSearch);

        double xMinus = center[0] - distanceToSearch;
        double xPlus = center[0] + distanceToSearch;
        searchQuery.setXMinus(xMinus);
        searchQuery.setXPlus(xPlus);

        double yMinus = center[1] - distanceToSearch;
        double yPlus = center[1] + distanceToSearch;
        searchQuery.setYMinus(yMinus);
        searchQuery.setYPlus(yPlus);

        double zMinus = center[2] - distanceToSearch;
        double zPlus = center[2] + distanceToSearch;
        searchQuery.setZMinus(zMinus);
        searchQuery.setZPlus(zPlus);

        return getAstrographicObjectsOnQuery(searchQuery);
    }

    // ========== Streaming methods for export ==========

    /**
     * Process all stars in a dataset using streaming.
     * The consumer is called for each star within a single transaction.
     * This ensures constant memory usage regardless of dataset size.
     *
     * @param dataSetName the dataset name
     * @param processor consumer to process each star
     * @return count of stars processed
     */
    @Transactional(readOnly = true)
    public long processDatasetStream(@NotNull String dataSetName, @NotNull java.util.function.Consumer<StarObject> processor) {
        long count = 0;
        try (Stream<StarObject> stream = starObjectRepository.findByDataSetName(dataSetName)) {
            java.util.Iterator<StarObject> iterator = stream.iterator();
            while (iterator.hasNext()) {
                processor.accept(iterator.next());
                count++;
            }
        }
        return count;
    }

    /**
     * Process stars matching a search query using streaming.
     * The consumer is called for each star within a single transaction.
     * This ensures constant memory usage regardless of result size.
     *
     * @param query the search query
     * @param processor consumer to process each star
     * @return count of stars processed
     */
    @Transactional(readOnly = true)
    public long processQueryStream(@NotNull AstroSearchQuery query, @NotNull java.util.function.Consumer<StarObject> processor) {
        long count = 0;
        try (Stream<StarObject> stream = starObjectRepository.findBySearchQueryStream(query)) {
            java.util.Iterator<StarObject> iterator = stream.iterator();
            while (iterator.hasNext()) {
                processor.accept(iterator.next());
                count++;
            }
        }
        return count;
    }

}
