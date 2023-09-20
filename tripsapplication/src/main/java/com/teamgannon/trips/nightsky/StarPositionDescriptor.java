package com.teamgannon.trips.nightsky;

import lombok.Data;

@Data
public class StarPositionDescriptor {

    private double ra;       // Right Ascension in degrees
    private double dec;      // Declination in degrees
    private double distance; // Distance from Earth in parsecs

    public StarPositionDescriptor(double ra, double dec, double distance) {
        this.ra = ra;
        this.dec = dec;
        this.distance = distance;
    }

}
