package com.teamgannon.trips.solarsystem.modelling.accrete;

import lombok.Data;

@Data
public class AtmosphericChemical {
    private Chemical chem;
    private double surfacePressure = 0.0;

    public AtmosphericChemical(Chemical c, double s) {
        this.chem = c;
        this.surfacePressure = s;
    }
}