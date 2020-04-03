package com.teamgannon.trips.search;


import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.elasticsearch.model.AstrographicObject;
import com.teamgannon.trips.elasticsearch.repository.AstrographicObjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SearchService {

    private TripsContext tripsContext;
    private AstrographicObjectRepository repository;

    public SearchService(
            TripsContext tripsContext,
            AstrographicObjectRepository repository
    ) {
        this.tripsContext = tripsContext;
        this.repository = repository;
    }

    List<AstrographicObject> findAllStarsFromSol(double distance) {
        List<AstrographicObject> astrographicObjects = new ArrayList<>();


        return astrographicObjects;
    }


    List<AstrographicObject> findAllStarsFromXYZ(double distance) {
        List<AstrographicObject> astrographicObjects = new ArrayList<>();


        return astrographicObjects;
    }

    List<AstrographicObject> FindStarsFromQuery(AstroSearchQuery astroSearchQuery) {
        List<AstrographicObject> astrographicObjects = new ArrayList<>();


        return astrographicObjects;
    }


}
