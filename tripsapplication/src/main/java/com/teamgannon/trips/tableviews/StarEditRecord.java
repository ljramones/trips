package com.teamgannon.trips.tableviews;

import lombok.Data;

import java.util.UUID;

@Data
public class StarEditRecord {

    private UUID id;

    private String displayName;

    private Double distanceToEarth;

    private String spectra;

    private Double radius;

    private Double ra;
    private Double declination;
    private Double parallax;

    private Double xCoord;

    private Double yCoord;

    private Double zCoord;

    private boolean real;

    private String comment;

    /**
     * this means that the data was changed and should be updated
     */
    private boolean dirty;

}
