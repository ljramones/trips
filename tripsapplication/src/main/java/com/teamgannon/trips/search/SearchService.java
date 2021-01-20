package com.teamgannon.trips.search;


import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
        List<StarObject> starObjects = new ArrayList<>();


        return starObjects;
    }


    @NotNull List<StarObject> findAllStarsFromXYZ(double distance) {
        List<StarObject> starObjects = new ArrayList<>();


        return starObjects;
    }

    @NotNull List<StarObject> FindStarsFromQuery(AstroSearchQuery astroSearchQuery) {
        List<StarObject> starObjects = new ArrayList<>();


        return starObjects;
    }


}
