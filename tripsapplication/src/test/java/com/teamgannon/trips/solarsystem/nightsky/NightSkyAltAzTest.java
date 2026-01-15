package com.teamgannon.trips.solarsystem.nightsky;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightSkyAltAzTest {

    private static final double DEG_TOL = 2.0;

    @Test
    void polarisAltitudeMatchesLatitudeApprox() {
        ObserverLocation obs = new ObserverLocation(43.6532, -79.3832);
        EquatorialCoordinates polaris = new EquatorialCoordinates(2.5303, 89.2641);
        AltAz altAz = NightSkyMath.equatorialToAltAz(polaris, obs, Instant.parse("2024-01-01T00:00:00Z"));

        double expectedAlt = obs.getLatitudeDeg();
        assertEquals(expectedAlt, altAz.getAltitudeDeg(), DEG_TOL);
    }

    @Test
    void siriusGreenwichJ2000MatchesReference() {
        ObserverLocation obs = new ObserverLocation(51.4779, 0.0);
        EquatorialCoordinates sirius = new EquatorialCoordinates(6.7525, -16.7161);
        AltAz altAz = NightSkyMath.equatorialToAltAz(sirius, obs, Instant.parse("2000-01-01T12:00:00Z"));

        assertEquals(-55.23195834688918, altAz.getAltitudeDeg(), DEG_TOL);
        assertEquals(1.3888321050786983, altAz.getAzimuthDeg(), DEG_TOL);
        assertTrue(altAz.getAzimuthDeg() >= 0 && altAz.getAzimuthDeg() < 360.0);
    }
}
