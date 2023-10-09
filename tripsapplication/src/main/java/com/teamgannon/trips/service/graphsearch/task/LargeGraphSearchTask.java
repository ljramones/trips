package com.teamgannon.trips.service.graphsearch.task;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.routing.automation.RouteGraph;
import com.teamgannon.trips.routing.model.*;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.service.graphsearch.GraphRouteResult;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LargeGraphSearchTask extends Task<GraphRouteResult> {

    private final DataSetDescriptor currentDataset;
    private final DatabaseManagementService databaseManagementService;
    private final StarService starService;
    private final RouteFindingOptions routeFindingOptions;

    private final AtomicInteger globalCounter = new AtomicInteger(0);

    private final ExecutorService executorService;

    private final Set<String> collisionSet;

    private final List<SparseTransit> sparseTransitList = new ArrayList<>();

    public LargeGraphSearchTask(DataSetDescriptor currentDataset,
                                DatabaseManagementService databaseManagementService,
                                StarService starService,
                                RouteFindingOptions routeFindingOptions) {

        this.currentDataset = currentDataset;

        this.databaseManagementService = databaseManagementService;
        this.starService = starService;
        this.routeFindingOptions = routeFindingOptions;

        Map<String, Integer> collisionMap = new ConcurrentHashMap<>();
        collisionSet = ConcurrentHashMap.newKeySet(collisionMap.size());

        // create a thread pool based on the number of cores on the machine
        executorService = Executors.newFixedThreadPool(getNumCores());
    }

    /**
     * find out how many cores we have
     * this is always problematic as it returns 2x #actual cores due to hyper threading
     *
     * @return number of estimated cores
     */
    private int getNumCores() {
        // we divide by two to get number of physical cores
        int processors = Runtime.getRuntime().availableProcessors();
        // in case processors returns a weird result
        if (processors < 1) {
            processors = 1;
        }
        return processors;
    }


    @Override
    protected GraphRouteResult call() throws Exception {

        GraphRouteResult graphRouteResult = GraphRouteResult.builder().build();

        StarObject origin = routeFindingOptions.getOriginStar();
        StarObject destination = routeFindingOptions.getDestinationStar();
        double lower = routeFindingOptions.getLowerBound();
        double upper = routeFindingOptions.getUpperBound();

        // get a set of stars that match the range request
        Map<String, SparseStarRecord> sparseStarRecordList = getStarRecords(routeFindingOptions);

        // figure out what the allowable transits are
        long startTime = System.currentTimeMillis();
        List<SparseTransit> transitRoutes = calculateTransits(lower, upper, new ArrayList<>(sparseStarRecordList.values()));
        long endTime = System.currentTimeMillis();
        log.info("Metrics: in long route search, transit calculation, time = {}", String.format("%,d", endTime - startTime));
        updateTaskInfo(String.format("Number of transits found is %d", transitRoutes.size()));
        log.info(String.format("Number of transits found is %d", transitRoutes.size()));

        RouteGraph routeGraph = new RouteGraph();
        startTime = System.currentTimeMillis();
        boolean connected = routeGraph.calculateGraphForSparseTransits(transitRoutes);
        endTime = System.currentTimeMillis();
        log.info("Metrics: in long route search, graph connectivity, time = {}", String.format("%,d", endTime - startTime));

        // so there is at least one path between the stars
        if (connected) {

            log.info("Source ({}) and destination ({}) stars have a path", origin.getDisplayName(), destination.getDisplayName());
            updateTaskInfo(String.format("The chosen stars (%s,%s) stars have a path", origin.getDisplayName(), destination.getDisplayName()));
            // find the k shortest paths. We add one because the first is null
            startTime = System.currentTimeMillis();
            List<String> kShortestPaths = routeGraph.findKShortestPaths(
                    origin.getDisplayName(), destination.getDisplayName(), routeFindingOptions.getNumberPaths() + 1);
            kShortestPaths.forEach(System.out::println);
            endTime = System.currentTimeMillis();
            log.info("Metrics: in long route search, get shortest paths, time = {}", String.format("%,d", endTime - startTime));

            PossibleRoutes possibleRoutes = new PossibleRoutes();
            possibleRoutes.setDesiredPath(String.format("Route %s to %s", origin.getDisplayName(), destination.getDisplayName()));

            List<String> pathToPlot = new ArrayList<>(kShortestPaths);

            startTime = System.currentTimeMillis();
            possibleRoutes = createRoutesFromPaths(routeGraph, pathToPlot, routeFindingOptions, sparseStarRecordList);
            log.info("paths are:" + possibleRoutes);
            endTime = System.currentTimeMillis();
            log.info("Metrics: in long route search, create paths form graph, time = {}", String.format("%,d", endTime - startTime));

            graphRouteResult.setRouteFound(true);
            graphRouteResult.setSearchCancelled(false);
            graphRouteResult.setPossibleRoutes(possibleRoutes);
            graphRouteResult.setMessage(String.format("Source (%s) and destination (%s) stars have a path", origin.getDisplayName(), destination.getDisplayName()));

        } else {
            String message = String.format("The chosen stars (%s,%s) have no path to connect them", origin.getDisplayName(), destination.getDisplayName());
            graphRouteResult.setMessage(message);
            graphRouteResult.setRouteFound(false);
            graphRouteResult.setSearchCancelled(false);
            graphRouteResult.setPossibleRoutes(new PossibleRoutes());
            log.info(message);
            updateTaskInfo(message);
        }

        return graphRouteResult;
    }

    private Map<String, SparseStarRecord> getStarRecords(RouteFindingOptions routeFindingOptions) {
        StarObject origin = routeFindingOptions.getOriginStar();
        StarObject destination = routeFindingOptions.getDestinationStar();

        double maxDistance = Math.max(origin.getDistance(), destination.getDistance());

        updateTaskInfo("searching for the star volume between origin and destination");
        long time0 = System.currentTimeMillis();
        Map<String, SparseStarRecord> sparseStarRecordList = starService.getFromDatasetWithinRanges(currentDataset, maxDistance);
        long time1 = System.currentTimeMillis();
        log.info("query took={} ms", time1 - time0);
        updateTaskInfo(String.format("%d stars found in volume in %d ms", sparseStarRecordList.size(), time1 - time0));

        log.info("number of stars = {}", sparseStarRecordList.size());
        return sparseStarRecordList;
    }

    /**
     * create a asynchronous batch to be run for a segment of the transits
     *
     * @param lower        the lower range to check
     * @param upper        the upper range to check
     * @param sourceRecord the start or source record
     * @param starRecords  the list of other stars
     * @return a list of transits that we found for this segment
     */
    public CompletableFuture<List<SparseTransit>> createBatch(double lower, double upper,
                                                              SparseStarRecord sourceRecord,
                                                              List<SparseStarRecord> starRecords) {

        // Run a batch task specified by a Supplier object asynchronously
        CompletableFuture<List<SparseTransit>> future = CompletableFuture.supplyAsync(()
                -> calculateBatchTransit(lower, upper, sourceRecord, starRecords), executorService);

        // add the found transits to the master after complete
        future.thenApplyAsync(result -> sparseTransitList.addAll(result));

        return future;
    }

    /**
     * calculate a transit and reject if not in range
     *
     * @param lower  the lower range value
     * @param upper  the upper range value
     * @param source the source record
     * @param target the target record
     * @return the transit or null if
     */
    public SparseTransit calculateTransit(double lower, double upper, SparseStarRecord source, SparseStarRecord target) {
        if (inSet(source, target)) {
            // we've see this one so reject
            return null;
        }
        double[] sourceCoords = source.getActualCoordinates();
        double x1 = sourceCoords[0];
        double y1 = sourceCoords[1];
        double z1 = sourceCoords[2];
        double[] targetCoords = target.getActualCoordinates();
        double x2 = targetCoords[0];
        double y2 = targetCoords[1];
        double z2 = targetCoords[2];
        double diffx = (x1 - x2);
        double diffy = (y1 - y2);
        double diffz = (z1 - z2);
        double distance = Math.sqrt(diffx * diffx + diffy * diffy + diffz * diffz);

        // if the distance is in range, then create a transit and return, otherwise return a null as a reject.
        // in any case, mark it as seen
        if (inRange(lower, upper, distance)) {
            // mark as done
            collisionSet.add(source.getStarName() + "-" + target.getStarName());
            // return the transit value
            return SparseTransit
                    .builder()
                    .source(source)
                    .target(target)
                    .distance(distance)
                    .build();
        } else {
            // not in range so reject
            return null;
        }

    }

    /**
     * calculate the transients based on range and stars
     *
     * @param lower       the lower range
     * @param upper       the upper range
     * @param starRecords the stars to caluate
     * @return the matching transits
     */
    public List<SparseTransit> calculateTransits(double lower, double upper, List<SparseStarRecord> starRecords) {

        try {
            updateTaskInfo("begin link calculation");
            List<CompletableFuture<List<SparseTransit>>> batchCompleteables = new ArrayList<>();

            int numBatches = starRecords.size();

            // create run batches
            for (int i = 0; i < numBatches; i++) {
                SparseStarRecord sourceRecord = starRecords.get(i);
                CompletableFuture<List<SparseTransit>> batch = createBatch(lower, upper, sourceRecord, starRecords);
                batchCompleteables.add(batch);
            }

            // now connect them all and run
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(batchCompleteables.toArray(new CompletableFuture[0]));

            // wait for completion
            allFutures.get();

            updateTaskInfo("link calculation complete");
            // return the summarization
            return sparseTransitList;
        } catch (InterruptedException | ExecutionException e) {
            log.error("failed due to:" + e.getMessage());
            // return what was done so far if anything
            return sparseTransitList;
        } finally {
            // clear our thread pool
            executorService.shutdown();
        }
    }


    /**
     * calculate a single batch
     *
     * @param lower        the lower range
     * @param upper        the upper range
     * @param sourceRecord the start or source record
     * @param starRecords  the records to compare
     * @return the list of valid transits within the specified range
     */
    private List<SparseTransit> calculateBatchTransit(double lower, double upper,
                                                      SparseStarRecord sourceRecord,
                                                      List<SparseStarRecord> starRecords) {
        List<SparseTransit> sparseTransits = new ArrayList<>();
        for (SparseStarRecord targetRecord : starRecords) {
            SparseTransit transit = calculateTransit(lower, upper, sourceRecord, targetRecord);
            if (transit != null) {
                sparseTransits.add(transit);
            }
        }
        // finished batch
        int count = globalCounter.getAndIncrement();
//        log.info(String.format("completed batch for %s batch number  %d", sourceRecord.getStarName(), count));
        updateTaskInfo(String.format("Batch %d of %d for %s ", count, starRecords.size(), sourceRecord.getStarName()));
        updateProgress(count, starRecords.size());
        return sparseTransits;
    }

    /**
     * don't calculate a transit if we have it's inverse. a to b == b to a
     *
     * @param source the source star
     * @param target the target star
     * @return true if we did this combination already
     */
    private boolean inSet(SparseStarRecord source, SparseStarRecord target) {
        if (collisionSet.contains(source.getStarName() + "-" + target.getStarName())) {
            return true;
        } else return collisionSet.contains(target.getStarName() + "-" + source.getStarName());
    }

    /**
     * check if distance is in specified range
     *
     * @param lower    the lower value of the range
     * @param upper    the upper value of the range
     * @param distance the distance to check in range
     * @return true if in range, false otherwise
     */
    private boolean inRange(double lower, double upper, double distance) {
        return distance >= lower && distance <= upper;
    }

    /**
     * update the task status message to the end user
     *
     * @param message the message to send
     */
    public void updateTaskInfo(String message) {
        updateMessage(message + "  ");
    }


    private PossibleRoutes createRoutesFromPaths(RouteGraph routeGraph, List<String> routePaths, RouteFindingOptions routeFindingOptions, Map<String, SparseStarRecord> sparseStarRecordList) {
        PossibleRoutes possibleRoutes = new PossibleRoutes();
        int i = 0;
        for (String path : routePaths) {
            if (path.contains("null")) {
                // this is a dead path
                continue;
            }

            RoutingMetric routingMetric = createRoute(i, path, routeGraph, sparseStarRecordList);
            possibleRoutes.getRoutes().add(routingMetric);
        }
        return possibleRoutes;
    }

    private RoutingMetric createRoute(int i, String path, RouteGraph routeGraph, Map<String, SparseStarRecord> sparseStarRecordList) {
        RouteDescriptor route = RouteDescriptor.builder().build();
        String extractPath = path.substring(1, path.length() - 1);
        String[] starList = extractPath.split(",");
        route.setNameList(Arrays.asList(starList));

        // set route info
        route.setName(String.format("Route %s to %s, path %d",
                routeFindingOptions.getOriginStar().getDisplayName(),
                routeFindingOptions.getDestinationStar().getDisplayName(),
                ++i));
        route.setRouteNotes(path);
        route.setDescriptor(currentDataset);
        route.setId(UUID.randomUUID());
        route.setLineWidth(routeFindingOptions.getLineWidth());
        setLengths(routeGraph, starList, route);
        route.setColor(routeFindingOptions.getColor());
        route.setMaxLength(starList.length - 1);
        route.setStartStar(starList[0]);

        Map<String, SparseStarRecord> starRecordMap = getStarsForRoute(route, sparseStarRecordList);

        route.setRouteList(getRouteStarList(route, sparseStarRecordList));

        // set routing metric
        return RoutingMetric
                .builder()
                .totalLength(route.getTotalLength())
                .routeDescriptor(route)
                .starRecordMap(starRecordMap)
                .path(path)
                .rank(i)
                .numberOfSegments(route.getNameList().size() - 1)
                .build();
    }

    private List<String> getRouteStarList(RouteDescriptor route, Map<String, SparseStarRecord> sparseStarRecordList) {
        List<String> routeStars = new ArrayList<>();
        for (String name : route.getNameList()) {
            SparseStarRecord sparseStarRecord = sparseStarRecordList.get(name.trim());
            routeStars.add(sparseStarRecord.getRecordId());
        }
        return routeStars;
    }

    private Map<String, SparseStarRecord> getStarsForRoute(RouteDescriptor route, Map<String, SparseStarRecord> sparseStarRecordList) {
        Map<String, SparseStarRecord> starRecordMap = new HashMap<>();
        for (String name : route.getNameList()) {
            SparseStarRecord sparseStarRecord = sparseStarRecordList.get(name.trim());
            starRecordMap.put(name.trim(), sparseStarRecord);
        }
        return starRecordMap;
    }

    private void setLengths(RouteGraph routeGraph, String[] starList, RouteDescriptor routeDescriptor) {
        List<Double> lengthList = new ArrayList<>();
        double totalLength = 0;
        for (int i = 0; i < starList.length - 1; i++) {
            Double length = routeGraph.findEdges(starList[i].trim(), starList[i + 1].trim());
            lengthList.add(length);
            totalLength += length;

        }
        routeDescriptor.setLengthList(lengthList);
        routeDescriptor.setTotalLength(totalLength);
    }

}
