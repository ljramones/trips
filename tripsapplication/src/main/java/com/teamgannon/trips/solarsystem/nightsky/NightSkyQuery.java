package com.teamgannon.trips.solarsystem.nightsky;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class NightSkyQuery {

    public List<AltAzResult> visibleStars(List<StarCatalogEntry> catalog,
                                          ObserverLocation obs,
                                          Instant t,
                                          SkyQueryOptions opts) {
        SkyQueryOptions options = opts == null ? new SkyQueryOptions() : opts;
        List<AltAzResult> results = new ArrayList<>();

        for (StarCatalogEntry star : catalog) {
            if (star == null) {
                continue;
            }
            if (star.getMagnitude() > options.getMinMagnitude()) {
                continue;
            }
            EquatorialCoordinates eq = new EquatorialCoordinates(star.getRaHours(), star.getDecDeg());
            AltAz altAz = NightSkyMath.equatorialToAltAz(eq, obs, t);
            if (altAz.getAltitudeDeg() > options.getMinAltitudeDeg()) {
                results.add(new AltAzResult(star, altAz.getAltitudeDeg(), altAz.getAzimuthDeg()));
            }
        }

        results.sort(resultComparator(options.getSortMode()));
        if (results.size() > options.getMaxResults()) {
            return results.subList(0, options.getMaxResults());
        }
        return results;
    }

    private Comparator<AltAzResult> resultComparator(SkyQueryOptions.SortMode sortMode) {
        if (sortMode == SkyQueryOptions.SortMode.HIGHEST) {
            return Comparator
                    .comparingDouble(AltAzResult::getAltitudeDeg).reversed()
                    .thenComparingDouble(r -> r.getStar().getMagnitude());
        }
        // BRIGHTEST: sort by magnitude ascending (lower = brighter), then altitude descending as tiebreaker
        return Comparator
                .comparingDouble((AltAzResult r) -> r.getStar().getMagnitude())
                .thenComparingDouble(r -> -r.getAltitudeDeg());
    }
}
