package com.teamgannon.trips.service.graphsearch.task;

import com.github.javafaker.Faker;
import com.teamgannon.trips.routing.model.SparseStarRecord;
import com.teamgannon.trips.routing.model.SparseTransit;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;


@Slf4j
public class SparseTransitComputor {

    static Random random = new Random();

    private final Faker faker = new Faker();

    private final ExecutorService executorService;

    private final Set<String> collisionSet;

    private final List<SparseTransit> sparseTransitList = new ArrayList<>();

    /**
     * the constructor
     */
    public SparseTransitComputor() {
        // create a concurrent hash set from the concurrent hashmap with the original size
        Map<String, Integer> collisionMap = new ConcurrentHashMap<>();
        collisionSet = ConcurrentHashMap.newKeySet(collisionMap.size());

        // create a thread pool based on the number of cores on the machine
        executorService = Executors.newFixedThreadPool(getNumCores());
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

            List<CompletableFuture<List<SparseTransit>>> batchCompleteables = new ArrayList<>();

            // create run batches
            for (int i = 0; i < starRecords.size(); i++) {
                SparseStarRecord sourceRecord = starRecords.get(i);
                CompletableFuture<List<SparseTransit>> batch = createBatch(lower, upper, sourceRecord, starRecords);
                batchCompleteables.add(batch);
            }

            // now connect them all and run
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(batchCompleteables.toArray(new CompletableFuture[0]));

            // wait for completion
            allFutures.get();

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
        return sparseTransits;
    }

    /**
     * find out how many cores we have
     * this is always problematic as it returns 2x #actual cores due to hyper threading
     *
     * @return number of estimated cores
     */
    private int getNumCores() {
        // we divide by two to get number of physical cores
        int processors = Runtime.getRuntime().availableProcessors() / 2;
        // in case processors returns a weird result
        if (processors < 1) {
            processors = 1;
        }
        return processors;
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

    ///////////////// UTILITIES ////////////

    /**
     * generate test stars for the testing
     *
     * @param count the number of star records to create
     * @return the test data to generate
     */
    public List<SparseStarRecord> generateStarRecords(int count) {
        List<SparseStarRecord> list = new ArrayList<>();
        IntStream.range(0, count).mapToDouble(i -> random.nextDouble() * 200 * (random.nextBoolean() ? +1 : -1)).forEach(x -> {
            double y = random.nextDouble() * 200 * (random.nextBoolean() ? +1 : -1);
            double z = random.nextDouble() * 200 * (random.nextBoolean() ? +1 : -1);
            SparseStarRecord starRecord = new SparseStarRecord();
            starRecord.setRecordId(UUID.randomUUID());
            starRecord.setStarName(faker.funnyName().name());
            starRecord.setActualCoordinates(new double[]{x, y, z});
            list.add(starRecord);
        });
        return list;
    }

    ////////////////

    public static void main(String[] args) {
        double lower = 5;
        double upper = 20;
        int count = 100000;

        SparseTransitComputor sparseTransitComputor = new SparseTransitComputor();
        int numCores = sparseTransitComputor.getNumCores();
        System.out.println("Number of cores = " + numCores);
        List<SparseStarRecord> starRecords = sparseTransitComputor.generateStarRecords(count);

        // calculate the transits for the simulated stars
        long start = System.currentTimeMillis();
        List<SparseTransit> transits = sparseTransitComputor.calculateTransits(lower, upper, starRecords);
        long end = System.currentTimeMillis();
        log.info(String.format("Time required for %d stars with %,d possible connections is %d ms gives %,d valid transits",
                count, (long) count * count / 2, end - start, transits.size()));

        log.info("numbers of discovered transits=" + transits.size());

        log.info("Success");

    }

}
