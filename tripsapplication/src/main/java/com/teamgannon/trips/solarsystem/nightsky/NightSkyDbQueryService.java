package com.teamgannon.trips.solarsystem.nightsky;

import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class NightSkyDbQueryService {

    private final StarObjectRepository starObjectRepository;
    private final StarObjectNightSkyAdapter adapter = new StarObjectNightSkyAdapter();
    private final NightSkyQuery nightSkyQuery = new NightSkyQuery();

    public NightSkyDbQueryService(StarObjectRepository starObjectRepository) {
        this.starObjectRepository = starObjectRepository;
    }

    public String describeVisibleSkyFromDb(ObserverLocation obs,
                                           Instant t,
                                           double minAltDeg,
                                           int maxResults) {
        List<StarCatalogEntry> catalog = new ArrayList<>();
        for (StarObject star : starObjectRepository.findAll()) {
            StarCatalogEntry entry = adapter.fromStarObject(star);
            if (entry != null) {
                catalog.add(entry);
            }
        }

        SkyQueryOptions opts = new SkyQueryOptions(minAltDeg, maxResults, SkyQueryOptions.SortMode.BRIGHTEST,
                Double.POSITIVE_INFINITY);
        List<AltAzResult> results = nightSkyQuery.visibleStars(catalog, obs, t, opts);
        return SkyDescriptionFormatter.format(results, opts);
    }
}
