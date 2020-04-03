package com.teamgannon.trips.elasticsearch.model;

import com.teamgannon.trips.file.chview.model.CHLinkDescriptor;
import javafx.scene.paint.Color;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A record from the ChView file
 * <p>
 * cjcName |string |Proper Name of Place
 * <p>
 * starName |string |Official name of star
 * <p>
 * dToEarth |double |Distance to earth
 * <p>
 * Spectra |string |Spectra
 * <p>
 * Mass |double |Collapsed mass
 * <p>
 * ActualMass |double |Uncollapsed mass
 * <p>
 * ords[0] |double |Galactic coordinates
 * <p>
 * ords[1] |double |
 * <p>
 * ords[2] |double |
 * <p>
 * Constellation |string |Constellation
 * <p>
 * Comment |string |Comment
 * <p>
 * Selected |int |Selected or not
 * <p>
 * index |int |Index in file
 * <p>
 * Group |int |Group of star
 * <p>
 * There is a single byte indicating if there is a subsidiary star orbiting this one. If so this entire
 * data structure repeats (recursively).
 * <p>
 * Lastly we record the links.
 * <p>
 * First is a integer value indicating the number of following links.
 * <p>
 * Each link is then represented by a integer containing the type of link and a string containing the starName
 * of the destination.
 * <p>
 * Created by larrymitchell on 2017-02-07.
 */
@Data
@Document(indexName = "chviewrecord", type = "chviewrecord", shards = 1, replicas = 0, refreshInterval = "-1")
public class ChViewRecord implements Serializable {

    private static final long serialVersionUID = -5333213779999784607L;

    @Id
    private String id;

    /**
     * the record number
     */
    private int recordNumber;

    /**
     * cjcName |string |Proper Name of Place
     */
    private String properPlaceName;

    /**
     * starName |string |Official name of star
     */
    private String starName;

    /**
     * dToEarth |double |Distance to earth
     */
    private String distanceToEarth;

    /**
     * Spectra |string |Spectra
     */
    private String spectra;

    /**
     * Mass |double |Collapsed mass
     */
    private double collapsedMass;

    /**
     * ActualMass |double |Uncollapsed mass
     */
    private double uncollapsedMass;

    /**
     * ords[0] |double |Galactic coordinates
     */
    private double[] ordinates = new double[3];

    /**
     * Constellation |string |Constellation
     */
    private String constellation;

    /**
     * Comment |string |Comment
     */
    private String comment;

    /**
     * Selected |int |Selected or not
     */
    private boolean selected;

    /**
     * index |int |Index in file
     */
    private int index;

    /**
     * Group |int |Group of star
     */
    private int groupNumber;

    /**
     * number of links
     */
    private int numberOfLinks;

    /**
     * the list of links for this star subsidiaryStar
     */
    private List<CHLinkDescriptor> CHLinkDescriptors = new ArrayList<>();

    /**
     * the subsidiary star
     */
    private ChViewRecord subsidiaryStar;

    // -------------------- the derived values  ----------------------------- //
    /**
     * this is derived frOm the spectra
     */
    private Color starColor;

    /**
     * the radius for display values
     */
    private double radius;

    /**
     * set the complete set of ordinates
     *
     * @param ord1 ord1
     * @param ord2 ord2
     * @param ord3 ord3
     */
    public void setOrdinates(double ord1, double ord2, double ord3) {
        ordinates[0] = ord1;
        ordinates[1] = ord2;
        ordinates[2] = ord3;
    }

    public String getCoordinatesAsString() {
        return Double.toString(ordinates[0]) + ","
                + Double.toString(ordinates[1]) + ","
                + Double.toString(ordinates[1]);
    }

}
