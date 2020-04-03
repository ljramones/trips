package com.teamgannon.trips.elasticsearch.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * Describes an object in a solar system
 * <p>
 * Created by larrymitchell on 2017-01-24.
 */
@Slf4j
@Data
public class SystemObject implements Serializable {

    private static final long serialVersionUID = 3275145853753732419L;

    private long id;

    private String name;

    private String shortDescription;

    private String longDescription;

    private OrbitalParameters orbitalParameters;

    public String convertToString() {
        return convertToString(this);
    }

    public String convertToString(SystemObject systemObject) {

        return "id:" + systemObject.getId() + "," +
                "name:" + systemObject.getName() + "," +
                "shortDescription:" + systemObject.getShortDescription() + "," +
                "longDescription:" + systemObject.getLongDescription() + "::" +
                // embed sub object
                systemObject.getOrbitalParameters().convertToString();
    }

    public SystemObject toSystemObject(String systemObjectStr) {
        SystemObject systemObject = new SystemObject();

        // split the string into the main and sub object
        String[] objects = systemObjectStr.split("::");

        // parse the orbital parameters subobject
        OrbitalParameters parameters = new OrbitalParameters().toOrbitalParameters(objects[1]);
        systemObject.setOrbitalParameters(parameters);

        String[] fields = objects[0].split(",");
        try {
            for (String field : fields) {
                String[] attribute = field.split(":");
                switch (attribute[0]) {
                    case "id":
                        systemObject.setId(Long.parseLong(attribute[1]));
                        break;
                    case "name":
                        systemObject.setName(attribute[1]);
                        break;
                    case "shortDescription":
                        systemObject.setShortDescription(attribute[1]);
                        break;
                    case "longDescription":
                        systemObject.setLongDescription(attribute[1]);
                        break;
                    default:
                        log.error("unknown label:{}", attribute[0]);
                        break;
                }
            }
        } catch (NumberFormatException nfe) {
            log.error("bad format error:{}", nfe);
        }
        return systemObject;
    }

}
