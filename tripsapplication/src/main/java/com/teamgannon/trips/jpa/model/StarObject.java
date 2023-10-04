package com.teamgannon.trips.jpa.model;

import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.file.chview.ChViewRecord;
import com.teamgannon.trips.routing.model.SparseStarRecord;
import com.teamgannon.trips.solarsysmodelling.accrete.SimStar;
import com.teamgannon.trips.stellarmodelling.StarCreator;
import com.teamgannon.trips.stellarmodelling.StarModel;
import com.teamgannon.trips.stellarmodelling.StarUtils;
import com.teamgannon.trips.stellarmodelling.StellarType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * an relational data model for astrographic objects
 * <p>
 * Created by larrymitchell on 2017-03-28.
 */
@Slf4j
@Getter
@Setter
@ToString
@DynamicUpdate
@Entity(name = "STAR_OBJ")
@Table(indexes = {
        @Index(columnList = "displayName ASC"),
        @Index(columnList = "constellationName ASC"),
        @Index(columnList = "distance DESC"),
        @Index(columnList = "orthoSpectralClass ASC"),
        @Index(columnList = "realStar"),
        @Index(columnList = "commonName ASC"),
        @Index(columnList = "simbadId ASC"),
        @Index(columnList = "hipCatId ASC"),
        @Index(columnList = "hdCatId ASC"),
        @Index(columnList = "glieseCatId ASC"),
        @Index(columnList = "tycho2CatId ASC"),
        @Index(columnList = "gaiaDR2CatId ASC"),
        @Index(columnList = "gaiaDR3CatId ASC"),
        @Index(columnList = "gaiaEDR3CatId ASC"),
        @Index(columnList = "twoMassCatId ASC"),
        @Index(columnList = "csiCatId ASC"),
        @Index(columnList = "bayerCatId ASC"),
        @Index(columnList = "flamsteedCatId ASC"),
        @Index(columnList = "exoplanets")
})
public class StarObject implements Serializable {

    public final static String SIMBAD_NO_ID = "UNDEFINED";
    public final static String SIMBAD_NO_TYPE = "UNDEFINED";
    public final static String POLITY_NOT_SET = "NOT+SET";

    @Serial
    private static final long serialVersionUID = -751366073413071183L;

    /**
     * match the pattern * nnn Con
     * nnn for 3 digits
     */
    @Transient
    Pattern flamsteedPattern = Pattern.compile("[\\*] +[0-9]{3} +[a-zA-Z]{3}");

    /**
     * match the pattern * ggg Con
     * ggg for 3 characters
     */
    @Transient
    Pattern bayerPattern = Pattern.compile("[\\*] +[a-zA-Z]{3} +[a-zA-Z]{3}");


    /**
     * id of the object
     */
    @Id
    private String id;

    /**
     * the dataset name which we are guaranteeing to be unique
     */
//    @Column(name = "DATASETNAME")
    private String dataSetName = "";

    /**
     * name to use for display
     */
    private String displayName = "";

    /**
     * the star's common name
     */
    private String commonName;

    /**
     * name of the system
     */
    private String systemName = "";

    /**
     * the epoch of the star normally J2000
     * but in some cases can be J2016
     */
    private String epoch = "";

    /**
     * updated by external gaia data files
     */
    private boolean gaiaUpdated = false;

    /**
     * update date from gaia data files
     */
    private String gaiaUpdatedDate = "";

    /**
     * list of alias names for this
     * <p>
     * EAGER is undesirable but needed because of underlying hibernate lazy load error
     */
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> aliasList = new HashSet<>();

    /**
     * name of the constellation that this is part of
     */
    private String constellationName = "";

    /**
     * the collapsed mass value
     */
    private double mass = 0.0;

    /**
     * A free form text field for any notes we want.  Preferentially DATA will be stored in data fields, even
     * if we have to add custom fields in the custom object, but sometimes text notes make sense.
     */
    @Lob
    private String notes = "";

    /**
     * the source catalog system used to hold this star
     * where did it come from?
     */
    @Lob
    private String source = "";

    /**
     * Same story. One object has names in many catalogs. The catalogIDs go in an array which can have
     * one-to-many entries.
     */
    @Lob
    private String catalogIdList = "";

    /**
     * the Simbad id
     */
    private String simbadId;

    /**
     * the Bayer catalog id
     * validated by the BayerChecker based on greek letter for first part
     * and genitive form of the constellation for the second part
     * <p>
     * this is dervied form catalog ids or directly, not read in
     */
    private String bayerCatId = "";

    /**
     * the gliese catalog id - GJ
     * <p>
     * this is dervied form catalog ids or directly, not read in
     */
    private String glieseCatId = "";

    /**
     * the hipparcos catalog id - HIP
     * <p>
     * this is dervied form catalog ids or directly, not read in
     */
    private String hipCatId = "";

    /**
     * the henry draper catalog id - HD
     * <p>
     * this is dervied form catalog ids or directly, not read in
     */
    private String hdCatId = "";

    /**
     * the flamsteed catalog id
     * the flamsteed format has a number as the first part
     * and the genitive form of the constellation as the second part
     * <p>
     * this is dervied form catalog ids or directly, not read in
     */
    private String flamsteedCatId = "";

    /**
     * the Tycho 2 catalog id - TYC2
     * <p>
     * this is dervied form catalog ids or directly, not read in
     */
    private String tycho2CatId = "";

    /**
     * the id in the gaia catalog
     */
    private String gaiaDR2CatId;


    /**
     * the Gaia DR3 catalog id - Gaia DR3
     * <p>
     * this is dervied form catalog ids or directly, not read in
     */
    private String gaiaDR3CatId = "";

    /**
     * the Gaia EDR3 catalog id - Gaia EDR3
     * <p>
     * this is dervied form catalog ids or directly, not read in
     */
    private String gaiaEDR3CatId = "";

    /**
     * the 2MASS catalog id - 2MASS
     * <p>
     * this is dervied form catalog ids or directly, not read in
     */
    private String twoMassCatId = "";

    /**
     * the CSI catalog id - CSI
     * <p>
     * this is derived form catalog ids or directly, not read in
     */
    private String csiCatId = "";

    /*
     * The cartesian coordinates of where the object is. Heliocentric (Sol at 0,0,0), Epoch J2000.0 the X axis
     * is oriented from the Sun to the galactic center at that J Date. The y axis is perpendicular, through
     * the sun, and oriented so that the X and Y axis define the plane of the earth’s orbit, and the Z axis
     * is at right angles to that plane.
     *
     * Cartesian position in LY. Heliocentric (Sol at 0,0,0), Epoch J2000.0
     */

    /**
     * X position, X axis is a line from Sol to the galactic center at that J Date
     * <p>
     * this is calculated directly from RA, dec and distance
     */
    private double x = 0.0;

    /**
     * Y position, Y axis is perpendicular, through sol, and oriented so that the X and Y axis
     * define the plane of the earth’s orbit
     * <p>
     * this is calculated directly from RA, dec and distance
     */
    private double y = 0.0;

    /**
     * Z position, Z axis is through Sol at right angles to the XY plane
     * <p>
     * this is calculated directly from RA, dec and distance
     */
    private double z = 0.0;

    /**
     * this is the radius in solar multiples
     */
    private double radius = 0.0;

    /**
     * expressed as right ascension in degrees
     */
    private double ra = 0.0;

    /**
     * expressed Declination in degrees
     */
    private double declination = 0.0;

    /**
     * galactic latitude
     */
    private double galacticLat;

    /**
     * galactic longitude
     */
    private double galacticLong;

    /**
     * Proper Motion in RA direction in degrees
     */
    private double pmra = 0.0;

    /**
     * Proper Motion in Dec direction in degrees
     */
    private double pmdec = 0.0;

    /**
     * the parallax measurement n milli-arc-seconds
     * <p>
     * We don't need this, we have the distance measure
     */
    private double parallax = 0.0;

    /**
     * distance in LY
     */
    private double distance = 0.0;

    /**
     * the star's radial velocity
     * from Sol in km/year
     */
    private double radialVelocity = 0.0;

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
     * this is a one character descriptor of spectralClass
     * <p>
     * this is derived from spectralClass
     */
    private String orthoSpectralClass;

    /**
     * the temperature of the star in K
     */
    private double temperature = 0.0;

    /**
     * This is an indicator on whether this is a real star or one we made up
     * <p>
     * values are - real/fictional
     */
    @NotNull
    private boolean realStar;

    /**
     * Gaia magnitude in the bprp band - crudely "red"
     */
    private double bprp = 0.0;

    /**
     * Gaia magnitude in the bpg band - crudely "blue"
     */
    private double bpg = 0.0;

    /**
     * Gaia magnitude in the grp band - crudely "green"
     */
    private double grp = 0.0;

    /**
     * We choose to store the standard luminosity bands. Luminosity is published based on a set of standard
     * filters that define the spectral bands. The luminosity in each band is a poor mans spectrum of the star.
     * Fluxes are measured in watts/m2 Not every object will have a measurement in every band.  Some objects will
     * have no flux measurements. The standard bands are:
     */
    private String luminosity;

    /**
     * Apparent magnitude
     */
    private String apparentMagnitude;

    /**
     * Absolute magnitude
     */
    private String absoluteMagnitude;

    /**
     * Magnitude in the astronomic "U" band
     */
    private double magu = 0.0;

    /**
     * Magnitude in the astronomic "B" band
     */
    private double magb = 0.0;

    /**
     * Magnitude in the astronomic "V" band
     */
    private double magv = 0.0;

    /**
     * Magnitude in the astronomic "R" band
     */
    private double magr = 0.0;

    /**
     * Magnitude in the astronomic "I" band
     */
    private double magi = 0.0;

    ///////////////////  for fiction writing   //////////////
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
     * age of the star
     */
    private double age;

    /**
     * star metallicity
     */
    private double metallicity;

    /////////////   Miscellaneous   /////////////////////////////////////

    /**
     * for user custom use in future versions
     */
    private String miscText1;

    /**
     * for user custom use in future versions
     */
    private String miscText2;

    /**
     * for user custom use in future versions
     */
    private String miscText3;

    /**
     * for user custom use in future versions
     */
    private String miscText4;

    /**
     * for user custom use in future versions
     */
    private String miscText5;

    /**
     * for user custom use in future versions
     */
    private double miscNum1 = 0.0;

    /**
     * for user custom use in future versions
     */
    private double miscNum2 = 0.0;

    /**
     * for user custom use in future versions
     */
    private double miscNum3 = 0.0;

    /**
     * for user custom use in future versions
     */
    private double miscNum4 = 0.0;

    /**
     * for user custom use in future versions
     */
    private double miscNum5 = 0.0;

    /**
     * a flag that tells us if this system has exoplanets
     * flag is set on read of number of exoplanets
     */
    private boolean exoplanets;

    /**
     * the number of exoplanets
     */
    private int numExoplanets;

    ///////////////////////

    /**
     * there are times when we want to ensure that the label is always show and not part of the algorithm for display
     * <p>
     * not from file
     */
    private boolean forceLabelToBeShown = false;

    /**
     * this is a computed heuristic that tells us whether to show the label on the graphics display or not
     * <p>
     * not from file
     */
    private double displayScore = 0;


    ///////////////////////////////////////


    public StarObject() {
        init();
    }

    private void init() {
        id = UUID.randomUUID().toString();
        dataSetName = "not specified";
        realStar = true;
        displayName = "no name";
        source = "no source identified";
        catalogIdList = "NA";
        epoch = "J2000";
        x = 0;
        y = 0;
        z = 0;
        radius = 0;
        ra = 0;
        pmra = 0;
        declination = 0;
        pmdec = 0;
        parallax = 0;
        distance = 0;
        radialVelocity = 0;
        spectralClass = "";
        temperature = 0;
        bprp = 0;
        bpg = 0;
        grp = 0;
        other = false;
        luminosity = " ";
        magu = 0;
        magb = 0;
        magv = 0;
        magr = 0;
        magi = 0;
        polity = "NA";
        worldType = "NA";
        fuelType = "NA";
        portType = "NA";
        populationType = "NA";
        techType = "NA";
        productType = "NA";
        milSpaceType = "NA";
        notes = "initial star file load";

        simbadId = "";
        bayerCatId = "";
        flamsteedCatId = "";
        gaiaDR3CatId = "";
        gaiaEDR3CatId = "";
        csiCatId = "";
        twoMassCatId = "";
        tycho2CatId = "";
        hipCatId = "";
        hdCatId = "";
        glieseCatId = "";
        commonName = "";
        age = 0;
        metallicity = 0;
        galacticLat = 0.0;
        galacticLong = 0.0;
        gaiaDR2CatId = "";

        exoplanets = false;

        miscNum1 = 0;
        miscNum2 = 0;
        miscNum3 = 0;
        miscNum4 = 0;
        miscNum5 = 0;

        miscText1 = "";
        miscText2 = "";
        miscText3 = "";
        miscText4 = "";
        miscText5 = "";

        displayScore = 0;
    }


    /////////////////  convertors  /////////////

    public String getRawCatalogIdList() {
        return catalogIdList;
    }

    public List<String> getCatalogIdList() {
        if (catalogIdList == null) {
            return new ArrayList<>();
        }
        if (catalogIdList.equals("NA")) {
            return new ArrayList<>();
        }
        return Arrays.asList(catalogIdList.split("\\s*,\\s*"));
    }

    public void setCatalogIdList(String catalogIdList) {
        this.catalogIdList = catalogIdList;
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


    /**
     * calculate the display score
     */
    public void calculateDisplayScore() {
        displayScore = calculateBaseScore() * calculateLabelMultiplier();
    }

    /**
     * calculate the display score multiplier
     *
     * @return the multiplier
     */
    private double calculateLabelMultiplier() {

        double cumulativeTotal = 0;

        // 1. Is there something in the Common Name field?
        if (!commonName.isEmpty()) {
            cumulativeTotal += 3;
        }

        // 2. Does the star have a Flamsteed catalog ID?
        //      In the CatalogID field there will be a value of "* nnn Con"
        //      where nnn is an integer and Con is a constellation Abbreviation
        Matcher flamMatcher = flamsteedPattern.matcher(catalogIdList);
        if (flamMatcher.find()) {
            cumulativeTotal += 3;
        }

        // 3. Does the star have a Bayer catalog ID?
        //      "* ggg Con" in CatalogID where GGG is
        //      a greek letter abbreviation, a-z, or A-Q  and Con is a constellation abbreviation
        Matcher bayerMatcher = bayerPattern.matcher(catalogIdList);
        if (bayerMatcher.find()) {
            cumulativeTotal += 3;
        }

        // 4. Is the star in the BD catalog?
        //      BD and DM are the same thing
        //      An entry in CatalogID staring with BD+ or BD-
        if (catalogIdList.contains("BD+") || catalogIdList.contains("BD-")) {
            cumulativeTotal += 1.5;
        }

        // 5. Is the star in the Gliese catalog?
        //      "GJ " in Catalog ID
        //      This is tricky. Alph Cen is GJ 559 But because it's binary, Alph Cen A is in GJ as GJ 559A and
        //      "GJ 559" isn't in the base catalog right now, because Simbad doesn't list it.
        if (catalogIdList.contains("GJ")) {
            cumulativeTotal += 1.5;
        }

        // 6. Is the star in Hipparchos?
        //      "HIP " in Catalog ID
        //      Same problem. Alpha Cen A is HIP 71683  Alph Cen is not in HIP…
        if (catalogIdList.contains("HIP")) {
            cumulativeTotal += 1.5;
        }

        // 7. Is the star in Henry Draper?
        //      "HD " in Catalog ID?
        if (catalogIdList.contains("HD")) {
            cumulativeTotal += 1.5;
        }

        // 8. is there an entry in polity?
        if (!polity.trim().isEmpty()) {
            cumulativeTotal += 3;
        }

        // check if any other fictional item is set
        if (otherFictionalFieldsPresent()) {
            cumulativeTotal += 3;
        }

        // 10. If none of the above, make the multiplier one. (1)
        if (cumulativeTotal == 0) {
            cumulativeTotal = 1;
        }

        return cumulativeTotal;
    }

    private boolean otherFictionalFieldsPresent() {
        if (!worldType.trim().isEmpty()) {
            return true;
        }
        if (!fuelType.trim().isEmpty()) {
            return true;
        }
        if (!portType.trim().isEmpty()) {
            return true;
        }
        if (!populationType.trim().isEmpty()) {
            return true;
        }
        if (!techType.trim().isEmpty()) {
            return true;
        }

        if (!milSpaceType.trim().isEmpty()) {
            return true;
        }

        return !milPlanType.trim().isEmpty();
    }

    private double calculateBaseScore() {
        int base = 0;

//        log.info("display name = <{}>, orthoSpectralClass = <{}>", displayName, orthoSpectralClass);
        StarModel starModel = new StarCreator().parseSpectral(orthoSpectralClass);

        if (starModel.getStellarClass() == null) {
            log.error("could not find stellar class");
            return 1;
        }

        // process harvard spectral class
        if (orthoSpectralClass.length() > 1) {
            StellarType x = starModel.getStellarClass();
            String harvardSpecClass = starModel.getStellarClass().getValue();

            switch (harvardSpecClass) {
                case "O", "A", "B" -> base += 2;
                case "F", "K" -> base += 4;
                case "G" -> base += 5;
                case "M" -> base += 3;
                case "L", "T", "Y" -> base += 1;
            }
        }


        // process luminosity
        String luminosityValue = starModel.getLuminosityClass();
        if (luminosityValue != null && !luminosityValue.isEmpty()) {
            int lumNum = 1;
            switch (luminosityValue) {
                case "I" -> lumNum = 1;
                case "II" -> lumNum = 2;
                case "III" -> lumNum = 3;
                case "IV" -> lumNum = 4;
                case "V" -> lumNum = 5;
                case "VI" -> lumNum = 6;
                case "VII" -> lumNum = 7;
                case "VIII" -> lumNum = 8;
                case "IX" -> lumNum = 9;
                case "X" -> lumNum = 10;
            }
            base += (11 - lumNum);
        } else {
            base += 1;
        }

        return base;
    }

    public SimStar toSimStar() {

        // create an idealized star model based on spectral class
        StarCreator starCreator = new StarCreator();
        StarModel starModel;
        if (!spectralClass.contains("fictional")) {
            starModel = starCreator.parseSpectral(spectralClass);
        } else {
            starModel = starCreator.parseSpectral(orthoSpectralClass);
        }

        // generate  a sim star form the idealized star
        SimStar simStar = starModel.toSimStar();

        if (mass != 0) {
            double sMass = StarUtils.relativeMass(mass);
            simStar.setMass(sMass);
        }

        if (radius != 0) {
            double sRadius = StarUtils.relativeRadius(radius);
            simStar.setRadius(sRadius);
        }

        if (!luminosity.isEmpty()) {
            try {
                double dLuminosity = Double.parseDouble(luminosity);
                simStar.setLuminosity(dLuminosity);
            } catch (NumberFormatException nfe) {
                log.error("luminosity value is bad:<{}>", luminosity);
            }
        }

        if (temperature != 0) {
            simStar.setTemperature(temperature);
        }

        return simStar;
    }

    public void fromChvRecord(Dataset dataset, ChViewRecord chViewRecord) {

        // preset
        init();

        this.dataSetName = dataset.getName();
        this.realStar = true;
        this.displayName = chViewRecord.getStarName();


        if (chViewRecord.getConstellation() != null) {
            this.constellationName = chViewRecord.getConstellation();
        } else {
            this.constellationName = "";
        }

        // set the collapsed mass
        this.mass = chViewRecord.getCollapsedMass();

        // mark that this is the first load of this object
        if (chViewRecord.getComment() != null) {
            this.notes = chViewRecord.getComment();
        } else {
            this.notes = "";
        }


        this.setCoordinates(chViewRecord.getOrdinates());
        this.setDistance(Double.parseDouble(chViewRecord.getDistanceToEarth()));

        this.setRadius(chViewRecord.getRadius());

        StarModel starModel = new StarCreator().parseSpectral(chViewRecord.getOrthoSpectra());
        if (starModel.getStellarClass() == null) {
            log.info("spectral class could not be verified, spectra = "
                    + chViewRecord.getSpectra()
                    + "\n\tchv record = " + chViewRecord);
        }


        this.setSpectralClass(chViewRecord.getSpectra());
        this.setOrthoSpectralClass(chViewRecord.getOrthoSpectra());

        switch (chViewRecord.getGroupNumber()) {
            case 1 -> this.setPolity(CivilizationDisplayPreferences.ARAKUR);
            case 2 -> this.setPolity(CivilizationDisplayPreferences.HKHRKH);
            case 4 -> this.setPolity(CivilizationDisplayPreferences.KTOR);
            case 8 -> this.setPolity(CivilizationDisplayPreferences.TERRAN);
        }
        this.setSource("CHView");

        // figure out display score
//        calculateLabelMultiplier();

    }

    public SparseStarRecord toSparseStarRecord() {
        SparseStarRecord sparseStarRecord = new SparseStarRecord();
        sparseStarRecord.setRecordId(this.id);
        sparseStarRecord.setStarName(this.getDisplayName());
        sparseStarRecord.setActualCoordinates(new double[]{x, y, z});
        return sparseStarRecord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        StarObject that = (StarObject) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
