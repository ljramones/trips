package com.teamgannon.trips.nightsky.service;

import com.teamgannon.trips.nightsky.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Main orchestrator for night sky computation.
 * This is the service that JavaFX calls to get the sky view.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NightSkyService {

    private final TimeService timeService;
    private final EphemerisService ephemerisService;
    private final StarQueryService starQueryService;
    private final SkyTransformService skyTransformService;
    private final PhotometryService photometryService;
    private final NightSkyCacheService cacheService;

    /**
     * Compute the night sky as seen from a planet's surface.
     *
     * @param request Night sky computation request
     * @return Result containing visible stars with positions and magnitudes
     */
    public NightSkyResult computeNightSky(NightSkyRequest request) {
        Instant startTime = Instant.now();

        // Check cache first
        NightSkyResult cached = cacheService.get(request);
        if (cached != null) {
            log.info("Returning cached sky for planet: {}", request.getPlanetId());
            return cached;
        }

        log.info("Computing night sky for planet: {} at time: {}",
                request.getPlanetId(), request.getInstantUtc());

        // 1. Get planet position and attitude
        PlanetAttitude attitude = ephemerisService.getPlanetAttitude(
                request.getPlanetId(),
                request.getHostStarId(),
                request.getInstantUtc());

        double[] observerPos = attitude.getPositionLy();

        // 2. Build observer's local ENU frame
        EnuFrame enuFrame = ephemerisService.getEnuFrame(
                attitude,
                request.getLatRad(),
                request.getLonRad());

        // 3. Query candidate stars
        List<StarRenderRow> candidateStars = starQueryService.queryStarsInRadius(
                observerPos[0], observerPos[1], observerPos[2],
                request.getRadiusLy(),
                request.getDatasetName(),
                request.getLod());

        log.debug("Queried {} candidate stars", candidateStars.size());

        // 4. Transform each star to sky coordinates
        List<SkyStarPoint> visibleStars = new ArrayList<>();

        for (StarRenderRow star : candidateStars) {
            // Transform to horizontal coordinates
            double[] horizontal = skyTransformService.worldToHorizontal(
                    star, observerPos, enuFrame);

            double azimuth = horizontal[0];
            double altitude = horizontal[1];
            double distance = horizontal[2];

            // Skip stars below horizon
            if (!skyTransformService.isAboveHorizon(altitude)) {
                continue;
            }

            // Calculate apparent magnitude
            float apparentMag = photometryService.calculateApparentMagnitude(
                    star.getAbsMag(), distance);

            // Apply atmospheric extinction
            apparentMag = photometryService.applyAtmosphericExtinction(
                    apparentMag, altitude, request.getAtmosphereModel());

            // Skip stars dimmer than limit
            if (apparentMag > request.getMaxMagnitude()) {
                continue;
            }

            // Get star color
            int color = photometryService.starToColor(star);

            // Create sky point
            SkyStarPoint point = SkyStarPoint.builder()
                    .azRad(azimuth)
                    .altRad(altitude)
                    .apparentMag(apparentMag)
                    .color(color)
                    .starId(star.getStarId())
                    .starName(star.getStarName())
                    .distanceLy(distance)
                    .build();

            visibleStars.add(point);
        }

        // 5. Sort by brightness and limit
        visibleStars.sort(Comparator.comparingDouble(SkyStarPoint::getApparentMag));
        if (visibleStars.size() > request.getMaxStars()) {
            visibleStars = visibleStars.subList(0, request.getMaxStars());
        }

        // 6. Compute host star position (the "sun")
        SkyStarPoint hostStar = computeHostStar(request, observerPos, enuFrame);

        // 7. Build result
        Duration computeTime = Duration.between(startTime, Instant.now());

        NightSkyResult result = NightSkyResult.builder()
                .stars(visibleStars)
                .hostStar(hostStar)
                .totalStarsQueried(candidateStars.size())
                .visibleCount(visibleStars.size())
                .computeTime(computeTime)
                .request(request)
                .computedAt(Instant.now())
                .fromCache(false)
                .build();

        // 8. Cache the result
        cacheService.put(request, result);

        log.info("Computed night sky: {} visible stars in {}ms",
                visibleStars.size(), computeTime.toMillis());

        return result;
    }

    /**
     * Compute the host star's position in the sky (the "sun").
     */
    private SkyStarPoint computeHostStar(NightSkyRequest request,
                                          double[] observerPos,
                                          EnuFrame enuFrame) {
        StarRenderRow hostStar = starQueryService.getHostStar(request.getHostStarId());
        if (hostStar == null) {
            return null;
        }

        double[] horizontal = skyTransformService.worldToHorizontal(
                hostStar, observerPos, enuFrame);

        double distance = horizontal[2];
        float apparentMag = photometryService.calculateApparentMagnitude(
                hostStar.getAbsMag(), distance);

        return SkyStarPoint.builder()
                .azRad(horizontal[0])
                .altRad(horizontal[1])
                .apparentMag(apparentMag)
                .color(photometryService.starToColor(hostStar))
                .starId(hostStar.getStarId())
                .starName(hostStar.getStarName())
                .distanceLy(distance)
                .build();
    }

    /**
     * Quick check if it's "night" at the observer location.
     */
    public boolean isNightTime(NightSkyRequest request) {
        PlanetAttitude attitude = ephemerisService.getPlanetAttitude(
                request.getPlanetId(),
                request.getHostStarId(),
                request.getInstantUtc());

        EnuFrame enuFrame = ephemerisService.getEnuFrame(
                attitude,
                request.getLatRad(),
                request.getLonRad());

        StarRenderRow hostStar = starQueryService.getHostStar(request.getHostStarId());
        if (hostStar == null) {
            return true;  // No host star means always night
        }

        double[] horizontal = skyTransformService.worldToHorizontal(
                hostStar, attitude.getPositionLy(), enuFrame);

        // Night if sun is below horizon
        return horizontal[1] < 0;
    }

    /**
     * Get cache statistics.
     */
    public String getCacheStats() {
        return cacheService.getStats();
    }

    /**
     * Clear the cache.
     */
    public void clearCache() {
        cacheService.clear();
    }
}
