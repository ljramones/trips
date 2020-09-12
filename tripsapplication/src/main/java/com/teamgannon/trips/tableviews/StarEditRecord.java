package com.teamgannon.trips.tableviews;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.AstrographicObject;
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

    public static StarEditRecord fromAstrographicObject(AstrographicObject astrographicObject) {
        StarEditRecord record = new StarEditRecord();
        record.setId(astrographicObject.getId());
        record.setDisplayName(astrographicObject.getDisplayName());
        record.setDistanceToEarth(astrographicObject.getDistance());
        record.setSpectra(astrographicObject.getSpectralClass());
        record.setRadius(astrographicObject.getRadius());
        record.setRa(astrographicObject.getRa());
        record.setDeclination(astrographicObject.getDeclination());
        record.setParallax(astrographicObject.getParallax());
        record.setXCoord(astrographicObject.getX());
        record.setYCoord(astrographicObject.getY());
        record.setZCoord(astrographicObject.getZ());
        record.setReal(astrographicObject.isRealStar());
        record.setComment(astrographicObject.getNotes());
        return record;
    }
}
