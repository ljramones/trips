package com.teamgannon.trips.jpa.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * The definition of a star
 * <p>
 * Created by larrymitchell on 2017-01-24.
 */
@Data
@Entity
public class Star implements Serializable {

    private static final long serialVersionUID = 4926100734682334476L;
    /**
     * this flag means that the star was actually generated and not real
     */
    boolean generated = false;
    @Id
    private String id;
    /**
     * we track which system that this belongs to
     */
    private String stellarSystemId;
    /**
     * id form imported data
     */
    private long catalogId;
    /**
     * hip: The star's ID in the Hipparcos catalog, if known
     */
    private String hipparcosId;
    /**
     * hd: The star's ID in the Henry Draper catalog, if known.
     */
    private String henryDraperId;
    /**
     * hr: The star's ID in the Harvard Revised catalog, which is the same as its number in the Yale Bright Star
     * Catalog.
     */
    private String harvardRevisedId;
    /**
     * gl: The star's ID in the third edition of the Gliese Catalog of Nearby Stars.
     */
    private String glieseId;
    /**
     * the SAO id of this entry
     */
    private String saoId;
    /**
     * Simbad id
     */
    private String simbadId;
    /**
     * bf: The Bayer / Flamsteed designation, primarily from the Fifth Edition of the Yale Bright Star Catalog.
     * This is a combination of the two designations. The Flamsteed number, if present, is given first; then a
     * three-letter abbreviation for the Bayer Greek letter; the Bayer superscript number, if present; and finally,
     * the three-letter constellation abbreviation. Thus Alpha Andromedae has the field value "21Alp And", and
     * Kappa1 Sculptoris (no Flamsteed number) has "Kap1Scl".
     */
    private String bayerFlamsteed;
    /**
     * raDec, decDec: The star's right ascension and declination, for epoch and equinox 2000.0.
     */
    private double ra, dec;
    /**
     * proper: A common name for the star, such as "Barnard's Star" or "Sirius". I have taken these names
     * primarily from the Hipparcos project's web site, which lists representative names for the 150 brightest
     * stars and many of the 150 closest stars. I have added a few names to this list. Most of the additions are
     * designations fromcatalogs mostly now forgotten (e.g., Lalande, Groombridge, and Gould ["G."]) except for
     * certain nearby starswhich are still best known by these designations.
     */
    private String commonName;
    /**
     * dist: The star's distance in parsecs, the most common unit in astrometry. To convert parsecs to light years,
     * multiply by 3.262. A value >= 10000000 indicates missing or dubious (e.g., negative) parallax data in Hipparcos.
     */
    private double distance;
    /**
     * pmra, pmdec: The star's proper motion in right ascension and declination, in milliarcseconds per year.
     */
    private double properMotionRightAscension, properMotionDeclination;
    /**
     * rv: The star's radial velocity in km/sec, where known
     */
    private double radialVelocity;
    /**
     * mag: The star's apparent visual magnitude.
     */
    private double magnitude;
    /**
     * absmag: The star's absolute visual magnitude (its apparent magnitude from a distance of 10 parsecs).
     */
    private double absoluteVisualMagnitude;
    /**
     * spect: The star's spectral type, if known.
     */
    private String spectralType;
    /**
     * ci: The star's color index (blue magnitude - visual magnitude), where known.
     */
    private String colorIndex;
    /**
     * x,y,z: The Cartesian coordinates of the star, in a system based on the equatorial coordinates as seen from Earth.
     * +X is in the direction of the vernal equinox (at epoch 2000), +Z towards the north celestial pole, and +Y in the
     * direction of R.A. 6 hours, declination 0 degrees.
     */
    private double x, y, z;
    /**
     * vx,vy,vz: The Cartesian velocity components of the star, in the same coordinate system described immediately
     * above.  They are determined from the proper motion and the radial velocity (when known). The velocity unit
     * is parsecs per year; these are small values (around 1 millionth of a parsec per year), but they enormously
     * simplify calculations using parsecs as base units for celestial mapping.
     */
    private double xVelocity, yVelocity, zVelocity;
    /**
     * rarad, decrad, pmrarad, prdecrad: The positions in radians, and proper motions in radians per year.
     */
    private double rarad, decrad, pmrarad, prdecrad;
    /**
     * bayer: The Bayer designation as a distinct value
     */
    private String bayer;
    /**
     * flam: The Flamsteed number as a distinct value
     */
    private String flam;
    /**
     * con: The standard constellation abbreviation
     */
    private String constellation;
    /**
     * comp, comp_primary, base: Identifies a star in a multiple star system. comp = ID of companion star,
     * comp_primary = ID of primary star for this component, and base = catalog ID or name for this multi-star system.
     */
    private String comp, comp_primary, base;
    /**
     * Currently only used for Gliese stars.
     * lum: Star's luminosity as a multiple of Solar luminosity.
     * var: Star's standard variable star designation, when known.
     * var_min, var_max: Star's approximate magnitude range, for variables. This value is based on the Hp magnitudes
     * for the range in the original Hipparcos catalog, adjusted to the V magnitude scale to match the "mag" field.
     */
    private String lum, var, var_min, var_max;
    /**
     * a wikipedia link to the star
     */
    private String wikipediaURL;

}
