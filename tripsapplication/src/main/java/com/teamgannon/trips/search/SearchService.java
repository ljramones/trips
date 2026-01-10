package com.teamgannon.trips.search;


import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SearchService {

    private final TripsContext tripsContext;
    private final StarObjectRepository repository;

    public SearchService(
            TripsContext tripsContext,
            StarObjectRepository repository
    ) {
        this.tripsContext = tripsContext;
        this.repository = repository;
    }

    @NotNull List<StarObject> findAllStarsFromSol(double distance) {
        AstroSearchQuery astroSearchQuery = new AstroSearchQuery();
        astroSearchQuery.setUpperDistanceLimit(distance);
        astroSearchQuery.setDescriptor(tripsContext.getSearchContext().getDataSetDescriptor());
        return repository.findBySearchQuery(astroSearchQuery);
    }


    @NotNull List<StarObject> findAllStarsFromXYZ(double distance) {
        AstroSearchQuery astroSearchQuery = new AstroSearchQuery();
        astroSearchQuery.setUpperDistanceLimit(distance);
        astroSearchQuery.setDescriptor(tripsContext.getSearchContext().getDataSetDescriptor());
        return repository.findBySearchQuery(astroSearchQuery);
    }

    @NotNull List<StarObject> FindStarsFromQuery(AstroSearchQuery astroSearchQuery) {
        return repository.findBySearchQuery(astroSearchQuery);
    }


}
