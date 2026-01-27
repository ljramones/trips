package com.teamgannon.trips.jpa.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;

/**
 * Embeddable component containing world-building and science fiction
 * attributes for a star system. These fields are used for fiction writing
 * and game scenarios.
 */
@Getter
@Setter
@Embeddable
public class StarWorldBuilding implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * What polity does this object belong to.
     * Must be null or one of the polities listed in the theme.
     */
    private String polity = "NA";

    /**
     * The type of world
     */
    private String worldType = "NA";

    /**
     * The type of fuel available
     */
    private String fuelType = "NA";

    /**
     * The type of port/starport
     */
    private String portType = "NA";

    /**
     * The type of population
     */
    private String populationType = "NA";

    /**
     * The technology level
     */
    private String techType = "NA";

    /**
     * The primary product type
     */
    private String productType = "NA";

    /**
     * The type of military presence in space
     */
    private String milSpaceType = "NA";

    /**
     * The type of military presence on the planet
     */
    private String milPlanType = "NA";

    /**
     * Generic marker that means this is marked by other (user defined)
     */
    private boolean other = false;

    /**
     * Flag for whether there is an anomaly
     */
    private boolean anomaly = false;

    /**
     * Check if any fictional/world-building fields have been set.
     *
     * @return true if any world-building field has a non-default value
     */
    public boolean hasAnyFieldsSet() {
        if (isFieldSet(polity)) return true;
        if (isFieldSet(worldType)) return true;
        if (isFieldSet(fuelType)) return true;
        if (isFieldSet(portType)) return true;
        if (isFieldSet(populationType)) return true;
        if (isFieldSet(techType)) return true;
        if (isFieldSet(productType)) return true;
        if (isFieldSet(milSpaceType)) return true;
        if (isFieldSet(milPlanType)) return true;
        return other || anomaly;
    }

    /**
     * Check if a string field is set (not null, not empty, not "NA").
     */
    private boolean isFieldSet(String value) {
        return value != null && !value.trim().isEmpty() && !"NA".equals(value);
    }

    /**
     * Initialize all fields to default values.
     */
    public void initDefaults() {
        polity = "NA";
        worldType = "NA";
        fuelType = "NA";
        portType = "NA";
        populationType = "NA";
        techType = "NA";
        productType = "NA";
        milSpaceType = "NA";
        milPlanType = "NA";
        other = false;
        anomaly = false;
    }
}
