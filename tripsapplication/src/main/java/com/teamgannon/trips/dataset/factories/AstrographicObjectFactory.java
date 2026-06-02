package com.teamgannon.trips.dataset.factories;

import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.file.chview.ChViewRecord;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.stellarmodelling.StarMassNormalizer;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

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
    public static @NotNull StarObject create(@NotNull Dataset dataset, @NotNull ChViewRecord chViewRecord) {
        StarObject starObject = new StarObject();

        starObject.setId(UUID.randomUUID().toString());

        starObject.setDataSetName(dataset.getName());

        starObject.setRealStar(true);

        starObject.setDisplayName(chViewRecord.getStarName());

        if (chViewRecord.getConstellation() != null) {
            starObject.setConstellationName(chViewRecord.getConstellation());
        } else {
            starObject.setConstellationName("none specified");
        }



        // ChView records carry mass in kg; canonical column is solar masses (Phase 1.1).
        starObject.setMass(StarMassNormalizer.toSolarMasses(chViewRecord.getCollapsedMass()));

        // mark that this is the first load of this object
        if (chViewRecord.getComment() != null) {
            starObject.setNotes(chViewRecord.getComment());
        } else {
            starObject.setNotes("none");
        }


        starObject.setCoordinates(chViewRecord.getOrdinates());
        starObject.setDistance(Double.parseDouble(chViewRecord.getDistanceToEarth()));

        starObject.setRadius(chViewRecord.getRadius());

        String spectra = chViewRecord.getSpectra();
        if (spectra == null || spectra.isEmpty()) {
            starObject.setSpectralClass("X");
            starObject.setOrthoSpectralClass("X");
        } else {
            starObject.setSpectralClass(spectra);
            starObject.setOrthoSpectralClass(spectra.substring(0, 1));
        }

        // ChView group-number → polity switch removed by the Worldbuilding Data Model
        // Normalization task. F.3 will reintroduce via FactionAssignment auto-seeding in
        // the Caine Riordan universe based on the same group-number mapping.
        starObject.setSource("CHView");

        return starObject;
    }


    public static double @NotNull [] setColor(@NotNull Color color) {
        double[] colors = new double[3];
        colors[0] = color.getRed();
        colors[1] = color.getGreen();
        colors[2] = color.getBlue();
        return colors;
    }


}
