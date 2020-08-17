package com.teamgannon.trips.dataset.factories;

import com.teamgannon.trips.dialogs.support.Dataset;
import com.teamgannon.trips.filedata.model.ChViewRecord;
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
     *
     * @param dataset the data set
     * @param chViewRecord the chview record
     * @return the astrographic object
     */
    public static AstrographicObject create(Dataset dataset, ChViewRecord chViewRecord) {
        AstrographicObject astrographicObject = new AstrographicObject();

        astrographicObject.setId(UUID.randomUUID());

        astrographicObject.setDataSetName(dataset.getName());

        astrographicObject.setRealStar(true);

        // the simbad id is not available from CH View
        astrographicObject.setSimbadId(AstrographicObject.SIMBAD_NO_ID);

        // this field is tied to the simbad id so we fill it out when we have it
        astrographicObject.setObjectType(AstrographicObject.SIMBAD_NO_TYPE);

        astrographicObject.setDisplayName(chViewRecord.getStarName());

        // since we are importing a real catalog then by default this cannot be fictional
        astrographicObject.setFictional(false);

        astrographicObject.setStarColor(chViewRecord.getStarColor());

        // mark that this is the first load of this object
        astrographicObject.setNotes("initial load of object");

//
//        astrographicObject.setChildren();
//        astrographicObject.setSiblings();
//        astrographicObject.setParentObject();

        astrographicObject.setCoordinates(chViewRecord.getOrdinates());
        astrographicObject.setDistance(Double.parseDouble(chViewRecord.getDistanceToEarth()));

        astrographicObject.setRadius(chViewRecord.getRadius());

        // ch view data records do not contain the RA and declination for objects
//        astrographicObject.setDeclination();
//        astrographicObject.setRaDec();

        astrographicObject.setSpectralClass(chViewRecord.getSpectra());


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
