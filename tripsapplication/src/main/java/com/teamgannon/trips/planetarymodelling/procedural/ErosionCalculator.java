package com.teamgannon.trips.planetarymodelling.procedural;

import org.hipparchus.geometry.euclidean.threed.Vector3D;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Post-tectonic erosion pass that simulates geological weathering,
 * creates river systems, and smooths coastlines for realistic planetary surfaces.
 *
 * Runs AFTER ClimateCalculator because rainfall distribution depends on climate zones.
 */
public class ErosionCalculator {

    // ==================== Physical Constants ====================
    // Default values are now in PlanetConfig. These are kept as fallbacks
    // and for documentation purposes.

    /**
     * Polygon count threshold for parallel processing.
     * Below this, single-threaded is faster due to thread overhead.
     * 5000 polygons ≈ Size.LARGE, where parallelism benefits appear.
     */
    private static final int PARALLEL_THRESHOLD = 5000;

    // Instance-level thresholds from config (with Earth-like defaults)
    private final double rainfallThreshold;       // Min rainfall for erosion (default 0.3)
    private final double riverSourceThreshold;    // Min rainfall for river sources (default 0.7)
    private final double riverSourceElevationMin; // Min elevation for river sources (default 0.5)
    private final double erosionCap;              // Max erosion per iteration (default 0.3)
    private final double depositionFactor;        // Fraction deposited (default 0.5)
    private final double riverCarveDepth;         // Max river carve depth (default 0.3)

    // Instance fields
    private final List<Polygon> polygons;
    private final AdjacencyGraph adjacency;
    private final ClimateCalculator.ClimateZone[] climates;
    private final PlanetConfig config;
    private final Random random;
    private final PlateAssigner.PlateAssignment plateAssignment;
    private final BoundaryDetector.BoundaryAnalysis boundaryAnalysis;

    private double[] rainfall;
    private double[] workingHeights;
    private List<List<Integer>> rivers;
    private List<Boolean> frozenTerminus;  // Track if each river ends frozen
    private boolean[] nearDivergentBoundary;  // Cache for boundary proximity

    /**
     * Result of erosion calculations.
     */
    public record ErosionResult(
        int[] erodedHeights,           // Modified heights after erosion (integer, for compatibility)
        double[] preciseHeights,       // High-precision heights for smooth rendering
        List<List<Integer>> rivers,    // River paths (polygon index sequences)
        double[] rainfall,             // Rainfall distribution (for visualization)
        boolean[] frozenRiverTerminus  // True if river ends frozen (polar) vs flowing (ocean)
    ) {
        /**
         * Returns true if precise heights are available for smooth rendering.
         */
        public boolean hasPreciseHeights() {
            return preciseHeights != null && preciseHeights.length > 0;
        }

        /**
         * Returns true if the specified river ends frozen (in polar zone) rather than
         * flowing into the ocean. Useful for visualization (ice vs water terminus).
         */
        public boolean isRiverFrozen(int riverIndex) {
            if (frozenRiverTerminus == null || riverIndex < 0 || riverIndex >= frozenRiverTerminus.length) {
                return false;
            }
            return frozenRiverTerminus[riverIndex];
        }
    }

    private ErosionCalculator(
            int[] heights,
            List<Polygon> polygons,
            AdjacencyGraph adjacency,
            ClimateCalculator.ClimateZone[] climates,
            PlanetConfig config,
            PlateAssigner.PlateAssignment plateAssignment,
            BoundaryDetector.BoundaryAnalysis boundaryAnalysis) {
        this(convertToDouble(heights), polygons, adjacency, climates, config, plateAssignment, boundaryAnalysis);
    }

    private ErosionCalculator(
            double[] heights,
            List<Polygon> polygons,
            AdjacencyGraph adjacency,
            ClimateCalculator.ClimateZone[] climates,
            PlanetConfig config,
            PlateAssigner.PlateAssignment plateAssignment,
            BoundaryDetector.BoundaryAnalysis boundaryAnalysis) {
        this.polygons = polygons;
        this.adjacency = adjacency;
        this.climates = climates;
        this.config = config;
        this.random = new Random(config.subSeed(4));  // Erosion uses seed phase 4
        this.rivers = new ArrayList<>();
        this.plateAssignment = plateAssignment;
        this.boundaryAnalysis = boundaryAnalysis;

        // Initialize erosion thresholds from config
        this.rainfallThreshold = config.rainfallThreshold();
        this.riverSourceThreshold = config.riverSourceThreshold();
        this.riverSourceElevationMin = config.riverSourceElevationMin();
        this.erosionCap = config.erosionCap();
        this.depositionFactor = config.depositionFactor();
        this.riverCarveDepth = config.riverCarveDepth();

        // Use continuous heights directly for erosion calculations
        this.workingHeights = new double[heights.length];
        System.arraycopy(heights, 0, workingHeights, 0, heights.length);

        // Pre-calculate divergent boundary proximity
        this.nearDivergentBoundary = calculateDivergentProximity();
    }

    /**
     * Main entry point for erosion calculation.
     */
    public static ErosionResult calculate(
            int[] heights,
            List<Polygon> polygons,
            AdjacencyGraph adjacency,
            ClimateCalculator.ClimateZone[] climates,
            PlanetConfig config) {
        // Call overload with null plate data (backward compatible)
        return calculate(heights, polygons, adjacency, climates, config, null, null);
    }

    /**
     * Main entry point for erosion calculation with continuous heights.
     */
    public static ErosionResult calculate(
            double[] heights,
            List<Polygon> polygons,
            AdjacencyGraph adjacency,
            ClimateCalculator.ClimateZone[] climates,
            PlanetConfig config,
            PlateAssigner.PlateAssignment plateAssignment,
            BoundaryDetector.BoundaryAnalysis boundaryAnalysis) {

        ErosionCalculator calculator = new ErosionCalculator(
            heights, polygons, adjacency, climates, config, plateAssignment, boundaryAnalysis);

        return calculator.runErosion();
    }

    /**
     * Main entry point for erosion calculation with plate boundary awareness.
     * Divergent boundaries boost rainfall due to upwelling moisture.
     */
    public static ErosionResult calculate(
            int[] heights,
            List<Polygon> polygons,
            AdjacencyGraph adjacency,
            ClimateCalculator.ClimateZone[] climates,
            PlanetConfig config,
            PlateAssigner.PlateAssignment plateAssignment,
            BoundaryDetector.BoundaryAnalysis boundaryAnalysis) {

        ErosionCalculator calculator = new ErosionCalculator(
            heights, polygons, adjacency, climates, config, plateAssignment, boundaryAnalysis);

        return calculator.runErosion();
    }

    private ErosionResult runErosion() {
        // Phase 1: Calculate rainfall distribution
        calculateRainfall();

        // Phase 2: Apply sediment flow erosion (cellular automata)
        applySedimentFlow();

        // Phase 3: Carve river valleys (if enabled)
        if (config.enableRivers()) {
            carveRivers();
        }

        // Phase 4: Smooth coastlines
        smoothCoastlines();

        if (config.useContinuousHeights()) {
            normalizeWorkingHeights();
        }

        // Create precise heights copy (clamped but not rounded)
        double[] preciseHeights = new double[workingHeights.length];
        for (int i = 0; i < workingHeights.length; i++) {
            preciseHeights[i] = Math.max(ElevationCalculator.DEEP_OCEAN,
                Math.min(ElevationCalculator.HIGH_MOUNTAINS, workingHeights[i]));
        }

        // Convert to integer heights for compatibility
        int[] finalHeights = new int[workingHeights.length];
        for (int i = 0; i < workingHeights.length; i++) {
            finalHeights[i] = (int) Math.round(preciseHeights[i]);
        }

        // Convert frozen terminus list to array
        boolean[] frozenArray = null;
        if (frozenTerminus != null && !frozenTerminus.isEmpty()) {
            frozenArray = new boolean[frozenTerminus.size()];
            for (int i = 0; i < frozenTerminus.size(); i++) {
                frozenArray[i] = frozenTerminus.get(i);
            }
        }

        return new ErosionResult(finalHeights, preciseHeights, rivers, rainfall, frozenArray);
    }

    /**
     * Phase 1: Calculate rainfall distribution based on climate zones.
     * Includes rain shadow effect where mountains block prevailing winds.
     */
    private void calculateRainfall() {
        rainfall = new double[polygons.size()];
        double rainfallScale = config.rainfallScale();

        // For stagnant-lid worlds (Venus/Mars-like), reduce rainfall significantly
        double stagnantLidFactor = config.enableActiveTectonics() ? 1.0 : 0.3;

        // Pre-calculate rain shadow factors for all polygons
        double[] rainShadowFactors = calculateRainShadowFactors();

        if (polygons.size() >= PARALLEL_THRESHOLD) {
            // Parallel processing for large meshes
            // Use seeded random per-polygon for reproducibility
            long baseSeed = config.subSeed(4);
            IntStream.range(0, polygons.size()).parallel().forEach(i -> {
                Random localRandom = new Random(baseSeed + i);
                rainfall[i] = calculateRainfallForPolygon(i, localRandom, rainfallScale,
                    stagnantLidFactor, rainShadowFactors[i]);
            });
        } else {
            // Sequential processing for small meshes
            for (int i = 0; i < polygons.size(); i++) {
                rainfall[i] = calculateRainfallForPolygon(i, random, rainfallScale,
                    stagnantLidFactor, rainShadowFactors[i]);
            }
        }
    }

    /**
     * Calculate rain shadow factors for all polygons.
     * Uses simplified global wind circulation:
     * - Trade winds (0-30° lat): blow from east
     * - Westerlies (30-60° lat): blow from west
     * - Polar easterlies (60-90° lat): blow from east
     *
     * @return Array of factors (0.2 to 1.0) where lower = more shadow
     */
    private double[] calculateRainShadowFactors() {
        double[] factors = new double[polygons.size()];

        if (polygons.size() >= PARALLEL_THRESHOLD) {
            // Parallel processing for large meshes
            IntStream.range(0, polygons.size()).parallel().forEach(i -> {
                factors[i] = calculateRainShadowForPolygon(i);
            });
        } else {
            // Sequential processing for small meshes
            for (int i = 0; i < polygons.size(); i++) {
                factors[i] = calculateRainShadowForPolygon(i);
            }
        }

        return factors;
    }

    /**
     * Calculate rain shadow factor for a single polygon.
     */
    private double calculateRainShadowForPolygon(int i) {
        Vector3D center = polygons.get(i).center();

        // Calculate latitude (Y is up in our coordinate system)
        double lat = Math.toDegrees(Math.asin(center.normalize().getY()));

        // Determine prevailing wind direction based on latitude
        Vector3D windDir = getPrevailingWindDirection(lat, center);

        // Trace upwind looking for blocking terrain
        double shadowAmount = traceUpwindForMountains(i, windDir);

        // Convert shadow amount to factor (more shadow = less rain)
        // shadowAmount of 0 = no shadow (factor 1.0)
        // shadowAmount of 1 = full shadow (factor 0.2)
        return Math.max(0.2, 1.0 - shadowAmount * 0.8);
    }

    /**
     * Get prevailing wind direction based on latitude.
     * Simplified global circulation model:
     * - 0-30° latitude: Trade winds (easterly, from the east)
     * - 30-60° latitude: Westerlies (from the west)
     * - 60-90° latitude: Polar easterlies (from the east)
     */
    private Vector3D getPrevailingWindDirection(double latitude, Vector3D position) {
        double absLat = Math.abs(latitude);

        // Calculate local "east" direction (tangent to surface, perpendicular to radius)
        Vector3D up = position.normalize();
        Vector3D north = new Vector3D(0, 1, 0);
        Vector3D east = north.crossProduct(up).normalize();

        // If we're near poles, east direction becomes degenerate
        if (east.getNorm() < 0.1) {
            east = new Vector3D(1, 0, 0);
        }

        // Determine wind direction based on latitude zone
        if (absLat < 30) {
            // Trade winds: blow FROM the east (so wind direction vector points west)
            return east.negate();
        } else if (absLat < 60) {
            // Westerlies: blow FROM the west (so wind direction vector points east)
            return east;
        } else {
            // Polar easterlies: blow FROM the east
            return east.negate();
        }
    }

    /**
     * Trace upwind from a polygon looking for blocking mountain terrain.
     * Returns a shadow amount from 0 (no shadow) to 1 (full shadow).
     */
    private double traceUpwindForMountains(int startIdx, Vector3D windDir) {
        double maxBlockingHeight = 0;
        double startHeight = workingHeights[startIdx];

        // Follow the wind direction through neighbor polygons (up to 5 steps)
        int current = startIdx;
        Set<Integer> visited = new HashSet<>();

        for (int step = 0; step < 5 && current >= 0; step++) {
            visited.add(current);

            // Find neighbor most aligned with upwind direction
            int bestNeighbor = -1;
            double bestAlignment = -1;

            for (int neighbor : adjacency.neighborsOnly(current)) {
                if (visited.contains(neighbor)) continue;

                Vector3D neighborDir = polygons.get(neighbor).center()
                    .subtract(polygons.get(current).center()).normalize();

                // Dot product gives alignment (-1 to 1)
                // We want the neighbor that is most OPPOSITE to wind direction (upwind)
                double alignment = -neighborDir.dotProduct(windDir);

                if (alignment > bestAlignment) {
                    bestAlignment = alignment;
                    bestNeighbor = neighbor;
                }
            }

            if (bestNeighbor < 0 || bestAlignment < 0.3) {
                break;  // No good upwind neighbor found
            }

            // Check if this upwind polygon is elevated terrain
            double neighborHeight = workingHeights[bestNeighbor];
            if (neighborHeight > startHeight && neighborHeight > 0) {
                // This is elevated land upwind - contributes to shadow
                double blockingAmount = (neighborHeight - startHeight) / 4.0;  // Normalize by max height diff
                maxBlockingHeight = Math.max(maxBlockingHeight, blockingAmount);
            }

            current = bestNeighbor;
        }

        return Math.min(1.0, maxBlockingHeight);
    }

    /**
     * Calculate rainfall for a single polygon.
     */
    private double calculateRainfallForPolygon(int i, Random rng, double rainfallScale,
            double stagnantLidFactor, double rainShadowFactor) {
        // Base rain from climate zone
        double baseRain = switch (climates[i]) {
            case TROPICAL -> 1.0 + rng.nextDouble() * 0.5;   // 1.0-1.5
            case TEMPERATE -> 0.5 + rng.nextDouble() * 0.5;  // 0.5-1.0
            case POLAR -> 0.1 + rng.nextDouble() * 0.2;      // 0.1-0.3
        };

        // Reduce rain at high elevations (orographic effect on peaks)
        // Normalize height to 0-1 range (heights range from -4 to 4)
        double normalizedHeight = (workingHeights[i] + 4.0) / 8.0;
        double elevationFactor = 1.0 - (normalizedHeight * 0.3);  // 30% reduction at max height

        // Boost rainfall near divergent plate boundaries (upwelling moisture)
        double divergentBoost = 1.0;
        if (nearDivergentBoundary != null && nearDivergentBoundary[i]) {
            divergentBoost = 1.3;  // 30% boost near divergent boundaries
        }

        // Apply rain shadow effect (can significantly reduce rainfall on leeward slopes)
        return baseRain * Math.max(0.1, elevationFactor) * rainfallScale
            * stagnantLidFactor * divergentBoost * rainShadowFactor;
    }

    /**
     * Pre-calculate which polygons are near divergent plate boundaries.
     * Returns null if plate data is not available.
     */
    private boolean[] calculateDivergentProximity() {
        if (plateAssignment == null || boundaryAnalysis == null) {
            return null;
        }

        boolean[] nearDivergent = new boolean[polygons.size()];
        int[] plateIndex = plateAssignment.plateIndex();
        var boundaries = boundaryAnalysis.boundaries();

        // First pass: mark polygons directly on divergent boundaries
        for (int i = 0; i < polygons.size(); i++) {
            int plate = plateIndex[i];

            // Check if any neighbor is on a different plate with divergent boundary
            for (int neighbor : adjacency.neighborsOnly(i)) {
                int neighborPlate = plateIndex[neighbor];
                if (neighborPlate != plate) {
                    BoundaryDetector.PlatePair pair = new BoundaryDetector.PlatePair(
                        Math.min(plate, neighborPlate),
                        Math.max(plate, neighborPlate)
                    );
                    BoundaryDetector.BoundaryType boundaryType = boundaries.get(pair);
                    if (boundaryType == BoundaryDetector.BoundaryType.DIVERGENT) {
                        nearDivergent[i] = true;
                        break;
                    }
                }
            }
        }

        // Second pass: extend to immediate neighbors of divergent boundary polygons
        boolean[] extended = nearDivergent.clone();
        for (int i = 0; i < polygons.size(); i++) {
            if (nearDivergent[i]) {
                for (int neighbor : adjacency.neighborsOnly(i)) {
                    extended[neighbor] = true;
                }
            }
        }

        return extended;
    }

    /**
     * Phase 2: Sediment flow erosion using mass-conserving sediment transport.
     * Simulates water carrying sediment from high areas to low areas.
     *
     * Uses a carrying capacity model where:
     * - Water can carry sediment proportional to slope and flow rate
     * - Excess capacity erodes the terrain (picks up sediment)
     * - Over-capacity deposits sediment (when slope decreases)
     * - All sediment is eventually deposited (mass conservation)
     */
    private void applySedimentFlow() {
        int iterations = config.erosionIterations();

        // For stagnant-lid worlds, use fewer iterations
        if (!config.enableActiveTectonics()) {
            iterations = Math.max(1, iterations / 2);
        }

        // Track sediment being carried at each polygon
        double[] sediment = new double[polygons.size()];

        for (int iter = 0; iter < iterations; iter++) {
            // Process polygons in height order (highest to lowest) for proper flow
            Integer[] sortedByHeight = getSortedByHeightDescending();

            // Reset sediment for this iteration
            Arrays.fill(sediment, 0.0);

            for (int i : sortedByHeight) {
                // Skip polygons with minimal rainfall
                if (rainfall[i] < rainfallThreshold) continue;

                // Find lowest neighbor for flow direction
                int lowestNeighbor = findLowestNeighbor(i);
                if (lowestNeighbor < 0) continue;

                // Calculate slope to downstream neighbor
                double slope = workingHeights[i] - workingHeights[lowestNeighbor];
                if (slope <= 0) continue;

                // Calculate water carrying capacity based on slope and rainfall
                // Steeper slopes and more water = higher capacity
                double carryingCapacity = Math.min(erosionCap, slope * 0.3 * rainfall[i]);

                // Current sediment load
                double currentLoad = sediment[i];

                if (currentLoad < carryingCapacity) {
                    // Under capacity: erode terrain to pick up more sediment
                    double erosionPotential = carryingCapacity - currentLoad;
                    double erosionAmount = Math.min(erosionPotential, slope * 0.15);

                    // Erode source polygon
                    workingHeights[i] -= erosionAmount;
                    currentLoad += erosionAmount;
                }

                // Calculate new carrying capacity at downstream location
                int nextLowest = findLowestNeighbor(lowestNeighbor);
                double downstreamSlope = 0;
                if (nextLowest >= 0) {
                    downstreamSlope = Math.max(0, workingHeights[lowestNeighbor] - workingHeights[nextLowest]);
                }
                double downstreamCapacity = Math.min(erosionCap, downstreamSlope * 0.3 * rainfall[lowestNeighbor]);

                // If downstream capacity is lower, deposit excess sediment
                if (currentLoad > downstreamCapacity) {
                    double toDeposit = (currentLoad - downstreamCapacity) * depositionFactor;
                    workingHeights[lowestNeighbor] += toDeposit;
                    currentLoad -= toDeposit;
                }

                // Pass remaining sediment downstream
                sediment[lowestNeighbor] += currentLoad;
            }

            // Deposit any remaining sediment at lowest points (ocean, basins)
            for (int i = 0; i < polygons.size(); i++) {
                if (sediment[i] > 0) {
                    int lowest = findLowestNeighbor(i);
                    if (lowest < 0 || workingHeights[lowest] >= workingHeights[i]) {
                        // No lower neighbor - deposit all remaining sediment here
                        workingHeights[i] += sediment[i];
                    }
                }
            }
        }

        // Apply smoothing pass to reduce sharp edges
        smoothHeights();
    }

    /**
     * Get polygon indices sorted by height (descending - highest first).
     * Used for processing erosion in proper flow order.
     * Uses parallel sorting for large meshes.
     */
    private Integer[] getSortedByHeightDescending() {
        Integer[] indices = new Integer[polygons.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }

        if (polygons.size() >= PARALLEL_THRESHOLD) {
            // Use parallel sort for large meshes
            Arrays.parallelSort(indices, (a, b) -> Double.compare(workingHeights[b], workingHeights[a]));
        } else {
            Arrays.sort(indices, (a, b) -> Double.compare(workingHeights[b], workingHeights[a]));
        }
        return indices;
    }

    /**
     * Phase 3: Carve river valleys from high-rainfall mountain sources to ocean.
     */
    private void carveRivers() {
        rivers = new ArrayList<>();
        frozenTerminus = new ArrayList<>();

        // Find potential river sources (high rainfall, positive elevation)
        // Copy to mutable list since findRiverSources may return immutable list
        List<Integer> sources = new ArrayList<>(findRiverSources());

        // Shuffle sources to add variety
        Collections.shuffle(sources, random);

        // Limit number of rivers based on planet size
        int maxRivers = Math.max(3, config.polyCount() / 500);
        int riverCount = 0;

        for (int source : sources) {
            if (riverCount >= maxRivers) break;

            List<Integer> path = traceRiverPath(source);

            // Only count significant rivers (more than 2 segments)
            if (path.size() > 2) {
                rivers.add(path);
                riverCount++;

                // Check if river ends frozen (in polar zone) or flowing (in ocean)
                int terminus = path.get(path.size() - 1);
                boolean isFrozen = climates[terminus] == ClimateCalculator.ClimateZone.POLAR
                    && workingHeights[terminus] > 0;  // Land, not ocean
                frozenTerminus.add(isFrozen);

                // Carve the river valley. Frozen rivers (polar termini) erode
                // at half rate since ice flows slower than liquid water.
                double carveMultiplier = isFrozen ? 0.5 : 1.0;
                for (int i = 0; i < path.size(); i++) {
                    int polyIdx = path.get(i);
                    // Deeper carving near source (steeper V-valley), shallower
                    // near mouth (wide floodplain). Factor ranges 1.0→0.0.
                    double carveFactor = 1.0 - (double) i / path.size();
                    // Maximum carve depth is 0.3 height units at source.
                    // This creates visible valleys without over-carving the terrain.
                    workingHeights[polyIdx] -= riverCarveDepth * carveFactor * carveMultiplier;
                }

                // Create delta at river mouth (sediment deposit) - only for flowing rivers
                if (!isFrozen) {
                    if (workingHeights[terminus] <= 0) {
                        workingHeights[terminus] += 0.2;
                    }
                }
            }
        }
    }

    /**
     * Trace a river path from source downhill until reaching ocean, dead end, or polar zone.
     * Rivers freeze when entering polar regions (ice world handling).
     */
    private List<Integer> traceRiverPath(int source) {
        List<Integer> path = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        int current = source;

        while (workingHeights[current] > -1 && !visited.contains(current)) {
            visited.add(current);
            path.add(current);

            // Ice world handling: river freezes when entering polar zone
            // (unless it started there, which is already blocked by isRiverSource)
            if (climates[current] == ClimateCalculator.ClimateZone.POLAR && path.size() > 1) {
                break;  // River freezes at polar terminus
            }

            int next = findLowestNeighbor(current);
            if (next < 0 || workingHeights[next] >= workingHeights[current]) {
                break;  // Dead end or uphill
            }

            current = next;
        }

        // Add final polygon if it's water (river mouth)
        if (!visited.contains(current) && workingHeights[current] < 0) {
            path.add(current);
        }

        return path;
    }

    /**
     * Find polygons suitable as river sources.
     */
    private List<Integer> findRiverSources() {
        if (polygons.size() >= PARALLEL_THRESHOLD) {
            // Parallel source finding for large meshes
            return IntStream.range(0, polygons.size())
                .parallel()
                .filter(this::isRiverSource)
                .boxed()
                .toList();
        } else {
            List<Integer> sources = new ArrayList<>();
            for (int i = 0; i < polygons.size(); i++) {
                if (isRiverSource(i)) {
                    sources.add(i);
                }
            }
            return sources;
        }
    }

    /**
     * Check if a polygon is suitable as a river source.
     */
    private boolean isRiverSource(int i) {
        // Source needs high rainfall and elevated terrain
        if (rainfall[i] <= riverSourceThreshold ||
            workingHeights[i] <= riverSourceElevationMin) {
            return false;
        }

        // Ice world handling: polar zones are too cold for liquid water rivers
        // Rivers freeze at their source in polar regions
        if (climates[i] == ClimateCalculator.ClimateZone.POLAR) {
            return false;
        }

        // Prefer polygons that are local maxima or near them
        for (int neighbor : adjacency.neighborsOnly(i)) {
            if (workingHeights[neighbor] > workingHeights[i] + 0.5) {
                return false;
            }
        }

        return true;
    }

    /**
     * Phase 4: Smooth coastlines with fractal noise.
     */
    private void smoothCoastlines() {
        // Skip for stagnant-lid worlds (minimal erosion)
        if (!config.enableActiveTectonics()) {
            // Just minimal smoothing for stagnant-lid
            for (int iter = 0; iter < 1; iter++) {
                smoothCoastalPass();
            }
            return;
        }

        // Apply Perlin-like noise to coastlines
        for (int i = 0; i < polygons.size(); i++) {
            if (!isCoastalPolygon(i)) continue;

            Vector3D center = polygons.get(i).center();
            double noise = simplexNoise(center.getX(), center.getY(), center.getZ());
            workingHeights[i] += noise * 0.4 - 0.2;  // ±0.2 adjustment
        }

        // Smooth coastal neighbors (multiple passes)
        for (int iter = 0; iter < 2; iter++) {
            smoothCoastalPass();
        }
    }

    /**
     * One pass of coastal smoothing - averages heights with neighbors.
     */
    private void smoothCoastalPass() {
        double[] newHeights = workingHeights.clone();

        for (int i = 0; i < polygons.size(); i++) {
            if (!isCoastalPolygon(i)) continue;
            newHeights[i] = averageWithNeighbors(i, 0.3);  // 30% neighbor influence
        }

        workingHeights = newHeights;
    }

    /**
     * Determines if a polygon is on the coastline (land adjacent to water).
     */
    private boolean isCoastalPolygon(int idx) {
        // Must be near sea level
        if (workingHeights[idx] > 0.5 || workingHeights[idx] < -0.5) {
            return false;
        }

        // Check if adjacent to water/land boundary
        boolean hasWaterNeighbor = false;
        boolean hasLandNeighbor = false;

        for (int neighbor : adjacency.neighborsOnly(idx)) {
            if (workingHeights[neighbor] < 0) {
                hasWaterNeighbor = true;
            } else {
                hasLandNeighbor = true;
            }
        }

        return hasWaterNeighbor && hasLandNeighbor;
    }

    /**
     * Apply Gaussian smoothing to heights to reduce sharp edges.
     */
    private void smoothHeights() {
        double[] newHeights = workingHeights.clone();

        if (polygons.size() >= PARALLEL_THRESHOLD) {
            // Parallel smoothing for large meshes
            IntStream.range(0, polygons.size()).parallel().forEach(i -> {
                newHeights[i] = averageWithNeighbors(i, 0.2);
            });
        } else {
            for (int i = 0; i < polygons.size(); i++) {
                newHeights[i] = averageWithNeighbors(i, 0.2);  // 20% neighbor influence
            }
        }

        workingHeights = newHeights;
    }

    /**
     * Calculate weighted average of height with neighbors.
     * @param idx polygon index
     * @param neighborWeight weight given to neighbors (0-1), remainder goes to current
     */
    private double averageWithNeighbors(int idx, double neighborWeight) {
        int[] neighbors = adjacency.neighborsOnly(idx);
        if (neighbors.length == 0) {
            return workingHeights[idx];
        }

        double neighborSum = 0;
        for (int neighbor : neighbors) {
            neighborSum += workingHeights[neighbor];
        }
        double neighborAvg = neighborSum / neighbors.length;

        return workingHeights[idx] * (1 - neighborWeight) + neighborAvg * neighborWeight;
    }

    /**
     * Find the lowest neighbor of a polygon.
     * @return neighbor index, or -1 if no valid neighbors
     */
    private int findLowestNeighbor(int idx) {
        int[] neighbors = adjacency.neighborsOnly(idx);
        int lowestIdx = -1;
        double lowestHeight = Double.MAX_VALUE;

        for (int neighbor : neighbors) {
            if (workingHeights[neighbor] < lowestHeight) {
                lowestHeight = workingHeights[neighbor];
                lowestIdx = neighbor;
            }
        }

        return lowestIdx;
    }

    /**
     * Create an array of indices in shuffled order.
     */
    private int[] createShuffledOrder() {
        int[] order = new int[polygons.size()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        shuffleArray(order);
        return order;
    }

    /**
     * Fisher-Yates shuffle for int array.
     */
    private void shuffleArray(int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    /**
     * Simple 3D simplex-like noise function for coastal variation.
     * Uses a pseudo-random approach based on position.
     */
    private double simplexNoise(double x, double y, double z) {
        // Simple hash-based noise (not true Simplex, but adequate for coastlines)
        long seed = config.seed();
        double freq = 5.0;  // Noise frequency

        // Hash the coordinates
        double nx = x * freq + seed * 0.001;
        double ny = y * freq + seed * 0.002;
        double nz = z * freq + seed * 0.003;

        // Generate pseudo-random value in range [0, 1]
        double v1 = Math.sin(nx * 12.9898 + ny * 78.233 + nz * 37.719) * 43758.5453;
        double noise = v1 - Math.floor(v1);

        return noise;
    }

    private void normalizeWorkingHeights() {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (double h : workingHeights) {
            if (h < min) min = h;
            if (h > max) max = h;
        }

        double targetMin = config.continuousReliefMin();
        double targetMax = config.continuousReliefMax();
        if (max <= min || targetMax <= targetMin) {
            return;
        }

        double scale = (targetMax - targetMin) / (max - min);
        for (int i = 0; i < workingHeights.length; i++) {
            workingHeights[i] = (workingHeights[i] - min) * scale + targetMin;
        }
    }

    private static double[] convertToDouble(int[] heights) {
        double[] converted = new double[heights.length];
        for (int i = 0; i < heights.length; i++) {
            converted[i] = heights[i];
        }
        return converted;
    }
}
