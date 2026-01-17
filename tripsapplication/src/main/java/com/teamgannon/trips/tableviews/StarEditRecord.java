package com.teamgannon.trips.tableviews;

import com.teamgannon.trips.jpa.model.StarObject;
import lombok.Data;
import org.jetbrains.annotations.NotNull;


@Data
public class StarEditRecord {

    private String id;

    private String displayName;

    private Double distanceToEarth;

    private String spectra;

    private Double radius;

    private Double mass;

    private String luminosity;

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

    public static @NotNull StarEditRecord fromAstrographicObject(@NotNull StarObject starObject) {
        StarEditRecord record = new StarEditRecord();
        record.setId(starObject.getId());
        record.setDisplayName(starObject.getDisplayName());
        record.setDistanceToEarth(starObject.getDistance());
        record.setSpectra(starObject.getSpectralClass());
        record.setRadius(starObject.getRadius());
        record.setMass(starObject.getMass());
        record.setLuminosity(starObject.getLuminosity());
        record.setRa(starObject.getRa());
        record.setDeclination(starObject.getDeclination());
        record.setParallax(starObject.getParallax());
        record.setXCoord(starObject.getX());
        record.setYCoord(starObject.getY());
        record.setZCoord(starObject.getZ());
        record.setReal(starObject.isRealStar());
        record.setComment(starObject.getNotes());
        return record;
    }
}
