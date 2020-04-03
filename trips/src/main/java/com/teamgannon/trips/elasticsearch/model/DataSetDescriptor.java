package com.teamgannon.trips.elasticsearch.model;

import com.teamgannon.trips.dataset.model.CustomDataDefinition;
import com.teamgannon.trips.dataset.model.CustomDataValue;
import com.teamgannon.trips.dataset.model.Routes;
import com.teamgannon.trips.dataset.model.Theme;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * The new model for the database
 * <p>
 * Created by larrymitchell on 2017-03-28.
 */
@Data
@Document(indexName = "datasetdescriptoridx", type = "datasetdescriptor", shards = 1, replicas = 0, refreshInterval = "-1")
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
    private long fileOriginalDate;

    /**
     * notes on the data in the file
     * <p>
     * e.g. A free-form text field containing notes about what this files purpose is, TTR or Honorverse,
     * possibly specifying things like ‘As of Raising Caine.’
     */
    @NotNull
    private String fileNotes;

    /**
     * the theme object
     */
    @Field(type = FieldType.Nested)
    private Theme theme;

    /**
     * a container object for astrographic data
     */
    private Set<UUID> astrographicDataList = new HashSet<>();

    /**
     * an object describing routes
     */
    @Field(type = FieldType.Nested)
    private Routes routes;

    /**
     * a set of custom data definitions
     */
    @Field(type = FieldType.Nested)
    private List<CustomDataDefinition> customDataDefs = new ArrayList<>();

    /**
     * a set of custom data values
     */
    @Field(type = FieldType.Nested)
    private List<CustomDataValue> customDataValues = new ArrayList<>();
}
