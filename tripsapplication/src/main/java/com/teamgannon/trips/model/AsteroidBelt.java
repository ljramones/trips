package com.teamgannon.trips.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Defines an asteroid belt
 * <p>
 * Created by larrymitchell on 2017-01-24.
 */
@Slf4j
@Data
public class AsteroidBelt implements Serializable {

    private static final long serialVersionUID = -4517069311241008353L;

    /**
     * used for JSON serialization
     */
    private final static ObjectMapper mapper = new ObjectMapper();

    /**
     * the asteroid id
     */
    final private UUID id = UUID.randomUUID();

    /**
     * whether one exists or not
     */
    private final boolean present = false;

    /**
     * inner edge of the asteroid belt
     */
    private double minRadius;

    /**
     * outer edge of the asteroid belt
     */
    private double maxRadius;


    public String convertToJson() {
        return convertToJson(this);
    }

    public String convertToJson(AsteroidBelt asteroidBelt) {
        try {
            String asteroidStr = mapper.writeValueAsString(asteroidBelt);
            log.debug("serialized as:" + asteroidStr);
            return asteroidStr;
        } catch (IOException e) {
            log.error("couldn't serialize this {} because of {}:", asteroidBelt, e.getMessage());
            return "";
        }
    }

    public String convertToJson(List<AsteroidBelt> asteroidBelts) {
        try {
            String asteroidStr = mapper.writeValueAsString(asteroidBelts);
            log.debug("serialized as:" + asteroidStr);
            return asteroidStr;
        } catch (IOException e) {
            log.error("couldn't serialize this {} because of {}:", asteroidBelts, e.getMessage());
            return "";
        }
    }

    public AsteroidBelt toAsteroidBelt(String parametersStr) {
        try {
            return mapper.readValue(parametersStr, AsteroidBelt.class);
        } catch (IOException e) {
            log.error("couldn't deserialize this {} because of {}:", parametersStr, e.getMessage());
            return null;
        }
    }

}
