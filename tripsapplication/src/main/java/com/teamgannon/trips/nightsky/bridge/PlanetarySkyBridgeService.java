package com.teamgannon.trips.nightsky.bridge;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import com.teamgannon.trips.nightsky.math.*;
import com.teamgannon.trips.nightsky.model.*;
import com.teamgannon.trips.nightsky.service.*;
import com.teamgannon.trips.planetary.PlanetaryContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Bridge service that adapts the top-level nightsky package to the solarsystem.nightsky models.
 *
 * This service provides:
 * - Efficient spatial queries (instead of loading all stars)
 * - Photometry with atmospheric extinction
 * - Caching for performance
 * - Compatibility with existing PlanetarySkyModel
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanetarySkyBridgeService {

    private final StarQueryService starQueryService;
    private final SkyTransformService skyTransformService;
    private final PhotometryService photometryService;
    private final NightSkyCacheService cacheService;
    private final StarObjectRepository starObjectRepository;

    // Default search radius in light years
    private static final double DEFAULT_RADIUS_LY = 100.0;

    /**
     * Compute the night sky from a planetary context using the top-level services.
     *
     * @param context    Planetary viewing context
     * @param datasetName Name of the dataset to query stars from (may be null)
     * @param radiusLy   Search radius in light years
     * @return PlanetarySkyModel compatible with existing rendering
     */
    @Transactional(readOnly = true)
    public PlanetarySkyModel computeSky(PlanetaryContext context, String datasetName, double radiusLy) {
        return computeSkyInternal(context, datasetName, radiusLy);
    }

    /**
     * Internal implementation that performs the actual computation.
     * Called within a transaction context from the public methods.
     */
    private PlanetarySkyModel computeSkyInternal(PlanetaryContext context, String datasetName, double radiusLy) {
        if (context == null) {
            log.warn("Null planetary context provided");
            return emptyModel(0.0, 6.0);
        }

        // Get observer position in light years
        double[] observerPositionLy = computeObserverPositionLy(context);
        if (observerPositionLy == null) {
            log.warn("Could not compute observer position");
            return emptyModel(0.0, context.getMagnitudeLimit());
        }

        log.info("Observer position: ({}, {}, {}) ly, hostStar: {} at ({}, {}, {})",
                observerPositionLy[0], observerPositionLy[1], observerPositionLy[2],
                context.getHostStarName(),
                context.getHostStar() != null ? context.getHostStar().getX() : "null",
                context.getHostStar() != null ? context.getHostStar().getY() : "null",
                context.getHostStar() != null ? context.getHostStar().getZ() : "null");

        // Compute host star altitude to determine day/night
        double[] hostStarHorizon = computeHostStarHorizon(context, observerPositionLy);
        double hostStarAltitudeDeg = hostStarHorizon[0];
        boolean isDay = hostStarAltitudeDeg > 0.0;

        log.info("Host star horizon: alt={}, az={}, isDay={}, localTime={}",
                hostStarAltitudeDeg, hostStarHorizon[1], isDay, context.getLocalTime());

        // During daytime, only show the brightest stars
        double effectiveMagLimit = isDay ? -3.0 : context.getMagnitudeLimit();

        if (isDay) {
            log.info("Daytime sky: hostStarAlt={}, returning empty model", hostStarAltitudeDeg);
            return new PlanetarySkyModel(List.of(), List.of(), 0, hostStarAltitudeDeg, effectiveMagLimit);
        }

        // Try cache first
        String cacheKey = buildCacheKey(context, observerPositionLy);
        NightSkyResult cachedResult = tryCache(context, observerPositionLy, radiusLy, datasetName);
        if (cachedResult != null) {
            log.debug("Using cached sky result");
            return convertFromNightSkyResult(cachedResult, hostStarAltitudeDeg, effectiveMagLimit);
        }

        // Load stars efficiently with magnitude pre-filtering at DB level
        // Add some headroom to the magnitude limit to account for atmospheric extinction
        double dbMagLimit = effectiveMagLimit + 1.0;
        List<StarRenderRow> allStars = queryStarsEfficiently(dbMagLimit, datasetName);

        // Fallback: if very few stars found, some may have magnitude in other fields
        // In that case, use the full streaming query
        if (allStars.size() < 50) {
            log.info("Few stars found with magv filter ({}), using full streaming query", allStars.size());
            allStars = queryAllStars();
        }

        // Build horizon basis for the observer (same as NightSkyQuery3D)
        PlanetRotationModel rotationModel = new PlanetRotationModel(0.0, 86400.0, 0.0);
        ObserverLocation observerLocation = new ObserverLocation(0.0, 0.0);
        double tSeconds = context.getLocalTime() * 3600.0;
        HorizonBasis basis = NightSkyMath.computeHorizonBasis(rotationModel, observerLocation, tSeconds);

        // Transform stars to sky coordinates and filter by visibility
        List<VisibleStarResult> visibleStars = transformAndFilter(
                allStars, observerPositionLy, basis, effectiveMagLimit,
                context.isShowAtmosphereEffects());

        log.info("Planetary sky: visibleStars={}", visibleStars.size());

        // Sort by magnitude (brightest first) and get top 20
        visibleStars.sort(Comparator.comparingDouble(VisibleStarResult::getMagnitude));
        List<VisibleStarResult> topBrightest = visibleStars.size() > 20
                ? visibleStars.subList(0, 20)
                : new ArrayList<>(visibleStars);

        return new PlanetarySkyModel(visibleStars, topBrightest, visibleStars.size(),
                hostStarAltitudeDeg, effectiveMagLimit);
    }

    /**
     * Compute sky using default radius.
     */
    @Transactional(readOnly = true)
    public PlanetarySkyModel computeSky(PlanetaryContext context, String datasetName) {
        return computeSkyInternal(context, datasetName, DEFAULT_RADIUS_LY);
    }

    /**
     * Compute sky without specifying dataset (queries all).
     */
    @Transactional(readOnly = true)
    public PlanetarySkyModel computeSky(PlanetaryContext context) {
        return computeSkyInternal(context, null, DEFAULT_RADIUS_LY);
    }

    private double[] computeObserverPositionLy(PlanetaryContext context) {
        // Use pre-computed position if available
        if (context.getPlanetPositionLy() != null) {
            return context.getPlanetPositionLy();
        }

        // Otherwise compute from host star and planet orbital position
        StarDisplayRecord hostStar = context.getHostStar();
        ExoPlanet planet = context.getPlanet();

        if (hostStar == null) {
            return null;
        }

        double semiMajorAxisAu = (planet != null && planet.getSemiMajorAxis() != null)
                ? planet.getSemiMajorAxis()
                : 1.0;

        // Create a StarObject for NightSkyFrameBridge
        StarObject hostStarObject = new StarObject();
        hostStarObject.setX(hostStar.getX());
        hostStarObject.setY(hostStar.getY());
        hostStarObject.setZ(hostStar.getZ());

        // Offset in AU from host star
        Vector3D offsetAu = new Vector3D(semiMajorAxisAu, 0.0, 0.0);

        // Use NightSkyFrameBridge for coordinate translation
        Vector3D positionLy = NightSkyFrameBridge.observerPositionLyFromAu(hostStarObject, offsetAu);

        return new double[]{positionLy.getX(), positionLy.getY(), positionLy.getZ()};
    }

    private double[] computeHostStarHorizon(PlanetaryContext context, double[] observerPositionLy) {
        StarDisplayRecord hostStar = context.getHostStar();
        if (hostStar == null) {
            return new double[]{90.0, 0.0}; // Default: sun at zenith
        }

        // Direction from observer to host star
        double dx = hostStar.getX() - observerPositionLy[0];
        double dy = hostStar.getY() - observerPositionLy[1];
        double dz = hostStar.getZ() - observerPositionLy[2];

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance == 0) {
            return new double[]{90.0, 0.0};
        }

        // Use rotation model and local time to compute horizon coordinates
        PlanetRotationModel rotationModel = new PlanetRotationModel(0.0, 86400.0, 0.0);
        ObserverLocation observerLocation = new ObserverLocation(0.0, 0.0);
        double tSeconds = context.getLocalTime() * 3600.0;

        Vector3D direction = new Vector3D(dx / distance, dy / distance, dz / distance);
        HorizonBasis basis = NightSkyMath.computeHorizonBasis(rotationModel, observerLocation, tSeconds);
        Vector3D horizon = NightSkyMath.toHorizonCoords(direction, basis);

        double altitudeDeg = NightSkyMath.altitudeDeg(horizon);
        double azimuthDeg = NightSkyMath.azimuthDeg(horizon);

        return new double[]{altitudeDeg, azimuthDeg};
    }

    /**
     * Query stars efficiently using streaming with magnitude pre-filtering.
     * This is much faster than loading ALL stars and filtering in memory.
     *
     * @param magLimit Maximum magnitude to include (dimmer stars filtered at DB level)
     * @param datasetName Optional dataset name filter
     * @return List of star render rows
     */
    private List<StarRenderRow> queryStarsEfficiently(double magLimit, String datasetName) {
        List<StarRenderRow> results = new ArrayList<>();

        // Use streaming query with magnitude filter for efficiency
        // The database filters out stars that are definitely too dim
        try (Stream<StarObject> starStream = (datasetName != null && !datasetName.isEmpty())
                ? starObjectRepository.streamByDatasetAndMagnitude(datasetName, magLimit)
                : starObjectRepository.streamByMagnitudeBrighterThan(magLimit)) {

            starStream.forEach(star -> {
                if (star != null) {
                    results.add(toStarRenderRow(star));
                }
            });
        }

        log.info("Loaded {} stars with mag <= {} from database", results.size(), magLimit);
        return results;
    }

    /**
     * Query all stars (legacy fallback when magnitude filtering isn't practical).
     * Uses streaming to avoid loading everything into memory at once.
     */
    private List<StarRenderRow> queryAllStars() {
        List<StarRenderRow> results = new ArrayList<>();

        // Use streaming instead of findAll() for better memory efficiency
        try (Stream<StarObject> starStream = starObjectRepository.streamAll()) {
            starStream.forEach(star -> {
                if (star != null) {
                    results.add(toStarRenderRow(star));
                }
            });
        }

        log.info("Loaded {} total stars from database", results.size());
        return results;
    }

    private StarRenderRow toStarRenderRow(StarObject star) {
        // Use the same magnitude selection as NightSkyQuery3D
        float absMag = (float) selectStarObjectMagnitude(star);

        float teff = star.getTemperature() > 0 ? (float) star.getTemperature() : 5500.0f;

        return StarRenderRow.builder()
                .starId(star.getId() != null ? star.getId().hashCode() : 0)
                .xLy(star.getX())
                .yLy(star.getY())
                .zLy(star.getZ())
                .absMag(absMag)
                .bpRpOrTeff(teff)
                .spectralClass(star.getSpectralClass())
                .starName(resolveStarName(star))
                .build();
    }

    private String resolveStarName(StarObject star) {
        if (star.getCommonName() != null && !star.getCommonName().trim().isEmpty()) {
            return star.getCommonName().trim();
        }
        if (star.getDisplayName() != null && !star.getDisplayName().trim().isEmpty()) {
            return star.getDisplayName().trim();
        }
        if (star.getSystemName() != null && !star.getSystemName().trim().isEmpty()) {
            return star.getSystemName().trim();
        }
        if (star.getId() != null && !star.getId().trim().isEmpty()) {
            return star.getId().trim();
        }
        return "Unknown";
    }

    private List<VisibleStarResult> transformAndFilter(List<StarRenderRow> candidates,
                                                        double[] observerLy,
                                                        HorizonBasis basis,
                                                        double magLimit,
                                                        boolean applyAtmosphere) {
        AtmosphereModel atmosphere = applyAtmosphere
                ? AtmosphereModel.earthLike()
                : AtmosphereModel.none();

        Vector3D observerPos = new Vector3D(observerLy[0], observerLy[1], observerLy[2]);

        // Use parallel stream for large datasets (>1000 stars)
        boolean useParallel = candidates.size() > 1000;
        Stream<StarRenderRow> stream = useParallel ? candidates.parallelStream() : candidates.stream();

        List<VisibleStarResult> visible = stream
                .map(candidate -> transformStar(candidate, observerPos, basis, magLimit, atmosphere, applyAtmosphere))
                .filter(result -> result != null)
                .collect(Collectors.toList());

        log.info("Transform filter: {} visible out of {} candidates (parallel={})",
                visible.size(), candidates.size(), useParallel);

        return visible;
    }

    /**
     * Transform a single star to horizon coordinates and filter by visibility.
     * Returns null if the star is not visible.
     */
    private VisibleStarResult transformStar(StarRenderRow candidate,
                                             Vector3D observerPos,
                                             HorizonBasis basis,
                                             double magLimit,
                                             AtmosphereModel atmosphere,
                                             boolean applyAtmosphere) {
        // Get star position in world coordinates
        Vector3D starPos = new Vector3D(candidate.getXLy(), candidate.getYLy(), candidate.getZLy());
        Vector3D delta = starPos.subtract(observerPos);
        double distanceLy = delta.getNorm();

        if (distanceLy == 0.0) {
            return null;
        }

        // Compute direction and transform to horizon coordinates using NightSkyMath
        Vector3D direction = delta.normalize();
        Vector3D horizon = NightSkyMath.toHorizonCoords(direction, basis);
        double altDeg = NightSkyMath.altitudeDeg(horizon);
        double azDeg = NightSkyMath.azimuthDeg(horizon);

        // Skip stars below horizon (with small tolerance)
        if (altDeg < -0.5) {
            return null;
        }

        // Use magnitude directly from database
        double magnitude = selectMagnitude(candidate);

        // Optionally apply atmospheric extinction near horizon (makes stars dimmer)
        if (applyAtmosphere && altDeg > 0 && altDeg < 15) {
            double altRad = Math.toRadians(altDeg);
            magnitude = photometryService.applyAtmosphericExtinction((float) magnitude, altRad, atmosphere);
        }

        // Skip if too dim
        if (magnitude > magLimit) {
            return null;
        }

        float apparentMag = (float) magnitude;

        // Create a minimal StarObject for VisibleStarResult
        StarObject starObj = new StarObject();
        starObj.setX(candidate.getXLy());
        starObj.setY(candidate.getYLy());
        starObj.setZ(candidate.getZLy());
        starObj.setDisplayName(candidate.getStarName());
        starObj.setSpectralClass(candidate.getSpectralClass());

        return new VisibleStarResult(starObj, altDeg, azDeg, apparentMag, distanceLy);
    }

    /**
     * Select the magnitude for a StarRenderRow.
     * The magnitude was already computed in toStarRenderRow using selectStarObjectMagnitude.
     */
    private double selectMagnitude(StarRenderRow star) {
        float mag = star.getAbsMag();
        if (Float.isNaN(mag)) {
            return 10.0;  // Default for invalid
        }
        return mag;
    }

    /**
     * Select the best available magnitude for a StarObject, same logic as NightSkyQuery3D.selectMagnitude.
     */
    private double selectStarObjectMagnitude(StarObject star) {
        // Try V-band magnitude first (most common)
        double magv = star.getMagv();
        if (magv != 0.0 && !Double.isNaN(magv)) {
            return magv;
        }
        // Try Gaia magnitudes
        double grp = star.getGrp();
        if (grp != 0.0 && !Double.isNaN(grp)) {
            return grp;
        }
        double bpg = star.getBpg();
        if (bpg != 0.0 && !Double.isNaN(bpg)) {
            return bpg;
        }
        double bprp = star.getBprp();
        if (bprp != 0.0 && !Double.isNaN(bprp)) {
            return bprp;
        }
        // Try other band magnitudes
        double magb = star.getMagb();
        if (magb != 0.0 && !Double.isNaN(magb)) {
            return magb;
        }
        double magr = star.getMagr();
        if (magr != 0.0 && !Double.isNaN(magr)) {
            return magr;
        }
        double magi = star.getMagi();
        if (magi != 0.0 && !Double.isNaN(magi)) {
            return magi;
        }
        double magu = star.getMagu();
        if (magu != 0.0 && !Double.isNaN(magu)) {
            return magu;
        }
        // Try apparent magnitude string
        String apparent = star.getApparentMagnitude();
        if (apparent != null && !apparent.trim().isEmpty()) {
            try {
                return Double.parseDouble(apparent.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        // Try absolute magnitude string
        String absolute = star.getAbsoluteMagnitude();
        if (absolute != null && !absolute.trim().isEmpty()) {
            try {
                return Double.parseDouble(absolute.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return 10.0; // Default for unknown
    }

    /**
     * Log magnitude field values for debugging (call on first few stars only).
     */
    private void logMagnitudeFields(StarObject star, int index) {
        if (index < 5) {  // Only log first 5 stars
            log.info("Star {} mags: magv={}, magb={}, magr={}, grp={}, bpg={}, bprp={}, apparent='{}', absolute='{}', name={}",
                    index,
                    star.getMagv(),
                    star.getMagb(),
                    star.getMagr(),
                    star.getGrp(),
                    star.getBpg(),
                    star.getBprp(),
                    star.getApparentMagnitude(),
                    star.getAbsoluteMagnitude(),
                    star.getDisplayName());
        }
    }

    private LevelOfDetail selectLod(double magLimit) {
        if (magLimit >= 10.0) return LevelOfDetail.ULTRA;
        if (magLimit >= 8.0) return LevelOfDetail.HIGH;
        if (magLimit >= 6.5) return LevelOfDetail.MEDIUM;
        return LevelOfDetail.LOW;
    }

    private NightSkyResult tryCache(PlanetaryContext context, double[] observerLy,
                                     double radiusLy, String datasetName) {
        // Build cache request
        NightSkyRequest request = NightSkyRequest.builder()
                .planetId(context.getPlanet() != null ? context.getPlanet().getName() : "unknown")
                .hostStarId(context.getHostStar() != null ? context.getHostStar().getRecordId() : null)
                .instantUtc(Instant.now())
                .latRad(0.0)
                .lonRad(context.getLocalTime() * Math.PI / 12.0) // Convert hours to radians
                .radiusLy(radiusLy)
                .maxMagnitude((float) context.getMagnitudeLimit())
                .lod(selectLod(context.getMagnitudeLimit()))
                .datasetName(datasetName)
                .build();

        return cacheService.get(request);
    }

    private String buildCacheKey(PlanetaryContext context, double[] observerLy) {
        return String.format("%s_%.2f_%.2f_%.2f_%.1f",
                context.getPlanet() != null ? context.getPlanet().getName() : "unknown",
                observerLy[0], observerLy[1], observerLy[2],
                context.getLocalTime());
    }

    private PlanetarySkyModel convertFromNightSkyResult(NightSkyResult result,
                                                         double hostStarAltDeg,
                                                         double effectiveMagLimit) {
        List<VisibleStarResult> visible = new ArrayList<>();

        for (SkyStarPoint point : result.getStars()) {
            StarObject starObj = new StarObject();
            starObj.setDisplayName(point.getStarName());

            visible.add(new VisibleStarResult(
                    starObj,
                    Math.toDegrees(point.getAltRad()),
                    Math.toDegrees(point.getAzRad()),
                    point.getApparentMag(),
                    point.getDistanceLy()
            ));
        }

        List<VisibleStarResult> topBrightest = visible.size() > 20
                ? visible.subList(0, 20)
                : new ArrayList<>(visible);

        return new PlanetarySkyModel(visible, topBrightest, visible.size(),
                hostStarAltDeg, effectiveMagLimit);
    }

    private PlanetarySkyModel emptyModel(double hostStarAltDeg, double magLimit) {
        return new PlanetarySkyModel(List.of(), List.of(), 0, hostStarAltDeg, magLimit);
    }

    /**
     * Clear the sky cache.
     */
    public void clearCache() {
        cacheService.clear();
    }

    /**
     * Get cache statistics.
     */
    public String getCacheStats() {
        return cacheService.getStats();
    }
}
