package com.teamgannon.trips.jpa.model;

import com.teamgannon.trips.dataset.model.CustomDataDefinition;
import com.teamgannon.trips.dataset.model.CustomDataValue;
import com.teamgannon.trips.dataset.model.Theme;
import com.teamgannon.trips.routing.Route;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The new model for the database
 * <p>
 * Created by larrymitchell on 2017-03-28.
 */
@Data
@Entity
public class DataSetDescriptor implements Serializable {

    private static final long serialVersionUID = 1132779255908975239L;

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
     * <p>
     * e.g. “Rick Boatright”
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
     * and completely error prone
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

    /**
     * since we can add a complex object like this, we convert between object and JSON
     * this is the getter
     *
     * @return the Theme object
     */
    public Theme getTheme() {
        return new Theme().toTheme(themeStr);
    }

    /**
     * set the theme by converting it to json
     *
     * @param theme the theme
     */
    public void setTheme(Theme theme) {
        themeStr = new Theme().convertToJson(theme);
    }

    /**
     * convert the internal route representation to a list for external representation
     *
     * @return the list of routes
     */
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
     * set the list of routes and convert to a JSON representation
     *
     * @param routes the list of routes
     */
    public void setRoutes(@org.jetbrains.annotations.NotNull List<Route> routes) {
        numberRoutes = routes.size();
        routesStr = new Route().convertToJson(routes);
    }

    /**
     * convert the internal route representation to a list for external representation
     *
     * @return the list of routes
     */
    public List<CustomDataDefinition> getCustomDataDefinitions() {
        return new CustomDataDefinition().toCustomDataDefinition(customDataDefsStr);
    }

    /**
     * set the list of routes and convert to a JSON representation
     *
     * @param customDataDefinitions the list of routes
     */
    public void setCustomDataDefinition(List<CustomDataDefinition> customDataDefinitions) {
        customDataDefsStr = new CustomDataDefinition().convertToJson(customDataDefinitions);
    }

    /**
     * convert the internal route representation to a list for external representation
     *
     * @return the list of routes
     */
    public List<CustomDataValue> getCustomDataValue() {
        return new CustomDataValue().toCustomDataValue(customDataValuesStr);
    }

    /**
     * set the list of routes and convert to a JSON representation
     *
     * @param customDataValues the list of routes
     */
    public void setCustomDataValue(List<CustomDataValue> customDataValues) {
        customDataValuesStr = new CustomDataValue().convertToJson(customDataValues);
    }

    public String getAstroDataString() {
        return astrographicDataList;
    }

    public void setAstroDataString(String astrographicDataList) {
        this.astrographicDataList = astrographicDataList;
    }

    public Set<UUID> getAstrographicDataList() {

        // if its null return an empty list
        if (astrographicDataList == null) {
            return new HashSet<>();
        }
        // separate and convert to a list
        List<String> stringList = Arrays.asList(astrographicDataList.split("\\s*,\\s*"));

        // convert to an array list of UUIDs
        return stringList.stream().map(UUID::fromString).collect(Collectors.toSet());
    }

    public void setAstrographicDataList(@org.jetbrains.annotations.NotNull Set<UUID> uuidList) {
        numberStars = (long) uuidList.size();
        astrographicDataList = uuidList.stream().map(uuid -> uuid.toString() + ",").collect(Collectors.joining());
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

}
