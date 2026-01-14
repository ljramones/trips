package com.teamgannon.trips.nightsky.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Central time conversion and epoch handling.
 * Bridges Java time API with Orekit time scales.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeService {

    private final OrekitBootstrapService orekitBootstrap;

    /**
     * Convert Java Instant to Orekit AbsoluteDate in UTC.
     */
    public AbsoluteDate toAbsoluteDate(Instant instant) {
        if (!orekitBootstrap.isInitialized()) {
            throw new IllegalStateException("Orekit not initialized");
        }

        ZonedDateTime zdt = instant.atZone(ZoneOffset.UTC);
        TimeScale utc = TimeScalesFactory.getUTC();

        return new AbsoluteDate(
                zdt.getYear(),
                zdt.getMonthValue(),
                zdt.getDayOfMonth(),
                zdt.getHour(),
                zdt.getMinute(),
                zdt.getSecond() + zdt.getNano() / 1e9,
                utc
        );
    }

    /**
     * Convert Orekit AbsoluteDate to Java Instant.
     */
    public Instant toInstant(AbsoluteDate date) {
        if (!orekitBootstrap.isInitialized()) {
            throw new IllegalStateException("Orekit not initialized");
        }

        // durationFrom takes only one argument (another AbsoluteDate)
        double seconds = date.durationFrom(AbsoluteDate.JAVA_EPOCH);
        return Instant.ofEpochSecond(
                (long) seconds,
                (long) ((seconds % 1) * 1e9)
        );
    }

    /**
     * Get AbsoluteDate in Terrestrial Time (TT) scale.
     * Returns the same instant expressed in TT time scale.
     */
    public AbsoluteDate toTT(Instant instant) {
        AbsoluteDate utcDate = toAbsoluteDate(instant);
        TimeScale tt = TimeScalesFactory.getTT();
        // Convert by getting offset and shifting - or simply return as-is
        // since AbsoluteDate internally represents an absolute moment in time
        // The time scale only affects how it's displayed/interpreted
        return utcDate;
    }

    /**
     * Calculate Julian Date from Instant.
     */
    public double toJulianDate(Instant instant) {
        AbsoluteDate date = toAbsoluteDate(instant);
        return date.durationFrom(AbsoluteDate.JULIAN_EPOCH) / 86400.0 + 2451545.0;
    }

    /**
     * Calculate local sidereal time for a given longitude.
     */
    public double getLocalSiderealTime(Instant instant, double longitudeRad) {
        double jd = toJulianDate(instant);
        double T = (jd - 2451545.0) / 36525.0;

        // Greenwich Mean Sidereal Time in degrees
        double gmst = 280.46061837 + 360.98564736629 * (jd - 2451545.0)
                + 0.000387933 * T * T - T * T * T / 38710000.0;

        // Normalize to 0-360
        gmst = gmst % 360.0;
        if (gmst < 0) gmst += 360.0;

        // Add longitude (convert rad to deg)
        double lst = gmst + Math.toDegrees(longitudeRad);
        lst = lst % 360.0;
        if (lst < 0) lst += 360.0;

        return Math.toRadians(lst);
    }
}
