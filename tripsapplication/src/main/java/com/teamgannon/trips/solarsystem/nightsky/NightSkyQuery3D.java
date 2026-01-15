package com.teamgannon.trips.solarsystem.nightsky;

import com.teamgannon.trips.jpa.model.StarObject;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class NightSkyQuery3D {

    public List<VisibleStarResult> visibleStarsFromXYZ(List<StarObject> stars,
                                                       Vector3D observerPositionLy,
                                                       PlanetRotationModel rotationModel,
                                                       ObserverLocation observerLatLon,
                                                       double tSeconds,
                                                       double minAltitudeDeg,
                                                       int maxResults,
                                                       SkyQueryOptions.SortMode sortMode) {
        return visibleStarsFromXYZ(stars, observerPositionLy, rotationModel, observerLatLon, tSeconds,
                minAltitudeDeg, maxResults, sortMode, Double.POSITIVE_INFINITY);
    }

    public List<VisibleStarResult> visibleStarsFromXYZ(List<StarObject> stars,
                                                       Vector3D observerPositionLy,
                                                       PlanetRotationModel rotationModel,
                                                       ObserverLocation observerLatLon,
                                                       double tSeconds,
                                                       double minAltitudeDeg,
                                                       int maxResults,
                                                       SkyQueryOptions.SortMode sortMode,
                                                       double maxMagnitude) {
        List<VisibleStarResult> results = new ArrayList<>();
        if (stars == null || observerPositionLy == null) {
            return results;
        }

        HorizonBasis basis = NightSkyMath.computeHorizonBasis(rotationModel, observerLatLon, tSeconds);

        for (StarObject star : stars) {
            if (star == null) {
                continue;
            }
            Vector3D starPos = new Vector3D(star.getX(), star.getY(), star.getZ());
            Vector3D delta = starPos.subtract(observerPositionLy);
            double distance = delta.getNorm();
            if (distance == 0.0) {
                continue;
            }
            Vector3D direction = delta.normalize();
            Vector3D horizon = NightSkyMath.toHorizonCoords(direction, basis);
            double alt = NightSkyMath.altitudeDeg(horizon);
            if (alt <= minAltitudeDeg) {
                continue;
            }
            double az = NightSkyMath.azimuthDeg(horizon);
            double magnitude = selectMagnitude(star);
            if (magnitude > maxMagnitude) {
                continue;
            }
            results.add(new VisibleStarResult(star, alt, az, magnitude, distance));
        }

        results.sort(resultComparator(sortMode));
        if (results.size() > maxResults) {
            return results.subList(0, maxResults);
        }
        return results;
    }

    private Comparator<VisibleStarResult> resultComparator(SkyQueryOptions.SortMode sortMode) {
        if (sortMode == SkyQueryOptions.SortMode.HIGHEST) {
            return Comparator
                    .comparingDouble(VisibleStarResult::getAltitudeDeg).reversed()
                    .thenComparingDouble(VisibleStarResult::getMagnitude);
        }
        return Comparator
                .comparingDouble(VisibleStarResult::getMagnitude)
                .thenComparing(Comparator.comparingDouble(VisibleStarResult::getAltitudeDeg).reversed());
    }

    private double selectMagnitude(StarObject star) {
        double magv = star.getMagv();
        if (magv != 0.0 && !Double.isNaN(magv)) {
            return magv;
        }
        String apparent = star.getApparentMagnitude();
        if (apparent != null && !apparent.trim().isEmpty()) {
            try {
                return Double.parseDouble(apparent.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return Double.POSITIVE_INFINITY;
    }
}
