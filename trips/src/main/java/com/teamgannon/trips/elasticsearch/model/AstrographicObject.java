package com.teamgannon.trips.elasticsearch.model;

import com.teamgannon.trips.dataset.model.OrbitalDescriptor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * an elasticsearch data model for astrographic objects
 * <p>
 * Created by larrymitchell on 2017-03-28.
 */
@Data
@Document(indexName = "astrographicobjectidx", type = "astrographicobject", shards = 1, replicas = 0, refreshInterval = "-1")
public class AstrographicObject implements Serializable {

    private static final long serialVersionUID = 1132779255908975239L;

    public final static String SIMBAD_NO_ID = "UNDEFINED";
    public final static String SIMBAD_NO_TYPE = "UNDEFINED";
    public final static String POLITY_NOT_SET = "NOT+SET";

    /**
     * id of the object
     */
    @Id
    private UUID id;

    /**
     * the dataset name which we are guaranteeing to be unique
     */
    private String dataSetName;

    /**
     * used for Rick Boatright Excel format files
     */
    private int rbNumber;

    /**
     * the simbad id of this object
     */
    private String simbadId;

    /**
     * if true then it is fictional and not a real object
     */
    private boolean fictional;

    /**
     * name to use for display
     */
    private String displayName;


    /**
     * Oh dear, why does it have to get complicated on only the third field?
     * <p>
     * I propose we use the Simbad object type codes, but a given star can fit in several categories.
     * For example, it could be a white dwarf, Type code WD*, and that white dwarf could have high proper
     * motion, so it’s a type PM*, and it could be one of several types of variable star with any of
     * several type codes beginning with V.
     * <p>
     * If I were going to do this myself, I would make the objectType an array which could have 1
     * to many types…  {“WD*”, “PM*”, “Vl”} and so on. “Planet” is a type.
     */
    private String objectType;

    /**
     * Same story. One object has names in many catalogs. The catalogIDs go in an array which can have
     * one to many entries.
     */
    private List<String> catalogIdList = new ArrayList<>();

    /*
     * The cartesian coordinates of where the object is. Heliocentric (Sol at 0,0,0), Epoch J2000.0 the X axis
     * is oriented from the Sun to the galactic center at that J Date. The y axis is perpendicular, through
     * the sun, and oriented so that the X and Y axis define the plane of the earth’s orbit, and the Z axis
     * is at right angles to that plane.
     */

    /**
     * The X position of the coordinates
     */
    private double x;

    /**
     * THe Y position of the coordinates
     */
    private double y;

    /**
     * THe Z position of the coordinates
     */
    private double z;

    /**
     * this is the radius in solar multiples
     */
    private double radius;

    /**
     * expressed as right ascension in HHMMSS
     */
    private double ra;

    /**
     * expressed Declination in DDMMSS
     */
    private double declination;

    private double dec_deg;

    private double rs_cdeg;

    /**
     * the parallax measurement
     */
    private double parallax;

    /**
     * distance in LY
     */
    private double distance;

    /**
     * From Simbad, We’re only storing ONE per object, and in reality we’re only interested in the first character
     * (OBAFGKMLTY) Objects which are sub-stellar such as planets should have a value of NULL.  We do not want
     * to try to use this field to code both spec class AND if something’s a planet or a black hole, or whatever.
     * That’s what the object type field is for. In the object type there will be types like “Planet” and
     * “Dust Disk” and “Nebula” etc as well as the various star types.
     * <p>
     * Also, we will need  to decide if we want to use Simbad’s spec types that contain extra info.  For example,
     * Simbad reports dM1.5  for a red dwarf, the initial d indicating a dwarf.  There are other such.  I don’t
     * like it.  The spectral class should be the temp and color of the star, not trying to code more than one
     * thing.  A stars dwarfness or giantness in my opinion goes in the Type array.
     */
    private String spectralClass;

    /**
     * form the Simbad definitions
     */
    private String starClassType;

    /**
     * This is an indicator on whether this is a real star or one we made up
     * <p>
     * values are - real/fictional
     */
    private String starType;

    /**
     * this is a generic marker that means that this is marked by other
     * <p>
     * user defined
     */
    private boolean other;

    /**
     * this is a flag for whether there is an anomaly
     */
    private boolean anomaly;

    /**
     * stellar color, this is temporary
     */
    private double[] starColor = new double[3];

    /**
     * We choose to store the standard luminosity bands. Luminosity is published based on a set of standard
     * filters that define the spectral bands. The luminosity in each band is a poor mans spectrum of the star.
     * Fluxes are measured in watts/m2 Not every object will have a measurement in every band.  Some objects will
     * have no flux measurements. The standard bands are:
     */
    private String luminosity;

    /**
     * various magnitudes
     */
    private double magu;
    private double magb;
    private double magv;
    private double magr;
    private double magi;

    /**
     * What polity does this object belong to.  Obviously, it has to be null or one of the polities listed in
     * the theme above.
     */
    private String polity;

    /**
     * the type of world
     */
    private String worldType;

    /**
     * the type of fuel
     */
    private String fuelType;

    /**
     * the type of port
     */
    private String portType;

    /**
     * the type of population
     */
    private String populationType;

    /**
     * the tech type
     */
    private String techType;

    /**
     * the product type
     */
    private String productType;

    /**
     * the type of military in space
     */
    private String milSpaceType;

    /**
     * the type of military on the planet
     */
    private String milPlanType;

    /**
     * parent of this entity if its a child
     */
    private UUID parentObject;

    /**
     * An array of guids of children of this object.
     * Note that it is possible to have many layers of parent and child. Gamma Fictionatus is a triple star system
     * consisting of G.F. A, B, and C so each of A,B, and C list G.F. as parents, and list the other two as siblings,
     * and then each of them could have planets which are children.
     */
    private List<UUID> children = new ArrayList<>();

    /**
     * An array of guids of the siblings of this object.
     * See the discussion under children.
     */
    private List<UUID> siblings = new ArrayList<>();

    /**
     * A free form text field for any notes we want.  Preferentially DATA will be stored in data fields, even
     * if we have to add custom fields in the custom object, but sometimes text notes make sense.
     */
    private String notes;

    /**
     * description
     */
    @Field(type = FieldType.Nested)
    private OrbitalDescriptor orbitalParameters = new OrbitalDescriptor();

    public double[] getCoordinates() {
        double[] coordinates = new double[3];
        coordinates[0] = x;
        coordinates[1] = y;
        coordinates[2] = z;

        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        x = coordinates[0];
        y = coordinates[1];
        z = coordinates[2];
    }

}
