package com.teamgannon.trips.dataset.factories;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.file.chview.ChViewRecord;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import javafx.scene.paint.Color;

import java.util.UUID;

/**
 * This factory us used to create AstrographicObject objects from various formats
 * <p>
 * Created by larrymitchell on 2017-03-29.
 */
public class AstrographicObjectFactory {

    /**
     * create an astrographic object from a CH View record
     *
     * @param dataset      the data set
     * @param chViewRecord the chview record
     * @return the astrographic object
     */
    public static AstrographicObject create(Dataset dataset, ChViewRecord chViewRecord) {
        AstrographicObject astrographicObject = new AstrographicObject();

        astrographicObject.setId(UUID.randomUUID());

        astrographicObject.setDataSetName(dataset.getName());

        astrographicObject.setRealStar(true);

        astrographicObject.setDisplayName(chViewRecord.getStarName());

        astrographicObject.setStarColor(chViewRecord.getStarColor());

        // mark that this is the first load of this object
        astrographicObject.setNotes("initial load of object for CHView file");


        astrographicObject.setCoordinates(chViewRecord.getOrdinates());
        astrographicObject.setDistance(Double.parseDouble(chViewRecord.getDistanceToEarth()));

        astrographicObject.setRadius(chViewRecord.getRadius());

        // ch view data records do not contain the RA and declination for objects

        astrographicObject.setSpectralClass(chViewRecord.getSpectra());
        astrographicObject.setOrthoSpectralClass(chViewRecord.getSpectra().substring(0, 1));

        astrographicObject.setSource("CHView");

        return astrographicObject;
    }


    public static double[] setColor(Color color) {
        double[] colors = new double[3];
        colors[0] = color.getRed();
        colors[1] = color.getGreen();
        colors[2] = color.getBlue();
        return colors;
    }


}
