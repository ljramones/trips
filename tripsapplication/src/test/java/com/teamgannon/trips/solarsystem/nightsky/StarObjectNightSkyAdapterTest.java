package com.teamgannon.trips.solarsystem.nightsky;

import com.teamgannon.trips.jpa.model.StarObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StarObjectNightSkyAdapterTest {

    @Test
    void convertsRaDecAndSelectsName() {
        StarObject star = new StarObject();
        star.setId("id-1");
        star.setDisplayName("Display");
        star.setRa(30.0);
        star.setDeclination(-10.0);
        star.setMagv(1.2);

        StarObjectNightSkyAdapter adapter = new StarObjectNightSkyAdapter();
        StarCatalogEntry entry = adapter.fromStarObject(star);

        assertEquals("Display", entry.getName());
        assertEquals(2.0, entry.getRaHours(), 1e-9);
        assertEquals(-10.0, entry.getDecDeg(), 1e-9);
        assertEquals(1.2, entry.getMagnitude(), 1e-9);
    }

    @Test
    void magvPreferredOverApparentMagnitude() {
        StarObject star = new StarObject();
        star.setDisplayName("Star");
        star.setRa(15.0);
        star.setDeclination(5.0);
        star.setMagv(-0.5);
        star.setApparentMagnitude("2.0");

        StarObjectNightSkyAdapter adapter = new StarObjectNightSkyAdapter();
        StarCatalogEntry entry = adapter.fromStarObject(star);

        assertEquals(-0.5, entry.getMagnitude(), 1e-9);
    }

    @Test
    void apparentMagnitudeFallbackAndNamePreference() {
        StarObject star = new StarObject();
        star.setId("id-2");
        star.setSystemName("System");
        star.setCommonName("Common");
        star.setRa(0.0);
        star.setDeclination(0.0);
        star.setMagv(0.0);
        star.setApparentMagnitude("3.3");

        StarObjectNightSkyAdapter adapter = new StarObjectNightSkyAdapter();
        StarCatalogEntry entry = adapter.fromStarObject(star);

        assertEquals("Common", entry.getName());
        assertEquals(3.3, entry.getMagnitude(), 1e-9);
    }

    @Test
    void invalidApparentMagnitudeYieldsInfinity() {
        StarObject star = new StarObject();
        star.setDisplayName("Star");
        star.setRa(0.0);
        star.setDeclination(0.0);
        star.setMagv(0.0);
        star.setApparentMagnitude("n/a");

        StarObjectNightSkyAdapter adapter = new StarObjectNightSkyAdapter();
        StarCatalogEntry entry = adapter.fromStarObject(star);

        assertTrue(Double.isInfinite(entry.getMagnitude()));
    }
}
