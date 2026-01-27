package com.teamgannon.trips.jpa.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamgannon.trips.dataset.model.CustomDataDefinition;
import com.teamgannon.trips.dataset.model.CustomDataValue;
import com.teamgannon.trips.dataset.model.Theme;
import com.teamgannon.trips.routing.model.Route;
import com.teamgannon.trips.service.export.model.DataSetDescriptorDTO;
import com.teamgannon.trips.transits.TransitDefinitions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Note: Complex object serialization has been extracted to DataSetDescriptorSerializationService.
 * The legacy methods in this entity are preserved for backward compatibility but delegate to
 * static helper methods. New code should use DataSetDescriptorSerializationService instead.
 */

/**
 * The new model for the dataset descriptor
 * <p>
 * Created by larrymitchell on 2017-03-28.
 */
@Slf4j
@Getter
@Setter
@ToString(exclude = {"themeStr", "astrographicDataList", "routesStr", "customDataDefsStr", "customDataValuesStr", "transitPreferencesStr"})
@RequiredArgsConstructor
@Entity
public class DataSetDescriptor implements Serializable {

    @Serial
    private static final long serialVersionUID = 1132779255908975239L;

    /**
     * used for JSON serialization
     */
    @Transient
    private final static ObjectMapper mapper = new ObjectMapper();

    /**
     * name for the dataset
     * <p>
     * e.g. “150LY Sphere Cain Riordan”
     */
    @Id
    private String dataSetName;

    /**
     * this is the file path where we got the data
     */
    @NotNull
    private String filePath;

    /**
     * name of the dataset file creator
     */
    @NotNull
    private String fileCreator;

    /**
     * the creation date of this dataset
     * <p>
     * e.g. “2017-02-27”
     */
    private long fileOriginalDate = Instant.now().toEpochMilli();

    /**
     * notes on the data in the file
     * <p>
     * e.g. A free-form text field containing notes about what this files purpose is, TTR or Honorverse,
     * possibly specifying things like ‘As of Raising Caine.’
     */
    private String fileNotes;

    /**
     * what type of data was this loaded form
     */
    private String datasetType;

    /**
     * number of entities in this dataset
     */
    private Long numberStars = 0L;

    /**
     * the max radius span of this dataset
     */
    private double distanceRange = 20;

    /**
     * number of routes in dataset
     */
    private Integer numberRoutes = 0;

    /**
     * the theme object stored as a json string
     */
    @Lob
    private String themeStr;
    /**
     * a container object for astrographic data
     * this is actually a command separated list of UUIDs
     * we have to convert back and forth between a set of UUIDs and a string on getter and setter methods
     * because although JPA allows lists adn sets through ElementCollection, it is a pain in the ass to use
     * and completely error-prone
     */
    @Lob
    private String astrographicDataList;
    /**
     * an object describing routes
     */
    @Lob
    @Column(length = 1000)
    private String routesStr;
    /**
     * a set of custom data definitions
     */
    @Lob
    private String customDataDefsStr;
    /**
     * a set of custom data values
     */
    @Lob
    private String customDataValuesStr;

    @Lob
    @Column(length = 1000)
    private String transitPreferencesStr;

    /**
     * Get the Theme object by deserializing from JSON.
     *
     * @return the Theme object
     * @deprecated Use DataSetDescriptorSerializationService.getTheme() instead
     */
    @Deprecated
    public Theme getTheme() {
        return new Theme().toTheme(themeStr);
    }

    /**
     * Set the theme by converting it to JSON.
     *
     * @param theme the theme
     * @deprecated Use DataSetDescriptorSerializationService.setTheme() instead
     */
    @Deprecated
    public void setTheme(Theme theme) {
        themeStr = new Theme().convertToJson(theme);
    }

    /**
     * Get the list of Routes by deserializing from JSON.
     *
     * @return the list of routes
     * @deprecated Use DataSetDescriptorSerializationService.getRoutes() instead
     */
    @Deprecated
    public List<Route> getRoutes() {
        if (routesStr != null) {
            if (!routesStr.isBlank()) {
                return new Route().toRoute(routesStr);
            } else {
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Set the list of routes by converting to JSON.
     *
     * @param routes the list of routes
     * @deprecated Use DataSetDescriptorSerializationService.setRoutes() instead
     */
    @Deprecated
    public void setRoutes(List<Route> routes) {
        numberRoutes = routes.size();
        routesStr = new Route().convertToJson(routes);
    }

    public void clearRoutes() {
        setRoutes(new ArrayList<>());
    }

    public void resetDate() {
        fileOriginalDate = Instant.now().toEpochMilli();
    }

    /**
     * Get the list of CustomDataDefinitions by deserializing from JSON.
     *
     * @return the list of custom data definitions
     * @deprecated Use DataSetDescriptorSerializationService.getCustomDataDefinitions() instead
     */
    @Deprecated
    public List<CustomDataDefinition> getCustomDataDefinitions() {
        return new CustomDataDefinition().toCustomDataDefinition(customDataDefsStr);
    }

    /**
     * Set the list of custom data definitions by converting to JSON.
     *
     * @param customDataDefinitions the list of custom data definitions
     * @deprecated Use DataSetDescriptorSerializationService.setCustomDataDefinitions() instead
     */
    @Deprecated
    public void setCustomDataDefinition(List<CustomDataDefinition> customDataDefinitions) {
        customDataDefsStr = new CustomDataDefinition().convertToJson(customDataDefinitions);
    }

    /**
     * Get the list of CustomDataValues by deserializing from JSON.
     *
     * @return the list of custom data values
     * @deprecated Use DataSetDescriptorSerializationService.getCustomDataValues() instead
     */
    @Deprecated
    public List<CustomDataValue> getCustomDataValue() {
        return new CustomDataValue().toCustomDataValue(customDataValuesStr);
    }

    /**
     * Set the list of custom data values by converting to JSON.
     *
     * @param customDataValues the list of custom data values
     * @deprecated Use DataSetDescriptorSerializationService.setCustomDataValues() instead
     */
    @Deprecated
    public void setCustomDataValue(List<CustomDataValue> customDataValues) {
        customDataValuesStr = new CustomDataValue().convertToJson(customDataValues);
    }

    /**
     * @deprecated Use getAstrographicDataList() instead (generated by Lombok)
     */
    @Deprecated
    public String getAstroDataString() {
        return astrographicDataList;
    }

    /**
     * @deprecated Use setAstrographicDataList() instead (generated by Lombok)
     */
    @Deprecated
    public void setAstroDataString(String astrographicDataList) {
        this.astrographicDataList = astrographicDataList;
    }

    /**
     * Get the set of astrographic data UUIDs by parsing the comma-separated string.
     *
     * @return the set of UUIDs
     * @deprecated Use DataSetDescriptorSerializationService.getAstrographicDataList() instead
     */
    @Deprecated
    public Set<UUID> getAstrographicDataUUIDs() {
        // if its null return an empty list
        if (astrographicDataList == null) {
            return new HashSet<>();
        }
        if (astrographicDataList.isBlank()) {
            return new HashSet<>();
        }
        // separate and convert to a list
        List<String> stringList = Arrays.asList(astrographicDataList.split("\\s*,\\s*"));

        // convert to an array list of UUIDs
        return stringList.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }

    /**
     * Set the astrographic data list from a set of UUIDs.
     *
     * @param uuidList the set of UUIDs
     * @deprecated Use DataSetDescriptorSerializationService.setAstrographicDataList() instead
     */
    @Deprecated
    public void setAstrographicDataUUIDs(@org.jetbrains.annotations.NotNull Set<UUID> uuidList) {
        numberStars = (long) uuidList.size();
        astrographicDataList = uuidList.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));
    }

    public @org.jetbrains.annotations.NotNull String getCreationDate() {
        Date date = new Date(fileOriginalDate);
        SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");
        return df2.format(date);
    }

    public String getNotes() {
        return fileNotes;
    }

    public @org.jetbrains.annotations.NotNull String getToolTipText() {
        return "name:" + dataSetName + "\n"
                + "creator: " + fileCreator + "\n"
                + "type: " + datasetType + "\n"
                + "notes: " + fileNotes;
    }

    public DataSetDescriptorDTO toDataSetDescriptorDTO() {
        DataSetDescriptorDTO dto = new DataSetDescriptorDTO();

        dto.setDataSetName(dataSetName);
        dto.setFilePath(filePath);
        dto.setFileCreator(fileCreator);
        dto.setFileOriginalDate(fileOriginalDate);
        dto.setFileNotes(fileNotes);
        dto.setDatasetType(datasetType);
        dto.setNumberStars(numberStars);
        dto.setDistanceRange(distanceRange);
        dto.setNumberRoutes(numberRoutes);
        dto.setThemeStr(themeStr);
        dto.setAstrographicDataList(astrographicDataList);
        dto.setRoutesStr(routesStr);
        dto.setCustomDataDefsStr(customDataDefsStr);
        dto.setCustomDataValuesStr(customDataValuesStr);

        return dto;
    }

    /**
     * Get TransitDefinitions by deserializing from JSON.
     *
     * @return the TransitDefinitions object
     * @deprecated Use DataSetDescriptorSerializationService.getTransitDefinitions() instead
     */
    @Deprecated
    public TransitDefinitions getTransitDefinitions() {
        if (transitPreferencesStr == null) {
            TransitDefinitions transitDefinitions = new TransitDefinitions();
            transitDefinitions.setSelected(false);
            transitDefinitions.setDataSetName(dataSetName);
            transitDefinitions.setTransitRangeDefs(new ArrayList<>());
            return transitDefinitions;
        }
        try {
            return mapper.readValue(transitPreferencesStr, new TypeReference<>() {
            });
        } catch (IOException e) {
            log.error("couldn't deserialize this {} because of {}:", transitPreferencesStr, e.getMessage());
            return null;
        }
    }

    /**
     * Set TransitDefinitions by converting to JSON.
     *
     * @param transitDefinitions the TransitDefinitions object
     * @deprecated Use DataSetDescriptorSerializationService.setTransitDefinitions() instead
     */
    @Deprecated
    public void setTransitDefinitions(TransitDefinitions transitDefinitions) {
        try {
            String transitDefsStr = mapper.writeValueAsString(transitDefinitions);
            log.debug("serialized as:" + transitDefsStr);
            transitPreferencesStr = transitDefsStr;
        } catch (IOException e) {
            log.error("couldn't serialize this {} because of {}:", transitDefinitions, e.getMessage());
            transitPreferencesStr = "";
        }
    }

    // Manual setters (Lombok @Setter should generate these but adding explicitly)
    public void setDataSetName(String dataSetName) {
        this.dataSetName = dataSetName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setFileCreator(String fileCreator) {
        this.fileCreator = fileCreator;
    }

    public void setFileOriginalDate(long fileOriginalDate) {
        this.fileOriginalDate = fileOriginalDate;
    }

    public void setFileNotes(String fileNotes) {
        this.fileNotes = fileNotes;
    }

    public void setDatasetType(String datasetType) {
        this.datasetType = datasetType;
    }

    public void setNumberStars(Long numberStars) {
        this.numberStars = numberStars;
    }

    public void setDistanceRange(double distanceRange) {
        this.distanceRange = distanceRange;
    }

    public void setNumberRoutes(Integer numberRoutes) {
        this.numberRoutes = numberRoutes;
    }

    public void setThemeStr(String themeStr) {
        this.themeStr = themeStr;
    }

    public void setRoutesStr(String routesStr) {
        this.routesStr = routesStr;
    }

    public void setCustomDataDefsStr(String customDataDefsStr) {
        this.customDataDefsStr = customDataDefsStr;
    }

    public void setCustomDataValuesStr(String customDataValuesStr) {
        this.customDataValuesStr = customDataValuesStr;
    }

    public void setTransitPreferencesStr(String transitPreferencesStr) {
        this.transitPreferencesStr = transitPreferencesStr;
    }

    public void setAstrographicDataList(String astrographicDataList) {
        this.astrographicDataList = astrographicDataList;
    }

    // Explicit getters for raw string fields (used by DataSetDescriptorSerializationService)
    public String getRoutesStr() {
        return routesStr;
    }

    public String getCustomDataDefsStr() {
        return customDataDefsStr;
    }

    public String getCustomDataValuesStr() {
        return customDataValuesStr;
    }

    public String getTransitPreferencesStr() {
        return transitPreferencesStr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        DataSetDescriptor that = (DataSetDescriptor) o;
        return dataSetName != null && Objects.equals(dataSetName, that.dataSetName);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
