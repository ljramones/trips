package com.teamgannon.trips.screenobjects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Plain mutable view-model for the {@code StarEditDialog} (Issue 23 /
 * Phase 7-follow-on).
 * <p>
 * Replaces the legacy "pass the live JPA {@link com.teamgannon.trips.jpa.model.StarObject}
 * straight into the dialog" pattern. The motivation per Issue 23: when the
 * dialog binds form fields directly to a JPA entity, lazy collections
 * (notably {@code aliasList}) throw {@link org.hibernate.LazyInitializationException}
 * the moment the entity leaves its loading transaction — which on the FX
 * thread is always.
 * <p>
 * This class is a deliberately-flat POJO. The mapper at
 * {@link StarEditMapper} converts between this and {@code StarObject},
 * performing the lazy-initialise work inside a transactional service so
 * the FX dialog only ever sees fully-materialised data. The dialog mutates
 * this object freely; the service applies the changes back on submit.
 *
 * <h2>Field grouping</h2>
 * Mirrors the dialog's tabs: overview / secondary / fictional / display.
 *
 * <h2>What's NOT here</h2>
 * Read-only display-only fields (display score, computed coordinates,
 * etc.) stay on the entity — the view-model only carries what the
 * dialog allows the user to mutate.
 */
@Getter
@Setter
@ToString
public class StarEditViewModel {

    // ==================== Identity (read-only in the dialog) ====================

    /** Entity primary key. Carried for the apply-back round trip; not editable. */
    private String id;
    /** Dataset name. Read-only in the dialog. */
    private String dataSetName;

    // ==================== Overview tab ====================

    private String displayName = "";
    private String commonName = "";
    private String constellationName = "";
    private String spectralClass = "";
    private double distance;
    private double metallicity;
    private double age;
    private double x;
    private double y;
    private double z;
    private String notes = "";

    // ==================== Secondary / scientific tab ====================

    private String simbadId = "";
    private double galacticLat;
    private double galacticLong;
    private double radius;
    private double mass;
    private String luminosity = "";
    private double temperature;
    private double ra;
    private double declination;
    private double pmra;
    private double pmdec;
    private double parallax;
    private double radialVelocity;
    private double bprp;
    private double bpg;
    private double grp;
    private double magu;
    private double magb;
    private double magv;
    private double magr;
    private double magi;
    private String gaiaDR2CatId = "";

    /**
     * Aliases as a fully-materialised list. The mapper initialises this
     * inside a transaction; the dialog can read/write without LIE risk.
     */
    private List<String> aliases = new ArrayList<>();

    // ==================== Fictional / sci-fi tab ====================

    private String polity = "";
    private String worldType = "";
    private String fuelType = "";
    private String techType = "";
    private String portType = "";
    private String populationType = "";
    private String productType = "";
    private String milSpaceType = "";
    private String milPlanType = "";

    // ==================== Display tab ====================

    private boolean forceLabelToBeShown;
    private boolean anomaly;
    private boolean other;
}
