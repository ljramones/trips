package com.teamgannon.trips.spaceshipmodeller.ui;

import com.terranrepublic.assets.SpaceshipDesign;

/**
 * Read-only JavaBean view of a {@link SpaceshipDesign} for use in a JavaFX {@code TableView}.
 * <p>
 * The TRIPS tables use {@code PropertyValueFactory}, which reflects on JavaBean getters; this wrapper
 * exposes the flat, display-ready properties (name, class label, drive, category, mass, crew) while
 * keeping a reference to the underlying immutable design via {@link #getDesign()}.
 */
public class SpaceshipRow {

    private final SpaceshipDesign design;

    public SpaceshipRow(SpaceshipDesign design) {
        this.design = design;
    }

    /** @return the underlying immutable design */
    public SpaceshipDesign getDesign() {
        return design;
    }

    public String getName() {
        return design.name();
    }

    public String getDesignation() {
        return design.designation();
    }

    public String getShipClass() {
        return design.shipClass().label();
    }

    public String getDriveType() {
        return design.driveType().name();
    }

    public String getCategory() {
        return design.driveType().category().label();
    }

    public double getMass() {
        return design.massBudget().wetMassTons();
    }

    public int getCrew() {
        return design.crewComplement();
    }

    /**
     * @return estimated delta-V in km/s as a raw number, so the table column sorts numerically
     * (NaN for reactionless drives, which sorts to the end)
     */
    public double getDeltaVValue() {
        return design.estimateDeltaVKmps();
    }

    /** @return "Yes" if this design is acting as a mothership, otherwise "No" */
    public String getMothership() {
        return design.isMothership() ? "Yes" : "No";
    }

    /** @return source universe / franchise, or an em dash when not set */
    public String getUniverse() {
        return design.sourceUniverse().isBlank() ? "—" : design.sourceUniverse();
    }

    /** @return in-universe faction / operator */
    public String getFaction() {
        return design.faction();
    }

    /** @return era / timeframe */
    public String getEra() {
        return design.era();
    }
}
