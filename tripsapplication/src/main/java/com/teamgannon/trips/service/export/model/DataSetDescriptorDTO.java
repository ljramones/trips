package com.teamgannon.trips.service.export.model;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import lombok.Data;

import java.time.Instant;

@Data
public class DataSetDescriptorDTO {

    /**
     * name for the dataset
     * <p>
     * e.g. “150LY Sphere Cain Riordan”
     */
    private String dataSetName;

    /**
     * this is the file path where we got the data
     */

    private String filePath;

    /**
     * name of the dataset file creator
     * <p>
     * e.g. “Rick Boatright”
     */

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
    private String themeStr;

    /**
     * a container object for astrographic data
     * this is actually a command separated list of UUIDs
     * we have to convert back and forth between a set of UUIDs and a string on getter and setter methods
     * because although JPA allows lists adn sets through ElementCollection, it is a pain in the ass to use
     * and completely error prone
     */
    private String astrographicDataList;

    /**
     * an object describing routes
     */
    private String routesStr;

    /**
     * a set of custom data definitions
     */
    private String customDataDefsStr;

    /**
     * a set of custom data values
     */
    private String customDataValuesStr;

    public DataSetDescriptor toDataSetDescriptor() {
        DataSetDescriptor descriptor = new DataSetDescriptor();

        descriptor.setDataSetName(dataSetName);
        descriptor.setFilePath(filePath);
        descriptor.setFileCreator(fileCreator);
        descriptor.setFileOriginalDate(fileOriginalDate);
        descriptor.setFileNotes(fileNotes);
        descriptor.setDatasetType(datasetType);
        descriptor.setNumberStars(numberStars);
        descriptor.setDistanceRange(distanceRange);
        descriptor.setNumberRoutes(numberRoutes);
        descriptor.setThemeStr(themeStr);
        descriptor.setAstroDataString(astrographicDataList);
        descriptor.setRoutesStr(routesStr);
        descriptor.setCustomDataDefsStr(customDataDefsStr);
        descriptor.setCustomDataValuesStr(customDataValuesStr);

        return descriptor;
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

}
