package com.teamgannon.trips.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to map to the various stellar objects and otain properties about them
 * <p>
 * Created by larrymitchell on 2017-01-29.
 */
@Service
public class InterstellarDatabase {


    public Map<String, String> getProperties(String objectId) {
        Map<String, String> objectProperties = new HashMap<>();

        // access elasticsearch repo

        // in the mean time, return an empty set
        objectProperties.put("Selected Item", objectId);
        objectProperties.put("Star Name", "Canis Major");
        objectProperties.put("Stellar Type", "Red Giant");
        objectProperties.put("Size", "4e10-30 kg");
        objectProperties.put("RA", "23'35");
        objectProperties.put("Declinaiton", "92'23");

        return objectProperties;
    }
}
