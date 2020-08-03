package com.teamgannon.trips.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Describes an object in a solar system
 * <p>
 * Created by larrymitchell on 2017-01-24.
 */
@Slf4j
@Data
public class SystemObject implements Serializable {

    private static final long serialVersionUID = 3275145853753732419L;

    /**
     * used for JSON serialization
     */
    private final static ObjectMapper mapper = new ObjectMapper();

    final private UUID id = UUID.randomUUID();

    private String name;

    private String shortDescription;

    private String longDescription;

    private OrbitalParameters orbitalParameters;

    public String convertToJson() {
        return convertToJson(this);
    }

    public String convertToJson(SystemObject systemObject) {
        try {
            String systemObjectStr = mapper.writeValueAsString(systemObject);
            log.debug("serialized as:" + systemObjectStr);
            return systemObjectStr;
        } catch (JsonProcessingException e) {
            log.error("couldn't serialize this {} because of {}:", systemObject, e.getMessage());
            return "";
        }
    }

    public String convertToJson(List<SystemObject> systemObjects) {
        try {
            String systemObjectStr = mapper.writeValueAsString(systemObjects);
            log.debug("serialized as:" + systemObjectStr);
            return systemObjectStr;
        } catch (JsonProcessingException e) {
            log.error("couldn't serialize this {} because of {}:", systemObjects, e.getMessage());
            return "";
        }
    }

    public List<SystemObject> toSystemObject(String parametersStr) {
        try {
            return mapper.readValue(parametersStr, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("couldn't deserialize this {} because of {}:", parametersStr, e.getMessage());
            return null;
        }
    }

}
