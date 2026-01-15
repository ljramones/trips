package com.teamgannon.trips.solarsystem.nightsky;

import com.teamgannon.trips.jpa.model.StarObject;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightSkyQuery3DTest {

    @Test
    void computesAltAzFromXYZAndFilters() {
        NightSkyQuery3D query = new NightSkyQuery3D();
        Vector3D observer = Vector3D.ZERO;
        PlanetRotationModel rotation = new PlanetRotationModel(0.0, 86400.0, 0.0);
        ObserverLocation location = new ObserverLocation(0.0, 0.0);
        HorizonBasis basis = NightSkyMath.computeHorizonBasis(rotation, location, 0.0);

        StarObject upStar = star("Up",
                basis.getUpUnit().getX(), basis.getUpUnit().getY(), basis.getUpUnit().getZ(), 1.0);
        StarObject eastStar = star("East",
                basis.getEastUnit().getX(), basis.getEastUnit().getY(), basis.getEastUnit().getZ(), 2.0);
        StarObject northStar = star("North",
                basis.getNorthUnit().getX(), basis.getNorthUnit().getY(), basis.getNorthUnit().getZ(), 1.5);
        StarObject downStar = star("Down",
                -basis.getUpUnit().getX(), -basis.getUpUnit().getY(), -basis.getUpUnit().getZ(), 0.5);

        List<VisibleStarResult> results = query.visibleStarsFromXYZ(
                List.of(upStar, eastStar, northStar, downStar),
                observer,
                rotation,
                location,
                0.0,
                -0.1,
                10,
                SkyQueryOptions.SortMode.HIGHEST
        );

        assertEquals(3, results.size());
        assertEquals("Up", results.get(0).getStar().getDisplayName());
        assertTrue(results.get(0).getAltitudeDeg() > results.get(1).getAltitudeDeg());
        assertEquals(90.0, results.get(0).getAltitudeDeg(), 1e-6);
        boolean foundEast = false;
        boolean foundNorth = false;
        for (VisibleStarResult result : results) {
            if (Math.abs(result.getAltitudeDeg()) < 1e-6 && Math.abs(result.getAzimuthDeg() - 90.0) < 1e-6) {
                foundEast = true;
            }
            if (Math.abs(result.getAltitudeDeg()) < 1e-6 && Math.abs(result.getAzimuthDeg()) < 1e-6) {
                foundNorth = true;
            }
        }
        assertTrue(foundEast);
        assertTrue(foundNorth);
    }

    @Test
    void brightestSortOrdersByMagnitude() {
        NightSkyQuery3D query = new NightSkyQuery3D();
        Vector3D observer = Vector3D.ZERO;
        PlanetRotationModel rotation = new PlanetRotationModel(0.0, 86400.0, 0.0);
        ObserverLocation location = new ObserverLocation(0.0, 0.0);

        StarObject bright = star("Bright", 1.0, 0.0, 0.0, -1.0);
        StarObject dim = star("Dim", 0.0, 0.0, 1.0, 1.0);

        List<VisibleStarResult> results = query.visibleStarsFromXYZ(
                List.of(dim, bright),
                observer,
                rotation,
                location,
                0.0,
                -1.0,
                10,
                SkyQueryOptions.SortMode.BRIGHTEST,
                Double.POSITIVE_INFINITY
        );

        assertEquals("Bright", results.get(0).getStar().getDisplayName());
    }

    private StarObject star(String name, double x, double y, double z, double magv) {
        StarObject star = new StarObject();
        star.setDisplayName(name);
        star.setX(x);
        star.setY(y);
        star.setZ(z);
        star.setMagv(magv);
        return star;
    }
}
