package com.teamgannon.trips.dataset.factories;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.file.chview.ChViewRecord;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
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

        if (chViewRecord.getConstellation() != null) {
            astrographicObject.setConstellationName(chViewRecord.getConstellation());
        } else {
            astrographicObject.setConstellationName("none specified");
        }

        // set the collapsed mass
        astrographicObject.setMass(chViewRecord.getCollapsedMass());

        // get the actual mass from the collapsed mass
        astrographicObject.setActualMass(chViewRecord.getUncollapsedMass());

        // mark that this is the first load of this object
        if (chViewRecord.getComment() != null) {
            astrographicObject.setNotes(chViewRecord.getComment());
        } else {
            astrographicObject.setNotes("none");
        }


        astrographicObject.setCoordinates(chViewRecord.getOrdinates());
        astrographicObject.setDistance(Double.parseDouble(chViewRecord.getDistanceToEarth()));

        astrographicObject.setRadius(chViewRecord.getRadius());

        astrographicObject.setSpectralClass(chViewRecord.getSpectra());
        astrographicObject.setOrthoSpectralClass(chViewRecord.getSpectra().substring(0, 1));

        switch (chViewRecord.getGroupNumber()) {
            case 1 -> astrographicObject.setPolity(CivilizationDisplayPreferences.ARAKUR);
            case 2 -> astrographicObject.setPolity(CivilizationDisplayPreferences.HKHRKH);
            case 4 -> astrographicObject.setPolity(CivilizationDisplayPreferences.KTOR);
            case 8 -> astrographicObject.setPolity(CivilizationDisplayPreferences.TERRAN);
        }
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
