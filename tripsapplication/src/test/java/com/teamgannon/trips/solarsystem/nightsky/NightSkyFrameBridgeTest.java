package com.teamgannon.trips.solarsystem.nightsky;

import com.teamgannon.trips.jpa.model.StarObject;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NightSkyFrameBridgeTest {

    @Test
    void addsRelativeObserverOffset() {
        StarObject systemStar = starAt(100.0, 0.0, 0.0);
        Vector3D observerRel = new Vector3D(0.001, 0.0, 0.0);

        Vector3D observer = NightSkyFrameBridge.observerPositionLy(systemStar, observerRel);

        assertEquals(100.001, observer.getX(), 1e-9);
        assertEquals(0.0, observer.getY(), 1e-9);
        assertEquals(0.0, observer.getZ(), 1e-9);
    }

    @Test
    void convertsAuToLy() {
        StarObject systemStar = starAt(0.0, 0.0, 0.0);

        Vector3D observer = NightSkyFrameBridge.observerPositionLyFromAu(systemStar, 1.0, 0.0, 0.0);

        assertEquals(NightSkyFrameBridge.AU_TO_LY, observer.getX(), 1e-12);
        assertEquals(0.0, observer.getY(), 1e-12);
        assertEquals(0.0, observer.getZ(), 1e-12);
    }

    @Test
    void translationYieldsCorrectDirection() {
        StarObject star1 = starAt(101.0, 0.0, 0.0);
        StarObject star2 = starAt(102.0, 0.0, 0.0);

        Vector3D observer = NightSkyFrameBridge.starPositionLy(star1);
        Vector3D direction = new Vector3D(star2.getX(), star2.getY(), star2.getZ())
                .subtract(observer)
                .normalize();

        assertEquals(1.0, direction.getX(), 1e-12);
        assertEquals(0.0, direction.getY(), 1e-12);
        assertEquals(0.0, direction.getZ(), 1e-12);
    }

    @Test
    void nullSystemStarTreatsOriginAsSol() {
        Vector3D observer = NightSkyFrameBridge.observerPositionLy(null, new Vector3D(1.0, 2.0, 3.0));
        assertEquals(1.0, observer.getX(), 1e-12);
        assertEquals(2.0, observer.getY(), 1e-12);
        assertEquals(3.0, observer.getZ(), 1e-12);
    }


    private StarObject starAt(double x, double y, double z) {
        StarObject star = new StarObject();
        star.setX(x);
        star.setY(y);
        star.setZ(z);
        return star;
    }
}
