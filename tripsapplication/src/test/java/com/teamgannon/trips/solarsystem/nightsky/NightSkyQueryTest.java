package com.teamgannon.trips.solarsystem.nightsky;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightSkyQueryTest {

    private static List<StarCatalogEntry> catalog() {
        return List.of(
                new StarCatalogEntry("Polaris", 2.5303, 89.2641, 1.98, "UMi"),
                new StarCatalogEntry("Sirius", 6.7525, -16.7161, -1.46, "CMa"),
                new StarCatalogEntry("Betelgeuse", 5.9195, 7.4071, 0.50, "Ori"),
                new StarCatalogEntry("Vega", 18.6156, 38.7837, 0.03, "Lyr"),
                new StarCatalogEntry("Arcturus", 14.2610, 19.1825, -0.05, "Boo")
        );
    }

    @Test
    void filtersByAltitude() {
        NightSkyQuery query = new NightSkyQuery();
        ObserverLocation obs = new ObserverLocation(51.4779, 0.0);
        Instant t = Instant.parse("2000-01-01T12:00:00Z");
        SkyQueryOptions opts = new SkyQueryOptions(10.0, 25, SkyQueryOptions.SortMode.BRIGHTEST, 6.0);

        List<AltAzResult> results = query.visibleStars(catalog(), obs, t, opts);
        assertTrue(results.stream().allMatch(r -> r.getAltitudeDeg() > 10.0));
    }

    @Test
    void brightestSortOrdersByMagnitude() {
        NightSkyQuery query = new NightSkyQuery();
        ObserverLocation obs = new ObserverLocation(51.4779, 0.0);
        Instant t = Instant.parse("2000-01-01T12:00:00Z");
        SkyQueryOptions opts = new SkyQueryOptions(-90.0, 25, SkyQueryOptions.SortMode.BRIGHTEST, 6.0);

        List<AltAzResult> results = query.visibleStars(catalog(), obs, t, opts);
        for (int i = 1; i < results.size(); i++) {
            double prevMag = results.get(i - 1).getStar().getMagnitude();
            double currMag = results.get(i).getStar().getMagnitude();
            assertTrue(prevMag <= currMag);
        }
    }

    @Test
    void formatterIncludesNameAndNumbers() {
        NightSkyQuery query = new NightSkyQuery();
        ObserverLocation obs = new ObserverLocation(51.4779, 0.0);
        Instant t = Instant.parse("2000-01-01T12:00:00Z");
        SkyQueryOptions opts = new SkyQueryOptions(-90.0, 5, SkyQueryOptions.SortMode.BRIGHTEST, 6.0);

        List<AltAzResult> results = query.visibleStars(catalog(), obs, t, opts);
        String text = SkyDescriptionFormatter.format(results, opts);

        assertTrue(text.contains("Sirius"));
        assertTrue(text.contains("alt"));
        assertTrue(text.contains("az"));
        assertTrue(text.contains("mag"));
    }
}
