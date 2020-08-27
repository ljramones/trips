package com.teamgannon.trips.dataset.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamgannon.trips.dataset.enums.CustomFieldType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The custom data definition
 * <p>
 * End-users will want to store data not in the basic database about objects.  In order to do that, they can
 * define custom data fields which are stored as meta-data and linked to individual star objects.
 * <p>
 * Created by larrymitchell on 2017-03-02.
 */
@Slf4j
@Data
public class CustomDataDefinition {

    /**
     * used for JSON serialization
     */
    private final static ObjectMapper mapper = new ObjectMapper();

    /**
     * specific id of this
     */
    private UUID id = UUID.randomUUID();

    /**
     * The name to display when entering or editing a custom field
     */
    private String customFieldName;

    /**
     * Text, numeric, multiple picklist or single picklist.
     */
    private CustomFieldType customFieldType;

    /**
     * Width of a text or numeric type.
     */
    private String customFieldWidth;

    /**
     * An array of the choices for custom fields that are picklists.
     * <p>
     * If a field is of type “multiple” then the user can select several of the items. For example, a multiple
     * could be “Installations: Military, Antimatter, Civilian, Jovian Fuel Station, Shopping Mall”
     * <p>
     * If a picklist field is of type single, the user can select only one option out of the list. An example
     * of a single could be “Population class: <1 mill, <100 mill, <1 billion, >1billion”
     */
    private List<String> customFieldOptions = new ArrayList<>();

    public String convertToJson() {
        return convertToJson(this);
    }

    public String convertToJson(CustomDataDefinition customDataDefinition) {
        try {
            String customDataDefinitionStr = mapper.writeValueAsString(customDataDefinition);
            log.debug("serialized as:" + customDataDefinitionStr);
            return customDataDefinitionStr;
        } catch (IOException e) {
            log.error("couldn't serialize this {} because of {}:", customDataDefinition, e.getMessage());
            return "";
        }
    }

    public String convertToJson(List<CustomDataDefinition> customDataDefinitionList) {
        try {
            String customDataDefinitionStr = mapper.writeValueAsString(customDataDefinitionList);
            log.debug("serialized as:" + customDataDefinitionStr);
            return customDataDefinitionStr;
        } catch (IOException e) {
            log.error("couldn't serialize this {} because of {}:", customDataDefinitionList, e.getMessage());
            return "";
        }
    }

    public List<CustomDataDefinition> toCustomDataDefinition(String parametersStr) {
        try {
            return mapper.readValue(parametersStr, new TypeReference<>() {
            });
        } catch (IOException e) {
            log.error("couldn't deserialize this {} because of {}:", parametersStr, e.getMessage());
            return null;
        }
    }

}
