package com.teamgannon.trips.dataset.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

/**
 * Stores the values of the custom data for various star objects.
 * <p>
 * Created by larrymitchell on 2017-03-02.
 */
@Slf4j
@Data
public class CustomDataValue {

    /**
     * used for JSON serialization
     */
    private final static ObjectMapper mapper = new ObjectMapper();

    private UUID id;

    private String customFieldName;

    private String customFieldValue;

    public String convertToJson() {
        return convertToJson(this);
    }

    public String convertToJson(CustomDataValue customDataValue) {
        try {
            String customDataValueStr = mapper.writeValueAsString(customDataValue);
            log.debug("serialized as:" + customDataValueStr);
            return customDataValueStr;
        } catch (JsonProcessingException e) {
            log.error("couldn't serialize this {} because of {}:", customDataValue, e.getMessage());
            return "";
        }
    }

    public String convertToJson(List<CustomDataValue> customDataValues) {
        try {
            String customDataValueStr = mapper.writeValueAsString(customDataValues);
            log.debug("serialized as:" + customDataValueStr);
            return customDataValueStr;
        } catch (JsonProcessingException e) {
            log.error("couldn't serialize this {} because of {}:", customDataValues, e.getMessage());
            return "";
        }
    }

    public List<CustomDataValue> toCustomDataValue(String parametersStr) {
        try {
            return mapper.readValue(parametersStr, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("couldn't deserialize this {} because of {}:", parametersStr, e.getMessage());
            return null;
        }
    }
}
