package com.teamgannon.trips.jpa.model;

import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.file.chview.ChViewRecord;
import com.teamgannon.trips.routing.model.SparseStarRecord;
import com.teamgannon.trips.solarsysmodelling.accrete.SimStar;
import com.teamgannon.trips.stellarmodelling.StarCreator;
import com.teamgannon.trips.stellarmodelling.StarModel;
import com.teamgannon.trips.stellarmodelling.StarUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * A relational data model for astrographic objects.
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
        @Index(columnList = "exoplanets"),
        @Index(columnList = "solarSystemId"),
        // Composite indexes for common query patterns
        @Index(name = "idx_star_dataset_coords", columnList = "dataSetName, x, y, z"),
        @Index(name = "idx_star_dataset_distance", columnList = "dataSetName, distance")
})
public class StarObject implements Serializable {

    public static final String SIMBAD_NO_ID = "UNDEFINED";
    public static final String SIMBAD_NO_TYPE = "UNDEFINED";
    public static final String POLITY_NOT_SET = "NOT+SET";

    @Serial
    private static final long serialVersionUID = -751366073413071183L;

    // ==================== Identity Fields ====================

    @Id
    private String id;

    /** The dataset name which we are guaranteeing to be unique */
    private String dataSetName = "";

    /** Name to use for display */
    private String displayName = "";

    /** The star's common name */
    private String commonName = "";

    /** Name of the system */
    private String systemName = "";

    /**
     * Foreign key reference to the SolarSystem entity this star belongs to.
     * Null if the star has not been associated with a detailed solar system model.
     */
    private String solarSystemId;

    /** The epoch of the star, normally J2000 but can be J2016 */
    private String epoch = "";

    /** Updated by external Gaia data files */
    private boolean gaiaUpdated = false;

    /** Update date from Gaia data files */
    private String gaiaUpdatedDate = "";

    /**
     * List of alias names.
     * Uses LAZY loading with BatchSize to avoid N+1 queries when aliases are accessed.
     * Access aliases within a transaction context to avoid LazyInitializationException.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @BatchSize(size = 100)
    private Set<String> aliasList = new HashSet<>();

    /** Name of the constellation that this is part of */
    private String constellationName = "";

    // ==================== Embedded Components ====================

    /** All catalog identifiers for this star */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "simbadId", column = @Column(name = "simbadId")),
            @AttributeOverride(name = "bayerCatId", column = @Column(name = "bayerCatId")),
            @AttributeOverride(name = "glieseCatId", column = @Column(name = "glieseCatId")),
            @AttributeOverride(name = "hipCatId", column = @Column(name = "hipCatId")),
            @AttributeOverride(name = "hdCatId", column = @Column(name = "hdCatId")),
            @AttributeOverride(name = "flamsteedCatId", column = @Column(name = "flamsteedCatId")),
            @AttributeOverride(name = "tycho2CatId", column = @Column(name = "tycho2CatId")),
            @AttributeOverride(name = "gaiaDR2CatId", column = @Column(name = "gaiaDR2CatId")),
            @AttributeOverride(name = "gaiaDR3CatId", column = @Column(name = "gaiaDR3CatId")),
            @AttributeOverride(name = "gaiaEDR3CatId", column = @Column(name = "gaiaEDR3CatId")),
            @AttributeOverride(name = "twoMassCatId", column = @Column(name = "twoMassCatId")),
            @AttributeOverride(name = "csiCatId", column = @Column(name = "csiCatId")),
            @AttributeOverride(name = "catalogIdList", column = @Column(name = "catalogIdList"))
    })
    private StarCatalogIds catalogIds = new StarCatalogIds();

    /** World-building and fiction attributes */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "polity", column = @Column(name = "polity")),
            @AttributeOverride(name = "worldType", column = @Column(name = "worldType")),
            @AttributeOverride(name = "fuelType", column = @Column(name = "fuelType")),
            @AttributeOverride(name = "portType", column = @Column(name = "portType")),
            @AttributeOverride(name = "populationType", column = @Column(name = "populationType")),
            @AttributeOverride(name = "techType", column = @Column(name = "techType")),
            @AttributeOverride(name = "productType", column = @Column(name = "productType")),
            @AttributeOverride(name = "milSpaceType", column = @Column(name = "milSpaceType")),
            @AttributeOverride(name = "milPlanType", column = @Column(name = "milPlanType")),
            @AttributeOverride(name = "other", column = @Column(name = "other")),
            @AttributeOverride(name = "anomaly", column = @Column(name = "anomaly"))
    })
    private StarWorldBuilding worldBuilding = new StarWorldBuilding();

    // ==================== Physical Properties ====================

    /** The collapsed mass value (solar masses) */
    private double mass = 0.0;

    /** The radius in solar multiples */
    private double radius = 0.0;

    /** The temperature of the star in Kelvin */
    private double temperature = 0.0;

    /** Age of the star */
    private double age = 0.0;

    /** Star metallicity */
    private double metallicity = 0.0;

    // ==================== Spectral Properties ====================

    /** Spectral class from Simbad */
    private String spectralClass = "";

    /** One character descriptor of spectralClass (derived) */
    private String orthoSpectralClass = "";

    /** Luminosity value */
    private String luminosity = "";

    /** Apparent magnitude */
    private String apparentMagnitude = "";

    /** Absolute magnitude */
    private String absoluteMagnitude = "";

    // ==================== Position & Coordinates ====================

    /** X position in light years (heliocentric, J2000) */
    @Column(name = "x")
    private double x = 0.0;

    /** Y position in light years */
    @Column(name = "y")
    private double y = 0.0;

    /** Z position in light years */
    @Column(name = "z")
    private double z = 0.0;

    /** Right ascension in degrees */
    private double ra = 0.0;

    /** Declination in degrees */
    private double declination = 0.0;

    /** Galactic latitude */
    private double galacticLat = 0.0;

    /** Galactic longitude */
    private double galacticLong = 0.0;

    /** Proper motion in RA direction (degrees) */
    private double pmra = 0.0;

    /** Proper motion in Dec direction (degrees) */
    private double pmdec = 0.0;

    /** Parallax measurement in milli-arc-seconds */
    private double parallax = 0.0;

    /** Distance in light years */
    private double distance = 0.0;

    /** Radial velocity from Sol in km/year */
    private double radialVelocity = 0.0;

    // ==================== Magnitude Bands ====================

    /** Gaia magnitude in bprp band (red) */
    private double bprp = 0.0;

    /** Gaia magnitude in bpg band (blue) */
    private double bpg = 0.0;

    /** Gaia magnitude in grp band (green) */
    private double grp = 0.0;

    /** Magnitude in U band */
    private double magu = 0.0;

    /** Magnitude in B band */
    private double magb = 0.0;

    /** Magnitude in V band */
    private double magv = 0.0;

    /** Magnitude in R band */
    private double magr = 0.0;

    /** Magnitude in I band */
    private double magi = 0.0;

    // ==================== Metadata ====================

    /** Free form text field for notes */
    @Lob
    private String notes = "";

    /** Source catalog system */
    @Lob
    private String source = "";

    /** Whether this is a real star or fictional */
    @NotNull
    private boolean realStar = true;

    /** Flag indicating if this system has exoplanets */
    private boolean exoplanets = false;

    /** Number of exoplanets */
    private int numExoplanets = 0;

    // ==================== Custom/Misc Fields ====================

    private String miscText1 = "";
    private String miscText2 = "";
    private String miscText3 = "";
    private String miscText4 = "";
    private String miscText5 = "";

    private double miscNum1 = 0.0;
    private double miscNum2 = 0.0;
    private double miscNum3 = 0.0;
    private double miscNum4 = 0.0;
    private double miscNum5 = 0.0;

    // ==================== Display Properties ====================

    /** Force the label to always be shown */
    private boolean forceLabelToBeShown = false;

    /** Computed heuristic for display label visibility */
    private double displayScore = 0.0;

    // ==================== Constructor ====================

    public StarObject() {
        init();
    }

    private void init() {
        id = UUID.randomUUID().toString();
        dataSetName = "not specified";
        realStar = true;
        displayName = "no name";
        source = "no source identified";
        epoch = "J2000";
        notes = "initial star file load";
        commonName = "";
        constellationName = "";

        catalogIds = new StarCatalogIds();
        catalogIds.initDefaults();

        worldBuilding = new StarWorldBuilding();
        worldBuilding.initDefaults();
    }

    // ==================== Coordinate Methods ====================

    @PrePersist
    @PreUpdate
    private void ensureCoordinates() {
        if (x == 0.0 && y == 0.0 && z == 0.0 && distance > 0) {
            double[] computed = com.teamgannon.trips.astrogation.Coordinates
                    .calculateEquatorialCoordinates(ra, declination, distance);
            x = computed[0];
            y = computed[1];
            z = computed[2];
        }
    }

    public double getX() {
        getCoordinates();
        return x;
    }

    public double getY() {
        getCoordinates();
        return y;
    }

    public double getZ() {
        getCoordinates();
        return z;
    }

    public double[] getCoordinates() {
        if (x == 0.0 && y == 0.0 && z == 0.0 && distance > 0) {
            double[] computed = com.teamgannon.trips.astrogation.Coordinates
                    .calculateEquatorialCoordinates(ra, declination, distance);
            x = computed[0];
            y = computed[1];
            z = computed[2];
        }
        return new double[]{x, y, z};
    }

    public void setCoordinates(double[] coordinates) {
        x = coordinates[0];
        y = coordinates[1];
        z = coordinates[2];
    }

    // ==================== Display Score ====================

    /**
     * Calculate the display score using the utility class.
     */
    public void calculateDisplayScore() {
        displayScore = DisplayScoreCalculator.calculate(this);
    }

    // ==================== Conversion Methods ====================

    public SimStar toSimStar() {
        StarCreator starCreator = new StarCreator();
        StarModel starModel;
        if (spectralClass != null && !spectralClass.contains("fictional")) {
            starModel = starCreator.parseSpectral(spectralClass);
        } else {
            starModel = starCreator.parseSpectral(orthoSpectralClass);
        }

        SimStar simStar = starModel.toSimStar();

        if (mass != 0) {
            simStar.setMass(StarUtils.relativeMass(mass));
        }
        if (radius != 0) {
            simStar.setRadius(StarUtils.relativeRadius(radius));
        }
        if (luminosity != null && !luminosity.isEmpty()) {
            try {
                simStar.setLuminosity(Double.parseDouble(luminosity));
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
        init();

        this.dataSetName = dataset.getName();
        this.realStar = true;
        this.displayName = chViewRecord.getStarName();
        this.constellationName = chViewRecord.getConstellation() != null ? chViewRecord.getConstellation() : "";
        this.mass = chViewRecord.getCollapsedMass();
        this.notes = chViewRecord.getComment() != null ? chViewRecord.getComment() : "";

        this.setCoordinates(chViewRecord.getOrdinates());
        this.setDistance(Double.parseDouble(chViewRecord.getDistanceToEarth()));
        this.setRadius(chViewRecord.getRadius());

        StarModel starModel = new StarCreator().parseSpectral(chViewRecord.getOrthoSpectra());
        if (starModel.getStellarClass() == null) {
            log.info("spectral class could not be verified, spectra = {} chv record = {}",
                    chViewRecord.getSpectra(), chViewRecord);
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
    }

    public SparseStarRecord toSparseStarRecord() {
        SparseStarRecord sparseStarRecord = new SparseStarRecord();
        sparseStarRecord.setRecordId(this.id);
        sparseStarRecord.setStarName(this.getDisplayName());
        sparseStarRecord.setActualCoordinates(getCoordinates());
        return sparseStarRecord;
    }

    // ==================== Backward-Compatible Accessors ====================
    // These delegate to embedded objects to maintain compatibility with existing code

    // --- Catalog ID Accessors ---

    public String getSimbadId() { return catalogIds.getSimbadId(); }
    public void setSimbadId(String simbadId) { catalogIds.setSimbadId(simbadId); }

    public String getBayerCatId() { return catalogIds.getBayerCatId(); }
    public void setBayerCatId(String bayerCatId) { catalogIds.setBayerCatId(bayerCatId); }

    public String getGlieseCatId() { return catalogIds.getGlieseCatId(); }
    public void setGlieseCatId(String glieseCatId) { catalogIds.setGlieseCatId(glieseCatId); }

    public String getHipCatId() { return catalogIds.getHipCatId(); }
    public void setHipCatId(String hipCatId) { catalogIds.setHipCatId(hipCatId); }

    public String getHdCatId() { return catalogIds.getHdCatId(); }
    public void setHdCatId(String hdCatId) { catalogIds.setHdCatId(hdCatId); }

    public String getFlamsteedCatId() { return catalogIds.getFlamsteedCatId(); }
    public void setFlamsteedCatId(String flamsteedCatId) { catalogIds.setFlamsteedCatId(flamsteedCatId); }

    public String getTycho2CatId() { return catalogIds.getTycho2CatId(); }
    public void setTycho2CatId(String tycho2CatId) { catalogIds.setTycho2CatId(tycho2CatId); }

    public String getGaiaDR2CatId() { return catalogIds.getGaiaDR2CatId(); }
    public void setGaiaDR2CatId(String gaiaDR2CatId) { catalogIds.setGaiaDR2CatId(gaiaDR2CatId); }

    public String getGaiaDR3CatId() { return catalogIds.getGaiaDR3CatId(); }
    public void setGaiaDR3CatId(String gaiaDR3CatId) { catalogIds.setGaiaDR3CatId(gaiaDR3CatId); }

    public String getGaiaEDR3CatId() { return catalogIds.getGaiaEDR3CatId(); }
    public void setGaiaEDR3CatId(String gaiaEDR3CatId) { catalogIds.setGaiaEDR3CatId(gaiaEDR3CatId); }

    public String getTwoMassCatId() { return catalogIds.getTwoMassCatId(); }
    public void setTwoMassCatId(String twoMassCatId) { catalogIds.setTwoMassCatId(twoMassCatId); }

    public String getCsiCatId() { return catalogIds.getCsiCatId(); }
    public void setCsiCatId(String csiCatId) { catalogIds.setCsiCatId(csiCatId); }

    public String getRawCatalogIdList() { return catalogIds.getRawCatalogIdList(); }

    public List<String> getCatalogIdList() { return catalogIds.getCatalogIdListParsed(); }

    public void setCatalogIdList(String catalogIdList) { catalogIds.setCatalogIdList(catalogIdList); }

    // --- World Building Accessors ---

    public String getPolity() { return worldBuilding.getPolity(); }
    public void setPolity(String polity) { worldBuilding.setPolity(polity); }

    public String getWorldType() { return worldBuilding.getWorldType(); }
    public void setWorldType(String worldType) { worldBuilding.setWorldType(worldType); }

    public String getFuelType() { return worldBuilding.getFuelType(); }
    public void setFuelType(String fuelType) { worldBuilding.setFuelType(fuelType); }

    public String getPortType() { return worldBuilding.getPortType(); }
    public void setPortType(String portType) { worldBuilding.setPortType(portType); }

    public String getPopulationType() { return worldBuilding.getPopulationType(); }
    public void setPopulationType(String populationType) { worldBuilding.setPopulationType(populationType); }

    public String getTechType() { return worldBuilding.getTechType(); }
    public void setTechType(String techType) { worldBuilding.setTechType(techType); }

    public String getProductType() { return worldBuilding.getProductType(); }
    public void setProductType(String productType) { worldBuilding.setProductType(productType); }

    public String getMilSpaceType() { return worldBuilding.getMilSpaceType(); }
    public void setMilSpaceType(String milSpaceType) { worldBuilding.setMilSpaceType(milSpaceType); }

    public String getMilPlanType() { return worldBuilding.getMilPlanType(); }
    public void setMilPlanType(String milPlanType) { worldBuilding.setMilPlanType(milPlanType); }

    public boolean isOther() { return worldBuilding.isOther(); }
    public void setOther(boolean other) { worldBuilding.setOther(other); }

    public boolean isAnomaly() { return worldBuilding.isAnomaly(); }
    public void setAnomaly(boolean anomaly) { worldBuilding.setAnomaly(anomaly); }

    // ==================== Equals & HashCode ====================

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
