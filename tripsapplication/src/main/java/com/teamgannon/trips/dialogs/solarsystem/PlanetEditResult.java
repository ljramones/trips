package com.teamgannon.trips.dialogs.solarsystem;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import lombok.Data;

/**
 * Result object returned from PlanetPropertiesDialog.
 * Contains the edited planet and flags indicating what changed.
 */
@Data
public class PlanetEditResult {

    private ExoPlanet planet;
    private boolean changed;
    private boolean orbitalChanged;  // If true, triggers visual redraw

    public PlanetEditResult(ExoPlanet planet, boolean changed, boolean orbitalChanged) {
        this.planet = planet;
        this.changed = changed;
        this.orbitalChanged = orbitalChanged;
    }

    public static PlanetEditResult unchanged(ExoPlanet planet) {
        return new PlanetEditResult(planet, false, false);
    }

    public static PlanetEditResult changed(ExoPlanet planet, boolean orbitalChanged) {
        return new PlanetEditResult(planet, true, orbitalChanged);
    }
}
