package com.teamgannon.trips.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamgannon.trips.dataset.model.CustomDataDefinition;
import com.teamgannon.trips.dataset.model.CustomDataValue;
import com.teamgannon.trips.dataset.model.Theme;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.model.Route;
import com.teamgannon.trips.transits.TransitDefinitions;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for serializing and deserializing complex objects
 * stored as JSON strings in DataSetDescriptor entities.
 * <p>
 * This centralizes all JSON conversion logic that was previously scattered
 * across the entity and model classes.
 */
@Slf4j
@Service
public class DataSetDescriptorSerializationService {

    private final ObjectMapper objectMapper;

    public DataSetDescriptorSerializationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ==================== Theme Serialization ====================

    /**
     * Deserialize a Theme from its JSON string representation.
     *
     * @param themeStr the JSON string, may be null or empty
     * @return the Theme object, or null if deserialization fails
     */
    public @Nullable Theme deserializeTheme(@Nullable String themeStr) {
        if (themeStr == null || themeStr.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(themeStr, Theme.class);
        } catch (IOException e) {
            log.error("Failed to deserialize Theme from: {} - {}", themeStr, e.getMessage());
            return null;
        }
    }

    /**
     * Serialize a Theme to its JSON string representation.
     *
     * @param theme the Theme object, may be null
     * @return the JSON string, or empty string if serialization fails
     */
    public @NotNull String serializeTheme(@Nullable Theme theme) {
        if (theme == null) {
            return "";
        }
        try {
            String json = objectMapper.writeValueAsString(theme);
            log.debug("Serialized Theme: {}", json);
            return json;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Theme: {} - {}", theme, e.getMessage());
            return "";
        }
    }

    /**
     * Get Theme from a DataSetDescriptor.
     */
    public @Nullable Theme getTheme(@NotNull DataSetDescriptor descriptor) {
        return deserializeTheme(descriptor.getThemeStr());
    }

    /**
     * Set Theme on a DataSetDescriptor.
     */
    public void setTheme(@NotNull DataSetDescriptor descriptor, @Nullable Theme theme) {
        descriptor.setThemeStr(serializeTheme(theme));
    }

    // ==================== Routes Serialization ====================

    /**
     * Deserialize a list of Routes from their JSON string representation.
     *
     * @param routesStr the JSON string, may be null or empty
     * @return the list of Routes, never null (empty list on failure)
     */
    public @NotNull List<Route> deserializeRoutes(@Nullable String routesStr) {
        if (routesStr == null || routesStr.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(routesStr, new TypeReference<List<Route>>() {});
        } catch (IOException e) {
            log.error("Failed to deserialize Routes from: {} - {}", routesStr, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Serialize a list of Routes to their JSON string representation.
     *
     * @param routes the list of Routes, may be null or empty
     * @return the JSON string, or empty string if serialization fails
     */
    public @NotNull String serializeRoutes(@Nullable List<Route> routes) {
        if (routes == null || routes.isEmpty()) {
            return "";
        }
        try {
            String json = objectMapper.writeValueAsString(routes);
            log.debug("Serialized {} routes", routes.size());
            return json;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Routes: {} - {}", routes, e.getMessage());
            return "";
        }
    }

    /**
     * Get Routes from a DataSetDescriptor.
     */
    public @NotNull List<Route> getRoutes(@NotNull DataSetDescriptor descriptor) {
        return deserializeRoutes(descriptor.getRoutesStr());
    }

    /**
     * Set Routes on a DataSetDescriptor.
     */
    public void setRoutes(@NotNull DataSetDescriptor descriptor, @Nullable List<Route> routes) {
        descriptor.setRoutesStr(serializeRoutes(routes));
        descriptor.setNumberRoutes(routes != null ? routes.size() : 0);
    }

    // ==================== CustomDataDefinition Serialization ====================

    /**
     * Deserialize a list of CustomDataDefinitions from their JSON string representation.
     *
     * @param customDataDefsStr the JSON string, may be null or empty
     * @return the list of CustomDataDefinitions, never null (empty list on failure)
     */
    public @NotNull List<CustomDataDefinition> deserializeCustomDataDefinitions(@Nullable String customDataDefsStr) {
        if (customDataDefsStr == null || customDataDefsStr.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(customDataDefsStr, new TypeReference<List<CustomDataDefinition>>() {});
        } catch (IOException e) {
            log.error("Failed to deserialize CustomDataDefinitions from: {} - {}", customDataDefsStr, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Serialize a list of CustomDataDefinitions to their JSON string representation.
     *
     * @param definitions the list of CustomDataDefinitions, may be null or empty
     * @return the JSON string, or empty string if serialization fails
     */
    public @NotNull String serializeCustomDataDefinitions(@Nullable List<CustomDataDefinition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return "";
        }
        try {
            String json = objectMapper.writeValueAsString(definitions);
            log.debug("Serialized {} custom data definitions", definitions.size());
            return json;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize CustomDataDefinitions: {} - {}", definitions, e.getMessage());
            return "";
        }
    }

    /**
     * Get CustomDataDefinitions from a DataSetDescriptor.
     */
    public @NotNull List<CustomDataDefinition> getCustomDataDefinitions(@NotNull DataSetDescriptor descriptor) {
        return deserializeCustomDataDefinitions(descriptor.getCustomDataDefsStr());
    }

    /**
     * Set CustomDataDefinitions on a DataSetDescriptor.
     */
    public void setCustomDataDefinitions(@NotNull DataSetDescriptor descriptor, @Nullable List<CustomDataDefinition> definitions) {
        descriptor.setCustomDataDefsStr(serializeCustomDataDefinitions(definitions));
    }

    // ==================== CustomDataValue Serialization ====================

    /**
     * Deserialize a list of CustomDataValues from their JSON string representation.
     *
     * @param customDataValuesStr the JSON string, may be null or empty
     * @return the list of CustomDataValues, never null (empty list on failure)
     */
    public @NotNull List<CustomDataValue> deserializeCustomDataValues(@Nullable String customDataValuesStr) {
        if (customDataValuesStr == null || customDataValuesStr.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(customDataValuesStr, new TypeReference<List<CustomDataValue>>() {});
        } catch (IOException e) {
            log.error("Failed to deserialize CustomDataValues from: {} - {}", customDataValuesStr, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Serialize a list of CustomDataValues to their JSON string representation.
     *
     * @param values the list of CustomDataValues, may be null or empty
     * @return the JSON string, or empty string if serialization fails
     */
    public @NotNull String serializeCustomDataValues(@Nullable List<CustomDataValue> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        try {
            String json = objectMapper.writeValueAsString(values);
            log.debug("Serialized {} custom data values", values.size());
            return json;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize CustomDataValues: {} - {}", values, e.getMessage());
            return "";
        }
    }

    /**
     * Get CustomDataValues from a DataSetDescriptor.
     */
    public @NotNull List<CustomDataValue> getCustomDataValues(@NotNull DataSetDescriptor descriptor) {
        return deserializeCustomDataValues(descriptor.getCustomDataValuesStr());
    }

    /**
     * Set CustomDataValues on a DataSetDescriptor.
     */
    public void setCustomDataValues(@NotNull DataSetDescriptor descriptor, @Nullable List<CustomDataValue> values) {
        descriptor.setCustomDataValuesStr(serializeCustomDataValues(values));
    }

    // ==================== TransitDefinitions Serialization ====================

    /**
     * Deserialize TransitDefinitions from their JSON string representation.
     *
     * @param transitPrefsStr the JSON string, may be null or empty
     * @param dataSetName     the dataset name to use for default TransitDefinitions
     * @return the TransitDefinitions object, or a default instance if deserialization fails
     */
    public @NotNull TransitDefinitions deserializeTransitDefinitions(@Nullable String transitPrefsStr, @NotNull String dataSetName) {
        if (transitPrefsStr == null || transitPrefsStr.isBlank()) {
            return createDefaultTransitDefinitions(dataSetName);
        }
        try {
            return objectMapper.readValue(transitPrefsStr, TransitDefinitions.class);
        } catch (IOException e) {
            log.error("Failed to deserialize TransitDefinitions from: {} - {}", transitPrefsStr, e.getMessage());
            return createDefaultTransitDefinitions(dataSetName);
        }
    }

    /**
     * Serialize TransitDefinitions to their JSON string representation.
     *
     * @param transitDefinitions the TransitDefinitions object, may be null
     * @return the JSON string, or empty string if serialization fails
     */
    public @NotNull String serializeTransitDefinitions(@Nullable TransitDefinitions transitDefinitions) {
        if (transitDefinitions == null) {
            return "";
        }
        try {
            String json = objectMapper.writeValueAsString(transitDefinitions);
            log.debug("Serialized TransitDefinitions: {}", json);
            return json;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize TransitDefinitions: {} - {}", transitDefinitions, e.getMessage());
            return "";
        }
    }

    /**
     * Get TransitDefinitions from a DataSetDescriptor.
     */
    public @NotNull TransitDefinitions getTransitDefinitions(@NotNull DataSetDescriptor descriptor) {
        return deserializeTransitDefinitions(descriptor.getTransitPreferencesStr(), descriptor.getDataSetName());
    }

    /**
     * Set TransitDefinitions on a DataSetDescriptor.
     */
    public void setTransitDefinitions(@NotNull DataSetDescriptor descriptor, @Nullable TransitDefinitions transitDefinitions) {
        descriptor.setTransitPreferencesStr(serializeTransitDefinitions(transitDefinitions));
    }

    private @NotNull TransitDefinitions createDefaultTransitDefinitions(@NotNull String dataSetName) {
        TransitDefinitions transitDefinitions = new TransitDefinitions();
        transitDefinitions.setSelected(false);
        transitDefinitions.setDataSetName(dataSetName);
        transitDefinitions.setTransitRangeDefs(new ArrayList<>());
        return transitDefinitions;
    }

    // ==================== AstrographicDataList Serialization ====================

    /**
     * Deserialize a set of UUIDs from a comma-separated string.
     *
     * @param astrographicDataList the comma-separated UUID string, may be null or empty
     * @return the set of UUIDs, never null (empty set on failure)
     */
    public @NotNull Set<UUID> deserializeAstrographicDataList(@Nullable String astrographicDataList) {
        if (astrographicDataList == null || astrographicDataList.isBlank()) {
            return new HashSet<>();
        }
        List<String> stringList = Arrays.asList(astrographicDataList.split("\\s*,\\s*"));
        return stringList.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }

    /**
     * Serialize a set of UUIDs to a comma-separated string.
     *
     * @param uuidSet the set of UUIDs, may be null or empty
     * @return the comma-separated string
     */
    public @NotNull String serializeAstrographicDataList(@Nullable Set<UUID> uuidSet) {
        if (uuidSet == null || uuidSet.isEmpty()) {
            return "";
        }
        return uuidSet.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));
    }

    /**
     * Get AstrographicDataList from a DataSetDescriptor.
     */
    public @NotNull Set<UUID> getAstrographicDataList(@NotNull DataSetDescriptor descriptor) {
        return deserializeAstrographicDataList(descriptor.getAstrographicDataList());
    }

    /**
     * Set AstrographicDataList on a DataSetDescriptor.
     */
    public void setAstrographicDataList(@NotNull DataSetDescriptor descriptor, @NotNull Set<UUID> uuidSet) {
        descriptor.setAstrographicDataList(serializeAstrographicDataList(uuidSet));
        descriptor.setNumberStars((long) uuidSet.size());
    }
}
