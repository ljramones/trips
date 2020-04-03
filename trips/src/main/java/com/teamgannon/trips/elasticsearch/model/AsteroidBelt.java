package com.teamgannon.trips.elasticsearch.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * Defines an asteroid belt
 * <p>
 * Created by larrymitchell on 2017-01-24.
 */
@Slf4j
@Data
public class AsteroidBelt implements Serializable {

    private static final long serialVersionUID = -1717094515770000268L;

    /**
     * whether one exists or not
     */
    private boolean present = false;

    /**
     * inner edge of the asteroid belt
     */
    private double minRadius;

    /**
     * outer edge of the asteroid belt
     */
    private double maxRadius;


    public String convertToString() {
        return convertToString(this);
    }

    private String convertToString(AsteroidBelt asteroidBelt) {
        return "present:" + asteroidBelt.isPresent() + "," +
                "minRadius:" + asteroidBelt.getMinRadius() + "," +
                "maxRadius:" + asteroidBelt.getMaxRadius();
    }

    public AsteroidBelt toAsteroidBelt(String parametersStr) {
        AsteroidBelt parameters = new AsteroidBelt();
        String[] fields = parametersStr.split(",");
        try {
            for (String field : fields) {
                String[] attribute = field.split(":");
                switch (attribute[0]) {
                    case "present":
                        parameters.setPresent(Boolean.parseBoolean(attribute[1]));
                        break;
                    case "minRadius":
                        parameters.setMinRadius(Double.parseDouble(attribute[1]));
                        break;
                    case "maxRadius":
                        parameters.setMaxRadius(Double.parseDouble(attribute[1]));
                        break;
                    default:
                        log.error("unknown label: {}", attribute[0]);
                }
            }
        } catch (NumberFormatException nfe) {
            log.error("bad format error:{}", nfe);
        }
        return parameters;
    }

}
