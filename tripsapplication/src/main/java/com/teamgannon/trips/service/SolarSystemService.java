package com.teamgannon.trips.service;


import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SolarSystemService {

    public SolarSystemService() {
    }


    public SolarSystemDescription getSolarSystem(StarDisplayRecord starDisplayRecord) {
        SolarSystemDescription solarSystemDescription = new SolarSystemDescription();
        solarSystemDescription.setStarDisplayRecord(starDisplayRecord);
        return solarSystemDescription;
    }

}
