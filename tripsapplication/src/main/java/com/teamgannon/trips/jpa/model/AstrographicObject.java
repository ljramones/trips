package com.teamgannon.trips.jpa.model;

import com.opencsv.bean.CsvBindByName;
import com.teamgannon.trips.dataset.model.OrbitalDescriptor;
import javafx.scene.paint.Color;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * an relational data model for astrographic objects
 * <p>
 * Created by larrymitchell on 2017-03-28.
 */
@Data
@Entity
public class AstrographicObject implements Serializable {

    public final static String SIMBAD_NO_ID = "UNDEFINED";
    public final static String SIMBAD_NO_TYPE = "UNDEFINED";
    public final static String POLITY_NOT_SET = "NOT+SET";
    private static final long serialVersionUID = 1132779255908975239L;
    /**
     * id of the object
     */
    @CsvBindByName(column = "dataSetName")
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * the dataset name which we are guaranteeing to be unique
     */
    @CsvBindByName(column = "dataSetName")
    private String dataSetName;

    /**
     * used for Rick Boatright Excel format files
     */
    @CsvBindByName(column = "rbNumber")
    private int rbNumber;

    /**
     * the simbad id of this object
     */
    @CsvBindByName(column = "simbadId")
    private String simbadId;

    /**
     * if true then it is fictional and not a real object
     */
    @CsvBindByName(column = "fictional")
    private boolean fictional;

    /**
     * name to use for display
     */
    @CsvBindByName(column = "displayName")
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
    @CsvBindByName(column = "objectType")
    private String objectType;

    /**
     * the class of object (Star,...)
     */
    @CsvBindByName(column = "nnclass")
    private String nnClass;

    /**
     * the source catalog system used to hold this star
     * where did it come from?
     */
    @CsvBindByName(column = "source")
    private String source;

    /**
     * Same story. One object has names in many catalogs. The catalogIDs go in an array which can have
     * one to many entries.
     */
    @CsvBindByName(column = "catalogIdList")
    @Lob
    private String catalogIdList;

    /*
     * The cartesian coordinates of where the object is. Heliocentric (Sol at 0,0,0), Epoch J2000.0 the X axis
     * is oriented from the Sun to the galactic center at that J Date. The y axis is perpendicular, through
     * the sun, and oriented so that the X and Y axis define the plane of the earth’s orbit, and the Z axis
     * is at right angles to that plane.
     */

    /**
     * The X position of the coordinates
     */
    @CsvBindByName(column = "x")
    private double x;

    /**
     * THe Y position of the coordinates
     */
    @CsvBindByName(column = "y")
    private double y;

    /**
     * THe Z position of the coordinates
     */
    @CsvBindByName(column = "z")
    private double z;

    /**
     * this is the radius in solar multiples
     */
    @CsvBindByName(column = "radius")
    private double radius;

    /**
     * expressed as right ascension in HHMMSS
     */
    @CsvBindByName(column = "ra")
    private double ra;

    /**
     * proper motion in right ascension)
     */
    @CsvBindByName(column = "pmra")
    private double pmra;

    /**
     * expressed Declination in DDMMSS
     */
    @CsvBindByName(column = "declination")
    private double declination;

    /**
     * proper motion in declination)
     */
    @CsvBindByName(column = "pmdec")
    private double pmdec;


    @CsvBindByName(column = "dec_deg")
    private double dec_deg;

    @CsvBindByName(column = "rs_cdeg")
    private double rs_cdeg;


    /**
     * the parallax measurement
     */
    @CsvBindByName(column = "parallax")
    private double parallax;

    /**
     * distance in LY
     */
    @CsvBindByName(column = "distance")
    private double distance;

    /**
     * the star's radial velocity
     */
    @CsvBindByName(column = "radialvel")
    private double radialVelocity;

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
    @CsvBindByName(column = "spectralClass")
    private String spectralClass;

    /**
     * the temperature of the star in K
     */
    @CsvBindByName(column = "temp")
    private double temperature;

    /**
     * from the Simbad definitions
     */
    @CsvBindByName(column = "starClassType")
    private String starClassType;

    /**
     * This is an indicator on whether this is a real star or one we made up
     * <p>
     * values are - real/fictional
     */
    @CsvBindByName(column = "realStar")
    @NotNull
    private boolean realStar;

    @CsvBindByName(column = "bprp")
    private double bprp;

    @CsvBindByName(column = "bpg")
    private double bpg;

    @CsvBindByName(column = "grp")
    private double grp;

    /**
     * this is a generic marker that means that this is marked by other
     * <p>
     * user defined
     */
    @CsvBindByName(column = "other")
    private boolean other;

    /**
     * this is a flag for whether there is an anomaly
     */
    @CsvBindByName(column = "anomaly")
    private boolean anomaly;

    private double redColor;

    private double greenColor;

    private double blueColor;

    /**
     * We choose to store the standard luminosity bands. Luminosity is published based on a set of standard
     * filters that define the spectral bands. The luminosity in each band is a poor mans spectrum of the star.
     * Fluxes are measured in watts/m2 Not every object will have a measurement in every band.  Some objects will
     * have no flux measurements. The standard bands are:
     */
    @CsvBindByName(column = "luminosity")
    private String luminosity;

    /**
     * various magnitudes
     */
    @CsvBindByName(column = "magu")
    private double magu;

    @CsvBindByName(column = "")
    private double magb;

    @CsvBindByName(column = "magv")
    private double magv;

    @CsvBindByName(column = "magr")
    private double magr;

    @CsvBindByName(column = "magi")
    private double magi;

    /**
     * What polity does this object belong to.  Obviously, it has to be null or one of the polities listed in
     * the theme above.
     */
    @CsvBindByName(column = "polity")
    private String polity;

    /**
     * the type of world
     */
    @CsvBindByName(column = "worldType")
    private String worldType;

    /**
     * the type of fuel
     */
    @CsvBindByName(column = "fuelType")
    private String fuelType;

    /**
     * the type of port
     */
    @CsvBindByName(column = "portType")
    private String portType;

    /**
     * the type of population
     */
    @CsvBindByName(column = "populationType")
    private String populationType;

    /**
     * the tech type
     */
    @CsvBindByName(column = "techType")
    private String techType;

    /**
     * the product type
     */
    @CsvBindByName(column = "productType")
    private String productType;

    /**
     * the type of military in space
     */
    @CsvBindByName(column = "milSpaceType")
    private String milSpaceType;

    /**
     * the type of military on the planet
     */
    @CsvBindByName(column = "milPlanType")
    private String milPlanType;

    /**
     * parent of this entity if its a child
     */
    @CsvBindByName(column = "parentObject")
    private UUID parentObject;

    /**
     * An array of guids of children of this object.
     * Note that it is possible to have many layers of parent and child. Gamma Fictionatus is a triple star system
     * consisting of G.F. A, B, and C so each of A,B, and C list G.F. as parents, and list the other two as siblings,
     * and then each of them could have planets which are children.
     */
    @CsvBindByName(column = "children")
    @Lob
    private String children;

    /**
     * An array of guids of the siblings of this object.
     * See the discussion under children.
     */
    @CsvBindByName(column = "siblings")
    @Lob
    private String siblings;

    /**
     * A free form text field for any notes we want.  Preferentially DATA will be stored in data fields, even
     * if we have to add custom fields in the custom object, but sometimes text notes make sense.
     */
    @Lob
    @CsvBindByName(column = "notes")
    private String notes;

    /**
     * orbital description
     */
    @Embedded
    private OrbitalDescriptor orbitalParameters;


    /////////////////  convertors  /////////////

    public List<String> getCatalogIdList() {
        if (catalogIdList == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(catalogIdList.split("\\s*,\\s*"));
    }

    public void setCatalogIdList(List<String> stringList) {
        catalogIdList = String.join(",", stringList);
    }

    public List<String> getSiblings() {
        if (siblings == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(siblings.split("\\s*,\\s*"));
    }

    public void setSiblings(List<String> stringList) {
        siblings = String.join(",", stringList);
    }

    public List<String> getChildren() {
        if (children == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(children.split("\\s*,\\s*"));
    }

    public void setChildren(List<String> stringList) {
        children = String.join(",", stringList);
    }

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

    public Color getStarColor() {
        return Color.color(redColor, greenColor, blueColor);
    }

    public void setStarColor(Color color) {
        redColor = color.getRed();
        greenColor = color.getGreen();
        blueColor = color.getBlue();
    }

}
