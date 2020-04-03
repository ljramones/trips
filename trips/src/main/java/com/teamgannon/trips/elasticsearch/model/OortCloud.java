package com.teamgannon.trips.elasticsearch.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * Defines the oort cloud (can only be one)
 * <p>
 * Created by larrymitchell on 2017-01-24.
 */
@Slf4j
@Data
public class OortCloud implements Serializable {

    private static final long serialVersionUID = -4231601630887014677L;

    /**
     * whether one exists or not
     */
    private boolean present = false;

    /**
     * inner edge of the oort cloud
     */
    private double minRadius;

    /**
     * outer edge of the cloud
     */
    private double maxRadius;


    public String convertToString() {
        return convertToString(this);
    }

    private String convertToString(OortCloud oortCloud) {
        return "present:" + oortCloud.isPresent() + "," +
                "minRadius:" + oortCloud.getMinRadius() + "," +
                "maxRadius:" + oortCloud.getMaxRadius();
    }

    public OortCloud toOortCloud(String parametersStr) {
        OortCloud parameters = new OortCloud();
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
