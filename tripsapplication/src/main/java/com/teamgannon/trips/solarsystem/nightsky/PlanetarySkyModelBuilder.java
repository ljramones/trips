package com.teamgannon.trips.solarsystem.nightsky;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.planetary.PlanetaryContext;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public final class PlanetarySkyModelBuilder {

    // NightSkyFrameBridge is now a utility class (private ctor + static methods),
    // so do NOT instantiate it.
    private final NightSkyQuery3D query3D = new NightSkyQuery3D();

    public PlanetarySkyModel build(PlanetaryContext context, List<StarObject> allStars) {
        if (context == null || allStars == null) {
            return new PlanetarySkyModel(List.of(), List.of(), 0, 0.0,
                    context != null ? context.getMagnitudeLimit() : 0.0);
        }

        int magvCount = 0;
        int grpCount = 0;
        int bpgCount = 0;
        int bprpCount = 0;
        for (StarObject star : allStars) {
            if (star == null) {
                continue;
            }
            if (star.getMagv() != 0.0 && !Double.isNaN(star.getMagv())) {
                magvCount++;
            }
            if (star.getGrp() != 0.0 && !Double.isNaN(star.getGrp())) {
                grpCount++;
            }
            if (star.getBpg() != 0.0 && !Double.isNaN(star.getBpg())) {
                bpgCount++;
            }
            if (star.getBprp() != 0.0 && !Double.isNaN(star.getBprp())) {
                bprpCount++;
            }
        }

        Vector3D observerPosition = computeObserverPosition(context, allStars);
        PlanetRotationModel rotationModel = new PlanetRotationModel(0.0, 86400.0, 0.0);
        ObserverLocation observerLocation = new ObserverLocation(0.0, 0.0);
        double tSeconds = context.getLocalTime() * 3600.0;
        double minAltitude = -0.1;
        double[] sunHorizon = computeHostStarHorizon(context, allStars, observerPosition,
                rotationModel, observerLocation, tSeconds);
        double sunAltitude = sunHorizon[0];
        double sunAzimuth = sunHorizon[1];
        boolean isDay = sunAltitude > 0.0;
        double maxMagnitude = sunAltitude > 0.0
                ? -3.0
                : context.getMagnitudeLimit();

        log.info("Planetary sky: stars={}, magv={}, grp={}, bpg={}, bprp={}, maxMag={}, sunAlt={}, sunAz={}, localTime={}, tSeconds={}",
                allStars.size(), magvCount, grpCount, bpgCount, bprpCount, maxMagnitude,
                sunAltitude, sunAzimuth, context.getLocalTime(), tSeconds);

        if (isDay) {
            return new PlanetarySkyModel(List.of(), List.of(), 0, sunAltitude, maxMagnitude);
        }

        List<VisibleStarResult> visible = query3D.visibleStarsFromXYZ(
                allStars,
                observerPosition,
                rotationModel,
                observerLocation,
                tSeconds,
                minAltitude,
                Integer.MAX_VALUE,
                SkyQueryOptions.SortMode.BRIGHTEST,
                maxMagnitude
        );

        log.info("Planetary sky: visibleStars={}", visible.size());

        List<VisibleStarResult> topBrightest = visible.size() > 20
                ? visible.subList(0, 20)
                : visible;

        return new PlanetarySkyModel(visible, topBrightest, visible.size(), sunAltitude, maxMagnitude);
    }

    private Vector3D computeObserverPosition(PlanetaryContext context, List<StarObject> allStars) {
        StarDisplayRecord hostStar = context.getHostStar();
        ExoPlanet planet = context.getPlanet();
        double semiMajorAxis = planet != null && planet.getSemiMajorAxis() != null
                ? planet.getSemiMajorAxis()
                : 1.0;

        Vector3D offsetAu = new Vector3D(semiMajorAxis, 0.0, 0.0);

        StarObject hostStarObject = findHostStarObject(hostStar, allStars);

        // Static call (no instance)
        return NightSkyFrameBridge.observerPositionLyFromAu(hostStarObject, offsetAu);
    }

    private StarObject findHostStarObject(StarDisplayRecord hostStar, List<StarObject> allStars) {
        if (hostStar == null || allStars.isEmpty()) {
            return null;
        }
        String recordId = hostStar.getRecordId();
        if (recordId != null && !recordId.isBlank()) {
            for (StarObject star : allStars) {
                if (star != null && recordId.equals(star.getId())) {
                    return star;
                }
            }
        }
        String starName = hostStar.getStarName();
        if (starName != null && !starName.isBlank()) {
            for (StarObject star : allStars) {
                if (star != null && starName.equals(star.getDisplayName())) {
                    return star;
                }
            }
        }
        StarObject fallback = new StarObject();
        fallback.setX(hostStar.getX());
        fallback.setY(hostStar.getY());
        fallback.setZ(hostStar.getZ());
        return fallback;
    }

    private double[] computeHostStarHorizon(PlanetaryContext context,
                                            List<StarObject> allStars,
                                            Vector3D observerPosition,
                                            PlanetRotationModel rotationModel,
                                            ObserverLocation observerLocation,
                                            double tSeconds) {
        StarObject hostStarObject = findHostStarObject(context.getHostStar(), allStars);
        Vector3D hostStarPos = NightSkyFrameBridge.starPositionLy(hostStarObject);
        Vector3D delta = hostStarPos.subtract(observerPosition);
        double distance = delta.getNorm();
        if (distance == 0.0) {
            return new double[]{90.0, 0.0};
        }
        Vector3D direction = delta.normalize();
        HorizonBasis basis = NightSkyMath.computeHorizonBasis(rotationModel, observerLocation, tSeconds);
        Vector3D horizon = NightSkyMath.toHorizonCoords(direction, basis);
        double altitude = NightSkyMath.altitudeDeg(horizon);
        double azimuth = NightSkyMath.azimuthDeg(horizon);
        return new double[]{altitude, azimuth};
    }
}
